package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLandscapeGeometryTest {

    private val natural = Size(200f, 100f)
    private val naturalTopLeft = Offset(24f, 50f)

    @Test
    fun `an unsaved pane width is a share of the window`() {
        assertEquals(400f, landscapeClockPaneWidthPx(persistedPx = null, contentWidthPx = 1000f, floorPx = 100f), 0.01f)
    }

    @Test
    fun `a saved pane width is kept`() {
        assertEquals(300f, landscapeClockPaneWidthPx(persistedPx = 300f, contentWidthPx = 1000f, floorPx = 100f), 0.01f)
    }

    @Test
    fun `the pane never goes below the clock and its margins`() {
        assertEquals(260f, landscapeClockPaneWidthPx(persistedPx = 100f, contentWidthPx = 1000f, floorPx = 260f), 0.01f)
    }

    @Test
    fun `the pane never takes more than half the window`() {
        assertEquals(500f, landscapeClockPaneWidthPx(persistedPx = 900f, contentWidthPx = 1000f, floorPx = 100f), 0.01f)
    }

    @Test
    fun `a clock wider than half the window still gets its floor`() {
        assertEquals(700f, landscapeClockPaneWidthPx(persistedPx = 900f, contentWidthPx = 1000f, floorPx = 700f), 0.01f)
    }

    @Test
    fun `a divider drag step moves the pane by the drag amount`() {
        assertEquals(350f, landscapeDividerDragWidthPx(currentPx = 300f, deltaPx = 50f, floorPx = 200f, contentWidthPx = 1000f), 0.01f)
        assertEquals(250f, landscapeDividerDragWidthPx(currentPx = 300f, deltaPx = -50f, floorPx = 200f, contentWidthPx = 1000f), 0.01f)
    }

    @Test
    fun `a divider drag stops at the smallest clock plus its margins`() {
        assertEquals(200f, landscapeDividerDragWidthPx(currentPx = 220f, deltaPx = -500f, floorPx = 200f, contentWidthPx = 1000f), 0.01f)
    }

    @Test
    fun `a divider drag past the floor comes back as soon as the finger reverses`() {
        // Given a drag that overshot the floor, then moved back by 30px
        val atFloor = landscapeDividerDragWidthPx(currentPx = 220f, deltaPx = -500f, floorPx = 200f, contentWidthPx = 1000f)
        val reversed = landscapeDividerDragWidthPx(currentPx = atFloor, deltaPx = 30f, floorPx = 200f, contentWidthPx = 1000f)

        // Then the pane follows the finger straight away instead of sitting in a dead zone
        assertEquals(230f, reversed, 0.01f)
    }

    @Test
    fun `a divider drag stops at half the window`() {
        assertEquals(500f, landscapeDividerDragWidthPx(currentPx = 480f, deltaPx = 400f, floorPx = 200f, contentWidthPx = 1000f), 0.01f)
    }

    @Test
    fun `max scale is limited by the pane width`() {
        // Given a 440px pane with 40px of margins around a 200px wide clock
        assertEquals(2f, landscapeMaxClockScale(natural, paneWidthPx = 440f, paneHeightPx = 1000f, horizontalMarginsPx = 40f, minScale = 0.5f, maxScale = 3f), 0.001f)
    }

    @Test
    fun `max scale is limited by the pane height`() {
        assertEquals(1.5f, landscapeMaxClockScale(natural, paneWidthPx = 2000f, paneHeightPx = 150f, horizontalMarginsPx = 40f, minScale = 0.5f, maxScale = 3f), 0.001f)
    }

    @Test
    fun `max scale stays inside the allowed range`() {
        assertEquals(3f, landscapeMaxClockScale(natural, 5000f, 5000f, 40f, 0.5f, 3f), 0.001f)
        assertEquals(0.5f, landscapeMaxClockScale(natural, 100f, 5000f, 40f, 0.5f, 3f), 0.001f)
    }

    @Test
    fun `an unmeasured clock allows the maximum`() {
        assertEquals(2f, landscapeMaxClockScale(Size.Zero, 400f, 300f, 40f, 0.5f, 2f), 0.001f)
    }

    @Test
    fun `a widget may be as wide as the pane's inner width and as tall as the pane`() {
        val max = landscapeMaxWidgetSize(paneWidthPx = 440f, paneHeightPx = 300f, horizontalMarginsPx = 40f)

        assertEquals(Size(400f, 300f), max)
    }

    @Test
    fun `an unmeasured pane height puts no limit on the widget's height`() {
        assertEquals(Float.POSITIVE_INFINITY, landscapeMaxWidgetSize(paneWidthPx = 440f, paneHeightPx = 0f, horizontalMarginsPx = 40f).height, 0f)
    }

    @Test
    fun `a pane narrower than its margins allows no widget width`() {
        assertEquals(0f, landscapeMaxWidgetSize(paneWidthPx = 30f, paneHeightPx = 300f, horizontalMarginsPx = 40f).width, 0.001f)
    }

    @Test
    fun `dragging the bottom right corner of a top anchored widget gives the size below and right of its top left`() {
        // Given a widget box at (24, 50), 200 x 100, anchored at the top
        val size = landscapeWidgetSizeAtTouch(Offset(324f, 250f), naturalTopLeft, natural, originY = 0f)

        // Then the corner at (324, 250) means 300 wide and 200 tall
        assertEquals(Size(300f, 200f), size)
    }

    @Test
    fun `dragging the top right corner of a bottom anchored widget measures up from its bottom edge`() {
        // The box's bottom is at 150, so a corner at y = 30 means 120 tall
        val size = landscapeWidgetSizeAtTouch(Offset(124f, 30f), naturalTopLeft, natural, originY = 1f)

        assertEquals(Size(100f, 120f), size)
    }

    @Test
    fun `dragging the top right corner of a middle anchored widget grows it around its centre`() {
        // The centre is at y = 100, so a corner 40 above it means 80 tall
        val size = landscapeWidgetSizeAtTouch(Offset(124f, 60f), naturalTopLeft, natural, originY = 0.5f)

        assertEquals(Size(100f, 80f), size)
    }

    @Test
    fun `a top aligned clock scales down and to the right from its top left corner`() {
        val rect = landscapeVisualClockRect(naturalTopLeft, natural, originY = 0f, scale = 0.5f)

        assertEquals(24f, rect.left, 0.01f)
        assertEquals(50f, rect.top, 0.01f)
        assertEquals(100f, rect.width, 0.01f)
        assertEquals(50f, rect.height, 0.01f)
    }

    @Test
    fun `a bottom aligned clock keeps its bottom edge`() {
        val rect = landscapeVisualClockRect(naturalTopLeft, natural, originY = 1f, scale = 0.5f)

        assertEquals(150f, rect.bottom, 0.01f)
        assertEquals(100f, rect.top, 0.01f)
    }

    @Test
    fun `a middle aligned clock keeps its centre`() {
        val rect = landscapeVisualClockRect(naturalTopLeft, natural, originY = 0.5f, scale = 0.5f)

        assertEquals(100f, rect.center.y, 0.01f)
    }

    @Test
    fun `dragging the bottom right corner of a top aligned clock gives the matching scale`() {
        // Given the corner of a clock scaled to 0.5 (100 x 50 at 24, 50)
        val scale = landscapeScaleAtTouch(Offset(124f, 100f), naturalTopLeft, natural, originY = 0f)

        assertEquals(0.5f, scale, 0.001f)
    }

    @Test
    fun `dragging the top right corner of a bottom aligned clock gives the matching scale`() {
        // Given the corner of a clock scaled to 0.5, with its bottom at 150
        val scale = landscapeScaleAtTouch(Offset(124f, 100f), naturalTopLeft, natural, originY = 1f)

        assertEquals(0.5f, scale, 0.001f)
    }

    @Test
    fun `dragging the top right corner of a middle aligned clock gives the matching scale`() {
        // Given the corner of a clock scaled to 0.5 around its centre (y = 100), so its top is at 75
        val scale = landscapeScaleAtTouch(Offset(124f, 75f), naturalTopLeft, natural, originY = 0.5f)

        assertEquals(0.5f, scale, 0.001f)
    }
}
