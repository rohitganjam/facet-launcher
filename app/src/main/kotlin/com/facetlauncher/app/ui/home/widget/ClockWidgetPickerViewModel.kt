package com.facetlauncher.app.ui.home.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.ReplaceClockStackItemUseCase
import com.facetlauncher.app.ui.hub.picker.AddFailureReason
import com.facetlauncher.app.ui.hub.picker.HubAddWidgetEvent
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerUiState
import com.facetlauncher.app.ui.hub.picker.toSortedGroups
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
 * configure activity) -> swap in for a stack page, or add to the stack. Mirrors
 * [com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel]'s own orchestration shape
 * (reusing its [HubAddWidgetEvent]/[HubWidgetPickerUiState]/[WidgetProviderGroup] types directly,
 * since the Activity-result-detour shape is identical) but finishes differently: no grid
 * placement — it replaces or adds one page of the facet's clock stack, not one of up to
 * [com.facetlauncher.app.domain.HUB_MAX_WIDGETS] grid widgets — so [HubWidgetPickerUiState.remaining] is always
 * `null` here (hides the picker's "N left" counter) and the finish step goes through
 * [ReplaceClockStackItemUseCase] or [AddClockStackItemUseCase] instead of [com.facetlauncher.app.data.WidgetPlacementRepository].
 */
@HiltViewModel
class ClockWidgetPickerViewModel @Inject constructor(
    private val appWidgetRepository: AppWidgetRepository,
    private val replaceClockStackItem: ReplaceClockStackItemUseCase,
    private val addClockStackItem: AddClockStackItemUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val allOptions = MutableStateFlow<List<WidgetProviderOption>?>(null)
    private val _events = MutableSharedFlow<HubAddWidgetEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HubAddWidgetEvent> = _events.asSharedFlow()

    /** The one bind in flight, if any — carried across the bind/configure round trips back from the composable. */
    private var pendingAppWidgetId: Int? = null

    /** What this bind is for: set when the flow starts, since it must not change mid-flow even if the user swipes to another page. */
    private var pendingTarget: ClockWidgetPickerTarget? = null

    val uiState: StateFlow<HubWidgetPickerUiState> = combine(query, allOptions) { currentQuery, options ->
        val currentOptions = options ?: appWidgetRepository.getWidgetProviderOptions().also { allOptions.value = it }
        val filtered = if (currentQuery.isBlank()) {
            currentOptions
        } else {
            currentOptions.filter { it.appLabel.contains(currentQuery, ignoreCase = true) }
        }
        HubWidgetPickerUiState(
            query = currentQuery,
            groups = filtered.toSortedGroups(),
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

    fun onProviderSelect(option: WidgetProviderOption, target: ClockWidgetPickerTarget) {
        viewModelScope.launch {
            pendingTarget = target
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

    /** Puts the new widget into the stack as the target says; if that fails (the page is gone, or the stack filled up meanwhile), the new id is released instead. */
    private suspend fun finishBinding(appWidgetId: Int) {
        val stored = when (val target = pendingTarget) {
            is ClockWidgetPickerTarget.Replace -> replaceClockStackItem.withWidget(target.itemId, appWidgetId)
            is ClockWidgetPickerTarget.Add -> addClockStackItem.addWidget(target.facetId, appWidgetId) == AddClockStackItemResult.ADDED
            null -> false
        }
        if (!stored) {
            failAndRelease(appWidgetId, AddFailureReason.SETUP_CANCELLED)
            return
        }
        _events.emit(HubAddWidgetEvent.WidgetAdded)
    }

    private suspend fun failAndRelease(appWidgetId: Int, reason: AddFailureReason) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        _events.emit(HubAddWidgetEvent.AddFailed(reason))
    }
}
