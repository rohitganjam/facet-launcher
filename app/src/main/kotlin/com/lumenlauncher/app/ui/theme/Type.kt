package com.lumenlauncher.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.FontWeightOption

/**
 * Every text role except [LumenType.clock] reads Material 3's own type scale directly
 * (`MaterialTheme.typography.*`, configured with [fontFamily] below) rather than a custom scale
 * transcribed from README.md's px values — see CLAUDE.md's Compose conventions. Each role keeps
 * [Typography]'s own real size/weight/line-height/letter-spacing untouched; only `fontFamily` is
 * swapped — to [fontFamily] app-wide (Settings → Appearance → Font;
 * [FontFamily.SansSerif] is both its `SYSTEM` resolution and this function's default, a stand-in
 * for Inter until real font assets are added). Clock/calendar text stays independent of this —
 * see `ui/theme/ClockFonts.kt`'s `ClockFontOption.resolveFontFamily`.
 */
private val m3Defaults = Typography()

fun lumenTypography(fontFamily: FontFamily = FontFamily.SansSerif) = Typography(
    displayLarge = m3Defaults.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = m3Defaults.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = m3Defaults.displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = m3Defaults.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = m3Defaults.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = m3Defaults.headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = m3Defaults.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = m3Defaults.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = m3Defaults.titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = m3Defaults.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = m3Defaults.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = m3Defaults.bodySmall.copy(fontFamily = fontFamily),
    labelLarge = m3Defaults.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = m3Defaults.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = m3Defaults.labelSmall.copy(fontFamily = fontFamily),
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

/** Resolves a [FontWeightOption] to its real [FontWeight] — see that enum's own doc for why it stops at [FontWeightOption.SEMI_BOLD]. */
fun FontWeightOption.resolve(): FontWeight = when (this) {
    FontWeightOption.THIN -> FontWeight.Thin
    FontWeightOption.EXTRA_LIGHT -> FontWeight.ExtraLight
    FontWeightOption.LIGHT -> FontWeight.Light
    FontWeightOption.REGULAR -> FontWeight.Normal
    FontWeightOption.MEDIUM -> FontWeight.Medium
    FontWeightOption.SEMI_BOLD -> FontWeight.SemiBold
}
