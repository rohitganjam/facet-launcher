package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface RefreshOutcome {
    data object Updated : RefreshOutcome

    /** Nothing newer can exist yet, so no request was made. */
    data object UpToDate : RefreshOutcome

    /** An attempt was made a moment ago; [retryAt] is when the next one is allowed. */
    data class TooSoon(val retryAt: Instant) : RefreshOutcome

    /** MET is over its limit or has blocked us: the call failed and the user can try again after [until]. */
    data class Busy(val until: Instant) : RefreshOutcome
    data object Offline : RefreshOutcome
    data object Failed : RefreshOutcome
}

/** How long a row is kept; the earlier hours of today are what lets today's low be known after it has passed. */
private val KEEP_HOURS_FOR: Duration = Duration.ofDays(2)

/**
 * The forecast for one place: stored hourly rows plus the fetch state, refreshed through [WeatherRefreshPolicy].
 * The only code that talks to [WeatherApi] and [WeatherStore]. Refreshes run one at a time, so a second tap
 * while one is running waits and then finds the gate closed; nothing here retries.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val store: WeatherStore,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    fun observeHours(): Flow<List<WeatherHour>> = store.observeHours()
    fun observeState(): Flow<WeatherFetchState> = store.observeState()

    suspend fun refresh(place: WeatherPlace): RefreshOutcome = mutex.withLock {
        val now = clock.instant()
        var state = store.readState()
        when (val decision = WeatherRefreshPolicy.decide(state, place, now)) {
            is RefreshDecision.UpToDate -> return RefreshOutcome.UpToDate
            is RefreshDecision.TooSoon -> return RefreshOutcome.TooSoon(decision.retryAt)
            is RefreshDecision.Blocked -> return RefreshOutcome.Busy(decision.until)
            RefreshDecision.Go -> Unit
        }
        if (state.place != place) {
            // Another place's hours must not show as this one's, even if this fetch fails.
            store.replaceHours(emptyList())
            state = WeatherFetchState(place = place, blockedUntil = state.blockedUntil)
        }
        when (val result = api.fetch(place, state.lastModified)) {
            is WeatherApiResult.Success -> {
                store.replaceHours(mergeStoredHours(store.readHours(), result.forecast.hours, now))
                store.writeState(state.copy(fetchedAt = now, expiresAt = result.expiresAt, lastModified = result.lastModified, lastAttemptAt = now, blockedUntil = null))
                RefreshOutcome.Updated
            }
            is WeatherApiResult.NotModified -> {
                store.writeState(state.copy(expiresAt = result.expiresAt ?: state.expiresAt, lastAttemptAt = now, blockedUntil = null))
                RefreshOutcome.UpToDate
            }
            is WeatherApiResult.Failed -> fail(state, result, now)
        }
    }

    private suspend fun fail(state: WeatherFetchState, result: WeatherApiResult.Failed, now: Instant): RefreshOutcome {
        val blockedUntil = when (result.reason) {
            WeatherFailure.RATE_LIMITED, WeatherFailure.BLOCKED -> result.retryAt ?: now.plus(WeatherRefreshPolicy.DEFAULT_RATE_LIMIT_WAIT)
            else -> null
        }
        store.writeState(state.copy(lastAttemptAt = now, blockedUntil = blockedUntil ?: state.blockedUntil))
        return when {
            blockedUntil != null -> RefreshOutcome.Busy(blockedUntil)
            result.reason == WeatherFailure.OFFLINE -> RefreshOutcome.Offline
            else -> RefreshOutcome.Failed
        }
    }
}

/**
 * New forecast on top of what is stored: the new points replace everything from their first time on, hours
 * before that stay as last seen (the best record of what happened), and rows older than two days go.
 */
internal fun mergeStoredHours(existing: List<WeatherHour>, incoming: List<WeatherHour>, now: Instant): List<WeatherHour> {
    val firstIncoming = incoming.minOfOrNull { it.time } ?: return existing
    val cutoff = now.minus(KEEP_HOURS_FOR)
    return (existing.filter { it.time < firstIncoming && it.time >= cutoff } + incoming).sortedBy { it.time }
}
