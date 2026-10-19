package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LandscapeHubUseCaseTest {

    private val useCase = LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase())

    private fun widget(id: Int, row: Int = 0, col: Int = 0, colSpan: Int = 1, rowSpan: Int = 1, landscapeOrder: Int? = null) = HubWidgetState(
        appWidgetId = id, row = row, col = col, colSpan = colSpan, rowSpan = rowSpan, providerLabel = null, isOrphaned = false, landscapeOrder = landscapeOrder,
    )

    @Test
    fun `an install with no orders lays widgets out in portrait reading order down the columns`() {
        // Given three widgets placed one per portrait row
        val widgets = listOf(widget(1, row = 0), widget(2, row = 1), widget(3, row = 2))

        // When laid out in two rows
        val packed = useCase.layout(widgets, rows = 2)

        // Then widgets 1 and 2 fill column 0 and widget 3 starts column 1
        assertEquals(listOf(Triple(1, 0, 0), Triple(2, 1, 0), Triple(3, 0, 1)), packed.map { Triple(it.id, it.row, it.col) })
    }

    @Test
    fun `saved orders decide the layout instead of portrait positions`() {
        val widgets = listOf(widget(1, row = 0, landscapeOrder = 2), widget(2, row = 1, landscapeOrder = 0), widget(3, row = 2, landscapeOrder = 1))

        val packed = useCase.layout(widgets, rows = 3)

        assertEquals(listOf(2, 3, 1), packed.map { it.id })
    }

    @Test
    fun `sizes are kept and a widget taller than the rows is clamped`() {
        val packed = useCase.layout(listOf(widget(1, colSpan = 2, rowSpan = 6)), rows = 3)

        assertEquals(2, packed.single().colSpan)
        assertEquals(3, packed.single().rowSpan)
    }

    @Test
    fun `dropping a widget in a later column moves it after the others`() {
        // Given widgets 1 to 3 in column 0 and 4 in column 1 (three rows)
        val widgets = (1..4).map { widget(it, row = it - 1) }

        // When widget 1 is dropped in column 2
        val orders = useCase.ordersAfterDrop(widgets, rows = 3, movedId = 1, dropRow = 0, dropCol = 2)

        // Then it is last, and everyone has an order
        assertEquals(mapOf(2 to 0, 3 to 1, 4 to 2, 1 to 3), orders)
    }

    @Test
    fun `dropping a widget onto another's cell puts it just before that widget`() {
        val widgets = (1..3).map { widget(it, row = it - 1) }

        // When widget 3 is dropped on widget 1's cell (row 0, column 0)
        val orders = useCase.ordersAfterDrop(widgets, rows = 3, movedId = 3, dropRow = 0, dropCol = 0)

        assertEquals(mapOf(3 to 0, 1 to 1, 2 to 2), orders)
    }

    @Test
    fun `dropping an unknown widget changes nothing`() {
        assertEquals(emptyMap<Int, Int>(), useCase.ordersAfterDrop(listOf(widget(1)), rows = 3, movedId = 99, dropRow = 0, dropCol = 0))
    }
}
