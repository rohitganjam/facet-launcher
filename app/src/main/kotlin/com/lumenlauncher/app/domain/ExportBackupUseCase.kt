package com.lumenlauncher.app.domain

import android.net.Uri
import com.lumenlauncher.app.data.BackupRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.model.BackupBundle
import com.lumenlauncher.app.data.model.BackupSettings
import com.lumenlauncher.app.data.model.LauncherSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F14 Backup & Restore export — composes every settings/data `Repository` into one [BackupBundle]
 * and hands it to [BackupRepository] to write. See [BackupBundle]'s own doc comment for exactly
 * what is and isn't included and why.
 */
class ExportBackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val backupRepository: BackupRepository,
) {
    /** Returns the bundle it wrote — mainly so callers (and tests) can inspect exactly what got exported without re-deriving it. */
    suspend operator fun invoke(uri: Uri): BackupBundle {
        val settings = settingsRepository.settings.first()
        val profiles = profileRepository.observeProfiles().first()
        val activeProfileIndex = profiles.indexOfFirst { it.id == settings.activeProfileId }.takeIf { it >= 0 }

        val backupProfiles = profiles.map { profile ->
            val favorites = favoriteAppRepository.getRawFavoritesForProfile(profile.id).map { it.toBackupEntry() }
            profile.toBackupProfile(favorites)
        }

        val bundle = BackupBundle(
            exportedAtEpochMillis = System.currentTimeMillis(),
            settings = settings.toBackupSettings(activeProfileIndex),
            profiles = backupProfiles,
            dockApps = dockAppRepository.getRawDockApps().map { it.toBackupEntry() },
            defaultFavoriteApps = defaultFavoriteAppRepository.getRawDefaultFavorites().map { it.toBackupEntry() },
            widgetPlacements = widgetPlacementRepository.observeAll().first().map { it.toBackupPlacement() },
        )
        backupRepository.writeBackup(uri, bundle)
        return bundle
    }
}

private fun LauncherSettings.toBackupSettings(activeProfileIndex: Int?): BackupSettings = BackupSettings(
    use24HourTime = use24HourTime,
    dockDisplayMode = dockDisplayMode.name,
    drawerPresentation = drawerPresentation.name,
    drawerGridSize = drawerGridSize.name,
    drawerListItemSize = drawerListItemSize.name,
    drawerOpacity = drawerOpacity,
    notificationDotsEnabled = notificationDotsEnabled,
    notificationBadgeStyle = notificationBadgeStyle.name,
    showDrawerIcons = showDrawerIcons,
    showDrawerLabels = showDrawerLabels,
    searchBarPosition = searchBarPosition.name,
    activeProfileIndex = activeProfileIndex,
    showAllDayEvents = showAllDayEvents,
    searchContactsEnabled = searchContactsEnabled,
    themeMode = themeMode.name,
    accentFromSystem = accentFromSystem,
    customAccentSwatch = customAccentSwatch,
    wallpaperAccentRole = wallpaperAccentRole.name,
    iconRenderMode = iconRenderMode.name,
    launcherFontOption = launcherFontOption.name,
    appLabelColorOption = appLabelColorOption.name,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    clockTemplateId = clockTemplateId.name,
    clockFontOption = clockFontOption.name,
    clockColorOption = clockColorOption.name,
    clockShowMeridiem = clockShowMeridiem,
    calendarFontOption = calendarFontOption.name,
    calendarColorOption = calendarColorOption.name,
    calendarFontWeight = calendarFontWeight.name,
    homeAppsFontWeight = homeAppsFontWeight.name,
)
