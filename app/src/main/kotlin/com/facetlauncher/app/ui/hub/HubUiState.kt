package com.facetlauncher.app.ui.hub

import com.facetlauncher.app.domain.HUB_COLUMNS
import com.facetlauncher.app.domain.HUB_MAX_WIDGETS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * A single placed widget, ready to render — a stable immutable projection of [com.facetlauncher.app.domain.HubWidgetState],
 * never a raw `AppWidgetProviderInfo`/`AppWidgetHostView` (those aren't stable/immutable and
 * would defeat Compose's recomposition-skipping, per `CLAUDE.md`).
 */
data class HubWidgetUi(
    val appWidgetId: Int,
    val row: Int,
    val col: Int,
    val colSpan: Int,
    val rowSpan: Int,
    val providerLabel: String?,
    val isOrphaned: Boolean,
    val landscapeOrder: Int? = null,
)

/** A widget placed in the landscape grid: [row]/[col] are landscape's own, packed from the saved order (see [com.facetlauncher.app.domain.LandscapeHubUseCase]), and [rowSpan] is clamped to the rows shown. */
data class HubLandscapeWidgetUi(
    val appWidgetId: Int,
    val row: Int,
    val col: Int,
    val colSpan: Int,
    val rowSpan: Int,
    val providerLabel: String?,
    val isOrphaned: Boolean,
)

data class HubUiState(
    val widgets: ImmutableList<HubWidgetUi> = persistentListOf(),
    /** The widgets laid out for landscape in the number of rows the screen last reported; empty until it has. */
    val landscapeWidgets: ImmutableList<HubLandscapeWidgetUi> = persistentListOf(),
    val isLoading: Boolean = true,
) {
    val columns: Int get() = HUB_COLUMNS
    val widgetCount: Int get() = widgets.size
    val isAtCapacity: Boolean get() = widgetCount >= HUB_MAX_WIDGETS
    val isEmpty: Boolean get() = widgets.isEmpty()
}
