package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

data class HubWidgetState(
    val appWidgetId: Int,
    val row: Int,
    val col: Int,
    val colSpan: Int,
    val rowSpan: Int,
    val providerLabel: String?,
    val isOrphaned: Boolean,
)

data class HubDomainState(
    val widgets: List<HubWidgetState>,
) {
    val widgetCount: Int get() = widgets.size
    val isAtCapacity: Boolean get() = widgetCount >= HUB_MAX_WIDGETS
}

/**
 * Combines [WidgetPlacementRepository]'s stored placements with [AppWidgetRepository]'s live
 * provider-info lookups (orphan detection, provider labels) — spans more than one repository, so
 * it's a UseCase rather than living in [com.lumenlauncher.app.ui.hub.HubViewModel] directly.
 */
class ObserveHubStateUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    // Orphan status/provider labels aren't captured by the placements Flow itself — this forces
    // a re-resolution against the live AppWidgetManager whenever a bound provider's app updates
    // (onProviderChanged) or a package is otherwise (un)installed.
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    operator fun invoke(): Flow<HubDomainState> {
        // merge, not combine, for the two trigger signals — observeProviderChanges() is a bare
        // SharedFlow with no replay that may never emit at all (no provider has changed yet), and
        // combine() only produces a value once *every* source has emitted at least once; merging
        // it with refreshTrigger (which always has an initial replayed value) means the very
        // first combined state doesn't wait on a provider-change event that might never come.
        val refreshSignal = merge(refreshTrigger, appWidgetRepository.observeProviderChanges().map {})
        return combine(widgetPlacementRepository.observeAll(), refreshSignal) { placements, _ -> placements }.map { placements ->
            HubDomainState(
                widgets = placements.map { placement ->
                    val info = appWidgetRepository.getAppWidgetInfo(placement.appWidgetId)
                    HubWidgetState(
                        appWidgetId = placement.appWidgetId,
                        row = placement.row,
                        col = placement.col,
                        colSpan = placement.colSpan,
                        rowSpan = placement.rowSpan,
                        providerLabel = if (info != null) appWidgetRepository.getProviderLabel(placement.appWidgetId) else null,
                        isOrphaned = info == null,
                    )
                },
            )
        }
    }
}
