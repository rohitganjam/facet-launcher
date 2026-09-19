package com.facetlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Launcher Hub (F5) — a fixed-column grid of user-placed widgets, to Home's left. */
@HiltViewModel
class HubViewModel @Inject constructor(
    observeHubState: ObserveHubStateUseCase,
    private val appWidgetRepository: AppWidgetRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val deleteWidget: DeleteWidgetUseCase,
    private val resolveWidgetDrop: ResolveWidgetDropUseCase,
    private val resolveWidgetResize: ResolveWidgetResizeUseCase,
    private val compactWidgets: CompactWidgetsUseCase,
) : ViewModel() {

    val uiState = observeHubState().map { domainState ->
        HubUiState(
            widgets = domainState.widgets.map { widget ->
                HubWidgetUi(
                    appWidgetId = widget.appWidgetId,
                    row = widget.row,
                    col = widget.col,
                    colSpan = widget.colSpan,
                    rowSpan = widget.rowSpan,
                    providerLabel = widget.providerLabel,
                    isOrphaned = widget.isOrphaned,
                )
            },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubUiState())

    /** Scoped to Hub's own visible lifetime — avoids unnecessary background IPC/battery cost. */
    fun onHubVisible() {
        appWidgetRepository.startListening()
    }

    fun onHubHidden() {
        appWidgetRepository.stopListening()
    }

    /** The tile's live on-screen size (dp) — see [AppWidgetRepository.updateWidgetSize]. */
    fun updateWidgetSize(appWidgetId: Int, widthDp: Int, heightDp: Int) =
        appWidgetRepository.updateWidgetSize(appWidgetId, widthDp, heightDp)

    /** `null` when the widget id has no resolvable provider (e.g. it's orphaned). */
    fun createHostView(context: Context, appWidgetId: Int): AppWidgetHostView? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let { info ->
            appWidgetRepository.createHostView(context, appWidgetId, info).apply {
                // Blocks the widget's own internal long-press handling (which might trigger
                // an app launch or internal menu) so it doesn't conflict with the launcher's
                // own drag/resize orchestration. Returning true means we've "handled" it.
                setOnLongClickListener { true }
            }
        }

    fun onRemoveOrphan(appWidgetId: Int) {
        viewModelScope.launch {
            deleteWidget(appWidgetId)
            compactRemaining()
        }
    }

    /** The footprint/dashed placeholder simply stays until the provider's app is reinstalled — no state to change. */
    fun onKeepOrphanSpace() = Unit

    /**
     * Resolves and commits a drop in one step — an empty target is a plain move; one occupied by
     * other widgets displaces them (right, then down) rather than bouncing back, unless even
     * displacement can't make room, in which case nothing changes and the dragged widget snaps
     * back to its origin (the UI never learns which case happened; it just re-renders from the
     * latest [uiState] either way). The whole grid is then gravity-compacted (see
     * [CompactWidgetsUseCase]) so this never leaves — or uncovers — an empty row gap.
     */
    fun onWidgetDrop(appWidgetId: Int, targetRow: Int, targetCol: Int, colSpan: Int, rowSpan: Int) {
        val current = currentPlacements()
        val resolved = resolveWidgetDrop(current, appWidgetId, targetRow, targetCol, colSpan, rowSpan) ?: return
        commitPlacements(compactWidgets(mergeResolved(current, resolved)))
    }

    fun onWidgetDroppedOnTrash(appWidgetId: Int) {
        viewModelScope.launch {
            deleteWidget(appWidgetId)
            compactRemaining()
        }
    }

    /**
     * Resolves and commits a resize in one step — [row]/[col] are parameters, not read off the
     * current widget, because a left/top-edge resize moves the anchor cell itself (grows/shrinks
     * from that edge while the opposite edge stays put); only a right/bottom-edge resize keeps
     * the widget's existing row/col unchanged. A target rectangle that overlaps other widgets
     * displaces them straight down (cascading further if needed) rather than bouncing back — see
     * [ResolveWidgetResizeUseCase] — unless even that can't make room, in which case nothing
     * changes and the resize snaps back to its pre-drag span.
     */
    fun onWidgetResize(appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) {
        val current = currentPlacements()
        val resolved = resolveWidgetResize(current, appWidgetId, row, col, colSpan, rowSpan) ?: return
        commitPlacements(compactWidgets(mergeResolved(current, resolved)))
    }

    /** Re-reads placements straight from Room (not [currentPlacements]'s [uiState] snapshot, which may not have caught up with the delete yet) before compacting what's left. */
    private suspend fun compactRemaining() {
        val remaining = widgetPlacementRepository.observeAll().first()
        commitEntities(compactWidgets(remaining))
    }

    /** [resolved]'s entries (built from [currentPlacements]'s placeholder provider fields) replace their matching entries in [current]; everything else passes through unchanged. */
    private fun mergeResolved(current: List<WidgetPlacementEntity>, resolved: List<WidgetPlacementEntity>): List<WidgetPlacementEntity> {
        val resolvedById = resolved.associateBy { it.appWidgetId }
        return current.map { resolvedById[it.appWidgetId] ?: it }
    }

    /** [placements] are built from [currentPlacements]'s placeholder provider fields — re-fetch each real entity so only row/col/span actually change. */
    private fun commitPlacements(placements: List<WidgetPlacementEntity>) {
        viewModelScope.launch { commitEntities(placements) }
    }

    private suspend fun commitEntities(placements: List<WidgetPlacementEntity>) {
        placements.forEach { placement ->
            val existing = widgetPlacementRepository.getById(placement.appWidgetId) ?: return@forEach
            if (existing.row != placement.row || existing.col != placement.col ||
                existing.colSpan != placement.colSpan || existing.rowSpan != placement.rowSpan
            ) {
                widgetPlacementRepository.upsert(
                    existing.copy(row = placement.row, col = placement.col, colSpan = placement.colSpan, rowSpan = placement.rowSpan),
                )
            }
        }
    }

    /**
     * A minimal, throwaway [WidgetPlacementEntity] projection of the latest known widgets —
     * [ResolveWidgetDropUseCase]/[ResolveWidgetResizeUseCase]/[CompactWidgetsUseCase] only read
     * `appWidgetId`/`row`/`col`/`colSpan`/`rowSpan`, so the provider fields are irrelevant
     * placeholders here.
     */
    private fun currentPlacements(): List<WidgetPlacementEntity> = uiState.value.widgets.map { widget ->
        WidgetPlacementEntity(
            appWidgetId = widget.appWidgetId,
            providerPackageName = "",
            providerClassName = "",
            row = widget.row,
            col = widget.col,
            colSpan = widget.colSpan,
            rowSpan = widget.rowSpan,
        )
    }
}
