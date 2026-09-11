package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class SeedDefaultDockUseCaseTest {

    private fun appInfo(packageName: String) = AppInfo(packageName, "MainActivity", packageName, icon = null)

    private suspend fun useCase(
        defaultsSeeded: Boolean,
        defaultPackages: List<String>,
        installed: List<AppInfo>,
    ): Triple<SeedDefaultDockUseCase, SettingsRepository, DockAppRepository> {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(defaultsSeeded = defaultsSeeded)))
        val defaultAppRepository = mock(DefaultAppRepository::class.java)
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(defaultPackages)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val getInstalledApps = mock(GetInstalledAppsUseCase::class.java)
        `when`(getInstalledApps.invoke()).thenReturn(installed)
        return Triple(
            SeedDefaultDockUseCase(settingsRepository, defaultAppRepository, dockAppRepository, getInstalledApps),
            settingsRepository,
            dockAppRepository,
        )
    }

    @Test
    fun `seeds the first resolved default apps in order and sets the flag`() = runTest {
        // Given three resolvable default packages, all installed
        val browser = appInfo("com.example.browser")
        val messaging = appInfo("com.example.messaging")
        val camera = appInfo("com.example.camera")
        val (useCase, settingsRepository, dockAppRepository) = useCase(
            defaultsSeeded = false,
            defaultPackages = listOf(browser.packageName, messaging.packageName, camera.packageName),
            installed = listOf(browser, messaging, camera),
        )

        // When the use case runs
        useCase()

        // Then each resolved app is added to the dock at its resolved position, in order
        verify(dockAppRepository).addDockApp(browser, 0)
        verify(dockAppRepository).addDockApp(messaging, 1)
        verify(dockAppRepository).addDockApp(camera, 2)
        verify(settingsRepository).setDefaultsSeeded(true)
    }

    @Test
    fun `tolerates an unresolvable package by skipping it`() = runTest {
        // Given one default package that isn't actually installed
        val browser = appInfo("com.example.browser")
        val (useCase, _, dockAppRepository) = useCase(
            defaultsSeeded = false,
            defaultPackages = listOf("com.example.notinstalled", browser.packageName),
            installed = listOf(browser),
        )

        // When the use case runs
        useCase()

        // Then only the resolvable app is seeded, at position 0 despite being second in the default list
        verify(dockAppRepository).addDockApp(browser, 0)
    }

    @Test
    fun `caps seeding at MAX_APPS`() = runTest {
        // Given more resolvable default packages than the dock can hold
        val apps = (1..DockAppRepository.MAX_APPS + 2).map { appInfo("com.example.app$it") }
        val (useCase, _, dockAppRepository) = useCase(
            defaultsSeeded = false,
            defaultPackages = apps.map { it.packageName },
            installed = apps,
        )

        // When the use case runs
        useCase()

        // Then only the first MAX_APPS are seeded
        verify(dockAppRepository).addDockApp(apps[DockAppRepository.MAX_APPS - 1], DockAppRepository.MAX_APPS - 1)
        verify(dockAppRepository, never()).addDockApp(apps[DockAppRepository.MAX_APPS], DockAppRepository.MAX_APPS)
    }

    @Test
    fun `no-ops when defaults are already seeded`() = runTest {
        // Given defaultsSeeded already true
        val (useCase, settingsRepository, dockAppRepository) = useCase(
            defaultsSeeded = true,
            defaultPackages = listOf("com.example.browser"),
            installed = listOf(appInfo("com.example.browser")),
        )

        // When the use case runs
        useCase()

        // Then nothing is added and the flag is never re-written
        verifyNoInteractions(dockAppRepository)
        verify(settingsRepository, never()).setDefaultsSeeded(true)
    }
}
