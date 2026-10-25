package com.facetlauncher.app.data.weather

import androidx.room.withTransaction
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.WeatherDao
import com.facetlauncher.app.data.local.WeatherHourEntity
import com.facetlauncher.app.data.local.WeatherMetaEntity
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [WeatherStore] over Room: the hourly rows in `weather_hours`, the fetch state in the single `weather_meta` row. */
@Singleton
class RoomWeatherStore @Inject constructor(
    private val database: FacetDatabase,
    private val dao: WeatherDao,
) : WeatherStore {

    override fun observeHours(): Flow<List<WeatherHour>> = dao.observeHours().map { rows -> rows.map { it.toHour() } }

    override fun observeState(): Flow<WeatherFetchState> = dao.observeMeta().map { it.toState() }

    override suspend fun readHours(): List<WeatherHour> = dao.getHours().map { it.toHour() }

    override suspend fun readState(): WeatherFetchState = dao.getMeta().toState()

    override suspend fun replaceHours(hours: List<WeatherHour>) {
        database.withTransaction {
            dao.clearHours()
            dao.insertHours(hours.map { it.toEntity() })
        }
    }

    override suspend fun writeState(state: WeatherFetchState) {
        dao.upsertMeta(
            WeatherMetaEntity(
                latitude = state.place?.latitude,
                longitude = state.place?.longitude,
                altitudeM = state.place?.altitudeM,
                fetchedAtEpochSecond = state.fetchedAt?.epochSecond,
                expiresAtEpochSecond = state.expiresAt?.epochSecond,
                lastModified = state.lastModified,
                lastAttemptAtEpochSecond = state.lastAttemptAt?.epochSecond,
                blockedUntilEpochSecond = state.blockedUntil?.epochSecond,
                placeName = state.placeName,
                placeNameSource = state.placeNameSource?.name,
            ),
        )
    }
}

internal fun WeatherHour.toEntity() = WeatherHourEntity(
    timeEpochSecond = time.epochSecond,
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    pressureHpa = pressureHpa,
    cloudPercent = cloudPercent,
    windSpeedMps = windSpeedMps,
    windFromDegrees = windFromDegrees,
    precipitationMm = precipitationMm,
    symbolCode = symbolCode,
)

internal fun WeatherHourEntity.toHour() = WeatherHour(
    time = Instant.ofEpochSecond(timeEpochSecond),
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    pressureHpa = pressureHpa,
    cloudPercent = cloudPercent,
    windSpeedMps = windSpeedMps,
    windFromDegrees = windFromDegrees,
    precipitationMm = precipitationMm,
    symbolCode = symbolCode,
)

internal fun WeatherMetaEntity?.toState(): WeatherFetchState {
    if (this == null) return WeatherFetchState()
    val place = if (latitude != null && longitude != null) WeatherPlace(latitude, longitude, altitudeM) else null
    return WeatherFetchState(
        place = place,
        fetchedAt = fetchedAtEpochSecond?.let(Instant::ofEpochSecond),
        expiresAt = expiresAtEpochSecond?.let(Instant::ofEpochSecond),
        lastModified = lastModified,
        lastAttemptAt = lastAttemptAtEpochSecond?.let(Instant::ofEpochSecond),
        blockedUntil = blockedUntilEpochSecond?.let(Instant::ofEpochSecond),
        placeName = placeName,
        placeNameSource = placeNameSource?.let { name -> PlaceNameSource.entries.firstOrNull { it.name == name } },
    )
}
