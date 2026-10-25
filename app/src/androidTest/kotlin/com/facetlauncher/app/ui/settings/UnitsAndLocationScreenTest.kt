package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UnitsAndLocationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        uiState: UnitsAndLocationUiState = UnitsAndLocationUiState(),
        navigation: UnitsAndLocationNavigation = UnitsAndLocationNavigation(onBack = {}, onOpenLocation = {}, onOpenPermissions = {}),
    ) {
        composeRule.setContent {
            FacetLauncherTheme { UnitsAndLocationContent(uiState, navigation, UnitsAndLocationActions()) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun theHeaderIsPinnedOutsideTheScrollingContent() {
        // Given the screen
        setContent()

        // Then the back button is not inside the scrolling content, so it stays pinned while that scrolls
        composeRule.onAllNodes(hasTestTag("back_button") and hasAnyAncestor(hasTestTag("units_and_location_screen"))).assertCountEquals(0)
        composeRule.onNodeWithTag("back_button").assertExists()
    }

    @Test
    fun showsTheFourUnitRowsAndTheLocationRow() {
        // Given the screen
        setContent()

        // Then the units and the location are there
        for (tag in listOf("unit_temperature_row", "unit_wind_row", "unit_rain_row", "unit_pressure_row", "accessory_location_row")) {
            composeRule.onNodeWithTag(tag).performScrollTo().assertExists()
        }
    }

    @Test
    fun theNoteAboutEveryFacetSitsAboveTheUnitsCard() {
        // Given the screen
        setContent()

        // Then the note is above the first Units row
        val note = composeRule.onNodeWithTag("units_and_location_note").fetchSemanticsNode().boundsInRoot
        val units = composeRule.onNodeWithTag("unit_temperature_row").performScrollTo().fetchSemanticsNode().boundsInRoot
        assertTrue("note ${note.top} should be above units ${units.top}", note.top < units.top)
    }

    @Test
    fun locationRowOpensTheLocationScreen() {
        // Given the screen
        var opened = false
        setContent(navigation = UnitsAndLocationNavigation(onBack = {}, onOpenLocation = { opened = true }, onOpenPermissions = {}))

        // When tapping Location
        composeRule.onNodeWithTag("accessory_location_row").performScrollTo().performClick()

        // Then it navigates there
        assertEquals(true, opened)
    }

    @Test
    fun theLocationPermissionRowOpensThePermissionsScreen() {
        // Given weather following the phone, without the permission
        var opened = false
        setContent(
            UnitsAndLocationUiState(settings = LauncherSettings(showWeather = true), locationPermissionGranted = false),
            navigation = UnitsAndLocationNavigation(onBack = {}, onOpenLocation = {}, onOpenPermissions = { opened = true }),
        )

        // When tapping the Location permission row
        composeRule.onNodeWithTag("permission_row_LOCATION").performScrollTo().performClick()

        // Then it sends the user to Permissions
        assertEquals(true, opened)
    }

    @Test
    fun aGrantedLocationRowShowsItIsOnAndIsNotTappable() {
        // Given the permission granted
        setContent(UnitsAndLocationUiState(settings = LauncherSettings(showWeather = true), locationPermissionGranted = true))

        // Then it reads as on, with no Turn on
        composeRule.onNodeWithTag("permission_status_LOCATION").performScrollTo().assertExists()
        composeRule.onNodeWithTag("permission_turn_on_LOCATION").assertDoesNotExist()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the screen
        var back = false
        setContent(navigation = UnitsAndLocationNavigation(onBack = { back = true }, onOpenLocation = {}, onOpenPermissions = {}))

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it goes back
        assertEquals(true, back)
    }
}
