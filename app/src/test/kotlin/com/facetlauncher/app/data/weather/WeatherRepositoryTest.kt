package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherForecast
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private class MutableClock(var now: Instant) : Clock() {
    override fun instant(): Instant = now
    override fun getZone() = ZoneOffset.UTC
    override fun withZone(zone: java.time.ZoneId?): Clock = this
}

private class FakeWeatherApi : WeatherApi {
    val results = ArrayDeque<WeatherApiResult>()
    val calls = mutableListOf<Pair<WeatherPlace, String?>>()
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun fetch(place: WeatherPlace, ifModifiedSince: String?): WeatherApiResult {
        calls += place to ifModifiedSince
        gate?.await()
        return results.removeFirst()
    }
}

private class InMemoryWeatherStore : WeatherStore {
    val hours = MutableStateFlow<List<WeatherHour>>(emptyList())
    val state = MutableStateFlow(WeatherFetchState())
    override fun observeHours(): Flow<List<WeatherHour>> = hours
    override fun observeState(): Flow<WeatherFetchState> = state
    override suspend fun readHours() = hours.value
    override suspend fun readState() = state.value
    override suspend fun replaceHours(hours: List<WeatherHour>) { this.hours.value = hours }
    override suspend fun writeState(state: WeatherFetchState) { this.state.value = state }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

    private val start = Instant.parse("2026-10-24T05:30:00Z")
    private val clock = MutableClock(start)
    private val api = FakeWeatherApi()
    private val store = InMemoryWeatherStore()
    private val repository = WeatherRepository(api, store, clock)
    private val place = WeatherPlace(12.97, 77.59)

    private fun hour(time: String, temperature: Double) = WeatherHour(Instant.parse(time), temperature, null, null, null, null, null, null, null)

    private fun success(vararg hours: WeatherHour, expiresAt: Instant? = start.plusSeconds(3600), lastModified: String? = "Sat, 24 Oct 2026 05:21:00 GMT") =
        WeatherApiResult.Success(WeatherForecast(null, hours.toList()), expiresAt, lastModified)

