package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind

/** One item of a facet's clock stack, ordered by [position]; see [ClockStackItem]. A facet with no rows has the plain native clock. */
@Entity(
    tableName = "clock_stack_items",
    foreignKeys = [ForeignKey(entity = FacetEntity::class, parentColumns = ["id"], childColumns = ["facetId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["facetId", "position"])],
)
data class ClockStackItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facetId: Long,
    val position: Int,
    val kind: ClockStackItemKind,
    val appWidgetId: Int? = null,
    val widthDp: Int? = null,
    val heightDp: Int? = null,
    val alignment: ClockAlignment? = null,
    val landscapeWidthDp: Int? = null,
    val landscapeHeightDp: Int? = null,
    val landscapeAlignment: ClockAlignment? = null,
)

fun ClockStackItemEntity.toModel() = ClockStackItem(
    id = id, facetId = facetId, position = position, kind = kind, appWidgetId = appWidgetId,
    widthDp = widthDp, heightDp = heightDp, alignment = alignment,
    landscapeWidthDp = landscapeWidthDp, landscapeHeightDp = landscapeHeightDp, landscapeAlignment = landscapeAlignment,
)
