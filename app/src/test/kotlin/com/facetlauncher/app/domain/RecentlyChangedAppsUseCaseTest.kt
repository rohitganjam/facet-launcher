package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** [RobolectricTestRunner] purely because [AppInfo]'s default `userHandle` calls `Process.myUserHandle()`. */
@RunWith(RobolectricTestRunner::class)
class RecentlyChangedAppsUseCaseTest {

    private val useCase = RecentlyChangedAppsUseCase()

    private fun appInfo(letter: Char, firstInstallTime: Long = 0L, lastUpdateTime: Long = 0L) = AppInfo(
        packageName = "com.example.$letter",
        activityName = ".Main",
        label = "$letter App",
        icon = null,
        firstInstallTime = firstInstallTime,
        lastUpdateTime = lastUpdateTime,
    )

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val WINDOW_MILLIS = 72 * 60 * 60 * 1000L
    }

    @Test
    fun `an app updated within the window counts though installed long ago`() {
        // Given an old install with a fresh update
        val updated = appInfo('a', firstInstallTime = 1L, lastUpdateTime = NOW - 1_000L)

        // When
        val result = useCase(listOf(updated), nowMillis = NOW)

        // Then
        assertEquals(listOf(updated), result)
    }

    @Test
    fun `apps are sorted by their latest install or update, newest first`() {
        val installed = appInfo('a', firstInstallTime = NOW - 5_000L)
        val updated = appInfo('b', firstInstallTime = 1L, lastUpdateTime = NOW - 1_000L)

        val result = useCase(listOf(installed, updated), nowMillis = NOW)

        assertEquals(listOf(updated, installed), result)
    }

    @Test
    fun `an app untouched for longer than the window is left out`() {
        val stale = appInfo('a', firstInstallTime = NOW - WINDOW_MILLIS - 1, lastUpdateTime = NOW - WINDOW_MILLIS - 1)

        assertEquals(emptyList<AppInfo>(), useCase(listOf(stale), nowMillis = NOW))
    }
}
