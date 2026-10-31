package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.domain.ReplaceClockStackItemUseCase
import javax.inject.Inject

/**
 * PRD F15's clock-widget writes: the swap to the native clock and the resize commit, both against a page of the
 * facet's clock stack, plus whether Pro is held. Sibling to [ClockWidgetHostController] (that one wraps
 * `AppWidgetRepository` hooks), both extracted out of [HomeViewModel] to stay under this codebase's
 * max-functions-per-class lint rule. The composable layer passes the visible page's id from
 * [HomeUiState.visibleClockItem].
 */
class ClockWidgetFacetController @Inject constructor(
    private val clockStackRepository: ClockStackRepository,
    private val replaceClockStackItem: ReplaceClockStackItemUseCase,
    entitlementRepository: EntitlementRepository,
) {
    /** Picking a hosted widget for the clock slot is Pro; a facet that already hosts one keeps it after a lapse. */
    val isPro = entitlementRepository.isPro

    /** PRD F15's "Switch to launcher clock widget" row: the page [itemId] becomes the native clock again, releasing its widget. */
    suspend fun replaceWithClock(itemId: Long) = replaceClockStackItem.withClock(itemId)

    /** PRD F15's interactive resize commit, fired once on release by the clock's resize handles; saved against the stack item [itemId]. */
    suspend fun commitSize(itemId: Long, widthDp: Int, heightDp: Int, mode: HomeLayoutMode = HomeLayoutMode.PORTRAIT) =
        if (mode == HomeLayoutMode.LANDSCAPE) {
            clockStackRepository.setLandscapeSize(itemId, widthDp, heightDp)
        } else {
            clockStackRepository.setSize(itemId, widthDp, heightDp)
        }
}
