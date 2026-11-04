package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.WidgetPlacementDao
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.encode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Wraps [WidgetPlacementDao] — grid placement rows only. Kept separate from [com.facetlauncher.app.data.widget.AppWidgetRepository],
 * which wraps `AppWidgetManager`/`AppWidgetHost` itself, mirroring this codebase's existing
 * split-by-Android-surface convention (e.g. [CalendarRepository] vs [CalendarPermissionRepository]).
 */
@Singleton
class WidgetPlacementRepository @Inject constructor(private val widgetPlacementDao: WidgetPlacementDao) {

    fun observeAll(): Flow<List<WidgetPlacementEntity>> = widgetPlacementDao.observeAll()

    suspend fun getById(appWidgetId: Int): WidgetPlacementEntity? = widgetPlacementDao.getById(appWidgetId)

    suspend fun upsert(placement: WidgetPlacementEntity) = widgetPlacementDao.upsert(placement)

    /** The id for the next widget Facet draws itself: one below the lowest stored, and always negative so it never meets a host-issued id. */
    suspend fun nextNativeId(): Int = minOf(widgetPlacementDao.minAppWidgetId() ?: 0, 0) - 1

    /** Removes every widget Facet draws itself, before a backup restore puts the saved ones back. Hosted widgets stay. */
    suspend fun deleteAllNative() = widgetPlacementDao.deleteAllNative()

    suspend fun setConfig(appWidgetId: Int, config: NativeWidgetConfig) = widgetPlacementDao.updateConfig(appWidgetId, config.encode())

    suspend fun deleteById(appWidgetId: Int) = widgetPlacementDao.deleteById(appWidgetId)
}
