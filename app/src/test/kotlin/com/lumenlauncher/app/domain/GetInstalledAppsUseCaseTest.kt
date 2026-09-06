package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LUMEN_LAUNCHER_PACKAGE_NAME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GetInstalledAppsUseCaseTest {

    @Test
    fun `the one-shot fetch delegates to the repository and returns its result unchanged, Lumen itself included`() = runTest {
        // Given a repository returning a known list, including Lumen's own entry
        val repository = mock(AppRepository::class.java)
        val apps = listOf(AppInfo("com.example.a", ".Main", "A", null), AppInfo(LUMEN_LAUNCHER_PACKAGE_NAME, ".Main", "Lumen Launcher", null))
        `when`(repository.getInstalledApps()).thenReturn(apps)

        // When invoking the one-shot use case (pickers' seed list — see its own doc comment)
        val result = GetInstalledAppsUseCase(repository)()

        // Then it returns exactly what the repository provided, unfiltered
        assertEquals(apps, result)
    }

    @Test
    fun `the live Drawer-facing observe() excludes Lumen's own entry`() = runTest {
        // Given a live installed-apps flow that includes Lumen's own entry
        val repository = mock(AppRepository::class.java)
        val apps = listOf(AppInfo("com.example.a", ".Main", "A", null), AppInfo(LUMEN_LAUNCHER_PACKAGE_NAME, ".Main", "Lumen Launcher", null))
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(apps))

        // When collecting the Drawer-facing live list
        val result = GetInstalledAppsUseCase(repository).observe().first()

        // Then Lumen's own entry never shows up in it
        assertEquals(listOf(AppInfo("com.example.a", ".Main", "A", null)), result)
    }
}
