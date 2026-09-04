package com.lumenlauncher.app.ui.profiles

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileCarouselUiStateTest {

    private val profiles = listOf(
        ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideClock = false),
        ProfileEntity(id = 2L, name = "Profile 2", position = 1, overrideClock = true, use24HourTime = true),
    )

    @Test
    fun `each profile resolves its own effective 24 hour time independently`() {
        val state = ProfileCarouselUiState(profiles = profiles, globalSettings = LauncherSettings(use24HourTime = false))

        // Profile 1 has no override — falls back to the global default
        assertEquals(false, state.effectiveUse24HourTime(1L))
        // Profile 2 overrides it — reflects its own stored value, not the global default
        assertEquals(true, state.effectiveUse24HourTime(2L))
    }
}
