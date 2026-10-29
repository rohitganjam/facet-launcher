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

class FontsScreenTest {

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
        FontsScreen(onBack = onBack, viewModel = viewModel)
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
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_font_row"))
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
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
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
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_font_weight_slider_control"))

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
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_font_size_slider_control"))

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
            seedApps = { appRepository, placementRepository, _ ->
                val app = appRepository.getInstalledApps()[0]
                favoriteLabel = app.label
                placementRepository.addApp(PlacementTarget.GlobalFavorites, app, position = 0)
            },
        )
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()

        // When changing App label color — the same value the preview's AppRow/DockIcon labelColor
        // is wired from. See launcherFontRowChangesTheSetting's own comment — this row moved
        // further down now, so it needs scrolling into view first.
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
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
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText(favoriteLabel).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
    }

    @Test
    fun fontsCardHasTheLauncherFontRowAndASamplePreview() {
        // Given the screen
        setContent()

        // Then the font style row and the sample text are there
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("appearance_font_row"))
        composeRule.onNodeWithText("Launcher Font Style").assertIsDisplayed()
        composeRule.onNodeWithTag("fonts_screen").performScrollToNode(hasTestTag("font_preview_card"))
        composeRule.onNodeWithTag("font_preview_card").assertIsDisplayed()
    }

    @Test
    fun headerNamesTheScreenAndTheBackButtonLeaves() {
        // Given the screen
        var backPressed = false
        setContent(onBack = { backPressed = true })

        // Then its own title shows, with the live preview card above the controls
        composeRule.onNodeWithText("Fonts").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_preview_card").assertExists()

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it leaves
        assertEquals(true, backPressed)
    }
}
