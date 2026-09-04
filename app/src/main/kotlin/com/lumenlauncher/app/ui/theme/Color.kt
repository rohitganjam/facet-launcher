package com.lumenlauncher.app.ui.theme

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

// Light/dark pairs — transcribed from design_handoff_minimal_launcher/README.md's Design
// Tokens table (`### Light`/`### Dark`). Every public color below resolves through
// LocalIsDarkTheme rather than being a plain constant, so referencing it (e.g. `color = Ink`)
// from any composable automatically follows the current theme — no call site needs to change.
// LocalIsDarkTheme itself is resolved once by LumenLauncherTheme from ThemeMode (an explicit
// Light/Dark override, or System following isSystemInDarkTheme()) — see Theme.kt.

private val WallpaperLight = Color(0xFFE9ECF2)
private val WallpaperDark = Color(0xFF14171D)
private val CarouselBackdropLight = Color(0xFFDFE3EA)
private val CarouselBackdropDark = Color(0xFF101319)
private val CarouselBackdropDraggingLight = Color(0xFFD3D8E1)
private val CarouselBackdropDraggingDark = Color(0xFF0D1015)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceDark = Color(0xFF171A21)
private val DrawerOverlayLight = Color(0xE0FFFFFF) // rgba(255,255,255,.88)
private val DrawerOverlayDark = Color(0xEB14171D) // rgba(20,23,29,.92)
private val ScrimLight = Color(0x47020817) // rgba(2,8,23,.28)
private val ScrimDark = Color(0x94000000) // rgba(0,0,0,.58)
private val InkLight = Color(0xFF020817)
private val InkDark = Color(0xFFE7EAF0)
private val MutedLight = Color(0x73020817) // rgba(2,8,23,.45)
private val MutedDark = Color(0x80E2E8F0) // rgba(226,232,240,.5)
// Dark "Faint" isn't in README's dark token table — derived at the same alpha *ratio* to Muted
// as the light table's Faint (.3) is to its Muted (.45), applied to Muted-dark's base color.
// Deliberately dimmer than Muted — reserved for decorative/disabled elements (drag-handle icons,
// inactive carousel dots, disabled menu items) where that's the point. Any *readable* secondary
// text that turned out too dim at this alpha uses Muted instead, not a locally-bumped Faint (see
// chat history — `SettingsScreen.kt`'s `SectionHeader` used to work around this with a local
// `Faint.copy(alpha = 0.6f)`, which was the tell that Faint itself was the wrong token there).
private val FaintLight = Color(0x4D020817) // rgba(2,8,23,.3)
private val FaintDark = Color(0x4DE2E8F0) // rgba(226,232,240,.3)
private val HairlineLight = Color(0x12020817) // rgba(2,8,23,.07)
private val HairlineDark = Color(0x12E2E8F0) // rgba(226,232,240,.07)
private val IconTileLight = Color(0xFF1E293B)
private val IconTileDark = Color(0xFF39424F)
// System-default *stand-ins* per README — the real default source is Material You (below);
// these only serve as a defensive fallback if reading the system's dynamic scheme ever fails,
// and as the literal design-reference values (also AccentSwatch.BLUE's exact values).
private val AccentLight = Color(0xFF2563EB)
private val AccentDark = Color(0xFFA8C7FA)
private val ErrorLight = Color(0xFFDC2626)
private val ErrorDark = Color(0xFFF2857F)
private val SuccessLight = Color(0xFF16A34A)
private val SuccessDark = Color(0xFF7FD493)

val Wallpaper: Color @Composable get() = if (LocalIsDarkTheme.current) WallpaperDark else WallpaperLight
val CarouselBackdrop: Color @Composable get() = if (LocalIsDarkTheme.current) CarouselBackdropDark else CarouselBackdropLight
val CarouselBackdropDragging: Color
    @Composable get() = if (LocalIsDarkTheme.current) CarouselBackdropDraggingDark else CarouselBackdropDraggingLight
val Surface: Color @Composable get() = if (LocalIsDarkTheme.current) SurfaceDark else SurfaceLight
val DrawerOverlay: Color @Composable get() = if (LocalIsDarkTheme.current) DrawerOverlayDark else DrawerOverlayLight
val Scrim: Color @Composable get() = if (LocalIsDarkTheme.current) ScrimDark else ScrimLight
val Ink: Color @Composable get() = if (LocalIsDarkTheme.current) InkDark else InkLight
val Muted: Color @Composable get() = if (LocalIsDarkTheme.current) MutedDark else MutedLight
val Faint: Color @Composable get() = if (LocalIsDarkTheme.current) FaintDark else FaintLight
val Hairline: Color @Composable get() = if (LocalIsDarkTheme.current) HairlineDark else HairlineLight
val IconTile: Color @Composable get() = if (LocalIsDarkTheme.current) IconTileDark else IconTileLight

