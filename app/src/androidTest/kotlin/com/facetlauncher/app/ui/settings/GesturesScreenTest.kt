package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.SwipeAction
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class GesturesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        accessGranted: Boolean = true,
        onNavigateToAccessibilityAccessExplanation: () -> Unit = {},
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "gestures-screen-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                GesturesViewModel(settingsRepository, FakeAccessibilityAccessRepository(context, accessGranted))
            }
            FacetLauncherTheme {
                GesturesScreen(
                    onBack = {},
                    onNavigateToAccessibilityAccessExplanation = onNavigateToAccessibilityAccessExplanation,
                    viewModel = viewModel,
                )
            }
        }
        return settingsRepository
    }

    /** The DataStore write runs off the main thread, so wait for it instead of reading right after the click. */
    private fun awaitStored(settingsRepository: SettingsRepository, check: (com.facetlauncher.app.data.model.LauncherSettings) -> Boolean) {
        composeRule.waitUntil(timeoutMillis = 5_000) { runBlocking { check(settingsRepository.settings.first()) } }
        composeRule.waitForIdle()
    }

    @Test
    fun listsEveryBuiltInGestureAndBothOptionalOnes() {
        // Given the gestures screen
        setContent()

        // Then each built-in swipe and the long-press is listed with what it does
        composeRule.onNodeWithText("Swipe up").assertExists()
        composeRule.onNodeWithText("Open the app drawer").assertExists()
        composeRule.onAllNodesWithText("Swipe right").onFirst().assertExists()
        composeRule.onAllNodesWithText("Open the Hub").onFirst().assertExists()
        composeRule.onAllNodesWithText("Swipe left").onFirst().assertExists()
        composeRule.onAllNodesWithText("Switch facets").onFirst().assertExists()
        composeRule.onAllNodesWithText("Swipe down").onFirst().assertExists()
        composeRule.onAllNodesWithText("Open the notification shade").onFirst().assertExists()
        composeRule.onNodeWithText("Long-press empty space").assertExists()

        // And the optional gesture has a switch, off by default (double-tap to lock is disabled for now)
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").assertIsOff()
    }

    @Test
    fun turningOnHomeButtonOpensSearchPersistsIt() {
        // Given the gestures screen
        val settingsRepository = setContent()

        // When turning on "Home button opens search"
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").performScrollTo().performClick()
        awaitStored(settingsRepository) { it.homeOpensSearch }

        // Then the switch is on and the setting is stored
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").assertIsOn()
        assertTrue(runBlocking { settingsRepository.settings.first().homeOpensSearch })
    }

    @Test
    fun pickingAnActionForASwipeStoresItAndShowsIt() {
        // Given the gestures screen
        val settingsRepository = setContent()

        // When swipe left is tapped and set to None
        composeRule.onNodeWithTag("gesture_swipe_left").performClick()
        composeRule.onNodeWithTag("swipe_action_none").performClick()
        awaitStored(settingsRepository) { it.swipeActions.left == SwipeAction.NONE }

        // Then the row reads None and the sheet is gone
        composeRule.onNodeWithText("None").assertExists()
        composeRule.onNodeWithTag("swipe_action_sheet").assertDoesNotExist()
    }

    @Test
    fun anActionAnotherSwipeHasAsksFirstThenGoesOnBoth() {
        // Given the gestures screen
        val settingsRepository = setContent()

        // When swipe down is set to the app drawer, which swipe up already opens
        composeRule.onNodeWithTag("gesture_swipe_down").performClick()
        composeRule.onNodeWithTag("swipe_action_app_drawer").performClick()

        // Then a warning asks first, and nothing is stored yet
        composeRule.onNodeWithTag("confirm_dialog").assertExists()
        assertEquals(SwipeAction.NOTIFICATION_SHADE, runBlocking { settingsRepository.settings.first().swipeActions.down })

        // When it is confirmed
        composeRule.onNodeWithText("Use it too").performClick()
        awaitStored(settingsRepository) { it.swipeActions.down == SwipeAction.APP_DRAWER }

        // Then both swipes open the drawer
        assertEquals(SwipeAction.APP_DRAWER, runBlocking { settingsRepository.settings.first().swipeActions.up })
    }

    @Test
    fun cancellingTheWarningLeavesTheSwipeAsItWas() {
        // Given the gestures screen
        val settingsRepository = setContent()

        // When swipe left is set to the Hub and the warning is cancelled
        composeRule.onNodeWithTag("gesture_swipe_left").performClick()
        composeRule.onNodeWithTag("swipe_action_hub").performClick()
        composeRule.onNodeWithTag("confirm_dialog_cancel").performClick()
        composeRule.waitForIdle()

        // Then swipe left still switches facets
        assertEquals(SwipeAction.SWITCH_FACETS, runBlocking { settingsRepository.settings.first().swipeActions.left })
    }

    @Test
    fun theAppDrawersOnlySwipeCannotBeChangedAway() {
        // Given the gestures screen with the drawer on swipe up only
        setContent()

        // When swipe up is tapped
        composeRule.onNodeWithTag("gesture_swipe_up").performClick()

        // Then every other action is disabled with a note saying why
        composeRule.onNodeWithTag("swipe_action_none").assertIsNotEnabled()
        composeRule.onNodeWithTag("swipe_action_hub").assertIsNotEnabled()
        composeRule.onAllNodesWithText("The app drawer needs a swipe. Give it another swipe first.").onFirst().assertExists()
    }

    // Skipped (an assumption, since the runner reports @Ignore as a failure) until the Play AccessibilityService declaration is approved.
    @Test
    fun turningOnDoubleTapToLockWithAccessibilityEnabledStaysOnWithoutOpeningTheExplanation() {
        assumeTrue("double-tap to lock disabled", false)
        // Given accessibility access is already enabled
        var explanationOpened = false
        val settingsRepository = setContent(accessGranted = true, onNavigateToAccessibilityAccessExplanation = { explanationOpened = true })

        // When turning on "Double-tap to lock"
        composeRule.onNodeWithTag("gesture_double_tap_lock_toggle").performClick()
        awaitStored(settingsRepository) { it.doubleTapToLock }

        // Then the switch is on, the setting is stored and no explanation screen opens
        composeRule.onNodeWithTag("gesture_double_tap_lock_toggle").assertIsOn()
        assertTrue(runBlocking { settingsRepository.settings.first().doubleTapToLock })
        assertFalse(explanationOpened)
    }

    // Skipped (an assumption, since the runner reports @Ignore as a failure) until the Play AccessibilityService declaration is approved.
    @Test
    fun turningOnDoubleTapToLockWithoutAccessibilityOpensTheExplanationAndStaysOff() {
        assumeTrue("double-tap to lock disabled", false)
        // Given accessibility access has not been enabled
        var explanationOpened = false
        val settingsRepository = setContent(accessGranted = false, onNavigateToAccessibilityAccessExplanation = { explanationOpened = true })

        // When turning on "Double-tap to lock"
        composeRule.onNodeWithTag("gesture_double_tap_lock_toggle").performClick()
        awaitStored(settingsRepository) { it.doubleTapToLock }

        // Then the explanation screen is opened, the choice is kept, and the switch reads off until access is granted
        assertEquals(true, explanationOpened)
        assertTrue(runBlocking { settingsRepository.settings.first().doubleTapToLock })
        composeRule.onNodeWithTag("gesture_double_tap_lock_toggle").assertIsOff()
    }
}
