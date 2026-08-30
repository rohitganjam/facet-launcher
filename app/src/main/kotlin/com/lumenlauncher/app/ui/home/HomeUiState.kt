package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
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
    /** The active profile's 24-hour-time override, falling back to the global default when unset. */
    val effectiveUse24HourTime: Boolean
        get() = profiles.find { it.id == settings.activeProfileId }?.use24HourTimeOverride ?: settings.use24HourTime

    val activeListContentMode: ListContentMode
        get() = profiles.find { it.id == settings.activeProfileId }?.listContentModeOverride ?: settings.listContentMode

    /** True when the active profile's mode needs `PACKAGE_USAGE_STATS` and it isn't granted yet. */
    val showUsageAccessPrompt: Boolean
        get() = activeListContentMode != ListContentMode.FAVORITES && !usageAccessGranted
}
