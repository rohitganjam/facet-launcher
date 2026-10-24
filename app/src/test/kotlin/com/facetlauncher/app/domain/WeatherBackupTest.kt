package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.BackupWeatherCity
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WindSpeedUnit
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/** What backup and restore do with the clock accessory switches and the weather options. */
class WeatherBackupTest {

    private val pune = WeatherCity("Pune", 18.52, 73.86, "Asia/Kolkata")
    private val london = WeatherCity("London", 51.51, -0.13, "Europe/London")

    // --- export ---

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val placementRepository = mock(PlacementRepository::class.java)
    private val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
    private val folderRepository = mock(FolderRepository::class.java).also {
        runBlocking { `when`(it.getRawFolders()).thenReturn(emptyList()) }
    }
    private val backupRepository = mock(BackupRepository::class.java)

    private val export = ExportBackupUseCase(
        settingsRepository, facetRepository, placementRepository, widgetPlacementRepository, folderRepository, backupRepository,
    )

    private val importUseCase = ImportBackupUseCase(
        backupRepository, settingsRepository, facetRepository, placementRepository, folderRepository,
        mock(RepairOrphanedProfileRowsUseCase::class.java),
    )

    @Test
    fun `export writes the accessory switches, units, mode and city for the launcher and the switches for each facet`() = runTest {
        // Given weather configured, and a facet that overrides the accessories
        val facet = Facet(id = 7, name = "Work", position = 0, clockShowAlarm = false, clockShowBattery = false, clockShowWeather = true)
        `when`(settingsRepository.settings).thenReturn(
            flowOf(
                LauncherSettings(
                    showNextAlarm = false, showBatteryPercentage = true, showWeather = true,
                    temperatureUnit = TemperatureUnit.FAHRENHEIT, windSpeedUnit = WindSpeedUnit.MPH,
                    rainUnit = RainUnit.INCH, pressureUnit = PressureUnit.INHG,
                    weatherLocationMode = WeatherLocationMode.CITY, weatherCity = pune, savedWeatherCities = listOf(pune, london),
                ),
            ),
        )
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        for (target in listOf(PlacementTarget(PlacementList.FAVORITES, 7), PlacementTarget(PlacementList.DOCK, 7), PlacementTarget.GlobalDock, PlacementTarget.GlobalFavorites)) {
            `when`(placementRepository.getRawApps(target)).thenReturn(emptyList())
            `when`(placementRepository.getRawFolderPlacements(target)).thenReturn(emptyList())
        }

        // When exported
        val bundle = export(mock(Uri::class.java))

        // Then the launcher and the facet carry them
        val settings = bundle.settings
        assertFalse(settings.showNextAlarm)
        assertTrue(settings.showBatteryPercentage)
        assertTrue(settings.showWeather)
        assertEquals(listOf("FAHRENHEIT", "MPH", "INCH", "INHG"), listOf(settings.temperatureUnit, settings.windSpeedUnit, settings.rainUnit, settings.pressureUnit))
        assertEquals("CITY", settings.weatherLocationMode)
        assertEquals(listOf("Pune", 18.52, 73.86, "Asia/Kolkata"), listOf(settings.weatherCityName, settings.weatherCityLatitude, settings.weatherCityLongitude, settings.weatherCityTimezone))
        assertEquals(listOf(BackupWeatherCity("Pune", 18.52, 73.86, "Asia/Kolkata"), BackupWeatherCity("London", 51.51, -0.13, "Europe/London")), settings.savedWeatherCities)
        val exported = bundle.facets[0]
        assertFalse(exported.clockShowAlarm)
        assertFalse(exported.clockShowBattery)
        assertTrue(exported.clockShowWeather)
    }

    // --- restore ---

    private fun backupSettings() = BackupSettings(
        use24HourTime = true, dockDisplayMode = "TEXT", drawerPresentation = "GRID", drawerGridSize = "FOUR_BY_FOUR",
        drawerListItemSize = "COMPACT", drawerOpacity = 0.5f, notificationDotsEnabled = false, notificationBadgeStyle = "COUNT",
        showNotificationsInAppMenu = false, showDrawerIcons = false, showDrawerLabels = false, searchBarPosition = "BOTTOM",
        activeFacetIndex = null, showAllDayEvents = false, searchContactsEnabled = true, themeMode = "DARK",
        systemBarIconStyle = "LIGHT", accentFromSystem = false, customAccentSwatch = "BLUE", wallpaperAccentRole = "SECONDARY",
        iconRenderMode = "MONOCHROME", launcherFontOption = "SYSTEM", appLabelColorOption = "ACCENT_SECONDARY",
        appRowPosition = "RIGHT", appRowPresentation = "TEXT_ONLY", listContentMode = "RECENTS", appsToShowCount = 6,
        clockTemplateId = "RULE", clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME_INVERTED", clockShowMeridiem = true,
    )

    private fun bundle(settings: BackupSettings) = BackupBundle(
        exportedAtEpochMillis = 0L, settings = settings,
        facets = emptyList(), dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = emptyList(),
    )

    @Test
    fun `restore applies the switches, units, mode and city`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            bundle(
                backupSettings().copy(
                    showNextAlarm = false, showBatteryPercentage = false, showWeather = true,
                    temperatureUnit = "FAHRENHEIT", windSpeedUnit = "MPH", rainUnit = "INCH", pressureUnit = "MMHG",
                    weatherLocationMode = "CITY", weatherCityName = "Pune", weatherCityLatitude = 18.52,
                    weatherCityLongitude = 73.86, weatherCityTimezone = "Asia/Kolkata",
                    savedWeatherCities = listOf(BackupWeatherCity("Pune", 18.52, 73.86, "Asia/Kolkata"), BackupWeatherCity("London", 51.51, -0.13, "Europe/London")),
                ),
            ),
        )

        importUseCase(uri)

        verify(settingsRepository).setShowNextAlarm(false)
        verify(settingsRepository).setShowBatteryPercentage(false)
        verify(settingsRepository).setShowWeather(true)
        verify(settingsRepository).setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        verify(settingsRepository).setWindSpeedUnit(WindSpeedUnit.MPH)
        verify(settingsRepository).setRainUnit(RainUnit.INCH)
        verify(settingsRepository).setPressureUnit(PressureUnit.MMHG)
        verify(settingsRepository).setWeatherLocationMode(WeatherLocationMode.CITY)
        verify(settingsRepository).setWeatherCity(pune)
        verify(settingsRepository).setSavedWeatherCities(listOf(pune, london))
    }

    @Test
    fun `restore from a backup without weather fields uses the defaults and clears the city`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(bundle(backupSettings()))

        importUseCase(uri)

        verify(settingsRepository).setShowNextAlarm(true)
        verify(settingsRepository).setShowBatteryPercentage(true)
        verify(settingsRepository).setShowWeather(false)
        verify(settingsRepository).setTemperatureUnit(TemperatureUnit.AUTO)
        verify(settingsRepository).setWeatherLocationMode(WeatherLocationMode.CURRENT)
        verify(settingsRepository).setWeatherCity(null)
    }

    @Test
    fun `restore ignores a half-saved city and unknown unit names`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(backupRepository.readBackup(uri)).thenReturn(
            bundle(backupSettings().copy(windSpeedUnit = "KNOTS", weatherCityName = "Pune", weatherCityLatitude = 18.52)),
        )

        importUseCase(uri)

        verify(settingsRepository).setWindSpeedUnit(WindSpeedUnit.AUTO)
        verify(settingsRepository).setWeatherCity(null)
        verify(settingsRepository, never()).setWeatherCity(pune)
    }
}
