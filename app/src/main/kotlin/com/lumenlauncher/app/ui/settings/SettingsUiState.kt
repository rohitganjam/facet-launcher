package com.lumenlauncher.app.ui.settings

import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings

data class SettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockApps: List<AppInfo> = emptyList(),
    val defaultFavorites: List<AppInfo> = emptyList(),
    val isDefaultLauncher: Boolean = false,
    val isLoading: Boolean = true,
)
