package com.lumenlauncher.app.ui.hub.picker

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.model.WidgetProviderOption
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.domain.HUB_MAX_WIDGETS
import com.lumenlauncher.app.domain.PlaceWidgetResult
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HubWidgetPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private val provider = ComponentName("com.example.widgets", ".Provider")
    private val option = WidgetProviderOption(provider = provider, appLabel = "Example", widgetLabel = "Widget", columns = 2, rows = 1)

    @Test
    fun `selecting a provider that binds synchronously with no configure activity places it and emits WidgetAdded`() = runTest {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val placeWidget = mock(PlaceWidgetUseCase::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(1)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(1, provider)).thenReturn(true)
        // Not `.apply { this.provider = provider }` — inside that lambda, the bare `provider` on
        // the right-hand side resolves to AppWidgetProviderInfo's own (still-null) field via
        // implicit-receiver scoping, not the outer val, so it silently self-assigns null.
        val info = AppWidgetProviderInfo()
        info.provider = provider
        info.configure = null
        info.minWidth = 110
        info.minHeight = 40
        `when`(appWidgetRepository.getAppWidgetInfo(1)).thenReturn(info)
        `when`(appWidgetRepository.createConfigureIntent(1, info)).thenReturn(null)
        `when`(placeWidget.invoke(emptyList(), 2, 1)).thenReturn(PlaceWidgetResult.Placed(row = 0, col = 0))

        val viewModel = HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, placeWidget)
        val events = mutableListOf<HubAddWidgetEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        // uiState is WhileSubscribed(5_000) — onProviderSelected reads its .value, so it must
        // actually be subscribed here or that value never advances past the empty default.
        val uiStateJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onProviderSelected(option)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.WidgetAdded), events)
        verify(widgetPlacementRepository).upsert(
            WidgetPlacementEntity(appWidgetId = 1, providerPackageName = provider.packageName, providerClassName = provider.className, row = 0, col = 0, colSpan = 2, rowSpan = 1),
        )
        job.cancel()
        uiStateJob.cancel()
    }

    @Test
    fun `a bind that needs the system dialog emits LaunchBindPermission and does not proceed until granted`() = runTest {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val placeWidget = mock(PlaceWidgetUseCase::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(1)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(1, provider)).thenReturn(false)
        val bindIntent = Intent("bind")
        `when`(appWidgetRepository.createBindIntent(1, provider)).thenReturn(bindIntent)

        val viewModel = HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, placeWidget)
        val events = mutableListOf<HubAddWidgetEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        // uiState is WhileSubscribed(5_000) — onProviderSelected reads its .value, so it must
        // actually be subscribed here or that value never advances past the empty default.
        val uiStateJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onProviderSelected(option)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.LaunchBindPermission(bindIntent)), events)
        verify(appWidgetRepository, never()).getAppWidgetInfo(anyInt())
        job.cancel()
        uiStateJob.cancel()
    }

    @Test
    fun `a denied bind releases the allocated id and emits AddFailed`() = runTest {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val placeWidget = mock(PlaceWidgetUseCase::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(1)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(1, provider)).thenReturn(false)
        `when`(appWidgetRepository.createBindIntent(1, provider)).thenReturn(Intent("bind"))

        val viewModel = HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, placeWidget)
        val events = mutableListOf<HubAddWidgetEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        // uiState is WhileSubscribed(5_000) — onProviderSelected reads its .value, so it must
        // actually be subscribed here or that value never advances past the empty default.
        val uiStateJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onProviderSelected(option)
        testDispatcher.scheduler.advanceUntilIdle()

        // When the system bind-permission dialog comes back denied
        viewModel.onBindResult(granted = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HubAddWidgetEvent.AddFailed, events.last())
        verify(appWidgetRepository).deleteAppWidgetId(1)
        job.cancel()
        uiStateJob.cancel()
    }

    @Test
    fun `a provider with a declared configure activity emits LaunchConfigure after a successful bind`() = runTest {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val placeWidget = mock(PlaceWidgetUseCase::class.java)
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(emptyList()))
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(1)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(1, provider)).thenReturn(true)
        val configureComponent = ComponentName("com.example.widgets", ".Configure")
        val info = AppWidgetProviderInfo()
        info.provider = provider
        info.configure = configureComponent
        info.minWidth = 110
        info.minHeight = 40
        `when`(appWidgetRepository.getAppWidgetInfo(1)).thenReturn(info)
        val configureIntent = Intent("configure")
        `when`(appWidgetRepository.createConfigureIntent(1, info)).thenReturn(configureIntent)

        val viewModel = HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, placeWidget)
        val events = mutableListOf<HubAddWidgetEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        // uiState is WhileSubscribed(5_000) — onProviderSelected reads its .value, so it must
        // actually be subscribed here or that value never advances past the empty default.
        val uiStateJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onProviderSelected(option)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.LaunchConfigure(configureIntent)), events)
        job.cancel()
        uiStateJob.cancel()
    }

    @Test
    fun `the hub already at capacity fails fast before allocating a host id`() = runTest {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val widgetPlacementRepository = mock(WidgetPlacementRepository::class.java)
        val placeWidget = mock(PlaceWidgetUseCase::class.java)
        val fullPlacements = (0 until HUB_MAX_WIDGETS).map {
            WidgetPlacementEntity(appWidgetId = it, providerPackageName = "com.example", providerClassName = ".P", row = it, col = 0, colSpan = 1, rowSpan = 1)
        }
        `when`(widgetPlacementRepository.observeAll()).thenReturn(flowOf(fullPlacements))

        val viewModel = HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, placeWidget)
        val events = mutableListOf<HubAddWidgetEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        // uiState is WhileSubscribed(5_000) — onProviderSelected reads its .value, so it must
        // actually be subscribed here or that value never advances past the empty default.
        val uiStateJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onProviderSelected(option)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.AddFailed), events)
        verify(appWidgetRepository, never()).allocateAppWidgetId()
        job.cancel()
        uiStateJob.cancel()
    }
}
