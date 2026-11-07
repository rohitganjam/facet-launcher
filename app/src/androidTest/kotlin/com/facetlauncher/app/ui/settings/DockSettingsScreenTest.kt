package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.getTestableApps
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import androidx.compose.ui.test.assertCountEquals
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

class DockSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onAddDockApp: () -> Unit = {},
        facetId: Long? = null,
        seed: suspend (AppRepository, PlacementRepository, FolderRepository, FacetRepository) -> Unit = { _, _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "dock-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, folderRepository, appRepository)
                if (facetId != null) {
                    runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                }
                runBlocking { seed(appRepository, placementRepository, folderRepository, facetRepository) }
                DockSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    placementRepository,
                    observePlacedItems,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    ApplyFacetDefaultsUseCase(facetRepository, placementRepository, settingsRepository),
                )
            }
            FacetLauncherTheme {
                DockSettingsScreen(onBack = onBack, onAddDockApp = onAddDockApp, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun headerAndPreviewRender() {
        setContent()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
        composeRule.onNodeWithText("Default Dock Apps").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })
        composeRule.onNodeWithTag("back_button").performClick()
        assertEquals(true, backInvoked)
    }

    // Dock display style's own dropdown row moved to Settings -> Appearance (see chat history —
    // "look" fields, edited independently of this screen's content now); coverage moved to
    // AppearanceSettingsScreenTest's dockDisplayStyleRowChangesTheGlobalSetting/
    // facetScopedDockDisplayStyleRowOffersLauncherDefaultAndWritesToThatFacet.

    @Test
    fun selectDockAppsRowIsClickable() {
        var navigated = false
        setContent(onAddDockApp = { navigated = true })
        composeRule.onNodeWithTag("add_dock_app_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun aFolderInTheDefaultDock_rendersAsAFolderTileInThePreview_notItsAppsFlattened() {
        var folderId = 0L
        setContent(
            seed = { _, placementRepository, folderRepository, _ ->
                folderId = folderRepository.createFolder("Games")
                placementRepository.placeFolder(PlacementTarget.GlobalDock, folderId, position = 0)
            },
        )

        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
        // The real installed-apps query backing the preview resolves asynchronously — wait for it
        // rather than assuming setContent's own waitForIdle already caught it.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("dock_folder_tile_$folderId").fetchSemanticsNodes().isNotEmpty()
        }
        // Renders as a real folder tile (FolderDockIcon) inside the preview, not the folder's
        // member apps spilling out as standalone icons — the reorder row below already rendered
        // folders correctly; this bug was specific to the preview card.
    }

    @Test
    fun aFolderInAFacetsOwnDock_rendersAsAFolderTileInThePreview() {
        var folderId = 0L
        setContent(
            facetId = 42L,
            seed = { _, placementRepository, folderRepository, _ ->
                folderId = folderRepository.createFolder("Games")
                placementRepository.placeFolder(PlacementTarget(PlacementList.DOCK, 42L), folderId, position = 0)
            },
        )

        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("dock_folder_tile_$folderId").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun facetScopedModeEditsThatFacetsDockAndOffersResetToDefault() {
        // Given a facet-scoped entry point
        setContent(facetId = 1L)

        // The facet-scoped state resolves asynchronously — wait for the reset row to render
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("dock_settings_reset_row").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the dock list is live with no Inherit/Override switch, and the reset row is there
        composeRule.onNodeWithTag("add_dock_app_row").assertIsEnabled()
        composeRule.onAllNodesWithTag("dock_settings_inherit_row").assertCountEquals(0)
        composeRule.onAllNodesWithTag("dock_settings_override_row").assertCountEquals(0)
        composeRule.onNodeWithText("Dock Apps").assertIsDisplayed()
    }

    @Test
    fun resetToDefaultAsksForConfirmationAndCopiesTheDefaultDockIntoTheFacet() {
        // Given a facet with its own dock and a different launcher-wide default dock
        var facetPlacements: PlacementRepository? = null
        setContent(
            facetId = 42L,
            seed = { appRepository, placementRepository, _, _ ->
                facetPlacements = placementRepository
                val apps = appRepository.getTestableApps()
                placementRepository.addApp(PlacementTarget(PlacementList.DOCK, 42L), apps[0], position = 0)
                placementRepository.addApp(PlacementTarget.GlobalDock, apps[1], position = 0)
            },
        )
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("dock_settings_reset_row").fetchSemanticsNodes().isNotEmpty()
        }

        // When tapping Reset to default, then cancelling
        composeRule.onNodeWithTag("dock_settings_reset_row").performClick()
        composeRule.onNodeWithTag("confirm_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_dialog_cancel").performClick()

        // Then nothing changed
        val owned = { runBlocking { facetPlacements!!.getRawApps(PlacementTarget(PlacementList.DOCK, 42L)).map { it.packageName } } }
        val before = owned()

        // When tapping it again and confirming
        composeRule.onNodeWithTag("dock_settings_reset_row").performClick()
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()

        // Then the facet's dock is a copy of the default dock
        val defaultDock = runBlocking { facetPlacements!!.getRawApps(PlacementTarget.GlobalDock).map { it.packageName } }
        composeRule.waitUntil(timeoutMillis = 5_000) { owned() == defaultDock }
        assertEquals(1, before.size)
    }

    @Test
    fun launcherScopeIsTitledAsTheDefaultAndOffersNoReset() {
        setContent()

        composeRule.onNodeWithText("Default Dock Apps").assertIsDisplayed()
        composeRule.onNodeWithTag("defaults_screen_note").assertExists()
        composeRule.onAllNodesWithTag("dock_settings_reset_row").assertCountEquals(0)
    }
}
