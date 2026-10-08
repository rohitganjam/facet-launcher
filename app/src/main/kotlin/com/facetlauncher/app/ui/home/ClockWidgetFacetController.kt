package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.domain.SwitchFacetToNativeClockUseCase
import javax.inject.Inject

/**
 * PRD F15's facet-writing clock-widget actions — takes the target [FacetEntity] as a parameter
 * rather than reading it from any ViewModel's own state, sibling to [ClockWidgetHostController]
 * (that one wraps `AppWidgetRepository` hooks; this one wraps `FacetRepository` writes) — both
 * extracted out of [HomeViewModel] to stay under this codebase's max-functions-per-class lint
 * rule. The Composable layer resolves the active facet from [HomeUiState.activeFacet] itself
 * (same pattern already used for e.g. `onNavigateToFacetSettings` in
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]) and passes it straight through.
 */
class ClockWidgetFacetController @Inject constructor(
    private val facetRepository: FacetRepository,
    private val switchFacetToNativeClock: SwitchFacetToNativeClockUseCase,
    entitlementRepository: EntitlementRepository,
) {
    /** Picking a hosted widget for the clock slot is Pro; a facet that already hosts one keeps it after a lapse. */
    val isPro = entitlementRepository.isPro

    /** PRD F15's "Switch to launcher clock widget" row — see [SwitchFacetToNativeClockUseCase]'s own doc. */
    suspend fun switchToNativeClock(facet: FacetEntity) = switchFacetToNativeClock(facet)

    /** PRD F15's interactive resize commit, fired once on release by the clock's resize handles — see [FacetEntity.clockWidgetWidthDp]'s own doc. */
    suspend fun commitSize(facet: FacetEntity, widthDp: Int, heightDp: Int) =
        facetRepository.setClockWidgetSize(facet, widthDp, heightDp)
}
