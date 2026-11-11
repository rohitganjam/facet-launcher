package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.WeatherIconStyle

@Entity(tableName = "facets")
data class FacetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,

    /** The clock widget's own horizontal placement — also governs the calendar events strip, which shares it rather than having its own (see chat history: the strip's font/color/weight/alignment were consolidated into Appearance's/clock's shared settings). See [com.facetlauncher.app.data.model.LauncherSettings.clockAlignment]. */
    val clockPlacementOwn: Boolean = false,
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** `null` until this facet drags the clock's grab handle for the first time — see [com.facetlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]. */
    val clockZoneHeightDp: Float? = null,
    /** Landscape's own clock alignment (top, middle or bottom there), zone height (its divider position); `null` uses the portrait value above. */
    val landscapeClockAlignment: ClockAlignment? = null,
    val landscapeClockZoneHeightDp: Float? = null,

    // Apps section: the facet's own values (copied from the launcher defaults when it is created)
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    /** Landscape's own value for [appRowPosition], [appListVerticalAlignment] and [appListColumnAlignment]; `null` is "same as portrait". */
    val landscapeAppRowPosition: AppRowPosition? = null,
    val landscapeAppListVerticalAlignment: AppListVerticalAlignment? = null,
    val landscapeAppListColumnAlignment: AppListColumnAlignment? = null,
    val allowLandscape: Boolean = true,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    val appListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    val appListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    val appListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    val appListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    val appListShrinkToFit: AppListShrinkToFit = AppListShrinkToFit.OFF,

    // Dock section
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,

    // Glass panels
    val dockGlass: GlassStyle = GlassStyle.OFF,
    val appsListGlass: GlassStyle = GlassStyle.OFF,
)
