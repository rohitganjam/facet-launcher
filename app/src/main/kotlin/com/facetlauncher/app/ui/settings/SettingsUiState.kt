package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem

data class SettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockItems: List<PlacedItem> = emptyList(),
    val defaultFavorites: List<PlacedItem> = emptyList(),
    val folderCount: Int = 0,
    val isDefaultLauncher: Boolean = false,
    val isLoading: Boolean = true,
) {
    /** `null` (nothing explicitly chosen yet) counts as zero here — it isn't the same as "every calendar", it's "none decided". See [LauncherSettings.selectedCalendarIds]. */
    val selectedCalendarCount: Int get() = settings.selectedCalendarIds?.size ?: 0
}
