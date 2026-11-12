package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.weather.WeatherRepository
import com.facetlauncher.app.data.weather.WidgetForecast
import com.facetlauncher.app.data.weather.WidgetWeatherRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** What one widget Facet draws itself shows instead of the launcher-wide data: `null` fields follow the launcher. */
data class NativeWidgetOverride(
    val events: List<CalendarEvent>? = null,
    val weather: WeatherState? = null,
    /** The widget is set to its own location but there is no forecast for it yet (no position, no permission, offline). */
    val weatherUnavailable: Boolean = false,
    /** The widget is unavailable because it wants the position and the location permission is off. */
    val locationMissing: Boolean = false,
    /** The widget's own background; `null` is the default. */
    val glass: GlassStyle? = null,
)

/** One native widget's own setting, as stored: [key] says which widget, [encodedConfig] is what `NativeWidgetConfig` encodes. */
data class NativeWidgetSetting<K>(val key: K, val type: NativeWidgetType?, val encodedConfig: String?)

/**
 * Works out what the widgets Facet draws itself show where they are set to their own calendars or location: the calendar's
 * events, or a forecast shared by place, or its own background. A widget that follows the launcher has no entry in the result.
 */
class NativeWidgetOverrideResolver @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val widgetWeatherRepository: WidgetWeatherRepository,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val resolvePlace: ResolvePlaceUseCase,
    private val clock: Clock,
) {
    suspend fun <K> resolve(widgets: List<NativeWidgetSetting<K>>, settings: LauncherSettings): Map<K, NativeWidgetOverride> =
        widgets.mapNotNull { setting ->
            val config = decodeNativeWidgetConfig(setting.encodedConfig)
            val override = overrideFor(setting.type, config, settings)?.copy(glass = config.glass) ?: config.glass?.let { NativeWidgetOverride(glass = it) }
            override?.let { setting.key to it }
        }.toMap()

    private suspend fun overrideFor(type: NativeWidgetType?, config: NativeWidgetConfig, settings: LauncherSettings): NativeWidgetOverride? = when (type) {
        NativeWidgetType.AGENDA -> NativeWidgetOverride(events = agendaEvents(config))
        NativeWidgetType.WEATHER -> weatherFor(config.weatherLocation, settings)
        else -> null
    }

    /** The events an Agenda shows: its own calendars (every calendar until it picks) and all-day choice (shown until it hides them). */
    private suspend fun agendaEvents(config: NativeWidgetConfig): List<CalendarEvent> =
        if (calendarPermissionRepository.isGranted()) {
            calendarRepository.getAgendaEvents(config.calendarIds, config.showAllDayEvents ?: true)
        } else {
            emptyList()
        }

    /**
     * A Weather widget shows its forecast whether or not the launcher's own Weather is on: it follows the launcher (no entry here) only
     * while that is on and for the same place, and otherwise fetches for its place itself, as long as the position it needs is allowed.
     * [chosen] is `null` for a widget that follows the launcher's own location setting.
     */
    private suspend fun weatherFor(chosen: WidgetWeatherLocation?, settings: LauncherSettings): NativeWidgetOverride? {
        if (chosen == null && settings.showWeather) return null
        val location = chosen ?: launcherLocation(settings) ?: return NativeWidgetOverride(weatherUnavailable = true)
        val unavailable = NativeWidgetOverride(weatherUnavailable = true, locationMissing = location == WidgetWeatherLocation.Current && !locationRepository.isPermissionGranted())
        val target = targetFor(location, settings) ?: return null
        val (place, ownSettings) = target.takeIf { it.first != null }?.let { it.first!! to it.second } ?: return unavailable
        val forecast = launcherForecastFor(place) ?: widgetWeatherRepository.refresh(place)
        // A chosen city carries its own name; only a position needs one looked up.
        val named = if (forecast != null && location == WidgetWeatherLocation.Current) nameIfMissing(place, forecast) else forecast
        return named?.let { NativeWidgetOverride(weather = ObserveWeatherUseCase.buildWeatherState(ownSettings, it.hours, it.state, clock.instant())) }
            ?: unavailable
    }

    /** What the launcher's own Weather setting points at: the position, or its chosen city (`null` when none is chosen yet). */
    private fun launcherLocation(settings: LauncherSettings): WidgetWeatherLocation? = when (settings.weatherLocationMode) {
        WeatherLocationMode.CURRENT -> WidgetWeatherLocation.Current
        WeatherLocationMode.CITY -> settings.weatherCity?.let { WidgetWeatherLocation.City(it) }
    }

    /**
     * Which place [location] means and the settings to derive its weather with; `null` when the widget just follows the launcher
     * (current location while the launcher's Weather is on and already follows the position), a `null` place when the position is not available.
     */
    private suspend fun targetFor(location: WidgetWeatherLocation, settings: LauncherSettings): Pair<WeatherPlace?, LauncherSettings>? = when (location) {
        is WidgetWeatherLocation.City -> WeatherPlace.rounded(location.city.latitude, location.city.longitude) to
            settings.copy(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = location.city)
        WidgetWeatherLocation.Current -> when {
            settings.showWeather && settings.weatherLocationMode == WeatherLocationMode.CURRENT -> null
            else -> currentPlace()?.let { it to settings.copy(weatherLocationMode = WeatherLocationMode.CURRENT, weatherCity = null) } ?: (null to settings)
        }
    }

    private suspend fun currentPlace(): WeatherPlace? {
        val fix = locationRepository.takeIf { it.isPermissionGranted() }?.let { it.recentLocation(RECENT_FIX_MS) ?: it.currentLocation() }
        return fix?.let { WeatherPlace.rounded(it.latitude, it.longitude, it.altitudeM) }
    }

    /** The launcher-wide forecast when it is for the same place, so a widget set to the launcher's own city makes no request of its own. */
    private suspend fun launcherForecastFor(place: WeatherPlace): WidgetForecast? {
        val state = weatherRepository.observeState().first()
        val stored = state.place ?: return null
        if (stored.latitude != place.latitude || stored.longitude != place.longitude) return null
        return weatherRepository.observeHours().first().takeIf { it.isNotEmpty() }?.let { WidgetForecast(it, state) }
    }

    private suspend fun nameIfMissing(place: WeatherPlace, forecast: WidgetForecast): WidgetForecast {
        if (forecast.state.placeName != null) return forecast
        val resolved = resolvePlace(place.latitude, place.longitude)
        if (resolved.name == null) return forecast
        widgetWeatherRepository.setPlaceName(place, resolved.name, resolved.source)
        return forecast.copy(state = forecast.state.copy(placeName = resolved.name, placeNameSource = resolved.source))
    }

    private companion object {
        const val RECENT_FIX_MS = RefreshWeatherUseCase.RECENT_FIX_MS
    }
}
