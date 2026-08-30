package com.lumenlauncher.app.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LongPressSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingLauncherSettingsInvokesItsCallback() {
        // Given the sheet rendered
        var settingsClicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                LongPressSheet(
                    activeProfileName = "Profile 1",
                    activeProfilePosition = 1,
                    profileCount = 1,
                    onSwitchProfileClick = {},
                    onEditProfileClick = {},
                    onLauncherSettingsClick = { settingsClicked = true },
                    onChangeWallpaperClick = {},
                )
            }
        }

        // When tapping "Launcher settings"
        composeRule.onNodeWithTag("long_press_sheet_launcher_settings").performClick()

        // Then its callback fires
        assertEquals(true, settingsClicked)
    }

    @Test
    fun tappingChangeWallpaperInvokesItsCallback() {
        // Given the sheet rendered
        var wallpaperClicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                LongPressSheet(
                    activeProfileName = "Profile 1",
                    activeProfilePosition = 1,
                    profileCount = 1,
                    onSwitchProfileClick = {},
                    onEditProfileClick = {},
                    onLauncherSettingsClick = {},
                    onChangeWallpaperClick = { wallpaperClicked = true },
                )
            }
        }

        // When tapping "Change wallpaper"
        composeRule.onNodeWithTag("long_press_sheet_change_wallpaper").performClick()

        // Then its callback fires
        assertEquals(true, wallpaperClicked)
    }

    @Test
    fun tappingSwitchProfileInvokesItsCallback() {
        // Given the sheet rendered — Switch profile is functional as of Phase 3
        var switchClicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                LongPressSheet(
                    activeProfileName = "Profile 1",
                    activeProfilePosition = 1,
                    profileCount = 2,
                    onSwitchProfileClick = { switchClicked = true },
                    onEditProfileClick = {},
                    onLauncherSettingsClick = {},
                    onChangeWallpaperClick = {},
                )
            }
        }

        // When tapping "Switch profile"
        composeRule.onNodeWithTag("long_press_sheet_switch_profile").performClick()

        // Then its callback fires
        assertEquals(true, switchClicked)
    }

    @Test
    fun tappingEditProfileInvokesItsCallback() {
        // Given the sheet rendered — Edit profile is functional as of Phase 3
        var editClicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                LongPressSheet(
                    activeProfileName = "Profile 1",
                    activeProfilePosition = 1,
                    profileCount = 1,
                    onSwitchProfileClick = {},
                    onEditProfileClick = { editClicked = true },
                    onLauncherSettingsClick = {},
                    onChangeWallpaperClick = {},
                )
            }
        }

        // When tapping "Edit profile"
        composeRule.onNodeWithTag("long_press_sheet_edit_profile").performClick()

        // Then its callback fires
        assertEquals(true, editClicked)
    }
}
