package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerBrowseModelTest {

    @Test
    fun `rail folder glyph follows the pinned section`() {
        assertEquals(RailFolderPosition.TOP, railFolderPositionFor(true, DrawerFolderDisplayMode.SHOW_FIRST))
        assertEquals(RailFolderPosition.BOTTOM, railFolderPositionFor(true, DrawerFolderDisplayMode.SHOW_LAST))
        assertEquals(RailFolderPosition.NONE, railFolderPositionFor(false, DrawerFolderDisplayMode.SHOW_FIRST))
        assertEquals(RailFolderPosition.NONE, railFolderPositionFor(true, DrawerFolderDisplayMode.INLINE))
    }

    @Test
    fun `rail recents glyph prefers the leading row`() {
        assertEquals(RailFolderPosition.TOP, railRecentlyInstalledPositionFor(hasLeadingRow = true, hasTrailingRow = false))
        assertEquals(RailFolderPosition.BOTTOM, railRecentlyInstalledPositionFor(hasLeadingRow = false, hasTrailingRow = true))
        assertEquals(RailFolderPosition.NONE, railRecentlyInstalledPositionFor(hasLeadingRow = false, hasTrailingRow = false))
    }

    @Test
    fun `recently installed takes two leading slots only in a list showing first with apps`() {
        val first = RecentlyInstalledPosition.SHOW_FIRST
        assertEquals(2, recentlyInstalledLeadingSlots(DrawerPresentation.LIST, first, hasApps = true))
        assertEquals(0, recentlyInstalledLeadingSlots(DrawerPresentation.GRID, first, hasApps = true))
        assertEquals(0, recentlyInstalledLeadingSlots(DrawerPresentation.LIST, first, hasApps = false))
        assertEquals(0, recentlyInstalledLeadingSlots(DrawerPresentation.LIST, RecentlyInstalledPosition.SHOW_LAST, hasApps = true))
    }
}
