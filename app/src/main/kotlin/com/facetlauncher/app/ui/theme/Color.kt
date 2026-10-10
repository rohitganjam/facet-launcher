package com.facetlauncher.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.facetlauncher.app.data.model.WallpaperAccentRole

// Light/dark pairs — transcribed from design_handoff_minimal_launcher/README.md's Design
// Tokens table (`### Light`/`### Dark`). Every public color below resolves through
// LocalIsDarkTheme rather than being a plain constant, so referencing it (e.g. `color = Ink`)
// from any composable automatically follows the current theme — no call site needs to change.
// LocalIsDarkTheme itself is resolved once by FacetLauncherTheme from ThemeMode (an explicit
// Light/Dark override, or System following isSystemInDarkTheme()) — see Theme.kt.

private val WallpaperLight = Color(0xFFFFFFFF)
private val WallpaperDark = Color(0xFF14171D)
private val CarouselBackdropLight = Color(0xFFDFE3EA)
private val CarouselBackdropDark = Color(0xFF101319)
private val CarouselBackdropDraggingLight = Color(0xFFD3D8E1)
private val CarouselBackdropDraggingDark = Color(0xFF0D1015)
private val SurfaceLight = Color(0xFFFFFFFF)
// Dark lifted a few steps above README's literal value (0xFF171A21) — that tone sat only ~1.03:1
// against [SurfaceContainerDark], so a SettingsCard read as flush with its page despite the shadow
// and hairline border. 0xFF20242D lands near M3's own dark `surfaceContainer` elevation step (see
// chat history) — same category of deliberate spec departure as the Muted/Faint contrast bumps below.
private val SurfaceDark = Color(0xFF20242D)
// A page's own background, one step dimmer than the [Surface] cards floating on it — previously
// both were the same [Surface] tone, distinguished only by a card's drop shadow/hairline border
// (see `SettingsCard.kt`'s own doc comment). The card is always the *lighter* of the two in both
// themes, matching Material's dark-theme convention of elevated surfaces reading lighter, not
// darker (see chat history). Dark's value coincides with [WallpaperDark] — a coincidence, not a
// reuse, since that token's own job (Home carousel backdrop) is unrelated to this one.
private val SurfaceContainerLight = Color(0xFFEEF1F6)
private val SurfaceContainerDark = Color(0xFF14171D)
private val DrawerOverlayLight = Color(0xE0FFFFFF) // rgba(255,255,255,.88)
private val DrawerOverlayDark = Color(0xEB14171D) // rgba(20,23,29,.92)
private val ScrimLight = Color(0x47020817) // rgba(2,8,23,.28)
private val ScrimDark = Color(0x94000000) // rgba(0,0,0,.58)
private val InkLight = Color(0xFF020817)
private val InkDark = Color(0xFFE7EAF0)
// .55, not README's original .45/.5 — that literal spec value read under WCAG AA's 4.5:1 text
// contrast minimum (~3.2:1 light, ~4.4:1 dark against Surface) despite Muted being relied on for
// actual readable secondary text (see chat history) — a deliberate departure from the spec value,
// same category of call as the M3-shape departure already recorded above.
private val MutedLight = Color(0x8C020817) // rgba(2,8,23,.55)
private val MutedDark = Color(0x8CE2E8F0) // rgba(226,232,240,.55)
// Dark "Faint" isn't in README's dark token table — derived at the same alpha *ratio* to Muted
// as the light table's Faint (.3) is to its *original* Muted (.45, since recalibrated above to
// .55 for contrast — Faint's own derivation wasn't revisited alongside it).
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

/** A settings-style screen's own page background — see this file's own doc comment above [SurfaceContainerLight]. [com.facetlauncher.app.ui.components.SettingsCard]s on top of it stay [Surface]. */
val SurfaceContainer: Color @Composable get() = if (LocalIsDarkTheme.current) SurfaceContainerDark else SurfaceContainerLight
val DrawerOverlay: Color @Composable get() = if (LocalIsDarkTheme.current) DrawerOverlayDark else DrawerOverlayLight
val Scrim: Color @Composable get() = if (LocalIsDarkTheme.current) ScrimDark else ScrimLight
val Ink: Color @Composable get() = if (LocalIsDarkTheme.current) InkDark else InkLight

/**
 * [Ink]'s deliberate opposite — [InkLight] in dark theme, [InkDark] in light theme — backing
 * [com.facetlauncher.app.data.model.ClockColorOption.THEME_INVERTED] (see that enum's own doc).
 */
