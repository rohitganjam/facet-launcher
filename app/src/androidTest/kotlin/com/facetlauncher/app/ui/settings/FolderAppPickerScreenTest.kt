package com.facetlauncher.app.ui.settings

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
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

/**
 * A folder's own "Add to folder" picker — apps only, no cap, no Folders tab (a folder can't
 * contain another folder). Same real-repository-over-in-memory-Room setup as
 * `DockAppPickerScreenTest`/`FavoritesPickerScreenTest`.
 */
class FolderAppPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var lastViewModel: FolderAppPickerViewModel

    /** [seed] runs against real (throwaway, in-memory) repositories before the screen renders. */
    private fun setContent(
        onDone: () -> Unit = {},
        seed: suspend (AppRepository, FolderRepository, Long) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val folderId = runBlocking {
                    val id = folderRepository.createFolder("Games")
                    seed(appRepository, folderRepository, id)
                    id
                }
                val usageStatsRepository = UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository)
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                FolderAppPickerViewModel(
                    SavedStateHandle(mapOf("folderId" to folderId)),
                    GetInstalledAppsUseCase(appRepository),
                    folderRepository,
                    SortAppsForPickerUseCase(usageStatsRepository),
                    usageAccessRepository,
                ).also { lastViewModel = it }
            }
            FacetLauncherTheme {
                FolderAppPickerScreen(onDone = onDone, onNavigateToUsageAccessExplanation = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun headerShowsTheFolderName() {
        setContent()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Games").assertExists() }.isSuccess
        }
    }

    @Test
    fun checkingAnAppAddsItToTheFolder() {
        // Given the picker, searched down to some real installed app
        var targetApp: AppInfo? = null
        setContent { appRepository, _, _ -> targetApp = runBlocking { appRepository.getInstalledApps() }.first() }
        val app = requireNotNull(targetApp)
        val rowTag = "folder_app_picker_row_${app.packageName}"

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("folder_app_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When it's checked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then its checkbox reflects the new folder membership
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun uncheckingRemovesItFromTheFolderWithoutDeletingTheFolder() {
        // Given an app already in the folder
        var seededApp: AppInfo? = null
        setContent { appRepository, folderRepository, folderId ->
            val app = appRepository.getInstalledApps().first()
            seededApp = app
            folderRepository.addAppToFolder(folderId, app)
        }
        val app = requireNotNull(seededApp)
        val rowTag = "folder_app_picker_row_${app.packageName}"

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }

        // When it's unchecked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then it's removed from the folder
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOff() }.isSuccess
        }
    }

    @Test
    fun inFolderAppsRenderFirstUnderTheirOwnSectionHeader() {
        // Given two real apps already in the folder
        var folderApps: List<AppInfo> = emptyList()
        setContent { appRepository, folderRepository, folderId ->
            val installed = appRepository.getInstalledApps()
            folderApps = installed.take(2)
            folderApps.forEach { app -> folderRepository.addAppToFolder(folderId, app) }
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("IN FOLDER").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("ALL APPS").assertExists()
        folderApps.forEach { app ->
            composeRule.onNodeWithTag("folder_app_picker_row_${app.packageName}").assertIsOn()
        }
    }

    @Test
    fun sortDirectionToggleReversesTheAllAppsSectionOrder() {
        // Given at least one real installed app, none placed in the folder
        setContent { _, _, _ -> }
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }
        val ascendingOrder = lastViewModel.uiState.value.otherResults.map { it.packageName }

        // When tapping the direction toggle — via the real rendered control, not the ViewModel
        // directly, so this exercises the actual click→ViewModel wiring
        composeRule.onNodeWithTag("folder_app_picker_sort_direction_toggle").performClick()

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
        // Given at least one real installed app, none placed in the folder
        setContent { _, _, _ -> }
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.otherResults.isNotEmpty() }

        // When opening the sort dropdown (the real popup, not calling the ViewModel directly)
        // and tapping "Installed Date"
        composeRule.onNodeWithTag("folder_app_picker_sort_menu_button").performClick()
        composeRule.onNodeWithTag("folder_app_picker_sort_option_${AppSortOption.INSTALL_DATE.name}").performClick()

        // Then the ViewModel reflects the new selection...
        composeRule.waitUntil(timeoutMillis = 3_000) { lastViewModel.uiState.value.sortOption == AppSortOption.INSTALL_DATE }
        // ...the dropdown button's own label updates to match...
        composeRule.onNodeWithText("Installed Date").assertExists()
        // ...and "ALL APPS" is genuinely ordered oldest-installed-first, not just relabeled
        val installTimes = lastViewModel.uiState.value.otherResults.map { it.firstInstallTime }
        assert(installTimes == installTimes.sorted())
    }
}
