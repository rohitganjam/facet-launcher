package com.facetlauncher.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InheritOverrideCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setCard(overriding: Boolean, onEditDefaults: (() -> Unit)?) {
        composeRule.setContent {
            FacetLauncherTheme {
                InheritOverrideCard(
                    overriding = overriding,
                    onOverridingChange = {},
                    testTagPrefix = "card",
                    onEditDefaults = onEditDefaults,
                )
            }
        }
    }

    @Test
    fun `edit defaults link opens the defaults while inheriting`() {
        // Given
        var opened = 0
        setCard(overriding = false, onEditDefaults = { opened++ })

        // When
        composeRule.onNodeWithTag("card_edit_defaults").performClick()

        // Then
        assertEquals(1, opened)
    }

    @Test
    fun `edit defaults link is hidden while overriding`() {
        setCard(overriding = true, onEditDefaults = {})

        composeRule.onNodeWithTag("card_edit_defaults").assertDoesNotExist()
    }

    @Test
    fun `edit defaults link is hidden without a callback`() {
        setCard(overriding = false, onEditDefaults = null)

        composeRule.onNodeWithTag("card_edit_defaults").assertDoesNotExist()
    }
}

