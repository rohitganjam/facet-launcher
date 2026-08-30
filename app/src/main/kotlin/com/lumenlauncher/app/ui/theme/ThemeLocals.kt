package com.lumenlauncher.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the app is currently rendering dark — resolved once by [LumenLauncherTheme] from
 * [com.lumenlauncher.app.data.model.ThemeMode] (an explicit Light/Dark override, or System
 * following [androidx.compose.foundation.isSystemInDarkTheme]) and read from here by every
 * color in `Color.kt` instead of each calling `isSystemInDarkTheme()` itself, so the in-app
 * override actually takes effect everywhere a color is used.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * F11's accent-source settings, provided once at the root by [LumenLauncherTheme] so [Accent]
 * (a plain top-level color, not a composable parameter) can read them without every call site
 * threading them through. Defaults here (Material You on, no custom pick) only apply to
 * previews/tests that render without going through [LumenLauncherTheme] explicitly.
 */
val LocalAccentFromSystem = staticCompositionLocalOf { true }
val LocalCustomAccentSwatch = compositionLocalOf<AccentSwatch?> { null }
