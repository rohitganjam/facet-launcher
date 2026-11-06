package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.data.model.ClockAlignment
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFrameBoxScaleTest {

    private val density = Density(2f)
    private val natural = IntSize(width = 400, height = 160)

    private fun state(boxWidthDp: Int?) = HomeScreenState(
        list = HomeListState(items = emptyList()),
        dock = HomeDockState(items = emptyList()),
        clock = HomeClockState(placement = ClockPlacement(ClockAlignment.LEFT, zoneHeightDp = null, scale = 0.8f), boxWidthDp = boxWidthDp),
    )

    @Test
    fun `a saved box draws at its width over the clock's natural width`() {
        // Given a 150dp box (300px at 2x) and a clock whose natural width is 400px
        val shown = state(boxWidthDp = 150).withBoxScale(natural, density)

        // Then the clock draws at 300 / 400
        assertEquals(0.75f, shown.clock.placement.scale, 0.0001f)
    }

    @Test
    fun `a new look with a different natural width keeps the box's width`() {
        // Given the same 150dp box and a clock whose natural width has doubled
        val shown = state(boxWidthDp = 150).withBoxScale(IntSize(800, 320), density)

        // Then the scale halves so the drawn width stays the same
        assertEquals(0.375f, shown.clock.placement.scale, 0.0001f)
    }

    @Test
    fun `without a box or before the clock is measured the placement's scale stands`() {
        assertEquals(0.8f, state(boxWidthDp = null).withBoxScale(natural, density).clock.placement.scale, 0f)
        assertEquals(0.8f, state(boxWidthDp = 150).withBoxScale(null, density).clock.placement.scale, 0f)
    }

    @Test
    fun `a scale commit is saved as the aspect-locked box in dp`() {
        // Given resize handles that commit a scale of 0.75 on a 400x160px clock at 2x
        var saved: Pair<Int, Int>? = null
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(onBoxSizeCommit = { w, h -> saved = w to h }),
        ).withBoxCommit(natural, density)

        // When the scale is committed
        actions.clock.onScaleCommit(0.75f)

        // Then the box is 150 x 60 dp
        assertEquals(150 to 60, saved)
    }

    @Test
    fun `before the clock is measured a scale commit is left to the old path`() {
        var scale: Float? = null
        val actions = HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            clock = HomeClockActions(onScaleCommit = { scale = it }),
        ).withBoxCommit(null, density)

        actions.clock.onScaleCommit(0.6f)

        assertEquals(0.6f, scale)
    }
}
