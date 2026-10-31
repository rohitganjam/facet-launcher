package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.FREE_CLOCK_STACK_ITEMS
import com.facetlauncher.app.data.model.MAX_CLOCK_STACK_ITEMS
import com.facetlauncher.app.data.model.clockStackLimit
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockStackLimitsTest {

    @Test
    fun `free keeps one page and pro gets the maximum`() {
        assertEquals(FREE_CLOCK_STACK_ITEMS, clockStackLimit(isPro = false))
        assertEquals(MAX_CLOCK_STACK_ITEMS, clockStackLimit(isPro = true))
        assertEquals(1, FREE_CLOCK_STACK_ITEMS)
        assertEquals(5, MAX_CLOCK_STACK_ITEMS)
    }
}
