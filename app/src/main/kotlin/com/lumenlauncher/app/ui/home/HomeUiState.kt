package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode

data class HomeUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockApps: List<AppInfo> = emptyList(),
    /** Favorites, Recents, or Most Used, depending on the active profile's [ListContentMode]. */
    val appListItems: List<AppInfo> = emptyList(),
    val profiles: List<ProfileEntity> = emptyList(),
    val usageAccessGranted: Boolean = false,
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val badgeCounts: Map<String, Int> = emptyMap(),
) {
    private val activeProfile: ProfileEntity?
        get() = profiles.find { it.id == settings.activeProfileId }

    /** The active profile's clock settings, falling back to the global defaults when not overriding. */
    val clockTemplateId: ClockTemplateId
        get() = activeProfile?.let { if (it.overrideClock) it.clockTemplateId else settings.clockTemplateId } ?: settings.clockTemplateId

    val clockFontOption: ClockFontOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockFontOption else settings.clockFontOption } ?: settings.clockFontOption

    val clockColorOption: ClockColorOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockColorOption else settings.clockColorOption } ?: settings.clockColorOption

    val effectiveUse24HourTime: Boolean
        get() = activeProfile?.let { if (it.overrideClock) it.use24HourTime else settings.use24HourTime } ?: settings.use24HourTime

    val clockShowMeridiem: Boolean
        get() = activeProfile?.let { if (it.overrideClock) it.clockShowMeridiem else settings.clockShowMeridiem } ?: settings.clockShowMeridiem

    val activeListContentMode: ListContentMode
        get() = activeProfile?.let { if (it.overrideApps) it.listContentMode else settings.listContentMode } ?: settings.listContentMode

    /** True when the active profile's mode needs `PACKAGE_USAGE_STATS` and it isn't granted yet. */
    val showUsageAccessPrompt: Boolean
        get() = activeListContentMode != ListContentMode.FAVORITES && !usageAccessGranted
}
