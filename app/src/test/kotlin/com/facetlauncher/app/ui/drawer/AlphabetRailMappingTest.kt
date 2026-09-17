package com.facetlauncher.app.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlphabetRailMappingTest {

    private val letters = listOf("A", "B", "C", "D")

    /** A 200px rail band sitting inside a taller activation zone, e.g. bandTop=100, bandBottom=300. */
    private val bandTop = 100f
    private val bandBottom = 300f

    @Test
    fun `maps the top of the band to the first letter`() {
        assertEquals("A", letterAt(y = bandTop, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `maps the bottom of the band to the last letter`() {
        assertEquals("D", letterAt(y = bandBottom - 1f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `maps the middle of the band to a middle letter`() {
        assertEquals("C", letterAt(y = 200f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `a touch above the band clamps to the first letter, not null or a crash`() {
        assertEquals("A", letterAt(y = 0f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `a touch below the band clamps to the last letter, not null or a crash`() {
        assertEquals("D", letterAt(y = 999f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `returns null when there are no letters or the band has no height yet`() {
        assertNull(letterAt(y = 150f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = emptyList()))
        assertNull(letterAt(y = 150f, bandTopPx = 100f, bandBottomPx = 100f, letters = letters))
    }

    @Test
    fun `railSelectionAt with NONE behaves exactly like letterAt, just wrapped`() {
        assertEquals(RailSelection.Letter("A"), railSelectionAt(y = bandTop, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.NONE))
        assertEquals(RailSelection.Letter("C"), railSelectionAt(y = 200f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.NONE))
    }

    @Test
    fun `railSelectionAt with TOP gives the folder glyph its own slot ahead of every letter`() {
        // 4 letters + 1 folder slot = 5 equal slots across the 200px band (40px each)
        assertEquals(RailSelection.Folders, railSelectionAt(y = 110f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.TOP))
        assertEquals(RailSelection.Letter("A"), railSelectionAt(y = 150f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.TOP))
        assertEquals(RailSelection.Letter("D"), railSelectionAt(y = 299f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.TOP))
    }

    @Test
    fun `railSelectionAt with BOTTOM gives the folder glyph its own slot after every letter`() {
        assertEquals(RailSelection.Letter("A"), railSelectionAt(y = 110f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.BOTTOM))
        assertEquals(RailSelection.Folders, railSelectionAt(y = 299f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters, folderPosition = RailFolderPosition.BOTTOM))
    }

    @Test
    fun `railSelectionAt resolves to Folders alone when there are no letters but a folder slot exists`() {
        assertEquals(
            RailSelection.Folders,
            railSelectionAt(y = 150f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = emptyList(), folderPosition = RailFolderPosition.TOP),
        )
    }
}
