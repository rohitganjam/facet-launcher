package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppDrawerSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "app-drawer-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                AppDrawerSettingsViewModel(
                    settingsRepository,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context),
                )
            }
            FacetLauncherTheme {
                AppDrawerSettingsScreen(onBack = onBack, viewModel = viewModel)
            }
        }
    }

    /** Any row or tile in the preview, whatever app it holds (the preview shows the device's own apps once they load). */
    private fun previewItem(prefix: String) = SemanticsMatcher("test tag starts with $prefix") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }

    private fun previewItemCount(prefix: String) =
        composeRule.onAllNodes(previewItem(prefix), useUnmergedTree = true).fetchSemanticsNodes().size

    @Test
    fun previewShowsListRowsThenGridTilesWhenPresentationChangesToGrid() {
        // Given the screen with the default List presentation
        setContent()
        composeRule.onNodeWithTag("app_drawer_preview").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 3_000) { previewItemCount("drawer_app_row_") > 0 }
        assertEquals(0, previewItemCount("drawer_grid_tile_"))

        // When switching Presentation to Grid (Popup root-registration note — see below)
        composeRule.onNodeWithTag("drawer_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_presentation_row_option_GRID").performClick()

        // Then the preview swaps its rows for tiles
        composeRule.waitUntil(timeoutMillis = 3_000) { previewItemCount("drawer_grid_tile_") > 0 }
        assertEquals(0, previewItemCount("drawer_app_row_"))
    }

    @Test
    fun draggingThePreviewScrollsItsApps() {
        // Given the preview with its first row at some position
        setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) { previewItemCount("drawer_app_row_") > 0 }
        val firstRow = composeRule.onAllNodes(previewItem("drawer_app_row_"), useUnmergedTree = true)[0]
        val topBefore = firstRow.fetchSemanticsNode().boundsInRoot.top

        // When swiping up on the preview
        composeRule.onNodeWithTag("app_drawer_preview").performTouchInput { swipeUp() }

        // Then the list inside it scrolled, so the first row moved up
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(previewItem("drawer_app_row_"), useUnmergedTree = true)[0].fetchSemanticsNode().boundsInRoot.top < topBefore
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("app_drawer_settings_screen").performScrollToNode(hasTestTag("drawer_opacity_slider"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("App Drawer").assertIsDisplayed()
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
    fun gridSizeRowOnlyAppearsWhenGridPresentationIsSelected() {
        // Given the screen, List presentation selected by default
        setContent()
        composeRule.onNodeWithTag("drawer_grid_size_row").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_list_item_size_row").assertExists()
        composeRule.onNodeWithTag("show_drawer_icons_toggle").assertExists()
        composeRule.onNodeWithTag("show_drawer_labels_toggle").assertDoesNotExist()

        // When switching Presentation to Grid (the dropdown is a Popup, a separate window that
        // doesn't always register with the test framework's root registry by the time
        // performClick() returns; waitForIdle() settles that before querying its content)
        composeRule.onNodeWithTag("drawer_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_presentation_row_option_GRID").performClick()

        // Then Grid size and Show labels appear, and Show icons/List item size are hidden — poll
        // rather than trust a single waitForIdle() caught the recomposition (same class of gap as
        // the StateFlow-vs-waitForIdle lesson elsewhere in this codebase, just triggered by a
        // Popup-driven state change instead of async I/O)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("drawer_grid_size_row").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("drawer_grid_size_row").assertExists()
        composeRule.onNodeWithTag("show_drawer_labels_toggle").assertExists()
        composeRule.onNodeWithTag("show_drawer_icons_toggle").assertDoesNotExist()
        composeRule.onNodeWithTag("drawer_list_item_size_row").assertDoesNotExist()
    }

    @Test
    fun drawerOpacitySliderShowsTheCurrentPercentage() {
        // Given the screen with the documented 60% default, scrolled to the slider
        setContent()
        composeRule.onNodeWithTag("app_drawer_settings_screen").performScrollToNode(hasTestTag("drawer_opacity_slider"))

        // Then the slider's label reflects it
        composeRule.onNodeWithText("60%").assertExists()
    }

    @Test
    fun searchBarPositionDropdownSwitchesBetweenTopAndBottom() {
        // Given the screen, Top selected by default
        setContent()
        composeRule.onNodeWithText("Top").assertExists()

        // When opening the dropdown and choosing "Bottom" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("search_bar_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("search_bar_position_row_option_BOTTOM").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Bottom").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun folderDisplayModeDropdownDefaultsToDoNotShowAndSwitchesToShowFirst() {
        // Given the screen, scrolled to the new row — "Do not show" selected by default
        setContent()
        composeRule.onNodeWithTag("app_drawer_settings_screen").performScrollToNode(hasTestTag("drawer_folder_display_mode_row"))
        composeRule.onNodeWithText("Do not show").assertExists()

        // When opening the dropdown and choosing "Show folders first" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("drawer_folder_display_mode_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_folder_display_mode_row_option_SHOW_FIRST").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Show folders first").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun recentlyInstalledPositionDropdownDefaultsToShowFirstAndSwitchesToDoNotShow() {
        // Given the screen, scrolled to the new row — "Show recents first" selected by default
        setContent()
        composeRule.onNodeWithTag("app_drawer_settings_screen").performScrollToNode(hasTestTag("recently_installed_position_row"))
        composeRule.onNodeWithText("Show recents first").assertExists()

        // When opening the dropdown and choosing "Do not show" (Popup root-registration note — see above)
        composeRule.onNodeWithTag("recently_installed_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("recently_installed_position_row_option_DO_NOT_SHOW").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Do not show").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
