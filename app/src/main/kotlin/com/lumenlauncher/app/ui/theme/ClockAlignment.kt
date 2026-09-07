package com.lumenlauncher.app.ui.theme

import androidx.compose.ui.Alignment
import com.lumenlauncher.app.data.model.ClockAlignment

/**
 * Resolves the persisted [ClockAlignment] setting to a real Compose horizontal alignment — reused
 * both for positioning the clock block itself within its container (see
 * [com.lumenlauncher.app.ui.home.HomeScreen], [com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen])
 * and for aligning the clock's own time/date content consistently with that position (see
 * [com.lumenlauncher.app.ui.home.clock.ClockDisplay]).
 */
fun ClockAlignment.resolve(): Alignment.Horizontal = when (this) {
    ClockAlignment.LEFT -> Alignment.Start
    ClockAlignment.CENTER -> Alignment.CenterHorizontally
    ClockAlignment.RIGHT -> Alignment.End
}
