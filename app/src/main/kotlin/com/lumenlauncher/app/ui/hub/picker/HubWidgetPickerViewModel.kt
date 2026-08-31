package com.lumenlauncher.app.ui.hub.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.model.WidgetProviderOption
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.domain.HUB_MAX_WIDGETS
import com.lumenlauncher.app.domain.PlaceWidgetResult
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
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
    private val _events = MutableSharedFlow<HubAddWidgetEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HubAddWidgetEvent> = _events.asSharedFlow()

    /** The one add in flight, if any — carried across the bind/configure round trips back from the composable. */
    private var pendingAppWidgetId: Int? = null

    val uiState: StateFlow<HubWidgetPickerUiState> = combine(
        query,
        widgetPlacementRepository.observeAll(),
    ) { currentQuery, placements ->
        val allOptions = appWidgetRepository.getWidgetProviderOptions()
        val filtered = if (currentQuery.isBlank()) {
            allOptions
        } else {
            allOptions.filter { it.appLabel.contains(currentQuery, ignoreCase = true) || it.widgetLabel.contains(currentQuery, ignoreCase = true) }
        }
        HubWidgetPickerUiState(
            query = currentQuery,
            groups = filtered.groupBy { it.appLabel }.map { (appLabel, options) -> WidgetProviderGroup(appLabel, options) },
            remaining = (HUB_MAX_WIDGETS - placements.size).coerceAtLeast(0),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubWidgetPickerUiState())

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun onProviderSelected(option: WidgetProviderOption) {
        viewModelScope.launch {
            if (uiState.value.remaining <= 0) {
                _events.emit(HubAddWidgetEvent.AddFailed)
                return@launch
            }
            val appWidgetId = appWidgetRepository.allocateAppWidgetId()
            if (!appWidgetRepository.bindAppWidgetIdIfAllowed(appWidgetId, option.provider)) {
                pendingAppWidgetId = appWidgetId
                _events.emit(HubAddWidgetEvent.LaunchBindPermission(appWidgetRepository.createBindIntent(appWidgetId, option.provider)))
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
            val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
            if (info == null) {
                failAndRelease(appWidgetId)
                return@launch
            }
            finishPlacing(appWidgetId, info.provider.packageName, info.provider.className, info.minWidth, info.minHeight)
        }
    }

    private suspend fun proceedAfterBind(appWidgetId: Int) {
        val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            failAndRelease(appWidgetId)
            return
        }
        val configureIntent = appWidgetRepository.createConfigureIntent(appWidgetId, info)
        if (configureIntent != null) {
            pendingAppWidgetId = appWidgetId
            _events.emit(HubAddWidgetEvent.LaunchConfigure(configureIntent))
            return
        }
        finishPlacing(appWidgetId, info.provider.packageName, info.provider.className, info.minWidth, info.minHeight)
    }

    private suspend fun finishPlacing(appWidgetId: Int, providerPackageName: String, providerClassName: String, minWidthDp: Int, minHeightDp: Int) {
        val existing = widgetPlacementRepository.observeAll().first()
        val colSpan = ((minWidthDp + 30) / 70).coerceAtLeast(1)
        val rowSpan = ((minHeightDp + 30) / 70).coerceAtLeast(1)
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
                    ),
                )
                _events.emit(HubAddWidgetEvent.WidgetAdded)
            }
            PlaceWidgetResult.HubFull -> failAndRelease(appWidgetId)
        }
    }

    private suspend fun failAndRelease(appWidgetId: Int) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        _events.emit(HubAddWidgetEvent.AddFailed)
    }
}
