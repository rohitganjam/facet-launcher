package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        composeRule.onNodeWithText("Swipe right").assertExists()
        composeRule.onNodeWithText("Open the Hub").assertExists()
        composeRule.onNodeWithText("Swipe left").assertExists()
        composeRule.onNodeWithText("Switch facets").assertExists()
        composeRule.onNodeWithText("Swipe down").assertExists()
        composeRule.onNodeWithText("Open the notification shade").assertExists()
        composeRule.onNodeWithText("Long-press empty space").assertExists()

        // And both optional gestures have a switch, off by default
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").assertIsOff()
        composeRule.onNodeWithTag("gesture_double_tap_lock_toggle").assertIsOff()
    }

    @Test
    fun turningOnHomeButtonOpensSearchPersistsIt() {
        // Given the gestures screen
        val settingsRepository = setContent()

        // When turning on "Home button opens search"
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").performClick()
        awaitStored(settingsRepository) { it.homeOpensSearch }

        // Then the switch is on and the setting is stored
        composeRule.onNodeWithTag("gesture_home_opens_search_toggle").assertIsOn()
        assertTrue(runBlocking { settingsRepository.settings.first().homeOpensSearch })
    }

    @Test
    fun turningOnDoubleTapToLockWithAccessibilityEnabledStaysOnWithoutOpeningTheExplanation() {
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

    @Test
    fun turningOnDoubleTapToLockWithoutAccessibilityOpensTheExplanationAndStaysOff() {
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
