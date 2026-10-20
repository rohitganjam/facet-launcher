package com.facetlauncher.app.ui.home

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Which Home layout a window gets. */
enum class HomeLayoutMode { PORTRAIT, LANDSCAPE }

/** Windows narrower than this are a phone's width, so they keep the portrait layout even when wider than tall. */
internal val HOME_PORTRAIT_MAX_WIDTH = 600.dp

/**
 * Follows the window, not a rotation flag. A window taller than it is wide is portrait whatever its
 * size (a tablet held upright included), and so is one narrower than [HOME_PORTRAIT_MAX_WIDTH]. Any
 * other window, wide enough and not taller than wide (a phone on its side, an unfolded foldable, a
 * tablet on its side), gets the landscape structure. [homeLandscapeSpaceFor] then tells a short phone
 * window from a roomy one.
 */
internal fun homeLayoutModeFor(width: Dp, height: Dp): HomeLayoutMode =
    if (height > width || width < HOME_PORTRAIT_MAX_WIDTH) HomeLayoutMode.PORTRAIT else HomeLayoutMode.LANDSCAPE

/** Landscape windows shorter than this are a phone on its side; taller ones (a tablet, an unfolded foldable) have room to spare. */
internal val HOME_LANDSCAPE_COMPACT_HEIGHT = 480.dp

/** How much vertical room a landscape window has — the same compact-height cut as Material's window size classes. */
enum class HomeLandscapeSpace { COMPACT, ROOMY }

internal fun homeLandscapeSpaceFor(height: Dp): HomeLandscapeSpace =
    if (height < HOME_LANDSCAPE_COMPACT_HEIGHT) HomeLandscapeSpace.COMPACT else HomeLandscapeSpace.ROOMY

/**
 * Whether the landscape layout is allowed at all (Settings → Appearance → Dock & Apps appearance). When it is
 * off, [rememberHomeLayoutMode] is always portrait. Provided app-wide by `LauncherActivity`; allowed by default
 * wherever nothing provides it (previews, tests).
 */
@Suppress("CompositionLocalAllowlist") // Read by every layout-mode consumer (Home, Hub, the route); threading it would touch them all.
val LocalAllowLandscape = staticCompositionLocalOf { true }

/** [homeLayoutModeFor] for the current window (its configuration tracks multi-window and fold changes), always portrait when [LocalAllowLandscape] is off. */
@Composable
internal fun rememberHomeLayoutMode(): HomeLayoutMode {
    val configuration = LocalConfiguration.current
    if (!LocalAllowLandscape.current) return HomeLayoutMode.PORTRAIT
    return homeLayoutModeFor(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
}

/** [homeLandscapeSpaceFor] for the current window. */
@Composable
internal fun rememberHomeLandscapeSpace(): HomeLandscapeSpace =
    homeLandscapeSpaceFor(LocalConfiguration.current.screenHeightDp.dp)

/** The screen orientation the activity requests: portrait only while landscape is off, the device's own choice otherwise. */
internal fun homeRequestedOrientation(allowLandscape: Boolean): Int =
    if (allowLandscape) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
