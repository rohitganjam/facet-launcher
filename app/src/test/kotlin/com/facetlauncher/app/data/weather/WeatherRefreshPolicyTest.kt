package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRefreshPolicyTest {

    private val now = Instant.parse("2026-10-24T10:00:00Z")
    private val place = WeatherPlace(12.97, 77.59)
    private val fresh = WeatherFetchState(place = place)

    private fun decide(state: WeatherFetchState, at: WeatherPlace = place) = WeatherRefreshPolicy.decide(state, at, now)

    @Test
    fun `nothing stored yet means go`() {
        assertEquals(RefreshDecision.Go, decide(WeatherFetchState()))
    }

    @Test
    fun `before Expires nothing new can exist`() {
        val expires = now.plusSeconds(600)

        assertEquals(RefreshDecision.UpToDate(expires), decide(fresh.copy(expiresAt = expires)))
    }

    @Test
    fun `after Expires but within ten minutes of the last attempt is too soon`() {
        val last = now.minusSeconds(120)

        assertEquals(
            RefreshDecision.TooSoon(last.plus(WeatherRefreshPolicy.MIN_ATTEMPT_INTERVAL)),
            decide(fresh.copy(expiresAt = now.minusSeconds(60), lastAttemptAt = last)),
        )
    }

    @Test
    fun `after Expires and ten minutes since the last attempt is go`() {
        val state = fresh.copy(expiresAt = now.minusSeconds(60), lastAttemptAt = now.minusSeconds(601))

        assertEquals(RefreshDecision.Go, decide(state))
    }

    @Test
    fun `a failed attempt counts too, so a failure is not retried at once`() {
        val state = fresh.copy(expiresAt = null, lastAttemptAt = now.minusSeconds(30))

        assertEquals(RefreshDecision.TooSoon(now.plusSeconds(570)), decide(state))
    }

    @Test
    fun `a rate limit blocks everything until it ends`() {
        val until = now.plusSeconds(1800)
        val state = fresh.copy(expiresAt = now.minusSeconds(60), blockedUntil = until)

        assertEquals(RefreshDecision.Blocked(until), decide(state))
    }

    @Test
    fun `a rate limit that has ended no longer blocks`() {
        val state = fresh.copy(blockedUntil = now.minusSeconds(1))

        assertEquals(RefreshDecision.Go, decide(state))
    }

    @Test
    fun `a different place skips Expires and the attempt interval`() {
        val state = fresh.copy(expiresAt = now.plusSeconds(3600), lastAttemptAt = now.minusSeconds(5))

        assertEquals(RefreshDecision.Go, decide(state, at = WeatherPlace(18.52, 73.86)))
    }

    @Test
    fun `a different place is still held back by a rate limit`() {
        val until = now.plusSeconds(600)

        assertEquals(RefreshDecision.Blocked(until), decide(fresh.copy(blockedUntil = until), at = WeatherPlace(18.52, 73.86)))
    }
}
