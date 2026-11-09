package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.FakeAppPlacementDao
import com.facetlauncher.app.data.FakeFolderPlacementDao
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SeedFacetDefaultsUseCaseTest {

    private val placementRepository = PlacementRepository(FakeAppPlacementDao(), FakeFolderPlacementDao())
    private val dock = PlacementTarget(PlacementList.DOCK, facetId = 7L)

    private fun appInfo(packageName: String) = AppInfo(packageName, "MainActivity", packageName, icon = null)

    private suspend fun useCase(defaultPackages: List<String>, installed: List<AppInfo>): SeedFacetDefaultsUseCase {
        val defaultAppRepository = mock(DefaultAppRepository::class.java)
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(defaultPackages)
        val getInstalledApps = mock(GetInstalledAppsUseCase::class.java)
        `when`(getInstalledApps.invoke()).thenReturn(installed)
        return SeedFacetDefaultsUseCase(defaultAppRepository, placementRepository, getInstalledApps)
    }

    private suspend fun dockPackages() = placementRepository.getRawApps(dock).sortedBy { it.position }.map { it.packageName }

    @Test
    fun `seeds the resolved default apps into the facet dock in order`() = runTest {
        // Given three resolvable default packages, all installed
        val apps = listOf("browser", "messaging", "camera").map { appInfo("com.example.$it") }

        // When the facet is seeded
        useCase(apps.map { it.packageName }, apps)(7L)

        // Then its dock holds them in order, and its Favorites stay empty
        assertEquals(apps.map { it.packageName }, dockPackages())
        assertEquals(emptyList<String>(), placementRepository.getRawApps(PlacementTarget(PlacementList.FAVORITES, 7L)).map { it.packageName })
    }

    @Test
    fun `skips a default package that is not installed`() = runTest {
        val browser = appInfo("com.example.browser")

        useCase(listOf("com.example.notinstalled", browser.packageName), listOf(browser))(7L)

        assertEquals(listOf(browser.packageName), dockPackages())
    }

    @Test
    fun `caps the dock at the dock limit`() = runTest {
        val apps = (1..AppListLimits.MAX_DOCK_APPS + 2).map { appInfo("com.example.app$it") }

        useCase(apps.map { it.packageName }, apps)(7L)

        assertEquals(AppListLimits.MAX_DOCK_APPS, dockPackages().size)
    }

    @Test
    fun `leaves a dock that already has apps alone`() = runTest {
        // Given a dock with one app of its own
        val own = appInfo("com.example.own")
        placementRepository.addApp(dock, own, 0)
        val browser = appInfo("com.example.browser")

        // When the facet is seeded without replace
        useCase(listOf(browser.packageName), listOf(browser))(7L)

        // Then nothing is added
        assertEquals(listOf(own.packageName), dockPackages())
    }

    @Test
    fun `replace clears the dock first`() = runTest {
        val own = appInfo("com.example.own")
        placementRepository.addApp(dock, own, 0)
        val browser = appInfo("com.example.browser")

        useCase(listOf(browser.packageName), listOf(browser))(7L, replace = true)

        assertEquals(listOf(browser.packageName), dockPackages())
    }

    @Test
    fun `another facet's dock is not touched`() = runTest {
        val other = PlacementTarget(PlacementList.DOCK, facetId = 8L)
        val own = appInfo("com.example.own")
        placementRepository.addApp(other, own, 0)
        val browser = appInfo("com.example.browser")

        useCase(listOf(browser.packageName), listOf(browser))(7L, replace = true)

        assertEquals(listOf(own.packageName), placementRepository.getRawApps(other).map { it.packageName })
    }
}
