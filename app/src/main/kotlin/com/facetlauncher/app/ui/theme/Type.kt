package com.facetlauncher.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.data.model.FontWeightOption

/**
 * Every text role except [FacetType.clock] reads Material 3's own type scale directly
 * (`MaterialTheme.typography.*`, configured with [fontFamily]/[fontWeight]/[fontScale] below)
 * rather than a custom scale transcribed from README.md's px values — see CLAUDE.md's Compose
 * conventions. [fontFamily] swaps app-wide (Settings → Appearance → Font; [FontFamily.SansSerif] is
 * both its `SYSTEM` resolution and this function's default, a stand-in for Inter until real font
 * assets are added). [fontScale] multiplies every role's real `fontSize`/`lineHeight` app-wide
 * (Settings → Appearance → Text size), reaching every `MaterialTheme.typography` consumer — Home,
 * Drawer, Settings, dialogs — for free, with no per-component wiring needed. [fontWeight] only
 * overrides `bodyLarge`/`bodyMedium`/`bodySmall` (Settings → Appearance → Text weight,
 * `homeAppsFontWeight`) — deliberately not `title*`/`label*`/`headline*`/`display*`: those roles
 * back dialog titles, context menus, and onboarding copy at Material's own Medium weight (see
 * `ConfirmDialog`/`RenameDialog`/`AppContextMenu`), and flattening them app-wide by default would
 * be a real visual regression there, not the "regular text" [FontWeightOption] is scoped to (its own
 * doc: "never headlines"). Home's own app-list/Dock labels (`titleMedium`) keep applying
 * `homeAppsFontWeight` via their own explicit `.copy(fontWeight = ...)`, same as before — this just
 * adds the same value to `bodyLarge`/`bodyMedium`/`bodySmall` too, so plain body/row text (most of
 * Settings' own rows — see `LabeledDropdownRow`) picks it up without every screen threading it
 * through individually. Clock/calendar text stays independent of all three — see
 * `ui/theme/ClockFonts.kt`'s `ClockFontOption.resolveFontFamily`; calendar's own size/weight instead
 * read `MaterialTheme.typography.bodyLarge` directly (`CalendarEventsBlock.kt`), so it inherits
 * [fontScale] the same way Settings does.
 */
private val m3Defaults = Typography()

fun facetTypography(
    fontFamily: FontFamily = FontFamily.SansSerif,
    fontWeight: FontWeight = FontWeight.Normal,
    fontScale: Float = 1f,
) = Typography(
    displayLarge = m3Defaults.displayLarge.scaledBy(fontFamily, fontScale),
    displayMedium = m3Defaults.displayMedium.scaledBy(fontFamily, fontScale),
    displaySmall = m3Defaults.displaySmall.scaledBy(fontFamily, fontScale),
    headlineLarge = m3Defaults.headlineLarge.scaledBy(fontFamily, fontScale),
    headlineMedium = m3Defaults.headlineMedium.scaledBy(fontFamily, fontScale),
    headlineSmall = m3Defaults.headlineSmall.scaledBy(fontFamily, fontScale),
    titleLarge = m3Defaults.titleLarge.scaledBy(fontFamily, fontScale),
    titleMedium = m3Defaults.titleMedium.scaledBy(fontFamily, fontScale),
    titleSmall = m3Defaults.titleSmall.scaledBy(fontFamily, fontScale),
    bodyLarge = m3Defaults.bodyLarge.scaledBy(fontFamily, fontScale, fontWeight),
    bodyMedium = m3Defaults.bodyMedium.scaledBy(fontFamily, fontScale, fontWeight),
    bodySmall = m3Defaults.bodySmall.scaledBy(fontFamily, fontScale, fontWeight),
    labelLarge = m3Defaults.labelLarge.scaledBy(fontFamily, fontScale),
    labelMedium = m3Defaults.labelMedium.scaledBy(fontFamily, fontScale),
    labelSmall = m3Defaults.labelSmall.scaledBy(fontFamily, fontScale),
)

private fun TextStyle.scaledBy(fontFamily: FontFamily, fontScale: Float, fontWeight: FontWeight? = null) = copy(
    fontFamily = fontFamily,
    fontWeight = fontWeight ?: this.fontWeight,
    fontSize = fontSize * fontScale,
    lineHeight = lineHeight * fontScale,
)

/**
 * The clock is the one deliberate exception to using M3's scale directly — its 200-weight
 * 72sp display treatment (README.md's `1c`) doesn't correspond to any M3 type role.
 */
object FacetType {
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
