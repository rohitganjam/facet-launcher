package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.Dp

/** Which Home layout a window gets. */
enum class HomeLayoutMode { PORTRAIT, LANDSCAPE }

/**
 * Follows the window's shape, not a rotation flag, so an unfolded foldable (near-square, but wider
 * than tall) gets the landscape layout and a tablet in portrait does not.
 */
internal fun homeLayoutModeFor(width: Dp, height: Dp): HomeLayoutMode =
    if (width > height) HomeLayoutMode.LANDSCAPE else HomeLayoutMode.PORTRAIT
