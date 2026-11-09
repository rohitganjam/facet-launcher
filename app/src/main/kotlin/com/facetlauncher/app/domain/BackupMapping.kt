package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.BackupNativeStackPage
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
import com.facetlauncher.app.data.model.BackupGridCells
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
import com.facetlauncher.app.data.model.decodeWeatherLocation
import com.facetlauncher.app.data.model.encodeWeatherLocation

/** F14 Backup & Restore — entity <-> DTO mapping shared by [ExportBackupUseCase]/[ImportBackupUseCase]. Every enum round-trips through its raw name with a fallback to the field's own default, matching `Converters.kt`'s established pattern. */

fun AppPlacementEntity.toBackupEntry(): BackupAppEntry =
    BackupAppEntry(packageName, activityName, position, profile.name, backupCells(portraitRow, portraitColumn, landscapeRow, landscapeColumn))

/** Null when the item has never been moved on a grid, so an old-style backup stays small. */
private fun backupCells(portraitRow: Int?, portraitColumn: Int?, landscapeRow: Int?, landscapeColumn: Int?): BackupGridCells? =
    if (listOf(portraitRow, portraitColumn, landscapeRow, landscapeColumn).all { it == null }) null
    else BackupGridCells(portraitRow, portraitColumn, landscapeRow, landscapeColumn)

/** [facetId] null restores into the launcher-wide default list of [list]. */
fun BackupAppEntry.toAppPlacementEntity(list: PlacementList, facetId: Long? = null): AppPlacementEntity =
    AppPlacementEntity(list = list, facetId = facetId, packageName = packageName, activityName = activityName, position = position, profile = profile.toEnumOrDefault(AppProfile.PERSONAL),
        portraitRow = gridCells?.portraitRow, portraitColumn = gridCells?.portraitColumn,
        landscapeRow = gridCells?.landscapeRow, landscapeColumn = gridCells?.landscapeColumn,
    )

fun WidgetPlacementEntity.toBackupPlacement(): BackupWidgetPlacement =
    BackupWidgetPlacement(providerPackageName, providerClassName, row, col, colSpan, rowSpan, profile.name, landscapeOrder, nativeWidgetType?.name,
        decodeNativeWidgetConfig(nativeWidgetConfig).copy(calendarIds = null).encode())

/** A restored native placement under [appWidgetId], a negative id of its own; hosted widgets are never restored this way. */
fun BackupWidgetPlacement.toNativePlacement(appWidgetId: Int, type: NativeWidgetType): WidgetPlacementEntity = WidgetPlacementEntity(
    appWidgetId = appWidgetId, providerPackageName = "", providerClassName = "", row = row, col = col, colSpan = colSpan, rowSpan = rowSpan,
    landscapeOrder = landscapeOrder, nativeWidgetType = type, nativeWidgetConfig = nativeWidgetConfig,
)

fun FolderWithApps.toBackupFolder(): BackupFolder = BackupFolder(
    name = folder.name,
    apps = apps.map { BackupAppEntry(it.packageName, it.activityName, it.position, it.profile.name) },
)

/** [folderIndexById] maps each folder's live row id to its position in [com.facetlauncher.app.data.model.BackupBundle.folders] — `null` only if the folder vanished between reads (a race, not a normal case), in which case the placement is simply dropped rather than exported with a dangling index. */
fun FolderPlacementEntity.toBackupPlacement(folderIndexById: Map<Long, Int>): BackupFolderPlacement? =
    folderIndexById[folderId]?.let {
        BackupFolderPlacement(folderIndex = it, position = position, gridCells = backupCells(portraitRow, portraitColumn, landscapeRow, landscapeColumn))
    }