/**
 * F11's accent handling — "one variable driving ~100 usages" per README. Settings → Theme →
 * Accent color offers two sources ([LocalAccentFromSystem]):
 * - **Wallpaper colors** (default): Material You ([dynamicLightColorScheme]/[dynamicDarkColorScheme],
 *   reading the system's wallpaper-derived `system_accent1_*` palette — always available since
 *   minSdk 33 ≥ API 31). No picker shown; it's computed and applied automatically.
 * - **Basic colors**: [LocalCustomAccentSwatch]'s fixed pick from the curated [AccentSwatch]
 *   palette — each swatch carries its own light/dark pair, so switching the app's theme mode
 *   restores the right value for the *same* swatch rather than needing to re-pick.
 *
 * Returns both tonal extremes (light-scheme, dark-scheme) rather than just the one matching the
 * *current* theme mode, so [Accent] can pick the current-theme one. Not `private` — [Accent] is
 * the only other reader, same file, so this could stay private; kept internal-visible only for
 * symmetry with [wallpaperPrimaryAndSecondary] below.
 */
@Composable
private fun accentTonalExtremes(): Pair<Color, Color> {
    val swatch = LocalCustomAccentSwatch.current
    if (!LocalAccentFromSystem.current && swatch != null) {
        return swatch.light to swatch.dark
    }
    val context = LocalContext.current
    // Keyed on the resume signal too (see its own doc) — otherwise this stays cached at
    // whatever the OS's dynamic palette was on first composition, and a wallpaper/theme change
    // made while Lumen sits resumed in the background (the common case for a launcher) wouldn't
    // show up until a full force-stop.
    val refreshSignal = LocalDynamicColorRefreshSignal.current
    return remember(context, refreshSignal) {
        val light = runCatching { dynamicLightColorScheme(context).primary }.getOrDefault(AccentLight)
        val dark = runCatching { dynamicDarkColorScheme(context).primary }.getOrDefault(AccentDark)
        light to dark
    }
}

val Accent: Color
    @Composable get() {
        val (light, dark) = accentTonalExtremes()
        return if (LocalIsDarkTheme.current) dark else light
    }

/**
 * The wallpaper's own two Material You tones — light-scheme primary ("wallpaper primary") and
 * dark-scheme primary ("wallpaper secondary") — always computed from the OS's actual dynamic
 * color scheme, deliberately ignoring [LocalAccentFromSystem]/[LocalCustomAccentSwatch] (the
 * "Basic colors" override [Accent] itself respects). [homeTextShadow] and
 * [com.lumenlauncher.app.data.model.ClockColorOption]'s `WALLPAPER_PRIMARY`/`WALLPAPER_SECONDARY`
 * options are specifically about the wallpaper's own two accent tones — picking a fixed
 * Basic-colors swatch here would defeat the point of offering "the wallpaper's" colors as an
 * explicit choice (see chat history).
 */
@Composable
fun wallpaperPrimaryAndSecondary(): Pair<Color, Color> {
    val context = LocalContext.current
    // Keyed on the resume signal too — see accentTonalExtremes' identical note above.
    val refreshSignal = LocalDynamicColorRefreshSignal.current
    return remember(context, refreshSignal) {
        val primary = runCatching { dynamicLightColorScheme(context).primary }.getOrDefault(AccentLight)
        val secondary = runCatching { dynamicDarkColorScheme(context).primary }.getOrDefault(AccentDark)
        primary to secondary
    }
}
val ErrorColor: Color @Composable get() = if (LocalIsDarkTheme.current) ErrorDark else ErrorLight
val SuccessColor: Color @Composable get() = if (LocalIsDarkTheme.current) SuccessDark else SuccessLight

/**
 * Home-surface text (clock, calendar events, favorites, dock labels) sits directly on the
 * user's wallpaper with no scrim behind it, unlike the Drawer (protected by [DrawerOverlay] and
 * so in no need of a shadow — see [DrawerAppTextColor] etc. below). [homeTextShadow] is the one
 * shared legibility treatment every Home composable already reaches for — tuning it here
 * upgrades every widget at once, not just one.
 *
 * "Crisp" treatment (see chat history — a comparative audit against two real third-party
 * launchers, Slate and Niagara): a symmetric, un-offset blur reads as a tight, high-contrast
 * edge around each glyph — closer to an outline than a drop shadow — and holds up even at a
 * light font weight, unlike the previous `1f`-blur/`50%`-opacity/`(0,2)`-offset shadow, which
 * was too small to read as either a soft glow or a crisp edge. Kept at max opacity (fully
 * opaque) since no Home widget currently needs a directional offset; if one ever does, cap its
 * opacity no lower than 85% rather than reintroducing a weak, barely-visible shadow.
 */
