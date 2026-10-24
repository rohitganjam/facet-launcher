package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Duration
import java.time.Instant

/** What the repository remembers between refreshes; persisted, so a killed process does not reset the gate. */
data class WeatherFetchState(
    val place: WeatherPlace? = null,
    val fetchedAt: Instant? = null,
    /** MET's `Expires`: nothing new exists before this. */
    val expiresAt: Instant? = null,
    val lastModified: String? = null,
    /** The last network attempt, successful or not. */
    val lastAttemptAt: Instant? = null,
    /** After a 429 (or a block): no attempt before this. */
    val blockedUntil: Instant? = null,
    /** The name resolved for [place] and which source gave it; both are dropped when the place changes. */
    val placeName: String? = null,
    val placeNameSource: PlaceNameSource? = null,
)

/** Where a place name came from: the device's geocoder (exact) or the nearest city of the offline list (shown as "Near X"). */
enum class PlaceNameSource { GEOCODER, OFFLINE }

sealed interface RefreshDecision {
    data object Go : RefreshDecision

    /** The forecast cannot have changed yet. */
    data class UpToDate(val expiresAt: Instant) : RefreshDecision

    /** An attempt was made a moment ago. */
    data class TooSoon(val retryAt: Instant) : RefreshDecision

    /** MET asked us to stop. */
    data class Blocked(val until: Instant) : RefreshDecision
}

/**
 * The one gate every refresh goes through (Home resume, the sheet's button, a changed location), so none of them
 * can hammer MET: never before `Expires`, never twice within [MIN_ATTEMPT_INTERVAL], and nothing at all while a
 * 429 is being waited out. A different place skips the first two, since that forecast is not a repeat.
 */
object WeatherRefreshPolicy {
    val MIN_ATTEMPT_INTERVAL: Duration = Duration.ofMinutes(10)

    /** Used when MET answers 429 without a `Retry-After`. */
    val DEFAULT_RATE_LIMIT_WAIT: Duration = Duration.ofMinutes(30)

    fun decide(state: WeatherFetchState, place: WeatherPlace, now: Instant): RefreshDecision {
        val blockedUntil = state.blockedUntil?.takeIf { it.isAfter(now) }
        val expiresAt = state.expiresAt?.takeIf { it.isAfter(now) }
        val nextAttemptAt = state.lastAttemptAt?.plus(MIN_ATTEMPT_INTERVAL)?.takeIf { it.isAfter(now) }
        return when {
            blockedUntil != null -> RefreshDecision.Blocked(blockedUntil)
            state.place != place -> RefreshDecision.Go
            expiresAt != null -> RefreshDecision.UpToDate(expiresAt)
            nextAttemptAt != null -> RefreshDecision.TooSoon(nextAttemptAt)
            else -> RefreshDecision.Go
        }
    }
}
