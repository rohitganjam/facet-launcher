package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ClockAccessoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")

    private fun setContent(
        uiState: ClockAccessoriesUiState,
        navigation: ClockAccessoriesNavigation = ClockAccessoriesNavigation(onBack = {}),
        actions: ClockAccessoriesActions = ClockAccessoriesActions(),
    ) {
        composeRule.setContent {
            FacetLauncherTheme { ClockAccessoriesContent(uiState, navigation, actions) }
        }
        composeRule.waitForIdle()
    }

    /** The accessories state for a facet whose clock holds [clock]. */
    private fun stateFor(clock: ClockWidgetConfig, settings: LauncherSettings = LauncherSettings()) = ClockAccessoriesUiState(
        facet = Facet(
            id = 1L, name = "Work", position = 1,
            clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = clock).encode())),
        ),
        settings = settings,
    )

    @Test
    fun theHeaderIsPinnedOutsideTheScrollingContent() {
        // Given the screen
        setContent(stateFor(ClockWidgetConfig()))

        // Then the back button is not inside the scrolling content, so it stays pinned while that scrolls
        composeRule.onAllNodes(hasTestTag("back_button") and hasAnyAncestor(hasTestTag("clock_accessories_screen"))).assertCountEquals(0)
        composeRule.onNodeWithTag("back_button").assertExists()
    }

    @Test
    fun iconsAreHiddenWhileTheClockShowsNoWeather() {
        // Given a clock with weather off
        setContent(stateFor(ClockWidgetConfig(showWeather = false)))

        // Then the switches show but the Icons card does not
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("icon_style_row").assertDoesNotExist()
    }

    @Test
    fun iconsShowOnceTheClockShowsWeather() {
        // Given a clock with weather on
        setContent(stateFor(ClockWidgetConfig(showWeather = true)))

        // Then the Icons card is there, and units and location are not on this screen: they live in Weather settings
        composeRule.onNodeWithTag("icon_style_row").assertExists()
        composeRule.onNodeWithTag("accessory_units_and_location_row").assertDoesNotExist()
        composeRule.onNodeWithTag("unit_temperature_row").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsColouredIconsByDefault() {
        setContent(stateFor(ClockWidgetConfig(showWeather = true)))

        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertDoesNotExist()
    }

    @Test
    fun theIconsCardPreviewShowsTheClocksOwnMonochromeIcons() {
        // Given a clock with its own monochrome icons, and a launcher that says coloured
        setContent(stateFor(ClockWidgetConfig(showWeather = true, weatherIconStyle = WeatherIconStyle.MONOCHROME), LauncherSettings(weatherIconStyle = WeatherIconStyle.COLORED)))

        // Then the clock's style shows, and it can be edited
        composeRule.onNodeWithTag("icon_style_row").assertIsEnabled()
        composeRule.onNodeWithTag("weather_icons_preview_MONOCHROME").assertExists()
        composeRule.onNodeWithTag("weather_icons_preview_COLORED").assertDoesNotExist()
    }

    @Test
    fun thePreviewShowsTheWeatherSampleOnlyWhileWeatherIsOn() {
        // Given weather on, in Celsius
        setContent(stateFor(ClockWidgetConfig(showWeather = true), LauncherSettings(temperatureUnit = TemperatureUnit.CELSIUS)))

        // Then the preview clock carries the weather sample
        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewSampleUsesTheChosenTemperatureUnit() {
        setContent(stateFor(ClockWidgetConfig(showWeather = true), LauncherSettings(temperatureUnit = TemperatureUnit.FAHRENHEIT)))

        composeRule.onNodeWithContentDescription("70°F, Partly cloudy").assertExists()
    }

    @Test
    fun thePreviewDropsTheWeatherSampleWhenWeatherIsOff() {
        setContent(stateFor(ClockWidgetConfig(showWeather = false)))

        composeRule.onNodeWithTag("clock_accessories_preview").assertExists()
        composeRule.onNodeWithContentDescription("21°C, Partly cloudy").assertDoesNotExist()
    }

    @Test
    fun weatherSwitchReportsTheNewValue() {
        // Given weather off
        var shown: Boolean? = null
        setContent(stateFor(ClockWidgetConfig(showWeather = false)), actions = ClockAccessoriesActions(onShowWeather = { shown = it }))

        // When flipping the switch
        composeRule.onNodeWithTag("accessory_weather_toggle").performClick()

        // Then it asks to turn weather on
        assertEquals(true, shown)
    }

    @Test
    fun theSwitchesAreLiveAndNothingSaysToInheritTheLauncher() {
        // Given a clock
        setContent(stateFor(ClockWidgetConfig()))

        // Then the switches are live, with no note about inheriting
        composeRule.onNodeWithTag("accessory_alarm_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("accessory_battery_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("accessory_weather_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("clock_accessories_open_clock_style").assertDoesNotExist()
    }

    @Test
    fun aClockPicksItsOwnWeatherLocationLikeAWidgetDoes() {
        // Given a clock with weather on, and a city saved in Weather settings
        var chosen: WidgetWeatherLocation? = WidgetWeatherLocation.Current
        setContent(
            stateFor(ClockWidgetConfig(showWeather = true), LauncherSettings(savedWeatherCities = listOf(oslo))),
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
    fun theLinkToWeatherSettingsComesAfterTheIconsCardAndOpensWeatherSettings() {
        // Given a clock with weather on
        var opened = false
        setContent(
            stateFor(ClockWidgetConfig(showWeather = true)),
            navigation = ClockAccessoriesNavigation(onBack = {}, onOpenWeatherSettings = { opened = true }),
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
        setContent(stateFor(ClockWidgetConfig(showWeather = false)))

        composeRule.onNodeWithTag("accessory_weather_settings_row").assertDoesNotExist()
        composeRule.onNodeWithTag("accessory_location_follow").assertDoesNotExist()
    }

    @Test
    fun theLocationPickerComesAfterTheIconsCard() {
        // Given a clock with weather on
        setContent(stateFor(ClockWidgetConfig(showWeather = true)))

        // Then the Icons card is under the switch, and the location picker is below the Icons card
        val toggle = composeRule.onNodeWithTag("accessory_weather_toggle").getUnclippedBoundsInRoot()
        val icons = composeRule.onNodeWithTag("icon_style_row").getUnclippedBoundsInRoot()
        val picker = composeRule.onNodeWithTag("accessory_location_follow").getUnclippedBoundsInRoot()
        assertTrue(icons.top >= toggle.bottom)
        assertTrue(picker.top >= icons.bottom)
    }
}
