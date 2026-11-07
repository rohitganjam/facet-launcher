package com.facetlauncher.app.ui.nativewidget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.home.HomeCalendarState
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeMediaState
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NativeWidgetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleState = NativeWidgetState(
        weather = previewWeatherState(),
        calendar = previewCalendarState(),
        media = previewMediaState(title = "Track", artist = "Artist"),
    )

    @Test
    fun resizingAWidgetSwitchesItsLayoutAtTheTierBoundary() {
        // Given a weather widget at the smallest size
        var size by mutableStateOf(DpSize(72.dp, 72.dp))
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.WEATHER, size, sampleState, NativeWidgetActions()) }
        }
        composeRule.onNodeWithTag("native_widget_tier_SMALL").assertExists()

        // When it grows to a 3 x 2 box and then to 2 x 1
        size = DpSize(224.dp, 148.dp)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllTags("native_widget_tier_LARGE") }
        composeRule.onNodeWithTag("native_widget_tier_LARGE").assertExists()
        size = DpSize(148.dp, 72.dp)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllTags("native_widget_tier_MEDIUM") }

        // Then the layout followed the box each time
        composeRule.onNodeWithTag("native_widget_tier_MEDIUM").assertExists()
    }

    @Test
    fun agendaShowsOnlyTheNextEventWhenSmallAndMoreWhenTall() {
        // Given an agenda in a small box
        var size by mutableStateOf(DpSize(148.dp, 72.dp))
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, size, sampleState, NativeWidgetActions()) }
        }

        // Then only the first event shows
        composeRule.onNodeWithText("Design review").assertExists()
        composeRule.onNodeWithText("Lunch with Sam").assertDoesNotExist()

        // When it is made taller
        size = DpSize(224.dp, 224.dp)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllText("Lunch with Sam") }

        // Then later events appear
        composeRule.onNodeWithText("Lunch with Sam").assertExists()
    }

    @Test
    fun agendaShowsTheRoomUnderTheTitleExceptWhenSmall() {
        // Given an agenda in a small box
        var size by mutableStateOf(DpSize(148.dp, 72.dp))
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, size, sampleState, NativeWidgetActions()) }
        }

        // Then the first event has no room line and there is no header
        composeRule.onNodeWithText("Marie Curie (4)").assertDoesNotExist()
        composeRule.onNodeWithTag("agenda_header").assertDoesNotExist()

        // When it grows to Medium
        size = DpSize(224.dp, 148.dp)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllText("Marie Curie (4)") }

        // Then the shortened room shows under a date header
        composeRule.onNodeWithText("Marie Curie (4)").assertExists()
        composeRule.onNodeWithTag("agenda_header").assertExists()
    }

    @Test
    fun smallAgendaPutsTheTimeBeforeTheTitleOnOneLine() {
        // Given a small agenda
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, DpSize(148.dp, 72.dp), sampleState, NativeWidgetActions()) }
        }

        // When reading where the time and the title sit
        val time = composeRule.onNodeWithTag("agenda_time", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val title = composeRule.onNodeWithText("Design review", useUnmergedTree = true).getUnclippedBoundsInRoot()

        // Then the time is to the left of the title, on the same line
        assertTrue(time.right <= title.left)
        assertTrue(abs((time.top + time.bottom).value - (title.top + title.bottom).value) < 8f)
    }

    @Test
    fun smallAgendaListsAsManyOneLineEventsAsFit() {
        // Given a small agenda just under the Medium height
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, DpSize(228.dp, 130.dp), sampleState, NativeWidgetActions()) }
        }

        // Then three events fit, with no headings, and a fourth does not
        composeRule.onNodeWithText("Design review").assertExists()
        composeRule.onNodeWithText("Lunch with Sam").assertExists()
        composeRule.onNodeWithText("Dentist").assertExists()
        composeRule.onNodeWithText("Team sync").assertDoesNotExist()
        composeRule.onNodeWithTag("agenda_header").assertDoesNotExist()
    }

    @Test
    fun agendaGivesEachDayItsOwnHeading() {
        // Given events on two different far-future days
        val day = 86_400_000L
        val base = 4_000_000_000_000L
        fun event(id: Long, title: String, start: Long) = CalendarEvent(id, "1", title, start, start + 1_800_000L, isAllDay = false)
        val state = sampleState.copy(
            calendar = HomeCalendarState(events = persistentListOf(event(1, "Morning", base), event(2, "Same day", base + 3_600_000L), event(3, "Next day", base + day))),
        )

        // When shown in a tall box
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, DpSize(224.dp, 300.dp), state, NativeWidgetActions()) }
        }

        // Then there is one heading per day, under a widget heading
        composeRule.onNodeWithTag("agenda_header").assertExists()
        val dayHeadings = composeRule.onAllNodes(SemanticsMatcher("is a day heading") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("agenda_day_") == true })
        dayHeadings.assertCountEquals(2)
    }

    @Test
    fun agendaScrollsThroughUpToTwentyEvents() {
        // Given 25 events in a short box
        val base = 4_000_000_000_000L
        val events = (1..25).map { CalendarEvent(it.toLong(), "1", "Event $it", base + it * 3_600_000L, base + it * 3_600_000L + 1_800_000L, isAllDay = false) }
        val state = sampleState.copy(calendar = HomeCalendarState(events = events.toImmutableList()))
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.AGENDA, DpSize(224.dp, 148.dp), state, NativeWidgetActions()) }
        }

        // Then only the first events are on screen
        composeRule.onNodeWithText("Event 1").assertExists()
        composeRule.onNodeWithText("Event 20").assertDoesNotExist()

        // When scrolled to the twentieth
        composeRule.onNodeWithTag("agenda_list").performScrollToNode(hasText("Event 20"))

        // Then the twentieth event is reachable and the twenty-first never is
        composeRule.onNodeWithText("Event 20").assertExists()
        composeRule.onNodeWithText("Event 21").assertDoesNotExist()
    }

    @Test
    fun tappingAnAgendaEventReportsIt() {
        // Given an agenda that reports taps
        var tapped: String? = null
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(NativeWidgetType.AGENDA, DpSize(224.dp, 148.dp), sampleState, NativeWidgetActions(onOpenEvent = { tapped = it.title }))
            }
        }

        // When the first event is tapped
        composeRule.onNodeWithText("Design review").performClick()

        // Then it is reported
        assertEquals("Design review", tapped)
    }

    @Test
    fun agendaWithNoEventsSaysSo() {
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(NativeWidgetType.AGENDA, DpSize(224.dp, 148.dp), sampleState.copy(calendar = HomeCalendarState()), NativeWidgetActions())
            }
        }

        composeRule.onNodeWithText("No upcoming events").assertExists()
    }

    @Test
    fun nowPlayingTransportButtonsReachTheMediaActions() {
        // Given a wide now-playing widget
        var plays = 0
        var nexts = 0
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(
                    NativeWidgetType.NOW_PLAYING, DpSize(300.dp, 72.dp), sampleState,
                    NativeWidgetActions(media = HomeMediaActions(onPlayPause = { plays++ }, onNext = { nexts++ })),
                )
            }
        }

        // When play/pause and next are tapped
        composeRule.onNodeWithTag("home_media_play_pause").performClick()
        composeRule.onNodeWithTag("home_media_next").performClick()

        // Then each action fires once and the title shows
        assertEquals(1, plays)
        assertEquals(1, nexts)
        composeRule.onNodeWithText("Track").assertExists()
    }

    @Test
    fun nowPlayingWithNothingPlayingSaysSo() {
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(NativeWidgetType.NOW_PLAYING, DpSize(300.dp, 72.dp), sampleState.copy(media = HomeMediaState()), NativeWidgetActions())
            }
        }

        composeRule.onNodeWithText("Nothing playing").assertExists()
    }

    @Test
    fun weatherWithoutAForecastSaysSoAndOpensTheSheetOnTap() {
        // Given a weather widget with no forecast yet
        var opened = 0
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(
                    NativeWidgetType.WEATHER, DpSize(148.dp, 148.dp),
                    sampleState.copy(weather = com.facetlauncher.app.ui.home.weather.WeatherUiState()),
                    NativeWidgetActions(onOpenWeather = { opened++ }),
                )
            }
        }

        // When tapped
        composeRule.onNodeWithText("No weather yet").performClick()

        // Then it opens the weather sheet
        assertEquals(1, opened)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllTags(tag: String): Boolean =
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllText(text: String): Boolean =
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun aLongPressOverTheWeatherWidgetFallsBackToHomesAndDoesNotAlsoOpenTheSheet() {
        // Given a weather widget on a page that has a long-press fallback
        var longPresses = 0
        var opened = 0
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(
                    NativeWidgetType.WEATHER, DpSize(148.dp, 148.dp), sampleState, NativeWidgetActions(onOpenWeather = { opened++ }),
                    modifier = Modifier.longPressFallback { longPresses++ },
                )
            }
        }

        // When it is held, then released
        composeRule.onNodeWithTag("native_widget_WEATHER").performTouchInput { longClick() }

        // Then Home's action ran once and the widget's own tap did not
        assertEquals(1, longPresses)
        assertEquals(0, opened)
    }

    @Test
    fun aQuickTapOverTheWeatherWidgetStillOpensTheSheet() {
        var longPresses = 0
        var opened = 0
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(
                    NativeWidgetType.WEATHER, DpSize(148.dp, 148.dp), sampleState, NativeWidgetActions(onOpenWeather = { opened++ }),
                    modifier = Modifier.longPressFallback { longPresses++ },
                )
            }
        }

        composeRule.onNodeWithTag("native_widget_WEATHER").performClick()

        assertEquals(1, opened)
        assertEquals(0, longPresses)
    }

    @Test
    fun aNowPlayingTitleTooLongForTheWidgetScrolls() {
        // Given a narrow Now playing widget with the clock paused
        composeRule.mainClock.autoAdvance = false
        val longTitle = "A very long track title that cannot possibly fit in the space it is given on the home screen"
        val title = longTitle
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(
                    NativeWidgetType.NOW_PLAYING, DpSize(220.dp, 72.dp),
                    sampleState.copy(media = previewMediaState(title = title, artist = "Artist")), NativeWidgetActions(),
                )
            }
        }
        composeRule.mainClock.advanceTimeByFrame()

        // When the marquee's start delay and some scrolling pass
        val before = composeRule.onNodeWithText(longTitle).captureToImage().toPixels()
        composeRule.mainClock.advanceTimeBy(4_000)
        val after = composeRule.onNodeWithText(longTitle).captureToImage().toPixels()

        // Then the long title has moved
        assertTrue("the long title should scroll", !before.contentEquals(after))
    }

    @Test
    fun aNowPlayingTitleThatFitsTheWidgetStaysPut() {
        // Given a widget wide enough for the title, with the clock paused
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            FacetLauncherTheme { NativeWidget(NativeWidgetType.NOW_PLAYING, DpSize(300.dp, 72.dp), sampleState, NativeWidgetActions()) }
        }
        composeRule.mainClock.advanceTimeByFrame()

        // When time passes
        val before = composeRule.onNodeWithText("Track").captureToImage().toPixels()
        composeRule.mainClock.advanceTimeBy(4_000)
        val after = composeRule.onNodeWithText("Track").captureToImage().toPixels()

        // Then it has not moved
        assertTrue("a short title should stay put", before.contentEquals(after))
    }

    private fun ImageBitmap.toPixels() = IntArray(width * height).also { readPixels(it) }
}
