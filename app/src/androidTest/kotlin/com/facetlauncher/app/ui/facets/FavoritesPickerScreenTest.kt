package com.facetlauncher.app.ui.facets

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class FavoritesPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var facetId = 0L
    private lateinit var lastViewModel: FavoritesPickerViewModel

    /** [seed] runs against real (throwaway, in-memory) repositories before the screen renders. */
    private fun setContent(
        onDone: () -> Unit = {},
        seed: suspend (AppRepository, FavoriteAppRepository, FolderRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), folderRepository, appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
                runBlocking {
                    facetId = database.facetDao().insert(FacetEntity(name = "Facet 1", position = 0))
                    seed(appRepository, favoriteAppRepository, folderRepository)
                }
                val usageStatsRepository = UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository)
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                FavoritesPickerViewModel(
                    SavedStateHandle(mapOf("facetId" to facetId)),
                    GetInstalledAppsUseCase(appRepository),
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
                    folderRepository,
                    SortAppsForPickerUseCase(usageStatsRepository),
                    usageAccessRepository,
                ).also { lastViewModel = it }
            }
            FacetLauncherTheme {
                FavoritesPickerScreen(onDone = onDone, onNavigateToUsageAccessExplanation = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun checkingAnAppAddsItToFavorites() {
        // Given the picker, searched down to some real installed app
        var targetApp: AppInfo? = null
        setContent { appRepository, _, _ -> targetApp = runBlocking { appRepository.getInstalledApps() }.first() }
        val app = requireNotNull(targetApp)
        val rowTag = "favorites_picker_row_${app.packageName}"

        // The ViewModel's installed-apps list comes from a real suspend fetch (init { launch { ... } }),
        // which isn't reliably caught by a single waitForIdle() — poll for the row before interacting
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("favorites_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When it's checked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then its checkbox reflects the new favorite membership
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToAllApps() {
        // Given two real apps already favorited, so both "FAVORITES" and "ALL APPS" render
        setContent { appRepository, favoriteAppRepository, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            installed.take(2).forEachIndexed { index, app -> favoriteAppRepository.addFavorite(facetId, app, index) }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("ALL APPS").assertExists() }.isSuccess
        }

        // When scrolling down to the "ALL APPS" section
        composeRule.onNodeWithText("ALL APPS").performScrollTo()

        // Then the pinned header (title + Done) is still on screen, not scrolled away
        composeRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeRule.onNodeWithTag("favorites_picker_done").assertIsDisplayed()
    }

    @Test
    fun favoritedAppsRenderFirstUnderTheirOwnSectionHeader() {
        // Given two real apps already favorited
        var favorites: List<AppInfo> = emptyList()
        setContent { appRepository, favoriteAppRepository, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            favorites = installed.take(2)
            favorites.forEachIndexed { index, app -> favoriteAppRepository.addFavorite(facetId, app, index) }
        }

        // Then both section headers render, with the favorited apps' rows present under "FAVORITES"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("FAVORITES").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("ALL APPS").assertExists()
        favorites.forEach { app ->
            composeRule.onNodeWithTag("favorites_picker_row_${app.packageName}").assertIsOn()
        }
    }

    @Test
    fun checkingIsBlockedAtTheMaxFavoritesCap() {
        // Given favorites already at the cap
        var overflowApp: AppInfo? = null
        setContent { appRepository, favoriteAppRepository, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            val capApps = installed.take(FavoriteAppRepository.MAX_FAVORITES)
            overflowApp = installed.getOrNull(FavoriteAppRepository.MAX_FAVORITES)
            runBlocking { capApps.forEachIndexed { index, app -> favoriteAppRepository.addFavorite(facetId, app, index) } }
        }
        val app = requireNotNull(overflowApp) {
            "test needs at least ${FavoriteAppRepository.MAX_FAVORITES + 1} installed apps visible to the test APK"
        }
        val rowTag = "favorites_picker_row_${app.packageName}"

        // The overflow app sits past the cap (13th+), i.e. below the fold of the lazy list, so
        // search for it first and only then wait for its row to be composed.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("favorites_picker_search").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("favorites_picker_search").performTextInput(app.label)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When trying to check one more app past the cap
        composeRule.onNodeWithTag(rowTag).performClick()
        composeRule.waitForIdle()
        Thread.sleep(500) // let a (correctly) blocked write's absence settle before asserting

        // Then it stays unchecked — the cap blocks the addition
        composeRule.onNodeWithTag(rowTag).assertIsOff()
    }

    @Test
    fun foldersTabCheckboxReflectsWhetherTheFolderIsPlacedInThisFavoritesList() {
        // Given a folder that exists but isn't placed in favorites yet
        var folderId: Long? = null
        setContent { _, _, folderRepository -> folderId = folderRepository.createFolder("Games") }
        val id = requireNotNull(folderId)

        composeRule.onNodeWithTag("favorites_picker_tab_folders").performClick()
        // The tab switch now slides+fades the panel in — give it a moment to land before querying.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").assertIsOff() }.isSuccess
        }

        // When checking it
        composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").performClick()

        // Then it's placed — the checkbox reflects placement-in-this-list, not folder existence
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").assertIsOn() }.isSuccess
        }
    }

    @Test
    fun uncheckingAFolderInThePickerUnplacesItWithoutDeletingIt() {
        // Given a folder already placed in favorites
        var folderId: Long? = null
        setContent { _, favoriteAppRepository, folderRepository ->
            val id = folderRepository.createFolder("Games")
            folderId = id
            favoriteAppRepository.placeFolder(facetId, id, position = 0)
        }
        val id = requireNotNull(folderId)

        composeRule.onNodeWithTag("favorites_picker_tab_folders").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").assertIsOn() }.isSuccess
        }

        // When unchecking it
        composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").performClick()

        // Then it's un-placed from favorites, but the folder itself still exists (it would
        // simply not appear here at all if it had been deleted rather than un-placed)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("favorites_folder_picker_row_${id}_checkbox").assertIsOff() }.isSuccess
        }
    }

    /** No `facetId` — the launcher-wide default Favorites list, reached from Settings. */
    private fun setContentForDefaultFavorites(): Pair<AppRepository, DefaultFavoriteAppRepository> {
        lateinit var appRepository: AppRepository
        lateinit var defaultFavoriteAppRepository: DefaultFavoriteAppRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), folderRepository, appRepository)
                defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
                val usageStatsRepository = UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository)
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                FavoritesPickerViewModel(
                    SavedStateHandle(),
                    GetInstalledAppsUseCase(appRepository),
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
                    folderRepository,
                    SortAppsForPickerUseCase(usageStatsRepository),
                    usageAccessRepository,
                )
            }
            FacetLauncherTheme {
                FavoritesPickerScreen(onDone = {}, onNavigateToUsageAccessExplanation = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return appRepository to defaultFavoriteAppRepository
    }

    @Test
    fun withNoFacetIdTheScreenEditsTheLauncherWideDefaultList() {
        // Given the picker reached with no facetId (Settings' own "Default favorites" row)
        val (appRepository, _) = setContentForDefaultFavorites()

        // Then the header reads "Default favorites", not "Favorites" — uiState.isFacetScoped
        // defaults to true until the ViewModel's real combine() emits, so a single waitForIdle()
        // in the setup helper isn't guaranteed to have caught it yet (same StateFlow-vs-idling
        // gap already documented elsewhere in this suite)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Default favorites").assertExists() }.isSuccess
        }

        // When checking a real installed app — poll for its row, since the ViewModel's
        // installed-apps list comes from a real suspend fetch (same async-I/O-vs-idling gap as
        // checkingAnAppAddsItToFavorites above)
        val app = runBlocking { appRepository.getInstalledApps() }.first()
        val rowTag = "favorites_picker_row_${app.packageName}"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then it's reflected as checked, via the real production write path — with no
        // facetId, the ViewModel routes reads/writes through DefaultFavoriteAppRepository
        // rather than FavoriteAppRepository, so this proves it landed in the launcher-wide
        // default list, not a per-facet one. Deliberately not asserted by calling
        // defaultFavoriteAppRepository.observeDefaultFavorites() directly from the test: that
        // flow chains into AppRepository.observeInstalledApps()'s live LauncherApps.registerCallback,
        // which needs a Looper-bound thread — fine when collected via the ViewModel's own
        // viewModelScope (Main), but calling it via a bare runBlocking on the instrumentation
        // test's own thread (no Looper.prepare()) crashes the instrumentation process.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun sortDirectionToggleReversesAllAppsWithoutTouchingFavorites() {
        // Given one real app already favorited, and at least one more not favorited
        var favorite: AppInfo? = null
        setContent { appRepository, favoriteAppRepository, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            favorite = installed.first()
            favoriteAppRepository.addFavorite(facetId, installed.first(), 0)
        }
        val favoriteTag = "favorites_picker_row_${requireNotNull(favorite).packageName}"
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }
        val ascendingOrder = lastViewModel.uiState.value.otherResults.map { it.packageName }

        // When tapping the direction toggle — via the real rendered control, not the ViewModel
        // directly, so this exercises the actual click→ViewModel wiring
        composeRule.onNodeWithTag("favorites_picker_sort_direction_toggle").performClick()

        // Then "ALL APPS" reverses — read from the ViewModel's own state rather than measured
        // pixel positions: reversing can push a long real installed-app list's top rows to the
        // list's far (now off-screen) end, and LazyColumn only composes semantics nodes for rows
        // near the current viewport, making a position-based assertion here unreliable. The
        // already-favorited row is untouched by any of this — still checked, still rendered —
        // it lives in its own frozen-order section entirely separate from "ALL APPS"'s sort.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            lastViewModel.uiState.value.otherResults.map { it.packageName } == ascendingOrder.reversed()
        }
        composeRule.onNodeWithTag(favoriteTag).assertIsOn()
    }

    @Test
    fun sortMenuSelectingLastInstalledReordersAllAppsByInstallTime() {
        // Given at least one real installed app, not favorited
        setContent()
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }

        // When opening the sort dropdown (the real popup, not calling the ViewModel directly)
        // and tapping "Installed Date"
        composeRule.onNodeWithTag("favorites_picker_sort_menu_button").performClick()
        composeRule.onNodeWithTag("favorites_picker_sort_option_${AppSortOption.INSTALL_DATE.name}").performClick()

        // Then the ViewModel reflects the new selection...
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.sortOption == AppSortOption.INSTALL_DATE }
        // ...the dropdown button's own label updates to match...
        composeRule.onNodeWithText("Installed Date").assertExists()
        // ...and "ALL APPS" is genuinely ordered oldest-installed-first, not just relabeled
        val installTimes = lastViewModel.uiState.value.otherResults.map { it.firstInstallTime }
        assert(installTimes == installTimes.sorted())
    }
}
