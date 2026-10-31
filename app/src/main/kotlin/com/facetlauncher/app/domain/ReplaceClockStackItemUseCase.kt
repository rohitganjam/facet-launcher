package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.ReplacedItem
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/**
 * Swaps one page of a facet's clock stack for the native clock or a hosted widget, in place, and releases the
 * `AppWidgetHost` id the old page held so none leaks. Spans [ClockStackRepository] and [AppWidgetRepository], so per
 * `CLAUDE.md`'s layering rule it is a use case, as [DeleteFacetUseCase] is.
 */
class ReplaceClockStackItemUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val appWidgetRepository: AppWidgetRepository,
) {
    /** `false` when the item no longer exists or the clock is already on another page; nothing changes then. */
    suspend fun withClock(itemId: Long): Boolean = release(clockStackRepository.replaceWithClock(itemId))

    /** `false` when the item no longer exists; the caller then releases the widget id it had bound. */
    suspend fun withWidget(itemId: Long, appWidgetId: Int): Boolean = release(clockStackRepository.replaceWithWidget(itemId, appWidgetId))

    private fun release(replaced: ReplacedItem?): Boolean {
        replaced?.releasedAppWidgetId?.let { appWidgetRepository.deleteAppWidgetId(it) }
        return replaced != null
    }
}
