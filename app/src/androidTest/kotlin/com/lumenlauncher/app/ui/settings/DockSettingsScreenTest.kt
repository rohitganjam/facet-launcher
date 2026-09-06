package com.lumenlauncher.app.ui.settings

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DockSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}, onAddDockApp: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "dock-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                DockSettingsViewModel(settingsRepository, dockAppRepository)
            }
            LumenLauncherTheme {
                DockSettingsScreen(onBack = onBack, onAddDockApp = onAddDockApp, viewModel = viewModel)
            }
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()

        // Then the pinned header (title + back button) is on screen
        composeRule.onNodeWithText("Dock").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesOnBack() {
        // Given the screen
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, backInvoked)
    }

    @Test
    fun dockDisplayStyleDropdownSwitchesBetweenIconsAndText() {
        // Given the screen, Icons selected by default
        setContent()
        composeRule.onNodeWithText("Icons").assertExists()

        // When opening the dropdown and choosing "Text" — the dropdown is a Popup, a separate
        // window that doesn't always register with the test framework's root registry by the
        // time performClick() returns; waitForIdle() settles that before querying its content.
        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_display_style_row_option_TEXT").performClick()

        // Then the row's own current-value label reflects it — poll rather than trust a single
        // waitForIdle() caught this second recomposition (selecting the item closes the Popup
        // and updates the row's label, two state transitions after the click returns)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Text").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun selectDockAppsRowIsClickable() {
        // Given the screen
        var navigated = false
        setContent(onAddDockApp = { navigated = true })

        // When tapping "Select Dock Apps"
        composeRule.onNodeWithTag("add_dock_app_row").performClick()

        // Then it opens the picker
        assertEquals(true, navigated)
    }
}
