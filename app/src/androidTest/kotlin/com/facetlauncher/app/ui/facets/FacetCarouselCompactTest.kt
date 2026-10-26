package com.facetlauncher.app.ui.facets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FacetCarouselCompactTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val facet = Facet(id = 1L, name = "Facet 1", position = 0)

    private fun setAddPage(height: Int, compact: Boolean) {
        composeRule.setContent {
            FacetLauncherTheme {
                Box(Modifier.size(width = 264.dp, height = height.dp)) {
                    AddFacetPage(onClick = {}, showProPill = false, compact = compact, modifier = Modifier.size(width = 264.dp, height = height.dp).testTag("add_page"))
                }
            }
        }
    }

    @Test
    fun aShortAddFacetCardShowsItsTitleWhollyInsideTheCard() {
        // Given a landscape phone's page: a 120dp card plus the 48dp name row
        setAddPage(height = 168, compact = true)

        // When measuring the title against the page
        val page = composeRule.onNodeWithTag("add_page").getUnclippedBoundsInRoot()
        val title = composeRule.onNodeWithText("Add facet").getUnclippedBoundsInRoot()

        // Then the title sits fully inside it, and the subtitle that needs more room is left out
        assertTrue("title $title should be inside page $page", title.top >= page.top && title.bottom <= page.bottom)
        composeRule.onAllNodesWithText("Starts with", substring = true).assertCountEquals(0)
    }

    @Test
    fun aTallAddFacetCardKeepsItsSubtitle() {
        // Given a portrait-sized page
        setAddPage(height = 320, compact = false)

        // Then the full content, subtitle included, is there
        composeRule.onAllNodesWithText("Starts with", substring = true).assertCountEquals(1)
    }

    @Test
    fun theFacetMenuOffersSettingsAndDelete() {
        // Given a facet's three-dot menu
        var edited = 0
        var deleted = 0
        composeRule.setContent {
            FacetLauncherTheme { FacetPageMenu(facet, canDelete = true, onEditFacetClick = { edited++ }, onDeleteClick = { deleted++ }) }
        }

        // When opening it and choosing each row
        composeRule.onNodeWithTag("facet_page_menu_1").performClick()
        composeRule.onNodeWithText("Facet settings").performClick()
        composeRule.onNodeWithTag("facet_page_menu_1").performClick()
        composeRule.onNodeWithText("Delete facet").performClick()

        // Then each fired once
        assertEquals(1, edited)
        assertEquals(1, deleted)
    }

    @Test
    fun deleteIsDisabledWhenTheFacetCannotBeDeleted() {
        // Given the only facet, which cannot be deleted
        composeRule.setContent {
            FacetLauncherTheme { FacetPageMenu(facet, canDelete = false, onEditFacetClick = {}, onDeleteClick = {}) }
        }

        // When opening the menu
        composeRule.onNodeWithTag("facet_page_menu_1").performClick()

        // Then Delete is shown but disabled
        composeRule.onNodeWithText("Delete facet").assertIsNotEnabled()
    }
}
