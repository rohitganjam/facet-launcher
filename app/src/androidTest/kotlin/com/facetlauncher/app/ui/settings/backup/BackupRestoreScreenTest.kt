package com.facetlauncher.app.ui.settings.backup

import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import com.facetlauncher.app.domain.ExportBackupUseCase
import com.facetlauncher.app.domain.ImportBackupUseCase
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import com.facetlauncher.app.domain.RepairOrphanedProfileRowsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BackupRestoreScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-backup-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val facetRepository = FacetRepository(database.facetDao())
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), folderRepository, appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), folderRepository, appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), folderRepository, appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), folderRepository, appRepository)
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val backupRepository = BackupRepository(context)
                val appWidgetRepository = AppWidgetRepository(context, AppWidgetManager.getInstance(context), LauncherAppWidgetHost(context), context.getSystemService(UserManager::class.java), appRepository)
                BackupRestoreViewModel(
                    ExportBackupUseCase(
                        settingsRepository, facetRepository, favoriteAppRepository, dockAppRepository,
                        facetDockAppRepository, defaultFavoriteAppRepository, widgetPlacementRepository, folderRepository, backupRepository,
                    ),
                    ImportBackupUseCase(
                        backupRepository, settingsRepository, facetRepository, favoriteAppRepository,
                        dockAppRepository, facetDockAppRepository, defaultFavoriteAppRepository, folderRepository,
                        RepairOrphanedProfileRowsUseCase(
                            appRepository, dockAppRepository, facetDockAppRepository, favoriteAppRepository,
                            defaultFavoriteAppRepository, folderRepository,
                        ),
                    ),
                    appRepository,
                    appWidgetRepository,
                    widgetPlacementRepository,
                    PlaceWidgetUseCase(),
                )
            }
            FacetLauncherTheme {
                BackupRestoreScreen(onBack = onBack, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun rendersExportAndImportRows() {
        setContent()

        composeRule.onNodeWithText("Backup & restore").assertIsDisplayed()
        composeRule.onNodeWithTag("export_backup_row").assertIsDisplayed()
        composeRule.onNodeWithTag("import_backup_row").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })

        composeRule.onNodeWithTag("back_button").performClick()

        assertEquals(true, backInvoked)
    }

    @Test
    fun tappingImportShowsAConfirmationBeforeAnyFilePickerLaunches() {
        // Given the screen
        setContent()

        // When tapping Import
        composeRule.onNodeWithTag("import_backup_row").performClick()

        // Then a destructive-replace confirmation shows first — the real file picker is a
        // separate system Activity outside this Compose tree, not exercised here
        composeRule.onNodeWithTag("confirm_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Replace").assertIsDisplayed()
    }

    @Test
    fun cancellingTheImportConfirmationDismissesItWithoutLaunchingAnything() {
        // Given the confirmation is showing
        setContent()
        composeRule.onNodeWithTag("import_backup_row").performClick()
        composeRule.onNodeWithTag("confirm_dialog").assertIsDisplayed()

        // When cancelling
        composeRule.onNodeWithTag("confirm_dialog_cancel").performClick()

        // Then it's gone
        composeRule.onNodeWithTag("confirm_dialog").assertDoesNotExist()
    }
}
