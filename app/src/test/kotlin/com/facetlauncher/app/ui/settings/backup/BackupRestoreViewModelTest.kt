package com.facetlauncher.app.ui.settings.backup

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Process
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.ExportBackupUseCase
import com.facetlauncher.app.domain.ImportBackupResult
import com.facetlauncher.app.domain.ImportBackupUseCase
import com.facetlauncher.app.domain.PlaceWidgetResult
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric — plain JUnit's `android.jar` stubs throw on real `ComponentName`/`AppWidgetProviderInfo` field access (used throughout below), same reason `HubWidgetPickerViewModelTest` needs it. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BackupRestoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private val exportBackup = mock(ExportBackupUseCase::class.java)
    private val importBackup = mock(ImportBackupUseCase::class.java)
    private val appRepository = mock(AppRepository::class.java).also {
        // The common case every test but the "no live handle" one assumes — a backup's own
        // recorded profile (defaulted to PERSONAL) resolves to this process's own handle.
        `when`(it.resolveUserHandle(AppProfile.PERSONAL)).thenReturn(Process.myUserHandle())
    }
    private val appWidgetRepository = mock(AppWidgetRepository::class.java)
    private val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
    private val placeWidget = mock(PlaceWidgetUseCase::class.java)

    private val viewModel = BackupRestoreViewModel(exportBackup, importBackup, appRepository, appWidgetRepository, widgetPlacementRepository, placeWidget)

    private val provider = ComponentName("com.example.widgets", ".Provider")
    private val placement = BackupWidgetPlacement("com.example.widgets", ".Provider", row = 1, col = 2, colSpan = 2, rowSpan = 1)

    @Test
    fun `a null export destination (picker cancelled) is a no-op`() = runTest {
        viewModel.onExportDestinationChosen(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BackupRestoreUiState(), viewModel.uiState.value)
    }

    @Test
    fun `a successful export shows the success message`() = runTest {
        val uri = mock(Uri::class.java)

        viewModel.onExportDestinationChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BackupRestoreMessage.ExportSucceeded, viewModel.uiState.value.message)
        assertEquals(false, viewModel.uiState.value.isBusy)
    }

    @Test
    fun `an export that throws shows the failure message instead of propagating`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(exportBackup(uri)).thenThrow(RuntimeException("disk full"))

        viewModel.onExportDestinationChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BackupRestoreMessage.ExportFailed, viewModel.uiState.value.message)
    }

    @Test
    fun `an invalid backup file shows the invalid-file message`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.InvalidFile)

        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BackupRestoreMessage.ImportFailedInvalidFile, viewModel.uiState.value.message)
    }

    @Test
    fun `a backup from a newer app version shows the unsupported-version message`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.UnsupportedVersion(99))

        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BackupRestoreMessage.ImportFailedUnsupportedVersion(99), viewModel.uiState.value.message)
    }

    @Test
    fun `a successful import with a pending widget whose provider is installed marks it re-addable`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.Success(facetCount = 1, dockAppCount = 0, defaultFavoriteCount = 0, pendingWidgetPlacements = listOf(placement)))
        `when`(appWidgetRepository.getWidgetProviderOptions()).thenReturn(
            listOf(WidgetProviderOption(provider = provider, appLabel = "Widgets Inc", widgetLabel = "Big Widget", columns = 2, rows = 1)),
        )

        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val pending = viewModel.uiState.value.pendingWidgets
        assertEquals(1, pending.size)
        assertTrue(pending[0].providerAvailable)
        assertEquals("Widgets Inc", pending[0].appLabel)
        assertEquals(false, pending[0].done)
    }

    @Test
    fun `a pending widget whose provider isn't installed is marked unavailable, not re-addable`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.Success(facetCount = 1, dockAppCount = 0, defaultFavoriteCount = 0, pendingWidgetPlacements = listOf(placement)))
        `when`(appWidgetRepository.getWidgetProviderOptions()).thenReturn(emptyList())

        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.pendingWidgets[0].providerAvailable)
    }

    private fun givenOnePendingWidget() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.Success(1, 0, 0, listOf(placement)))
        `when`(appWidgetRepository.getWidgetProviderOptions()).thenReturn(
            listOf(WidgetProviderOption(provider = provider, appLabel = "Widgets Inc", widgetLabel = "Big Widget", columns = 2, rows = 1)),
        )
        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `skipping a pending widget marks it done without touching AppWidgetRepository`() = runTest {
        givenOnePendingWidget()

        viewModel.onSkipWidget(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.pendingWidgets[0].done)
        verify(appWidgetRepository, never()).allocateAppWidgetId()
    }

    @Test
    fun `re-adding a widget that binds synchronously with no configure activity places it at the backup's own recorded position`() = runTest {
        givenOnePendingWidget()
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(5)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(5, provider, Process.myUserHandle())).thenReturn(true)
        val info = AppWidgetProviderInfo()
        info.provider = provider
        info.configure = null
        `when`(appWidgetRepository.getAppWidgetInfo(5)).thenReturn(info)
        `when`(appWidgetRepository.createConfigureIntentSender(5, info)).thenReturn(null)
        `when`(appWidgetRepository.profileForWidget(5)).thenReturn(AppProfile.PERSONAL)
        `when`(appWidgetRepository.userIdForWidget(5)).thenReturn(0)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        `when`(placeWidget.invoke(emptyList(), 2, 1, 1, 2)).thenReturn(PlaceWidgetResult.Placed(row = 1, col = 2))

        viewModel.onReimportWidget(0)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(widgetPlacementRepository).upsert(
            WidgetPlacementEntity(appWidgetId = 5, providerPackageName = "com.example.widgets", providerClassName = ".Provider", row = 1, col = 2, colSpan = 2, rowSpan = 1, profile = AppProfile.PERSONAL, userId = 0),
        )
        assertEquals(true, viewModel.uiState.value.pendingWidgets[0].done)
    }

    @Test
    fun `re-adding a widget needing the system bind dialog emits LaunchBindPermission and does not place it yet`() = runTest {
        givenOnePendingWidget()
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(5)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(5, provider, Process.myUserHandle())).thenReturn(false)
        val bindIntent = Intent("bind")
        `when`(appWidgetRepository.createBindIntent(5, provider, Process.myUserHandle())).thenReturn(bindIntent)
        val events = mutableListOf<BackupRestoreEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onReimportWidget(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(BackupRestoreEvent.LaunchBindPermission(bindIntent)), events)
        org.mockito.Mockito.verifyNoInteractions(widgetPlacementRepository)
        job.cancel()
    }

    @Test
    fun `re-adding a widget whose provider is not installed is a no-op`() = runTest {
        val uri = mock(Uri::class.java)
        `when`(importBackup(uri)).thenReturn(ImportBackupResult.Success(1, 0, 0, listOf(placement)))
        `when`(appWidgetRepository.getWidgetProviderOptions()).thenReturn(emptyList())
        viewModel.onImportFileChosen(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReimportWidget(0)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(appWidgetRepository, never()).allocateAppWidgetId()
    }

    @Test
    fun `re-adding a widget whose backed-up profile no longer has a live matching handle is marked done without a bind attempt`() = runTest {
        // Given a backup's own recorded profile that no longer resolves to any live handle on this
        // device (e.g. the Work Profile it came from isn't this device's) — the sanctioned
        // single-representative lookup (AppRepository.resolveUserHandle) returns null
        `when`(appRepository.resolveUserHandle(AppProfile.PERSONAL)).thenReturn(null)
        givenOnePendingWidget()

        viewModel.onReimportWidget(0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then no bind is even attempted — the widget is simply marked done (skipped)
        verify(appWidgetRepository, never()).allocateAppWidgetId()
        assertEquals(true, viewModel.uiState.value.pendingWidgets[0].done)
    }
}
