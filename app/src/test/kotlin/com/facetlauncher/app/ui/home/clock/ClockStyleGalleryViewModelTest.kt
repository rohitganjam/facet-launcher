package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ClockStyleGalleryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val clockStackRepository = mock(ClockStackRepository::class.java)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** A facet with a clock page (id 9) holding [config]. */
    private fun facetWith(config: ClockWidgetConfig) = Facet(
        id = 5L, name = "Work", position = 0,
        clockStack = listOf(ClockStackItem(id = 9, facetId = 5, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    private fun createViewModel(facet: Facet?): ClockStyleGalleryViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOfNotNull(facet)))
        facet?.let { runBlocking { `when`(facetRepository.getById(it.id)).thenReturn(it) } }
        val handle = if (facet != null) SavedStateHandle(mapOf("facetId" to facet.id)) else SavedStateHandle()
        return ClockStyleGalleryViewModel(handle, settingsRepository, facetRepository, UpdateClockConfigUseCase(settingsRepository, facetRepository, clockStackRepository))
    }

    private suspend fun assertStored(expected: ClockWidgetConfig, act: ClockStyleGalleryViewModel.() -> Unit) {
        val viewModel = createViewModel(facetWith(ClockWidgetConfig()))
        viewModel.act()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(clockStackRepository).setConfig(9, NativeWidgetConfig(clock = expected))
    }

    @Test
    fun `selecting a template stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)) { setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR) }
    }

    @Test
    fun `changing the clock font stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(fontOption = ClockFontOption.entries.last())) { setClockFontOption(ClockFontOption.entries.last()) }
    }

    @Test
    fun `changing the clock color stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(colorOption = ClockColorOption.ACCENT_SECONDARY)) { setClockColorOption(ClockColorOption.ACCENT_SECONDARY) }
    }

    @Test
    fun `changing the clock accent color stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(accentColorOption = ClockColorOption.ACCENT_SECONDARY)) { setClockAccentColorOption(ClockColorOption.ACCENT_SECONDARY) }
    }

    @Test
    fun `changing the date style stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(dateStyle = ClockDateStyle.CONDENSED)) { setClockDateStyle(ClockDateStyle.CONDENSED) }
    }

    @Test
    fun `toggling 24 hour time stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(use24HourTime = true)) { setUse24HourTime(true) }
    }

    @Test
    fun `toggling show meridiem stores it on the clock`() = runTest {
        assertStored(ClockWidgetConfig(showMeridiem = true)) { setClockShowMeridiem(true) }
    }

    @Test
    fun `a change keeps the clock's other settings`() = runTest {
        // Given a clock with its own template and accessories
        val own = ClockWidgetConfig(templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, showWeather = true, showAlarm = false)
        val viewModel = createViewModel(facetWith(own))

        // When only the date style changes
        viewModel.setClockDateStyle(ClockDateStyle.CONDENSED)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then everything else is stored as it was
        verify(clockStackRepository).setConfig(9, NativeWidgetConfig(clock = own.copy(dateStyle = ClockDateStyle.CONDENSED)))
    }

    @Test
    fun `with no facet nothing is written`() = runTest {
        val viewModel = createViewModel(null)

        viewModel.setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(clockStackRepository, never()).setConfig(9, NativeWidgetConfig())
    }

    @Test
    fun `resetClockPosition gives a facet's placement back to the launcher fallback`() = runTest {
        val facet = facetWith(ClockWidgetConfig()).copy(clockZoneHeightDp = 200f)
        val viewModel = createViewModel(facet)

        viewModel.resetClockPosition()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).resetClockPosition(facet.id)
    }

    @Test
    fun `uiState reads the look from the clock's own settings`() = runTest {
        val facet = facetWith(ClockWidgetConfig(accentColorOption = ClockColorOption.ACCENT_SECONDARY, dateStyle = ClockDateStyle.CONDENSED))
        val viewModel = createViewModel(facet)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockColorOption.ACCENT_SECONDARY, viewModel.uiState.value.accentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, viewModel.uiState.value.dateStyle)
    }
}
