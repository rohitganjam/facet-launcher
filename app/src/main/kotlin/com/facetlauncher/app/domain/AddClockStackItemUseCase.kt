package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.clockStackLimit
import javax.inject.Inject

/** How adding a page to a facet's clock stack went. */
enum class AddClockStackItemResult { ADDED, LIMIT_REACHED, CLOCK_ALREADY_IN_STACK }

/** What adding a page did, and the id of the page it made. */
data class AddedPage(val result: AddClockStackItemResult, val itemId: Long? = null)

/**
 * Appends the native clock or a hosted widget to a facet's clock stack, within the limit its plan allows: one page
 * on Free, [com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS] with Pro. Replacing or removing a page never
 * needs this, so they stay free. On anything but [AddClockStackItemResult.ADDED] nothing is written, and a caller
 * that had bound a widget id releases it.
 */
class AddClockStackItemUseCase @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val entitlementRepository: EntitlementRepository,
) {
    /** Whether another page fits in the facet's stack right now: the Add action's upsell check. */
    suspend fun hasRoom(facetId: Long): Boolean =
        clockStackRepository.getAll(facetId).size < clockStackLimit(entitlementRepository.isPro.value)

    suspend fun addClock(facetId: Long): AddClockStackItemResult = when {
        !hasRoom(facetId) -> AddClockStackItemResult.LIMIT_REACHED
        clockStackRepository.addClock(facetId) -> AddClockStackItemResult.ADDED
        else -> AddClockStackItemResult.CLOCK_ALREADY_IN_STACK
    }

    /** [AddedPage.itemId] is the new page's id when it was [AddClockStackItemResult.ADDED], so the caller can open its settings. */
    suspend fun addNativeWidget(facetId: Long, type: NativeWidgetType): AddedPage {
        if (!hasRoom(facetId)) return AddedPage(AddClockStackItemResult.LIMIT_REACHED)
        val itemId = clockStackRepository.addNativeWidget(facetId, type)
        return if (itemId != null) AddedPage(AddClockStackItemResult.ADDED, itemId) else AddedPage(AddClockStackItemResult.LIMIT_REACHED)
    }

    suspend fun addWidget(facetId: Long, appWidgetId: Int): AddClockStackItemResult = when {
        !hasRoom(facetId) -> AddClockStackItemResult.LIMIT_REACHED
        else -> if (clockStackRepository.addWidget(facetId, appWidgetId)) AddClockStackItemResult.ADDED else AddClockStackItemResult.LIMIT_REACHED
    }
}
