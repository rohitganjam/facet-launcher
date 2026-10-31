package com.facetlauncher.app.ui.home.stack

import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockStackUiStateTest {

    private fun rows(count: Int, withClock: Boolean = false) = List(count) { index ->
        val isClock = withClock && index == 0
        ClockStackRow(
            id = index.toLong(),
            kind = if (isClock) ClockStackItemKind.CLOCK else ClockStackItemKind.WIDGET,
            appWidgetId = if (isClock) null else index,
            providerName = null,
            canConfigure = true,
        )
    }

    @Test
    fun `the limit is one page on free and five on pro`() {
        assertEquals(1, ClockStackUiState(isPro = false).limit)
        assertEquals(MAX_CLOCK_STACK_ITEMS, ClockStackUiState(isPro = true).limit)
    }

    @Test
    fun `a free stack with its one page shows the add row with a pro pill`() {
        val state = ClockStackUiState(rows = rows(1), isPro = false)

        assertTrue(state.showAdd)
        assertTrue(state.addNeedsPro)
    }

    @Test
    fun `an empty free stack adds without pro`() {
        val state = ClockStackUiState(rows = rows(0), isPro = false)

        assertTrue(state.showAdd)
        assertFalse(state.addNeedsPro)
    }

    @Test
    fun `a pro stack adds without a pill until it is full, then hides the add row`() {
        assertFalse(ClockStackUiState(rows = rows(4), isPro = true).addNeedsPro)
        assertTrue(ClockStackUiState(rows = rows(4), isPro = true).showAdd)

        val full = ClockStackUiState(rows = rows(MAX_CLOCK_STACK_ITEMS), isPro = true)
        assertFalse(full.showAdd)
    }

    @Test
    fun `a lapsed stack over the free limit keeps its rows and has no room to add`() {
        val state = ClockStackUiState(rows = rows(3), isPro = false)

        assertEquals(3, state.rows.size)
        assertTrue(state.addNeedsPro)
        assertTrue(state.showAdd)
    }

    @Test
    fun `hasClock is true only while a clock row is in the stack`() {
        assertTrue(ClockStackUiState(rows = rows(3, withClock = true)).hasClock)
        assertFalse(ClockStackUiState(rows = rows(3, withClock = false)).hasClock)
        assertFalse(ClockStackUiState().hasClock)
    }
}
