package com.lumenlauncher.app.ui.theme

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
 */
val Accent: Color
    @Composable get() {
        val dark = LocalIsDarkTheme.current
        val swatch = LocalCustomAccentSwatch.current
        if (!LocalAccentFromSystem.current && swatch != null) {
            return if (dark) swatch.dark else swatch.light
        }
        val context = LocalContext.current
        return remember(context, dark) {
            runCatching {
                if (dark) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
            }.getOrDefault(if (dark) AccentDark else AccentLight)
        }
    }
val ErrorColor: Color @Composable get() = if (LocalIsDarkTheme.current) ErrorDark else ErrorLight
val SuccessColor: Color @Composable get() = if (LocalIsDarkTheme.current) SuccessDark else SuccessLight

/**
 * Home-surface text (clock, calendar events, favorites, dock labels) sits directly on the
 * user's wallpaper with no scrim behind it, unlike the Drawer (protected by [DrawerOverlay] and
 * so in no need of a shadow — see [DrawerAppTextColor] etc. below). [HomeTextShadow] is the one
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
val HomeTextShadow = Shadow(
    color = Color(0xFF020817),
    offset = Offset.Zero,
    blurRadius = 8f,
)

/**
 * App Drawer text colors — independent knobs from Home's, even though both currently
 * resolve to the same [Ink]/[Faint] values. The Drawer's [DrawerOverlay] already keeps
 * these legible against any wallpaper, so no shadow is needed here.
 */
val DrawerAppTextColor: Color @Composable get() = Ink
val DrawerHeaderTextColor: Color @Composable get() = Faint
val DrawerRailTextColor: Color @Composable get() = Faint
