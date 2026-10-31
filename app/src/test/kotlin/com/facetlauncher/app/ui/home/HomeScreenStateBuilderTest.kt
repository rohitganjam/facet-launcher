package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.widgetItem
import com.facetlauncher.app.data.clockItem
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.domain.resolveFacetStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeScreenStateBuilderTest {

    private val settings = LauncherSettings(
        activeFacetId = 1L,
        clockAlignment = ClockAlignment.RIGHT,
        clockScale = 1.2f,
        clockZoneHeightDp = 300f,
        appRowPosition = AppRowPosition.CENTER,
        appListVerticalAlignment = AppListVerticalAlignment.TOP,
        showMediaSkipButtons = false,
        landscape = LandscapeOverrides(clockScale = 2.0f, appRowPosition = AppRowPosition.RIGHT),
    )

    private val content = HomeScreenContent(listItems = emptyList(), dockItems = emptyList(), calendarEvents = emptyList())

    private fun build(facet: Facet?, landscape: Boolean, widgetSizing: HomeWidgetSizing = HomeWidgetSizing()) =
        resolveFacetStyle(facet, settings).toHomeScreenState(landscape, settings, content, widgetSizing)

    @Test
    fun `the state has the same placement as Home resolves for each orientation and kind of facet`() {
        // Given facets that inherit, override the clock, and have never dragged the divider
        val facets = listOf(
            null,
            Facet(id = 1L, name = "Inherits", position = 0),
            Facet(id = 1L, name = "Overrides", position = 0, overrideClock = true, clockAlignment = ClockAlignment.LEFT, clockScale = 0.5f, clockZoneHeightDp = 120f),
            Facet(id = 1L, name = "Never dragged", position = 0, overrideClock = true, clockZoneHeightDp = null),
            Facet(id = 1L, name = "Own landscape", position = 0, landscapeAppRowPosition = AppRowPosition.LEFT, landscapeClockScale = 0.9f),
        )

        for (facet in facets) {
            val home = HomeUiState(settings = settings, facets = listOfNotNull(facet))
            for (mode in HomeLayoutMode.entries) {
                // When the shared builder makes the state for that orientation
                val state = build(facet, landscape = mode == HomeLayoutMode.LANDSCAPE)

                // Then the clock and list are where the live Home puts them
                assertEquals(home.clockPlacement(mode), state.clock.placement)
                assertEquals(home.listAlignment(mode), state.list.alignment)
            }
        }
    }

    @Test
    fun `the clock look and list look come from the resolved style`() {
        val facet = Facet(id = 1L, name = "Own", position = 0, overrideClock = true, use24HourTime = true, dockDisplayMode = com.facetlauncher.app.data.model.DockDisplayMode.TEXT)

        val state = build(facet, landscape = false)
        val home = HomeUiState(settings = settings, facets = listOf(facet))

        assertEquals(home.effectiveUse24HourTime, state.clock.use24HourTime)
        assertEquals(home.clockTemplateId, state.clock.templateId)
        assertEquals(home.activeDockDisplayMode, state.dock.displayMode)
        assertEquals(home.activeAppListLayout, state.list.layout)
        assertEquals(home.activeAppRowPresentation, state.list.rowPresentation)
    }

    @Test
    fun `a facet with a hosted widget gets its saved size, then the provider default`() {
        val saved = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(widgetItem(41, id = 1L, widthDp = 160, heightDp = 90)))
        val unsized = Facet(id = 1L, name = "B", position = 0, clockStack = listOf(widgetItem(42, id = 2L)))
        val sizing = HomeWidgetSizing(defaultSizeDp = { 250 to 110 }, resizeFloorDp = { 60 to 60 })

        val savedWidget = build(saved, landscape = false, widgetSizing = sizing).widget
        val unsizedWidget = build(unsized, landscape = false, widgetSizing = sizing).widget

        assertEquals(HomeClockWidgetState(1L, 41, 160.dp, 90.dp, 60.dp, 60.dp), savedWidget)
        assertEquals(HomeClockWidgetState(2L, 42, 250.dp, 110.dp, 60.dp, 60.dp), unsizedWidget)
    }

    @Test
    fun `a widget size in landscape is its own where saved, else the portrait one`() {
        val facet = Facet(
            id = 1L, name = "A", position = 0,
            clockStack = listOf(widgetItem(41, widthDp = 160, heightDp = 90, landscapeWidthDp = 400, landscapeHeightDp = 300)),
        )

        assertEquals(160.dp, build(facet, landscape = false).widget?.widthDp)
        assertEquals(400.dp, build(facet, landscape = true).widget?.widthDp)
    }

    @Test
    fun `a widget page shows its own alignment and the clock page the facet's`() {
        // Given a facet whose clock page is left-aligned and whose widget page is centred in portrait and right in landscape
        val widget = widgetItem(41, id = 11L, position = 1).copy(alignment = ClockAlignment.CENTER, landscapeAlignment = ClockAlignment.RIGHT)
        val facet = Facet(id = 1L, name = "A", position = 0, clockPlacementOwn = true, clockAlignment = ClockAlignment.LEFT, clockStack = listOf(clockItem(id = 10L, position = 0), widget))
        val style = resolveFacetStyle(facet, settings)

        // When building each page, in each orientation
        fun alignmentOf(pageId: Long, landscape: Boolean) =
            style.toHomeScreenState(landscape, settings, content, live = HomeLiveState(visibleClockItemId = pageId)).clock.placement.alignment

        // Then the clock page uses the facet's, and the widget page its own
        assertEquals(ClockAlignment.LEFT, alignmentOf(10L, landscape = false))
        assertEquals(ClockAlignment.CENTER, alignmentOf(11L, landscape = false))
        assertEquals(ClockAlignment.RIGHT, alignmentOf(11L, landscape = true))
    }

    @Test
    fun `a widget page with no alignment of its own uses the facet's`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockPlacementOwn = true, clockAlignment = ClockAlignment.CENTER, clockStack = listOf(widgetItem(41, id = 11L)))

        val state = resolveFacetStyle(facet, settings).toHomeScreenState(false, settings, content)

        assertEquals(ClockAlignment.CENTER, state.clock.placement.alignment)
    }

    @Test
    fun `a stack whose first page is the clock builds the native clock`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(position = 0), widgetItem(41, position = 1)))

        assertNull(build(facet, landscape = false).widget)
    }

    @Test
    fun `the visible page decides whether the clock or a widget is built`() {
        // Given a clock page followed by a widget page
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1)))
        val style = resolveFacetStyle(facet, settings)

        // When building with each page visible
        val onClock = style.toHomeScreenState(false, settings, content, live = HomeLiveState(visibleClockItemId = 10L))
        val onWidget = style.toHomeScreenState(false, settings, content, live = HomeLiveState(visibleClockItemId = 11L))

        // Then the clock page has no widget and the widget page has its own, and both know the stack
        assertNull(onClock.widget)
        assertEquals(41, onWidget.widget?.appWidgetId)
        assertEquals(HomeClockStackState(pageCount = 2, pageIndex = 0), onClock.clockStack)
        assertEquals(HomeClockStackState(pageCount = 2, pageIndex = 1), onWidget.clockStack)
    }

    @Test
    fun `a page that is no longer in the stack falls back to the first`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1)))

        val state = resolveFacetStyle(facet, settings).toHomeScreenState(false, settings, content, live = HomeLiveState(visibleClockItemId = 99L))

        assertEquals(0, state.clockStack.pageIndex)
        assertNull(state.widget)
    }

    @Test
    fun `without pro the state has only the first clock page`() {
        // Given a clock and a widget, with the widget page remembered as visible
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1)))
        val style = resolveFacetStyle(facet, settings)

        // When building for a user without Pro
        val state = style.toHomeScreenState(false, settings, content, live = HomeLiveState(visibleClockItemId = 11L, isPro = false))

        // Then there is one page, the clock, and no widget
        assertEquals(HomeClockStackState(pageCount = 1, pageIndex = 0, hasClock = true), state.clockStack)
        assertNull(state.widget)
    }

    @Test
    fun `an empty stack builds a blank clock area`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = emptyList())

        val state = build(facet, landscape = false)

        assertEquals(true, state.clockStack.isBlank)
        assertNull(state.widget)
    }

    @Test
    fun `a facet without a hosted widget builds the native clock`() {
        assertNull(build(Facet(id = 1L, name = "A", position = 0), landscape = false).widget)
    }

    @Test
    fun `the live-only parts default to a quiet Home`() {
        val state = build(null, landscape = false)

        assertEquals(ClockAdjustMode.NONE, state.clock.adjustMode)
        assertNull(state.clock.placementFacetId)
        assertNull(state.clock.styleOwnerFacetName)
        assertNull(state.media.player)
        assertEquals(false, state.media.showSkipButtons)
    }

    @Test
    fun `a media player in the content reaches the state`() {
        val player = MediaPlayerState("pkg", "Song", "Artist", null, false, 0L, 1000L, 0L, 1f, true, true)

        val state = resolveFacetStyle(null, settings).toHomeScreenState(false, settings, content.copy(mediaPlayer = player))

        assertEquals(player, state.media.player)
    }
}
