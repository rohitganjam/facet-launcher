package com.facetlauncher.app.domain

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.weather.PlaceNameSource
import com.facetlauncher.app.data.weather.WeatherFetchState
import com.facetlauncher.app.data.weather.WeatherRepository
import java.time.Clock
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/** Everything the accessory and the sheet show, derived from the stored hours at one moment. Units are applied by the UI. */
data class WeatherState(
    /** The city, or the resolved name of the position; `null` when none is known. */
    val placeName: String?,
    /** `true` for a name taken from the offline list, which the UI shows as "Near X". */
    val placeNameIsApproximate: Boolean,
    /** The forecast for the current hour. */
    val now: WeatherHour,
    /** Up to 24 hourly points from the current hour on. */
    val hourly: List<WeatherHour>,
    /** Local days from today on, in [zone]. Today's low and high include the hours stored earlier in the day. */
    val days: List<WeatherDay>,
    val summary: RainSummary,
    /** When the stored forecast was last fetched. */
    val updatedAt: Instant?,
    /** Older than [ObserveWeatherUseCase.HIDE_AFTER]: the accessory hides, the sheet says how old it is. */
    val isOutdated: Boolean,
    /** The device's zone for the current location, the chosen city's own otherwise. */
    val zone: ZoneId,
)

/**
 * The weather as the UI shows it: the stored hours and the fetch state joined with the settings (which place, which zone) and
 * the time, re-derived every few minutes so "now" and the day boundaries move on by themselves. Emits `null` while nothing
 * is stored.
 */
class ObserveWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<WeatherState?> = combine(
        settingsRepository.settings,
        weatherRepository.observeHours(),
        weatherRepository.observeState(),
        ticks(),
    ) { settings, hours, state, now -> buildWeatherState(settings, hours, state, now) }

    private fun ticks(): Flow<Instant> = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK.toMillis())
        }
    }

    companion object {
        val TICK: Duration = Duration.ofMinutes(5)
        val HIDE_AFTER: Duration = Duration.ofHours(3)
        private const val HOURLY_POINTS = 24

        internal fun buildWeatherState(settings: LauncherSettings, hours: List<WeatherHour>, state: WeatherFetchState, now: Instant): WeatherState? {
            val zone = zoneFor(settings)
            val currentHour = now.truncatedTo(ChronoUnit.HOURS)
            val current = hours.lastOrNull { it.time <= now } ?: hours.firstOrNull()
            val hourly = hours.filter { it.time >= currentHour && it.precipitationMm != null }.take(HOURLY_POINTS)
            val today = now.atZone(zone).toLocalDate()
            val city = settings.weatherCity?.takeIf { settings.weatherLocationMode == WeatherLocationMode.CITY }
            return current?.let {
                WeatherState(
                    placeName = city?.name ?: state.placeName,
                    placeNameIsApproximate = city == null && state.placeNameSource == PlaceNameSource.OFFLINE,
                    now = it,
                    hourly = hourly,
                    days = dailySummaries(hours, zone).filter { day -> !day.date.isBefore(today) },
                    summary = summarizeRain(hours, now),
                    updatedAt = state.fetchedAt,
                    isOutdated = state.fetchedAt?.let { fetched -> Duration.between(fetched, now) > HIDE_AFTER } ?: true,
                    zone = zone,
                )
            }
        }

        private fun zoneFor(settings: LauncherSettings): ZoneId {
            val chosen = settings.weatherCity?.takeIf { settings.weatherLocationMode == WeatherLocationMode.CITY }?.timezone
            return try {
                if (chosen != null) ZoneId.of(chosen) else ZoneId.systemDefault()
            } catch (_: DateTimeException) {
                ZoneId.systemDefault()
            }
        }
    }
}
