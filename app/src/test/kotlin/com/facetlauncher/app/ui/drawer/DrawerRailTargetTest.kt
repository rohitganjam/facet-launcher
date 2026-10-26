package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.GroupedItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrawerRailTargetTest {

    // A: 2 apps, B: 1 app. List rows: A header 0, a, a, B header 3, b. Grid items: a, a, b.
    private val grouped = GroupedItems(
        groups = linkedMapOf("A" to listOf("a1", "a2"), "B" to listOf("b1")),
        letters = listOf("A", "B"),
        headerIndexForLetter = mapOf("A" to 0, "B" to 3),
        firstAppIndexForLetter = mapOf("A" to 0, "B" to 2),
    )

    private fun target(
        selection: RailSelection,
        presentation: DrawerPresentation = DrawerPresentation.LIST,
        folderMode: DrawerFolderDisplayMode = DrawerFolderDisplayMode.DO_NOT_SHOW,
        folders: Int = 0,
        recents: RecentlyInstalledPosition = RecentlyInstalledPosition.DO_NOT_SHOW,
        recentsOffset: Int = 0,
    ) = railScrollTarget(selection, presentation, grouped, folderMode, folders, recents, recentsOffset)

    @Test
    fun `list letter jumps to its header row`() {
        assertEquals(3, target(RailSelection.Letter("B")))
    }

    @Test
    fun `grid letter jumps to its first app`() {
        assertEquals(2, target(RailSelection.Letter("B"), presentation = DrawerPresentation.GRID))
    }

    @Test
    fun `leading folders and recents shift the letter by their slots`() {
        val index = target(
            RailSelection.Letter("B"),
            folderMode = DrawerFolderDisplayMode.SHOW_FIRST,
            folders = 2,
            recents = RecentlyInstalledPosition.SHOW_FIRST,
            recentsOffset = 2,
        )

        assertEquals(3 + 2 + (1 + 2), index)
    }

    @Test
    fun `show first folders with none pinned add no header slot`() {
        val index = target(RailSelection.Letter("A"), folderMode = DrawerFolderDisplayMode.SHOW_FIRST, folders = 0)

        assertEquals(0, index)
    }

    @Test
    fun `grid folder section has no header slot`() {
        val index = target(RailSelection.Letter("A"), presentation = DrawerPresentation.GRID, folderMode = DrawerFolderDisplayMode.SHOW_FIRST, folders = 3)

        assertEquals(3, index)
    }

    @Test
    fun `unknown letter has no target`() {
        assertNull(target(RailSelection.Letter("Z")))
    }

    @Test
    fun `folders jump to the top or after the lettered content`() {
        assertEquals(2, target(RailSelection.Folders, folderMode = DrawerFolderDisplayMode.SHOW_FIRST, recentsOffset = 2))
        assertEquals(5, target(RailSelection.Folders, folderMode = DrawerFolderDisplayMode.SHOW_LAST))
        assertNull(target(RailSelection.Folders, folderMode = DrawerFolderDisplayMode.INLINE))
    }

    @Test
    fun `recently installed jumps to the top or past letters and trailing folders`() {
        assertEquals(0, target(RailSelection.RecentlyInstalled, recents = RecentlyInstalledPosition.SHOW_FIRST, recentsOffset = 2))
        assertEquals(5, target(RailSelection.RecentlyInstalled, recents = RecentlyInstalledPosition.SHOW_LAST))
        assertEquals(5 + 3, target(RailSelection.RecentlyInstalled, folderMode = DrawerFolderDisplayMode.SHOW_LAST, folders = 2, recents = RecentlyInstalledPosition.SHOW_LAST))
        assertNull(target(RailSelection.RecentlyInstalled))
    }
}
