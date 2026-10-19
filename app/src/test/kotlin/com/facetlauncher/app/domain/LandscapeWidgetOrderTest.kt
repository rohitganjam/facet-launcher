package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LandscapeWidgetOrderTest {

    private val order = OrderWidgetsForLandscapeUseCase()
    private val reorder = ReorderLandscapeWidgetUseCase()

    private fun widget(id: Int, row: Int = 0, col: Int = 0, landscapeOrder: Int? = null) = HubWidgetState(
        appWidgetId = id, row = row, col = col, colSpan = 1, rowSpan = 1, providerLabel = null, isOrphaned = false, landscapeOrder = landscapeOrder,
    )

    private fun List<HubWidgetState>.ids() = map { it.appWidgetId }

    @Test
    fun `widgets without an order follow portrait reading order`() {
        val widgets = listOf(widget(1, row = 2, col = 0), widget(2, row = 0, col = 3), widget(3, row = 0, col = 1))

        assertEquals(listOf(3, 2, 1), order(widgets).ids())
    }

    @Test
    fun `ordered widgets come first lowest order first`() {
        val widgets = listOf(widget(1, landscapeOrder = 2), widget(2, landscapeOrder = 0), widget(3, landscapeOrder = 1))

        assertEquals(listOf(2, 3, 1), order(widgets).ids())
    }

    @Test
    fun `a widget added later without an order goes after the ordered ones`() {
        val widgets = listOf(widget(1, row = 0, landscapeOrder = 1), widget(2, row = 0, landscapeOrder = 0), widget(9, row = 0))

        assertEquals(listOf(2, 1, 9), order(widgets).ids())
    }

    @Test
    fun `moving a widget renumbers everyone with the moved one at the requested place`() {
        // Given four widgets in order 1, 2, 3, 4
        val ordered = (1..4).map { widget(it, landscapeOrder = it - 1) }

        // When widget 1 is moved after two of the others
        val orders = reorder(ordered, movedId = 1, beforeCount = 2)

        // Then the order is 2, 3, 1, 4
        assertEquals(mapOf(2 to 0, 3 to 1, 1 to 2, 4 to 3), orders)
    }

    @Test
    fun `moving a widget to the front or the end clamps the place`() {
        val ordered = (1..3).map { widget(it, landscapeOrder = it - 1) }

        assertEquals(mapOf(3 to 0, 1 to 1, 2 to 2), reorder(ordered, movedId = 3, beforeCount = -5))
        assertEquals(mapOf(2 to 0, 3 to 1, 1 to 2), reorder(ordered, movedId = 1, beforeCount = 99))
    }

    @Test
    fun `an unordered install gets a full order after its first move`() {
        // Given widgets that have never been ordered, read in portrait order 1, 2, 3
        val ordered = order(listOf(widget(1, row = 0), widget(2, row = 1), widget(3, row = 2)))

        val orders = reorder(ordered, movedId = 3, beforeCount = 0)

        assertEquals(mapOf(3 to 0, 1 to 1, 2 to 2), orders)
    }

    @Test
    fun `moving an unknown widget changes nothing`() {
        assertEquals(emptyMap<Int, Int>(), reorder(listOf(widget(1)), movedId = 99, beforeCount = 0))
    }

    @Test
    fun `dropping on an empty cell after everything puts the widget last`() {
        // Given three rows and widgets packed down column 0 and into column 1
        val packed = listOf(PackedWidget(1, 0, 0, 1, 1), PackedWidget(2, 1, 0, 1, 1), PackedWidget(3, 2, 0, 1, 1), PackedWidget(4, 0, 1, 1, 1))

        // When widget 1 is dropped in column 3
        val count = landscapeBeforeCount(packed, movedId = 1, dropRow = 0, dropCol = 3, rows = 3)

        // Then all three others come before it
        assertEquals(3, count)
    }

    @Test
    fun `dropping onto another widget's cell puts the widget just before it`() {
        val packed = listOf(PackedWidget(1, 0, 0, 1, 1), PackedWidget(2, 1, 0, 1, 1), PackedWidget(3, 2, 0, 1, 1))

        // When widget 3 is dropped onto widget 2's cell (row 1, column 0)
        val count = landscapeBeforeCount(packed, movedId = 3, dropRow = 1, dropCol = 0, rows = 3)

        // Then only widget 1 is before it
        assertEquals(1, count)
    }

    @Test
    fun `dropping back on its own cell keeps its place`() {
        val packed = listOf(PackedWidget(1, 0, 0, 1, 1), PackedWidget(2, 1, 0, 1, 1), PackedWidget(3, 2, 0, 1, 1))

        assertEquals(1, landscapeBeforeCount(packed, movedId = 2, dropRow = 1, dropCol = 0, rows = 3))
    }

    @Test
    fun `columns are read before rows when comparing places`() {
        // Given a widget at the bottom of column 0 and one at the top of column 1
        val packed = listOf(PackedWidget(1, 2, 0, 1, 1), PackedWidget(2, 0, 1, 1, 1))

        // When a widget is dropped at the top of column 1 (after column 0's bottom cell)
        val count = landscapeBeforeCount(packed, movedId = 9, dropRow = 0, dropCol = 1, rows = 3)

        // Then only the column 0 widget comes first
        assertEquals(1, count)
    }
}
