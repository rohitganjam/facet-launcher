package com.lumenlauncher.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** In-memory fake — mirrors [com.lumenlauncher.app.data.ProfileRepositoryTest]'s. */
private class FakeProfileDao : ProfileDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<ProfileEntity>>(emptyList())

    override fun observeAll(): Flow<List<ProfileEntity>> = state

    override suspend fun upsert(profile: ProfileEntity): Long {
        val id = if (profile.id != 0L) profile.id else nextId++
        state.value = state.value.filterNot { it.id == id } + profile.copy(id = id)
        return id
    }

    override suspend fun delete(profile: ProfileEntity) {
        state.value = state.value.filterNot { it.id == profile.id }
    }

    override suspend fun getById(id: Long): ProfileEntity? = state.value.find { it.id == id }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class EnsureActiveProfileUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createSettingsRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return SettingsRepository(dataStore)
    }

    @Test
    fun `seeds a default profile and sets it active when the database is empty`() = runTest {
        // Given no profiles and no active profile id
        val profileRepository = ProfileRepository(FakeProfileDao())
        val settingsRepository = createSettingsRepository()

        // When ensuring an active profile
        EnsureActiveProfileUseCase(profileRepository, settingsRepository)()

        // Then exactly one profile now exists and it's the active one
        val profiles = profileRepository.observeProfiles().first()
        assertEquals(1, profiles.size)
        assertEquals(profiles.single().id, settingsRepository.settings.first().activeProfileId)
    }

    @Test
    fun `leaves an already-valid active profile id untouched`() = runTest {
        // Given a profile that's already set active
        val profileRepository = ProfileRepository(FakeProfileDao())
        val settingsRepository = createSettingsRepository()
        val profile = profileRepository.addProfile()
        settingsRepository.setActiveProfileId(profile.id)

        // When ensuring an active profile again
        EnsureActiveProfileUseCase(profileRepository, settingsRepository)()

        // Then it's unchanged, and no second profile was created
        assertEquals(profile.id, settingsRepository.settings.first().activeProfileId)
        assertEquals(1, profileRepository.observeProfiles().first().size)
    }

    @Test
    fun `falls back to an existing profile when the active id no longer resolves`() = runTest {
        // Given a stored profile but an active id pointing at a profile that no longer exists
        val profileRepository = ProfileRepository(FakeProfileDao())
        val settingsRepository = createSettingsRepository()
        val profile = profileRepository.addProfile()
        settingsRepository.setActiveProfileId(profile.id + 999)

        // When ensuring an active profile
        EnsureActiveProfileUseCase(profileRepository, settingsRepository)()

        // Then it falls back to the real profile rather than creating a redundant one
        assertEquals(profile.id, settingsRepository.settings.first().activeProfileId)
        assertEquals(1, profileRepository.observeProfiles().first().size)
    }
}
