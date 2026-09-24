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
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeAppsListSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** [onFacetRepo] receives the screen's real [FacetRepository]; when [facetScoped] the VM
     *  is scoped to a freshly-added facet in that same DB, whose id is passed back too.
     *  [seed] runs against that same DB's real repositories before the screen renders, so a test
     *  can place a folder in favorites the same way the app's own long-press flow would. */
    private fun setContent(
        onBack: () -> Unit = {},
        onEditFavorites: () -> Unit = {},
        facetScoped: Boolean = false,
        onFacetRepo: (FacetRepository, Long?) -> Unit = { _, _ -> },
        seed: suspend (FolderRepository, FavoriteAppRepository, DefaultFavoriteAppRepository, FacetRepository, Long?) -> Unit = { _, _, _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "home-apps-list-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val facetRepository = FacetRepository(database.facetDao())
                val facetId = if (facetScoped) runBlocking { facetRepository.addFacet().id } else null
                onFacetRepo(facetRepository, facetId)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), folderRepository, appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
                runBlocking { seed(folderRepository, favoriteAppRepository, defaultFavoriteAppRepository, facetRepository, facetId) }
                HomeAppsListSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
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

    @Test
    fun headerAndPreviewRender() {
        setContent()
        composeRule.onNodeWithText("Home Apps List").assertIsDisplayed()
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
        setContent(onEditFavorites = { navigated = true })
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        composeRule.onNodeWithTag("default_favorites_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun aFolderFavoritedGlobally_isCountedAndShownInPreviewAndReorderList() {
        var folderId = 0L
        setContent(
            seed = { folderRepository, _, defaultFavoriteAppRepository, _, _ ->
                folderId = folderRepository.createFolder("Games")
                defaultFavoriteAppRepository.placeFolder(folderId, position = 0)
            },
        )

        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        // The real installed-apps query backing the preview/uiState resolves asynchronously —
        // wait for it rather than assuming setContent's own waitForIdle already caught it.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("default_favorites_row").assertTextContains("1 of ${com.facetlauncher.app.data.DefaultFavoriteAppRepository.MAX_FAVORITES}") }.isSuccess
        }
        composeRule.onNodeWithTag("default_favorite_reorder_row_folder_$folderId").assertExists()
        // Renders as a real folder row in the preview, not a flattened list of the folder's apps.
        composeRule.onNodeWithTag("home_surface_preview_folder_row_$folderId").assertExists()
    }

    @Test
    fun aFolderFavoritedOnAFacet_isCountedAndShownInPreviewAndReorderList() {
        // This facet must actually be overriding for its own favorites to be the ones shown/edited
        // here — otherwise the screen correctly shows the (empty) launcher-wide default instead
        // (see chat history: editing while inheriting used to silently write without taking effect).
        var folderId = 0L
        setContent(
            facetScoped = true,
            seed = { folderRepository, favoriteAppRepository, _, facetRepository, facetId ->
                val id = requireNotNull(facetId)
                folderId = folderRepository.createFolder("Games")
                favoriteAppRepository.placeFolder(id, folderId, position = 0)
                facetRepository.getById(id)?.let { facetRepository.setOverrideApps(it, true) }
            },
        )

        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("default_favorites_row").assertTextContains("1 of ${com.facetlauncher.app.data.DefaultFavoriteAppRepository.MAX_FAVORITES}") }.isSuccess
        }
        composeRule.onNodeWithTag("default_favorite_reorder_row_folder_$folderId").assertExists()
        composeRule.onNodeWithTag("home_surface_preview_folder_row_$folderId").assertExists()
    }

    @Test
    fun facetScopedScreenWritesTheListContentModeToThatFacetsRowOnceOverriding() {
        // Position/presentation/vertical alignment moved to Settings -> Appearance (see chat
        // history) — list content mode is still this screen's own content, gated by overrideApps.
        lateinit var facetRepo: FacetRepository
        setContent(facetScoped = true, onFacetRepo = { repo, _ -> facetRepo = repo })

        // The facet-scoped state resolves asynchronously (Room query behind the ViewModel's
        // StateFlow combine) — wait for the switch to actually render rather than assuming
        // setContent()'s own waitForIdle() already caught it (see chat history: this raced on a
        // real device even though it passed reliably enough locally to miss in review).
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("home_apps_list_override_row").fetchSemanticsNodes().isNotEmpty()
        }

        // Given this facet starts out inheriting — the content-mode control is disabled
        composeRule.onNodeWithTag("default_list_content_row").assertIsNotEnabled()

        // When switching to Override
        composeRule.onNodeWithTag("home_apps_list_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepo.observeFacets().first().any { it.overrideApps } }
        }

        // Then the control becomes live and writes to this facet's own row
        composeRule.onNodeWithTag("default_list_content_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_list_content_row_option_RECENTS").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepo.observeFacets().first().any { it.listContentMode == com.facetlauncher.app.data.model.ListContentMode.RECENTS } }
        }
    }

    @Test
    fun facetScopedModeShowsInheritOverrideSwitch() {
        // Given a facet-scoped entry point
        setContent(facetScoped = true)

        // The facet-scoped state resolves asynchronously — wait for the switch to actually render
        // rather than assuming setContent()'s own waitForIdle() already caught it (see chat history)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("home_apps_list_override_row").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the inherit/override switch is present, defaulting to Inherit (controls disabled) —
        // position/presentation/vertical alignment moved to Appearance (see chat history), so this
        // now gates the content-mode control that's still left on this screen.
        composeRule.onNodeWithTag("home_apps_list_inherit_row").assertExists()
        composeRule.onNodeWithTag("home_apps_list_override_row").assertExists()
        composeRule.onNodeWithTag("default_list_content_row").assertIsNotEnabled()

        // When switching to Override
        composeRule.onNodeWithTag("home_apps_list_override_row").performClick()

        // Then the controls become live
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_list_content_row").assertIsEnabled() }.isSuccess
        }
    }
}
