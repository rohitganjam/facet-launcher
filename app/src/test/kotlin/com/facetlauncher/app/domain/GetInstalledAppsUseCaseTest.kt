package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GetInstalledAppsUseCaseTest {

    @Test
    fun `the one-shot fetch delegates to the repository and returns its result unchanged, Facet itself included`() = runTest {
        // Given a repository returning a known list, including Facet's own entry
        val repository = mock(AppRepository::class.java)
        val apps = listOf(AppInfo("com.example.a", ".Main", "A", null), AppInfo(FACET_LAUNCHER_PACKAGE_NAME, ".Main", "Facet Launcher", null))
        `when`(repository.getInstalledApps()).thenReturn(apps)

        // When invoking the one-shot use case (pickers' seed list — see its own doc comment)
        val result = GetInstalledAppsUseCase(repository)()

        // Then it returns exactly what the repository provided, unfiltered
        assertEquals(apps, result)
    }

    @Test
    fun `the live Drawer-facing observe() excludes Facet's own entry`() = runTest {
        // Given a live installed-apps flow that includes Facet's own entry
        val repository = mock(AppRepository::class.java)
        val apps = listOf(AppInfo("com.example.a", ".Main", "A", null), AppInfo(FACET_LAUNCHER_PACKAGE_NAME, ".Main", "Facet Launcher", null))
        `when`(repository.observeInstalledApps()).thenReturn(flowOf(apps))

        // When collecting the Drawer-facing live list
        val result = GetInstalledAppsUseCase(repository).observe().first()

        // Then Facet's own entry never shows up in it
        assertEquals(listOf(AppInfo("com.example.a", ".Main", "A", null)), result)
    }
}
