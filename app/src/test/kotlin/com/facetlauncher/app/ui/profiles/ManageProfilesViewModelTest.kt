package com.facetlauncher.app.ui.profiles

import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileDao
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/** In-memory fake — enough of [ProfileDao] for [ProfileRepository]'s needs. */
private class FakeProfileDao : ProfileDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<ProfileEntity>>(emptyList())

    override fun observeAll(): Flow<List<ProfileEntity>> = state
    override suspend fun upsert(profile: ProfileEntity): Long {
        val id = if (profile.id != 0L) profile.id else nextId++
        state.value = (state.value.filterNot { it.id == id } + profile.copy(id = id)).sortedBy { it.position }
        return id
    }
    override suspend fun delete(profile: ProfileEntity) {
        state.value = state.value.filterNot { it.id == profile.id }
    }
    override suspend fun getById(id: Long): ProfileEntity? = state.value.find { it.id == id }
    override suspend fun deleteAll() { state.value = emptyList() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ManageProfilesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun fakeSettings(activeProfileId: Long = 0L): SettingsRepository = mock(SettingsRepository::class.java).also {
        `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings(activeProfileId = activeProfileId)))
    }

    @Test
    fun `uiState reflects the observed profiles and the add-max rule`() = runTest(dispatcher) {
        val profileRepository = ProfileRepository(FakeProfileDao())
        profileRepository.addProfile()
        profileRepository.addProfile()
        val viewModel = ManageProfilesViewModel(profileRepository, fakeSettings())

        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Profile 1", "Profile 2"), viewModel.uiState.value.profiles.map { it.name })
        assertTrue(viewModel.uiState.value.canAddProfile)
        assertTrue(viewModel.uiState.value.canDeleteProfile)
    }

    @Test
    fun `reorderProfiles persists the new order`() = runTest(dispatcher) {
        val profileRepository = ProfileRepository(FakeProfileDao())
        profileRepository.addProfile()
        profileRepository.addProfile()
        val viewModel = ManageProfilesViewModel(profileRepository, fakeSettings())
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        val reversed = viewModel.uiState.value.profiles.reversed()
        viewModel.reorderProfiles(reversed)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(reversed.map { it.id }, profileRepository.observeProfiles().first().map { it.id })
    }

    @Test
    fun `addProfile is a no-op once the maximum is reached`() = runTest(dispatcher) {
        val profileRepository = ProfileRepository(FakeProfileDao())
        repeat(ProfileRepository.MAX_PROFILES) { profileRepository.addProfile() }
        val viewModel = ManageProfilesViewModel(profileRepository, fakeSettings())
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canAddProfile)

        viewModel.addProfile()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ProfileRepository.MAX_PROFILES, profileRepository.observeProfiles().first().size)
    }

    @Test
    fun `deleting the active profile re-points active to a survivor`() = runTest(dispatcher) {
        val profileRepository = ProfileRepository(FakeProfileDao())
        val first = profileRepository.addProfile()
        val second = profileRepository.addProfile()
        val settings = fakeSettings(activeProfileId = first.id)
        val viewModel = ManageProfilesViewModel(profileRepository, settings)
        backgroundScope.launch { viewModel.uiState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteProfile(first)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(second.id), profileRepository.observeProfiles().first().map { it.id })
        verify(settings).setActiveProfileId(second.id)
    }
}
