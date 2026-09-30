package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** [RobolectricTestRunner] purely because [AppInfo]'s default `userHandle` calls `Process.myUserHandle()`, which needs a shadow — see [SortAppsForPickerUseCaseTest]'s identical note. */
@RunWith(RobolectricTestRunner::class)
class RecentlyInstalledAppsUseCaseTest {

    private val useCase = RecentlyInstalledAppsUseCase()

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
    fun `apps installed within the window are included, sorted newest-first`() {
        val old = appInfo('a', firstInstallTime = NOW - WINDOW_MILLIS + 1)
        val newer = appInfo('b', firstInstallTime = NOW - 1_000L)

        val result = useCase(listOf(old, newer), nowMillis = NOW)

        assertEquals(listOf(newer, old), result)
    }

    @Test
    fun `an app exactly at the window boundary is included`() {
        val atBoundary = appInfo('a', firstInstallTime = NOW - WINDOW_MILLIS)

        val result = useCase(listOf(atBoundary), nowMillis = NOW)

        assertEquals(listOf(atBoundary), result)
    }

    @Test
    fun `an app just past the window boundary is excluded`() {
        val justPast = appInfo('a', firstInstallTime = NOW - WINDOW_MILLIS - 1)

        val result = useCase(listOf(justPast), nowMillis = NOW)

        assertEquals(emptyList<AppInfo>(), result)
    }

    @Test
    fun `empty input returns empty output`() {
        assertEquals(emptyList<AppInfo>(), useCase(emptyList(), nowMillis = NOW))
    }

    @Test
    fun `an app installed long ago but recently updated does not qualify`() {
        // firstInstallTime is what matters — an ordinary update must not resurrect an old app.
        val updatedNotInstalled = appInfo('a', firstInstallTime = NOW - WINDOW_MILLIS * 10, lastUpdateTime = NOW - 1_000L)

        val result = useCase(listOf(updatedNotInstalled), nowMillis = NOW)

        assertEquals(emptyList<AppInfo>(), result)
    }
}
