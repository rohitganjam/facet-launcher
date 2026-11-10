package com.facetlauncher.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CopyFromFacetCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val facets = persistentListOf(
        Facet(id = 1L, name = "Work", position = 0),
        Facet(id = 3L, name = "Play", position = 2),
    )

    private fun setContent(facets: ImmutableList<Facet> = this.facets, onCopy: (Long) -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                CopyFromFacetCard(
                    facets = facets,
                    subtitle = "Use another facet's Dock",
                    confirmMessageRes = R.string.copy_from_facet_dock_message,
                    onCopy = onCopy,
                    testTagPrefix = "test",
                )
            }
        }
    }

    private fun nodeCount(tag: String) = composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().size

    @Test
    fun `the row opens a sheet listing the other facets`() {
        setContent()

        composeRule.onNodeWithTag("test_copy_from_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { nodeCount("test_copy_from_sheet") > 0 }

        composeRule.onNodeWithText("Work").assertIsDisplayed()
        composeRule.onNodeWithText("Play").assertIsDisplayed()
    }

    @Test
    fun `picking a facet asks to confirm and copies only after confirming`() {
        var copied: Long? = null
        setContent(onCopy = { copied = it })
        composeRule.onNodeWithTag("test_copy_from_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { nodeCount("test_copy_from_sheet") > 0 }

        composeRule.onNodeWithTag("test_copy_from_facet_3").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { nodeCount("confirm_dialog") > 0 }
        assertNull(copied)
        composeRule.onNodeWithText("Copy from Play?").assertIsDisplayed()
        composeRule.onNodeWithText("Copy").performClick()

        assertEquals(3L, copied)
    }

    @Test
    fun `cancelling the confirmation copies nothing`() {
        var copied: Long? = null
        setContent(onCopy = { copied = it })
        composeRule.onNodeWithTag("test_copy_from_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { nodeCount("test_copy_from_sheet") > 0 }
        composeRule.onNodeWithTag("test_copy_from_facet_1").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) { nodeCount("confirm_dialog") > 0 }

        composeRule.onNodeWithText("Cancel").performClick()

        assertNull(copied)
    }

    @Test
    fun `with no other facets the row is disabled`() {
        setContent(facets = persistentListOf())

        composeRule.onNodeWithTag("test_copy_from_row").assertIsNotEnabled()
        composeRule.onNodeWithText("No other facets to copy from").assertIsDisplayed()
    }
}
