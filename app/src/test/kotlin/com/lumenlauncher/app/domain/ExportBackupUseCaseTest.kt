package com.lumenlauncher.app.domain

import android.net.Uri
import com.lumenlauncher.app.data.BackupRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.DefaultFavoriteAppEntity
import com.lumenlauncher.app.data.local.DockAppEntity
import com.lumenlauncher.app.data.local.FavoriteAppEntity
import com.lumenlauncher.app.data.local.ProfileDockAppEntity
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ExportBackupUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val profileRepository = mock(ProfileRepository::class.java)
    private val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
    private val dockAppRepository = mock(DockAppRepository::class.java)
    private val profileDockAppRepository = mock(ProfileDockAppRepository::class.java)
    private val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
    private val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
    private val backupRepository = mock(BackupRepository::class.java)

    private val useCase = ExportBackupUseCase(
        settingsRepository,
        profileRepository,
        favoriteAppRepository,
        dockAppRepository,
        profileDockAppRepository,
        defaultFavoriteAppRepository,
        widgetPlacementRepository,
        backupRepository,
    )

    @Test
    fun `bundles every repository's data and hands it to BackupRepository`() = runTest {
        // Given one active profile with a favorite, one dock app, one default favorite, one widget
        val profile = ProfileEntity(id = 7, name = "Work", position = 0, clockColorOption = ClockColorOption.ACCENT_PRIMARY, selectedCalendarIdsCsv = "3,4")
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(activeProfileId = 7)))
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(favoriteAppRepository.getRawFavoritesForProfile(7)).thenReturn(
            listOf(FavoriteAppEntity(id = 1, profileId = 7, packageName = "com.example.a", activityName = ".Main", position = 0)),
        )
        `when`(dockAppRepository.getRawDockApps()).thenReturn(
            listOf(DockAppEntity(id = 1, packageName = "com.example.b", activityName = ".Main", position = 0)),
        )
        `when`(profileDockAppRepository.getRawDockAppsForProfile(7)).thenReturn(
            listOf(ProfileDockAppEntity(id = 1, profileId = 7, packageName = "com.example.d", activityName = ".Main", position = 0)),
        )
        `when`(defaultFavoriteAppRepository.getRawDefaultFavorites()).thenReturn(
            listOf(DefaultFavoriteAppEntity(id = 1, packageName = "com.example.c", activityName = ".Main", position = 0)),
        )
        `when`(widgetPlacementRepository.observeAll()).thenReturn(
            flowOf(listOf(WidgetPlacementEntity(appWidgetId = 42, providerPackageName = "com.example.widgets", providerClassName = ".W", row = 1, col = 2, colSpan = 2, rowSpan = 1))),
        )
        val uri = mock(Uri::class.java)

        // When exporting
        val bundle = useCase(uri)

        // Then the returned (and written) bundle reflects every source, with the active profile resolved to its index
        verify(backupRepository).writeBackup(uri, bundle)
        assertEquals(1, bundle.profiles.size)
        assertEquals("Work", bundle.profiles[0].name)
        assertEquals("ACCENT_PRIMARY", bundle.profiles[0].clockColorOption)
        assertEquals(1, bundle.profiles[0].favorites.size)
        assertEquals("com.example.a", bundle.profiles[0].favorites[0].packageName)
        assertEquals(listOf("3", "4"), bundle.profiles[0].selectedCalendarIds)
        assertEquals(0, bundle.settings.activeProfileIndex)
        assertEquals(1, bundle.dockApps.size)
        assertEquals("com.example.b", bundle.dockApps[0].packageName)
        assertEquals(1, bundle.profiles[0].dockApps.size)
        assertEquals("com.example.d", bundle.profiles[0].dockApps[0].packageName)
        assertEquals(1, bundle.defaultFavoriteApps.size)
        assertEquals(1, bundle.widgetPlacements.size)
        assertEquals("com.example.widgets", bundle.widgetPlacements[0].providerPackageName)
        assertEquals(2, bundle.widgetPlacements[0].colSpan)
    }

    @Test
    fun `no active profile leaves activeProfileIndex null`() = runTest {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(emptyList()))
        `when`(dockAppRepository.getRawDockApps()).thenReturn(emptyList())
        `when`(defaultFavoriteAppRepository.getRawDefaultFavorites()).thenReturn(emptyList())
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        val uri = mock(Uri::class.java)

        val bundle = useCase(uri)

        assertNull(bundle.settings.activeProfileIndex)
    }
}
