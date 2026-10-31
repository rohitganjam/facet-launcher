package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LandscapeOverrides
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Saves a change to a facet's clock placement (alignment, scale, divider) from Home's adjust mode. Placement is always
 * the facet's own: the first time it is adjusted, the placement the facet is showing, which is the launcher's fallback
 * until then, is copied into the facet in both orientations, so changing one value never moves the others. Spans
 * [FacetRepository] and [SettingsRepository] (the fallback), so it is a use case.
 */
class CommitClockPlacementUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
) {
    /** Applies [change] to the placement of the orientation being adjusted ([landscape]). */
    suspend operator fun invoke(facetId: Long, landscape: Boolean, change: (ClockPlacement) -> ClockPlacement) {
        val facet = facetRepository.getById(facetId) ?: return
        val shown = resolveFacetStyle(facet, settingsRepository.settings.first()).clockPlacement
        val portrait = if (landscape) shown.portrait else change(shown.portrait)
        val inLandscape = if (landscape) change(shown.landscape) else shown.landscape
        facetRepository.setClockPlacement(
            facetId = facetId,
            alignment = portrait.alignment,
            zoneHeightDp = portrait.zoneHeightDp,
            scale = portrait.scale,
            // A landscape value equal to portrait's stays null, which means "same as portrait".
            landscape = LandscapeOverrides(
                clockAlignment = inLandscape.alignment.takeIf { it != portrait.alignment },
                clockZoneHeightDp = inLandscape.zoneHeightDp.takeIf { it != portrait.zoneHeightDp },
                clockScale = inLandscape.scale.takeIf { it != portrait.scale },
            ),
        )
    }
}
