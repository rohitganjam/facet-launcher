package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.CalendarEvent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** What one widget Facet draws itself shows instead of the launcher-wide data: `null` fields follow the launcher. */
data class NativeWidgetOverride(
    val events: List<CalendarEvent>? = null,
    val weather: WeatherState? = null,
    /** The widget is set to its own location but there is no forecast for it yet (no position, no permission, offline). */
    val weatherUnavailable: Boolean = false,
)

/** What [HubViewModel][com.facetlauncher.app.ui.hub.HubViewModel] reads; an interface so a test can pass a fixed answer. */
fun interface NativeWidgetOverrides {
    operator fun invoke(): Flow<Map<Int, NativeWidgetOverride>>
}

/**
 * The data of the Hub's Agenda and Weather widgets that are set to their own calendars or location, by widget id. A widget that
 * follows the launcher has no entry, so the launcher-wide state is used for it. Re-derived every [ObserveWeatherUseCase.TICK],
 * which also lets a widget's forecast refresh once MET's `Expires` has passed.
 */
class ObserveNativeWidgetOverridesUseCase @Inject constructor(
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val widgetWeatherRepository: WidgetWeatherRepository,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val resolvePlace: ResolvePlaceUseCase,
    private val clock: Clock,
) : NativeWidgetOverrides {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<Map<Int, NativeWidgetOverride>> = combine(
        widgetPlacementRepository.observeAll().map { placements ->
            placements.filter { it.nativeWidgetType != null }.map { Triple(it.appWidgetId, it.nativeWidgetType, it.nativeWidgetConfig) }
        }.distinctUntilChanged(),
        settingsRepository.settings,
    ) { widgets, settings -> widgets to settings }
        .flatMapLatest { (widgets, settings) ->
            flow {
                while (true) {
                    emit(widgets.mapNotNull { (id, type, encoded) -> overrideFor(type, decodeNativeWidgetConfig(encoded), settings)?.let { id to it } }.toMap())
                    delay(ObserveWeatherUseCase.TICK.toMillis())
                }
            }
        }

    private suspend fun overrideFor(type: NativeWidgetType?, config: NativeWidgetConfig, settings: LauncherSettings): NativeWidgetOverride? = when (type) {
        NativeWidgetType.AGENDA -> config.calendarIds?.let { ids ->
            NativeWidgetOverride(events = if (calendarPermissionRepository.isGranted()) calendarRepository.getUpcomingEvents(ids, settings.showAllDayEvents) else emptyList())
        }
        NativeWidgetType.WEATHER -> config.weatherLocation?.let { weatherFor(it, settings) }
        else -> null
    }

    private suspend fun weatherFor(location: WidgetWeatherLocation, settings: LauncherSettings): NativeWidgetOverride? {
        val target = targetFor(location, settings) ?: return null
        val (place, ownSettings) = target.takeIf { it.first != null }?.let { it.first!! to it.second } ?: return NativeWidgetOverride(weatherUnavailable = true)
        val forecast = launcherForecastFor(place) ?: widgetWeatherRepository.refresh(place)
        // A chosen city carries its own name; only a position needs one looked up.
        val named = if (forecast != null && location == WidgetWeatherLocation.Current) nameIfMissing(place, forecast) else forecast
        return named?.let { NativeWidgetOverride(weather = ObserveWeatherUseCase.buildWeatherState(ownSettings, it.hours, it.state, clock.instant())) }
            ?: NativeWidgetOverride(weatherUnavailable = true)
    }

    /**
     * Which place [location] means and the settings to derive its weather with; `null` when the widget just follows the launcher
     * (current location while the launcher already follows the position), a `null` place when the position is not available.
     */
    private suspend fun targetFor(location: WidgetWeatherLocation, settings: LauncherSettings): Pair<WeatherPlace?, LauncherSettings>? = when (location) {
        is WidgetWeatherLocation.City -> WeatherPlace.rounded(location.city.latitude, location.city.longitude) to
            settings.copy(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = location.city)
        WidgetWeatherLocation.Current -> when {
            settings.weatherLocationMode == WeatherLocationMode.CURRENT -> null
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
