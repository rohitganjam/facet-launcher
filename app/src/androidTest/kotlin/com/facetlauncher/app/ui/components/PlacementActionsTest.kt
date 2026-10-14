package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.model.FontScaleOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlacementActionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun actions(
        favoritesLabel: String = "Add to Favorites",
        dockLabel: String = "Remove from Dock",
        dockFacet: String? = null,
        onClick: (String) -> Unit = {},
    ) = listOf(
        PlacementActionUi("fav", Icons.Outlined.StarBorder, favoritesLabel, PlacementScope(null), isRemove = false, onClick = { onClick("fav") }),
        PlacementActionUi("dock", Icons.Outlined.Dock, dockLabel, PlacementScope(dockFacet), isRemove = true, onClick = { onClick("dock") }),
        PlacementActionUi("folder", Icons.Outlined.CreateNewFolder, "Add to folder", scope = null, isRemove = false, onClick = { onClick("folder") }),
    )

    private fun setContent(
        actions: List<PlacementActionUi> = actions(),
        systemFontScale: Float = 1f,
        appTextSize: FontScaleOption = FontScaleOption.DEFAULT,
    ) {
        composeRule.setContent {
            WithTextScale(systemFontScale, appTextSize) {
                Box(Modifier.width(360.dp).padding(24.dp)) { PlacementActions(actions) }
            }
        }
        composeRule.waitForIdle()
    }

    private fun bounds(tag: String) = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot()

    private fun assertTiles() {
        // Side by side: one top edge, strictly increasing left edges
        assertEquals(bounds("fav").top.value, bounds("dock").top.value, 0.5f)
        assertEquals(bounds("dock").top.value, bounds("folder").top.value, 0.5f)
        assertTrue(bounds("fav").left < bounds("dock").left && bounds("dock").left < bounds("folder").left)
    }

    private fun assertRows() {
        // Stacked: one left edge, strictly increasing tops, each as wide as the whole zone
        assertEquals(bounds("fav").left.value, bounds("dock").left.value, 0.5f)
        assertEquals(bounds("dock").left.value, bounds("folder").left.value, 0.5f)
        assertTrue(bounds("fav").top < bounds("dock").top && bounds("dock").top < bounds("folder").top)
        assertTrue(bounds("fav").width.value > 280f)
    }

    @Test
    fun threeActionsAreTilesSideBySideAtDefaultSize() {
        setContent()

        assertTiles()
        assertEquals(bounds("fav").width.value, bounds("dock").width.value, 0.5f)
        assertEquals(bounds("dock").width.value, bounds("folder").width.value, 0.5f)
    }

    @Test
    fun tilesStayTilesAtTheSmallestTextSize() {
        setContent(appTextSize = FontScaleOption.SMALL)

        assertTiles()
    }

    @Test
    fun tilesAreEqualHeightAndTheirIconsShareOneBaselineEvenWhenFolderHasNoPill() {
        setContent()

        assertEquals(bounds("fav").height.value, bounds("dock").height.value, 0.5f)
        assertEquals(bounds("dock").height.value, bounds("folder").height.value, 0.5f)
        assertEquals(bounds("fav_icon").top.value, bounds("dock_icon").top.value, 0.5f)
        assertEquals(bounds("dock_icon").top.value, bounds("folder_icon").top.value, 0.5f)
    }

    @Test
    fun aLongFacetNameThatCannotFitAPillSwitchesEveryActionToRows() {
        setContent(actions = actions(dockFacet = "Weekend reading list for the family"))

        assertRows()
    }

    @Test
    fun aLabelNeedingThreeLinesSwitchesEveryActionToRows() {
        // Stands in for a long translation: it can't wrap into two lines in a third of the width
        setContent(actions = actions(favoritesLabel = "Aus der Liste der Favoriten entfernen und noch mehr dazu"))

        assertRows()
    }

    @Test
    fun theLargestInAppTextSizeSwitchesToRows() {
        setContent(appTextSize = FontScaleOption.HUGE)

        assertRows()
    }

    @Test
    fun theLargestSystemFontScaleSwitchesToRows() {
        setContent(systemFontScale = 2f)

        assertRows()
    }

    @Test
    fun bothScalesRaisedTogetherStillGiveReachableRows() {
        var clicked = ""
        setContent(systemFontScale = 2f, appTextSize = FontScaleOption.HUGE, actions = actions(onClick = { clicked = it }))

        assertRows()
        composeRule.onNodeWithTag("dock").performClick()
        assertEquals("dock", clicked)
    }

    @Test
    fun rowIconsShareOneLeftEdgeAndSize() {
        setContent(systemFontScale = 2f)

        assertEquals(bounds("fav_icon").left.value, bounds("dock_icon").left.value, 0.5f)
        assertEquals(bounds("dock_icon").left.value, bounds("folder_icon").left.value, 0.5f)
        assertEquals(bounds("fav_icon").width.value, bounds("folder_icon").width.value, 0.5f)
    }

    @Test
    fun rowsAreAtLeast56dpTall() {
        setContent(systemFontScale = 2f)

        assertTrue(bounds("fav").height.value >= 56f)
        assertTrue(bounds("folder").height.value >= 56f)
    }

    @Test
    fun iconsKeepTheirBaseSizeAtDefaultText() {
        setContent()

        assertEquals(24f, bounds("fav_icon").width.value, 0.5f)
    }

    @Test
    fun iconsNeverShrinkBelowTheirBaseSizeAtSmallText() {
        setContent(appTextSize = FontScaleOption.SMALL, systemFontScale = 0.85f)

        assertEquals(24f, bounds("fav_icon").width.value, 0.5f)
    }

    @Test
    fun iconsGrowWithTheTextSize() {
        setContent(systemFontScale = 1.15f)

        assertEquals(24f * 1.15f, bounds("fav_icon").width.value, 0.5f)
    }

    @Test
    fun iconsStopGrowingAtOnePointThreeTimesAtTheSystemMaximum() {
        setContent(systemFontScale = 2f)

        assertEquals(24f * 1.3f, bounds("fav_icon").width.value, 0.5f)
    }

    @Test
    fun iconsStopGrowingAtOnePointThreeTimesWhenBothScalesAreRaised() {
        setContent(systemFontScale = 2f, appTextSize = FontScaleOption.HUGE)

        assertEquals(24f * 1.3f, bounds("fav_icon").width.value, 0.5f)
        assertEquals(24f * 1.3f, bounds("dock_icon").width.value, 0.5f)
    }

    @Test
    fun anActionTheAppIsAlreadyInIsFilledDifferentlyFromOneItIsNot() {
        setContent()

        // A point inside each tile's top-left, clear of the rounded corner and the centred content
        val density = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        val inset = (16 * density).toInt()
        val add = composeRule.onNodeWithTag("fav").captureToImage().toPixelMap()[inset, inset]
        val remove = composeRule.onNodeWithTag("dock").captureToImage().toPixelMap()[inset, inset]

        assertNotEquals(add, remove)
    }

    @Test
    fun tappingATileOrARowFiresItsAction() {
        var clicked = ""
        setContent(actions = actions(onClick = { clicked = it }))
        composeRule.onNodeWithTag("fav").performClick()
        assertEquals("fav", clicked)
        composeRule.onNodeWithTag("folder").performClick()
        assertEquals("folder", clicked)
    }

    @Test
    fun tappingARowFiresItsAction() {
        var clicked = ""
        setContent(systemFontScale = 2f, actions = actions(onClick = { clicked = it }))

        composeRule.onNodeWithTag("folder").performClick()

        assertEquals("folder", clicked)
    }
}

