package com.facetlauncher.app.data

import androidx.room.withTransaction
import com.facetlauncher.app.data.local.ClockStackItemDao
import com.facetlauncher.app.data.local.ClockStackItemEntity
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.toModel
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A facet's clock stack: the native clock and hosted widgets, in swipe order. Rows only: the caller
 * owns the `AppWidgetHost` allocate/bind/configure round trip and releases the ids that [remove] and
 * [clear] hand back. A facet with no rows has the plain native clock.
 */
@Singleton
class ClockStackRepository @Inject constructor(
    private val database: FacetDatabase,
    private val dao: ClockStackItemDao,
) {
    fun observe(facetId: Long): Flow<List<ClockStackItem>> = dao.observe(facetId).map { rows -> rows.map { it.toModel() } }

    suspend fun getAll(facetId: Long): List<ClockStackItem> = dao.getAll(facetId).map { it.toModel() }

    /** Appends a widget at the end. `false`, and nothing written, when the stack is full. */
    suspend fun addWidget(facetId: Long, appWidgetId: Int): Boolean = append(facetId) {
        ClockStackItemEntity(facetId = facetId, position = it, kind = ClockStackItemKind.WIDGET, appWidgetId = appWidgetId)
    }

    /** Appends the native clock. `false`, and nothing written, when the stack is full or already has the clock. */
    suspend fun addClock(facetId: Long): Boolean = database.withTransaction {
        if (dao.getAll(facetId).any { it.kind == ClockStackItemKind.CLOCK }) return@withTransaction false
        append(facetId) { ClockStackItemEntity(facetId = facetId, position = it, kind = ClockStackItemKind.CLOCK) }
    }

    /** Replaces the whole stack with one widget, as the free single custom widget does; returns the previous widget ids for the caller to release. */
    suspend fun replaceWithWidget(facetId: Long, appWidgetId: Int): List<Int> = database.withTransaction {
        val released = dao.getAll(facetId).mapNotNull { it.appWidgetId }
        dao.deleteAll(facetId)
        dao.insert(ClockStackItemEntity(facetId = facetId, position = 0, kind = ClockStackItemKind.WIDGET, appWidgetId = appWidgetId))
        released
    }

    /** Removes the item and closes the gap; returns its widget id for the caller to release, or `null` for the clock or an unknown id. */
    suspend fun remove(itemId: Long): Int? = database.withTransaction {
        val item = dao.getById(itemId) ?: return@withTransaction null
        dao.delete(itemId)
        renumber(item.facetId)
        item.appWidgetId
    }

    /** Removes every item; returns the widget ids to release. */
    suspend fun clear(facetId: Long): List<Int> = database.withTransaction {
        val ids = dao.getAll(facetId).mapNotNull { it.appWidgetId }
        dao.deleteAll(facetId)
        ids
    }

    /** Applies [orderedItemIds] as the new order; ids not in the facet's stack are ignored and items left out keep their relative order at the end. */
    suspend fun reorder(facetId: Long, orderedItemIds: List<Long>) = database.withTransaction {
        val rows = dao.getAll(facetId)
        val byId = rows.associateBy { it.id }
        val ordered = orderedItemIds.mapNotNull { byId[it] }.distinct()
        (ordered + rows.filterNot { it in ordered }).forEachIndexed { index, row -> dao.setPosition(row.id, index) }
    }

    suspend fun setSize(itemId: Long, widthDp: Int, heightDp: Int) = dao.setSize(itemId, widthDp, heightDp)

    suspend fun setLandscapeSize(itemId: Long, widthDp: Int, heightDp: Int) = dao.setLandscapeSize(itemId, widthDp, heightDp)

    suspend fun setAlignment(itemId: Long, alignment: ClockAlignment?) = dao.setAlignment(itemId, alignment)

    suspend fun setLandscapeAlignment(itemId: Long, alignment: ClockAlignment?) = dao.setLandscapeAlignment(itemId, alignment)

    private suspend fun append(facetId: Long, row: (position: Int) -> ClockStackItemEntity): Boolean = database.withTransaction {
        val count = dao.getAll(facetId).size
        if (count >= MAX_CLOCK_STACK_ITEMS) return@withTransaction false
        dao.insert(row(count))
        true
    }

    private suspend fun renumber(facetId: Long) {
        dao.getAll(facetId).forEachIndexed { index, row -> if (row.position != index) dao.setPosition(row.id, index) }
    }
}
