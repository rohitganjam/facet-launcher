package com.facetlauncher.app.ui.home.stack

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.clockStackLimit
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.CopyFacetWidgetsUseCase
import com.facetlauncher.app.domain.RemoveClockStackItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
    /** The other facets, for Copy from facet. */
    val otherFacets: ImmutableList<Facet> = persistentListOf(),
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
    private val copyFacetWidgets: CopyFacetWidgetsUseCase,
    facetRepository: FacetRepository,
    entitlementRepository: EntitlementRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    /** The facet whose stack this is: the one in the route, else whichever is active. */
    private val routeFacetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    val uiState: StateFlow<ClockStackUiState> = settingsRepository.settings
        .map { routeFacetId ?: it.activeFacetId }
        .distinctUntilChanged()
        .flatMapLatest { facetId ->
            combine(clockStackRepository.observe(facetId), entitlementRepository.isPro, facetRepository.observeFacets()) { items, isPro, facets ->
                ClockStackUiState(
                    facetId = facetId,
                    rows = items.map { it.toRow() },
                    isPro = isPro,
                    otherFacets = facets.filter { it.id != facetId }.toImmutableList(),
                )
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

    // --- Copy from facet: the stack is cleared, then each source page is copied in order. A hosted widget needs a fresh
    // host id, bound (maybe after a permission screen) and configured (its own screen) before it goes in.

    private val _copyEvents = Channel<ClockStackCopyEvent>(Channel.BUFFERED)

    /** The system screens the copy needs next; the composable launches them and reports back. */
    val copyEvents: Flow<ClockStackCopyEvent> = _copyEvents.receiveAsFlow()

    private var copyTargetId = NO_ACTIVE_FACET_ID
    private var copyQueue = ArrayDeque<ClockStackItem>()
    private var copyPage: ClockStackItem? = null
    private var pendingAppWidgetId: Int? = null
    private var copyOmitted = 0

    /** Replaces this stack with a copy of [sourceFacetId]'s, widgets included (re-added one by one). */
    fun copyFromFacet(sourceFacetId: Long) {
        viewModelScope.launch {
            val target = activeFacetId()
            if (sourceFacetId == target) return@launch
            val plan = copyFacetWidgets.prepare(sourceFacetId, target)
            copyTargetId = target
            copyQueue = ArrayDeque(plan.pages)
            copyOmitted = plan.omitted
            continueCopy()
        }
    }

    /** The composable's bind-permission launcher result. */
    fun onCopyBindResult(granted: Boolean) {
        val appWidgetId = pendingAppWidgetId ?: return
        pendingAppWidgetId = null
        viewModelScope.launch {
            if (!granted) {
                dropPage(appWidgetId)
            } else if (proceedAfterBind(appWidgetId)) {
                return@launch
            }
            continueCopy()
        }
    }

    /** The composable's configure-activity launcher result. */
    fun onCopyConfigureResult(resultOk: Boolean) {
        val appWidgetId = pendingAppWidgetId ?: return
        pendingAppWidgetId = null
        viewModelScope.launch {
            if (resultOk) addCopiedPage(appWidgetId) else dropPage(appWidgetId)
            continueCopy()
        }
    }

    /** Copies pages until one needs the system (then it returns and waits for the result) or the queue is empty. */
    private suspend fun continueCopy() {
        var waiting = false
        while (!waiting) {
            val page = copyQueue.removeFirstOrNull() ?: break
            copyPage = page
            waiting = copyPageOrWait(page)
        }
        if (!waiting) {
            copyPage = null
            _copyEvents.send(ClockStackCopyEvent.Done(omitted = copyOmitted))
        }
    }

    /** `true` when [page] needs a system screen first and the copy waits for its result; else it is already in or counted as omitted. */
    private suspend fun copyPageOrWait(page: ClockStackItem): Boolean {
        if (page.kind != ClockStackItemKind.WIDGET) {
            if (!copyFacetWidgets.appendNative(copyTargetId, page)) copyOmitted++
            return false
        }
        val info = page.appWidgetId?.let { appWidgetRepository.getAppWidgetInfo(it) }
        if (info == null) {
            copyOmitted++
            return false
        }
        val newId = appWidgetRepository.allocateAppWidgetId()
        if (appWidgetRepository.bindAppWidgetIdIfAllowed(newId, info.provider, info.profile)) return proceedAfterBind(newId)
        pendingAppWidgetId = newId
        _copyEvents.send(ClockStackCopyEvent.LaunchBindPermission(appWidgetRepository.createBindIntent(newId, info.provider, info.profile)))
        return true
    }

    /** `true` when the widget's own configure screen was launched and the copy waits for it; else the page is already in or dropped. */
    private suspend fun proceedAfterBind(appWidgetId: Int): Boolean {
        val info = appWidgetRepository.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            dropPage(appWidgetId)
            return false
        }
        val configureIntent = appWidgetRepository.createConfigureIntent(appWidgetId, info)
        if (configureIntent != null) {
            pendingAppWidgetId = appWidgetId
            _copyEvents.send(ClockStackCopyEvent.LaunchConfigure(configureIntent))
            return true
        }
        addCopiedPage(appWidgetId)
        return false
    }

    private suspend fun addCopiedPage(appWidgetId: Int) {
        val page = copyPage
        if (page == null || !copyFacetWidgets.appendHosted(copyTargetId, page, appWidgetId)) dropPage(appWidgetId)
    }

    private fun dropPage(appWidgetId: Int) {
        appWidgetRepository.deleteAppWidgetId(appWidgetId)
        copyOmitted++
    }
}

/** What the widget-stack copy needs from the screen next. */
sealed interface ClockStackCopyEvent {
    data class LaunchBindPermission(val intent: Intent) : ClockStackCopyEvent
    data class LaunchConfigure(val intent: Intent) : ClockStackCopyEvent

    /** Every page is handled; [omitted] could not be copied (over the plan's limit, its app gone, or its setup cancelled). */
    data class Done(val omitted: Int) : ClockStackCopyEvent
}
