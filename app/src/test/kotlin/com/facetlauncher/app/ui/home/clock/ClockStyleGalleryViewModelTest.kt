package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.LauncherSettings
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ClockStyleGalleryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        facetId: Long? = null,
    ): ClockStyleGalleryViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(emptyList()))
        val savedStateHandle = if (facetId != null) SavedStateHandle(mapOf("facetId" to facetId)) else SavedStateHandle()
        return ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, facetRepository)
    }

    @Test
    fun `selecting a template calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
    }

    @Test
    fun `changing the clock font calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockFontOption(ClockFontOption.POPPINS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockFontOption(ClockFontOption.POPPINS)
    }

    @Test
    fun `changing the clock color calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockColorOption(ClockColorOption.THEME_INVERTED)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockColorOption(ClockColorOption.THEME_INVERTED)
    }

    @Test
    fun `changing the clock accent color calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockAccentColorOption(ClockColorOption.ACCENT_SECONDARY)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockAccentColorOption(ClockColorOption.ACCENT_SECONDARY)
    }

    @Test
    fun `changing the date style calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockDateStyle(ClockDateStyle.CONDENSED)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockDateStyle(ClockDateStyle.CONDENSED)
    }

    @Test
    fun `toggling 24 hour time calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setUse24HourTime(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setUse24HourTime(true)
    }

    @Test
    fun `toggling show meridiem calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockShowMeridiem(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockShowMeridiem(true)
    }

    @Test
    fun `changing clock alignment calls the global repository setter when not facet-scoped`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockAlignment(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockAlignment(ClockAlignment.CENTER)
    }

    @Test
    fun `changing clock alignment writes the facet's own override when facet-scoped`() = runTest {
        // Now part of the same Clock+Calendar design bundle as font/color/template — facet-
        // overridable like those, unlike its earlier global-only design (see chat history).
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideClock = true)
        val facetRepository = mock(FacetRepository::class.java)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        `when`(facetRepository.getById(5L)).thenReturn(facet)
        val viewModel = createViewModel(facetRepository = facetRepository, facetId = 5L)

        viewModel.setClockAlignment(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setClockAlignment(facet, ClockAlignment.CENTER)
    }

    @Test
    fun `resetClockPosition resets the global height and alignment when not facet-scoped`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.resetClockPosition()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).resetClockZoneHeight()
        verify(settingsRepository).setClockAlignment(ClockAlignment.LEFT)
    }

    @Test
    fun `resetClockPosition resets the facet's own height and both alignments when facet-scoped`() = runTest {
        val facet = FacetEntity(id = 5L, name = "Work", position = 0, overrideClock = true, clockZoneHeightDp = 200f)
        val facetRepository = mock(FacetRepository::class.java)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        `when`(facetRepository.getById(5L)).thenReturn(facet)
        val viewModel = createViewModel(facetRepository = facetRepository, facetId = 5L)

        viewModel.resetClockPosition()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).resetClockPosition(facet)
    }

    @Test
    fun `uiState resolves clock alignment from the facet when scoped`() = runTest {
        val facet = FacetEntity(
            id = 5L,
            name = "Work",
            position = 0,
            overrideClock = true,
            clockAlignment = ClockAlignment.CENTER,
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val facetRepository = mock(FacetRepository::class.java)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        val savedStateHandle = SavedStateHandle(mapOf("facetId" to 5L))
        val viewModel = ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockAlignment.CENTER, viewModel.uiState.value.clockAlignment)
    }

    @Test
    fun `uiState resolves accent color and date style from the facet when scoped`() = runTest {
        val facet = FacetEntity(
            id = 5L,
            name = "Work",
            position = 0,
            overrideClock = true,
            clockAccentColorOption = ClockColorOption.ACCENT_SECONDARY,
            clockDateStyle = ClockDateStyle.CONDENSED,
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val facetRepository = mock(FacetRepository::class.java)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(facet)))
        val savedStateHandle = SavedStateHandle(mapOf("facetId" to 5L))
        val viewModel = ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockColorOption.ACCENT_SECONDARY, viewModel.uiState.value.accentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, viewModel.uiState.value.dateStyle)
    }

}
