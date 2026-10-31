package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * Makes [appWidgetId] the facet's one clock widget ("Use custom widget" and "Replace custom widget"): the stack
 * becomes just that widget and every id it held before is released, so none leaks.
 */
class ReplaceClockWidgetUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    suspend operator fun invoke(facetId: Long, appWidgetId: Int) {
        clockStackRepository.replaceWithWidget(facetId, appWidgetId).forEach { appWidgetRepository.deleteAppWidgetId(it) }
    }
}
