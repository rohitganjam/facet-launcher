package com.lumenlauncher.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.lumenlauncher.app.data.model.ThemeMode

@Composable
private fun lumenColorScheme(isDark: Boolean) = if (isDark) {
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
 */
@Composable
fun LumenLauncherTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentFromSystem: Boolean = true,
    customAccentSwatch: AccentSwatch? = null,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalAccentFromSystem provides accentFromSystem,
        LocalCustomAccentSwatch provides customAccentSwatch,
    ) {
        MaterialTheme(
            colorScheme = lumenColorScheme(isDark),
            typography = LumenTypography,
            content = content,
        )
    }
}
