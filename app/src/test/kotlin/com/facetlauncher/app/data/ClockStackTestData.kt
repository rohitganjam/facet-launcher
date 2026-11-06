package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.encode

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

/** A clock page that holds [config] as its own settings. */
fun clockItemWith(config: ClockWidgetConfig, id: Long = 100L, facetId: Long = 1L, position: Int = 0) =
    clockItem(id, facetId, position).copy(nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())

fun nativeItem(
    type: NativeWidgetType,
    id: Long = 200L,
    facetId: Long = 1L,
    position: Int = 0,
    widthDp: Int? = null,
    heightDp: Int? = null,
) = ClockStackItem(
    id = id, facetId = facetId, position = position, kind = ClockStackItemKind.NATIVE_WIDGET, nativeWidgetType = type,
    widthDp = widthDp, heightDp = heightDp,
)
