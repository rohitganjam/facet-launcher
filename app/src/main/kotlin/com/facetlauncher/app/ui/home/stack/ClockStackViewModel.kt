package com.facetlauncher.app.ui.home.stack

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.clockStackLimit
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.RemoveClockStackItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One page of the stack as the screen lists it. [providerName] is the widget's provider name, `null` for the clock or a widget whose app is gone. */
@Immutable
data class ClockStackRow(
    val id: Long,
    val kind: ClockStackItemKind,
    val appWidgetId: Int?,
    val nativeWidgetType: NativeWidgetType? = null,
    val providerName: String?,
    /** The clock opens Appearance; a widget can be configured only if its provider declares a configure activity. */
    val canConfigure: Boolean,
)

@Immutable
data class ClockStackUiState(
    val facetId: Long = NO_ACTIVE_FACET_ID,
    val rows: List<ClockStackRow> = emptyList(),
    val isPro: Boolean = true,
) {
    val limit: Int get() = clockStackLimit(isPro)
    val hasClock: Boolean get() = rows.any { it.kind == ClockStackItemKind.CLOCK }

    /** The add row is hidden only once a Pro stack is full; a full free stack still shows it, with a Pro pill, so the way up is visible. */
    val showAdd: Boolean get() = rows.size < MAX_CLOCK_STACK_ITEMS

    /** Adding would need Pro: the plan's limit is reached. */
    val addNeedsPro: Boolean get() = rows.size >= limit
}

/**
 * The widget stack screen: a facet's clock stack (the active one unless the route names another) as a list to reorder, remove from, configure and add to.
 * Replacing a page is on Home's sheet, not here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClockStackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val clockStackRepository: ClockStackRepository,
    private val appWidgetRepository: AppWidgetRepository,
    private val removeClockStackItem: RemoveClockStackItemUseCase,
    private val addClockStackItem: AddClockStackItemUseCase,
    entitlementRepository: EntitlementRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    /** The facet whose stack this is: the one in the route, else whichever is active. */
    private val routeFacetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    val uiState: StateFlow<ClockStackUiState> = settingsRepository.settings
        .map { routeFacetId ?: it.activeFacetId }
        .distinctUntilChanged()
        .flatMapLatest { facetId ->
            combine(clockStackRepository.observe(facetId), entitlementRepository.isPro) { items, isPro ->
                ClockStackUiState(facetId = facetId, rows = items.map { it.toRow() }, isPro = isPro)
            }
        }
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockStackUiState())

    /** The provider lookups are system calls, so [uiState] builds its rows on the IO dispatcher. */
    private fun ClockStackItem.toRow() = ClockStackRow(
        id = id,
        kind = kind,
        appWidgetId = appWidgetId,
        nativeWidgetType = nativeWidgetType,
        providerName = appWidgetId?.let { appWidgetRepository.getProviderLabel(it) },
        canConfigure = appWidgetId?.let { configureIntent(it) != null } ?: (kind == ClockStackItemKind.CLOCK),
    )

    /** Read when an action runs, not from [uiState] (which only holds a value while something collects it). */
    private suspend fun activeFacetId() = routeFacetId ?: settingsRepository.settings.first().activeFacetId

    fun reorder(orderedRowIds: List<Long>) {
        viewModelScope.launch { clockStackRepository.reorder(activeFacetId(), orderedRowIds) }
    }

    fun remove(itemId: Long) {
        viewModelScope.launch { removeClockStackItem(itemId) }
    }

    /** Whether another page fits the plan's limit right now: when it does not, Add leads to the upsell. */
    suspend fun hasRoom(): Boolean = addClockStackItem.hasRoom(activeFacetId())

    suspend fun addClock(): AddClockStackItemResult = addClockStackItem.addClock(activeFacetId())

    /** Launches the widget's own configure screen; `null` when its provider declares none. */
    fun configureIntent(appWidgetId: Int): Intent? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let { appWidgetRepository.createConfigureIntent(appWidgetId, it) }
}
