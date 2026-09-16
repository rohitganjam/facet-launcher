package com.facetlauncher.app.data

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.UserHandle
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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app", Process.myUserHandle())

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.mail", Process.myUserHandle())

        // Then it maps id/package/label straight through, carrying the requested handle
        assertEquals(listOf(AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose", userHandle = Process.myUserHandle())), result)
    }

    @Test
    fun `disabled shortcuts are filtered out`() = runTest {
        // Given a shortcut the publishing app has since disabled
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val disabled = fakeShortcut(id = "old", packageName = "com.example.app", shortLabel = "Old", enabled = false)
        `when`(launcherApps.getShortcuts(any(), any())).thenReturn(listOf(disabled))

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app", Process.myUserHandle())

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app", Process.myUserHandle())

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app", Process.myUserHandle())

        // Then it degrades to empty rather than crashing the long-press menu
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `returns empty list when the requested handle's profile has since vanished`() = runTest {
        // Given a Work Profile handle that no longer exists (a race with unenrollment) — the
        // underlying system query throws for it
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(true)
        val vanishedHandle = mock(UserHandle::class.java)
        `when`(launcherApps.getShortcuts(any(), org.mockito.ArgumentMatchers.eq(vanishedHandle))).thenThrow(IllegalStateException("no such user"))

        // When requesting shortcuts for it
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app", vanishedHandle)

        // Then there's no crash — empty, not a propagated exception
        assertEquals(emptyList<AppShortcut>(), result)
    }

    @Test
    fun `launchShortcut launches against the shortcut's own carried handle`() {
        // Given a shortcut fetched from a specific (e.g. Work Profile) handle
        val launcherApps = mock(LauncherApps::class.java)
        val workHandle = mock(UserHandle::class.java)
        val shortcut = AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose", userHandle = workHandle)

        // When launching it
        AppShortcutRepository(launcherApps).launchShortcut(shortcut)

        // Then it's started against that same handle, not re-derived from anything else
        org.mockito.Mockito.verify(launcherApps).startShortcut("com.example.mail", "compose", null, null, workHandle)
    }

    @Test
    fun `launchShortcut swallows an exception when the shortcut's profile has since vanished`() {
        // Given a shortcut whose profile has since been unenrolled — the system call throws
        val launcherApps = mock(LauncherApps::class.java)
        val vanishedHandle = mock(UserHandle::class.java)
        `when`(launcherApps.startShortcut(any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq(vanishedHandle)))
            .thenThrow(IllegalStateException("no such user"))
        val shortcut = AppShortcut(id = "compose", packageName = "com.example.mail", label = "Compose", userHandle = vanishedHandle)

        // When attempting to launch it — then it doesn't propagate the exception
        AppShortcutRepository(launcherApps).launchShortcut(shortcut)
    }
}
