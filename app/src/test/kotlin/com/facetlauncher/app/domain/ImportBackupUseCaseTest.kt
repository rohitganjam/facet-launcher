package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.BackupNativeStackPage
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.LandscapeTransformCaptor
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.LandscapeOverrides
import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.BackupAppEntry
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.PlacementRepository

class ImportBackupUseCaseTest {

    private val backupRepository = mock(BackupRepository::class.java)
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val placementRepository = mock(PlacementRepository::class.java)
    private val folderRepository = mock(FolderRepository::class.java)
    private val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)
    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)

    private val useCase = ImportBackupUseCase(
        backupRepository,
        settingsRepository,
        facetRepository,
        placementRepository,
        folderRepository,
        repairOrphanedProfileRows,
        clockStackRepository,
        widgetPlacementRepository,
    )

    private fun minimalSettings(activeFacetIndex: Int? = null) = BackupSettings(
        use24HourTime = true,
        dockDisplayMode = "TEXT",
        drawerPresentation = "GRID",
        drawerGridSize = "FOUR_BY_FOUR",
        drawerListItemSize = "COMPACT",
        drawerOpacity = 0.5f,
        notificationDotsEnabled = false,
        notificationBadgeStyle = "COUNT",
        showNotificationsInAppMenu = false,
        homeOpensSearch = true,
        doubleTapToLock = true,
        showDrawerIcons = false,
        showDrawerLabels = false,
        searchBarPosition = "BOTTOM",
        activeFacetIndex = activeFacetIndex,
        showAllDayEvents = false,
        searchContactsEnabled = true,
        themeMode = "DARK",
        systemBarIconStyle = "LIGHT",
        accentFromSystem = false,
        customAccentSwatch = "BLUE",
        wallpaperAccentRole = "SECONDARY",
        iconRenderMode = "MONOCHROME",
        launcherFontOption = "SYSTEM",
        appLabelColorOption = "ACCENT_SECONDARY",
        appRowPosition = "RIGHT",
        appRowPresentation = "TEXT_ONLY",
        appListLayout = "GRID",
        appListColumnAlignment = "MIRRORED",
        appListGridColumns = "SIX",
        appListGridDisplayMode = "TEXT",
        listContentMode = "RECENTS",
        appsToShowCount = 6,
        clockTemplateId = "RULE",
        clockFontOption = "LAUNCHER_DEFAULT",
        clockColorOption = "THEME_INVERTED",
        clockShowMeridiem = true,
    )

    @Test
    fun `an unreadable or non-backup file returns InvalidFile without touching any repository`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(null)

        val result = useCase(uri)

        assertEquals(ImportBackupResult.InvalidFile, result)
        verify(facetRepository, org.mockito.Mockito.never()).deleteAllFacets()
    }

    @Test
    fun `a backup from a newer app version is rejected without touching any repository`() = runTest {
        val uri = mock(Uri::class.java)
        val bundle = BackupBundle(
            backupVersion = CURRENT_BACKUP_VERSION + 1,
            exportedAtEpochMillis = 0L,
            settings = minimalSettings(),
            facets = emptyList(),
            dockApps = emptyList(),
            defaultFavoriteApps = emptyList(),
            widgetPlacements = emptyList(),
        )
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle)

        val result = useCase(uri)

        assertEquals(ImportBackupResult.UnsupportedVersion(CURRENT_BACKUP_VERSION + 1), result)
        verify(facetRepository, org.mockito.Mockito.never()).deleteAllFacets()
    }

    @Test
    fun `a valid backup replaces every facet, dock, and default favorite, then reports what was restored`() = runTest {
        // Given a backup with one facet (with a favorite), one dock app, one default favorite, and one pending widget
        val uri = mock(Uri::class.java)
        val bundle = BackupBundle(
            exportedAtEpochMillis = 0L,
            settings = minimalSettings(activeFacetIndex = 0),
            facets = listOf(
                BackupFacet(
                    name = "Work", position = 0, overrideClock = false, clockTemplateId = "LIGHT_STACK",
                    clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME", use24HourTime = false,
                    clockShowMeridiem = false, overrideApps = false, appRowPosition = "LEFT",
                    appRowPresentation = "ICON_AND_TEXT", listContentMode = "FAVORITES", appsToShowCount = 5,
                    overridingFavorites = true, overrideCalendar = false, showAllDayEvents = true,
                    selectedCalendarIds = listOf("3", "4"),
                    favorites = listOf(BackupAppEntry(packageName = "com.example.a", activityName = ".Main", position = 0)),
                    overrideDock = true,
                    dockDisplayMode = "TEXT",
                    dockApps = listOf(BackupAppEntry(packageName = "com.example.dock", activityName = ".Main", position = 0)),
                ),
            ),
            dockApps = listOf(BackupAppEntry(packageName = "com.example.b", activityName = ".Main", position = 0)),
            defaultFavoriteApps = listOf(BackupAppEntry(packageName = "com.example.c", activityName = ".Main", position = 0)),
            widgetPlacements = listOf(BackupWidgetPlacement("com.example.widgets", ".W", row = 1, col = 2, colSpan = 2, rowSpan = 1, landscapeOrder = 4)),
        )
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle)
        // A concrete expected entity, not any(...): Mockito's static any()/eq() return a raw
        // `null` placeholder, which Kotlin's compiler-inserted non-null check on
        // restoreFacet's `facet: Facet` parameter rejects outright.
        val expectedFacetEntity = Facet(
            id = 0, name = "Work", position = 0,
            appRowPosition = AppRowPosition.LEFT, appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
            listContentMode = ListContentMode.FAVORITES, appsToShowCount = 5,
            dockDisplayMode = com.facetlauncher.app.data.model.DockDisplayMode.TEXT,
        )
        `when`(facetRepository.restoreFacet(expectedFacetEntity)).thenReturn(99L)

        // When importing
        val result = useCase(uri) as ImportBackupResult.Success

        // Then existing data is wiped first...
        verify(folderRepository).deleteAllFolders()
        verify(facetRepository).deleteAllFacets()
        verify(placementRepository).deleteAll(PlacementTarget.GlobalDock)
        verify(placementRepository).deleteAll(PlacementTarget.GlobalFavorites)
        // ...the backup's own data is restored, favorites attached to the newly-created facet's real id...
        verify(placementRepository).restoreApp(AppPlacementEntity(list = PlacementList.FAVORITES, facetId = 99L, packageName = "com.example.a", activityName = ".Main", position = 0))
        verify(placementRepository).restoreApp(AppPlacementEntity(list = PlacementList.DOCK, facetId = 99L, packageName = "com.example.dock", activityName = ".Main", position = 0))
        // ...the active facet resolves from an index to that new id...
        verify(settingsRepository).setActiveFacetId(99L)
        // ...every other setting field is applied...
        verify(settingsRepository).setUse24HourTime(true)
        verify(settingsRepository).setShowNotificationsInAppMenu(false)
        verify(settingsRepository).setHomeOpensSearch(true)
        verify(settingsRepository).setDoubleTapToLock(true)
        verify(settingsRepository).setThemeMode(com.facetlauncher.app.data.model.ThemeMode.DARK)
        verify(settingsRepository).setSystemBarIconStyle(com.facetlauncher.app.data.model.SystemBarIconStyle.LIGHT)
        verify(settingsRepository).setAppListLayout(com.facetlauncher.app.data.model.AppListLayout.GRID)
        verify(settingsRepository).setAppListColumnAlignment(com.facetlauncher.app.data.model.AppListColumnAlignment.MIRRORED)
        verify(settingsRepository).setAppListGridColumns(com.facetlauncher.app.data.model.AppListGridColumns.SIX)
        verify(settingsRepository).setAppListGridDisplayMode(com.facetlauncher.app.data.model.AppListGridDisplayMode.TEXT)
        // ...restored rows (which land with no resolvable userId — see BackupBundle's own doc
        // comment) are repaired immediately, not left orphaned until the next app launch...
        verify(repairOrphanedProfileRows).invoke()
        // ...and widgets are reported back, not silently written to widget_placements.
        assertEquals(1, result.facetCount)
        assertEquals(1, result.dockAppCount)
        assertEquals(1, result.defaultFavoriteCount)
        assertEquals(1, result.pendingWidgetPlacements.size)
        assertEquals("com.example.widgets", result.pendingWidgetPlacements[0].providerPackageName)
        assertEquals(4, result.pendingWidgetPlacements[0].landscapeOrder)
    }

    @Test
    fun `an unrecognized enum string in the backup falls back to that field's default instead of failing the import`() = runTest {
        val uri = mock(Uri::class.java)
        val bundle = BackupBundle(
            exportedAtEpochMillis = 0L,
            settings = minimalSettings().copy(themeMode = "NOT_A_REAL_VALUE"),
            facets = emptyList(),
            dockApps = emptyList(),
            defaultFavoriteApps = emptyList(),
            widgetPlacements = emptyList(),
        )
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle)

        val result = useCase(uri)

        assertTrue(result is ImportBackupResult.Success)
        verify(settingsRepository).setThemeMode(com.facetlauncher.app.data.model.ThemeMode.SYSTEM)
    }

    @Test
    fun `restores the whole landscape group, replacing what was there`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            BackupBundle(
                exportedAtEpochMillis = 0L,
                settings = minimalSettings().copy(
                    landscapeAppRowPosition = "CENTER", landscapeAppListColumnAlignment = "MIRRORED",
                    landscapeClockAlignment = "RIGHT", landscapeClockZoneHeightDp = 310f,
                    landscapeAppListVerticalAlignment = "TOP",
                ),
                facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
            ),
        )

        useCase(uri)

        val captor = LandscapeTransformCaptor()
        verify(settingsRepository).updateLandscape(captor.capture())
        // A full restore replaces the group: the old scale is gone, the backed-up values are in
        val before = LandscapeOverrides(appRowPosition = AppRowPosition.LEFT)
        assertEquals(
            LandscapeOverrides(
                clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 310f,
                appRowPosition = AppRowPosition.CENTER, appListVerticalAlignment = AppListVerticalAlignment.TOP,
                appListColumnAlignment = AppListColumnAlignment.MIRRORED,
            ),
            captor.last(before),
        )
    }

    @Test
    fun `restores the clock and list settings that older backups left out`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            BackupBundle(
                exportedAtEpochMillis = 0L,
                settings = minimalSettings().copy(
                    clockAlignment = "RIGHT",
                    clockZoneHeightDp = 240f, appListVerticalAlignment = "TOP",
                    recentlyInstalledPosition = "SHOW_LAST", searchSettingsEnabled = true,
                ),
                facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
            ),
        )

        useCase(uri)

        verify(settingsRepository).setClockAlignment(ClockAlignment.RIGHT)
        verify(settingsRepository).setClockZoneHeight(240f)
        verify(settingsRepository).setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
        verify(settingsRepository).setRecentlyInstalledPosition(com.facetlauncher.app.data.model.RecentlyInstalledPosition.SHOW_LAST)
        verify(settingsRepository).setSearchSettingsEnabled(true)
    }

    @Test
    fun `restores the icon pack and its fallback, and an older backup without them clears the pack`() = runTest {
        val withPack = mock(Uri::class.java)
        val older = mock(Uri::class.java)
        fun bundle(settings: com.facetlauncher.app.data.model.BackupSettings) = BackupBundle(
            exportedAtEpochMillis = 0L, settings = settings,
            facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
        )
        `when`(backupRepository.readBackup(withPack)).thenReturn(bundle(minimalSettings().copy(iconPackPackage = "com.example.pack", iconPackFallback = "KEEP_DEFAULT", forceIconShape = true)))
        `when`(backupRepository.readBackup(older)).thenReturn(bundle(minimalSettings()))

        useCase(withPack)
        useCase(older)

        verify(settingsRepository).setIconPack("com.example.pack")
        verify(settingsRepository).setIconPackFallback(com.facetlauncher.app.data.model.IconPackFallback.KEEP_DEFAULT)
        verify(settingsRepository).setForceIconShape(true)
        verify(settingsRepository).setIconPack(null)
    }

    @Test
    fun `restores icon size and shrink list to fit, an older backup getting their defaults`() = runTest {
        val newer = mock(Uri::class.java)
        val older = mock(Uri::class.java)
        fun bundle(settings: com.facetlauncher.app.data.model.BackupSettings) = BackupBundle(
            exportedAtEpochMillis = 0L, settings = settings,
            facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
        )
        `when`(backupRepository.readBackup(newer)).thenReturn(bundle(minimalSettings().copy(iconScalePercent = 115, appListShrinkToFit = "ON")))
        `when`(backupRepository.readBackup(older)).thenReturn(bundle(minimalSettings()))

        useCase(newer)
        useCase(older)

        verify(settingsRepository).setIconScalePercent(115)
        verify(settingsRepository).setAppListShrinkToFit(com.facetlauncher.app.data.model.AppListShrinkToFit.ON)
        verify(settingsRepository).setIconScalePercent(100)
        verify(settingsRepository).setAppListShrinkToFit(com.facetlauncher.app.data.model.AppListShrinkToFit.OFF)
    }

    @Test
    fun `a backup whose clock divider was never dragged clears the current one`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            BackupBundle(
                exportedAtEpochMillis = 0L, settings = minimalSettings().copy(clockZoneHeightDp = null),
                facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
            ),
        )

        useCase(uri)

        verify(settingsRepository).resetClockZoneHeight()
    }

    @Test
    fun `a backup without landscape fields restores them as same as portrait`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            BackupBundle(
                exportedAtEpochMillis = 0L, settings = minimalSettings(),
                facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
            ),
        )

        useCase(uri)

        val captor = LandscapeTransformCaptor()
        verify(settingsRepository).updateLandscape(captor.capture())
        assertEquals(LandscapeOverrides(), captor.last(LandscapeOverrides(appRowPosition = AppRowPosition.LEFT, appListColumnAlignment = AppListColumnAlignment.MIRRORED)))
    }

    @Test
    fun `widgets Facet draws itself are restored straight away and hosted ones wait in the re-add queue`() = runTest {
        // Given a backup with a native weather widget on the Hub and a native agenda page on a facet's stack, next to a hosted widget
        val uri = mock(Uri::class.java)
        val facet = BackupFacet(
            name = "Work", position = 0, overrideClock = false, clockTemplateId = "LIGHT_STACK",
            clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME", use24HourTime = false,
            clockShowMeridiem = false, overrideApps = false, appRowPosition = "LEFT",
            appRowPresentation = "ICON_AND_TEXT", listContentMode = "FAVORITES", appsToShowCount = 5,
            overridingFavorites = false, overrideCalendar = false, showAllDayEvents = true, favorites = emptyList(),
            overrideDock = false, dockDisplayMode = "ICONS", dockApps = emptyList(),
            nativeStackPages = listOf(
                BackupNativeStackPage(type = "AGENDA", widthDp = 300, heightDp = 200, alignment = "CENTER"),
                BackupNativeStackPage(type = "FROM_THE_FUTURE"),
            ),
        )
        val bundle = BackupBundle(
            exportedAtEpochMillis = 0L, settings = minimalSettings(), facets = listOf(facet), dockApps = emptyList(), defaultFavoriteApps = emptyList(),
            widgetPlacements = listOf(
                BackupWidgetPlacement("", "", row = 0, col = 0, colSpan = 2, rowSpan = 2, nativeWidgetType = "WEATHER"),
                BackupWidgetPlacement("com.example.widgets", ".W", row = 2, col = 0, colSpan = 2, rowSpan = 1),
            ),
        )
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle)
        `when`(facetRepository.restoreFacet(facet.toFacet())).thenReturn(5L)
        `when`(widgetPlacementRepository.nextNativeId()).thenReturn(-1)

        // When importing
        val result = useCase(uri) as ImportBackupResult.Success

        // Then the stale native widgets go, the saved one returns under a negative id of its own, the unknown stack type is skipped
        verify(widgetPlacementRepository).deleteAllNative()
        verify(widgetPlacementRepository).upsert(
            WidgetPlacementEntity(appWidgetId = -1, providerPackageName = "", providerClassName = "", row = 0, col = 0, colSpan = 2, rowSpan = 2, nativeWidgetType = NativeWidgetType.WEATHER),
        )
        verify(clockStackRepository).restoreNativeWidgets(
            5L,
            listOf(ClockStackItem(0L, 0L, 0, ClockStackItemKind.NATIVE_WIDGET, nativeWidgetType = NativeWidgetType.AGENDA, widthDp = 300, heightDp = 200, alignment = ClockAlignment.CENTER)),
        )
        // And only the hosted widget is queued for re-adding
        assertEquals(listOf("com.example.widgets"), result.pendingWidgetPlacements.map { it.providerPackageName })
    }
}
