package com.facetlauncher.app.ui.home.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.ui.hub.picker.AddFailureReason
import com.facetlauncher.app.ui.hub.picker.HubAddWidgetEvent
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerUiState
import com.facetlauncher.app.ui.hub.picker.WidgetProviderGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * PRD F15's "Use custom widget" flow — allocate -> bind (possibly detouring through the system's
 * own bind-permission dialog) -> configure (possibly detouring through the provider's own
 * configure activity) -> bind as the target facet's clock. Mirrors
 * [com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel]'s own orchestration shape
 * (reusing its [HubAddWidgetEvent]/[HubWidgetPickerUiState]/[WidgetProviderGroup] types directly,
 * since the Activity-result-detour shape is identical) but finishes differently: no grid
 * placement — this facet has exactly one clock slot, not up to
 * [com.facetlauncher.app.domain.HUB_MAX_WIDGETS] — so [HubWidgetPickerUiState.remaining] is always
 * `null` here (hides the picker's "N left" counter) and the finish step writes straight to
 * [FacetRepository] instead of [com.facetlauncher.app.data.WidgetPlacementRepository].
 */
@HiltViewModel
class ClockWidgetPickerViewModel @Inject constructor(
    private val appWidgetRepository: AppWidgetRepository,
    private val facetRepository: FacetRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val allOptions = MutableStateFlow<List<WidgetProviderOption>?>(null)
    private val _events = MutableSharedFlow<HubAddWidgetEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HubAddWidgetEvent> = _events.asSharedFlow()

    /** The one bind in flight, if any — carried across the bind/configure round trips back from the composable. */
    private var pendingAppWidgetId: Int? = null

    /** Which facet this bind is for — set when the flow starts, since it must not change mid-flow even if the user could otherwise switch facets. */
    private var pendingFacetId: Long? = null

    val uiState: StateFlow<HubWidgetPickerUiState> = combine(query, allOptions) { currentQuery, options ->
        val currentOptions = options ?: appWidgetRepository.getWidgetProviderOptions().also { allOptions.value = it }
        val filtered = if (currentQuery.isBlank()) {
            currentOptions
        } else {
            currentOptions.filter { it.appLabel.contains(currentQuery, ignoreCase = true) }
        }
        HubWidgetPickerUiState(
            query = currentQuery,
            groups = filtered.groupBy { it.appLabel }.map { (appLabel, options) -> WidgetProviderGroup(appLabel, options) },
            remaining = null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubWidgetPickerUiState(remaining = null))

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /** Called when the picker is dismissed, so a fresh open starts clean — mirrors [com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel.reset]. */
    fun reset() {
        query.value = ""
        allOptions.value = null
    }

    fun onProviderSelect(option: WidgetProviderOption, facetId: Long) {
        viewModelScope.launch {
            pendingFacetId = facetId
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
            finishBinding(appWidgetId)
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
        finishBinding(appWidgetId)
    }

    /** Releases this facet's previously-bound clock widget id (if any) before storing the new one — never leaves the old id leaked. */
    private suspend fun finishBinding(appWidgetId: Int) {
        val facetId = pendingFacetId
        val facet = facetId?.let { facetRepository.getById(it) }
        if (facet == null) {
            failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
            return
        }
        facet.clockWidgetAppWidgetId?.let { appWidgetRepository.deleteAppWidgetId(it) }
        facetRepository.setClockWidgetAppWidgetId(facet, appWidgetId)
        _events.emit(HubAddWidgetEvent.WidgetAdded)
    }

    private suspend fun failAndRelease(appWidgetId: Int, reason: AddFailureReason) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        _events.emit(HubAddWidgetEvent.AddFailed(reason))
    }
}
