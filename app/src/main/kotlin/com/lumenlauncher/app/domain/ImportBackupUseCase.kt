package com.lumenlauncher.app.domain

import android.net.Uri
import com.lumenlauncher.app.data.BackupRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.BackupSettings
import com.lumenlauncher.app.data.model.BackupWidgetPlacement
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole
import javax.inject.Inject

sealed interface ImportBackupResult {
    data class Success(
        val profileCount: Int,
        val dockAppCount: Int,
        val defaultFavoriteCount: Int,
        /** Never auto-restored — see [com.lumenlauncher.app.data.model.BackupWidgetPlacement]'s own doc comment. Empty when the backup had none. */
        val pendingWidgetPlacements: List<BackupWidgetPlacement>,
    ) : ImportBackupResult

    /** [uri] wasn't valid JSON, or wasn't shaped like a backup at all. */
    data object InvalidFile : ImportBackupResult

    /** From a newer app version than this one understands — refuse rather than silently dropping fields it doesn't recognize. */
    data class UnsupportedVersion(val backupVersion: Int) : ImportBackupResult
}

/**
 * F14 Backup & Restore import — replaces every current profile/dock/default-favorite with the
 * backup's own (a destructive, full-replace restore, not a merge — the caller must confirm this
 * with the user before invoking; see `BackupRestoreScreen.kt`), then applies every setting field.
 * Widget placements are surfaced back to the caller rather than written automatically — see
 * [ImportBackupResult.Success.pendingWidgetPlacements].
 *
 * Not wrapped in a single Room transaction (a failure partway leaves a partially-restored state)
 * — a deliberate scope trade-off for a rare, user-initiated, already-confirmed operation, not an
 * oversight; revisit if this ever needs a stronger guarantee.
 */
class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(uri: Uri): ImportBackupResult {
        val bundle = backupRepository.readBackup(uri) ?: return ImportBackupResult.InvalidFile
        if (bundle.backupVersion > CURRENT_BACKUP_VERSION) return ImportBackupResult.UnsupportedVersion(bundle.backupVersion)

        applySettings(bundle.settings)

        profileRepository.deleteAllProfiles()
        var activeProfileId: Long? = null
        bundle.profiles.forEachIndexed { index, backupProfile ->
            val newId = profileRepository.restoreProfile(backupProfile.toProfileEntity())
            backupProfile.favorites.forEach { entry ->
                favoriteAppRepository.restoreFavorite(entry.toFavoriteAppEntity(newId))
            }
            if (index == bundle.settings.activeProfileIndex) activeProfileId = newId
        }
        activeProfileId?.let { settingsRepository.setActiveProfileId(it) }

        dockAppRepository.deleteAllDockApps()
        bundle.dockApps.forEach { dockAppRepository.restoreDockApp(it.toDockAppEntity()) }

        defaultFavoriteAppRepository.deleteAllDefaultFavorites()
        bundle.defaultFavoriteApps.forEach { defaultFavoriteAppRepository.restoreDefaultFavorite(it.toDefaultFavoriteAppEntity()) }

        return ImportBackupResult.Success(
            profileCount = bundle.profiles.size,
            dockAppCount = bundle.dockApps.size,
            defaultFavoriteCount = bundle.defaultFavoriteApps.size,
            pendingWidgetPlacements = bundle.widgetPlacements,
        )
    }

    private suspend fun applySettings(settings: BackupSettings) {
        with(settingsRepository) {
            setUse24HourTime(settings.use24HourTime)
            setDockDisplayMode(settings.dockDisplayMode.toEnumOrDefault(DockDisplayMode.ICONS))
            setDrawerPresentation(settings.drawerPresentation.toEnumOrDefault(DrawerPresentation.LIST))
            setDrawerGridSize(settings.drawerGridSize.toEnumOrDefault(DrawerGridSize.FIVE_BY_SIX))
            setDrawerListItemSize(settings.drawerListItemSize.toEnumOrDefault(DrawerListItemSize.REGULAR))
            setDrawerOpacity(settings.drawerOpacity)
            setNotificationDotsEnabled(settings.notificationDotsEnabled)
            setNotificationBadgeStyle(settings.notificationBadgeStyle.toEnumOrDefault(NotificationBadgeStyle.DOT))
            setShowDrawerIcons(settings.showDrawerIcons)
            setShowDrawerLabels(settings.showDrawerLabels)
            setSearchBarPosition(settings.searchBarPosition.toEnumOrDefault(SearchBarPosition.TOP))
            setShowAllDayEvents(settings.showAllDayEvents)
            setSearchContactsEnabled(settings.searchContactsEnabled)
            setThemeMode(settings.themeMode.toEnumOrDefault(ThemeMode.SYSTEM))
            setAccentFromSystem(settings.accentFromSystem)
            settings.customAccentSwatch?.let { setCustomAccentSwatch(it) }
            setWallpaperAccentRole(settings.wallpaperAccentRole.toEnumOrDefault(WallpaperAccentRole.PRIMARY))
            setIconRenderMode(settings.iconRenderMode.toEnumOrDefault(IconRenderMode.SYSTEM_DEFAULT))
            setLauncherFontOption(settings.launcherFontOption.toEnumOrDefault(LauncherFontOption.SYSTEM))
            setAppLabelColorOption(settings.appLabelColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setAppRowPosition(settings.appRowPosition.toEnumOrDefault(AppRowPosition.LEFT))
            setAppRowPresentation(settings.appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT))
            setListContentMode(settings.listContentMode.toEnumOrDefault(ListContentMode.FAVORITES))
            setAppsToShowCount(settings.appsToShowCount)
            setClockTemplateId(settings.clockTemplateId.toEnumOrDefault(ClockTemplateId.LIGHT_STACK))
            setClockFontOption(settings.clockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT))
            setClockColorOption(settings.clockColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setClockShowMeridiem(settings.clockShowMeridiem)
            setCalendarFontOption(settings.calendarFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT))
            setCalendarColorOption(settings.calendarColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setCalendarFontWeight(settings.calendarFontWeight.toEnumOrDefault(FontWeightOption.REGULAR))
            setHomeAppsFontWeight(settings.homeAppsFontWeight.toEnumOrDefault(FontWeightOption.REGULAR))
        }
    }
}
