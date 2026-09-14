package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.DefaultFavoriteAppEntity
import com.facetlauncher.app.data.local.DefaultFavoriteFolderPlacementEntity
import com.facetlauncher.app.data.local.DockAppEntity
import com.facetlauncher.app.data.local.DockFolderPlacementEntity
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.local.FavoriteFolderPlacementEntity
import com.facetlauncher.app.data.local.FacetDockAppEntity
import com.facetlauncher.app.data.local.FacetDockFolderPlacementEntity
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.FolderWithApps
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.BackupAppEntry
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupFolder
import com.facetlauncher.app.data.model.BackupFolderPlacement
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.selectedCalendarIds

/** F14 Backup & Restore — entity <-> DTO mapping shared by [ExportBackupUseCase]/[ImportBackupUseCase]. Every enum round-trips through its raw name with a fallback to the field's own default, matching `Converters.kt`'s established pattern. */

fun FavoriteAppEntity.toBackupEntry(): BackupAppEntry = BackupAppEntry(packageName, activityName, position, profile.name)

fun DockAppEntity.toBackupEntry(): BackupAppEntry = BackupAppEntry(packageName, activityName, position, profile.name)

fun FacetDockAppEntity.toBackupEntry(): BackupAppEntry = BackupAppEntry(packageName, activityName, position, profile.name)

fun BackupAppEntry.toFacetDockAppEntity(facetId: Long): FacetDockAppEntity =
    FacetDockAppEntity(facetId = facetId, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

fun DefaultFavoriteAppEntity.toBackupEntry(): BackupAppEntry = BackupAppEntry(packageName, activityName, position, profile.name)

fun BackupAppEntry.toFavoriteAppEntity(facetId: Long): FavoriteAppEntity =
    FavoriteAppEntity(facetId = facetId, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

fun BackupAppEntry.toDockAppEntity(): DockAppEntity =
    DockAppEntity(packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

fun BackupAppEntry.toDefaultFavoriteAppEntity(): DefaultFavoriteAppEntity =
    DefaultFavoriteAppEntity(packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

fun WidgetPlacementEntity.toBackupPlacement(): BackupWidgetPlacement =
    BackupWidgetPlacement(providerPackageName, providerClassName, row, col, colSpan, rowSpan)

fun FolderWithApps.toBackupFolder(): BackupFolder = BackupFolder(
    name = folder.name,
    apps = apps.map { BackupAppEntry(it.packageName, it.activityName, it.position, it.profile.name) },
)

/** [folderIndexById] maps each folder's live row id to its position in [com.facetlauncher.app.data.model.BackupBundle.folders] — `null` only if the folder vanished between reads (a race, not a normal case), in which case the placement is simply dropped rather than exported with a dangling index. */
fun DockFolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let { BackupFolderPlacement(folderIndex = it, position = position) }

fun DefaultFavoriteFolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let { BackupFolderPlacement(folderIndex = it, position = position) }

fun FacetDockFolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let { BackupFolderPlacement(folderIndex = it, position = position) }

fun FavoriteFolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let { BackupFolderPlacement(folderIndex = it, position = position) }

fun FacetEntity.toBackupFacet(
    favorites: List<BackupAppEntry>,
    dockApps: List<BackupAppEntry>,
    dockFolderPlacements: List<BackupFolderPlacement> = emptyList(),
    favoriteFolderPlacements: List<BackupFolderPlacement> = emptyList(),
): BackupFacet = BackupFacet(
    name = name,
    position = position,
    overrideClock = overrideClock,
    clockTemplateId = clockTemplateId.name,
    clockFontOption = clockFontOption.name,
    clockColorOption = clockColorOption.name,
    use24HourTime = use24HourTime,
    clockShowMeridiem = clockShowMeridiem,
    overrideApps = overrideApps,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    overridingFavorites = overridingFavorites,
    overrideDock = overrideDock,
    dockDisplayMode = dockDisplayMode.name,
    dockApps = dockApps,
    overrideCalendar = overrideCalendar,
    showAllDayEvents = showAllDayEvents,
    calendarFontOption = calendarFontOption.name,
    calendarColorOption = calendarColorOption.name,
    calendarFontWeight = calendarFontWeight.name,
    selectedCalendarIds = selectedCalendarIds?.toList(),
    favorites = favorites,
    dockFolderPlacements = dockFolderPlacements,
    favoriteFolderPlacements = favoriteFolderPlacements,
)

/** The restored facet's own row id is always `0` (autogenerated fresh on insert) — see `FacetRepository.restoreFacet`. */
fun BackupFacet.toFacetEntity(): FacetEntity = FacetEntity(
    id = 0,
    name = name,
    position = position,
    overrideClock = overrideClock,
    clockTemplateId = clockTemplateId.toEnumOrDefault(ClockTemplateId.LIGHT_STACK),
    clockFontOption = clockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT),
    clockColorOption = clockColorOption.toEnumOrDefault(ClockColorOption.THEME),
    use24HourTime = use24HourTime,
    clockShowMeridiem = clockShowMeridiem,
    overrideApps = overrideApps,
    appRowPosition = appRowPosition.toEnumOrDefault(AppRowPosition.LEFT),
    appRowPresentation = appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT),
    listContentMode = listContentMode.toEnumOrDefault(ListContentMode.FAVORITES),
    appsToShowCount = appsToShowCount.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
    overridingFavorites = overridingFavorites,
    overrideDock = overrideDock,
    dockDisplayMode = dockDisplayMode.toEnumOrDefault(DockDisplayMode.ICONS),
    overrideCalendar = overrideCalendar,
    showAllDayEvents = showAllDayEvents,
    calendarFontOption = calendarFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT),
    calendarColorOption = calendarColorOption.toEnumOrDefault(ClockColorOption.THEME),
    calendarFontWeight = calendarFontWeight.toEnumOrDefault(FontWeightOption.REGULAR),
    selectedCalendarIdsCsv = selectedCalendarIds?.joinToString(","),
)

/** The restored folder's own row id is always `0` (autogenerated fresh on insert) — see `FolderRepository.restoreFolder`. Its members likewise get fresh ids; [com.facetlauncher.app.data.local.FolderAppEntity.folderId] is filled in by the repository once the new folder id is known. */
fun BackupFolder.toFolderEntity(): com.facetlauncher.app.data.local.FolderEntity =
    com.facetlauncher.app.data.local.FolderEntity(id = 0, name = name)

fun BackupAppEntry.toFolderAppEntity(): com.facetlauncher.app.data.local.FolderAppEntity =
    com.facetlauncher.app.data.local.FolderAppEntity(folderId = 0, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

/** An unrecognized stored name (an old export from before an enum constant was renamed) falls back to [default] rather than failing the whole import — same contract as `Converters.kt`. */
inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)
