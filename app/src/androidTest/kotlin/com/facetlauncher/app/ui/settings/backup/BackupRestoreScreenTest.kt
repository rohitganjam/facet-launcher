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
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import com.facetlauncher.app.domain.ExportBackupUseCase
import com.facetlauncher.app.domain.ImportBackupUseCase
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import com.facetlauncher.app.domain.RepairOrphanedProfileRowsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import android.net.Uri
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.model.BackupAutomationRule
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.domain.CanUseTriggerUseCase
import com.facetlauncher.app.domain.RestoreAutomationRulesUseCase
import com.facetlauncher.app.ui.FakeEntitlementRepository
import com.facetlauncher.app.ui.testAutomation
import com.facetlauncher.app.ui.testAutomationStateRepository
import kotlinx.coroutines.runBlocking
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase

class BackupRestoreScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: BackupRestoreViewModel
    private lateinit var exportBackup: ExportBackupUseCase
    private lateinit var backupRepository: BackupRepository

    private fun setContent(onBack: () -> Unit = {}, onOpenFacetAutomation: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val screenViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-backup-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, folderRepository, appRepository)
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                backupRepository = BackupRepository(context)
                val appWidgetRepository = AppWidgetRepository(context, AppWidgetManager.getInstance(context), LauncherAppWidgetHost(context), context.getSystemService(UserManager::class.java), appRepository)
                val automationRules = AutomationRuleRepository(database.automationRuleDao(), database.facetDao())
                val entitlement = FakeEntitlementRepository()
                val restoreAutomationRules = RestoreAutomationRulesUseCase(
                    automationRules, testAutomationStateRepository(context),
                    testAutomation(context, database, facetRepository, settingsRepository, entitlement).activate,
                    CanUseTriggerUseCase(), entitlement, AutomationPermissionRepository(context),
                )
                exportBackup = ExportBackupUseCase(
                    settingsRepository, facetRepository, placementRepository, widgetPlacementRepository, folderRepository, backupRepository, automationRules,
                )
                BackupRestoreViewModel(
                    exportBackup,
                    ImportBackupUseCase(
                        backupRepository, settingsRepository, facetRepository, placementRepository, folderRepository,
                        RepairOrphanedProfileRowsUseCase(
                            appRepository, placementRepository, folderRepository,
                        ),
                        ClockStackRepository(database, database.clockStackItemDao()),
                        widgetPlacementRepository,
                        restoreAutomationRules,
                    ),
                    appRepository,
                    appWidgetRepository,
                    widgetPlacementRepository,
                    PlaceWidgetUseCase(),
                )
            }
            viewModel = screenViewModel
            FacetLauncherTheme {
                BackupRestoreScreen(onBack = onBack, viewModel = screenViewModel, onOpenFacetAutomation = onOpenFacetAutomation)
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

    /** Writes this phone's own (empty) backup changed by [edit] to a file, then imports it through the screen's ViewModel. */
    private fun importEdited(edit: (BackupBundle) -> BackupBundle) {
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "backup-test-${System.nanoTime()}.json")
        val uri = Uri.fromFile(file)
        runBlocking { backupRepository.writeBackup(uri, edit(exportBackup(uri))) }
        composeRule.runOnIdle { viewModel.onImportFileChosen(uri) }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("automation_review").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun facet(uuid: String) = BackupFacet(
        name = "Work", position = 0, uuid = uuid, appRowPosition = "LEFT", appRowPresentation = "ICON_AND_TEXT",
        listContentMode = "FAVORITES", appsToShowCount = 5, favorites = emptyList(),
    )

    @Test
    fun aRestoredRuleWhoseFacetIsMissingIsListedAsSkippedWithALinkToAutomation() {
        // Given the screen, and a backup with one rule on its facet and one on a facet it doesn't have
        var opened = false
        setContent(onOpenFacetAutomation = { opened = true })

        // When importing it
        importEdited { bundle ->
            bundle.copy(
                facets = listOf(facet("work-uuid")),
                automationRules = listOf(
                    BackupAutomationRule(targetFacetUuid = "work-uuid", endBehavior = "STAY", triggerType = "HEADPHONES"),
                    BackupAutomationRule(targetFacetUuid = "gone-uuid", endBehavior = "STAY", triggerType = "HEADPHONES"),
                ),
            )
        }

        // Then the skipped rule is explained and Facet automation is one tap away
        composeRule.onNodeWithText("1 rule skipped: its facet isn't in this backup").assertIsDisplayed()
        composeRule.onNodeWithTag("open_facet_automation_button").performClick()
        assertEquals(true, opened)
    }

    @Test
    fun aBackupMadeBeforeRulesWereBackedUpSaysItHasNone() {
        // Given the screen
        setContent()

        // When importing a backup with no rule list at all
        importEdited { bundle -> bundle.copy(facets = listOf(facet("work-uuid")), automationRules = null) }

        // Then the screen says so, with nothing to open
        composeRule.onNodeWithText("This backup has no automation rules: it was made before Facet backed them up.").assertIsDisplayed()
        composeRule.onNodeWithTag("open_facet_automation_button").assertDoesNotExist()
    }
}
