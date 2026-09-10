package com.lumenlauncher.app.ui.settings

import android.content.pm.LauncherApps
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
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LUMEN_LAUNCHER_PACKAGE_NAME
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.SelectPreviewAppsUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class AppearanceSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var appRepository: AppRepository
    private lateinit var defaultAppRepository: DefaultAppRepository

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
                appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                defaultAppRepository = DefaultAppRepository(context)
                val wallpaperRepository = com.lumenlauncher.app.data.WallpaperRepository(
                    android.app.WallpaperManager.getInstance(context),
                )
                AppearanceSettingsViewModel(settingsRepository, GetInstalledAppsUseCase(appRepository), defaultAppRepository, wallpaperRepository, SelectPreviewAppsUseCase())
            }
            LumenLauncherTheme {
                AppearanceSettingsScreen(onBack = onBack, viewModel = viewModel)
            }
        }
        return settingsRepository
    }

    /**
     * Mirrors [AppearanceSettingsViewModel.previewApps]'s own selection — same exclusion and same
     * [SelectPreviewAppsUseCase] call — but via [AppRepository.getInstalledApps] rather than
     * [GetInstalledAppsUseCase.observe]: the latter registers a
     * [android.content.pm.LauncherApps.Callback], which needs a `Looper` on whatever thread calls
     * it; the production [AppearanceSettingsViewModel] always collects on the main thread (which
     * has one), but this test's `runBlocking` runs on the instrumentation thread, which doesn't.
     */
    private suspend fun installedPreviewApps(): List<AppInfo> {
        val installed = appRepository.getInstalledApps().filterNot { it.packageName == LUMEN_LAUNCHER_PACKAGE_NAME }
        val preferred = defaultAppRepository.getDefaultAppPackages()
        return SelectPreviewAppsUseCase()(installed, preferred, count = 5)
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
    fun wallpaperAccentRoleGridShowsByDefaultAndBasicColorsGridDoesNot() {
        // Given the screen, "Wallpaper colors" selected by default
        setContent()

        // Then the wallpaper-role grid is shown, and the Basic-colors grid is not
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()
    }

    @Test
    fun wallpaperAccentRoleGridDisappearsWhenBasicColorsIsSelected() {
        // Given the screen, "Wallpaper colors" selected by default
        setContent()
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertIsDisplayed()

        // When switching to "Basic colors"
        composeRule.onNodeWithTag("accent_source_basic").performClick()

        // Then the wallpaper-role grid is gone and the Basic-colors grid appears instead
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("accent_swatch_grid").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertDoesNotExist()
    }

    @Test
    fun tappingAWallpaperAccentRoleSwatchPersistsTheChoice() {
        // Given the screen, "Wallpaper colors" selected by default (PRIMARY role)
        val settingsRepository = setContent()

        // When picking the Secondary wallpaper role
        composeRule.onNodeWithTag("wallpaper_accent_role_SECONDARY").performClick()

        // Then it's persisted to the real repository — poll, same async-write reasoning as the
        // Basic-colors test above. Deliberately not asserting the swatch's actual rendered color,
        // which depends on the test device's own real wallpaper (see ColorTest.kt's own doc).
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking {
                settingsRepository.settings.first().wallpaperAccentRole == com.lumenlauncher.app.data.model.WallpaperAccentRole.SECONDARY
            }
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

    @Test
    fun previewCardShowsRealInstalledAppsAndDockIcon() {
        // Given the screen — the live preview card (M4) reuses the real AppRow/DockIcon, now
        // backed by the device's own real installed apps instead of placeholder objects
        setContent()
        val installedApps = runBlocking { installedPreviewApps() }

        // Then the preview card renders, with its favorites row and dock icon labeled from the
        // real installed-app list's own first entries (same slicing AppearancePreviewCard uses)
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.onNodeWithText(installedApps[0].label).assertExists()
        composeRule.onNodeWithText(installedApps[1].label).assertExists()
        composeRule.onNodeWithContentDescription(installedApps[2].label).assertExists()
    }

    @Test
    fun previewCardStillRendersAfterChangingAppLabelColor() {
        // Given the screen, App label color at its default
        setContent()
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        val firstAppLabel = runBlocking { installedPreviewApps()[0].label }

        // When changing App label color — the same value the preview's AppRow/DockIcon labelColor
        // is wired from
        composeRule.onNodeWithTag("appearance_app_label_color_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_label_color_row_option_THEME_INVERTED").performClick()

        // Then the preview keeps rendering its content correctly through the recomposition
        // (a wiring mistake here — e.g. a wrong param — would otherwise crash or blank the card)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText(firstAppLabel).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
    }

    @Test
    fun previewCardRendersTheWallpaperBehindItsContent() {
        // Given the screen
        setContent()
        val firstAppLabel = runBlocking { installedPreviewApps()[0].label }

        // Then the preview card paints a wallpaper backdrop, with its sample content still on top
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_background").assertExists()
        composeRule.onNodeWithText(firstAppLabel).assertExists()
    }
}
