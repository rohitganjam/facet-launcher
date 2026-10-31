package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * PRD F15's "Switch to launcher clock widget" row: reverts [facet] to its own native clock and
 * releases every `AppWidgetHost` id its clock stack held. Spans [ClockStackRepository] and
 * [AppWidgetRepository], so per `CLAUDE.md`'s layering rule this is a domain use case rather than a
 * `ViewModel` calling both, as [DeleteFacetUseCase] does.
 */
class SwitchFacetToNativeClockUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(facet: Facet) {
        clockStackRepository.clear(facet.id).forEach { appWidgetRepository.deleteAppWidgetId(it) }
    }
}
