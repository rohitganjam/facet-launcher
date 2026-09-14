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
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.DefaultFavoriteAppEntity
import com.facetlauncher.app.data.local.DockAppEntity
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.local.FacetDockAppEntity
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ExportBackupUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
    private val dockAppRepository = mock(DockAppRepository::class.java)
    private val facetDockAppRepository = mock(FacetDockAppRepository::class.java)
    private val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
    private val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
    private val folderRepository = mock(FolderRepository::class.java).also {
        runBlocking { `when`(it.getRawFolders()).thenReturn(emptyList()) }
    }
    private val backupRepository = mock(BackupRepository::class.java)

    private val useCase = ExportBackupUseCase(
        settingsRepository,
        facetRepository,
        favoriteAppRepository,
        dockAppRepository,
        facetDockAppRepository,
        defaultFavoriteAppRepository,
        widgetPlacementRepository,
        folderRepository,
        backupRepository,
    )

    @Test
    fun `bundles every repository's data and hands it to BackupRepository`() = runTest {
        // Given one active facet with a favorite, one dock app, one default favorite, one widget
        val facet = FacetEntity(id = 7, name = "Work", position = 0, clockColorOption = ClockColorOption.ACCENT_PRIMARY, selectedCalendarIdsCsv = "3,4")
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(activeFacetId = 7)))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        `when`(favoriteAppRepository.getRawFavoritesForFacet(7)).thenReturn(
            listOf(FavoriteAppEntity(id = 1, facetId = 7, packageName = "com.example.a", activityName = ".Main", position = 0)),
        )
        `when`(dockAppRepository.getRawDockApps()).thenReturn(
            listOf(DockAppEntity(id = 1, packageName = "com.example.b", activityName = ".Main", position = 0)),
        )
        `when`(facetDockAppRepository.getRawDockAppsForFacet(7)).thenReturn(
            listOf(FacetDockAppEntity(id = 1, facetId = 7, packageName = "com.example.d", activityName = ".Main", position = 0)),
        )
        `when`(facetDockAppRepository.getRawDockFolderPlacementsForFacet(7)).thenReturn(emptyList())
        `when`(favoriteAppRepository.getRawFavoriteFolderPlacementsForFacet(7)).thenReturn(emptyList())
        `when`(defaultFavoriteAppRepository.getRawDefaultFavorites()).thenReturn(
            listOf(DefaultFavoriteAppEntity(id = 1, packageName = "com.example.c", activityName = ".Main", position = 0)),
        )
        `when`(dockAppRepository.getRawDockFolderPlacements()).thenReturn(emptyList())
        `when`(defaultFavoriteAppRepository.getRawDefaultFavoriteFolderPlacements()).thenReturn(emptyList())
        `when`(widgetPlacementRepository.observeAll()).thenReturn(
            flowOf(listOf(WidgetPlacementEntity(appWidgetId = 42, providerPackageName = "com.example.widgets", providerClassName = ".W", row = 1, col = 2, colSpan = 2, rowSpan = 1))),
        )
        val uri = mock(Uri::class.java)

        // When exporting
        val bundle = useCase(uri)

        // Then the returned (and written) bundle reflects every source, with the active facet resolved to its index
        verify(backupRepository).writeBackup(uri, bundle)
        assertEquals(1, bundle.facets.size)
        assertEquals("Work", bundle.facets[0].name)
        assertEquals("ACCENT_PRIMARY", bundle.facets[0].clockColorOption)
        assertEquals(1, bundle.facets[0].favorites.size)
        assertEquals("com.example.a", bundle.facets[0].favorites[0].packageName)
        assertEquals(listOf("3", "4"), bundle.facets[0].selectedCalendarIds)
        assertEquals(0, bundle.settings.activeFacetIndex)
        assertEquals(1, bundle.dockApps.size)
        assertEquals("com.example.b", bundle.dockApps[0].packageName)
        assertEquals(1, bundle.facets[0].dockApps.size)
        assertEquals("com.example.d", bundle.facets[0].dockApps[0].packageName)
        assertEquals(1, bundle.defaultFavoriteApps.size)
        assertEquals(1, bundle.widgetPlacements.size)
        assertEquals("com.example.widgets", bundle.widgetPlacements[0].providerPackageName)
        assertEquals(2, bundle.widgetPlacements[0].colSpan)
    }

    @Test
    fun `no active facet leaves activeFacetIndex null`() = runTest {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(emptyList()))
        `when`(dockAppRepository.getRawDockApps()).thenReturn(emptyList())
        `when`(dockAppRepository.getRawDockFolderPlacements()).thenReturn(emptyList())
        `when`(defaultFavoriteAppRepository.getRawDefaultFavorites()).thenReturn(emptyList())
        `when`(defaultFavoriteAppRepository.getRawDefaultFavoriteFolderPlacements()).thenReturn(emptyList())
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        val uri = mock(Uri::class.java)

        val bundle = useCase(uri)

        assertNull(bundle.settings.activeFacetIndex)
    }
}
