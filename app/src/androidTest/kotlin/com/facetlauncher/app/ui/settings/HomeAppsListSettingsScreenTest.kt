package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.facetlauncher.app.data.model.AppRowPosition
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
        seed: suspend (FolderRepository, FavoriteAppRepository, DefaultFavoriteAppRepository, Long?) -> Unit = { _, _, _, _ -> },
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
                runBlocking { seed(folderRepository, favoriteAppRepository, defaultFavoriteAppRepository, facetId) }
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

    @Test
    fun appRowPositionDropdownSwitchesBetweenLeftAndRight() {
        setContent()
        composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Left")

        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Right") }.isSuccess
        }
    }

    @Test
    fun appRowPositionDropdownOffersCenter() {
        setContent()
        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_CENTER").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Center") }.isSuccess
        }
    }

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
            seed = { folderRepository, _, defaultFavoriteAppRepository, _ ->
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
        var folderId = 0L
        setContent(
            facetScoped = true,
            seed = { folderRepository, favoriteAppRepository, _, facetId ->
                folderId = folderRepository.createFolder("Games")
                favoriteAppRepository.placeFolder(requireNotNull(facetId), folderId, position = 0)
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
    fun facetScopedScreenWritesThePositionToThatFacetsRow() {
        lateinit var facetRepo: FacetRepository
        setContent(facetScoped = true, onFacetRepo = { repo, _ -> facetRepo = repo })

        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepo.observeFacets().first().any { it.appRowPosition == AppRowPosition.RIGHT } }
        }
    }
}
