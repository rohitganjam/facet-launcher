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
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
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

class DockSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onAddDockApp: () -> Unit = {},
        facetId: Long? = null,
        seed: suspend (AppRepository, DockAppRepository, FacetDockAppRepository, FolderRepository, FacetRepository) -> Unit = { _, _, _, _, _ -> },
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
                val facetRepository = FacetRepository(database.facetDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), folderRepository, appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), folderRepository, appRepository)
                if (facetId != null) {
                    runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                }
                runBlocking { seed(appRepository, dockAppRepository, facetDockAppRepository, folderRepository, facetRepository) }
                DockSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    dockAppRepository,
                    facetDockAppRepository,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
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
        composeRule.onNodeWithText("Dock").assertIsDisplayed()
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
            seed = { _, dockAppRepository, _, folderRepository, _ ->
                folderId = folderRepository.createFolder("Games")
                dockAppRepository.placeFolderInDock(folderId, position = 0)
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
        // This facet must actually be overriding for its own dock to be the one shown — otherwise
        // the screen correctly shows the (empty) launcher-wide default instead (see chat history:
        // editing while inheriting used to silently write without taking effect).
        var folderId = 0L
        setContent(
            facetId = 42L,
            seed = { _, _, facetDockAppRepository, folderRepository, facetRepository ->
                folderId = folderRepository.createFolder("Games")
                facetDockAppRepository.placeFolder(42L, folderId, position = 0)
                facetRepository.getById(42L)?.let { facetRepository.updateOverridingDock(it, true) }
            },
        )

        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("dock_folder_tile_$folderId").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun facetScopedModeShowsInheritOverrideSwitch() {
        // Given a facet-scoped entry point
        setContent(facetId = 1L)

        // The facet-scoped state resolves asynchronously — wait for the switch to actually render
        // rather than assuming setContent()'s own waitForIdle() already caught it (see chat history)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("dock_settings_override_row").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the inherit/override switch is present, defaulting to Inherit (controls disabled) —
        // dockDisplayMode's own row moved to Appearance (see chat history), so this now gates the
        // one control still left on this screen: the dock app list picker row.
        composeRule.onNodeWithTag("dock_settings_inherit_row").assertExists()
        composeRule.onNodeWithTag("dock_settings_override_row").assertExists()
        composeRule.onNodeWithTag("add_dock_app_row").assertIsNotEnabled()

        // When switching to Override
        composeRule.onNodeWithTag("dock_settings_override_row").performClick()

        // Then the controls become live
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("add_dock_app_row").assertIsEnabled() }.isSuccess
        }
    }
}
