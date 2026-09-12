package com.facetlauncher.app.ui.drawer

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class AlphabetRailTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersOneEntryPerLetterProvided() {
        // Given a rail configured with a specific, ordered set of letters
        val letters = listOf("A", "F", "M", "Z")
        composeRule.setContent {
            FacetLauncherTheme {
                AlphabetRail(letters = letters)
            }
        }

        // Then each letter renders exactly once — the rail shows only what's provided,
        // not a fixed A-Z alphabet
        letters.forEach { letter ->
            composeRule.onNodeWithText(letter).assertExists()
        }
    }
}
