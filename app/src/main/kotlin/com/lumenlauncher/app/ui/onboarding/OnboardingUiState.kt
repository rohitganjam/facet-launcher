package com.lumenlauncher.app.ui.onboarding

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListLimits
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.data.model.ListContentMode

/**
 * Home-setup step (`ONBOARDING_FLOW.md` step 2) state — the launcher-wide default Dock and
 * Favorites/Recents/Most-used content mode. Always the global/default repositories — onboarding
 * has no concept of a `profileId`. Adding/removing apps happens on the full-screen pickers
 * ([com.lumenlauncher.app.ui.dock.DockAppPickerScreen], [com.lumenlauncher.app.ui.profiles.FavoritesPickerScreen])
 * reused as-is from Settings; this state only carries what's needed to render the live dock/
 * favorites rows and drive their in-place drag-reorder.
 */
data class OnboardingUiState(
    /** Dock apps in their current order (the seeded defaults, until edited). */
    val dockApps: List<AppInfo> = emptyList(),
    /** Favorited apps, in their favorites order. */
    val favoriteApps: List<AppInfo> = emptyList(),
    /** Which source populates Home's app list — see `HomeAppsListSettingsScreen` for the same choice in Settings. */
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    /** Only meaningful when [listContentMode] isn't [ListContentMode.FAVORITES] — see `HomeAppsListSettingsScreen`'s own identical row. */
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    /** For the live "your home screen so far" preview (steps 2–4) — see `ui/components/HomeSurfacePreview.kt`. */
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    /** List vs Grid — see `AppDrawerSettingsScreen`'s own identical "Show apps as" row. */
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Set-as-default step (`4h`) — whether Lumen already holds the `HOME` role (e.g. a reinstall), swapping in the "already default" sheet variant. */
    val isDefaultLauncher: Boolean = false,
) {
    val dockCountLabel: String get() = "${dockApps.size} of ${DockAppRepository.MAX_APPS}"
    val favoriteCountLabel: String get() = "${favoriteApps.size} of ${DefaultFavoriteAppRepository.MAX_FAVORITES}"
}
