package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class ClockAdjustSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(overrideFacetName: String?, onEditStylesClick: () -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockAdjustSheet(
                    onAdjustClick = {},
                    onEditStylesClick = onEditStylesClick,
                    onFacetSettingsClick = {},
                    onLauncherSettingsClick = {},
                    overrideFacetName = overrideFacetName,
                )
            }
        }
    }

    @Test
    fun editStylesRowShowsTheGlobalBadgeWhenNoFacetOverridesItsOwn() {
        // Given no facet owns the clock's own style right now
        setContent(overrideFacetName = null)

        // Then the row's label stays generic and its badge reads "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Edit clock & calendar styles")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Global")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun editStylesRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        // Given the "Work" facet owns the clock's own style right now
        setContent(overrideFacetName = "Work")

        // Then the row's label is unchanged, and its badge names that facet instead of "Global"
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Edit clock & calendar styles")),
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNode(
            hasTestTag("clock_adjust_edit_styles") and hasAnyDescendant(hasText("Work")),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun tappingEditStylesRowFiresOnEditStylesClick() {
        var clicked = false
        setContent(overrideFacetName = null, onEditStylesClick = { clicked = true })

        composeRule.onNodeWithTag("clock_adjust_edit_styles").performClick()

        assert(clicked)
    }
}
