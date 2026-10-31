package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.domain.CommitClockPlacementUseCase
import com.facetlauncher.app.domain.RemoveClockStackItemUseCase
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
    private val removeClockStackItem: RemoveClockStackItemUseCase,
    private val commitClockPlacement: CommitClockPlacementUseCase,
    entitlementRepository: EntitlementRepository,
) {
    /** Picking a hosted widget for the clock slot is Pro; a facet that already hosts one keeps it after a lapse. */
    val isPro = entitlementRepository.isPro

    /** PRD F15's "Switch to launcher clock widget" row: the page [itemId] becomes the native clock again, releasing its widget. */
    suspend fun replaceWithClock(itemId: Long) = replaceClockStackItem.withClock(itemId)

    /** Drops the page [itemId], releasing its widget; used for a widget whose app was uninstalled. */
    suspend fun remove(itemId: Long) = removeClockStackItem(itemId)

    /** PRD F15's interactive resize commit, fired once on release by the clock's resize handles; saved against the stack item [itemId]. */
    suspend fun commitSize(itemId: Long, widthDp: Int, heightDp: Int, mode: HomeLayoutMode = HomeLayoutMode.PORTRAIT) =
        if (mode == HomeLayoutMode.LANDSCAPE) {
            clockStackRepository.setLandscapeSize(itemId, widthDp, heightDp)
        } else {
            clockStackRepository.setSize(itemId, widthDp, heightDp)
        }

    /** Saves a change to the facet's own clock placement (alignment, scale or divider) for [mode]; see [CommitClockPlacementUseCase]. */
    suspend fun commitPlacement(facetId: Long, mode: HomeLayoutMode, change: (ClockPlacement) -> ClockPlacement) =
        commitClockPlacement(facetId, mode == HomeLayoutMode.LANDSCAPE, change)

    /** Saves the alignment of one widget page, which has its own apart from the clock's. */
    suspend fun commitPageAlignment(itemId: Long, alignment: ClockAlignment, mode: HomeLayoutMode) =
        if (mode == HomeLayoutMode.LANDSCAPE) {
            clockStackRepository.setLandscapeAlignment(itemId, alignment)
        } else {
            clockStackRepository.setAlignment(itemId, alignment)
        }
}
