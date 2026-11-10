package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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

    private lateinit var settingsRepository: SettingsRepository

    private fun setContent(
        onBack: () -> Unit = {},
        onAddDockApp: () -> Unit = {},
        facetId: Long = 1L,
        seed: suspend (AppRepository, PlacementRepository, FolderRepository, FacetRepository) -> Unit = { _, _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
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
                runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                runBlocking { seed(appRepository, placementRepository, folderRepository, facetRepository) }
                DockSettingsViewModel(
                    SavedStateHandle(mapOf("facetId" to facetId)),
                    settingsRepository,
                    facetRepository,
                    placementRepository,
                    observePlacedItems,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    ApplyFacetDefaultsUseCase(facetRepository, com.facetlauncher.app.domain.SeedFacetDefaultsUseCase(com.facetlauncher.app.data.DefaultAppRepository(context), placementRepository, com.facetlauncher.app.domain.GetInstalledAppsUseCase(appRepository)), settingsRepository),
                    com.facetlauncher.app.domain.CopyFacetSettingsUseCase(placementRepository, facetRepository),
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
        composeRule.onNodeWithText("Dock Apps").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })
        composeRule.onNodeWithTag("back_button").performClick()
        assertEquals(true, backInvoked)
    }

    @Test
    fun dockDisplayStyleRowOffersIconsAndTextButNoLauncherDefaultChoice() {
        setContent()
        composeRule.onNodeWithTag("dock_settings_screen").performScrollToNode(hasTestTag("dock_display_style_row"))

        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("dock_display_style_row_option_ICONS_AND_TEXT").assertExists()
        composeRule.onAllNodesWithTag("dock_display_style_row_option_LAUNCHER_DEFAULT").assertCountEquals(0)
    }

    @Test
    fun facetScopedDockDisplayStyleRowWritesToThatFacetNotTheDefault() {
        // Given a facet's own dock screen
        setContent(facetId = 1L)
        composeRule.onNodeWithTag("dock_settings_screen").performScrollToNode(hasTestTag("dock_display_style_row"))

        // When picking "Text"
        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_display_style_row_option_TEXT").performClick()

        // Then the facet shows Text and the launcher-wide default is untouched
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("dock_display_style_row").assertTextContains("Text") }.isSuccess
        }
        assertEquals(com.facetlauncher.app.data.model.DockDisplayMode.ICONS, runBlocking { settingsRepository.settings.first().dockDisplayMode })
    }

    @Test
    fun selectDockAppsRowIsClickable() {
        var navigated = false
        setContent(onAddDockApp = { navigated = true })
        composeRule.onNodeWithTag("add_dock_app_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun aFolderInTheDock_rendersAsAFolderTileInThePreview_notItsAppsFlattened() {
        var folderId = 0L
        setContent(
            seed = { _, placementRepository, folderRepository, _ ->
                folderId = folderRepository.createFolder("Games")
                placementRepository.placeFolder(PlacementTarget(PlacementList.DOCK, 1L), folderId, position = 0)
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

        // Then the dock list is live, and the title is the facet's
        composeRule.onNodeWithTag("add_dock_app_row").assertIsEnabled()
        composeRule.onNodeWithText("Dock Apps").assertIsDisplayed()
    }

    @Test
    fun resetToDefaultAsksForConfirmationAndReseedsTheDockFromTheDeviceDefaults() {
        // Given a facet whose own dock holds a stale app that is not installed
        var facetPlacements: PlacementRepository? = null
        setContent(
            facetId = 42L,
            seed = { _, placementRepository, _, _ ->
                facetPlacements = placementRepository
                placementRepository.restoreApp(
                    com.facetlauncher.app.data.local.AppPlacementEntity(list = PlacementList.DOCK, facetId = 42L, packageName = "com.example.stale", activityName = ".Main", position = 0),
                )
            },
        )
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("dock_settings_reset_row").fetchSemanticsNodes().isNotEmpty()
        }
        val owned = { runBlocking { facetPlacements!!.getRawApps(PlacementTarget(PlacementList.DOCK, 42L)).map { it.packageName } } }

        // When tapping Reset to default, then cancelling, nothing changes
        composeRule.onNodeWithTag("dock_settings_reset_row").performClick()
        composeRule.onNodeWithTag("confirm_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_dialog_cancel").performClick()
        assertEquals(listOf("com.example.stale"), owned())

        // When tapping it again and confirming
        composeRule.onNodeWithTag("dock_settings_reset_row").performClick()
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()

        // Then the stale app is gone: the dock holds the device's default apps instead
        composeRule.waitUntil(timeoutMillis = 5_000) { "com.example.stale" !in owned() }
    }
}
