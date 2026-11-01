package com.facetlauncher.app.ui.settings

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.IconPackOption
import com.facetlauncher.app.domain.IconPickerState
import com.facetlauncher.app.domain.ObserveIconPickerUseCase
import com.facetlauncher.app.domain.SelectIconSamplesUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import com.facetlauncher.app.ui.home.LocalAllowLandscape
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IconsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val host = AppearanceScreenTestHost(composeRule)

    private fun setContent(): SettingsRepository =
        host.setContent(null, false, true, emptyList(), { _, _ -> }, { _, _, _ -> }) { viewModel ->
            val context = LocalContext.current
            val iconsViewModel = remember { iconsViewModel(context) }
            IconsScreen(onBack = {}, viewModel = viewModel, iconsViewModel = iconsViewModel)
        }

    /** The real picker over the host's repositories; the emulator has no icon packs installed. */
    private fun iconsViewModel(context: Context): IconsViewModel {
        val iconPacks = IconPackRepository(context, host.settingsRepository)
        val observeIconPicker = ObserveIconPickerUseCase(
            host.observePlacedItems,
            GetInstalledAppsUseCase(host.appRepository),
            host.appRepository,
            iconPacks,
            DefaultAppRepository(context),
            SelectIconSamplesUseCase(SelectPreviewAppsUseCase()),
        )
        return IconsViewModel(observeIconPicker, host.settingsRepository, iconPacks)
    }

    /** The screen's content alone, over [picker] — packs can't be installed on the emulator, so their cards are tested this way. */
    private fun setPackContent(settings: LauncherSettings, picker: IconPickerState, onSelectPack: (String) -> Unit = {}) {
        composeRule.setContent {
            CompositionLocalProvider(LocalAllowLandscape provides false) {
                FacetLauncherTheme {
                    IconsContent(
                        settings = settings,
                        uiState = AppearanceSettingsUiState(settings = settings),
                        picker = picker,
                        homeWallpaper = HomeWallpaper.Unavailable,
                        onBack = {},
                        onSelectBuiltIn = {},
                        onIconRenderModeChange = {},
                        onSelectPack = onSelectPack,
                        onPackFallbackChange = {},
                        onOpenPack = {},
                        onFindPacks = {},
                        onIconShapeChange = {},
                    )
                }
            }
        }
    }

    /** The colour and background rows sit below the pack row, off a phone's first screen. */
    private fun openDropdown(tag: String) {
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag(tag))
        composeRule.onNodeWithTag(tag).performClick()
    }

    private val twoPacks = IconPickerState(
        packs = listOf(
            IconPackOption(IconPackInfo("pack.alpha", "Alpha", icon = null), coveredApps = 3),
            IconPackOption(IconPackInfo("pack.beta", "Beta", icon = null)),
        ),
        totalApps = 10,
        isLoading = false,
    )

    @Test
    fun withNoPacksInstalledTheScreenOffersToFindSome() {
        // Given no icon packs on the device
        var searched = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAllowLandscape provides false) {
                FacetLauncherTheme {
                    IconsContent(
                        settings = LauncherSettings(), uiState = AppearanceSettingsUiState(), picker = IconPickerState(isLoading = false),
                        homeWallpaper = HomeWallpaper.Unavailable, onBack = {}, onSelectBuiltIn = {}, onIconRenderModeChange = {}, onSelectPack = {},
                        onPackFallbackChange = {}, onOpenPack = {}, onFindPacks = { searched = true }, onIconShapeChange = {},
                    )
                }
            }
        }

        // When tapping the offer to find some
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag("icons_find_packs"))
        composeRule.onNodeWithTag("icons_no_packs").assertIsDisplayed()
        composeRule.onNodeWithTag("icons_find_packs").performClick()

        // Then the pack search opens
        assertEquals(true, searched)
    }

    @Test
    fun pickingABuiltInStyleUnappliesAnIconPack() {
        // Given an icon pack applied
        val settingsRepository = setContent()
        runBlocking { settingsRepository.setIconPack("com.example.pack") }
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodes(hasTestTag("icons_applied_pack")).fetchSemanticsNodes().isNotEmpty() }

        // When picking the glyph style
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()

        // Then the pack is cleared and the style stored
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { settingsRepository.settings.first().iconPackPackage == null } }
    }

    @Test
    fun packCardsShowTheAppliedPackAndPickAnother() {
        // Given two packs, Alpha applied
        var picked: String? = null
        setPackContent(LauncherSettings(iconPackPackage = "pack.alpha"), twoPacks, onSelectPack = { picked = it })

        // Then Alpha is selected, no built-in style is, and Alpha's coverage and fallback rows show
        composeRule.onNodeWithTag("icons_pack_pack.alpha").assertIsSelected()
        composeRule.onNodeWithTag("icons_style_DEFAULT").assertIsNotSelected()
        composeRule.onNodeWithText("Covers 3 of 10 apps").assertExists()
        composeRule.onNodeWithTag("icons_pack_misses_row").assertExists()

        // When picking Beta
        composeRule.onNodeWithTag("icons_pack_pack.beta").performClick()

        // Then Beta is asked for
        assertEquals("pack.beta", picked)
    }

    @Test
    fun aPackBeingAppliedShowsItIsLoading() {
        // Given Beta just picked and still being read
        val loading = twoPacks.copy(packs = twoPacks.packs.map { if (it.pack.packageName == "pack.beta") it.copy(isLoading = true) else it.copy(coveredApps = null) })
        setPackContent(LauncherSettings(iconPackPackage = "pack.beta"), loading)

        // Then its card shows a spinner, and no coverage is claimed yet
        composeRule.onNodeWithTag("icons_pack_loading", useUnmergedTree = true).assertExists()
        assertEquals(0, composeRule.onAllNodesWithText("Covers", substring = true).fetchSemanticsNodes().size)
    }

    @Test
    fun aPackThatThemesEveryAppTakesOverTheIconShape() {
        // Given a pack applied with the Match pack fallback
        setPackContent(LauncherSettings(iconPackPackage = "pack.alpha", iconPackFallback = IconPackFallback.MATCH_PACK), twoPacks)
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

        // Then the shapes can't be picked, and the colour row is gone
        composeRule.onNodeWithTag("icon_shape_CIRCLE").assertIsNotEnabled()
        composeRule.onNodeWithText("Set by Alpha for the icons it draws").assertExists()
        assertEquals(0, composeRule.onAllNodesWithText("Colour").fetchSemanticsNodes().size)
    }

    @Test
    fun keepingDefaultIconsLeavesTheShapePickable() {
        // Given a pack applied that leaves missed apps their own icons
        setPackContent(LauncherSettings(iconPackPackage = "pack.alpha", iconPackFallback = IconPackFallback.KEEP_DEFAULT), twoPacks)
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

        // Then the shape still applies to them
        composeRule.onNodeWithTag("icon_shape_CIRCLE").assertIsEnabled()
    }

    private fun SettingsRepository.awaitMode(mode: IconRenderMode) =
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { settings.first().iconRenderMode == mode } }

    @Test
    fun styleCardsSetTheStyleAndKeepTheColour() {
        // Given the screen on the default style
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_DEFAULT").assertIsSelected()

        // When picking Glyph and then Outline
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)
        composeRule.onNodeWithTag("icons_style_OUTLINE").performClick()

        // Then the accent colour carries over
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_ACCENT)
        composeRule.onNodeWithTag("icons_style_OUTLINE").assertIsSelected()
    }

    @Test
    fun colourDropdownOffersThemeInvertedForGlyphAndPersistsIt() {
        // Given the glyph style selected
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)

        // When choosing Theme inverted
        openDropdown("icons_colour_row")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME_INVERTED").performClick()

        // Then the inverted glyph mode is stored
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INVERTED)
    }

    @Test
    fun backgroundDropdownPutsGlyphAndOutlineOnATile() {
        // Given the outline style
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_OUTLINE").performClick()
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_ACCENT)

        // When choosing the tile background
        openDropdown("icons_background_row")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_background_row_option_TILE").performClick()

        // Then the tiled outline mode is stored
        settingsRepository.awaitMode(IconRenderMode.OUTLINE_TILE_ACCENT)
    }

    @Test
    fun themeInvertedSamplesGetADifferentBackdrop() {
        // Given the glyph style in the theme colour
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_style_GLYPH").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_ACCENT)
        openDropdown("icons_colour_row")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INK)
        val backdropOf = { composeRule.onNodeWithTag("icons_style_GLYPH").captureToImage().toPixelMap().let { it[it.width / 2, 6] } }
        val themed = backdropOf()

        // When switching to theme inverted
        openDropdown("icons_colour_row")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("icons_colour_row_option_THEME_INVERTED").performClick()
        settingsRepository.awaitMode(IconRenderMode.GLYPH_INVERTED)

        // Then the sample's backdrop is no longer the page tone
        composeRule.waitUntil(timeoutMillis = 3_000) { backdropOf() != themed }
    }

    @Test
    fun colourRowIsHiddenForTheDefaultStyle() {
        // Given the default style
        setContent()

        // Then there is no colour row
        assertEquals(0, composeRule.onAllNodesWithText("Colour").fetchSemanticsNodes().size)
    }

    @Test
    fun iconShapePickerPersistsTheSelectedShape() {
        // Given the screen, with the picker scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("icons_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

        // When picking Circle
        composeRule.onNodeWithTag("icon_shape_CIRCLE").performClick()

        // Then it's selected and persisted
        composeRule.waitUntil(timeoutMillis = 3_000) { runBlocking { settingsRepository.settings.first().iconShape == IconShape.CIRCLE } }
        composeRule.onNodeWithTag("icon_shape_CIRCLE").assertIsSelected()
    }

    @Test
    fun previewAndStyleCardsAreShown() {
        // Given the screen
        setContent()

        // Then the Home preview and all four style cards are there
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        listOf("DEFAULT", "MONOCHROME", "GLYPH", "OUTLINE").forEach { composeRule.onNodeWithTag("icons_style_$it").assertIsDisplayed() }
    }
}
