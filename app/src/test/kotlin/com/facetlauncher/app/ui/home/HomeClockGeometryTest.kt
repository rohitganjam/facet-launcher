package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.facetlauncher.app.data.model.ClockAlignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeClockGeometryTest {

    private val metrics = ClockMetrics(
        topInsetPx = 40f,
        minGapPx = 10f,
        topReservePx = 100f,
        sideMarginPx = 20f,
        handleHeadroomPx = 60f,
        landscapeMarginsPx = 30f,
    )

    private fun inputs(
        landscape: Boolean = false,
        adjusting: Boolean = false,
        alignment: ClockAlignment = ClockAlignment.CENTER,
        scale: Float = 1f,
        liveScale: Float? = null,
        zoneHeightPx: Float? = 400f,
        liveHandlePx: Float? = null,
        liveDividerWidthPx: Float? = null,
        widget: ClockWidgetMeasure? = null,
        boxSize: IntSize? = IntSize(200, 100),
        boxOriginInRoot: Offset? = Offset(100f, 300f),
        rootWidthPx: Int? = 400,
        contentHeightPx: Float = 1000f,
        landscapeContentWidthPx: Float = 0f,
        blockBottomInRoot: Float? = null,
    ) = ClockGeometryInputs(
        landscape = landscape,
        adjusting = adjusting,
        alignment = alignment,
        scale = scale,
        liveScale = liveScale,
        zoneHeightPx = zoneHeightPx,
        liveHandlePx = liveHandlePx,
        liveDividerWidthPx = liveDividerWidthPx,
        widget = widget,
        boxSize = boxSize,
        boxOriginInRoot = boxOriginInRoot,
        rootOriginInRoot = Offset.Zero,
        rootWidthPx = rootWidthPx,
        clockNaturalHeightPx = 100f,
        contentHeightPx = contentHeightPx,
        landscapeContentWidthPx = landscapeContentWidthPx,
        blockBottomInRoot = blockBottomInRoot,
        metrics = metrics,
    )

    private fun widget(saved: Offset? = Offset(150f, 150f), liveW: Float? = null, liveH: Float? = null) =
        ClockWidgetMeasure(saved = saved, liveWidthPx = liveW, liveHeightPx = liveH, minWidthPx = 50f, minHeightPx = 40f)

    private fun fit(
        alignment: ClockAlignment = ClockAlignment.CENTER,
        blockBottomInRoot: Float? = null,
    ) = ClockFit(alignment, topReservePx = 100f, sideMarginPx = 20f, targetBottomPx = 500f, blockBottomInRoot = blockBottomInRoot, rootOriginY = 0f)

    // --- portraitHandleBounds ---

    @Test
    fun `an unset zone places the handle at 40 percent of the content height`() {
        // Given no saved zone height and a 1000px content area
        val bounds = portraitHandleBounds(inputs(zoneHeightPx = null))

        // Then the base and the resting handle are at 400px
        assertEquals(400f, bounds.basePx, 0.01f)
        assertEquals(400f, bounds.handlePx, 0.01f)
    }

    @Test
    fun `an unset zone never puts the handle above the clock`() {
        // Given a content area so short that 40 percent would sit inside the clock
        val bounds = portraitHandleBounds(inputs(zoneHeightPx = null, contentHeightPx = 200f))

        // Then the handle is held at the floor
        assertEquals(bounds.floorPx, bounds.handlePx, 0.01f)
    }

    @Test
    fun `the live handle is clamped between the floor and half the content height`() {
        // Given a handle dragged far past the ceiling
        val bounds = portraitHandleBounds(inputs(liveHandlePx = 9_000f))

        // Then it stops at half the 1000px content
        assertEquals(500f, bounds.handlePx, 0.01f)
    }

    @Test
    fun `an unmeasured content height never puts the ceiling under the floor`() {
        // Given a content height of zero before the first layout
        val bounds = portraitHandleBounds(inputs(contentHeightPx = 0f))

        // Then the ceiling equals the floor
        assertEquals(bounds.floorPx, bounds.ceilingPx, 0.01f)
    }

    // --- effectiveClockScale ---

    @Test
    fun `landscape clamps the scale to its pane limit`() {
        // When a 2x scale meets a 1.2 landscape limit
        val scale = effectiveClockScale(inputs(landscape = true, scale = 2f), fit(), landscapeMaxScale = 1.2f)

        // Then it is clamped
        assertEquals(1.2f, scale, 0.001f)
    }

    @Test
    fun `portrait read mode ignores the fit limit`() {
        // When a 2x scale is read while not adjusting, even though the fit would allow less
        val scale = effectiveClockScale(inputs(scale = 2f), fit(), landscapeMaxScale = 1.2f)

        // Then only the global bounds apply
        assertEquals(2f, scale, 0.001f)
    }

    @Test
    fun `portrait adjusting clamps to what fits the measured box`() {
        // Given a 200px box centred in a 400px root with 20px side margins: the width allows 1.8
        val scale = effectiveClockScale(inputs(adjusting = true, scale = 2f), fit(), landscapeMaxScale = 1.2f)

        // Then the scale is held to the fit
        assertEquals(1.8f, scale, 0.001f)
    }

    @Test
    fun `a live scale wins over the saved one`() {
        // When a drag holds 1.5 while 1 is saved
        val scale = effectiveClockScale(inputs(scale = 1f, liveScale = 1.5f), fit(), landscapeMaxScale = 2f)

        // Then the live value renders
        assertEquals(1.5f, scale, 0.001f)
    }

    // --- effectiveWidgetSize ---

    @Test
    fun `no hosted widget means no widget size`() {
        assertNull(effectiveWidgetSize(inputs(), fit(), Size(500f, 500f)))
    }

    @Test
    fun `a widget with no saved size has no size yet`() {
        assertNull(effectiveWidgetSize(inputs(widget = widget(saved = null)), fit(), Size(500f, 500f)))
    }

    @Test
    fun `landscape clamps a widget to the pane`() {
        // Given a saved 150x150 widget and a 100x80 pane limit
        val size = effectiveWidgetSize(inputs(landscape = true, widget = widget()), fit(), Size(100f, 80f))

        // Then it is held to the pane
        assertEquals(Offset(100f, 80f), size)
    }

    @Test
    fun `portrait clamps a widget to the room between the margins whatever its box measures`() {
        // Given a 400px root with 20px margins, a 500px target bottom and a 100px top reserve: 360 x 400 of room
        val size = effectiveWidgetSize(inputs(widget = widget(saved = Offset(900f, 900f))), fit(), Size(0f, 0f))
        val moved = effectiveWidgetSize(inputs(widget = widget(saved = Offset(900f, 900f)), boxOriginInRoot = Offset(103f, 340f)), fit(), Size(0f, 0f))

        // Then it is held to that room, and a box that shifts a few pixels does not change it
        assertEquals(Offset(360f, 400f), size)
        assertEquals(size, moved)
    }

    @Test
    fun `an unmeasured box only enforces the widget minimum`() {
        // Given the box has not been measured yet
        val size = effectiveWidgetSize(inputs(boxSize = null, widget = widget(saved = Offset(10f, 10f))), fit(), Size(0f, 0f))

        // Then the saved size is raised to the minimum, not clamped down
        assertEquals(Offset(50f, 40f), size)
    }

    @Test
    fun `a live drag size wins over the saved one`() {
        val size = effectiveWidgetSize(inputs(widget = widget(liveW = 120f, liveH = 90f)), fit(), Size(0f, 0f))

        assertEquals(Offset(120f, 90f), size)
    }

    // --- ClockFit ---

    @Test
    fun `maxScale is capped by the width for a centred clock`() {
        // Given a 200px clock in a 300px root with 20px margins: 260 / 200
        val max = fit().maxScale(IntSize(200, 100), Offset(50f, 500f), rootW = 300)

        assertEquals(1.3f, max, 0.001f)
    }

    @Test
    fun `maxScale for a left clock counts from its left edge`() {
        // Given the clock starts at x = 50 in a 300px root: (300 - 20 - 50) / 200
        val max = fit(ClockAlignment.LEFT).maxScale(IntSize(200, 100), Offset(50f, 500f), rootW = 300)

        assertEquals(1.15f, max, 0.001f)
    }

    @Test
    fun `maxScale for a right clock counts from its right edge`() {
        // Given a clock spanning x 50..250: (50 + 200 - 20) / 200
        val max = fit(ClockAlignment.RIGHT).maxScale(IntSize(200, 100), Offset(50f, 500f), rootW = 300)

        assertEquals(1.15f, max, 0.001f)
    }

    @Test
    fun `maxScale is capped by the room above the clock`() {
        // Given a clock near the top: (110 + 100 - 100) / 100
        val max = fit().maxScale(IntSize(200, 100), Offset(50f, 110f), rootW = 1_000)

        assertEquals(1.1f, max, 0.001f)
    }

    @Test
    fun `an empty clock box can grow to the maximum`() {
        assertEquals(HOME_CLOCK_MAX_SCALE, fit().maxScale(IntSize(0, 0), Offset.Zero, rootW = 300), 0f)
    }

    @Test
    fun `maxWidgetSize uses the target bottom less the tail under the box not the box's stale bottom`() {
        // Given a box at y 300..400 whose block ends at 450, so 50px sits under it, and a target bottom of 500
        val max = fit(blockBottomInRoot = 450f).maxWidgetSize(IntSize(200, 100), Offset(100f, 300f), rootW = 400)

        // Then the box bottom is 500 - 50 = 450 and the height 450 - 100 = 350, whatever the box measured
        assertEquals(350f, max.y, 0.01f)
    }

    @Test
    fun `maxWidgetSize falls back to the measured box bottom before the block is measured`() {
        val max = fit().maxWidgetSize(IntSize(200, 100), Offset(100f, 300f), rootW = 400)

        // Then the bottom is 300 + 100 and the height 400 - 100
        assertEquals(300f, max.y, 0.01f)
    }

    // --- landscape ---

    @Test
    fun `landscape origin follows the alignment`() {
        assertEquals(0f, landscapeLimits(inputs(alignment = ClockAlignment.LEFT)).originY, 0f)
        assertEquals(0.5f, landscapeLimits(inputs(alignment = ClockAlignment.CENTER)).originY, 0f)
        assertEquals(1f, landscapeLimits(inputs(alignment = ClockAlignment.RIGHT)).originY, 0f)
    }

    @Test
    fun `landscape content width falls back to the root width until measured`() {
        assertEquals(400f, landscapeLimits(inputs(landscapeContentWidthPx = 0f)).contentWidthPx, 0f)
        assertEquals(300f, landscapeLimits(inputs(landscapeContentWidthPx = 300f)).contentWidthPx, 0f)
    }

    @Test
    fun `the landscape pane is at least the margins plus the clock as drawn`() {
        // Given a saved pane width far narrower than a 200px clock at scale 1 with 30px margins
        val i = inputs(landscape = true, zoneHeightPx = 10f, landscapeContentWidthPx = 1_000f)
        val limits = landscapeLimits(i)

        // When the pane is sized to the native clock
        val width = landscapePaneWidth(i, limits, effectiveScale = 1f, effectiveWidget = null)

        // Then it is 30 + 200
        assertEquals(230f, width, 0.01f)
    }

    @Test
    fun `a hosted widget sizes the landscape pane by its own width`() {
        val i = inputs(landscape = true, zoneHeightPx = 10f, landscapeContentWidthPx = 1_000f, widget = widget())
        val limits = landscapeLimits(i)

        val width = landscapePaneWidth(i, limits, effectiveScale = 1f, effectiveWidget = Offset(120f, 100f))

        assertEquals(150f, width, 0.01f)
    }

    @Test
    fun `computeClockGeometry ties the parts together`() {
        // Given a portrait hosted widget
        val geometry = computeClockGeometry(inputs(widget = widget(saved = Offset(100f, 100f))))

        // Then the fit's target bottom is top inset + handle - gap and the widget keeps its saved size
        assertEquals(40f + geometry.handle.handlePx - 10f, geometry.fit.targetBottomPx, 0.01f)
        assertEquals(Offset(100f, 100f), geometry.effectiveWidgetSizePx)
    }

    // --- widgetRoom ---

    @Test
    fun `a widget has the width between the margins and the height down to the target bottom whichever page it is`() {
        // Given a 400px wide root, 20px margins, a 100px top reserve and a 500px target bottom
        val room = widgetRoom(inputs(), fit(), Size.Zero)

        // Then the room does not depend on any box
        assertEquals(Offset(360f, 400f), room)
        assertEquals(room, widgetRoom(inputs(boxSize = null, boxOriginInRoot = null), fit(ClockAlignment.LEFT), Size.Zero))
    }

    @Test
    fun `the room is unknown before the root is measured and landscape's own pane when landscape`() {
        assertNull(widgetRoom(inputs(rootWidthPx = null), fit(), Size.Zero))
        assertEquals(Offset(300f, 700f), widgetRoom(inputs(landscape = true), fit(), Size(300f, 700f)))
        assertNull(widgetRoom(inputs(landscape = true), fit(), Size(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
    }
}
