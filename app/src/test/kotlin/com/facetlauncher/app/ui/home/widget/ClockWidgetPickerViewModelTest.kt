package com.facetlauncher.app.ui.home.widget

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.ReplaceClockStackItemUseCase
import com.facetlauncher.app.ui.hub.picker.AddFailureReason
import com.facetlauncher.app.ui.hub.picker.HubAddWidgetEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClockWidgetPickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private val provider = ComponentName("com.example.widgets", ".Provider")
    private val option = WidgetProviderOption(provider = provider, appLabel = "Example", widgetLabel = "Widget", columns = 2, rows = 1)
    private val appWidgetRepository = mock(AppWidgetRepository::class.java)
    private val replaceClockStackItem = mock(ReplaceClockStackItemUseCase::class.java)
    private val addClockStackItem = mock(AddClockStackItemUseCase::class.java)

    private fun viewModel() = ClockWidgetPickerViewModel(appWidgetRepository, replaceClockStackItem, addClockStackItem)

    private fun infoWithoutConfigure(appWidgetId: Int): AppWidgetProviderInfo {
        val info = AppWidgetProviderInfo()
        info.provider = provider
        `when`(appWidgetRepository.getAppWidgetInfo(appWidgetId)).thenReturn(info)
        `when`(appWidgetRepository.createConfigureIntentSender(appWidgetId, info)).thenReturn(null)
        return info
    }

    private fun bindsImmediately(appWidgetId: Int) {
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(appWidgetId)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(appWidgetId, provider, option.userHandle)).thenReturn(true)
    }

    private fun kotlinx.coroutines.test.TestScope.collect(viewModel: ClockWidgetPickerViewModel): MutableList<HubAddWidgetEvent> {
        val events = mutableListOf<HubAddWidgetEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { events.add(it) } }
        testDispatcher.scheduler.advanceUntilIdle()
        return events
    }

    @Test
    fun `replace target swaps the page for the new widget and emits WidgetAdded`() = runTest {
        // Given a provider that binds at once with no configure step
        bindsImmediately(1)
        infoWithoutConfigure(1)
        `when`(replaceClockStackItem.withWidget(5L, 1)).thenReturn(true)
        val viewModel = viewModel()
        val events = collect(viewModel)

        // When it is picked for the page 5
        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Replace(5L))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the page is replaced and nothing is released
        assertEquals(listOf(HubAddWidgetEvent.WidgetAdded), events)
        verify(replaceClockStackItem).withWidget(5L, 1)
        verify(appWidgetRepository, never()).deleteAppWidgetId(1)
    }

    @Test
    fun `add target appends the widget and emits WidgetAdded`() = runTest {
        bindsImmediately(2)
        infoWithoutConfigure(2)
        `when`(addClockStackItem.addWidget(9L, 2)).thenReturn(AddClockStackItemResult.ADDED)
        val viewModel = viewModel()
        val events = collect(viewModel)

        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Add(9L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.WidgetAdded), events)
        verify(addClockStackItem).addWidget(9L, 2)
        verify(replaceClockStackItem, never()).withWidget(9L, 2)
    }

    @Test
    fun `an add that finds the stack full releases the new id and emits AddFailed`() = runTest {
        bindsImmediately(3)
        infoWithoutConfigure(3)
        `when`(addClockStackItem.addWidget(9L, 3)).thenReturn(AddClockStackItemResult.LIMIT_REACHED)
        val viewModel = viewModel()
        val events = collect(viewModel)

        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Add(9L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.AddFailed(AddFailureReason.SETUP_CANCELLED)), events)
        verify(appWidgetRepository).deleteAppWidgetId(3)
    }

    @Test
    fun `a replace of a page that is gone releases the new id and emits AddFailed`() = runTest {
        bindsImmediately(4)
        infoWithoutConfigure(4)
        `when`(replaceClockStackItem.withWidget(5L, 4)).thenReturn(false)
        val viewModel = viewModel()
        val events = collect(viewModel)

        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Replace(5L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(HubAddWidgetEvent.AddFailed(AddFailureReason.SETUP_CANCELLED)), events)
        verify(appWidgetRepository).deleteAppWidgetId(4)
    }

    @Test
    fun `bind permission needed launches the permission screen and a denied result releases the id`() = runTest {
        // Given a provider that needs the system's bind permission
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(5)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(5, provider, option.userHandle)).thenReturn(false)
        val bindIntent = Intent("bind")
        `when`(appWidgetRepository.createBindIntent(5, provider, option.userHandle)).thenReturn(bindIntent)
        val viewModel = viewModel()
        val events = collect(viewModel)

        // When it is picked, then the user denies
        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Add(9L))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf<HubAddWidgetEvent>(HubAddWidgetEvent.LaunchBindPermission(bindIntent)), events.toList())
        viewModel.onBindResult(granted = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the id is released and the add failed
        verify(appWidgetRepository).deleteAppWidgetId(5)
        assertEquals(HubAddWidgetEvent.AddFailed(AddFailureReason.SETUP_CANCELLED), events.last())
        verify(addClockStackItem, never()).addWidget(9L, 5)
    }

    @Test
    fun `a granted bind permission carries on and stores the widget`() = runTest {
        `when`(appWidgetRepository.allocateAppWidgetId()).thenReturn(6)
        `when`(appWidgetRepository.bindAppWidgetIdIfAllowed(6, provider, option.userHandle)).thenReturn(false)
        `when`(appWidgetRepository.createBindIntent(6, provider, option.userHandle)).thenReturn(Intent("bind"))
        infoWithoutConfigure(6)
        `when`(addClockStackItem.addWidget(9L, 6)).thenReturn(AddClockStackItemResult.ADDED)
        val viewModel = viewModel()
        val events = collect(viewModel)

        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Add(9L))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onBindResult(granted = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HubAddWidgetEvent.WidgetAdded, events.last())
        verify(addClockStackItem).addWidget(9L, 6)
    }

    @Test
    fun `a configure step launches the provider's screen and a cancelled result releases the id`() = runTest {
        // Given a provider with a configure activity
        bindsImmediately(7)
        val info = AppWidgetProviderInfo()
        info.provider = provider
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(info)
        val sender = mock(IntentSender::class.java)
        `when`(appWidgetRepository.createConfigureIntentSender(7, info)).thenReturn(sender)
        val viewModel = viewModel()
        val events = collect(viewModel)

        // When it is picked, then the configure screen is cancelled
        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Replace(5L))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(events.single() is HubAddWidgetEvent.LaunchConfigure)
        viewModel.onConfigureResult(resultOk = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the id is released and nothing was stored
        verify(appWidgetRepository).deleteAppWidgetId(7)
        verify(replaceClockStackItem, never()).withWidget(5L, 7)
        assertEquals(HubAddWidgetEvent.AddFailed(AddFailureReason.SETUP_CANCELLED), events.last())
    }

    @Test
    fun `a configured widget is stored for the target chosen when the flow began`() = runTest {
        bindsImmediately(8)
        val info = AppWidgetProviderInfo()
        info.provider = provider
        `when`(appWidgetRepository.getAppWidgetInfo(8)).thenReturn(info)
        `when`(appWidgetRepository.createConfigureIntentSender(8, info)).thenReturn(mock(IntentSender::class.java))
        `when`(replaceClockStackItem.withWidget(5L, 8)).thenReturn(true)
        val viewModel = viewModel()
        val events = collect(viewModel)

        viewModel.onProviderSelect(option, ClockWidgetPickerTarget.Replace(5L))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onConfigureResult(resultOk = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HubAddWidgetEvent.WidgetAdded, events.last())
        verify(replaceClockStackItem).withWidget(5L, 8)
    }
}
