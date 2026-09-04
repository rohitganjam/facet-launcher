package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `effective 24 hour time falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideClock = false)),
        )

        assertEquals(true, state.effectiveUse24HourTime)
    }

    @Test
    fun `effective 24 hour time uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideClock = true, use24HourTime = false)),
        )

        assertEquals(false, state.effectiveUse24HourTime)
    }

    @Test
    fun `usage access prompt is hidden for favorites mode regardless of grant state`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.FAVORITES)),
            usageAccessGranted = false,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt shows for recents mode when access isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
        )

        assertEquals(true, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt is hidden for most used mode once access is granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.MOST_USED)),
            usageAccessGranted = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt stays hidden once dismissed even though access still isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
            usageAccessPromptDismissed = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `active app row position falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.RIGHT, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row position uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.LEFT, activeProfileId = 1L),
            profiles = listOf(
                ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideApps = true, appRowPosition = AppRowPosition.RIGHT),
            ),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row presentation falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.TEXT_ONLY, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPresentation.TEXT_ONLY, state.activeAppRowPresentation)
    }

    @Test
    fun `active app row presentation uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.ICON_AND_TEXT, activeProfileId = 1L),
            profiles = listOf(
                ProfileEntity(
                    id = 1L,
                    name = "Profile 1",
                    position = 0,
                    overrideApps = true,
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                ),
            ),
        )

        assertEquals(AppRowPresentation.ICON_ONLY, state.activeAppRowPresentation)
    }

    @Test
    fun `active calendar font falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(calendarFontOption = ClockFontOption.POPPINS, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideCalendar = false)),
        )

        assertEquals(ClockFontOption.POPPINS, state.activeCalendarFontOption)
    }

    @Test
    fun `active calendar font uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(calendarFontOption = ClockFontOption.POPPINS, activeProfileId = 1L),
            profiles = listOf(
                ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideCalendar = true, calendarFontOption = ClockFontOption.MANROPE),
            ),
        )

        assertEquals(ClockFontOption.MANROPE, state.activeCalendarFontOption)
    }

    @Test
    fun `active calendar color falls back to the global default when the active profile has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(calendarColorOption = ClockColorOption.WHITE, activeProfileId = 1L),
            profiles = listOf(ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideCalendar = false)),
        )

        assertEquals(ClockColorOption.WHITE, state.activeCalendarColorOption)
    }

    @Test
    fun `active calendar color uses the active profile's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(calendarColorOption = ClockColorOption.WHITE, activeProfileId = 1L),
            profiles = listOf(
                ProfileEntity(id = 1L, name = "Profile 1", position = 0, overrideCalendar = true, calendarColorOption = ClockColorOption.BLACK),
            ),
        )

        assertEquals(ClockColorOption.BLACK, state.activeCalendarColorOption)
    }
}
