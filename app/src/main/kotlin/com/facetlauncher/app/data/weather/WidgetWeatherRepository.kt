package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One place's forecast as a Weather widget with its own location holds it: [state] is the same gate state [WeatherRepository] keeps. */
data class WidgetForecast(val hours: List<WeatherHour>, val state: WeatherFetchState)

/**
 * Forecasts for the places Weather widgets on the Hub are set to, apart from the launcher-wide one [WeatherRepository] stores.
 * Kept in memory, a place at a time, through the same [WeatherRefreshPolicy] gate, so a widget never asks MET more often than
 * the launcher-wide weather does; a restart fetches each place again once. Refreshes run one at a time.
 */
@Singleton
class WidgetWeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    private val forecasts = mutableMapOf<WeatherPlace, WidgetForecast>()

    /** Fetches [place] if the gate allows it, and returns what is held for it afterwards (possibly stale, or `null` if nothing has ever arrived). */
    suspend fun refresh(place: WeatherPlace): WidgetForecast? = mutex.withLock {
        val now = clock.instant()
        val held = forecasts[place]
        val state = held?.state ?: WeatherFetchState(place = place)
        if (WeatherRefreshPolicy.decide(state, place, now) != RefreshDecision.Go) return@withLock held?.takeIf { it.hours.isNotEmpty() }
        apply(place, held, state, api.fetch(place, state.lastModified), now)
    }

    /** Records what the request returned for [place] and returns the forecast to show: the new one, or the held one when the request brought nothing. */
    private fun apply(place: WeatherPlace, held: WidgetForecast?, state: WeatherFetchState, result: WeatherApiResult, now: Instant): WidgetForecast? {
        val updated = when (result) {
            is WeatherApiResult.Success -> WidgetForecast(
                hours = mergeStoredHours(held?.hours.orEmpty(), result.forecast.hours, now),
                state = state.copy(fetchedAt = now, expiresAt = result.expiresAt, lastModified = result.lastModified, lastAttemptAt = now, blockedUntil = null),
            )
            is WeatherApiResult.NotModified -> WidgetForecast(
                hours = held?.hours.orEmpty(),
                state = state.copy(expiresAt = result.expiresAt ?: state.expiresAt, lastAttemptAt = now, blockedUntil = null),
            )
            is WeatherApiResult.Failed -> WidgetForecast(
                hours = held?.hours.orEmpty(),
                state = state.copy(lastAttemptAt = now, blockedUntil = blockedUntil(result, now) ?: state.blockedUntil),
            )
        }
        forecasts[place] = updated
        return updated.takeIf { it.hours.isNotEmpty() }
    }

    private fun blockedUntil(result: WeatherApiResult.Failed, now: Instant): Instant? = when (result.reason) {
        WeatherFailure.RATE_LIMITED, WeatherFailure.BLOCKED -> result.retryAt ?: now.plus(WeatherRefreshPolicy.DEFAULT_RATE_LIMIT_WAIT)
        else -> null
    }

    /** Stores [name] for [place], so the name is looked up once. */
    suspend fun setPlaceName(place: WeatherPlace, name: String?, source: PlaceNameSource?) {
        mutex.withLock {
            forecasts[place]?.let { forecasts[place] = it.copy(state = it.state.copy(placeName = name, placeNameSource = source)) }
        }
    }
}
