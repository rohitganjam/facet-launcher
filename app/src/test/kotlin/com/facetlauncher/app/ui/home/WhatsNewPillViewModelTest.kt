package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.AppVersion
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ReleaseNotes
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class WhatsNewPillViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mock(SettingsRepository::class.java)
    private val releaseNotesRepository = mock(ReleaseNotesRepository::class.java)
    private val appVersion = object : AppVersion() {
        override val name = "9.9.9"
    }

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private suspend fun announced(settings: LauncherSettings, notesFor: List<String> = listOf("9.9.9", "9.9.8")): String? {
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        `when`(releaseNotesRepository.releases()).thenReturn(notesFor.map { ReleaseNotes(it, listOf("a"), emptyList()) })
        val viewModel = WhatsNewPillViewModel(settingsRepository, releaseNotesRepository, appVersion)
        kotlinx.coroutines.coroutineScope {
            val job = launch(testDispatcher) { viewModel.version.collect {} }
            testDispatcher.scheduler.advanceUntilIdle()
            job.cancel()
        }
        return viewModel.version.value
    }

    @Test
    fun `announces the running version after an update when it has notes and they are unseen`() = runTest {
        assertEquals("9.9.9", announced(LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf("WHATS_NEW_9.9.8"))))
    }

    @Test
    fun `announces nothing once this version's notes were seen`() = runTest {
        assertNull(announced(LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf("WHATS_NEW_9.9.9"))))
    }

    @Test
    fun `announces nothing before onboarding is finished`() = runTest {
        assertNull(announced(LauncherSettings(onboardingCompleted = false)))
    }

    @Test
    fun `announces nothing when this version has no notes`() = runTest {
        assertNull(announced(LauncherSettings(onboardingCompleted = true), notesFor = listOf("9.9.8")))
    }

    @Test
    fun `showing the pill marks this version as seen`() = runTest {
        val viewModel = WhatsNewPillViewModel(settingsRepository, releaseNotesRepository, appVersion)

        viewModel.onShown()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).markCoachMarkSeen("WHATS_NEW_9.9.9")
    }
}
