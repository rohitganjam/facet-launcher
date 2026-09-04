package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.ListContentMode

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,

    // Clock section
    val overrideClock: Boolean = false,
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.INK,
    val use24HourTime: Boolean = false,
    val clockShowMeridiem: Boolean = false,

    // Apps section
    val overrideApps: Boolean = false,
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    val appsToShowCount: Int = 5,
    /** 
     * Independent of [overrideApps] flag for the mode/count, but typically switched 
     * together in the UI (see [com.lumenlauncher.app.ui.profiles.ProfileSettingsViewModel.setOverridingApps]).
     * [false] = use global default favorites; [true] = use this profile's own list.
     */
    val overridingFavorites: Boolean = false,

    // Calendar section
    val overrideCalendar: Boolean = false,
    val showAllDayEvents: Boolean = true,
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.INK,
)
