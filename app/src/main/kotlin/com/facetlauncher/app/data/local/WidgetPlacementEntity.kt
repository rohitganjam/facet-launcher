package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppProfile

/**
 * A widget placed in the Launcher Hub (F5). Keyed on the real `AppWidgetHost`-issued
 * `appWidgetId` rather than a surrogate — delete, orphan-detection (`AppWidgetManager.getAppWidgetInfo`),
 * and `onProviderChanged` all key off that same int, so there's no id translation layer to keep
 * in sync. [profile] records which Android user the provider was bound from — needed to rebind a
 * Work Profile widget correctly (e.g. after a reboot); see `AppWidgetRepository`. [userId] is the
 * real `UserHandle.hashCode()` the provider was bound from (`getIdentifier()` itself is hidden
 * API, not in the public SDK) — the actual rebind target; [profile] is display-only. See
 * [Migrations.MIGRATION_19_20].
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
    val profile: AppProfile = AppProfile.PERSONAL,
    val userId: Int = -1,
)
