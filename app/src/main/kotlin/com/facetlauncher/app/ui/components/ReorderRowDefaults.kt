package com.facetlauncher.app.ui.components

import androidx.compose.ui.unit.dp

/**
 * Shared sizing/motion constants for the app-tile and row drag-to-reorder lists that appear in
 * both Settings ([com.facetlauncher.app.ui.settings.DockSettingsScreen],
 * [com.facetlauncher.app.ui.settings.HomeAppsListSettingsScreen],
 * [com.facetlauncher.app.ui.facets.ManageFacetsScreen]) and onboarding's own mirrored copies
 * ([com.facetlauncher.app.ui.onboarding.OnboardingHomeSetupPage]) — each screen still keeps its
 * own row/tile composable per [DragReorderState]'s own "headless, no shared styling" design (so
 * one screen's look can still diverge later), but these particular values were already being
 * hand-retyped identically everywhere (one call site's own doc comment even says so: "matching
 * every other reorderable list in Settings") — see chat history.
 */
object ReorderRowDefaults {
    /** Tile size for the dock's own reorder row is [AppIconSize.TILE] — shared with App Drawer's grid and the real Home Dock, not repeated here. */
    val DOCK_TILE_SPACING = 10.dp
    val FAVORITE_ROW_HEIGHT = 52.dp

    /** The "picked up" lift applied to a tile/row while it's being dragged. */
    val DRAG_ELEVATION = 6.dp
    const val DRAG_SCALE = 1.04f
}
