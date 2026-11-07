package com.facetlauncher.app.ui.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.facetlauncher.app.data.SETTINGS_CATALOG
import com.facetlauncher.app.data.model.SettingsSearchEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SettingsSearchIconTest {

    private fun entry(id: String) = SettingsSearchEntry(id = id, label = id, action = "x")

    @Test
    fun `every searchable setting has its own icon`() {
        // Given the catalog of findable settings
        val ids = SETTINGS_CATALOG.map { it.id }

        // When resolving each one's icon
        val icons = ids.map { entry(it).icon() }

        // Then none falls back to the generic cog and no two share an icon
        icons.forEachIndexed { index, icon -> assertNotEquals("${ids[index]} has no icon of its own", Icons.Default.Settings, icon) }
        assertEquals(ids.size, icons.toSet().size)
    }

    @Test
    fun `an unknown setting falls back to the generic settings icon`() {
        // When resolving a setting that has no mapping
        val icon = entry("not_a_setting").icon()

        // Then it is the generic cog
        assertEquals(Icons.Default.Settings, icon)
    }
}
