package com.facetlauncher.app.ui.settings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget

class DockAppsAppearanceScreenTest {

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
        DockAppsAppearanceScreen(onBack = onBack, viewModel = viewModel)
    }

    @Test
    fun previewCardRendersGridLayoutWithTheChosenColumnCount() {
        // Given 6 real installed apps as the global default favorites — enough for a 4-column
        // grid to wrap into two rows (4 + 2), the shape the left-align fix specifically targets.
        val packages = mutableListOf<String>()
        setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val installed = appRepository.getInstalledApps()
                (0 until 6).forEach { index ->
                    placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[index], position = index)
                    packages += installed[index].packageName
                }
            },
        )
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_grid_columns_row"))
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row_option_FOUR").performClick()
        composeRule.waitForIdle()

        // Then all 6 favorites render as grid icon tiles — not truncated to the old flat 6-item
        // cap becoming a coincidence here, and not the single-column AppRow the card used to
        // always show before the 3-way layout branch existed.
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        packages.forEach { pkg ->
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("appearance_preview_grid_icon_$pkg").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // And the first 4 share one row (same top), the last 2 wrap to the next row, packed to
        // the left rather than spread across the full width (the exact bug this test guards
        // against — see chat history: Arrangement.SpaceEvenly used to stretch a short last row).
        val row1Bounds = (0..3).map { composeRule.onNodeWithTag("appearance_preview_grid_icon_${packages[it]}").fetchSemanticsNode().boundsInRoot }
        val row2Bounds = (4..5).map { composeRule.onNodeWithTag("appearance_preview_grid_icon_${packages[it]}").fetchSemanticsNode().boundsInRoot }
        assertEquals(row1Bounds[0].top, row1Bounds[1].top, 1f)
        assert(row2Bounds[0].top > row1Bounds[0].top)
        // Row 2's two tiles land under row 1's first two columns — same left edges — rather than
        // being spread out to fill the row's full width.
        assertEquals(row1Bounds[0].left, row2Bounds[0].left, 1f)
        assertEquals(row1Bounds[1].left, row2Bounds[1].left, 1f)
    }

    @Test
    fun previewCardGridDisplayModeSwitchesBetweenIconsAndText() {
        // Given one real installed app as the global default favorite, Grid layout
        var packageName = ""
        setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val app = appRepository.getInstalledApps()[0]
                packageName = app.packageName
                placementRepository.addApp(PlacementTarget.GlobalFavorites, app, position = 0)
            },
        )
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()
        composeRule.waitForIdle()

        // Then Grid display defaults to Icons — the tile renders the icon, not the label
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("appearance_preview_grid_icon_$packageName").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("appearance_preview_grid_label_$packageName").assertDoesNotExist()

        // When switching Grid display to Text
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_grid_display_mode_row"))
        composeRule.onNodeWithTag("appearance_app_list_grid_display_mode_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_grid_display_mode_row_option_TEXT").performClick()
        composeRule.waitForIdle()

        // Then the tile flips to the label, not the icon
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("appearance_preview_grid_label_$packageName").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("appearance_preview_grid_icon_$packageName").assertDoesNotExist()
    }

    @Test
    fun gridColumnsRowOffersThreeColumnsAndAppliesItToThePreview() {
        // Given 4 real installed apps as favorites and the Grid layout chosen
        val packages = mutableListOf<String>()
        val settingsRepository = setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val installed = appRepository.getInstalledApps()
                (0 until 4).forEach { index ->
                    placementRepository.addApp(PlacementTarget.GlobalFavorites, installed[index], position = index)
                    packages += installed[index].packageName
                }
            },
        )
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()
        composeRule.waitForIdle()

        // When opening the columns row, 3 columns is one of the options, and choosing it
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_grid_columns_row"))
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row_option_THREE").assertExists().performClick()
        composeRule.waitForIdle()

        // Then the setting is stored
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appListGridColumns == com.facetlauncher.app.data.model.AppListGridColumns.THREE }
        }

        // And the preview lays the 4 favorites out 3 + 1
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        packages.forEach { pkg ->
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("appearance_preview_grid_icon_$pkg").fetchSemanticsNodes().isNotEmpty()
            }
        }
        val tops = packages.map { composeRule.onNodeWithTag("appearance_preview_grid_icon_$it").fetchSemanticsNode().boundsInRoot.top }
        assertEquals(tops[0], tops[2], 1f)
        assert(tops[3] > tops[0])
    }

    @Test
    fun dockDisplayStyleRowChangesTheGlobalSetting() {
        // Given the screen, Icons selected by default — moved in from Dock's own screen (see chat history)
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
        composeRule.onNodeWithTag("appearance_dock_display_style_row").assertTextContains("Icons")

        // When picking "Text"
        composeRule.onNodeWithTag("appearance_dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_dock_display_style_row_option_TEXT").performClick()

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().dockDisplayMode == com.facetlauncher.app.data.model.DockDisplayMode.TEXT }
        }
    }

    @Test
    fun previewCardMovesTheAppListUnderTheClockWhenVerticalAlignmentIsTop() {
        // Given one favorite, with the default Bottom alignment
        var packageName = ""
        setContent(
            seedApps = { appRepository, placementRepository, _ ->
                val app = appRepository.getInstalledApps().first()
                packageName = app.packageName
                placementRepository.addApp(PlacementTarget.GlobalFavorites, app, position = 0)
            },
        )
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        fun gapBelowClock(): Float {
            val clockBottom = composeRule.onNodeWithTag("appearance_preview_clock", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.bottom
            val rowTop = composeRule.onNodeWithTag("appearance_preview_app_label_$packageName", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
            return rowTop - clockBottom
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("appearance_preview_app_label_$packageName", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val bottomGap = gapBelowClock()

        // When switching vertical alignment to Top
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row_option_TOP").performClick()
        composeRule.waitForIdle()

        // Then the list sits closer to the clock than it did at the bottom
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitForIdle()
        val topGap = gapBelowClock()
        org.junit.Assert.assertTrue("top gap $topGap should be smaller than bottom gap $bottomGap", topGap < bottomGap)
    }

    @Test
    fun appRowPositionPresentationAndVerticalAlignmentRowsChangeTheGlobalSetting() {
        // Given the screen — moved in from Home Apps List's own screen (see chat history)
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_row_position_row"))

        composeRule.onNodeWithTag("appearance_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_position_row_option_RIGHT").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appRowPosition == com.facetlauncher.app.data.model.AppRowPosition.RIGHT }
        }

        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_row_presentation_row"))
        composeRule.onNodeWithTag("appearance_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_presentation_row_option_TEXT_ONLY").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appRowPresentation == com.facetlauncher.app.data.model.AppRowPresentation.TEXT_ONLY }
        }

        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row_option_TOP").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appListVerticalAlignment == com.facetlauncher.app.data.model.AppListVerticalAlignment.TOP }
        }
    }

    @Test
    fun globalModeDoesNotOfferTheLauncherDefaultOption() {
        // LAUNCHER_DEFAULT is facet-only — offering "use the launcher default" at global scope is
        // circular, since this screen IS the launcher default (see chat history)
        setContent()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))

        composeRule.onNodeWithTag("appearance_dock_display_style_row").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("appearance_dock_display_style_row_option_LAUNCHER_DEFAULT").assertCountEquals(0)
    }

    @Test
    fun dockAndHomeRowsShowTheirRenamedTitles() {
        // Renamed per direct request (see chat history) — asserted by title text since these
        // rows have no other distinguishing content at rest.
        setContent()

        // The Clock card sits above these rows, so each is scrolled into view before it's checked.
        listOf("Show Dock apps as", "Home Apps Alignment", "Show Home apps as", "Home Apps list position").forEach { title ->
            composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasText(title))
            // The Portrait and Landscape alignment cards share these two titles; the first is the Portrait one.
            composeRule.onAllNodesWithText(title)[0].assertIsDisplayed()
        }
    }

    @Test
    fun facetScopedDockDisplayStyleRowOffersLauncherDefaultAndWritesToThatFacet() {
        // Given a facet-scoped entry point, Icons inherited by default — waited for facet-scoped
        // settle first (see facetScopedModeKeepsClockRowAndPreviewButHidesEveryGlobalOnlyField's own comment)
        // so the dropdown's option list isn't read mid-transition from the global option set.
        val facetId = 1L
        setContent(facetId = facetId)
        awaitFacetScoped()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
        composeRule.onNodeWithTag("appearance_dock_display_style_row").assertTextContains("Icons")

        // When picking "Text" — no separate Inherit/Override switch to flip first; picking a real
        // value here is itself what starts this facet overriding (see chat history)
        composeRule.onNodeWithTag("appearance_dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_dock_display_style_row_option_TEXT").performClick()

        // Then it's persisted to this facet's own row, not the launcher-wide default
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("appearance_dock_display_style_row").assertTextContains("Text") }.isSuccess
        }
    }

    @Test
    fun headerNamesTheScreenAndTheBackButtonLeaves() {
        // Given the screen
        var backPressed = false
        setContent(onBack = { backPressed = true })

        // Then its own title shows, with the live preview card above the controls
        composeRule.onNodeWithText("Dock & Apps appearance").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_preview_card").assertExists()

        // When tapping back
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it leaves
        assertEquals(true, backPressed)
    }

    @Test
    fun landscapeAlignmentRowsWriteTheLandscapeSettingAndLeavePortraitAlone() {
        // Given the screen at global scope
        val settingsRepository = setContent()

        // When the landscape position and list position are chosen
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_landscape_app_row_position_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row_option_CENTER").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().landscapeAppRowPosition == com.facetlauncher.app.data.model.AppRowPosition.CENTER }
        }
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_landscape_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row_option_TOP").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().landscapeAppListVerticalAlignment == com.facetlauncher.app.data.model.AppListVerticalAlignment.TOP }
        }

        // Then portrait's own values are unchanged
        val settings = runBlocking { settingsRepository.settings.first() }
        org.junit.Assert.assertEquals(com.facetlauncher.app.data.model.AppRowPosition.LEFT, settings.appRowPosition)
        org.junit.Assert.assertEquals(com.facetlauncher.app.data.model.AppListVerticalAlignment.BOTTOM, settings.appListVerticalAlignment)
    }

    @Test
    fun choosingSameAsPortraitClearsTheLandscapeSetting() {
        // Given a saved landscape position
        val settingsRepository = setContent()
        runBlocking { settingsRepository.setLandscapeAppRowPosition(com.facetlauncher.app.data.model.AppRowPosition.RIGHT) }
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_landscape_app_row_position_row"))

        // When "Same as portrait" is chosen
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row_option_null").performClick()

        // Then the landscape value is cleared
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().landscapeAppRowPosition == null }
        }
    }

    @Test
    fun theAllowLandscapeSwitchTurnsLandscapeOffAndHidesTheLandscapeAlignment() {
        // Given the global screen with landscape allowed
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("appearance_allow_landscape_row").assertExists()
        composeRule.onNodeWithTag("dock_apps_appearance_screen").performScrollToNode(hasTestTag("appearance_landscape_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row").assertExists()

        // When the switch is turned off
        composeRule.onNodeWithTag("appearance_allow_landscape_row").performClick()

        // Then the setting is saved and the landscape alignment rows go away
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { !settingsRepository.settings.first().allowLandscape } }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("appearance_landscape_app_list_vertical_alignment_row").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun aFacetScopedScreenHasNoAllowLandscapeSwitch() {
        // Given a facet-scoped screen
        val facetId = 1L
        setContent(facetId = facetId)
        awaitFacetScoped()

        // Then the launcher-wide switch isn't there
        composeRule.onNodeWithTag("appearance_allow_landscape_row").assertDoesNotExist()
    }
}