fun Facet.toBackupFacet(
    favorites: List<BackupAppEntry>,
    dockApps: List<BackupAppEntry>,
    dockFolderPlacements: List<BackupFolderPlacement> = emptyList(),
    favoriteFolderPlacements: List<BackupFolderPlacement> = emptyList(),
): BackupFacet {
    val clockPage = clockStack.firstOrNull { it.kind == ClockStackItemKind.CLOCK }
    return BackupFacet(
    name = name,
    position = position,
    clockPlacementOwn = clockPlacementOwn,
    clockAlignment = clockAlignment.name,
    clockZoneHeightDp = clockZoneHeightDp,
    landscapeClockAlignment = landscapeClockAlignment?.name,
    landscapeClockZoneHeightDp = landscapeClockZoneHeightDp,
    landscapeAppListVerticalAlignment = landscapeAppListVerticalAlignment?.name,
    appListVerticalAlignment = appListVerticalAlignment.name,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    appListLayout = appListLayout.name,
    appListColumnAlignment = appListColumnAlignment.name,
    landscapeAppRowPosition = landscapeAppRowPosition?.name,
    landscapeAppListColumnAlignment = landscapeAppListColumnAlignment?.name,
    allowLandscape = allowLandscape,
    appListGridColumns = appListGridColumns.name,
    appListGridDisplayMode = appListGridDisplayMode.name,
    appListShrinkToFit = appListShrinkToFit.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    dockDisplayMode = dockDisplayMode.name,
    dockApps = dockApps,
    favorites = favorites,
    dockFolderPlacements = dockFolderPlacements,
    favoriteFolderPlacements = favoriteFolderPlacements,
    nativeStackPages = clockStack.mapNotNull { it.toBackupNativePage() },
    clockConfig = clockPage?.let { decodeNativeWidgetConfig(it.nativeWidgetConfig).clock }?.let { NativeWidgetConfig(clock = it.copy(calendar = it.calendar.copy(calendarIds = null))).encode() },
    clockWidthDp = clockPage?.widthDp,
    clockHeightDp = clockPage?.heightDp,
    clockLandscapeWidthDp = clockPage?.landscapeWidthDp,
    clockLandscapeHeightDp = clockPage?.landscapeHeightDp,
)
}

private fun ClockStackItem.toBackupNativePage() = nativeWidgetType?.let {
    BackupNativeStackPage(
        it.name, widthDp, heightDp, alignment?.name, landscapeWidthDp, landscapeHeightDp, landscapeAlignment?.name,
        decodeNativeWidgetConfig(nativeWidgetConfig).copy(calendarIds = null).encode(),
    )
}

/** The page as a stack item for the facet; `null` for a type this version does not know, so a newer backup still restores. */
fun BackupNativeStackPage.toStackItem(): ClockStackItem? = type.toEnumOrNull<NativeWidgetType>()?.let {
    ClockStackItem(
        id = 0L, facetId = 0L, position = 0, kind = ClockStackItemKind.NATIVE_WIDGET, nativeWidgetType = it,
        widthDp = widthDp, heightDp = heightDp, alignment = alignment?.toEnumOrNull(),
        landscapeWidthDp = landscapeWidthDp, landscapeHeightDp = landscapeHeightDp, landscapeAlignment = landscapeAlignment?.toEnumOrNull(), nativeWidgetConfig = nativeWidgetConfig,
    )
}

/** The restored facet's own row id is always `0` (autogenerated fresh on insert) — see `FacetRepository.restoreFacet`. */
fun BackupFacet.toFacet(): Facet = Facet(
    id = 0,
    name = name,
    position = position,
    clockPlacementOwn = clockPlacementOwn ?: overrideClock,
    clockAlignment = clockAlignment.toEnumOrDefault(ClockAlignment.LEFT),
    clockZoneHeightDp = clockZoneHeightDp,
    landscapeClockAlignment = landscapeClockAlignment?.toEnumOrNull(),
    landscapeClockZoneHeightDp = landscapeClockZoneHeightDp,
    landscapeAppListVerticalAlignment = landscapeAppListVerticalAlignment?.toEnumOrNull(),
    appListVerticalAlignment = appListVerticalAlignment.toEnumOrDefault(AppListVerticalAlignment.BOTTOM),
    appRowPosition = appRowPosition.toEnumOrDefault(AppRowPosition.LEFT),
    appRowPresentation = appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT),
    appListLayout = appListLayout.toEnumOrDefault(AppListLayout.SINGLE_COLUMN),
    appListColumnAlignment = appListColumnAlignment.toEnumOrDefault(AppListColumnAlignment.BOTH_LEFT),
    landscapeAppRowPosition = landscapeAppRowPosition?.toEnumOrNull(),
    landscapeAppListColumnAlignment = landscapeAppListColumnAlignment?.toEnumOrNull(),
    allowLandscape = allowLandscape,
    appListGridColumns = appListGridColumns.toEnumOrDefault(AppListGridColumns.FOUR),
    appListGridDisplayMode = appListGridDisplayMode.toEnumOrDefault(AppListGridDisplayMode.ICONS),
    appListShrinkToFit = appListShrinkToFit.toEnumOrDefault(AppListShrinkToFit.OFF),
    listContentMode = listContentMode.toEnumOrDefault(ListContentMode.FAVORITES),
    appsToShowCount = appsToShowCount.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW),
    dockDisplayMode = dockDisplayMode.toEnumOrDefault(DockDisplayMode.ICONS),
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
