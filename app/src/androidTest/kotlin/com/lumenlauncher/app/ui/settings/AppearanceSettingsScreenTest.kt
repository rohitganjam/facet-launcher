package com.lumenlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class AppearanceSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "appearance-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                AppearanceSettingsViewModel(settingsRepository)
            }
            LumenLauncherTheme {
                AppearanceSettingsScreen(onBack = onBack, viewModel = viewModel)
            }
        }
        return settingsRepository
    }

    @Test
    fun headerStaysVisibleAfterScrollingToTheBottom() {
        // Given the screen, scrolled all the way down
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun themeModeDropdownSwitchesBetweenLightDarkAndSystem() {
        // Given the screen, System selected by default
        setContent()
        composeRule.onNodeWithTag("theme_mode_dropdown").assertTextContains("System")

        // When opening the dropdown and choosing "Dark"
        composeRule.onNodeWithTag("theme_mode_dropdown").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("theme_mode_dropdown_option_DARK").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Dark").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun accentSwatchGridOnlyAppearsWhenBasicColorsIsSelectedAndPickingOneShowsItChecked() {
        // Given the screen, "Wallpaper colors" selected by default
        setContent()
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()

        // When switching to "Basic colors"
        composeRule.onNodeWithTag("accent_source_basic").performClick()

        // Then the swatch grid appears, defaulting to the first swatch (Blue) selected
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("accent_swatch_grid").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Blue").assertExists()

        // When picking a different swatch (Teal)
        composeRule.onNodeWithTag("accent_swatch_TEAL").performClick()

        // Then that swatch shows as checked instead — poll, since the pick round-trips through
        // DataStore before the grid re-renders (same async-write lesson as elsewhere)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithContentDescription("Teal").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun iconRenderModeRowChangesTheSetting() {
        // Given the screen, "System default" selected by default (F11)
        setContent()
        composeRule.onNodeWithText("System default").assertExists()

        // When picking "Monochrome (Accent)"
        composeRule.onNodeWithTag("appearance_icons_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_icons_row_option_MONOCHROME_ACCENT").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Monochrome (Accent)").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Monochrome (Accent)").assertExists()
    }

    @Test
    fun launcherFontRowChangesTheSetting() {
        // Given the screen, "Font" row, System selected by default — scoped to this row's own
        // tag, not a global text search, since the "Launcher theme" dropdown also defaults to
        // "System" (see chat history)
        setContent()
        composeRule.onNodeWithTag("appearance_font_row").assertTextContains("System")

        // When picking "Manrope"
        composeRule.onNodeWithTag("appearance_font_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_font_row_option_MANROPE").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_font_row").assertTextContains("Manrope") }.isSuccess
        }
    }

    @Test
    fun appLabelColorRowChangesTheSetting() {
        // Given the screen, "App label color" row, Theme selected by default — scoped to this
        // row's own tag, consistent with the Font row above
        setContent()
        composeRule.onNodeWithTag("appearance_app_label_color_row").assertTextContains("Theme")

        // When picking "Theme Inverted"
        composeRule.onNodeWithTag("appearance_app_label_color_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_label_color_row_option_THEME_INVERTED").performClick()

        // Then the row reflects the new selection
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_app_label_color_row").assertTextContains("Theme Inverted") }.isSuccess
        }
    }

    @Test
    fun homeAppsFontWeightSliderChangesTheSetting() {
        // Given the screen, Regular weight by default
        val settingsRepository = setContent()

        // When dragging the Home Apps weight slider to its last stop (Semi Bold)
        composeRule.onNodeWithTag("appearance_font_weight_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontWeightOption.entries.size - 1).toFloat()) }

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().homeAppsFontWeight == FontWeightOption.SEMI_BOLD }
        }
    }
}
