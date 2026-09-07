package com.lumenlauncher.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole

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

/** Which wallpaper-derived tonal role [Accent] resolves to when [LocalAccentFromSystem] is `true` — see [WallpaperAccentRole]. */
val LocalWallpaperAccentRole = staticCompositionLocalOf { WallpaperAccentRole.PRIMARY }

/**
 * Bumped by [LumenLauncherTheme] on every `ON_RESUME` so [Accent]'s dynamic-color lookup re-reads
 * the OS's current wallpaper-derived palette instead of trusting a value cached from whenever the
 * Activity was first composed. Needed because Lumen, as the Home app, is almost always *resumed*
 * rather than freshly launched — without this, a wallpaper/theme-palette change made in system
 * Settings would only show up in Lumen after a full force-stop, not just returning to Home (see
 * chat history).
 */
val LocalDynamicColorRefreshSignal = staticCompositionLocalOf { 0 }

/**
 * F11's global icon-rendering mode, provided once at the root by [LumenLauncherTheme] so
 * [com.lumenlauncher.app.ui.components.AppIcon] — called from a dozen+ surfaces (Home, Drawer,
 * Dock, context menu, pickers) — can read it without every call site threading it through.
 */
val LocalIconRenderMode = staticCompositionLocalOf { IconRenderMode.SYSTEM_DEFAULT }
