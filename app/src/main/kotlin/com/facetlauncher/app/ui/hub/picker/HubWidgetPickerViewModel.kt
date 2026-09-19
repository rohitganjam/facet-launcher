package com.facetlauncher.app.ui.hub.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.HUB_MAX_WIDGETS
import com.facetlauncher.app.domain.PlaceWidgetResult
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orchestrates the add-widget flow (README `4c`): allocate -> bind (possibly detouring through
 * the system's own bind-permission dialog) -> configure (possibly detouring through the
 * provider's own configure activity) -> place -> persist. Generalizes `CalendarSettingsScreen.kt`'s
 * direct-onClick `rememberLauncherForActivityResult` pattern into an event-driven one, since this
 * flow has two independent, sequential system-Activity detours rather than just one permission ask.
 */
@HiltViewModel
class HubWidgetPickerViewModel @Inject constructor(
    private val appWidgetRepository: AppWidgetRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val placeWidget: PlaceWidgetUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val allOptions = MutableStateFlow<List<WidgetProviderOption>?>(null)
    private val _events = MutableSharedFlow<HubAddWidgetEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HubAddWidgetEvent> = _events.asSharedFlow()

    /** The one add in flight, if any — carried across the bind/configure round trips back from the composable. */
    private var pendingAppWidgetId: Int? = null

    val uiState: StateFlow<HubWidgetPickerUiState> = combine(
        query,
        widgetPlacementRepository.observeAll(),
        allOptions,
    ) { currentQuery, placements, options ->
        val currentOptions = options ?: appWidgetRepository.getWidgetProviderOptions().also { allOptions.value = it }
        val filtered = if (currentQuery.isBlank()) {
            currentOptions
        } else {
            // Search only by app name, not widget name, per user request.
            currentOptions.filter { it.appLabel.contains(currentQuery, ignoreCase = true) }
        }
        HubWidgetPickerUiState(
            query = currentQuery,
            groups = filtered.groupBy { it.appLabel }.map { (appLabel, options) -> WidgetProviderGroup(appLabel, options) },
            remaining = (HUB_MAX_WIDGETS - placements.size).coerceAtLeast(0),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubWidgetPickerUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /**
     * Resets search query and cached widget options. Called when the user exits the hub screen
     * to ensure a fresh state on return.
     */
    fun reset() {
        query.value = ""
        allOptions.value = null
    }

    fun onProviderSelect(option: WidgetProviderOption) {
        viewModelScope.launch {
            if (uiState.value.remaining <= 0) {
                _events.emit(HubAddWidgetEvent.AddFailed(AddFailureReason.HUB_FULL))
                return@launch
            }
            val appWidgetId = appWidgetRepository.allocateAppWidgetId()
            if (!appWidgetRepository.bindAppWidgetIdIfAllowed(appWidgetId, option.provider, option.userHandle)) {
                pendingAppWidgetId = appWidgetId
                _events.emit(HubAddWidgetEvent.LaunchBindPermission(appWidgetRepository.createBindIntent(appWidgetId, option.provider, option.userHandle)))
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
                failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
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
                failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
                return@launch
            }
            val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
            if (info == null) {
                failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
                return@launch
            }
            finishPlacing(appWidgetId, info.provider.packageName, info.provider.className)
        }
    }

    private suspend fun proceedAfterBind(appWidgetId: Int) {
        val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
            return
        }
        val configureIntentSender = appWidgetRepository.createConfigureIntentSender(appWidgetId, info)
        if (configureIntentSender != null) {
            pendingAppWidgetId = appWidgetId
            _events.emit(HubAddWidgetEvent.LaunchConfigure(configureIntentSender))
            return
        }
        finishPlacing(appWidgetId, info.provider.packageName, info.provider.className)
    }

    private suspend fun finishPlacing(appWidgetId: Int, providerPackageName: String, providerClassName: String) {
        val existing = widgetPlacementRepository.observeAll().first()
        // Prefers the provider's declared targetCellWidth/Height (API 31+) over the legacy
        // minWidth/minHeight dp math — see AppWidgetRepository.defaultSpanFor.
        val (colSpan, rowSpan) = appWidgetRepository.defaultSpan(appWidgetId) ?: (1 to 1)
        when (val result = placeWidget(existing, colSpan, rowSpan)) {
            is PlaceWidgetResult.Placed -> {
                widgetPlacementRepository.upsert(
                    WidgetPlacementEntity(
                        appWidgetId = appWidgetId,
                        providerPackageName = providerPackageName,
                        providerClassName = providerClassName,
                        row = result.row,
                        col = result.col,
                        colSpan = colSpan,
                        rowSpan = rowSpan,
                        profile = appWidgetRepository.profileForWidget(appWidgetId),
                        userId = appWidgetRepository.userIdForWidget(appWidgetId),
                    ),
                )
                _events.emit(HubAddWidgetEvent.WidgetAdded)
            }
            PlaceWidgetResult.HubFull -> failAndRelease(appWidgetId, AddFailureReason.HUB_FULL)
        }
    }

    private suspend fun failAndRelease(appWidgetId: Int, reason: AddFailureReason) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        _events.emit(HubAddWidgetEvent.AddFailed(reason))
    }
}
