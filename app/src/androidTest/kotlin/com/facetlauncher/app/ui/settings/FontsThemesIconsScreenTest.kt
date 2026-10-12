package com.facetlauncher.app.ui.settings

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class FontsThemesIconsScreenTest {

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
        seedApps: suspend (AppRepository, FavoriteAppRepository, DefaultFavoriteAppRepository, DockAppRepository, FacetDockAppRepository, Long?) -> Unit = { _, _, _, _, _, _ -> },
    ): SettingsRepository = host.setContent(facetId, calendarGranted, notificationAccessGranted, calendarEvents, seed, seedApps) { viewModel ->
        FontsThemesIconsScreen(onBack = onBack, viewModel = viewModel)
    }

    @Test
    fun themeModeDropdownSwitchesBetweenLightDarkAndSystem() {
        // Given the screen, System selected by default — now in the separate "General" card,
        // below the "Dock & Home" and "Clock" cards (see chat history), so it needs scrolling into
        // view first.
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("theme_mode_dropdown"))
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
    fun systemBarIconsDropdownDefaultsToMatchThemeAndSwitchesToDark() {
        // Given the screen, "Match theme" selected by default, in the General card under "Launcher theme"
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_system_bar_icons_row"))
        composeRule.onNodeWithTag("appearance_system_bar_icons_row").assertTextContains("Match theme")

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
    fun accentSwatchGridOnlyAppearsWhenBasicColorsIsSelectedAndPickingOneShowsItChecked() {
        // Wallpaper colours (and so the source tabs) exist from Android 12
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        // Given the screen, "Wallpaper colors" selected by default — now in the "General" card,
        // below "Dock & Home"/"Clock" (see chat history), so it needs scrolling into view first.
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("accent_source_basic"))
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
    fun wallpaperAccentRoleGridShowsByDefaultAndBasicColorsGridDoesNot() {
        // Wallpaper colours (and so the source tabs) exist from Android 12
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view (see
        // themeModeDropdownSwitchesBetweenLightDarkAndSystem's own comment)
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))

        // Then the wallpaper-role grid is shown, and the Basic-colors grid is not
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()
    }

    @Test
    fun wallpaperAccentRoleGridDisappearsWhenBasicColorsIsSelected() {
        // Wallpaper colours (and so the source tabs) exist from Android 12
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))
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
        // Wallpaper colours (and so the source tabs) exist from Android 12
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        // Given the screen, "Wallpaper colors" selected by default (PRIMARY role) — scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_SECONDARY"))

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
    fun iconRenderModeRowChangesTheSetting() {
        // Given the screen, "System default" selected by default (F11) — scrolled into view
        setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_icons_row"))
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
    fun iconShapePickerPersistsTheSelectedShape() {
        // Given the screen, with the picker scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

        // When picking Circle
        composeRule.onNodeWithTag("icon_shape_CIRCLE").performClick()

        // Then it's selected and persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().iconShape == com.facetlauncher.app.data.model.IconShape.CIRCLE }
        }
        composeRule.onNodeWithTag("icon_shape_CIRCLE").assertIsSelected()
    }

    @Test
    fun launcherFontRowChangesTheSetting() {
        // Given the screen, "Font" row, System selected by default — scoped to this row's own
        // tag, not a global text search, since the "Launcher theme" dropdown also defaults to
        // "System" (see chat history)
        setContent()
        // The new "look fields" card (dock display style/app row position/presentation/vertical
        // alignment) pushed this row further down — see chat history: those moved in from Dock's/
        // Home Apps List's own screens — so it needs an explicit scroll into view now, otherwise
        // its dropdown popup can open off the visible viewport.
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_font_row"))
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
        // Given the screen, "Font Color" row, Theme selected by default — scoped to this
        // row's own tag, consistent with the Font row above
        setContent()
        // See launcherFontRowChangesTheSetting's own comment — this row moved further down too.
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
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
        // Given the screen, Regular weight by default — scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_font_weight_slider_control"))

        // When dragging the Home Apps weight slider to its last stop (Semi Bold)
        composeRule.onNodeWithTag("appearance_font_weight_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontWeightOption.entries.size - 1).toFloat()) }

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().homeAppsFontWeight == FontWeightOption.SEMI_BOLD }
        }
    }

    @Test
    fun fontSizeSliderChangesTheSetting() {
        // Given the screen, Default text size by default — scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_font_size_slider_control"))

        // When dragging the Text Size slider to its last stop (Huge)
        composeRule.onNodeWithTag("appearance_font_size_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontScaleOption.entries.size - 1).toFloat()) }

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().fontScaleOption == FontScaleOption.HUGE }
        }
    }

    @Test
    fun previewCardStillRendersAfterChangingAppLabelColor() {
        // Given the screen, App label color at its default, one real (installed) favorite seeded
        var favoriteLabel = ""
        setContent(
            seedApps = { appRepository, _, defaultFavoriteAppRepository, _, _, _ ->
                val app = appRepository.getInstalledApps()[0]
                favoriteLabel = app.label
                defaultFavoriteAppRepository.addFavorite(app, position = 0)
            },
        )
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()

        // When changing App label color — the same value the preview's AppRow/DockIcon labelColor
        // is wired from. See launcherFontRowChangesTheSetting's own comment — this row moved
        // further down now, so it needs scrolling into view first.
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
        composeRule.onNodeWithTag("appearance_app_label_color_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_label_color_row_option_THEME_INVERTED").performClick()
        composeRule.waitForIdle()

        // Then the preview keeps rendering its content correctly through the recomposition (a
        // wiring mistake here — e.g. a wrong param — would otherwise crash or blank the card).
        // The preview card is now far above the row just edited (see chat history — the "Dock &
        // Home"/"Clock" cards sit between them), so it's scrolled out of the LazyColumn's composed
        // range; scroll back up to it before asserting, rather than waiting on a node that's been
        // disposed entirely.
        composeRule.onNodeWithTag("fonts_themes_icons_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText(favoriteLabel).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
    }

    @Test
    fun headerNamesTheScreenAndTheBackButtonLeaves() {
        // Given the screen
        var backPressed = false
        setContent(onBack = { backPressed = true })

        // Then its own title shows, with the live preview card above the controls
        composeRule.onNodeWithText("Fonts, themes and icons").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_preview_card").assertExists()

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it leaves
        assertEquals(true, backPressed)
    }
}
