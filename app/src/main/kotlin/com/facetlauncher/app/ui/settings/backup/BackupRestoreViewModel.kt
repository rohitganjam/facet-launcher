package com.facetlauncher.app.ui.settings.backup

import android.content.ComponentName
import android.net.Uri
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.ExportBackupUseCase
import com.facetlauncher.app.domain.ImportBackupResult
import com.facetlauncher.app.domain.ImportBackupUseCase
import com.facetlauncher.app.domain.PlaceWidgetResult
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import com.facetlauncher.app.domain.toEnumOrDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/**
 * F14 Backup & Restore — export/import orchestration, plus the guided widget re-add queue that
 * follows a successful import (kept in this same ViewModel/screen rather than a separate `NavHost`
 * destination, since carrying [BackupWidgetPlacement]s across a nav-argument boundary has no clean
 * answer here — mirrors `HomeDrawerRoute`'s own reasoning for keeping its widget picker an
 * in-place overlay instead of a destination). The re-add flow itself (allocate -> bind -> maybe
 * configure -> place) mirrors `HubWidgetPickerViewModel` exactly, the one difference being the
 * target provider is fixed per row (from the backup) rather than user-chosen from a catalog, and
 * the desired grid position/span is already known (the backup's own recorded [BackupWidgetPlacement.row]/
 * [BackupWidgetPlacement.col]/span) rather than derived from the provider's declared minimum size.
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase,
    private val appRepository: AppRepository,
    private val appWidgetRepository: AppWidgetRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val placeWidget: PlaceWidgetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BackupRestoreEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<BackupRestoreEvent> = _events.asSharedFlow()

    /** The one re-add in flight, if any — carried across the bind/configure round trips back from the composable. */
    private var pendingAppWidgetId: Int? = null
    private var pendingIndex: Int? = null

    fun onExportDestinationChosen(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = runCatching { exportBackup(uri) }
            _uiState.update {
                it.copy(isBusy = false, message = if (result.isSuccess) BackupRestoreMessage.ExportSucceeded else BackupRestoreMessage.ExportFailed)
            }
        }
    }

    /** The caller (screen) must confirm the destructive-replace with the user before ever launching the file picker that leads here. */
    fun onImportFileChosen(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            when (val result = importBackup(uri)) {
                is ImportBackupResult.Success -> {
                    val options = appWidgetRepository.getWidgetProviderOptions()
                    val pending = result.pendingWidgetPlacements.map { placement ->
                        val match = options.firstOrNull {
                            it.provider.packageName == placement.providerPackageName && it.provider.className == placement.providerClassName
                        }
                        PendingWidgetUi(
                            placement = placement,
                            providerAvailable = match != null,
                            appLabel = match?.appLabel,
                            widgetLabel = match?.widgetLabel,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            message = BackupRestoreMessage.ImportSucceeded(result.facetCount, pending.count { p -> !p.done }),
                            pendingWidgets = pending,
                        )
                    }
                }
                ImportBackupResult.InvalidFile -> _uiState.update { it.copy(isBusy = false, message = BackupRestoreMessage.ImportFailedInvalidFile) }
                is ImportBackupResult.UnsupportedVersion ->
                    _uiState.update { it.copy(isBusy = false, message = BackupRestoreMessage.ImportFailedUnsupportedVersion(result.backupVersion)) }
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun onSkipWidget(index: Int) {
        _uiState.update { state -> state.withPendingMarkedDone(index) }
    }

    fun onReimportWidget(index: Int) {
        val pending = _uiState.value.pendingWidgets.getOrNull(index) ?: return
        if (!pending.providerAvailable || pending.done) return
        viewModelScope.launch {
            val provider = ComponentName(pending.placement.providerPackageName, pending.placement.providerClassName)
            // The backup's own recorded profile is only a hint for this first attempt — a Work
            // Profile widget's bind grant can't survive a backup/restore round trip regardless, so
            // this always goes through a real bind either way (see BackupWidgetPlacement's own doc).
            // A backup only ever recorded a display category, never a real UserHandle, so this is
            // the sanctioned single-representative lookup (see AppRepository.resolveUserHandle's
            // own doc) — null means that category no longer has a live match (e.g. the Work
            // Profile from the backup's source device isn't this device's), so this widget simply
            // can't be restored; marked done rather than left stuck.
            val profile = pending.placement.profile.toEnumOrDefault(AppProfile.PERSONAL)
            val handle = appRepository.resolveUserHandle(profile)
            if (handle == null) {
                _uiState.update { it.withPendingMarkedDone(index) }
                return@launch
            }
            val appWidgetId = appWidgetRepository.allocateAppWidgetId()
            pendingIndex = index
            if (!appWidgetRepository.bindAppWidgetIdIfAllowed(appWidgetId, provider, handle)) {
                pendingAppWidgetId = appWidgetId
                _events.emit(BackupRestoreEvent.LaunchBindPermission(appWidgetRepository.createBindIntent(appWidgetId, provider, handle)))
                return@launch
            }
            proceedAfterBind(appWidgetId)
        }
    }

    /** The composable's bind-permission launcher result. */
    fun onBindResult(granted: Boolean) {
        val appWidgetId = pendingAppWidgetId ?: return
        pendingAppWidgetId = null
        viewModelScope.launch {
            if (!granted) {
                failAndRelease(appWidgetId)
                return@launch
            }
            proceedAfterBind(appWidgetId)
        }
    }

    /** The composable's configure-activity launcher result. */
    fun onConfigureResult(resultOk: Boolean) {
        val appWidgetId = pendingAppWidgetId ?: return
        pendingAppWidgetId = null
        viewModelScope.launch {
            if (!resultOk) {
                failAndRelease(appWidgetId)
                return@launch
            }
            finishPlacing(appWidgetId)
        }
    }

    private suspend fun proceedAfterBind(appWidgetId: Int) {
        val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            failAndRelease(appWidgetId)
            return
        }
        val configureIntentSender = appWidgetRepository.createConfigureIntentSender(appWidgetId, info)
        if (configureIntentSender != null) {
            pendingAppWidgetId = appWidgetId
            _events.emit(BackupRestoreEvent.LaunchConfigure(configureIntentSender))
            return
        }
        finishPlacing(appWidgetId)
    }

    private suspend fun finishPlacing(appWidgetId: Int) {
        val index = pendingIndex
        val pending = index?.let { _uiState.value.pendingWidgets.getOrNull(it) }
        if (index == null || pending == null) {
            appWidgetRepository.deleteAppWidgetId(appWidgetId)
            return
        }
        val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            failAndRelease(appWidgetId)
            return
        }
        val placement = pending.placement
        val existing = widgetPlacementRepository.observeAll().first()
        when (
            val result = placeWidget(existing, placement.colSpan, placement.rowSpan, preferredRow = placement.row, preferredCol = placement.col)
        ) {
            is PlaceWidgetResult.Placed -> {
                widgetPlacementRepository.upsert(
                    WidgetPlacementEntity(
                        appWidgetId = appWidgetId,
                        providerPackageName = info.provider.packageName,
                        providerClassName = info.provider.className,
                        row = result.row,
                        col = result.col,
                        colSpan = placement.colSpan,
                        rowSpan = placement.rowSpan,
                        profile = appWidgetRepository.profileForWidget(appWidgetId),
                        userId = appWidgetRepository.userIdForWidget(appWidgetId),
                    ),
                )
                pendingIndex = null
                _uiState.update { it.withPendingMarkedDone(index) }
            }
            PlaceWidgetResult.HubFull -> failAndRelease(appWidgetId)
        }
    }

    private suspend fun failAndRelease(appWidgetId: Int) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        pendingIndex = null
    }

    private fun BackupRestoreUiState.withPendingMarkedDone(index: Int): BackupRestoreUiState =
        copy(pendingWidgets = pendingWidgets.mapIndexed { i, widget -> if (i == index) widget.copy(done = true) else widget })
}
