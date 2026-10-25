package com.facetlauncher.app.ui.drawer

import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.DrawerGridDensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerGridLayoutTest {

    @Test
    fun `phone width gives five compact columns and four for the looser densities`() {
        // Given the grid width measured on a 393dp wide phone (after the rail and start padding)
        val width = 297.dp

        // Then the old 5 and 4 column presets are kept
        assertEquals(5, drawerGridColumns(width, DrawerGridDensity.COMPACT))
        assertEquals(4, drawerGridColumns(width, DrawerGridDensity.REGULAR))
        assertEquals(4, drawerGridColumns(width, DrawerGridDensity.SPACIOUS))
    }

    @Test
    fun `a wider window gets more columns at every density`() {
        DrawerGridDensity.entries.forEach { density ->
            // Given the same density on a phone and on an unfolded foldable
            val phone = drawerGridColumns(297.dp, density)
            val foldable = drawerGridColumns(745.dp, density)

            // Then the wider window is denser
            assertTrue("$density: $foldable should exceed $phone", foldable > phone)
        }
    }

    @Test
    fun `looser density never has more columns or rows than a tighter one`() {
        listOf(300.dp, 335.dp, 500.dp, 760.dp).forEach { width ->
            val columns = DrawerGridDensity.entries.map { drawerGridColumns(width, it) }
            assertEquals(columns.sortedDescending(), columns)
        }
        listOf(400.dp, 650.dp, 900.dp).forEach { height ->
            val rows = DrawerGridDensity.entries.map { drawerGridRows(height, it) }
            assertEquals(rows.sortedDescending(), rows)
        }
    }

    @Test
    fun `typical phone height gives six five and four rows`() {
        // Given the grid height measured on a 393dp wide phone
        val height = 601.dp

        assertEquals(6, drawerGridRows(height, DrawerGridDensity.COMPACT))
        assertEquals(5, drawerGridRows(height, DrawerGridDensity.REGULAR))
        assertEquals(4, drawerGridRows(height, DrawerGridDensity.SPACIOUS))
    }

    @Test
    fun `unfolded foldable is denser than a phone at every density`() {
        // Given the grid area measured on an unfolded Pixel Fold
        val width = 745.dp
        val height = 540.dp

        assertEquals(12 to 5, drawerGridColumns(width, DrawerGridDensity.COMPACT) to drawerGridRows(height, DrawerGridDensity.COMPACT))
        assertEquals(10 to 4, drawerGridColumns(width, DrawerGridDensity.REGULAR) to drawerGridRows(height, DrawerGridDensity.REGULAR))
        assertEquals(9 to 3, drawerGridColumns(width, DrawerGridDensity.SPACIOUS) to drawerGridRows(height, DrawerGridDensity.SPACIOUS))
    }

    @Test
    fun `tiny windows still get a minimum grid`() {
        assertEquals(3, drawerGridColumns(60.dp, DrawerGridDensity.SPACIOUS))
        assertEquals(2, drawerGridRows(60.dp, DrawerGridDensity.SPACIOUS))
    }

    @Test
    fun `a short card shows more rows at compact than at regular`() {
        // Given the grid area of the settings preview card
        val height = 320.dp

        assertEquals(3, drawerGridRows(height, DrawerGridDensity.COMPACT))
        assertEquals(2, drawerGridRows(height, DrawerGridDensity.REGULAR))
    }
}
