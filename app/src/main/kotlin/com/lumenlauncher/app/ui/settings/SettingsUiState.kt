package com.lumenlauncher.app.ui.settings

import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings

data class SettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockApps: List<AppInfo> = emptyList(),
    val defaultFavorites: List<AppInfo> = emptyList(),
    val isDefaultLauncher: Boolean = false,
    val isLoading: Boolean = true,
) {
    /** `null` (nothing explicitly chosen yet) counts as zero here — it isn't the same as "every calendar", it's "none decided". See [LauncherSettings.selectedCalendarIds]. */
    val selectedCalendarCount: Int get() = settings.selectedCalendarIds?.size ?: 0
}
