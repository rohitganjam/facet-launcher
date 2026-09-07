package com.lumenlauncher.app.ui.settings

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeAppsListSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}, onEditDefaultFavorites: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "home-apps-list-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                HomeAppsListSettingsViewModel(settingsRepository, defaultFavoriteAppRepository)
            }
            LumenLauncherTheme {
                HomeAppsListSettingsScreen(onBack = onBack, onEditDefaultFavorites = onEditDefaultFavorites, viewModel = viewModel)
            }
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Home Apps List").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
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
    fun appRowPositionDropdownSwitchesBetweenLeftAndRightAndAppearsAboveListContent() {
        // Given the screen, Left selected by default
        setContent()
        composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Left")

        // Then it renders above the "Default App list content" row within the same card
        val positionTop = composeRule.onNodeWithTag("default_app_row_position_row").fetchSemanticsNode().boundsInRoot.top
        val listContentTop = composeRule.onNodeWithTag("default_list_content_row").fetchSemanticsNode().boundsInRoot.top
        assert(positionTop < listContentTop)

        // When opening the dropdown and choosing "Right" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Right") }.isSuccess
        }
    }

    @Test
    fun appRowPresentationDropdownSwitchesBetweenTheThreeOptions() {
        // Given the screen, "Icon & Text" selected by default
        setContent()
        composeRule.onNodeWithTag("default_app_row_presentation_row").assertTextContains("Icon & Text")

        // When opening the dropdown and choosing "Text Only" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("default_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_presentation_row_option_TEXT_ONLY").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_presentation_row").assertTextContains("Text Only") }.isSuccess
        }
    }

    @Test
    fun appListVerticalAlignmentDropdownSwitchesBetweenTopAndBottom() {
        // Given the screen, "Bottom" selected by default
        setContent()
        composeRule.onNodeWithTag("app_list_vertical_alignment_row").assertTextContains("Bottom")

        // When opening the dropdown and choosing "Top"
        composeRule.onNodeWithTag("app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_list_vertical_alignment_row_option_TOP").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_list_vertical_alignment_row").assertTextContains("Top") }.isSuccess
        }
    }

    @Test
    fun defaultFavoritesRowIsClickable() {
        // Given the screen
        var navigated = false
        setContent(onEditDefaultFavorites = { navigated = true })

        // When tapping "Default favorites"
        composeRule.onNodeWithTag("default_favorites_row").performClick()

        // Then its callback fires
        assertEquals(true, navigated)
    }
}
