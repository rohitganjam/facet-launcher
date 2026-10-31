package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ClockWidgetTileTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun committedSizeChangeIsPushedToTheProvider() {
        // Given a hosted widget tile committed at 200 x 100
        val pushed = mutableListOf<Pair<Int, Int>>()
        var committedWidth by mutableStateOf(200.dp)
        var committedHeight by mutableStateOf(100.dp)
        composeRule.setContent {
            ClockWidgetTile(
                appWidgetId = 7,
                committedWidthDp = committedWidth,
                committedHeightDp = committedHeight,
                createHostView = { context, _ -> AppWidgetHostView(context) },
                onSizeChange = { _, w, h -> pushed += w to h },
                onLongPress = {},
            )
        }
        composeRule.waitForIdle()

        // When the size is committed again at 360 x 251, as after a resize drag
        composeRule.runOnIdle {
            committedWidth = 360.dp
            committedHeight = 251.dp
        }
        composeRule.waitForIdle()

        // Then the provider was told the first size, then the new one
        assertEquals(200 to 100, pushed.first())
        assertEquals(360 to 251, pushed.last())
    }

    @Test
    fun zeroCommittedSizeIsNotPushed() {
        // Given a tile that has not been measured yet
        val pushed = mutableListOf<Pair<Int, Int>>()
        composeRule.setContent {
            ClockWidgetTile(
                appWidgetId = 7,
                committedWidthDp = 0.dp,
                committedHeightDp = 0.dp,
                createHostView = { context, _ -> AppWidgetHostView(context) },
                onSizeChange = { _, w, h -> pushed += w to h },
                onLongPress = {},
            )
        }

        // When composition settles
        composeRule.waitForIdle()

        // Then nothing is pushed, as a 0-size would break the provider
        assertEquals(emptyList<Pair<Int, Int>>(), pushed)
    }

    @Test
    fun aHorizontalDragOnAStaticWidgetIsHandedToHome() {
        // Given a widget that takes no part in nested scrolling
        val calls = mutableListOf<String>()
        var scrolledX = 0f
        composeRule.setContent {
            ClockWidgetTile(
                appWidgetId = 7,
                committedWidthDp = 300.dp,
                committedHeightDp = 200.dp,
                createHostView = { context, _ -> AppWidgetHostView(context) },
                onSizeChange = { _, _, _ -> },
                onLongPress = {},
                onNestedScrollStart = { calls += "start" },
                onLeftoverScroll = { dx, _ -> scrolledX += dx },
                onNestedScrollStop = { calls += "stop" },
            )
        }
        composeRule.waitForIdle()

        // When dragging left across it
        composeRule.onNodeWithTag("clock_widget_host_7").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // Then Home was handed the drag as a scroll to the right (the negation of the finger), start to stop
        assertEquals(listOf("start", "stop"), calls)
        assertTrue("scrolled $scrolledX", scrolledX > 0f)
    }

    @Test
    fun aVerticalDragOnAStaticWidgetStaysWithTheWidget() {
        val calls = mutableListOf<String>()
        composeRule.setContent {
            ClockWidgetTile(
                appWidgetId = 7,
                committedWidthDp = 300.dp,
                committedHeightDp = 200.dp,
                createHostView = { context, _ -> AppWidgetHostView(context) },
                onSizeChange = { _, _, _ -> },
                onLongPress = {},
                onNestedScrollStart = { calls += "start" },
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("clock_widget_host_7").performTouchInput { swipeUp() }
        composeRule.waitForIdle()

        assertEquals(emptyList<String>(), calls)
    }
}
