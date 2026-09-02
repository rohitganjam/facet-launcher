package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CompactWidgetsUseCaseTest {

    private val useCase = CompactWidgetsUseCase()

    private fun placement(id: Int, row: Int, col: Int, colSpan: Int = 1, rowSpan: Int = 1) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = col,
        colSpan = colSpan,
        rowSpan = rowSpan,
    )

    private fun List<WidgetPlacementEntity>.find(id: Int) = single { it.appWidgetId == id }

    @Test
    fun `a widget with an empty row gap above it settles to the top`() {
        val result = useCase(listOf(placement(id = 1, row = 5, col = 0)))

        assertEquals(0, result.find(1).row)
    }

    @Test
    fun `a widget already flush against the top does not move`() {
        val result = useCase(listOf(placement(id = 1, row = 0, col = 0)))

        assertEquals(0, result.find(1).row)
    }

    @Test
    fun `stacked widgets with a gap between them close the gap, higher one settles first`() {
        // Widget 1 at row 3, widget 2 four rows below it with an empty row in between
        val placements = listOf(placement(id = 1, row = 3, col = 0), placement(id = 2, row = 8, col = 0))

        val result = useCase(placements)

        assertEquals(0, result.find(1).row)
        assertEquals(1, result.find(2).row)
    }

    @Test
    fun `a widget does not settle past another widget already occupying its column`() {
        // Widget 2 sits directly above where widget 1 would otherwise settle to
        val placements = listOf(placement(id = 1, row = 3, col = 0), placement(id = 2, row = 1, col = 0))

        val result = useCase(placements)

        assertEquals(0, result.find(2).row)
        assertEquals(1, result.find(1).row)
    }

    @Test
    fun `widgets in different columns settle independently`() {
        val placements = listOf(placement(id = 1, row = 5, col = 0), placement(id = 2, row = 5, col = 1))

        val result = useCase(placements)

        assertEquals(0, result.find(1).row)
        assertEquals(0, result.find(2).row)
    }

    @Test
    fun `a wide widget only settles as far as its full footprint allows`() {
        // Widget 2 occupies column 1 at row 0; widget 1 spans columns 0-1 and can't pass it
        val placements = listOf(placement(id = 1, row = 4, col = 0, colSpan = 2), placement(id = 2, row = 0, col = 1))

        val result = useCase(placements)

        assertEquals(0, result.find(2).row)
        assertEquals(1, result.find(1).row)
    }
}
