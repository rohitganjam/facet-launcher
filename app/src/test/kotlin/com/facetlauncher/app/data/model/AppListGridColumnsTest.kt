package com.facetlauncher.app.data.model

import com.facetlauncher.app.domain.toEnumOrDefault
import org.junit.Assert.assertEquals
import org.junit.Test

class AppListGridColumnsTest {

    @Test
    fun `every concrete option has its column count in ascending order`() {
        // Given the options a user can pick (all but the facet-only launcher default)
        val options = AppListGridColumns.entries - AppListGridColumns.LAUNCHER_DEFAULT

        // Then they run 3, 4, 5, 6 columns
        assertEquals(listOf(3, 4, 5, 6), options.map { it.columns })
        assertEquals(listOf(3, 4, 5, 6), options.map { it.columns }.sorted())
    }

    @Test
    fun `three columns survives a backup round trip by name`() {
        // Given three columns exported by name, as the backup stores it
        val stored = AppListGridColumns.THREE.name

        // Then importing it gives three columns back, not the launcher default
        assertEquals(AppListGridColumns.THREE, stored.toEnumOrDefault(AppListGridColumns.LAUNCHER_DEFAULT))
    }

    @Test
    fun `an unrecognized stored name falls back to the default`() {
        assertEquals(AppListGridColumns.LAUNCHER_DEFAULT, "SEVEN".toEnumOrDefault(AppListGridColumns.LAUNCHER_DEFAULT))
    }
}
