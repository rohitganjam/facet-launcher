package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AddAppToDockUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeProfileId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val profileRepository = mock(ProfileRepository::class.java)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val profileDockAppRepository = mock(ProfileDockAppRepository::class.java)

        val useCase = AddAppToDockUseCase(settingsRepository, profileRepository, dockAppRepository, profileDockAppRepository)
    }

    @Test
    fun `writes to the launcher-wide default when the active profile isn't overriding the dock`() = runTest {
        // Given profile 1 active, not overriding its own dock, with 3 already in the default dock
        val fixture = Fixture()
        `when`(fixture.profileRepository.getById(1L)).thenReturn(ProfileEntity(id = 1L, name = "P1", position = 0, overrideDock = false))
        `when`(fixture.dockAppRepository.observeDockApps()).thenReturn(flowOf(listOf(app, app, app)))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to the default dock, at the next position
        verify(fixture.dockAppRepository).addDockApp(app, 3)
    }

    @Test
    fun `writes to the active profile's own dock when it's overriding`() = runTest {
        // Given profile 1 active, overriding its own dock, empty so far
        val fixture = Fixture()
        `when`(fixture.profileRepository.getById(1L)).thenReturn(ProfileEntity(id = 1L, name = "P1", position = 0, overrideDock = true))
        `when`(fixture.profileDockAppRepository.observeDockAppsForProfile(1L)).thenReturn(flowOf(emptyList()))

        // When adding an app
        fixture.useCase(app)

        // Then it's added to profile 1's own dock, not the default
        verify(fixture.profileDockAppRepository).addDockApp(1L, app, 0)
    }
}
