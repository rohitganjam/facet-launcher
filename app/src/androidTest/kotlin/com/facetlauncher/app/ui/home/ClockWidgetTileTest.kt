package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
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
}