val HomeAppTextColor: Color @Composable get() = Ink
val HomeAppTextColorFaint: Color @Composable get() = HomeAppTextColor.copy(alpha = 0.3f)

/** [homeTextShadow]'s default blur, for the clock's own large (60–80sp) digits. */
private const val CLOCK_SHADOW_BLUR_RADIUS = 2f

/**
 * The blur every *other* Home-surface shadow uses — [homeAppLabelShadow] (app-list/dock labels)
 * and [homeTextShadow]'s calendar callers (`CalendarEventsBlock`, passed explicitly). These all
 * render far smaller than the clock's own digits (11–16sp vs. 60–80sp), so [CLOCK_SHADOW_BLUR_RADIUS] —
 * tuned for those much thicker glyph strokes — covers a proportionally much larger share of these
 * thinner strokes and reads as genuinely blurred rather than a crisp edge (see chat history). Not
 * `private` — read from `ui/home/clock/CalendarEventsBlock.kt` too.
 */
const val SMALL_TEXT_SHADOW_BLUR_RADIUS = 0.6f

/**
 * Clock/calendar text only ([ClockTemplates][com.lumenlauncher.app.ui.home.clock.ClockTemplates]/
 * [CalendarEventsBlock][com.lumenlauncher.app.ui.home.clock.CalendarEventsBlock]) — for Home's
 * app-list labels and dock text, use [homeAppLabelShadow] instead (see chat history: the
 * wallpaper-accent tone this function draws from was reported as looking out of place on plain
 * app labels — it's meant specifically for the surfaces that offer `WALLPAPER_PRIMARY`/
 * `WALLPAPER_SECONDARY` as an explicit text-color choice).
 *
 * A fixed dark shadow only reads as a crisp edge when the text itself is light — around dark
 * text (light theme's [Ink], [com.lumenlauncher.app.data.model.ClockColorOption.BLACK], or a
 * wallpaper tone that happens to resolve dark) the same dark shadow just blurs into the glyphs
 * instead (see chat history). Rather than falling back to a neutral [Ink] tone, the glow is drawn
 * from [wallpaperPrimaryAndSecondary] — picking whichever of the two contrasts against
 * [textColor] (always the actual wallpaper's tones, never a Basic-colors swatch — see that
 * function's own doc). Resolving via [textColor]'s own [Color.luminance] (WCAG relative
 * luminance, ignores alpha — so a muted/alpha variant of a color always agrees with its opaque
 * base here), rather than just following the current theme mode, keeps it correct even for an
 * explicit White/Black override that doesn't track the theme mode.
 */
@Composable
fun homeTextShadow(textColor: Color, blurRadius: Float = CLOCK_SHADOW_BLUR_RADIUS): Shadow {
    val (wallpaperPrimary, wallpaperSecondary) = wallpaperPrimaryAndSecondary()
    val (darkerTone, lighterTone) = if (wallpaperPrimary.luminance() <= wallpaperSecondary.luminance()) {
        wallpaperPrimary to wallpaperSecondary
    } else {
        wallpaperSecondary to wallpaperPrimary
    }
    val shadowColor = if (textColor.luminance() > 0.5f) darkerTone else lighterTone
    return Shadow(color = shadowColor, offset = Offset.Zero, blurRadius = blurRadius)
}

/**
 * Home's app-list labels and dock text (not clock/calendar — see [homeTextShadow]'s doc). Always
 * a plain [Ink] tone — [InkLight] for light text, [InkDark] for dark text — never the wallpaper's
 * accent tones, unlike [homeTextShadow] (see chat history).
 */
@Composable
fun homeAppLabelShadow(textColor: Color): Shadow {
    val shadowColor = if (textColor.luminance() > 0.5f) InkLight else InkDark
    return Shadow(color = shadowColor, offset = Offset.Zero, blurRadius = SMALL_TEXT_SHADOW_BLUR_RADIUS)
}

/**
 * App Drawer text colors — independent knobs from Home's, even though both currently
 * resolve to the same [Ink]/[Faint] values. The Drawer's [DrawerOverlay] already keeps
 * these legible against any wallpaper, so no shadow is needed here.
 */
val DrawerAppTextColor: Color @Composable get() = Ink
val DrawerHeaderTextColor: Color @Composable get() = Faint
val DrawerRailTextColor: Color @Composable get() = Faint