val InkInverted: Color @Composable get() = if (LocalIsDarkTheme.current) InkLight else InkDark
val Muted: Color @Composable get() = if (LocalIsDarkTheme.current) MutedDark else MutedLight
val Faint: Color @Composable get() = if (LocalIsDarkTheme.current) FaintDark else FaintLight
val Hairline: Color @Composable get() = if (LocalIsDarkTheme.current) HairlineDark else HairlineLight
val IconTile: Color @Composable get() = if (LocalIsDarkTheme.current) IconTileDark else IconTileLight

/** Hue for a Settings section's leading-icon circle (glyph at full [color], circle at a low-alpha tint of it). Fixed colours, independent of the user's [Accent]. */
enum class SettingsSectionHue(private val light: Color, private val dark: Color) {
    FACETS(Color(0xFF2563EB), Color(0xFF93C5FD)),
    APPEARANCE(Color(0xFF7C3AED), Color(0xFFC4B5FD)),
    HOME_APPS(Color(0xFF0D9488), Color(0xFF5EEAD4)),
    SYSTEM(Color(0xFF475569), Color(0xFFCBD5E1)),
    ;

    val color: Color
        @Composable get() = if (LocalIsDarkTheme.current) dark else light
}

/** [FolderTileGlyph][com.facetlauncher.app.ui.home.FolderTileGlyph]'s tile background — deliberately theme-invariant, unlike [IconTile], so the folder plate reads the same over the wallpaper in light or dark mode. */
val FolderGlyphBackground: Color = IconTileDark

/**
 * Maps a [WallpaperAccentRole] to its actual M3 tonal role on a wallpaper-derived [scheme] —
 * shared by [accentTonalExtremes] and Settings' wallpaper-color picker row, which needs each
 * role's live color to render its own swatch circles. `internal`, not `private` — same visibility
 * as [accentTonalExtremes] itself, for the same reason (read from `ui/settings/AppearanceSettingsScreen.kt`).
 */
internal fun WallpaperAccentRole.toneOf(scheme: ColorScheme): Color = when (this) {
    WallpaperAccentRole.PRIMARY -> scheme.primary
    WallpaperAccentRole.SECONDARY -> scheme.secondary
    WallpaperAccentRole.TERTIARY -> scheme.tertiary
}

/**
 * F11's accent handling — "one variable driving ~100 usages" per README. Settings → Theme →
 * Accent color offers two sources ([LocalAccentFromSystem]):
 * - **Wallpaper colors** (default): Material You ([dynamicLightColorScheme]/[dynamicDarkColorScheme],
 *   reading the system's wallpaper-derived `system_accent1/2/3` palette — always available since
 *   minSdk 33 ≥ API 31), with the user's own pick of which of the three tonal roles to use
 *   ([LocalWallpaperAccentRole] — see Settings' wallpaper-color picker row).
 * - **Basic colors**: [LocalCustomAccentSwatch]'s fixed pick from the curated [AccentSwatch]
 *   palette — each swatch carries its own light/dark pair, so switching the app's theme mode
 *   restores the right value for the *same* swatch rather than needing to re-pick.
 *
 * Returns both tonal extremes (light-scheme, dark-scheme) rather than just the one matching the
 * *current* theme mode, so [Accent] can pick the current-theme one. `internal`, not `private` —
 * also read from `ui/theme/ClockColors.kt`'s `ClockColorOption.resolve()` for
 * [com.facetlauncher.app.data.model.ClockColorOption.ACCENT_PRIMARY]/`ACCENT_SECONDARY`.
 */
@Composable
internal fun accentTonalExtremes(): Pair<Color, Color> {
    val swatch = LocalCustomAccentSwatch.current
    if (!LocalAccentFromSystem.current && swatch != null) {
        return swatch.light to swatch.dark
    }
    val context = LocalContext.current
    val role = LocalWallpaperAccentRole.current
    // Keyed on the resume signal too (see its own doc) — otherwise this stays cached at
    // whatever the OS's dynamic palette was on first composition, and a wallpaper/theme change
    // made while Facet sits resumed in the background (the common case for a launcher) wouldn't
    // show up until a full force-stop. Also keyed on the role — switching which tonal role is
    // picked inside the same composition must recompute too.
    val refreshSignal = LocalDynamicColorRefreshSignal.current
    return remember(context, refreshSignal, role) {
        val light = runCatching { role.toneOf(dynamicLightColorScheme(context)) }.getOrDefault(AccentLight)
        val dark = runCatching { role.toneOf(dynamicDarkColorScheme(context)) }.getOrDefault(AccentDark)
        light to dark
    }
}

