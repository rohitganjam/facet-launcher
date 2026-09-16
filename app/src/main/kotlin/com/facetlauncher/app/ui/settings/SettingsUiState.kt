package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem

data class SettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val dockItems: List<PlacedItem> = emptyList(),
    val defaultFavorites: List<PlacedItem> = emptyList(),
    val folderCount: Int = 0,
    val isDefaultLauncher: Boolean = false,
    val isLoading: Boolean = true,
    /** Every genuine Work Profile on this device — one settings row per entry; empty hides the section entirely. See [WorkProfileInfo]'s own doc. */
    val workProfiles: List<WorkProfileInfo> = emptyList(),
) {
    /** `null` (nothing explicitly chosen yet) counts as zero here — it isn't the same as "every calendar", it's "none decided". See [LauncherSettings.selectedCalendarIds]. */
    val selectedCalendarCount: Int get() = settings.selectedCalendarIds?.size ?: 0
}
