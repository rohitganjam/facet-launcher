package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * PRD F15's "Switch to launcher clock widget" row — reverts [facet] to its own native clock and
 * releases its previously-bound `AppWidgetHost` id (if any). Spans [FacetRepository] and
 * [AppWidgetRepository], so per `CLAUDE.md`'s layering rule this is a domain use case rather than
 * [com.facetlauncher.app.ui.home.HomeViewModel] calling both repositories directly — mirrors
 * [DeleteFacetUseCase]'s own reasoning. A no-op release when [Facet.clockWidgetAppWidgetId]
 * is already `null` (nothing to release).
 */
class SwitchFacetToNativeClockUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(facet: Facet) {
        facet.clockWidgetAppWidgetId?.let { appWidgetRepository.deleteAppWidgetId(it) }
        facetRepository.setClockWidgetAppWidgetId(facet.id, null)
    }
}
