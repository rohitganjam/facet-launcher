package com.facetlauncher.app.domain

import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.SortDirection
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/** [RobolectricTestRunner] purely because [AppInfo]'s default `userHandle` calls `Process.myUserHandle()`, which needs a shadow. */
@RunWith(RobolectricTestRunner::class)
class SortAppsForPickerUseCaseTest {

    private fun appInfo(letter: Char, firstInstallTime: Long = 0L, lastUpdateTime: Long = 0L) = AppInfo(
        packageName = "com.example.$letter",
        activityName = ".Main",
        label = "$letter App",
        icon = null,
        firstInstallTime = firstInstallTime,
        lastUpdateTime = lastUpdateTime,
    )

    private suspend fun useCase(lastUsed: Map<String, Long> = emptyMap()): SortAppsForPickerUseCase {
        val usageStatsRepository = mock(UsageStatsRepository::class.java)
        `when`(usageStatsRepository.getLastUsedTimestamps()).thenReturn(lastUsed)
        return SortAppsForPickerUseCase(usageStatsRepository)
    }

    @Test
    fun `ALPHABETICAL ascending sorts A to Z regardless of input order`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')

        val result = useCase()(listOf(c, a, b), AppSortOption.ALPHABETICAL, SortDirection.ASCENDING)

        assertEquals(listOf(a, b, c), result)
    }

    @Test
    fun `ALPHABETICAL descending reverses it to Z to A`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')

        val result = useCase()(listOf(a, b), AppSortOption.ALPHABETICAL, SortDirection.DESCENDING)

        assertEquals(listOf(b, a), result)
    }

    @Test
    fun `INSTALL_DATE ascending is oldest first`() = runTest {
        val old = appInfo('a', firstInstallTime = 100L)
        val new = appInfo('b', firstInstallTime = 200L)

        val result = useCase()(listOf(new, old), AppSortOption.INSTALL_DATE, SortDirection.ASCENDING)

        assertEquals(listOf(old, new), result)
    }

    @Test
    fun `LAST_UPDATED descending is most recently updated first`() = runTest {
        val old = appInfo('a', lastUpdateTime = 100L)
        val new = appInfo('b', lastUpdateTime = 200L)

        val result = useCase()(listOf(old, new), AppSortOption.LAST_UPDATED, SortDirection.DESCENDING)

        assertEquals(listOf(new, old), result)
    }

    @Test
    fun `LAST_USED orders used apps by recency and always pushes never-used apps last, both directions`() = runTest {
        val neverUsed = appInfo('a')
        val usedOld = appInfo('b')
        val usedNew = appInfo('c')
        val useCase = useCase(lastUsed = mapOf(usedOld.packageName to 100L, usedNew.packageName to 200L))
        val input = listOf(neverUsed, usedOld, usedNew)

        val ascending = useCase(input, AppSortOption.LAST_USED, SortDirection.ASCENDING)
        val descending = useCase(input, AppSortOption.LAST_USED, SortDirection.DESCENDING)

        assertEquals(listOf(usedOld, usedNew, neverUsed), ascending)
        assertEquals(listOf(usedNew, usedOld, neverUsed), descending)
    }

    @Test
    fun `LAST_USED with no usage data at all (Usage Access not granted) falls back to input order`() = runTest {
        val a = appInfo('a')
        val b = appInfo('b')

        val result = useCase(lastUsed = emptyMap())(listOf(a, b), AppSortOption.LAST_USED, SortDirection.ASCENDING)

        assertEquals(listOf(a, b), result)
    }
}
