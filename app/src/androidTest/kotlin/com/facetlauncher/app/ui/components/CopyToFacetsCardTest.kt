package com.facetlauncher.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CopyToFacetsCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val facets = persistentListOf(
        Facet(id = 1L, name = "Work", position = 0),
        Facet(id = 2L, name = "Home", position = 1),
        Facet(id = 3L, name = "Play", position = 2),
    )

    private fun setContent(onCopy: (Set<Long>) -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                CopyToFacetsCard(
                    facets = facets,
                    activeFacetId = 2L,
                    subtitle = "Copy the default",
                    sheetMessage = "Replaces their dock",
                    onCopy = onCopy,
                    testTagPrefix = "test",
                )
            }
        }
    }

    private fun openSheet() {
        composeRule.onNodeWithTag("test_copy_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTagCount("copy_to_facets_sheet") > 0
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTagCount(tag: String): Int =
        onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().size

    @Test
    fun theSheetListsEveryFacetPreselectedAndMarksTheCurrentOne() {
        setContent()

        openSheet()

        composeRule.onNodeWithText("Work").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Play").assertIsDisplayed()
        composeRule.onNodeWithText("Current").assertIsDisplayed()
        composeRule.onNodeWithTag("copy_to_facets_confirm").assertTextContains("Copy to 3 facets")
    }

    @Test
    fun deselectingAFacetLeavesItOutOfTheCopy() {
        var copied: Set<Long>? = null
        setContent(onCopy = { copied = it })
        openSheet()

        composeRule.onNodeWithTag("copy_to_facets_row_2").performClick()
        composeRule.onNodeWithTag("copy_to_facets_confirm").assertTextContains("Copy to 2 facets")
        composeRule.onNodeWithTag("copy_to_facets_confirm").performClick()

        assertEquals(setOf(1L, 3L), copied)
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodesWithTagCount("copy_to_facets_sheet") == 0 }
    }

    @Test
    fun copyIsDisabledWhileNoFacetIsSelected() {
        setContent()
        openSheet()

        composeRule.onNodeWithTag("copy_to_facets_row_1").performClick()
        composeRule.onNodeWithTag("copy_to_facets_row_2").performClick()
        composeRule.onNodeWithTag("copy_to_facets_row_3").performClick()
        composeRule.onNodeWithTag("copy_to_facets_confirm").assertIsNotEnabled()

        composeRule.onNodeWithTag("copy_to_facets_row_3").performClick()
        composeRule.onNodeWithTag("copy_to_facets_confirm").assertIsEnabled()
        composeRule.onNodeWithTag("copy_to_facets_confirm").assertTextContains("Copy to 1 facet")
    }
}
