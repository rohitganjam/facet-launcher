package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** In-memory fake — simpler than mocking every [ProfileDao] method for this repository's needs. */
private class FakeProfileDao : ProfileDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<ProfileEntity>>(emptyList())

    override fun observeAll(): Flow<List<ProfileEntity>> = state

    override suspend fun upsert(profile: ProfileEntity): Long {
        val id = if (profile.id != 0L) profile.id else nextId++
        val stored = profile.copy(id = id)
        state.value = state.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun delete(profile: ProfileEntity) {
        state.value = state.value.filterNot { it.id == profile.id }
    }

    override suspend fun getById(id: Long): ProfileEntity? = state.value.find { it.id == id }
}

class ProfileRepositoryTest {

    @Test
    fun `addProfile names it Profile N and appends after the last position`() = runTest {
        // Given one existing profile
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        repository.addProfile()

        // When a second profile is added
        val second = repository.addProfile()

        // Then it's named/positioned after the first
        assertEquals("Profile 2", second.name)
        assertEquals(1, second.position)
        assertEquals(2, repository.observeProfiles().first().size)
    }

    @Test
    fun `renameProfile updates only the name`() = runTest {
        // Given a stored profile
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        // When it's renamed
        repository.renameProfile(profile, "Work")

        // Then the stored profile reflects the new name at the same position
        val stored = repository.observeProfiles().first().single()
        assertEquals("Work", stored.name)
        assertEquals(profile.position, stored.position)
    }

    @Test
    fun `reorderProfiles re-assigns position by list order`() = runTest {
        // Given two profiles
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val first = repository.addProfile()
        val second = repository.addProfile()

        // When reordered with the second profile first
        repository.reorderProfiles(listOf(second, first))

        // Then their stored positions reflect the new order
        val stored = repository.observeProfiles().first().associateBy { it.id }
        assertEquals(0, stored[second.id]?.position)
        assertEquals(1, stored[first.id]?.position)
    }

    @Test
    fun `deleteProfile removes it`() = runTest {
        // Given a stored profile
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        // When it is deleted
        repository.deleteProfile(profile)

        // Then it's gone
        assertTrue(repository.observeProfiles().first().isEmpty())
    }

    @Test
    fun `getById returns null for an id that doesn't exist`() = runTest {
        // Given an empty repository
        val repository = ProfileRepository(FakeProfileDao())

        // Then a lookup for a nonexistent id returns null
        assertNull(repository.getById(999))
    }

    @Test
    fun `setUse24HourTimeOverride round-trips null and a real value`() = runTest {
        // Given a stored profile with no override (inherits the global default)
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        assertNull(repository.observeProfiles().first().single().use24HourTimeOverride)

        // When an override is set
        repository.setUse24HourTimeOverride(profile, true)
        assertEquals(true, repository.observeProfiles().first().single().use24HourTimeOverride)

        // And then cleared back to inherit
        repository.setUse24HourTimeOverride(profile, null)
        assertNull(repository.observeProfiles().first().single().use24HourTimeOverride)
    }

    @Test
    fun `setShowAllDayEventsOverride round-trips null and a real value`() = runTest {
        // Given a stored profile with no override
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        // When an override is set then cleared
        repository.setShowAllDayEventsOverride(profile, false)
        assertEquals(false, repository.observeProfiles().first().single().showAllDayEventsOverride)
        repository.setShowAllDayEventsOverride(profile, null)
        assertNull(repository.observeProfiles().first().single().showAllDayEventsOverride)
    }

    @Test
    fun `setListContentModeOverride updates the stored override`() = runTest {
        // Given a stored profile with no override yet (inherits the global default)
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        assertEquals(null, repository.observeProfiles().first().single().listContentModeOverride)

        // When switched to Most used
        repository.setListContentModeOverride(profile, ListContentMode.MOST_USED)

        // Then the stored override reflects it
        assertEquals(ListContentMode.MOST_USED, repository.observeProfiles().first().single().listContentModeOverride)

        // When reverted to Inherit
        repository.setListContentModeOverride(profile, null)

        // Then the override clears
        assertEquals(null, repository.observeProfiles().first().single().listContentModeOverride)
    }

    @Test
    fun `setOverridingFavorites round-trips true and false`() = runTest {
        // Given a stored profile that hasn't overridden its own favorites yet
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        assertEquals(false, repository.observeProfiles().first().single().overridingFavorites)

        // When it switches to overriding
        repository.setOverridingFavorites(profile, true)
        assertEquals(true, repository.observeProfiles().first().single().overridingFavorites)

        // And back to inheriting
        repository.setOverridingFavorites(profile, false)
        assertEquals(false, repository.observeProfiles().first().single().overridingFavorites)
    }

    @Test
    fun `setOverridingApps sets mode, apps-to-show, and the favorites flag together in one write`() = runTest {
        // Given a stored profile with nothing overridden
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        // When switching to Override with real effective values — this is the exact shape of
        // call that broke when it was three separate setter calls off the same stale snapshot:
        // each one did a full-row replace, so only the *last* call's own field actually stuck.
        repository.setOverridingApps(profile, overriding = true, effectiveMode = ListContentMode.MOST_USED, effectiveAppsToShowCount = 7)

        // Then all three fields reflect the change at once — none of them clobbered by the others
        val stored = repository.observeProfiles().first().single()
        assertEquals(ListContentMode.MOST_USED, stored.listContentModeOverride)
        assertEquals(7, stored.appsToShowCountOverride)
        assertEquals(true, stored.overridingFavorites)

        // When switching back to Inherit
        repository.setOverridingApps(profile, overriding = false, effectiveMode = ListContentMode.MOST_USED, effectiveAppsToShowCount = 7)

        // Then all three revert together
        val reverted = repository.observeProfiles().first().single()
        assertEquals(null, reverted.listContentModeOverride)
        assertEquals(null, reverted.appsToShowCountOverride)
        assertEquals(false, reverted.overridingFavorites)
    }

    @Test
    fun `setAppsToShowCountOverride coerces into the 4 to 8 range`() = runTest {
        // Given a stored profile
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        // When set within range, it's stored as-is
        repository.setAppsToShowCountOverride(profile, 6)
        assertEquals(6, repository.observeProfiles().first().single().appsToShowCountOverride)

        // When set below the floor, it's coerced up to 4
        repository.setAppsToShowCountOverride(profile, 1)
        assertEquals(4, repository.observeProfiles().first().single().appsToShowCountOverride)

        // When set above the ceiling, it's coerced down to 8
        repository.setAppsToShowCountOverride(profile, 99)
        assertEquals(8, repository.observeProfiles().first().single().appsToShowCountOverride)

        // When reverted to Inherit
        repository.setAppsToShowCountOverride(profile, null)
        assertEquals(null, repository.observeProfiles().first().single().appsToShowCountOverride)
    }
}
