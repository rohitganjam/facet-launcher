package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherHour
import kotlinx.coroutines.flow.Flow

/** Where [WeatherRepository] keeps the stored hourly rows and the fetch state. Room-backed in the app. */
interface WeatherStore {
    fun observeHours(): Flow<List<WeatherHour>>
    fun observeState(): Flow<WeatherFetchState>
    suspend fun readHours(): List<WeatherHour>
    suspend fun readState(): WeatherFetchState
    suspend fun replaceHours(hours: List<WeatherHour>)
    suspend fun writeState(state: WeatherFetchState)
}
