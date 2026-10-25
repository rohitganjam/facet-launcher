package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.model.ReleaseNotes
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class FakeReleaseNotesRepository(private val releases: List<ReleaseNotes>) :
    ReleaseNotesRepository(InstrumentationRegistry.getInstrumentation().targetContext) {
    override suspend fun releases(): List<ReleaseNotes> = releases
}

class WhatsNewScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(releases: List<ReleaseNotes>, onBack: () -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                val viewModel = remember { WhatsNewViewModel(FakeReleaseNotesRepository(releases)) }
                WhatsNewScreen(onBack = onBack, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    private val sample = listOf(
        ReleaseNotes("9.9.9", newFeatures = listOf("A brand new thing."), improvements = listOf("A better thing.")),
        ReleaseNotes("9.9.8", newFeatures = emptyList(), improvements = listOf("Only an improvement.")),
    )

    @Test
    fun eachReleaseShowsItsVersionAndBullets() {
        // Given two releases
        setContent(sample)

        // Then both cards show their version and bullets
        composeRule.onNodeWithTag("whats_new_release_9.9.9").assertIsDisplayed()
        composeRule.onNodeWithText("A brand new thing.").assertIsDisplayed()
        composeRule.onNodeWithText("A better thing.").assertIsDisplayed()
        composeRule.onNodeWithTag("whats_new_release_9.9.8").assertIsDisplayed()
        composeRule.onNodeWithText("Only an improvement.").assertIsDisplayed()
    }

    @Test
    fun aReleaseWithoutNewFeaturesOmitsTheNewHeading() {
        // Given a single release that only has an improvement
        setContent(listOf(sample[1]))

        // Then the Improved heading shows and the New heading does not
        composeRule.onNodeWithText("Improved").assertIsDisplayed()
        composeRule.onNodeWithText("New").assertDoesNotExist()
    }

    @Test
    fun theInstalledVersionIsMarked() {
        // Given a release matching the installed version and one that doesn't
        setContent(listOf(ReleaseNotes(BuildConfig.VERSION_NAME, emptyList(), listOf("Current.")), sample[0]))

        // Then only the installed one carries the marker
        composeRule.onNodeWithTag("whats_new_installed_${BuildConfig.VERSION_NAME}").assertIsDisplayed()
        composeRule.onNodeWithTag("whats_new_installed_9.9.9").assertDoesNotExist()
    }

    @Test
    fun noReleasesShowsTheEmptyMessage() {
        // Given no notes at all
        setContent(emptyList())

        // Then the empty state shows
        composeRule.onNodeWithTag("whats_new_empty").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the screen
        var backInvoked = false
        setContent(sample, onBack = { backInvoked = true })

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }
}


