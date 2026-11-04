package com.facetlauncher.app.ui.home.weather

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WeatherSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(onConfigure: (() -> Unit)?) {
        composeRule.setContent {
            FacetLauncherTheme {
                WeatherSheet(
                    uiState = WeatherUiState(),
                    onRefresh = {},
                    onDismiss = {},
                    onChooseCity = {},
                    onOpenPermissions = {},
                    onConfigure = onConfigure,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun theSheetHasAThreeDotButtonThatOpensTheSettingsOfWhatItShows() {
        // Given a weather sheet that has somewhere to configure (a widget's settings, or the clock's)
        var opened = 0
        show(onConfigure = { opened++ })

        // When tapping the 3-dot button
        composeRule.onNodeWithTag("weather_sheet_menu").performClick()

        // Then it asks to open those settings
        assertEquals(1, opened)
    }

    @Test
    fun withNothingToConfigureThereIsNoThreeDotButton() {
        // Given a weather sheet with no settings to open
        show(onConfigure = null)

        // Then there is no button
        composeRule.onNodeWithTag("weather_sheet_menu").assertDoesNotExist()
    }
}
