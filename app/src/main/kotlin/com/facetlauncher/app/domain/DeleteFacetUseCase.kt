package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * Deletes a facet, releasing every widget id in its clock stack first (PRD F15) — spans
 * [FacetRepository] and [AppWidgetRepository], so per `CLAUDE.md`'s layering rule this is a
 * domain use case rather than either `ViewModel` calling both repositories directly. Without this,
 * [AppWidgetHost][android.appwidget.AppWidgetHost] would leak the id (it isn't released just
 * because the [Facet] row referencing it is gone) — same "don't leak ids" rule the Hub's
 * own widget removal already follows.
 */
class DeleteFacetUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(facet: Facet) {
        facet.clockStack.mapNotNull { it.appWidgetId }.forEach { appWidgetRepository.deleteAppWidgetId(it) }
        facetRepository.deleteFacet(facet.id)
    }
}
