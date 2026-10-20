package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Which Home layout a window gets. */
enum class HomeLayoutMode { PORTRAIT, LANDSCAPE }

/** Windows narrower than this are a phone held upright; anything wider has room for the landscape structure. */
internal val HOME_PORTRAIT_MAX_WIDTH = 600.dp

/**
 * Follows the window's width, not a rotation flag or its shape: a phone held upright (narrow) gets the
 * portrait layout, and anything 600dp or wider gets the landscape one: a phone on its side, an
 * unfolded foldable, and a tablet in either orientation. [homeLandscapeSpaceFor] then tells a short
 * phone window from a roomy one.
 */
internal fun homeLayoutModeFor(width: Dp): HomeLayoutMode =
    if (width < HOME_PORTRAIT_MAX_WIDTH) HomeLayoutMode.PORTRAIT else HomeLayoutMode.LANDSCAPE

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
    return homeLayoutModeFor(configuration.screenWidthDp.dp)
}

/** [homeLandscapeSpaceFor] for the current window. */
@Composable
internal fun rememberHomeLandscapeSpace(): HomeLandscapeSpace =
    homeLandscapeSpaceFor(LocalConfiguration.current.screenHeightDp.dp)
