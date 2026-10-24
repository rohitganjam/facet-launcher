package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.domain.PlaceCandidate
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WeatherLocationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pune = PlaceCandidate("Pune", "Maharashtra", "IN", 18.52, 73.85, "Asia/Kolkata")

    private fun setContent(
        uiState: WeatherLocationUiState,
        onUseCurrentLocation: () -> Unit = {},
        onQueryChange: (String) -> Unit = {},
        onChooseCity: (PlaceCandidate) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                WeatherLocationContent(
                    uiState = uiState,
                    onBack = {},
                    onUseCurrentLocation = onUseCurrentLocation,
                    onAllowLocation = {},
                    onQueryChange = onQueryChange,
                    onChooseCity = onChooseCity,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun tappingASearchResultChoosesThatCity() {
        // Given search results for "pun"
        var chosen: PlaceCandidate? = null
        setContent(WeatherLocationUiState(query = "pun", results = listOf(pune)), onChooseCity = { chosen = it })

        // When tapping Pune
        composeRule.onAllNodesWithTag("weather_city_result")[0].performClick()

        // Then that place is chosen
        assertEquals(pune, chosen)
    }

    @Test
    fun typingInTheSearchFieldReportsTheQuery() {
        // Given an empty search
        var query = ""
        setContent(WeatherLocationUiState(), onQueryChange = { query = it })

        // When typing
        composeRule.onNodeWithTag("weather_city_search").performTextInput("pune")

        // Then the query is reported
        assertEquals("pune", query)
    }

    @Test
    fun currentLocationRowSelectsCurrentLocation() {
        // Given a saved city
        var used = false
        setContent(WeatherLocationUiState(mode = WeatherLocationMode.CITY), onUseCurrentLocation = { used = true })

        // When tapping Use current location
        composeRule.onNodeWithTag("weather_mode_current").performClick()

        // Then it asks to use the position
        assertEquals(true, used)
    }
}
