package com.lumenlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

        viewModel.setClockColorOption(ClockColorOption.WHITE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setClockColorOption(ClockColorOption.WHITE)
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
}