    @Test
    fun `the first refresh fetches and stores the hours and the fetch state`() = runTest {
        // Given an answer
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))

        // When refreshed
        val outcome = repository.refresh(place)

        // Then it is stored with Expires and Last-Modified
        assertEquals(RefreshOutcome.Updated, outcome)
        assertEquals(listOf(26.6), store.hours.value.map { it.temperatureC })
        assertEquals(start.plusSeconds(3600), store.state.value.expiresAt)
        assertEquals("Sat, 24 Oct 2026 05:21:00 GMT", store.state.value.lastModified)
        assertEquals(place, store.state.value.place)
    }

    @Test
    fun `a refresh before Expires makes no request`() = runTest {
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))
        repository.refresh(place)

        // When asked again ten minutes later, still before Expires
        clock.now = start.plus(Duration.ofMinutes(10))
        val outcome = repository.refresh(place)

        // Then nothing is requested
        assertEquals(RefreshOutcome.UpToDate, outcome)
        assertEquals(1, api.calls.size)
    }

    @Test
    fun `after Expires the next request is conditional on Last-Modified`() = runTest {
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))
        repository.refresh(place)
        api.results += WeatherApiResult.NotModified(expiresAt = start.plus(Duration.ofHours(2)))

        // When asked after Expires
        clock.now = start.plus(Duration.ofMinutes(61))
        val outcome = repository.refresh(place)

        // Then the request carries If-Modified-Since, and a 304 moves Expires on
        assertEquals("Sat, 24 Oct 2026 05:21:00 GMT", api.calls.last().second)
        assertEquals(RefreshOutcome.UpToDate, outcome)
        assertEquals(start.plus(Duration.ofHours(2)), store.state.value.expiresAt)
        assertEquals(listOf(26.6), store.hours.value.map { it.temperatureC })
    }

    @Test
    fun `a rate limit fails the call and blocks until Retry-After`() = runTest {
        // Given MET answers 429 with Retry-After
        val retryAt = start.plus(Duration.ofMinutes(20))
        api.results += WeatherApiResult.Failed(WeatherFailure.RATE_LIMITED, retryAt)

        // When refreshed
        val outcome = repository.refresh(place)

        // Then the user is told to try later, and nothing is requested before then
        assertEquals(RefreshOutcome.Busy(retryAt), outcome)
        clock.now = start.plus(Duration.ofMinutes(15))
        assertEquals(RefreshOutcome.Busy(retryAt), repository.refresh(place))
        assertEquals(1, api.calls.size)

        // And after it, a request is allowed again
        clock.now = retryAt.plusSeconds(1)
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))
        assertEquals(RefreshOutcome.Updated, repository.refresh(place))
    }

    @Test
    fun `a rate limit with no Retry-After waits the default`() = runTest {
        api.results += WeatherApiResult.Failed(WeatherFailure.RATE_LIMITED)

        val outcome = repository.refresh(place)

        assertEquals(RefreshOutcome.Busy(start.plus(WeatherRefreshPolicy.DEFAULT_RATE_LIMIT_WAIT)), outcome)
    }

    @Test
    fun `a failure is not retried inside the minimum interval and does not block`() = runTest {
        api.results += WeatherApiResult.Failed(WeatherFailure.OFFLINE)

        assertEquals(RefreshOutcome.Offline, repository.refresh(place))
        assertNull(store.state.value.blockedUntil)

        // When tried again a minute later
        clock.now = start.plusSeconds(60)
        val outcome = repository.refresh(place)

        // Then it is too soon, with no second request
        assertEquals(RefreshOutcome.TooSoon(start.plus(WeatherRefreshPolicy.MIN_ATTEMPT_INTERVAL)), outcome)
        assertEquals(1, api.calls.size)
    }

    @Test
    fun `a server error or a bad answer is a plain failure`() = runTest {
        api.results += WeatherApiResult.Failed(WeatherFailure.SERVER)

        assertEquals(RefreshOutcome.Failed, repository.refresh(place))
    }

    @Test
    fun `a new place clears the old hours and ignores Expires`() = runTest {
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))
        repository.refresh(place)

        // When the place changes while the old forecast is still fresh, and the new fetch fails
        api.results += WeatherApiResult.Failed(WeatherFailure.OFFLINE)
        val pune = WeatherPlace(18.52, 73.86)
        val outcome = repository.refresh(pune)

        // Then a request was made, unconditionally, and the old place's hours are gone
        assertEquals(RefreshOutcome.Offline, outcome)
        assertEquals(pune to null, api.calls.last())
        assertEquals(emptyList<WeatherHour>(), store.hours.value)
        assertEquals(pune, store.state.value.place)
    }

    @Test
    fun `earlier hours of the day survive the next fetch`() = runTest {
        // Given a morning fetch that covers 05:00 and 06:00
        api.results += success(hour("2026-10-24T05:00:00Z", 24.0), hour("2026-10-24T06:00:00Z", 25.0), expiresAt = start.plusSeconds(60))
        repository.refresh(place)

        // When an afternoon fetch starts at 09:00
        clock.now = start.plus(Duration.ofHours(4))
        api.results += success(hour("2026-10-24T09:00:00Z", 27.0), hour("2026-10-24T10:00:00Z", 28.0))
        repository.refresh(place)

        // Then the morning hours are still stored, ahead of the new ones
        assertEquals(listOf(24.0, 25.0, 27.0, 28.0), store.hours.value.map { it.temperatureC })
    }

    @Test
    fun `new points replace stored ones from their first time on`() {
        val existing = listOf(hour("2026-10-24T05:00:00Z", 24.0), hour("2026-10-24T09:00:00Z", 99.0), hour("2026-10-24T10:00:00Z", 98.0))
        val incoming = listOf(hour("2026-10-24T09:00:00Z", 27.0))

        val merged = mergeStoredHours(existing, incoming, Instant.parse("2026-10-24T10:00:00Z"))

        assertEquals(listOf(24.0, 27.0), merged.map { it.temperatureC })
    }

    @Test
    fun `rows older than two days are dropped`() {
        val existing = listOf(hour("2026-10-21T05:00:00Z", 20.0), hour("2026-10-23T05:00:00Z", 22.0))
        val incoming = listOf(hour("2026-10-24T05:00:00Z", 27.0))

        val merged = mergeStoredHours(existing, incoming, Instant.parse("2026-10-24T06:00:00Z"))

        assertEquals(listOf(22.0, 27.0), merged.map { it.temperatureC })
    }

    @Test
    fun `two refreshes at once make one request`() = runTest {
        // Given a request that is held open
        val gate = CompletableDeferred<Unit>()
        api.gate = gate
        api.results += success(hour("2026-10-24T05:00:00Z", 26.6))

        // When two callers refresh together
        val first = async { repository.refresh(place) }
        val second = async { repository.refresh(place) }
        advanceUntilIdle()
        gate.complete(Unit)

        // Then one fetched and the other found the gate closed
        assertEquals(RefreshOutcome.Updated, first.await())
        assertEquals(RefreshOutcome.UpToDate, second.await())
        assertEquals(1, api.calls.size)
    }
}
