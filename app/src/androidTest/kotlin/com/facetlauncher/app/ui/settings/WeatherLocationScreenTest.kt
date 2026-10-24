package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.facetlauncher.app.data.model.WeatherCity
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
    private val puneCity = pune.toWeatherCity()
    private val london = WeatherCity("London", 51.51, -0.13, "Europe/London")

    private fun setContent(
        uiState: WeatherLocationUiState,
        onUseCurrentLocation: () -> Unit = {},
        onUseCityMode: () -> Unit = {},
        onQueryChange: (String) -> Unit = {},
        onChooseCity: (PlaceCandidate) -> Unit = {},
        onSelectSavedCity: (WeatherCity) -> Unit = {},
        onRemoveCity: (WeatherCity) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                WeatherLocationContent(
                    uiState = uiState,
                    onBack = {},
                    onUseCurrentLocation = onUseCurrentLocation,
                    onUseCityMode = onUseCityMode,
                    onAllowLocation = {},
                    onQueryChange = onQueryChange,
                    onChooseCity = onChooseCity,
                    onSelectSavedCity = onSelectSavedCity,
                    onRemoveCity = onRemoveCity,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private val cityMode = WeatherLocationUiState(mode = WeatherLocationMode.CITY)

    @Test
    fun tappingASearchResultChoosesThatCity() {
        // Given search results for "pun" in city mode
        var chosen: PlaceCandidate? = null
        setContent(cityMode.copy(query = "pun", results = listOf(pune)), onChooseCity = { chosen = it })

        // When tapping Pune
        composeRule.onAllNodesWithTag("weather_city_result")[0].performClick()

        // Then that place is chosen
        assertEquals(pune, chosen)
    }

    @Test
    fun typingInTheSearchFieldReportsTheQuery() {
        // Given city mode with an empty search
        var query = ""
        setContent(cityMode, onQueryChange = { query = it })

        // When typing
        composeRule.onNodeWithTag("weather_city_search").performTextInput("pune")

        // Then the query is reported
        assertEquals("pune", query)
    }

    @Test
    fun currentLocationRowSelectsCurrentLocation() {
        // Given city mode
        var used = false
        setContent(cityMode, onUseCurrentLocation = { used = true })

        // When tapping Use current location
        composeRule.onNodeWithTag("weather_mode_current").performClick()

        // Then it asks to use the position
        assertEquals(true, used)
    }

    @Test
    fun chooseACityCanBeSelectedWithNoCitySavedAndPromptsForOne() {
        // Given current-location mode
        var cityMode = false
        setContent(WeatherLocationUiState(), onUseCityMode = { cityMode = true })

        // When tapping Choose a city
        composeRule.onNodeWithTag("weather_mode_city").performClick()

        // Then it switches to city mode
        assertEquals(true, cityMode)
    }

    @Test
    fun cityModeWithNoCitiesPromptsToSearchBelow() {
        // Given city mode and an empty list
        setContent(cityMode)

        // Then the prompt and the search box show
        composeRule.onNodeWithTag("weather_city_prompt").assertExists()
        composeRule.onNodeWithTag("weather_city_search").assertExists()
        composeRule.onAllNodesWithTag("weather_saved_city").assertCountEquals(0)
    }

    @Test
    fun currentLocationModeHidesTheCityListAndSearch() {
        // Given current-location mode
        setContent(WeatherLocationUiState(savedCities = listOf(puneCity)))

        // Then no list and no search
        composeRule.onAllNodesWithTag("weather_saved_city").assertCountEquals(0)
        composeRule.onAllNodesWithTag("weather_city_search").assertCountEquals(0)
    }

    @Test
    fun tappingASavedCitySelectsIt() {
        // Given two saved cities, Pune chosen
        var selected: WeatherCity? = null
        setContent(cityMode.copy(city = puneCity, savedCities = listOf(puneCity, london)), onSelectSavedCity = { selected = it })

        // When tapping London
        composeRule.onAllNodesWithTag("weather_saved_city")[1].performClick()

        // Then London is selected
        assertEquals(london, selected)
    }

    @Test
    fun theRemoveButtonRemovesThatCity() {
        // Given two saved cities
        var removed: WeatherCity? = null
        setContent(cityMode.copy(city = puneCity, savedCities = listOf(puneCity, london)), onRemoveCity = { removed = it })

        // When removing London
        composeRule.onAllNodesWithTag("weather_saved_city_remove")[1].performClick()

        // Then London is removed
        assertEquals(london, removed)
    }

    @Test
    fun aSavedListWithNoTickedCityAsksForOne() {
        // Given saved cities but none chosen
        setContent(cityMode.copy(city = null, savedCities = listOf(puneCity)))

        // Then the tick prompt shows
        composeRule.onNodeWithTag("weather_city_prompt").assertExists()
    }

    @Test
    fun theLimitMessageShowsWhenTheListIsFull() {
        // Given the list refused a new city
        setContent(cityMode.copy(cityLimitHit = true))

        // Then the message shows
        composeRule.onNodeWithTag("weather_city_limit").assertExists()
    }

    @Test
    fun theNoCityDialogOffersTheThreeWaysOut() {
        // Given the dialog
        val taken = mutableListOf<String>()
        composeRule.setContent {
            FacetLauncherTheme {
                NoCityDialog(
                    onTurnOffWeather = { taken += "off" },
                    onUseCurrentLocation = { taken += "current" },
                    onChooseCity = { taken += "city" },
                )
            }
        }
        composeRule.waitForIdle()

        // When tapping each button
        composeRule.onNodeWithTag("weather_leave_turn_off").performClick()
        composeRule.onNodeWithTag("weather_leave_use_current").performClick()
        composeRule.onNodeWithTag("weather_leave_choose_city").performClick()

        // Then each reports itself
        assertEquals(listOf("off", "current", "city"), taken)
    }
}
