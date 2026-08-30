package com.lumenlauncher.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Every text role except [LumenType.clock] reads Material 3's own type scale directly
 * (`MaterialTheme.typography.*`, configured with this app's font family below) rather than a
 * custom scale transcribed from README.md's px values — see CLAUDE.md's Compose conventions.
 * Each role keeps [Typography]'s own real size/weight/line-height/letter-spacing untouched;
 * only `fontFamily` is swapped to [FontFamily.SansSerif] (a stand-in for Inter until real font
 * assets are added).
 */
private val m3Defaults = Typography()

val LumenTypography = Typography(
    displayLarge = m3Defaults.displayLarge.copy(fontFamily = FontFamily.SansSerif),
    displayMedium = m3Defaults.displayMedium.copy(fontFamily = FontFamily.SansSerif),
    displaySmall = m3Defaults.displaySmall.copy(fontFamily = FontFamily.SansSerif),
    headlineLarge = m3Defaults.headlineLarge.copy(fontFamily = FontFamily.SansSerif),
    headlineMedium = m3Defaults.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
    headlineSmall = m3Defaults.headlineSmall.copy(fontFamily = FontFamily.SansSerif),
    titleLarge = m3Defaults.titleLarge.copy(fontFamily = FontFamily.SansSerif),
    titleMedium = m3Defaults.titleMedium.copy(fontFamily = FontFamily.SansSerif),
    titleSmall = m3Defaults.titleSmall.copy(fontFamily = FontFamily.SansSerif),
    bodyLarge = m3Defaults.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = m3Defaults.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    bodySmall = m3Defaults.bodySmall.copy(fontFamily = FontFamily.SansSerif),
    labelLarge = m3Defaults.labelLarge.copy(fontFamily = FontFamily.SansSerif),
    labelMedium = m3Defaults.labelMedium.copy(fontFamily = FontFamily.SansSerif),
    labelSmall = m3Defaults.labelSmall.copy(fontFamily = FontFamily.SansSerif),
)

/**
 * The clock is the one deliberate exception to using M3's scale directly — its 200-weight
 * 72sp display treatment (README.md's `1c`) doesn't correspond to any M3 type role.
 */
object LumenType {
    val clock = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = (-4).sp,
    )
}
