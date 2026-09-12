package com.facetlauncher.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole

/**
 * App-wide ripple, stronger than Material3's own faint default (see chat history — the default
 * was hard to see, especially over Home's wallpaper). Only the alpha levels change here — the
 * actual tint is passed as [Ink] explicitly at the call site below, *not* left
 * [androidx.compose.ui.graphics.Color.Unspecified]: this app's screens don't wrap their content in
 * a Material3 `Surface` (Home/Drawer rows are plain `Column`/`Box` + `.background()`), so
 * `LocalContentColor` — what `Color.Unspecified` would otherwise defer to — never actually gets
 * set to the theme-aware [Ink] here; it stays stuck at Compose's own hardcoded default (a fixed
 * black), which is why the ripple looked identically dark in both themes before this fix.
 */
private val FacetRippleAlpha = RippleAlpha(
    draggedAlpha = 0.32f,
    focusedAlpha = 0.24f,
    hoveredAlpha = 0.16f,
    pressedAlpha = 0.24f,
)

@Composable
private fun facetColorScheme(isDark: Boolean) = if (isDark) {
    darkColorScheme(
        primary = Accent,
        onPrimary = Surface,
        background = Wallpaper,
        surface = Surface,
        onSurface = Ink,
        onSurfaceVariant = Muted,
        error = ErrorColor,
        outline = Hairline,
    )
} else {
    lightColorScheme(
        primary = Accent,
        onPrimary = Surface,
        background = Wallpaper,
        surface = Surface,
        onSurface = Ink,
        onSurfaceVariant = Muted,
        error = ErrorColor,
        outline = Hairline,
    )
}

/**
 * @param themeMode Settings → Theme → "Select launcher theme" — [ThemeMode.SYSTEM] (default)
 * follows [isSystemInDarkTheme]; [ThemeMode.LIGHT]/[ThemeMode.DARK] force that mode app-wide
 * regardless of the device's own setting.
 * @param accentFromSystem Settings → Theme → Accent color's source — Material You when `true`
 * (default), [customAccentSwatch]'s fixed pick when `false`.
 * @param wallpaperAccentRole Only meaningful when [accentFromSystem] is `true` — which of the
 * wallpaper's three Material You tonal roles backs the accent color.
 * @param launcherFontOption Settings → Appearance → "Font" — the base font for every text role
 * app-wide (via [facetTypography]) except the clock/calendar, which resolve their own font
 * independently (see `ClockFontOption.resolveFontFamily`).
 */
@Composable
fun FacetLauncherTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentFromSystem: Boolean = true,
    customAccentSwatch: AccentSwatch? = null,
    wallpaperAccentRole: WallpaperAccentRole = WallpaperAccentRole.PRIMARY,
    iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    // Re-reads the OS's dynamic color palette on every resume, not just first composition — see
    // LocalDynamicColorRefreshSignal's own doc for why this matters for a launcher specifically.
    var dynamicColorRefreshSignal by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        dynamicColorRefreshSignal++
        onPauseOrDispose { }
    }
    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalAccentFromSystem provides accentFromSystem,
        LocalCustomAccentSwatch provides customAccentSwatch,
        LocalWallpaperAccentRole provides wallpaperAccentRole,
        LocalDynamicColorRefreshSignal provides dynamicColorRefreshSignal,
        LocalIconRenderMode provides iconRenderMode,
    ) {
        MaterialTheme(
            colorScheme = facetColorScheme(isDark),
            typography = facetTypography(launcherFontOption.fontFamily),
        ) {
            CompositionLocalProvider(
                LocalRippleConfiguration provides RippleConfiguration(color = Ink, rippleAlpha = FacetRippleAlpha),
                content = content,
            )
        }
    }
}
