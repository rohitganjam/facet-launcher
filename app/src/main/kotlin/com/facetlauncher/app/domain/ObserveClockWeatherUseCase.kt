package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.encodeWeatherLocation
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * The weather the clock shows. [weather] is `null` while nothing is known; [own] is whether it is for the place the active facet
 * chose for itself (and not the launcher-wide one), where [unavailable] and [locationMissing] say why there is none.
 */
data class ClockWeather(
    val weather: WeatherState? = null,
    val own: Boolean = false,
    val unavailable: Boolean = false,
    val locationMissing: Boolean = false,
)

/**
 * The weather for Home's clock and its sheet: the launcher-wide forecast, unless the active facet overrides the clock with a
 * location of its own, in which case that place is resolved the way a Weather widget's is (so a place two things show is fetched
 * once), and the launcher's own forecast is used again whenever that already covers it. Re-derived every
 * [ObserveWeatherUseCase.TICK] and on [refreshOwn].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveClockWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val observeWeather: ObserveWeatherUseCase,
    private val resolver: NativeWidgetOverrideResolver,
) {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Looks the active facet's own place up again now (still through the forecast gate, so it cannot ask MET more often than that allows). */
    fun refreshOwn() {
        refreshRequests.tryEmit(Unit)
    }

    operator fun invoke(): Flow<ClockWeather> = combine(settingsRepository.settings, facetRepository.observeFacets()) { settings, facets ->
        val location = resolveFacetStyle(facets.firstOrNull { it.id == settings.activeFacetId }, settings).clock.weatherLocation
        // Only what decides which place and whose forecast counts: any other setting changing must not start a new lookup.
        Key(location, settings.showWeather, settings.weatherLocationMode, settings.weatherCity?.let { it.latitude to it.longitude })
    }.distinctUntilChanged().flatMapLatest { key ->
        val location = key.location
        if (location == null) {
            observeWeather().map { ClockWeather(weather = it) }
        } else {
            combine(observeWeather(), ownOverride(location)) { launcher, own ->
                if (own == null) {
                    ClockWeather(weather = launcher)
                } else {
                    ClockWeather(weather = own.weather, own = true, unavailable = own.weatherUnavailable, locationMissing = own.locationMissing)
                }
            }
        }
    }

    /** The widget-path result for [location]: `null` when the launcher's own forecast already covers it. */
    private fun ownOverride(location: WidgetWeatherLocation): Flow<NativeWidgetOverride?> =
        refreshRequests.onStart { emit(Unit) }.flatMapLatest {
            flow {
                while (true) {
                    val settings = settingsRepository.settings.first()
                    val setting = NativeWidgetSetting(KEY, NativeWidgetType.WEATHER, encodeWeatherLocation(location))
                    emit(resolver.resolve(listOf(setting), settings)[KEY])
                    delay(ObserveWeatherUseCase.TICK.toMillis())
                }
            }
        }

    private data class Key(
        val location: WidgetWeatherLocation?,
        val launcherWeatherOn: Boolean,
        val launcherMode: WeatherLocationMode,
        val launcherCity: Pair<Double, Double>?,
    )

    private companion object {
        const val KEY = 0
    }
}
