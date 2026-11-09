package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import androidx.compose.ui.test.assertCountEquals
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Test
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

class HomeAppsListSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var settingsRepository: SettingsRepository

    /** [onFacetRepo] receives the screen's real [FacetRepository]; when [facetScoped] the VM
     *  is scoped to a freshly-added facet in that same DB, whose id is passed back too.
     *  [seed] runs against that same DB's real repositories before the screen renders, so a test
     *  can place a folder in favorites the same way the app's own long-press flow would. */
    private fun setContent(
        onBack: () -> Unit = {},
        onEditFavorites: () -> Unit = {},
        facetScoped: Boolean = false,
        onFacetRepo: (FacetRepository, Long?) -> Unit = { _, _ -> },
        seed: suspend (FolderRepository, PlacementRepository, FacetRepository, Long?) -> Unit = { _, _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "home-apps-list-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val facetId = if (facetScoped) runBlocking { facetRepository.addFacet().id } else null
                onFacetRepo(facetRepository, facetId)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, folderRepository, appRepository)
                runBlocking { seed(folderRepository, placementRepository, facetRepository, facetId) }
                HomeAppsListSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    placementRepository,
                    observePlacedItems,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    DefaultAppRepository(context),
                    GetInstalledAppsUseCase(appRepository),
                    SelectPreviewAppsUseCase(),
                )
            }
            FacetLauncherTheme {
                HomeAppsListSettingsScreen(onBack = onBack, onEditFavorites = onEditFavorites, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    /** The screen over a freshly-added facet; returns the real [FacetRepository] it writes to. */
    private fun setFacetContent(): FacetRepository {
        lateinit var facetRepo: FacetRepository
        setContent(facetScoped = true, onFacetRepo = { repo, _ -> facetRepo = repo })
        awaitFacetTitle()
        return facetRepo
    }

    private fun facets(repo: FacetRepository) = runBlocking { repo.observeFacets().first() }

    /** The facet-scoped state resolves asynchronously (a Room query behind the ViewModel's StateFlow), and shows as the facet's title. */
    private fun awaitFacetTitle() {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Apps List").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun aFacetScopedScreenRendersItsHeaderAndPreview() {
        setContent(facetScoped = true)
        awaitFacetTitle()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_apps_list_preview_card").assertExists()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })
        composeRule.onNodeWithTag("back_button").performClick()
        assertEquals(true, backInvoked)
    }

    // App row position/presentation/vertical-alignment dropdown rows moved to Settings ->
    // Appearance (see chat history — "look" fields, edited independently of this screen's content
    // now); coverage moved to AppearanceSettingsScreenTest's
    // appRowPositionPresentationAndVerticalAlignmentRowsChangeTheGlobalSetting.

    @Test
    fun favoritesRowIsClickable() {
        var navigated = false
        setContent(facetScoped = true, onEditFavorites = { navigated = true })
        awaitFacetTitle()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        composeRule.onNodeWithTag("default_favorites_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun aFolderFavoritedOnAFacet_isCountedAndShownInPreviewAndReorderList() {
        var folderId = 0L
        setContent(
            facetScoped = true,
            seed = { folderRepository, placementRepository, facetRepository, facetId ->
                val id = requireNotNull(facetId)
                folderId = folderRepository.createFolder("Games")
                placementRepository.placeFolder(PlacementTarget(PlacementList.FAVORITES, id), folderId, position = 0)
            },
        )

        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("default_favorites_row").assertTextContains("1 of ${com.facetlauncher.app.data.model.AppListLimits.MAX_FAVORITES}") }.isSuccess
        }
        composeRule.onNodeWithTag("default_favorite_reorder_row_folder_$folderId").assertExists()
        composeRule.onNodeWithTag("home_surface_preview_folder_row_$folderId").assertExists()
    }

    @Test
    fun facetScopedScreenWritesTheListContentModeToThatFacetsRow() {
        lateinit var facetRepo: FacetRepository
        setContent(facetScoped = true, onFacetRepo = { repo, _ -> facetRepo = repo })

        // The facet-scoped state resolves asynchronously (Room query behind the ViewModel's
        // StateFlow combine) — wait for the reset row to actually render rather than assuming
        // setContent()'s own waitForIdle() already caught it.
        awaitFacetTitle()

        // The content-mode control is live, with no Inherit/Override switch, and writes to this facet's own row
        composeRule.onNodeWithTag("default_list_content_row").assertIsEnabled()
        composeRule.onNodeWithTag("default_list_content_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_list_content_row_option_RECENTS").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepo.observeFacets().first().any { it.listContentMode == com.facetlauncher.app.data.model.ListContentMode.RECENTS } }
        }
    }

    @Test
    fun facetScopedModeHasLiveControlsAndTheFacetsTitle() {
        // Given a facet-scoped entry point
        setContent(facetScoped = true)

        // The facet-scoped state resolves asynchronously — wait for the reset row to render
        awaitFacetTitle()

        // Then the controls are live and the title is the facet's
        composeRule.onNodeWithTag("default_list_content_row").assertIsEnabled()
        composeRule.onNodeWithText("Apps List").assertIsDisplayed()
    }

    private fun appInfoForTest() = com.facetlauncher.app.data.model.AppInfo(
        packageName = "com.example.fav", activityName = ".Main", label = "Fav", icon = null,
    )

    @Test
    fun appRowPositionPresentationAndVerticalAlignmentRowsChangeTheFacetsValues() {
        // Given a facet's screen
        val repo = setFacetContent()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_app_row_position_row"))

        composeRule.onNodeWithTag("appearance_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_position_row_option_RIGHT").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).any { it.appRowPosition == com.facetlauncher.app.data.model.AppRowPosition.RIGHT }
        }

        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_app_row_presentation_row"))
        composeRule.onNodeWithTag("appearance_app_row_presentation_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_row_presentation_row_option_TEXT_ONLY").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).any { it.appRowPresentation == com.facetlauncher.app.data.model.AppRowPresentation.TEXT_ONLY }
        }

        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_vertical_alignment_row_option_TOP").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).any { it.appListVerticalAlignment == com.facetlauncher.app.data.model.AppListVerticalAlignment.TOP }
        }
    }

    @Test
    fun lookRowsShowTheirTitles() {
        setFacetContent()

        // The Clock card sits above these rows, so each is scrolled into view before it's checked.
        listOf("Apps list alignment", "Show Home apps as", "Apps list position").forEach { title ->
            composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasText(title))
            // The Portrait and Landscape alignment cards share these two titles; the first is the Portrait one.
            composeRule.onAllNodesWithText(title)[0].assertIsDisplayed()
        }
    }

    @Test
    fun shrinkListToFitIsOfferedForAListAndStoredWhenTurnedOn() {
        // Given a facet with the default list layout, where long lists scroll
        val facetRepo = setFacetContent()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_shrink_to_fit_row"))
        composeRule.onNodeWithTag("appearance_app_list_shrink_to_fit_row").assertTextContains("Off")

        // When turning it on
        composeRule.onNodeWithTag("appearance_app_list_shrink_to_fit_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_shrink_to_fit_row_option_ON").performClick()

        // Then it's stored on the facet
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepo.observeFacets().first().any { it.appListShrinkToFit == com.facetlauncher.app.data.model.AppListShrinkToFit.ON } }
        }
    }

    @Test
    fun shrinkListToFitIsNotOfferedForTheGrid() {
        // Given a facet on the grid layout
        setFacetContent()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_app_list_layout_row"))
        composeRule.onNodeWithTag("appearance_app_list_layout_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_app_list_layout_row_option_GRID").performClick()

        // Then there is no row to shrink a list that isn't one
        composeRule.waitUntil(timeoutMillis = 3_000) { composeRule.onAllNodesWithTag("appearance_app_list_grid_display_mode_row").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("appearance_app_list_shrink_to_fit_row").assertDoesNotExist()
    }

    @Test
    fun landscapeAlignmentRowsWriteTheLandscapeSettingAndLeavePortraitAlone() {
        // Given a facet's screen
        val repo = setFacetContent()

        // When the landscape position and list position are chosen
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_landscape_app_row_position_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row_option_CENTER").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).any { it.landscape.appRowPosition == com.facetlauncher.app.data.model.AppRowPosition.CENTER }
        }
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_landscape_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row_option_TOP").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).any { it.landscape.appListVerticalAlignment == com.facetlauncher.app.data.model.AppListVerticalAlignment.TOP }
        }

        // Then portrait's own values are unchanged
        val facet = facets(repo).single()
        org.junit.Assert.assertEquals(com.facetlauncher.app.data.model.AppRowPosition.LEFT, facet.appRowPosition)
        org.junit.Assert.assertEquals(com.facetlauncher.app.data.model.AppListVerticalAlignment.BOTTOM, facet.appListVerticalAlignment)
    }

    @Test
    fun choosingSameAsPortraitClearsTheLandscapeSetting() {
        // Given a facet with a saved landscape position
        val repo = setFacetContent()
        runBlocking { repo.updateLandscape(facets(repo).single().id) { it.copy(appRowPosition = com.facetlauncher.app.data.model.AppRowPosition.RIGHT) } }
        // The seeded value reaches the screen asynchronously; wait for it so choosing "Same as portrait" is a real change
        composeRule.waitUntil(timeoutMillis = 3_000) { facets(repo).single().landscape.appRowPosition == com.facetlauncher.app.data.model.AppRowPosition.RIGHT }
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_landscape_app_row_position_row"))
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(hasTestTag("appearance_landscape_app_row_position_row") and hasText("Right", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }

        // When "Same as portrait" is chosen
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_landscape_app_row_position_row_option_null").performClick()

        // Then the landscape value is cleared
        composeRule.waitUntil(timeoutMillis = 3_000) {
            facets(repo).single().landscape.appRowPosition == null
        }
    }

    @Test
    fun turningLandscapeOffHidesTheLandscapeAlignmentAndIsSavedOnTheFacet() {
        // Given a facet's screen with landscape allowed
        val repo = setFacetContent()
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_landscape_app_list_vertical_alignment_row"))
        composeRule.onNodeWithTag("appearance_landscape_app_list_vertical_alignment_row").assertExists()

        // When the facet's landscape switch is turned off
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("appearance_allow_landscape_row"))
        composeRule.onNodeWithTag("appearance_allow_landscape_row").performClick()

        // Then it is saved on that facet and the landscape alignment rows go away
        composeRule.waitUntil(timeoutMillis = 3_000) { facets(repo).single().allowLandscape.not() }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("appearance_landscape_app_list_vertical_alignment_row").fetchSemanticsNodes().isEmpty()
        }
    }
}
