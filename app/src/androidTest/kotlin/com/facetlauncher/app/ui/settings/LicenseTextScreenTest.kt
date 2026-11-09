package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.LicenseRepository
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LicenseTextScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(licenseId: String, onBack: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember { LicenseTextViewModel(SavedStateHandle(mapOf("licenseId" to licenseId)), LicenseRepository(context)) }
            FacetLauncherTheme {
                LicenseTextScreen(onBack = onBack, viewModel = viewModel)
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { composeRule.onAllNodesWithTag("license_text_body").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun showsTheBundledLicenceTextAsPublished() {
        // Given the GeoNames entry (CC BY 4.0)
        setContent("geonames")

        // Then the licence text is on screen under the entry's name
        composeRule.onNodeWithText("City names, GeoNames").assertExists()
        composeRule.onAllNodesWithTag("license_text_body")[0].assertTextContains("Attribution 4.0 International", substring = true)
    }

    @Test
    fun fontsShowEachFontsOwnNotice() {
        // Given the fonts entry
        setContent("fonts")

        // Then the first font's heading and copyright line are shown
        composeRule.onNodeWithText("Inter").assertExists()
        composeRule.onAllNodesWithTag("license_text_body")[0].assertTextContains("Copyright", substring = true)
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given a licence text screen
        var backInvoked = false
        setContent("dagger", onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }
}
