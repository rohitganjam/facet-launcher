package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasTestTag
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockAccessoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        uiState: ClockAccessoriesUiState,
        navigation: ClockAccessoriesNavigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}),
        actions: ClockAccessoriesActions = ClockAccessoriesActions(),
    ) {
        composeRule.setContent {
            FacetLauncherTheme { ClockAccessoriesContent(uiState, navigation, actions) }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun theHeaderIsPinnedOutsideTheScrollingContent() {
        // Given the screen
        setContent(ClockAccessoriesUiState())

        // Then the back button is not inside the scrolling content, so it stays pinned while that scrolls
        composeRule.onAllNodes(hasTestTag("back_button") and hasAnyAncestor(hasTestTag("clock_accessories_screen"))).assertCountEquals(0)
        composeRule.onNodeWithTag("back_button").assertExists()
    }

    @Test
    fun iconsAreHiddenWhileWeatherIsOffEverywhere() {
        // Given weather off for the default and every facet
        setContent(ClockAccessoriesUiState(weatherEnabledAnywhere = false))

        // Then the switches show but the Icons card does not
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("icon_style_row").assertDoesNotExist()
    }

    @Test
    fun iconsShowOnceWeatherIsOnSomewhere() {
        // Given weather on for the launcher default
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the Icons card is there, and units and location are not on this screen: they live in Weather settings
        composeRule.onNodeWithTag("icon_style_row").assertExists()
        composeRule.onNodeWithTag("accessory_units_and_location_row").assertDoesNotExist()
        composeRule.onNodeWithTag("unit_temperature_row").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsColouredIconsByDefault() {
        // Given weather on with the default icon style
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the preview row shows the coloured set
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsMonochromeIconsWhenMonochromeIsSelected() {
        // Given weather on with monochrome icons chosen
        setContent(
            ClockAccessoriesUiState(
                settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME),
                weatherEnabledAnywhere = true,
            ),
        )

        // Then the preview row shows the monochrome set, not the coloured one
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun aFacetThatOverridesTheClockEditsItsOwnIconStyle() {
        // Given a facet that overrides the clock with weather on and its own monochrome icons
        val facet = Facet(
            id = 1L, name = "Work", position = 1, overrideClock = true, clockShowWeather = true,
            clockWeatherIconStyle = WeatherIconStyle.MONOCHROME,
        )
        setContent(
            ClockAccessoriesUiState(facet = facet, settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true),
        )

        // Then its own icon style shows and can be edited
        composeRule.onNodeWithTag("icon_style_row").assertIsEnabled()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
    }

    @Test
    fun aFacetThatInheritsShowsTheLaunchersIconStyle() {
        // Given a facet that inherits the clock, with the launcher on monochrome icons
        val facet = Facet(id = 1L, name = "Work", position = 1, overrideClock = false, clockWeatherIconStyle = WeatherIconStyle.COLORED)
        setContent(
            ClockAccessoriesUiState(
                facet = facet,
                settings = LauncherSettings(showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME),
                weatherEnabledAnywhere = true,
            ),
        )

        // Then the launcher's style shows, not the facet's own
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun thePreviewShowsTheWeatherSampleOnlyWhileWeatherIsOn() {
        // Given weather on, in Celsius
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true, temperatureUnit = TemperatureUnit.CELSIUS), weatherEnabledAnywhere = true))

        // Then the preview clock carries the weather sample
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewSampleUsesTheChosenTemperatureUnit() {
        // Given weather on, in Fahrenheit
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true, temperatureUnit = TemperatureUnit.FAHRENHEIT), weatherEnabledAnywhere = true))

        // Then the sample reads in Fahrenheit, with its letter
        composeRule.onNodeWithContentDescription("70°F, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewDropsTheWeatherSampleWhenWeatherIsOff() {
        // Given weather off
        setContent(ClockAccessoriesUiState())

        // Then the preview clock has none
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertDoesNotExist()
    }

    @Test
    fun weatherSwitchReportsTheNewValue() {
        // Given weather off
        var shown: Boolean? = null
        setContent(ClockAccessoriesUiState(), actions = ClockAccessoriesActions(onShowWeather = { shown = it }))

        // When flipping the switch
        composeRule.onNodeWithTag("accessory_weather_toggle").performClick()

        // Then it asks to turn weather on
        assertEquals(true, shown)
    }

    @Test
    fun aFacetThatInheritsCannotEditItsSwitches() {
        // Given a facet that does not override the clock
        setContent(ClockAccessoriesUiState(facet = Facet(id = 1L, name = "Work", position = 1), weatherEnabledAnywhere = true))

        // Then its switches are disabled
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsNotEnabled()
    }

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")

    @Test
    fun aFacetOverridingTheClockPicksItsOwnWeatherLocationLikeAWidgetDoes() {
        // Given a facet overriding the clock with weather on, and a city saved in Weather settings
        var chosen: WidgetWeatherLocation? = WidgetWeatherLocation.Current
        val facet = Facet(id = 1L, name = "Work", position = 1, overrideClock = true, clockShowWeather = true)
        setContent(
            ClockAccessoriesUiState(facet = facet, settings = LauncherSettings(showWeather = true, savedWeatherCities = listOf(oslo)), weatherEnabledAnywhere = true),
            actions = ClockAccessoriesActions(onWeatherLocation = { chosen = it }),
        )

        // Then it follows Weather settings to begin with, and tapping a city chooses that city
        composeRule.onNodeWithTag("accessory_location_follow").performScrollTo().assertIsEnabled()
        composeRule.onNodeWithTag("accessory_location_city_Oslo").performScrollTo().performClick()
        assertEquals(WidgetWeatherLocation.City(oslo), chosen)

        // And "same as Weather settings" gives the place back
        composeRule.onNodeWithTag("accessory_location_follow").performClick()
        assertEquals(null, chosen)
    }

    @Test
    fun theLocationCardIsNotOnTheLauncherDefaultScreen() {
        // Given the default screen with weather on
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then there is no location card: the launcher-wide location is in Weather settings
        composeRule.onNodeWithTag("accessory_location_follow").assertDoesNotExist()
    }

    @Test
    fun aFacetThatInheritsTheClockShowsTheLocationDimmedAndCannotChangeIt() {
        // Given a facet that inherits the clock, with weather shown
        val facet = Facet(id = 1L, name = "Work", position = 1, overrideClock = false)
        setContent(ClockAccessoriesUiState(facet = facet, settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the choices are there but cannot be changed here
        composeRule.onNodeWithTag("accessory_location_follow").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithTag("accessory_location_current").assertIsNotEnabled()
    }


    @Test
    fun theLinkToWeatherSettingsComesAfterTheIconsCardAndOpensWeatherSettings() {
        // Given weather on for the launcher default
        var opened = false
        setContent(
            ClockAccessoriesUiState(settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenClockStyle = {}, onOpenWeatherSettings = { opened = true }),
        )

        // Then the Icons card follows the switch, and the link to Weather settings comes after the Icons card
        val toggle = composeRule.onNodeWithTag("accessory_weather_toggle").getUnclippedBoundsInRoot()
        val icons = composeRule.onNodeWithTag("icon_style_row").getUnclippedBoundsInRoot()
        val link = composeRule.onNodeWithTag("accessory_weather_settings_row").getUnclippedBoundsInRoot()
        assertTrue(icons.top >= toggle.bottom)
        assertTrue(link.top >= icons.bottom)

        // And tapping it opens Weather settings
        composeRule.onNodeWithTag("accessory_weather_settings_row").performScrollTo().performClick()
        assertEquals(true, opened)
    }

    @Test
    fun theWeatherOptionsAreGoneWhileWeatherIsOff() {
        // Given weather off
        setContent(ClockAccessoriesUiState(settings = LauncherSettings(showWeather = false)))

        // Then there is no link to Weather settings
        composeRule.onNodeWithTag("accessory_weather_settings_row").assertDoesNotExist()
    }

    @Test
    fun aFacetsLocationPickerComesAfterTheIconsCard() {
        // Given a facet overriding the clock with weather on
        val facet = Facet(id = 1L, name = "Work", position = 1, overrideClock = true, clockShowWeather = true)
        setContent(ClockAccessoriesUiState(facet = facet, settings = LauncherSettings(showWeather = true), weatherEnabledAnywhere = true))

        // Then the Icons card is under the switch, and its location picker is below the Icons card
        val toggle = composeRule.onNodeWithTag("accessory_weather_toggle").getUnclippedBoundsInRoot()
        val icons = composeRule.onNodeWithTag("icon_style_row").getUnclippedBoundsInRoot()
        val picker = composeRule.onNodeWithTag("accessory_location_follow").getUnclippedBoundsInRoot()
        assertTrue(icons.top >= toggle.bottom)
        assertTrue(picker.top >= icons.bottom)
    }
}
