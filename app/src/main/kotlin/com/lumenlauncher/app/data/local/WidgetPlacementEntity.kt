package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A widget placed in the Launcher Hub (F5). Keyed on the real `AppWidgetHost`-issued
 * `appWidgetId` rather than a surrogate — delete, orphan-detection (`AppWidgetManager.getAppWidgetInfo`),
 * and `onProviderChanged` all key off that same int, so there's no id translation layer to keep
 * in sync.
 */
@Entity(tableName = "widget_placements")
data class WidgetPlacementEntity(
    @PrimaryKey val appWidgetId: Int,
    val providerPackageName: String,
    val providerClassName: String,
    val row: Int,
    val col: Int,
    val colSpan: Int,
    val rowSpan: Int,
)
