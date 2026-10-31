package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind

fun widgetItem(
    appWidgetId: Int,
    id: Long = appWidgetId.toLong(),
    facetId: Long = 1L,
    position: Int = 0,
    widthDp: Int? = null,
    heightDp: Int? = null,
    landscapeWidthDp: Int? = null,
    landscapeHeightDp: Int? = null,
) = ClockStackItem(
    id = id, facetId = facetId, position = position, kind = ClockStackItemKind.WIDGET, appWidgetId = appWidgetId,
    widthDp = widthDp, heightDp = heightDp, landscapeWidthDp = landscapeWidthDp, landscapeHeightDp = landscapeHeightDp,
)

fun clockItem(id: Long = 100L, facetId: Long = 1L, position: Int = 0) =
    ClockStackItem(id = id, facetId = facetId, position = position, kind = ClockStackItemKind.CLOCK)
