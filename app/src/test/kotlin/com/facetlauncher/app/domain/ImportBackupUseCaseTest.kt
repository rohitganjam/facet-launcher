package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.local.FacetEntity
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

class ImportBackupUseCaseTest {

    private val backupRepository = mock(BackupRepository::class.java)
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
    private val dockAppRepository = mock(DockAppRepository::class.java)
    private val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
    private val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
    private val folderRepository = mock(FolderRepository::class.java)
    private val repairOrphanedProfileRows = mock(RepairOrphanedProfileRowsUseCase::class.java)

    private val useCase = ImportBackupUseCase(
        backupRepository,
        settingsRepository,
        facetRepository,
        favoriteAppRepository,
        dockAppRepository,
        facetDockAppRepository,
        defaultFavoriteAppRepository,
        folderRepository,
        repairOrphanedProfileRows,
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
            widgetPlacements = listOf(BackupWidgetPlacement("com.example.widgets", ".W", row = 1, col = 2, colSpan = 2, rowSpan = 1)),
        )
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle)
        // A concrete expected entity, not any(...): Mockito's static any()/eq() return a raw
        // `null` placeholder, which Kotlin's compiler-inserted non-null check on
        // restoreFacet's `facet: FacetEntity` parameter rejects outright.
        val expectedFacetEntity = FacetEntity(
            id = 0, name = "Work", position = 0, overrideClock = false,
            clockTemplateId = ClockTemplateId.LIGHT_STACK, clockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
            clockColorOption = ClockColorOption.THEME, use24HourTime = false, clockShowMeridiem = false,
            overrideApps = false, appRowPosition = AppRowPosition.LEFT, appRowPresentation = AppRowPresentation.ICON_AND_TEXT,
            listContentMode = ListContentMode.FAVORITES, appsToShowCount = 5, overridingFavorites = true,
            overrideCalendar = false, showAllDayEvents = true, selectedCalendarIdsCsv = "3,4",
            overrideDock = true, dockDisplayMode = com.facetlauncher.app.data.model.DockDisplayMode.TEXT,
        )
        `when`(facetRepository.restoreFacet(expectedFacetEntity)).thenReturn(99L)

        // When importing
        val result = useCase(uri) as ImportBackupResult.Success

        // Then existing data is wiped first...
        verify(folderRepository).deleteAllFolders()
        verify(facetRepository).deleteAllFacets()
        verify(dockAppRepository).deleteAllDockApps()
        verify(defaultFavoriteAppRepository).deleteAllDefaultFavorites()
        // ...the backup's own data is restored, favorites attached to the newly-created facet's real id...
        verify(favoriteAppRepository).restoreFavorite(FavoriteAppEntity(facetId = 99L, packageName = "com.example.a", activityName = ".Main", position = 0))
        verify(facetDockAppRepository).restoreDockApp(
            com.facetlauncher.app.data.local.FacetDockAppEntity(facetId = 99L, packageName = "com.example.dock", activityName = ".Main", position = 0),
        )
        // ...the active facet resolves from an index to that new id...
        verify(settingsRepository).setActiveFacetId(99L)
        // ...every other setting field is applied...
        verify(settingsRepository).setUse24HourTime(true)
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
}
