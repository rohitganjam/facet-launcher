package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.WidgetPlacementDao
import com.facetlauncher.app.data.local.WidgetPlacementEntity
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

    suspend fun deleteById(appWidgetId: Int) = widgetPlacementDao.deleteById(appWidgetId)
}
