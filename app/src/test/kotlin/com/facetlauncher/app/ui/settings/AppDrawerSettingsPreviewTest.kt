package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric: AppInfo's default user handle calls Process.myUserHandle().
@RunWith(RobolectricTestRunner::class)
class AppDrawerSettingsPreviewTest {

    private val labels = listOf("Maps", "Clock", "Camera", "Notes", "Photos")
    private val now = 1_000_000_000L

    @Test
    fun `sample apps are sorted by label and keep every label`() {
        // When building the sample
        val sample = drawerPreviewSample(labels, "Work", now)

        // Then the apps come out alphabetically, none dropped
        assertEquals(listOf("Camera", "Clock", "Maps", "Notes", "Photos"), sample.apps.map { it.label })
    }

    @Test
    fun `some sample apps count as recently installed and some do not`() {
        // Given the sample
        val sample = drawerPreviewSample(labels, "Work", now)

        // When asking which are recently installed
        val recent = RecentlyInstalledAppsUseCase()(sample.apps, now)

        // Then it is the first two labels given, not all of them
        assertEquals(setOf("Maps", "Clock"), recent.map { it.label }.toSet())
    }

    @Test
    fun `sample folder has a name and apps from the sample`() {
        // When building the sample
        val sample = drawerPreviewSample(labels, "Work", now)

        // Then it carries one folder whose apps are sample apps
        val folder = sample.folders.single()
        assertEquals("Work", folder.name)
        assertTrue(folder.apps.isNotEmpty() && folder.apps.all { it in sample.apps })
    }

    @Test
    fun `sample apps have distinct keys`() {
        val sample = drawerPreviewSample(labels, "Work", now)

        assertEquals(sample.apps.size, sample.apps.map { it.listKey }.toSet().size)
    }
}
