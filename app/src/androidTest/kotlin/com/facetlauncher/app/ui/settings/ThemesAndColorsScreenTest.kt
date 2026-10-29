package com.facetlauncher.app.ui.settings

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import androidx.test.filters.SdkSuppress
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget

class ThemesAndColorsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val host = AppearanceScreenTestHost(composeRule)

    private fun awaitFacetScoped() = host.awaitFacetScoped()

    private fun setContent(
        onBack: () -> Unit = {},
        facetId: Long? = null,
        calendarGranted: Boolean = false,
        notificationAccessGranted: Boolean = true,
        calendarEvents: List<CalendarEvent> = emptyList(),
        seed: suspend (FacetRepository, Long?) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, PlacementRepository, Long?) -> Unit = { _, _, _ -> },
    ): SettingsRepository = host.setContent(facetId, calendarGranted, notificationAccessGranted, calendarEvents, seed, seedApps) { viewModel ->
        ThemesAndColorsScreen(onBack = onBack, viewModel = viewModel)
    }

    @Test
    fun themeModeDropdownSwitchesBetweenLightDarkAndSystem() {
        // Given the screen, System selected by default — now in the separate "General" card,
        // below the "Dock & Home" and "Clock" cards (see chat history), so it needs scrolling into
        // view first.
        setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("theme_mode_dropdown"))
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
    fun statusBarIconsDropdownDefaultsToDefaultAndSwitchesToDark() {
        // Given the screen, "Default" selected by default, in the Theme card under "Launcher theme"
        setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("appearance_system_bar_icons_row"))
        composeRule.onNodeWithTag("appearance_system_bar_icons_row").assertTextContains("Default")

        // When opening the dropdown and choosing "Dark"
        composeRule.onNodeWithTag("appearance_system_bar_icons_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_system_bar_icons_row_option_DARK").performClick()

        // Then the row's own current-value label reflects it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_system_bar_icons_row").assertTextContains("Dark") }.isSuccess
        }
    }

    @Test
    // Wallpaper colours (and so the source tabs) exist from Android 12; older devices skip these rather than run them
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun accentSwatchGridOnlyAppearsWhenBasicColorsIsSelectedAndPickingOneShowsItChecked() {
        // Given the screen, "Wallpaper colors" selected by default — now in the "General" card,
        // below "Dock & Home"/"Clock" (see chat history), so it needs scrolling into view first.
        setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("accent_source_basic"))
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()

        // When switching to "Basic colors"
        composeRule.onNodeWithTag("accent_source_basic").performClick()

        // Then the swatch grid appears, defaulting to the first swatch (Blue) selected
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("accent_swatch_grid").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Blue").assertExists()

        // When picking a different swatch (Teal) — its row sits below the fold, so scroll it in first
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("accent_swatch_TEAL"))
        composeRule.onNodeWithTag("accent_swatch_TEAL").performClick()

        // Then that swatch shows as checked instead — poll, since the pick round-trips through
        // DataStore before the grid re-renders (same async-write lesson as elsewhere)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithContentDescription("Teal").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun withoutSourceTabsOnlyThePresetSwatchesShowAndPickingOneReportsIt() {
        // Given the accent section as Android 11 shows it (no wallpaper colours, so no source tabs)
        var picked: com.facetlauncher.app.ui.theme.AccentSwatch? = null
        composeRule.setContent {
            FacetLauncherTheme {
                AccentColorSection(
                    accentFromSystem = true,
                    customAccentSwatch = null,
                    wallpaperAccentRole = WallpaperAccentRole.PRIMARY,
                    onAccentFromSystemChange = {},
                    onCustomAccentSwatchChange = { picked = it },
                    onWallpaperAccentRoleChange = {},
                    showSourceTabs = false,
                )
            }
        }

        // Then the presets show with Blue checked by default, and neither tab nor wallpaper grid exists
        composeRule.onNodeWithTag("accent_swatch_grid").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Blue").assertExists()
        composeRule.onNodeWithTag("accent_source_wallpaper").assertDoesNotExist()
        composeRule.onNodeWithTag("accent_source_basic").assertDoesNotExist()
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertDoesNotExist()

        // When tapping Teal
        composeRule.onNodeWithTag("accent_swatch_TEAL").performClick()

        // Then it is reported
        assertEquals(com.facetlauncher.app.ui.theme.AccentSwatch.TEAL, picked)
    }

    @Test
    fun withSourceTabsBothTabsShow() {
        // Given the accent section as Android 12+ shows it
        composeRule.setContent {
            FacetLauncherTheme {
                AccentColorSection(
                    accentFromSystem = true,
                    customAccentSwatch = null,
                    wallpaperAccentRole = WallpaperAccentRole.PRIMARY,
                    onAccentFromSystemChange = {},
                    onCustomAccentSwatchChange = {},
                    onWallpaperAccentRoleChange = {},
                    showSourceTabs = true,
                )
            }
        }

        // Then both source tabs are there
        composeRule.onNodeWithTag("accent_source_wallpaper").assertIsDisplayed()
        composeRule.onNodeWithTag("accent_source_basic").assertIsDisplayed()
    }

    @Test
    // Wallpaper colours (and so the source tabs) exist from Android 12; older devices skip these rather than run them
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun wallpaperAccentRoleGridShowsByDefaultAndBasicColorsGridDoesNot() {
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view (see
        // themeModeDropdownSwitchesBetweenLightDarkAndSystem's own comment)
        setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))

        // Then the wallpaper-role grid is shown, and the Basic-colors grid is not
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()
    }

    @Test
    // Wallpaper colours (and so the source tabs) exist from Android 12; older devices skip these rather than run them
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun wallpaperAccentRoleGridDisappearsWhenBasicColorsIsSelected() {
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view
        setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))
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
    // Wallpaper colours (and so the source tabs) exist from Android 12; older devices skip these rather than run them
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun tappingAWallpaperAccentRoleSwatchPersistsTheChoice() {
        // Given the screen, "Wallpaper colors" selected by default (PRIMARY role) — scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("themes_colors_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_SECONDARY"))

        // When picking the Secondary wallpaper role
        composeRule.onNodeWithTag("wallpaper_accent_role_SECONDARY").performClick()

        // Then it's persisted to the real repository — poll, same async-write reasoning as the
        // Basic-colors test above. Deliberately not asserting the swatch's actual rendered color,
        // which depends on the test device's own real wallpaper (see ColorTest.kt's own doc).
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking {
                settingsRepository.settings.first().wallpaperAccentRole == com.facetlauncher.app.data.model.WallpaperAccentRole.SECONDARY
            }
        }
    }

    @Test
    fun headerNamesTheScreenAndTheBackButtonLeaves() {
        // Given the screen
        var backPressed = false
        setContent(onBack = { backPressed = true })

        // Then its own title shows, with the live preview card above the controls
        composeRule.onNodeWithText("Themes and colors").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_preview_card").assertExists()

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it leaves
        assertEquals(true, backPressed)
    }
}
