package com.facetlauncher.app.data

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppShortcut
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppShortcutRepositoryTest {

    /** An [AppRepository] that resolves [AppProfile.PERSONAL] to this process's own handle — the common case every test but the "no handle" one assumes. */
    private fun fakeAppRepository(): AppRepository {
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PERSONAL)).thenReturn(Process.myUserHandle())
        return appRepository
    }

    private fun fakeShortcut(
        id: String,
        packageName: String,
        shortLabel: String?,
        enabled: Boolean = true,
    ): ShortcutInfo {
        val shortcut = mock(ShortcutInfo::class.java)
        `when`(shortcut.id).thenReturn(id)
        `when`(shortcut.`package`).thenReturn(packageName)
        `when`(shortcut.shortLabel).thenReturn(shortLabel)
        `when`(shortcut.isEnabled).thenReturn(enabled)
        return shortcut
    }

    @Test
    fun `returns empty list when this app is not the shortcut host`() = runTest {
        // Given Facet isn't the active default launcher
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(false)

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps, fakeAppRepository()).getShortcuts("com.example.app", AppProfile.PERSONAL)

        // Then it's an empty list, not an exception — the menu simply omits the section
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `maps enabled shortcuts to AppShortcut, preferring the short label`() = runTest {
        // Given the host permission is held and one enabled shortcut is published
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val shortcut = fakeShortcut(id = "compose", packageName = "com.example.mail", shortLabel = "Compose")
        `when`(launcherApps.getShortcuts(any(), any())).thenReturn(listOf(shortcut))

        // When requesting shortcuts for that package
        val result = AppShortcutRepository(launcherApps, fakeAppRepository()).getShortcuts("com.example.mail", AppProfile.PERSONAL)

        // Then it maps id/package/label straight through
        assertEquals(listOf(AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose")), result)
    }

    @Test
    fun `disabled shortcuts are filtered out`() = runTest {
        // Given a shortcut the publishing app has since disabled
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val disabled = fakeShortcut(id = "old", packageName = "com.example.app", shortLabel = "Old", enabled = false)
        `when`(launcherApps.getShortcuts(any(), any())).thenReturn(listOf(disabled))

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps, fakeAppRepository()).getShortcuts("com.example.app", AppProfile.PERSONAL)

        // Then it's excluded
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `results are capped at 5`() = runTest {
        // Given an app publishing more than 5 shortcuts
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val shortcuts = (1..8).map { fakeShortcut(id = "s$it", packageName = "com.example.app", shortLabel = "Action $it") }
        `when`(launcherApps.getShortcuts(any(), any())).thenReturn(shortcuts)

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps, fakeAppRepository()).getShortcuts("com.example.app", AppProfile.PERSONAL)

        // Then only the first 5 come back
        assertEquals(5, result.size)
    }

    @Test
    fun `a failed system query resolves to an empty list rather than throwing`() = runTest {
        // Given the system call returns null (e.g. a transient provider failure)
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        `when`(launcherApps.getShortcuts(any(), any())).thenReturn(null)

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps, fakeAppRepository()).getShortcuts("com.example.app", AppProfile.PERSONAL)

        // Then it degrades to empty rather than crashing the long-press menu
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `returns empty list when the requested profile has no resolvable handle`() = runTest {
        // Given a Work Profile that no longer exists (a race with unenrollment)
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.WORK)).thenReturn(null)

        // When requesting shortcuts for it
        val result = AppShortcutRepository(launcherApps, appRepository).getShortcuts("com.example.app", AppProfile.WORK)

        // Then there's no handle to query against — empty, not a crash
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `launchShortcut resolves the shortcut's own profile, not the personal handle unconditionally`() {
        // Given a Work Profile shortcut
        val launcherApps = mock(LauncherApps::class.java)
        val appRepository = mock(AppRepository::class.java)
        val workHandle = mock(android.os.UserHandle::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.WORK)).thenReturn(workHandle)
        val shortcut = AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose", profile = AppProfile.WORK)

        // When launching it
        AppShortcutRepository(launcherApps, appRepository).launchShortcut(shortcut)

        // Then it's started against the Work Profile's own handle
        org.mockito.Mockito.verify(launcherApps).startShortcut("com.example.mail", "compose", null, null, workHandle)
    }

    @Test
    fun `launchShortcut is a no-op when the shortcut's profile has vanished`() {
        // Given a Work Profile that's since been unenrolled
        val launcherApps = mock(LauncherApps::class.java)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.WORK)).thenReturn(null)
        val shortcut = AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose", profile = AppProfile.WORK)

        // When attempting to launch it
        AppShortcutRepository(launcherApps, appRepository).launchShortcut(shortcut)

        // Then nothing is started — no handle to launch it in
        org.mockito.Mockito.verify(launcherApps, org.mockito.Mockito.never()).startShortcut(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), any(), any(), any(),
        )
    }
}
