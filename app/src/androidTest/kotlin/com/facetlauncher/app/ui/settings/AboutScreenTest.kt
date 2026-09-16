package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AboutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                AboutScreen(onBack = onBack)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun versionRowShowsTheAppsActualVersionName() {
        // Given the About screen — the version comes from BuildConfig, never hardcoded, so it
        // can't drift from what scripts/release.sh bumps
        setContent()

        // Then it shows the real BuildConfig.VERSION_NAME
        composeRule.onNodeWithTag("about_version_row").assertTextContains(BuildConfig.VERSION_NAME, substring = true)
    }

    @Test
    fun checkForUpdatesRowIsInteractive() {
        // Given the About screen — launching the Play Store listing isn't itself verifiable from
        // a Compose test (same category as SettingsScreenTest's changeWallpaperRowIsInteractive)
        setContent()

        // Then it's genuinely interactive
        composeRule.onNodeWithTag("about_check_updates_row").assertHasClickAction()
    }

    @Test
    fun discordRowIsInteractive() {
        // Given the About screen — same category as the Play Store row above
        setContent()

        // Then it's genuinely interactive
        composeRule.onNodeWithTag("about_discord_row").assertHasClickAction()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the About screen
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }
}