val Accent: Color
    @Composable get() {
        val (light, dark) = accentTonalExtremes()
        return if (LocalIsDarkTheme.current) dark else light
    }

/**
 * The wallpaper's own two Material You tones — light-scheme primary and dark-scheme primary —
 * always computed from the OS's actual dynamic color scheme, deliberately ignoring
 * [LocalAccentFromSystem]/[LocalCustomAccentSwatch] (the "Basic colors" override [Accent] itself
 * respects). Read only by [homeTextShadow], which draws its glow from the *actual* wallpaper
 * regardless of the user's Accent color source setting — the clock/calendar/app-label text color
 * itself ([com.facetlauncher.app.data.model.ClockColorOption]'s `ACCENT_PRIMARY`/`ACCENT_SECONDARY`)
 * instead follows [accentTonalExtremes], which *does* respect that setting (see chat history).
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
 * The treatment is a soft drop shadow offset down and right, in a translucent ink tone that
 * contrasts with the text. A wallpaper-derived tone or a zero-offset blur vanished on light
 * wallpapers; this matches Niagara's clock and labels.
 */
val HomeAppTextColor: Color @Composable get() = Ink
val HomeAppTextColorFaint: Color @Composable get() = HomeAppTextColor.copy(alpha = 0.3f)

/** [homeTextShadow]'s default blur, in dp, for the clock's large (60–80sp) digits. */
private const val CLOCK_SHADOW_BLUR_RADIUS = 2f

/** Blur in dp for text of 11–16sp (app labels, calendar) — wider than the clock's, since thin strokes need more spread. */
const val SMALL_TEXT_SHADOW_BLUR_RADIUS = 4f

private const val HOME_SHADOW_ALPHA = 0.5f

/** Drop shadow tinted against [textColor]'s luminance, so dark text gets a light shadow. [blurRadius] is in dp. */
@Composable
private fun homeShadow(textColor: Color, blurRadius: Float): Shadow {
    val density = LocalDensity.current.density
    val tone = if (textColor.luminance() > 0.5f) InkLight else InkDark
    return Shadow(
        color = tone.copy(alpha = HOME_SHADOW_ALPHA),
        offset = Offset(blurRadius / 4f * density, blurRadius / 2f * density),
        blurRadius = blurRadius * density,
    )
}

/** Clock, calendar and accessory text. For app-list and dock labels use [homeAppLabelShadow]. */
@Composable
fun homeTextShadow(textColor: Color, blurRadius: Float = CLOCK_SHADOW_BLUR_RADIUS): Shadow =
    homeShadow(textColor, blurRadius)

/**
 * The knocked-out content color for text/icons painted directly on a solid, non-wallpaper panel
 * fill — currently [NegativePanelTemplate][com.facetlauncher.app.ui.home.clock.ClockTemplates]'s
 * digits, meridiem and accessory row, all drawn on top of the clock's own resolved
 * [com.facetlauncher.app.data.model.ClockColorOption] color rather than the wallpaper. A fixed
 * [Surface] read there stayed legible for `THEME` (whose panel and [Surface] land on opposite ends
 * of the current theme) but went low-contrast for `THEME_INVERTED` (whose panel deliberately lands
 * on [Surface]'s *own* end) — deriving from [background]'s actual [Color.luminance] instead keeps
 * both cases (and any other panel fill, e.g. an accent color) correctly flipped.
 */
fun contentColorFor(background: Color): Color = if (background.luminance() > 0.5f) InkLight else InkDark

/** Home's app-list labels and dock text. */
@Composable
fun homeAppLabelShadow(textColor: Color): Shadow = homeShadow(textColor, SMALL_TEXT_SHADOW_BLUR_RADIUS)

/**
 * App Drawer text colors — independent knobs from Home's, even though [DrawerAppTextColor]
 * currently resolves to the same [Ink] value. The Drawer's [DrawerOverlay] already keeps
 * these legible against any wallpaper, so no shadow is needed here.
 *
 * [DrawerHeaderTextColor]/[DrawerRailTextColor] use [Muted], not [Faint] — both are real,
 * readable navigational text (section headers like "APPS"/"CONTACTS", the A-Z fast-scroll rail),
 * not the decorative/disabled use [Faint]'s own doc reserves it for. Mirrors the identical fix
 * `SettingsScreen.kt`'s own `SectionHeader` already made for the same reason (see chat history).
 */
val DrawerAppTextColor: Color @Composable get() = Ink
val DrawerHeaderTextColor: Color @Composable get() = Muted
val DrawerRailTextColor: Color @Composable get() = Muted
