package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.local.FolderWithApps
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.BackupAppEntry
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupFolder
import com.facetlauncher.app.data.model.BackupFolderPlacement
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.WeatherIconStyle

/** F14 Backup & Restore — entity <-> DTO mapping shared by [ExportBackupUseCase]/[ImportBackupUseCase]. Every enum round-trips through its raw name with a fallback to the field's own default, matching `Converters.kt`'s established pattern. */

fun AppPlacementEntity.toBackupEntry(): BackupAppEntry = BackupAppEntry(packageName, activityName, position, profile.name)

/** [facetId] null restores into the launcher-wide default list of [list]. */
fun BackupAppEntry.toAppPlacementEntity(list: PlacementList, facetId: Long? = null): AppPlacementEntity =
    AppPlacementEntity(list = list, facetId = facetId, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

fun WidgetPlacementEntity.toBackupPlacement(): BackupWidgetPlacement =
    BackupWidgetPlacement(providerPackageName, providerClassName, row, col, colSpan, rowSpan, profile.name, landscapeOrder)

fun FolderWithApps.toBackupFolder(): BackupFolder = BackupFolder(
    name = folder.name,
    apps = apps.map { BackupAppEntry(it.packageName, it.activityName, it.position, it.profile.name) },
)

/** [folderIndexById] maps each folder's live row id to its position in [com.facetlauncher.app.data.model.BackupBundle.folders] — `null` only if the folder vanished between reads (a race, not a normal case), in which case the placement is simply dropped rather than exported with a dangling index. */
fun FolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let { BackupFolderPlacement(folderIndex = it, position = position) }

fun Facet.toBackupFacet(
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
    clockAccentColorOption = clockAccentColorOption.name,
    clockDateStyle = clockDateStyle.name,
    clockShowAlarm = clockShowAlarm,
    clockShowBattery = clockShowBattery,
    clockShowWeather = clockShowWeather,
    clockWeatherIconStyle = clockWeatherIconStyle.name,
    clockPlacementOwn = clockPlacementOwn,
    clockAlignment = clockAlignment.name,
    clockZoneHeightDp = clockZoneHeightDp,
    clockScale = clockScale,
    landscapeClockAlignment = landscapeClockAlignment?.name,
    landscapeClockZoneHeightDp = landscapeClockZoneHeightDp,
    landscapeClockScale = landscapeClockScale,
    landscapeAppListVerticalAlignment = landscapeAppListVerticalAlignment?.name,
    appListVerticalAlignment = appListVerticalAlignment.name,
    overrideApps = overrideApps,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    appListLayout = appListLayout.name,
    appListColumnAlignment = appListColumnAlignment.name,
    landscapeAppRowPosition = landscapeAppRowPosition?.name,
    landscapeAppListColumnAlignment = landscapeAppListColumnAlignment?.name,
    appListGridColumns = appListGridColumns.name,
    appListGridDisplayMode = appListGridDisplayMode.name,
    appListShrinkToFit = appListShrinkToFit.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    overridingFavorites = overridingFavorites,
    overrideDock = overrideDock,
    dockDisplayMode = dockDisplayMode.name,
    dockApps = dockApps,
    overrideCalendar = overrideCalendar,
    showAllDayEvents = showAllDayEvents,
    showCalendarIndicator = showCalendarIndicator,
    calendarItemsToShow = calendarItemsToShow,
    selectedCalendarIds = selectedCalendarIds?.toList(),
    favorites = favorites,
    dockFolderPlacements = dockFolderPlacements,
    favoriteFolderPlacements = favoriteFolderPlacements,
)

/** The restored facet's own row id is always `0` (autogenerated fresh on insert) — see `FacetRepository.restoreFacet`. */
fun BackupFacet.toFacet(): Facet = Facet(
    id = 0,
    name = name,
    position = position,
    overrideClock = overrideClock,
    clockTemplateId = clockTemplateId.toEnumOrDefault(ClockTemplateId.LIGHT_STACK),
    clockFontOption = clockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT),
    clockColorOption = clockColorOption.toEnumOrDefault(ClockColorOption.THEME),
    use24HourTime = use24HourTime,
    clockShowMeridiem = clockShowMeridiem,
    clockAccentColorOption = clockAccentColorOption.toEnumOrDefault(ClockColorOption.ACCENT_PRIMARY),
    clockDateStyle = clockDateStyle.toEnumOrDefault(ClockDateStyle.FULL),
    clockShowAlarm = clockShowAlarm,
    clockShowBattery = clockShowBattery,
    clockShowWeather = clockShowWeather,
    clockWeatherIconStyle = clockWeatherIconStyle.toEnumOrDefault(WeatherIconStyle.COLORED),
    clockPlacementOwn = clockPlacementOwn ?: overrideClock,
    clockAlignment = clockAlignment.toEnumOrDefault(ClockAlignment.LEFT),
    clockZoneHeightDp = clockZoneHeightDp,
    clockScale = clockScale,
    landscapeClockAlignment = landscapeClockAlignment?.toEnumOrNull(),
    landscapeClockZoneHeightDp = landscapeClockZoneHeightDp,
    landscapeClockScale = landscapeClockScale,
    landscapeAppListVerticalAlignment = landscapeAppListVerticalAlignment?.toEnumOrNull(),
    appListVerticalAlignment = appListVerticalAlignment.toEnumOrDefault(AppListVerticalAlignment.LAUNCHER_DEFAULT),
    overrideApps = overrideApps,
    // A facet's own row falls back to LAUNCHER_DEFAULT, not a concrete value — see
    // Facet.appRowPosition's own doc for why these are resolved independently of
    // overrideApps/overrideDock now, mirroring ClockFontOption's existing facet-scope fallback.
    appRowPosition = appRowPosition.toEnumOrDefault(AppRowPosition.LAUNCHER_DEFAULT),
    appRowPresentation = appRowPresentation.toEnumOrDefault(AppRowPresentation.LAUNCHER_DEFAULT),
    appListLayout = appListLayout.toEnumOrDefault(AppListLayout.LAUNCHER_DEFAULT),
    appListColumnAlignment = appListColumnAlignment.toEnumOrDefault(AppListColumnAlignment.LAUNCHER_DEFAULT),
    landscapeAppRowPosition = landscapeAppRowPosition?.toEnumOrNull(),
    landscapeAppListColumnAlignment = landscapeAppListColumnAlignment?.toEnumOrNull(),
    appListGridColumns = appListGridColumns.toEnumOrDefault(AppListGridColumns.LAUNCHER_DEFAULT),
    appListGridDisplayMode = appListGridDisplayMode.toEnumOrDefault(AppListGridDisplayMode.LAUNCHER_DEFAULT),
    appListShrinkToFit = appListShrinkToFit.toEnumOrDefault(AppListShrinkToFit.LAUNCHER_DEFAULT),
    listContentMode = listContentMode.toEnumOrDefault(ListContentMode.FAVORITES),
    appsToShowCount = appsToShowCount.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
    overridingFavorites = overridingFavorites,
    overrideDock = overrideDock,
    dockDisplayMode = dockDisplayMode.toEnumOrDefault(DockDisplayMode.LAUNCHER_DEFAULT),
    overrideCalendar = overrideCalendar,
    showAllDayEvents = showAllDayEvents,
    showCalendarIndicator = showCalendarIndicator,
    calendarItemsToShow = calendarItemsToShow.coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS),
    selectedCalendarIds = selectedCalendarIds?.toSet(),
)

/** The restored folder's own row id is always `0` (autogenerated fresh on insert) — see `FolderRepository.restoreFolder`. Its members likewise get fresh ids; [com.facetlauncher.app.data.local.FolderAppEntity.folderId] is filled in by the repository once the new folder id is known. */
fun BackupFolder.toFolderEntity(): com.facetlauncher.app.data.local.FolderEntity =
    com.facetlauncher.app.data.local.FolderEntity(id = 0, name = name)

fun BackupAppEntry.toFolderAppEntity(): com.facetlauncher.app.data.local.FolderAppEntity =
    com.facetlauncher.app.data.local.FolderAppEntity(folderId = 0, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL))

/** An unrecognized stored name (an old export from before an enum constant was renamed) falls back to [default] rather than failing the whole import — same contract as `Converters.kt`. */
inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)

/** [toEnumOrDefault] for an optional field: an unrecognized name is just "not set". */
inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    runCatching { enumValueOf<T>(this) }.getOrNull()
