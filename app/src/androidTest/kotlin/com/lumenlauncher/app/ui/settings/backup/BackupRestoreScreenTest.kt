package com.lumenlauncher.app.ui.settings.backup

import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.BackupRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.data.widget.LauncherAppWidgetHost
import com.lumenlauncher.app.domain.ExportBackupUseCase
import com.lumenlauncher.app.domain.ImportBackupUseCase
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
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
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val profileRepository = ProfileRepository(database.profileDao())
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val profileDockAppRepository = ProfileDockAppRepository(database.profileDockAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val backupRepository = BackupRepository(context)
                val appWidgetRepository = AppWidgetRepository(context, AppWidgetManager.getInstance(context), LauncherAppWidgetHost(context))
                BackupRestoreViewModel(
                    ExportBackupUseCase(
                        settingsRepository, profileRepository, favoriteAppRepository, dockAppRepository,
                        profileDockAppRepository, defaultFavoriteAppRepository, widgetPlacementRepository, backupRepository,
                    ),
                    ImportBackupUseCase(
                        backupRepository, settingsRepository, profileRepository, favoriteAppRepository,
                        dockAppRepository, profileDockAppRepository, defaultFavoriteAppRepository,
                    ),
                    appWidgetRepository,
                    widgetPlacementRepository,
                    PlaceWidgetUseCase(),
                )
            }
            LumenLauncherTheme {
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
