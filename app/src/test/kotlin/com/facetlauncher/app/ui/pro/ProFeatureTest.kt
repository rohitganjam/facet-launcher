package com.facetlauncher.app.ui.pro

import com.facetlauncher.app.data.model.ProReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProFeatureTest {

    @Test
    fun `every pro limit has a row on the facet pro screen`() {
        // Given every reason the screen can open for, except Settings
        val limits = ProReason.entries - ProReason.ABOUT

        // When each is mapped to its row
        val rows = limits.map { it.feature }

        // Then none is missing, and every row is reached by some limit
        assertEquals(emptyList<ProReason>(), limits.filter { it.feature == null })
        assertEquals(ProFeature.entries.toSet(), rows.filterNotNull().toSet())
    }

    @Test
    fun `opening from settings points at no row`() {
        assertNull(ProReason.ABOUT.feature)
    }

    @Test
    fun `the rule limit and device triggers share the automation row`() {
        assertEquals(ProFeature.AUTOMATION, ProReason.TRIGGER.feature)
        assertEquals(ProFeature.AUTOMATION, ProReason.RULE_LIMIT.feature)
    }
}
