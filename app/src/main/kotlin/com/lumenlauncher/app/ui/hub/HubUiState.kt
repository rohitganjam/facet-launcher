package com.lumenlauncher.app.ui.hub

import com.lumenlauncher.app.domain.HUB_COLUMNS
import com.lumenlauncher.app.domain.HUB_MAX_WIDGETS

/**
 * A single placed widget, ready to render — a stable immutable projection of [com.lumenlauncher.app.domain.HubWidgetState],
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
)

data class HubUiState(
    val widgets: List<HubWidgetUi> = emptyList(),
    val isLoading: Boolean = true,
) {
    val columns: Int get() = HUB_COLUMNS
    val widgetCount: Int get() = widgets.size
    val isAtCapacity: Boolean get() = widgetCount >= HUB_MAX_WIDGETS
    val isEmpty: Boolean get() = widgets.isEmpty()
}
