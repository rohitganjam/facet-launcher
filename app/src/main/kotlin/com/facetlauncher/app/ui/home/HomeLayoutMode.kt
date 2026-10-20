package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Composable
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

/** [homeLayoutModeFor] for the current window (its configuration tracks multi-window and fold changes). */
@Composable
internal fun rememberHomeLayoutMode(): HomeLayoutMode {
    val configuration = LocalConfiguration.current
    return homeLayoutModeFor(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
}

/** [homeLandscapeSpaceFor] for the current window. */
@Composable
internal fun rememberHomeLandscapeSpace(): HomeLandscapeSpace =
    homeLandscapeSpaceFor(LocalConfiguration.current.screenHeightDp.dp)
