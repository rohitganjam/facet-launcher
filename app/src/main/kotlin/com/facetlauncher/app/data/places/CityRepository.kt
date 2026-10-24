package com.facetlauncher.app.data.places

import android.content.Context
import com.facetlauncher.app.data.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The bundled city list (`assets/cities.tsv`), read once on first use and kept in memory. When the asset is missing or
 * unreadable the list is empty, and every lookup answers "none", so weather still works without place names or the
 * offline picker.
 */
@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()
    private var cached: CityList? = null

    private suspend fun list(): CityList = mutex.withLock {
        cached ?: withContext(ioDispatcher) { load() }.also { cached = it }
    }

    suspend fun search(query: String, limit: Int = CityList.DEFAULT_LIMIT): List<City> = list().search(query, limit)

    suspend fun nearest(latitude: Double, longitude: Double, countryCode: String? = null, maxKm: Double? = null): City? =
        list().nearest(latitude, longitude, countryCode, maxKm)

    private fun load(): CityList = try {
        context.assets.open(ASSET).bufferedReader().useLines { CityList.parse(it) }
    } catch (_: IOException) {
        CityList(emptyList())
    }

    private companion object {
        const val ASSET = "cities.tsv"
    }
}
