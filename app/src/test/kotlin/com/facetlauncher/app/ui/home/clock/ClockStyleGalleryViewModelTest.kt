package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
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
        profileRepository: ProfileRepository = mock(ProfileRepository::class.java),
        profileId: Long? = null,
    ): ClockStyleGalleryViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(emptyList()))
        val savedStateHandle = if (profileId != null) SavedStateHandle(mapOf("profileId" to profileId)) else SavedStateHandle()
        return ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, profileRepository)
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
    fun `changing the calendar font calls the repository setter globally`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setCalendarFontOption(ClockFontOption.POPPINS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCalendarFontOption(ClockFontOption.POPPINS)
    }

    @Test
    fun `changing the calendar color calls the repository setter globally`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setCalendarColorOption(ClockColorOption.THEME_INVERTED)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCalendarColorOption(ClockColorOption.THEME_INVERTED)
    }

    @Test
    fun `changing the calendar font weight calls the repository setter globally`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setCalendarFontWeight(FontWeightOption.SEMI_BOLD)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCalendarFontWeight(FontWeightOption.SEMI_BOLD)
    }

    @Test
    fun `changing clock alignment calls the global repository setter when not profile-scoped`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setClockAlignment(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockAlignment(ClockAlignment.CENTER)
    }

    @Test
    fun `changing clock alignment writes the profile's own override when profile-scoped`() = runTest {
        // Now part of the same Clock+Calendar design bundle as font/color/template — profile-
        // overridable like those, unlike its earlier global-only design (see chat history).
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, overrideClock = true)
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(profileRepository.getById(5L)).thenReturn(profile)
        val viewModel = createViewModel(profileRepository = profileRepository, profileId = 5L)

        viewModel.setClockAlignment(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setClockAlignment(profile, ClockAlignment.CENTER)
    }

    @Test
    fun `changing calendar alignment calls the global repository setter when not profile-scoped, independent of clock alignment`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setCalendarAlignment(ClockAlignment.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCalendarAlignment(ClockAlignment.RIGHT)
    }

    @Test
    fun `changing calendar alignment writes the profile's own override when profile-scoped`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, overrideClock = true)
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(profileRepository.getById(5L)).thenReturn(profile)
        val viewModel = createViewModel(profileRepository = profileRepository, profileId = 5L)

        viewModel.setCalendarAlignment(ClockAlignment.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setCalendarAlignment(profile, ClockAlignment.RIGHT)
    }

    @Test
    fun `resetClockPosition resets the global height and both alignments when not profile-scoped`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.resetClockPosition()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).resetClockZoneHeight()
        verify(settingsRepository).setClockAlignment(ClockAlignment.LEFT)
        verify(settingsRepository).setCalendarAlignment(ClockAlignment.LEFT)
    }

    @Test
    fun `resetClockPosition resets the profile's own height and both alignments when profile-scoped`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, overrideClock = true, clockZoneHeightDp = 200f)
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(profileRepository.getById(5L)).thenReturn(profile)
        val viewModel = createViewModel(profileRepository = profileRepository, profileId = 5L)

        viewModel.resetClockPosition()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).resetClockPosition(profile)
    }

    @Test
    fun `profile-scoped calendar font, color, and weight changes write the profile's own override`() = runTest {
        val profile = ProfileEntity(id = 5L, name = "Work", position = 0, overrideClock = true)
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        `when`(profileRepository.getById(5L)).thenReturn(profile)
        val viewModel = createViewModel(profileRepository = profileRepository, profileId = 5L)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCalendarFontOption(ClockFontOption.MANROPE)
        viewModel.setCalendarColorOption(ClockColorOption.ACCENT_SECONDARY)
        viewModel.setCalendarFontWeight(FontWeightOption.LIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setCalendarFontOption(profile, ClockFontOption.MANROPE)
        verify(profileRepository).setCalendarColorOption(profile, ClockColorOption.ACCENT_SECONDARY)
        verify(profileRepository).setCalendarFontWeight(profile, FontWeightOption.LIGHT)
    }

    @Test
    fun `uiState resolves calendar font, color, and weight from the profile when scoped`() = runTest {
        val profile = ProfileEntity(
            id = 5L,
            name = "Work",
            position = 0,
            overrideClock = true,
            calendarFontOption = ClockFontOption.MANROPE,
            calendarColorOption = ClockColorOption.ACCENT_SECONDARY,
            calendarFontWeight = FontWeightOption.SEMI_BOLD,
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        val savedStateHandle = SavedStateHandle(mapOf("profileId" to 5L))
        val viewModel = ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockFontOption.MANROPE, viewModel.uiState.value.calendarFontOption)
        assertEquals(ClockColorOption.ACCENT_SECONDARY, viewModel.uiState.value.calendarColorOption)
        assertEquals(FontWeightOption.SEMI_BOLD, viewModel.uiState.value.calendarFontWeight)
    }

    @Test
    fun `uiState resolves clock and calendar alignment from the profile when scoped`() = runTest {
        val profile = ProfileEntity(
            id = 5L,
            name = "Work",
            position = 0,
            overrideClock = true,
            clockAlignment = ClockAlignment.CENTER,
            calendarAlignment = ClockAlignment.RIGHT,
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        val savedStateHandle = SavedStateHandle(mapOf("profileId" to 5L))
        val viewModel = ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockAlignment.CENTER, viewModel.uiState.value.clockAlignment)
        assertEquals(ClockAlignment.RIGHT, viewModel.uiState.value.calendarAlignment)
    }

    @Test
    fun `uiState resolves accent color and date style from the profile when scoped`() = runTest {
        val profile = ProfileEntity(
            id = 5L,
            name = "Work",
            position = 0,
            overrideClock = true,
            clockAccentColorOption = ClockColorOption.ACCENT_SECONDARY,
            clockDateStyle = ClockDateStyle.CONDENSED,
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(listOf(profile)))
        val savedStateHandle = SavedStateHandle(mapOf("profileId" to 5L))
        val viewModel = ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockColorOption.ACCENT_SECONDARY, viewModel.uiState.value.accentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, viewModel.uiState.value.dateStyle)
    }

    @Test
    fun `uiState resolves calendar font, color, and weight from global settings when not scoped`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(
            flowOf(
                LauncherSettings(
                    calendarFontOption = ClockFontOption.POPPINS,
                    calendarColorOption = ClockColorOption.THEME_INVERTED,
                    calendarFontWeight = FontWeightOption.LIGHT,
                ),
            ),
        )
        val profileRepository = mock(ProfileRepository::class.java)
        `when`(profileRepository.observeProfiles()).thenReturn(flowOf(emptyList()))
        val viewModel = ClockStyleGalleryViewModel(SavedStateHandle(), settingsRepository, profileRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ClockFontOption.POPPINS, viewModel.uiState.value.calendarFontOption)
        assertEquals(ClockColorOption.THEME_INVERTED, viewModel.uiState.value.calendarColorOption)
        assertEquals(FontWeightOption.LIGHT, viewModel.uiState.value.calendarFontWeight)
    }
}
