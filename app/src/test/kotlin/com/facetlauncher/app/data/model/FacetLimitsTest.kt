package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FacetLimitsTest {

    @Test
    fun `free allows three facets and pro allows ten`() {
        assertEquals(3, FacetLimits.maxFor(isPro = false))
        assertEquals(10, FacetLimits.maxFor(isPro = true))
    }

    @Test
    fun `a free user can add until three and a pro user until ten`() {
        assertTrue(FacetLimits.canAdd(2, isPro = false))
        assertFalse(FacetLimits.canAdd(3, isPro = false))
        assertTrue(FacetLimits.canAdd(9, isPro = true))
        assertFalse(FacetLimits.canAdd(10, isPro = true))
    }

    @Test
    fun `a free user who still has more than the limit cannot add`() {
        assertFalse(FacetLimits.canAdd(7, isPro = false))
    }

    @Test
    fun `pro makes every facet selectable`() {
        assertEquals(setOf(5L, 6L, 7L, 8L), FacetLimits.selectableIds(listOf(5, 6, 7, 8), isPro = true))
    }

    @Test
    fun `free makes only the first three in list order selectable`() {
        assertEquals(setOf(5L, 6L, 7L), FacetLimits.selectableIds(listOf(5, 6, 7, 8, 9), isPro = false))
    }

    @Test
    fun `free with three or fewer facets loses nothing`() {
        assertEquals(setOf(1L, 2L), FacetLimits.selectableIds(listOf(1, 2), isPro = false))
    }

    @Test
    fun `selectability follows list order not ids so reordering chooses which are usable`() {
        assertEquals(setOf(9L, 8L, 7L), FacetLimits.selectableIds(listOf(9, 8, 7, 1, 2), isPro = false))
    }
}
