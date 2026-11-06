package com.facetlauncher.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * A size the user saved must survive a change of facet: Home re-fits what it draws as the page changes, but it never writes a
 * smaller size back on its own. The clock once saved a smaller box after a switch, because its re-clamp worked from the frame
 * before the new clock was measured.
 */
class HomeSizeStabilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val emptyState = HomeScreenState(list = HomeListState(items = emptyList()), dock = HomeDockState(items = emptyList()))

    private var state by mutableStateOf(emptyState)
    private val saved = mutableListOf<String>()
    private var widgetBounds: Rect? = null

    private fun setHome(initial: HomeScreenState) {
        state = initial
        composeRule.setContent {
            FacetLauncherTheme {
                HomeScreen(
                    state = state,
                    actions = HomeScreenActions(
                        item = HomeItemActions(onAppClick = {}),
                        clock = HomeClockActions(
                            onScaleCommit = { saved += "scale $it" },
                            onBoxSizeCommit = { w, h -> saved += "box ${w}x$h" },
                            onZoneHeightCommit = { saved += "zone $it" },
                        ),
                        widget = HomeClockWidgetActions(
                            onSizeCommit = { w, h -> saved += "widget ${w}x$h" },
                            onBoundsChange = { widgetBounds = it },
                        ),
                    ),
                    layoutMode = HomeLayoutMode.PORTRAIT,
                )
            }
        }
        composeRule.waitForIdle()
    }

    /** Lets the re-clamp's geometry-settle wait (and its debounce) run out, so anything it would save has been saved. */
    private fun letGeometrySettle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        Thread.sleep(700)
        composeRule.waitForIdle()
    }

    private fun clockOf(template: ClockTemplateId, facetId: Long, boxWidthDp: Int?) =
        state.clock.copy(templateId = template, placementFacetId = facetId, boxWidthDp = boxWidthDp)

    @Test
    fun aSwitchFromAFacetWhoseClockMeasuresNarrowerSavesNothingMoreThanASteadyClockWould() {
        // Given a clock that measures narrow (condensed date), one that measures like the saved one (no box), and a sweep of saved boxes,
        // some of which genuinely do not fit (the re-clamp rightly trims those, whichever clock came before)
        val narrow = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 3L, boxWidthDp = null).copy(dateStyle = ClockDateStyle.CONDENSED)
        val sameWidth = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 2L, boxWidthDp = null)
        setHome(emptyState.copy(clock = sameWidth))
        letGeometrySettle()

        for (boxDp in SWEPT_BOX_DP) {
            val target = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 1L, boxWidthDp = boxDp)

            // When the saved box's facet comes after a clock of the same width: what a correct re-clamp saves
            state = emptyState.copy(clock = sameWidth)
            letGeometrySettle()
            saved.clear()
            state = emptyState.copy(clock = target)
            letGeometrySettle()
            val steady = saved.toList()

            // And after the narrower clock
            state = emptyState.copy(clock = narrow)
            letGeometrySettle()
            saved.clear()
            state = emptyState.copy(clock = target)
            letGeometrySettle()

            // Then it saved exactly the same
            assertEquals("a $boxDp dp box after a narrower clock", steady, saved.toList())
        }
    }

    @Test
    fun aSavedClockBoxIsNotShrunkWhenSwitchingAwayAndBackToTheSameClock() {
        // Given a large saved box on the default clock style
        setHome(emptyState.copy(clock = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 1L, boxWidthDp = LARGE_BOX_DP)))
        letGeometrySettle()
        saved.clear()

        // When another facet's clock (no box yet) shows and the first facet comes back
        state = state.copy(clock = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 2L, boxWidthDp = null))
        letGeometrySettle()
        state = state.copy(clock = clockOf(ClockTemplateId.LIGHT_STACK, facetId = 1L, boxWidthDp = LARGE_BOX_DP))
        letGeometrySettle()

        // Then nothing was saved
        assertTrue("saved sizes after the switches: $saved", saved.isEmpty())
    }

    @Test
    fun aWeatherWidgetKeepsItsSavedSizeAcrossAFacetSwitch() = widgetKeepsItsSizeAcrossAFacetSwitch(NativeWidgetType.WEATHER)

    @Test
    fun anAgendaWidgetKeepsItsSavedSizeAcrossAFacetSwitch() = widgetKeepsItsSizeAcrossAFacetSwitch(NativeWidgetType.AGENDA)

    @Test
    fun aNowPlayingWidgetKeepsItsSavedSizeAcrossAFacetSwitch() = widgetKeepsItsSizeAcrossAFacetSwitch(NativeWidgetType.NOW_PLAYING)

    private fun widgetState(type: NativeWidgetType, facetId: Long) = emptyState.copy(
        clock = state.clock.copy(placementFacetId = facetId),
        widget = HomeClockWidgetState(
            itemId = facetId, appWidgetId = -(facetId.toInt()), widthDp = 260.dp, heightDp = 160.dp,
            minWidthDp = 60.dp, minHeightDp = 60.dp, nativeType = type,
        ),
    )

    private fun widgetKeepsItsSizeAcrossAFacetSwitch(type: NativeWidgetType) {
        // Given a Facet widget saved at a size
        setHome(widgetState(type, facetId = 1L))
        letGeometrySettle()
        val before = widgetBounds
        saved.clear()

        // When the active facet changes, and then changes back
        state = widgetState(type, facetId = 2L)
        letGeometrySettle()
        state = widgetState(type, facetId = 1L)
        letGeometrySettle()

        // Then nothing was saved, and the widget is drawn in the same place at the same size
        assertTrue("saved sizes after the switches: $saved", saved.isEmpty())
        assertEquals(before, widgetBounds)
    }

    private companion object {
        /** Wider than the default clock draws, so a re-clamp that mistook its scale would find it too big. */
        const val LARGE_BOX_DP = 247
        val SWEPT_BOX_DP = (150..420 step 10).toList()
    }
}
