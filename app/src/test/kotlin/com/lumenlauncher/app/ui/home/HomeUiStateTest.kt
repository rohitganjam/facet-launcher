package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `effective 24 hour time falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, use24HourTimeOverride = null)),
        )

        assertEquals(true, state.effectiveUse24HourTime)
    }

    @Test
    fun `effective 24 hour time uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, use24HourTimeOverride = false)),
        )

        assertEquals(false, state.effectiveUse24HourTime)
    }

    @Test
    fun `usage access prompt is hidden for favorites mode regardless of grant state`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, listContentModeOverride = ListContentMode.FAVORITES)),
            usageAccessGranted = false,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt shows for recents mode when access isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, listContentModeOverride = ListContentMode.RECENTS)),
            usageAccessGranted = false,
        )

        assertEquals(true, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt is hidden for most used mode once access is granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, listContentModeOverride = ListContentMode.MOST_USED)),
            usageAccessGranted = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }
}
