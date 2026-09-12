package com.facetlauncher.app.ui.theme

import androidx.compose.ui.Alignment
import com.facetlauncher.app.data.model.ClockAlignment

/**
 * Resolves the persisted [ClockAlignment] setting to a real Compose horizontal alignment — reused
 * both for positioning the clock block itself within its container (see
 * [com.facetlauncher.app.ui.home.HomeScreen], [com.facetlauncher.app.ui.profiles.ProfileCarouselScreen])
 * and for aligning the clock's own time/date content consistently with that position (see
 * [com.facetlauncher.app.ui.home.clock.ClockDisplay]).
 */
fun ClockAlignment.resolve(): Alignment.Horizontal = when (this) {
    ClockAlignment.LEFT -> Alignment.Start
    ClockAlignment.CENTER -> Alignment.CenterHorizontally
    ClockAlignment.RIGHT -> Alignment.End
}
