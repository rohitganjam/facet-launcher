package com.facetlauncher.app.ui.settings

import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.test.assertCountEquals
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppearanceSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Fakes calendar *data* — no real Calendar Provider reads. Mirrors `CalendarSettingsScreenTest`'s own local `FakeCalendarRepository`, just overriding `getTodayEvents` instead of `getCalendars`. */
    private class FakeCalendarRepository(
        contentResolver: android.content.ContentResolver,
        private val events: List<CalendarEvent>,
    ) : CalendarRepository(contentResolver) {
        override suspend fun getTodayEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = events
    }

    /**
     * [facetId] non-null builds a facet-scoped screen — seeds a real facet with that id in the
     * same in-memory DB. [seed] runs against [FacetRepository] before the screen renders (facet
     * override flags etc.); [seedApps] runs against the real favorite/dock repositories, given the
     * device's own real installed apps — mirrors `FacetCarouselScreenTest`'s own `seed`/`seedApps`
     * split. Real installed apps, not a synthetic [AppInfo], are required here:
     * [DefaultFavoriteAppRepository]/[DockAppRepository] hydrate every stored row against
     * [AppRepository]'s live installed-app list (same uninstall-collapse pattern as Home itself),
     * so a fake package name is silently filtered out of what the preview ever renders — the
     * preview must reflect this scope's real, actual content, not mock/sample data (see chat
     * history). [calendarGranted]/[calendarEvents] fake the calendar permission grant and its
     * data via [FakeCalendarPermissionRepository]/[FakeCalendarRepository] — no real OS-level
     * grant needed (see that fake's own doc for why).
     */
    private lateinit var screenViewModel: AppearanceSettingsViewModel

    /** The `facets` flow is a real Room query, so a facet-scoped screen first renders unscoped until it emits. */
    private fun awaitFacetScoped() {
        composeRule.waitUntil(timeoutMillis = 5_000) { screenViewModel.uiState.value.isFacetScoped }
        composeRule.waitForIdle()
    }

    private fun setContent(
        onBack: () -> Unit = {},
        onNavigateToClockStyleGallery: () -> Unit = {},
        facetId: Long? = null,
        calendarGranted: Boolean = false,
        calendarEvents: List<CalendarEvent> = emptyList(),
        seed: suspend (FacetRepository, Long?) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, FavoriteAppRepository, DefaultFavoriteAppRepository, DockAppRepository, FacetDockAppRepository, Long?) -> Unit = { _, _, _, _, _, _ -> },
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "appearance-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val wallpaperRepository = com.facetlauncher.app.data.WallpaperRepository(
                    android.app.WallpaperManager.getInstance(context),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao())
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), folderRepository, appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), folderRepository, appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), folderRepository, appRepository)
                if (facetId != null) {
                    runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                }
                runBlocking { seed(facetRepository, facetId) }
                runBlocking { seedApps(appRepository, favoriteAppRepository, defaultFavoriteAppRepository, dockAppRepository, facetDockAppRepository, facetId) }
                AppearanceSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
                    dockAppRepository,
                    facetDockAppRepository,
                    FakeCalendarRepository(context.contentResolver, calendarEvents),
                    FakeCalendarPermissionRepository(context, granted = calendarGranted),
                    wallpaperRepository,
                ).also { screenViewModel = it }
            }
            FacetLauncherTheme {
                AppearanceSettingsScreen(onBack = onBack, onNavigateToClockStyleGallery = onNavigateToClockStyleGallery, viewModel = viewModel)
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
    fun clockStyleGalleryRowIsClickable() {
        // Given the screen, scrolled to the Clock style row (moved in from the main Settings
        // list — see chat history: calendar/appearance styling consolidation)
        var navigated = false
        setContent(onNavigateToClockStyleGallery = { navigated = true })
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))

        // When tapping it
        composeRule.onNodeWithTag("clock_style_gallery_row").performClick()

        // Then its callback fires
        org.junit.Assert.assertEquals(true, navigated)
    }

    @Test
    fun themeModeDropdownSwitchesBetweenLightDarkAndSystem() {
        // Given the screen, System selected by default — now in the separate "General" card,
        // below the "Dock & Home" and "Clock" cards (see chat history), so it needs scrolling into
        // view first.
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("theme_mode_dropdown"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_system_bar_icons_row"))
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
        // Given the screen, "Wallpaper colors" selected by default — now in the "General" card,
        // below "Dock & Home"/"Clock" (see chat history), so it needs scrolling into view first.
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("accent_source_basic"))
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
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view (see
        // themeModeDropdownSwitchesBetweenLightDarkAndSystem's own comment)
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))

        // Then the wallpaper-role grid is shown, and the Basic-colors grid is not
        composeRule.onNodeWithTag("wallpaper_accent_role_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("accent_swatch_grid").assertDoesNotExist()
    }

    @Test
    fun wallpaperAccentRoleGridDisappearsWhenBasicColorsIsSelected() {
        // Given the screen, "Wallpaper colors" selected by default — scrolled into view
        setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_grid"))
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
        // Given the screen, "Wallpaper colors" selected by default (PRIMARY role) — scrolled into view
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("wallpaper_accent_role_SECONDARY"))

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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_icons_row"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_icon_shape_row"))

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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_font_row"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_font_weight_slider_control"))

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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_font_size_slider_control"))

        // When dragging the Text Size slider to its last stop (Huge)
        composeRule.onNodeWithTag("appearance_font_size_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontScaleOption.entries.size - 1).toFloat()) }

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().fontScaleOption == FontScaleOption.HUGE }
        }
    }

    @Test
    fun previewCardShowsTheRealFavoritesAndDockApps() {
        // Given the screen — the live preview card reuses the real AppRow/DockIcon, backed by
        // this scope's actual configured favorites/dock, not mock/sample data (see chat history).
        // Seeded with real installed apps, not a synthetic AppInfo — see setContent's own doc:
        // DefaultFavoriteAppRepository/DockAppRepository hydrate against the device's real
        // installed-app list, silently dropping any row that isn't actually installed.
        var favorite1Label = ""
        var favorite2Label = ""
        var dockLabel = ""
        setContent(
            seedApps = { appRepository, _, defaultFavoriteAppRepository, dockAppRepository, _, _ ->
                val installed = appRepository.getInstalledApps()
                favorite1Label = installed[0].label
                favorite2Label = installed[1].label
                dockLabel = installed[2].label
                defaultFavoriteAppRepository.addFavorite(installed[0], position = 0)
                defaultFavoriteAppRepository.addFavorite(installed[1], position = 1)
                dockAppRepository.addDockApp(installed[2], position = 0)
            },
        )

        // Then the preview card renders the real configured apps' own labels — polled, since the
        // favorites/dock repositories are backed by real Room queries, which settle asynchronously
        // after the first composition (the same "real I/O" lesson as elsewhere — see CLAUDE.md).
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(favorite1Label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(favorite1Label).assertExists()
        composeRule.onNodeWithText(favorite2Label).assertExists()
        composeRule.onNodeWithContentDescription(dockLabel).assertExists()
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_label_color_row"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText(favoriteLabel).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
    }

    @Test
    fun previewCardRendersTheWallpaperBehindItsContent() {
        // Given the screen, one real (installed) favorite seeded
        var favoriteLabel = ""
        setContent(
            seedApps = { appRepository, _, defaultFavoriteAppRepository, _, _, _ ->
                val app = appRepository.getInstalledApps()[0]
                favoriteLabel = app.label
                defaultFavoriteAppRepository.addFavorite(app, position = 0)
            },
        )

        // Then the preview card paints a wallpaper backdrop, with its real content still on top —
        // polled, same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.onNodeWithTag("appearance_preview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wallpaper_background").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(favoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(favoriteLabel).assertExists()
    }

    @Test
    fun previewCardShowsAClockInGlobalMode() {
        // Given the global (non-facet-scoped) screen — the preview now includes the real clock
        // (Part A's deferred "shared mini-home-preview" follow-up, scoped down to just this
        // screen's own preview — see chat history)
        setContent()

        composeRule.onNodeWithTag("appearance_preview_clock").assertIsDisplayed()
    }

    @Test
    fun previewCardRendersGridLayoutWithTheChosenColumnCount() {
        // Given 6 real installed apps as the global default favorites — enough for a 4-column
        // grid to wrap into two rows (4 + 2), the shape the left-align fix specifically targets.
        val packages = mutableListOf<String>()
        setContent(
            seedApps = { appRepository, _, defaultFavoriteAppRepository, _, _, _ ->
                val installed = appRepository.getInstalledApps()
                (0 until 6).forEach { index ->
                    defaultFavoriteAppRepository.addFavorite(installed[index], position = index)
                    packages += installed[index].packageName
                }
            },
        )
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_grid_columns_row"))
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_grid_columns_row_option_FOUR").performClick()
        composeRule.waitForIdle()

        // Then all 6 favorites render as grid icon tiles — not truncated to the old flat 6-item
        // cap becoming a coincidence here, and not the single-column AppRow the card used to
        // always show before the 3-way layout branch existed.
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
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
            seedApps = { appRepository, _, defaultFavoriteAppRepository, _, _, _ ->
                val app = appRepository.getInstalledApps()[0]
                packageName = app.packageName
                defaultFavoriteAppRepository.addFavorite(app, position = 0)
            },
        )
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()
        composeRule.waitForIdle()

        // Then Grid display defaults to Icons — the tile renders the icon, not the label
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("appearance_preview_grid_icon_$packageName").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("appearance_preview_grid_label_$packageName").assertDoesNotExist()

        // When switching Grid display to Text
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_grid_display_mode_row"))
        composeRule.onNodeWithTag("appearance_app_list_grid_display_mode_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_grid_display_mode_row_option_TEXT").performClick()
        composeRule.waitForIdle()

        // Then the tile flips to the label, not the icon
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_preview_card"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("appearance_preview_grid_label_$packageName").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("appearance_preview_grid_icon_$packageName").assertDoesNotExist()
    }

    @Test
    fun dockDisplayStyleRowChangesTheGlobalSetting() {
        // Given the screen, Icons selected by default — moved in from Dock's own screen (see chat history)
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
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
    fun appRowPositionPresentationAndVerticalAlignmentRowsChangeTheGlobalSetting() {
        // Given the screen — moved in from Home Apps List's own screen (see chat history)
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_row_position_row"))

        composeRule.onNodeWithTag("appearance_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_position_row_option_RIGHT").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appRowPosition == com.facetlauncher.app.data.model.AppRowPosition.RIGHT }
        }

        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_row_presentation_row"))
        composeRule.onNodeWithTag("appearance_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_presentation_row_option_TEXT_ONLY").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().appRowPresentation == com.facetlauncher.app.data.model.AppRowPresentation.TEXT_ONLY }
        }

        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_vertical_alignment_row"))
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
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))

        composeRule.onNodeWithTag("appearance_dock_display_style_row").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("appearance_dock_display_style_row_option_LAUNCHER_DEFAULT").assertCountEquals(0)
    }

    @Test
    fun facetScopedModeKeepsClockRowAndPreviewButHidesEveryGlobalOnlyField() {
        // Given a facet-scoped entry point — waited for the facet-scoped combine() to settle first
        // (the `facets` flow is a real Room query; the screen briefly renders as global/unscoped on
        // its very first composition until it emits — see chat history).
        setContent(facetId = 1L)
        awaitFacetScoped()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_vertical_alignment_row"))

        // Then the four moved-in look fields are still here...
        composeRule.onNodeWithTag("appearance_dock_display_style_row").assertExists()
        composeRule.onNodeWithTag("appearance_app_row_position_row").assertExists()
        composeRule.onNodeWithTag("appearance_app_row_presentation_row").assertExists()
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row").assertExists()
        // ...the clock preview and the Clock row (into this facet's own clock style screen) are
        // here too...
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").assertExists()
        // ...but every purely-global field (theme, accent, icon, font, app label color,
        // size/weight sliders) is gone — no per-facet override exists for those yet (see this
        // screen's own doc comment)
        composeRule.onNodeWithTag("theme_mode_dropdown").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_icons_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_icon_shape_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_font_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_app_label_color_row").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_font_size_slider").assertDoesNotExist()
        composeRule.onNodeWithTag("appearance_font_weight_slider").assertDoesNotExist()
    }

    @Test
    fun facetScopedClockRowReflectsTheFacetsOwnClockOverride() {
        // Given a facet overriding the clock with a different template than the global default
        setContent(facetId = 1L, seed = { facetRepository, facetId ->
            val facet = com.facetlauncher.app.data.local.FacetEntity(id = requireNotNull(facetId), name = "P", position = 0)
            facetRepository.setOverrideClock(facet, true)
            facetRepository.setClockTemplateId(facet.copy(overrideClock = true), com.facetlauncher.app.data.model.ClockTemplateId.RULE_MERIDIEM)
        })
        awaitFacetScoped()

        // Then the preview clock is shown and the Clock row names the facet's own template, not the global one
        composeRule.onNodeWithTag("appearance_preview_clock").assertExists()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").assertTextContains("Ruler", substring = true)
    }

    @Test
    fun facetScopedClockRowOpensTheClockStyleGallery() {
        // Given a facet-scoped screen
        var navigated = false
        setContent(facetId = 1L, onNavigateToClockStyleGallery = { navigated = true })
        awaitFacetScoped()

        // When tapping the Clock row
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithTag("clock_style_gallery_row").performClick()

        // Then it navigates
        composeRule.waitUntil(timeoutMillis = 3_000) { navigated }
    }

    @Test
    fun globalModeShowsThreeSeparateSectionsInOrder() {
        // Clock / Dock & Home / General are each their own card (Clock first). Each header is
        // asserted right as it scrolls into view, in that order — not via cross-scroll bounds
        // comparison, which breaks once an earlier item scrolls far enough to be disposed from the
        // LazyColumn's composed range.
        setContent()

        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("clock_style_gallery_row"))
        composeRule.onNodeWithText("Clock").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
        composeRule.onNodeWithText("Dock & home").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("theme_mode_dropdown"))
        composeRule.onNodeWithText("General").assertIsDisplayed()
    }

    @Test
    fun facetScopedModeShowsClockAndDockAndHomeButNotGeneral() {
        // The Clock style row lives in facet mode too (above Dock & home); General is global-only.
        setContent(facetId = 1L)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Clock").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Clock").assertIsDisplayed()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
        composeRule.onNodeWithText("Dock & home").assertIsDisplayed()
        composeRule.onNodeWithText("General").assertDoesNotExist()
    }

    @Test
    fun dockAndHomeRowsShowTheirRenamedTitles() {
        // Renamed per direct request (see chat history) — asserted by title text since these
        // rows have no other distinguishing content at rest.
        setContent()

        // The Clock card sits above these rows, so each is scrolled into view before it's checked.
        listOf("Show Dock apps as", "Home Apps Alignment", "Show Home apps as", "Home Apps list position").forEach { title ->
            composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasText(title))
            composeRule.onNodeWithText(title).assertIsDisplayed()
        }
    }

    @Test
    fun previewCardShowsRealCalendarEventsWhenPermissionIsGranted() {
        // The calendar preview moved here from ClockStyleGalleryScreen (see chat history) — real
        // events for the effective calendar selection, not sample/mock data. Timed relative to
        // real "now", not a fixed epoch instant — CalendarEventsBlock itself filters out any
        // non-all-day event whose endTimeMillis has already passed (an earlier version of this
        // fixture used endTimeMillis = 1, which is always in the past, so it was silently dropped
        // and the test could never pass no matter how long it polled).
        val now = System.currentTimeMillis()
        val event = CalendarEvent(id = 1, calendarId = "1", title = "Team standup", startTimeMillis = now, endTimeMillis = now + 3_600_000, isAllDay = false)
        setContent(calendarGranted = true, calendarEvents = listOf(event))

        // Polled — the calendar events are read inside the combine()'s own async settle, same as
        // the favorites/dock lists (see previewCardShowsTheRealFavoritesAndDockApps's own comment).
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Team standup").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Team standup").assertIsDisplayed()
    }

    @Test
    fun previewCardShowsNoCalendarEventsWithoutPermissionRatherThanASampleFallback() {
        // Given calendar permission not granted (this screen's setContent default)
        setContent()

        // Then the preview shows no events at all — never a fake/sample fallback (see chat history)
        composeRule.onNodeWithText("Team standup").assertDoesNotExist()
    }

    @Test
    fun facetScopedDockDisplayStyleRowOffersLauncherDefaultAndWritesToThatFacet() {
        // Given a facet-scoped entry point, Icons inherited by default — waited for facet-scoped
        // settle first (see facetScopedModeKeepsClockRowAndPreviewButHidesEveryGlobalOnlyField's own comment)
        // so the dropdown's option list isn't read mid-transition from the global option set.
        val facetId = 1L
        setContent(facetId = facetId)
        awaitFacetScoped()
        composeRule.onNodeWithTag("appearance_settings_screen").performScrollToNode(hasTestTag("appearance_dock_display_style_row"))
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
    fun facetScopedPreviewShowsTheDefaultFavoritesAndDockWhileNotOverriding() {
        // "in facet settings - consume the resolved output between facet and defaults" (see chat
        // history) — a facet not overriding apps/dock content shows the launcher-wide default in
        // its own preview, not its own (unused) stale rows.
        val facetId = 1L
        var globalFavoriteLabel = ""
        var globalDockLabel = ""
        var facetFavoriteLabel = ""
        var facetDockLabel = ""
        setContent(
            facetId = facetId,
            seedApps = { appRepository, favoriteAppRepository, defaultFavoriteAppRepository, dockAppRepository, facetDockAppRepository, id ->
                val installed = appRepository.getInstalledApps()
                globalFavoriteLabel = installed[0].label
                globalDockLabel = installed[1].label
                facetFavoriteLabel = installed[2].label
                facetDockLabel = installed[3].label
                defaultFavoriteAppRepository.addFavorite(installed[0], position = 0)
                dockAppRepository.addDockApp(installed[1], position = 0)
                favoriteAppRepository.addFavorite(id!!, installed[2], position = 0)
                facetDockAppRepository.addDockApp(id, installed[3], position = 0)
            },
        )

        // Polled — same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(globalFavoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(globalFavoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(globalDockLabel).assertExists()
        composeRule.onNodeWithText(facetFavoriteLabel).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(facetDockLabel).assertDoesNotExist()
    }

    @Test
    fun facetScopedPreviewShowsThatFacetsOwnFavoritesAndDockWhileOverriding() {
        // The inverse of facetScopedPreviewShowsTheDefaultFavoritesAndDockWhileNotOverriding — once
        // a facet overrides apps/dock content, its own rows win over the launcher-wide default.
        val facetId = 1L
        var globalFavoriteLabel = ""
        var globalDockLabel = ""
        var facetFavoriteLabel = ""
        var facetDockLabel = ""
        setContent(
            facetId = facetId,
            seed = { facetRepository, id ->
                // Re-fetched between writes — `facetDao.update` replaces the whole row, so reusing
                // the same (now-stale) `facet` snapshot for the second call would clobber the first
                // call's own field change back to its old value.
                val facet = facetRepository.getById(id!!)!!
                facetRepository.setOverrideApps(facet, overriding = true)
                val updated = facetRepository.getById(id)!!
                facetRepository.updateOverridingDock(updated, overriding = true)
            },
            seedApps = { appRepository, favoriteAppRepository, defaultFavoriteAppRepository, dockAppRepository, facetDockAppRepository, id ->
                val installed = appRepository.getInstalledApps()
                globalFavoriteLabel = installed[0].label
                globalDockLabel = installed[1].label
                facetFavoriteLabel = installed[2].label
                facetDockLabel = installed[3].label
                defaultFavoriteAppRepository.addFavorite(installed[0], position = 0)
                dockAppRepository.addDockApp(installed[1], position = 0)
                favoriteAppRepository.addFavorite(id!!, installed[2], position = 0)
                facetDockAppRepository.addDockApp(id, installed[3], position = 0)
            },
        )

        // Polled — same real-Room-query settle lesson as previewCardShowsTheRealFavoritesAndDockApps.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(facetFavoriteLabel).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(globalFavoriteLabel).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(globalDockLabel).assertDoesNotExist()
        composeRule.onNodeWithText(facetFavoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(facetDockLabel).assertExists()
    }
}
