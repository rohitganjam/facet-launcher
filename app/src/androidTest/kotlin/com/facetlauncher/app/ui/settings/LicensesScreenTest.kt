package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LicensesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}, onOpenLicense: (String) -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                LicensesScreen(onBack = onBack, onOpenLicense = onOpenLicense)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun listsLibrariesFontsAndDataSourcesAsInteractiveRows() {
        // Given the licences screen
        setContent()

        // Then a library, the fonts and a data source are each present and tappable
        composeRule.onNodeWithTag("license_row_dagger").assertHasClickAction()
        composeRule.onNodeWithTag("licenses_screen").performScrollToNode(hasTestTag("license_row_fonts"))
        composeRule.onNodeWithTag("license_row_fonts").assertHasClickAction()
        composeRule.onNodeWithTag("licenses_screen").performScrollToNode(hasTestTag("license_row_geonames"))
        composeRule.onNodeWithTag("license_row_geonames").assertHasClickAction()
    }

    @Test
    fun tappingARowOpensThatLicenseById() {
        // Given the licences screen
        var opened: String? = null
        setContent(onOpenLicense = { opened = it })

        // When tapping the Fonts row
        composeRule.onNodeWithTag("licenses_screen").performScrollToNode(hasTestTag("license_row_fonts"))
        composeRule.onNodeWithTag("license_row_fonts").performClick()

        // Then it asks to open the fonts licence text
        assertEquals("fonts", opened)
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the licences screen
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }
}
