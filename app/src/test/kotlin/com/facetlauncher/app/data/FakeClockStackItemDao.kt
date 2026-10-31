package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.ClockStackItemDao
import com.facetlauncher.app.data.local.ClockStackItemEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockStackItemKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** An in-memory [ClockStackItemDao] for tests that only need [FacetRepository] to have one. */
class FakeClockStackItemDao(initial: List<ClockStackItemEntity> = emptyList()) : ClockStackItemDao {
    private val rows = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    private fun List<ClockStackItemEntity>.ordered() = sortedWith(compareBy({ it.facetId }, { it.position }, { it.id }))

    private fun change(id: Long, transform: (ClockStackItemEntity) -> ClockStackItemEntity) {
        rows.value = rows.value.map { if (it.id == id) transform(it) else it }
    }

    override fun observe(facetId: Long): Flow<List<ClockStackItemEntity>> =
        rows.map { all -> all.filter { it.facetId == facetId }.ordered() }

    override fun observeAll(): Flow<List<ClockStackItemEntity>> = rows.map { it.ordered() }

    override suspend fun getAll(facetId: Long): List<ClockStackItemEntity> = rows.value.filter { it.facetId == facetId }.ordered()

    override suspend fun getById(id: Long): ClockStackItemEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun insert(item: ClockStackItemEntity): Long {
        val stored = item.copy(id = nextId++)
        rows.value = rows.value + stored
        return stored.id
    }

    override suspend fun delete(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll(facetId: Long) {
        rows.value = rows.value.filterNot { it.facetId == facetId }
    }

    override suspend fun setPosition(id: Long, position: Int) = change(id) { it.copy(position = position) }

    override suspend fun replace(id: Long, kind: ClockStackItemKind, appWidgetId: Int?) = change(id) {
        it.copy(kind = kind, appWidgetId = appWidgetId, widthDp = null, heightDp = null, landscapeWidthDp = null, landscapeHeightDp = null)
    }

    override suspend fun setSize(id: Long, widthDp: Int, heightDp: Int) = change(id) { it.copy(widthDp = widthDp, heightDp = heightDp) }

    override suspend fun setLandscapeSize(id: Long, widthDp: Int, heightDp: Int) =
        change(id) { it.copy(landscapeWidthDp = widthDp, landscapeHeightDp = heightDp) }

    override suspend fun setAlignment(id: Long, alignment: ClockAlignment?) = change(id) { it.copy(alignment = alignment) }

    override suspend fun setLandscapeAlignment(id: Long, alignment: ClockAlignment?) = change(id) { it.copy(landscapeAlignment = alignment) }
}
