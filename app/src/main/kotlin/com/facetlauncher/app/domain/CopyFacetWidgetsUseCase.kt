package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.clockStackLimit
import com.facetlauncher.app.data.widget.AppWidgetRepository
import javax.inject.Inject

/** What is left to copy once the target's stack is cleared: [pages] in order, and how many [omitted] pages the plan's limit cut. */
data class WidgetCopyPlan(val pages: List<ClockStackItem>, val omitted: Int)

/**
 * "Copy from facet" for the widget stack. [prepare] clears the target's stack (releasing its widget ids) and copies the clock's
 * placement, then the caller walks [WidgetCopyPlan.pages] in order: a page Facet draws itself goes in through [appendNative];
 * a hosted widget needs a fresh `AppWidgetHost` id, bound and configured through the system (the UI's job), then goes in
 * through [appendHosted]. Every page keeps its size, alignment and settings. The stack never goes past the plan's limit.
 */
class CopyFacetWidgetsUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val facetRepository: FacetRepository,
    private val appWidgetRepository: AppWidgetRepository,
    private val entitlementRepository: EntitlementRepository,
) {
    private val limit: Int get() = clockStackLimit(entitlementRepository.isPro.value)

    suspend fun prepare(sourceId: Long, targetId: Long): WidgetCopyPlan {
        val source = clockStackRepository.getAll(sourceId)
        clockStackRepository.clear(targetId).forEach(appWidgetRepository::deleteAppWidgetId)
        facetRepository.copyClockPlacement(sourceId, targetId)
        val pages = source.take(limit)
        return WidgetCopyPlan(pages, omitted = source.size - pages.size)
    }

    suspend fun appendNative(targetId: Long, page: ClockStackItem): Boolean = clockStackRepository.appendCopy(targetId, page, null, limit)

    /** [appWidgetId] is the new id bound for this copy; the caller releases it when this returns `false`. */
    suspend fun appendHosted(targetId: Long, page: ClockStackItem, appWidgetId: Int): Boolean =
        clockStackRepository.appendCopy(targetId, page, appWidgetId, limit)
}
