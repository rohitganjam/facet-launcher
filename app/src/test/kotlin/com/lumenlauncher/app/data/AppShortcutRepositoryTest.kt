package com.lumenlauncher.app.data

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import com.lumenlauncher.app.data.model.AppShortcut
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
        // Given Lumen isn't the active default launcher
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.hasShortcutHostPermission()).thenReturn(false)

        // When requesting shortcuts
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app")

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.mail")

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app")

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app")

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
        val result = AppShortcutRepository(launcherApps).getShortcuts("com.example.app")

        // Then it degrades to empty rather than crashing the long-press menu
        assertEquals(emptyList<AppShortcut>(), result)
    }
}
