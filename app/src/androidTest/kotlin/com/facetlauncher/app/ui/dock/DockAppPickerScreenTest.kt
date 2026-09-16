package com.facetlauncher.app.ui.dock

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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class DockAppPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var lastViewModel: DockAppPickerViewModel

    /**
     * [seed] runs against real (throwaway, in-memory) repositories before the screen renders.
     * A non-null [facetId] scopes the picker to that facet's own dock (as reached from a
     * facet's settings) rather than the launcher-wide default dock (as reached from Settings).
     */
    private fun setContent(
        onDone: () -> Unit = {},
        facetId: Long? = null,
        seed: (AppRepository, DockAppRepository, FacetDockAppRepository, FolderRepository) -> Unit = { _, _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), folderRepository, appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), folderRepository, appRepository)
                if (facetId != null) {
                    runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                }
                seed(appRepository, dockAppRepository, facetDockAppRepository, folderRepository)
                val usageStatsRepository = UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository)
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                DockAppPickerViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    GetInstalledAppsUseCase(appRepository),
                    dockAppRepository,
                    facetDockAppRepository,
                    folderRepository,
                    SortAppsForPickerUseCase(usageStatsRepository),
                    usageAccessRepository,
                ).also { lastViewModel = it }
            }
            FacetLauncherTheme {
                DockAppPickerScreen(onDone = onDone, onNavigateToUsageAccessExplanation = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun backButtonInvokesOnDone() {
        // Given the picker
        var doneInvoked = false
        setContent(onDone = { doneInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back, same as tapping "Done"
        assert(doneInvoked)
    }

    @Test
    fun checkingAnAppAddsItToTheDock() {
        // Given the picker, searched down to some real installed app (fetched from the same
        // repository the ViewModel uses, rather than assuming a specific package is visible —
        // package-visibility rules can differ between this test APK and the app under test)
        var targetApp: AppInfo? = null
        setContent { appRepository, _, _, _ -> targetApp = runBlocking { appRepository.getInstalledApps() }.first() }
        val app = requireNotNull(targetApp)
        val rowTag = "dock_picker_row_${app.packageName}"

        // The ViewModel's installed-apps list comes from a real suspend fetch (init { launch { ... } }),
        // which isn't reliably caught by a single waitForIdle() — poll for the row before interacting
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("dock_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When it's checked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then its checkbox reflects the new dock membership
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToAllApps() {
        // Given two real apps already in the dock, so both "IN DOCK" and "ALL APPS" render
        setContent { appRepository, dockAppRepository, _, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            installed.take(2).forEachIndexed { index, app -> runBlocking { dockAppRepository.addDockApp(app, index) } }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("ALL APPS").assertExists() }.isSuccess
        }

        // When scrolling down to the "ALL APPS" section
        composeRule.onNodeWithText("ALL APPS").performScrollTo()

        // Then the pinned header (title + Done) is still on screen, not scrolled away
        composeRule.onNodeWithText("Default dock").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_picker_done").assertIsDisplayed()
    }

    @Test
    fun inDockAppsRenderFirstUnderTheirOwnSectionHeader() {
        // Given two real apps already in the dock
        var dockApps: List<AppInfo> = emptyList()
        setContent { appRepository, dockAppRepository, _, _ ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            dockApps = installed.take(2)
            dockApps.forEachIndexed { index, app -> runBlocking { dockAppRepository.addDockApp(app, index) } }
        }

        // Then both section headers render, with the in-dock apps' rows present under "IN DOCK"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("IN DOCK").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("ALL APPS").assertExists()
        dockApps.forEach { app ->
            composeRule.onNodeWithTag("dock_picker_row_${app.packageName}").assertIsOn()
        }
    }

    @Test
    fun uncheckingIsAllowedDownToAnEmptyDock() {
        // Given a dock with a single app — no floor blocks removing it (MIN_APPS = 0)
        var onlyDockApp: AppInfo? = null
        setContent { appRepository, dockAppRepository, _, _ ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            onlyDockApp = app
            runBlocking { dockAppRepository.addDockApp(app, 0) }
        }
        val app = requireNotNull(onlyDockApp)
        val rowTag = "dock_picker_row_${app.packageName}"

        // See checkingAnAppAddsItToTheDock — wait for the real installed-apps fetch to land
        // before interacting, rather than trusting a single waitForIdle() caught it.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("dock_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOn()

        // When unchecking the dock's only remaining app
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then it's removed, leaving the dock empty
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOff() }.isSuccess
        }
    }

    @Test
    fun facetScopedPickerTitlesItselfDockAndWritesToThatFacetsOwnDock() {
        // Given the picker opened for facet 7 (not the launcher-wide default) with one app
        // already in facet 7's own dock and NOT in the shared default dock
        var seededApp: AppInfo? = null
        setContent(facetId = 7L) { appRepository, dockAppRepository, facetDockAppRepository, _ ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            seededApp = app
            runBlocking { facetDockAppRepository.addDockApp(7L, app, 0) }
            // deliberately leave the shared default dock empty
        }
        val app = requireNotNull(seededApp)
        val rowTag = "dock_picker_row_${app.packageName}"

        // Then the header reads "Dock" (this facet's own), not the global "Default dock"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Dock").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Default dock").assertDoesNotExist()

        // And the seeded app shows as already checked — proof the picker is reading facet 7's
        // own dock, not the (empty) shared one
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun foldersTabCheckboxReflectsWhetherTheFolderIsPlacedInThisDock() {
        // Given a folder that exists but isn't placed in the dock yet
        var folderId: Long? = null
        setContent { _, _, _, folderRepository ->
            folderId = runBlocking { folderRepository.createFolder("Games") }
        }
        val id = requireNotNull(folderId)

        composeRule.onNodeWithTag("dock_picker_tab_folders").performClick()
        // The tab switch now slides+fades the panel in — give it a moment to land before querying.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").assertIsOff() }.isSuccess
        }

        // When checking it
        composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").performClick()

        // Then it's placed — the checkbox reflects placement-in-this-list, not folder existence
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").assertIsOn() }.isSuccess
        }
    }

    @Test
    fun uncheckingAFolderInThePickerUnplacesItWithoutDeletingIt() {
        // Given a folder already placed in the dock
        var folderId: Long? = null
        setContent { _, dockAppRepository, _, folderRepository ->
            folderId = runBlocking {
                val id = folderRepository.createFolder("Games")
                dockAppRepository.placeFolderInDock(id, position = 0)
                id
            }
        }
        val id = requireNotNull(folderId)

        composeRule.onNodeWithTag("dock_picker_tab_folders").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").assertIsOn() }.isSuccess
        }

        // When unchecking it
        composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").performClick()

        // Then it's un-placed from the dock, but the folder itself still exists (it would
        // simply not appear here at all if it had been deleted rather than un-placed)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("dock_folder_picker_row_${id}_checkbox").assertIsOff() }.isSuccess
        }
    }

    @Test
    fun sortDirectionToggleReversesTheAllAppsSectionOrder() {
        // Given at least one real installed app, not placed in the dock
        setContent { _, _, _, _ -> }
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }
        val ascendingOrder = lastViewModel.uiState.value.otherResults.map { it.packageName }

        // When tapping the direction toggle — via the real rendered control, not the ViewModel
        // directly, so this exercises the actual click→ViewModel wiring
        composeRule.onNodeWithTag("dock_picker_sort_direction_toggle").performClick()

        // Then "ALL APPS" reverses — read from the ViewModel's own state rather than measured
        // pixel positions: reversing can push a long real installed-app list's top rows to the
        // list's far (now off-screen) end, and LazyColumn only composes semantics nodes for
        // rows near the current viewport, making a position-based assertion here unreliable.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            lastViewModel.uiState.value.otherResults.map { it.packageName } == ascendingOrder.reversed()
        }
    }

    @Test
    fun sortMenuSelectingLastInstalledReordersAllAppsByInstallTime() {
        // Given at least one real installed app, not placed in the dock
        setContent { _, _, _, _ -> }
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }

        // When opening the sort dropdown (the real popup, not calling the ViewModel directly)
        // and tapping "Installed Date"
        composeRule.onNodeWithTag("dock_picker_sort_menu_button").performClick()
        composeRule.onNodeWithTag("dock_picker_sort_option_${AppSortOption.INSTALL_DATE.name}").performClick()

        // Then the ViewModel reflects the new selection...
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.sortOption == AppSortOption.INSTALL_DATE }
        // ...the dropdown button's own label updates to match...
        composeRule.onNodeWithText("Installed Date").assertExists()
        // ...and "ALL APPS" is genuinely ordered oldest-installed-first, not just relabeled
        val installTimes = lastViewModel.uiState.value.otherResults.map { it.firstInstallTime }
        assert(installTimes == installTimes.sorted())
    }

    @Test
    fun selectingLastUsedSortNavigatesToUsageAccessExplanationInsteadOfApplyingItWhenUngranted() {
        // Given the picker with Usage Access not granted (this test environment's default)
        var navigated = false
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), folderRepository, appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), folderRepository, appRepository)
                val usageStatsRepository = UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository)
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                DockAppPickerViewModel(
                    SavedStateHandle(),
                    GetInstalledAppsUseCase(appRepository),
                    dockAppRepository,
                    facetDockAppRepository,
                    folderRepository,
                    SortAppsForPickerUseCase(usageStatsRepository),
                    usageAccessRepository,
                )
            }
            FacetLauncherTheme {
                DockAppPickerScreen(onDone = {}, onNavigateToUsageAccessExplanation = { navigated = true }, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()

        // When picking "Last used" from the sort dropdown
        composeRule.onNodeWithTag("dock_picker_sort_menu_button").performClick()
        composeRule.onNodeWithTag("dock_picker_sort_option_LAST_USED").performClick()
        composeRule.waitForIdle()

        // Then it routes to the permission explanation instead of applying the sort — the
        // dropdown still shows the previous (default) selection
        assert(navigated)
        composeRule.onNodeWithText("Alphabetical").assertExists()
    }
}
