package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.ListContentMode

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int,

    // Clock + Calendar design section — one override flag for the whole visual block (see
    // ProfileRepository.updateOverridingClock's own doc comment): template, font, color, time
    // format, plus the calendar events strip's own font/color (calendarFontOption/calendarColorOption
    // moved in from what used to be a separate Calendar-design override — see chat history).
    val overrideClock: Boolean = false,
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.THEME,
    val use24HourTime: Boolean = false,
    val clockShowMeridiem: Boolean = false,
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,

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

    // Calendar selection section — content/productivity concerns (which calendars, which kinds
    // of events), deliberately separate from the Clock+Calendar *design* section above (see
    // ProfileRepository.updateOverridingCalendar's own doc comment).
    val overrideCalendar: Boolean = false,
    val showAllDayEvents: Boolean = true,
    /**
     * Comma-joined [com.lumenlauncher.app.data.model.CalendarInfo.id] values — Room has no
     * native `Set<String>` column type, and ids are always numeric so a comma delimiter is safe
     * (same manual-encoding style [com.lumenlauncher.app.data.SettingsRepository.calendarColors]
     * already uses). `null` means every calendar is implicitly selected, mirroring
     * [com.lumenlauncher.app.data.model.LauncherSettings.selectedCalendarIds]'s own null
     * semantics exactly. Never read/written directly outside [com.lumenlauncher.app.data.ProfileRepository] —
     * see its `selectedCalendarIds` extension property for the decoded `Set<String>?` view.
     */
    val selectedCalendarIdsCsv: String? = null,
)
