package com.facetlauncher.app.ui.onboarding

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [SetDefaultLauncherSheet] is now a plain, stateless composable — it no longer needs a
 * repository/database-backed host the way it did back when it rendered its own preview of Home
 * (see [OnboardingScreenTest]'s own history: this behavior used to live there, inside a
 * `SET_DEFAULT` onboarding step, before it moved to being an overlay [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]
 * shows over the real Home screen instead).
 */
class SetDefaultLauncherSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val requestIntentAction = "com.facetlauncher.app.TEST_REQUEST_DEFAULT_LAUNCHER"

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun setContent(isDefaultLauncher: Boolean = false, onFinish: () -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                SetDefaultLauncherSheet(
                    isDefaultLauncher = isDefaultLauncher,
                    requestDefaultLauncherIntent = { Intent(requestIntentAction) },
                    onFinish = onFinish,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `not yet default shows the Set as default action`() {
        setContent(isDefaultLauncher = false)

        composeRule.onNodeWithTag("onboarding_set_default").assertExists()
        composeRule.onNodeWithTag("onboarding_later").assertExists()
        composeRule.onNodeWithTag("onboarding_done").assertDoesNotExist()
    }

    @Test
    fun `tapping Set as default launches the request intent`() {
        setContent(isDefaultLauncher = false)

        composeRule.onNodeWithTag("onboarding_set_default").performClick()

        Intents.intended(hasAction(requestIntentAction))
    }

    @Test
    fun `tapping Later finishes`() {
        var finished = false
        setContent(isDefaultLauncher = false, onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_later").performClick()

        assertTrue(finished)
    }

    @Test
    fun `tapping the scrim finishes the same as Later`() {
        var finished = false
        setContent(isDefaultLauncher = false, onFinish = { finished = true })

        composeRule.onNodeWithTag("set_default_launcher_scrim").performClick()

        assertTrue(finished)
    }

    @Test
    fun `already default shows the Done action instead`() {
        setContent(isDefaultLauncher = true)

        composeRule.onNodeWithText("Facet is already your home screen").assertExists()
        composeRule.onNodeWithTag("onboarding_done").assertExists()
        composeRule.onNodeWithTag("onboarding_set_default").assertDoesNotExist()
        composeRule.onNodeWithTag("onboarding_later").assertDoesNotExist()
    }

    @Test
    fun `tapping Done finishes`() {
        var finished = false
        setContent(isDefaultLauncher = true, onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_done").performClick()

        assertTrue(finished)
    }
}
