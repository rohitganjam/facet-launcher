package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockColorOption
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
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        repository.addProfile()
        val second = repository.addProfile()
        assertEquals("Profile 2", second.name)
        assertEquals(1, second.position)
        assertEquals(2, repository.observeProfiles().first().size)
    }

    @Test
    fun `renameProfile updates only the name`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.renameProfile(profile, "Work")
        val stored = repository.observeProfiles().first().single()
        assertEquals("Work", stored.name)
        assertEquals(profile.position, stored.position)
    }

    @Test
    fun `reorderProfiles re-assigns position by list order`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val first = repository.addProfile()
        val second = repository.addProfile()
        repository.reorderProfiles(listOf(second, first))
        val stored = repository.observeProfiles().first().associateBy { it.id }
        assertEquals(0, stored[second.id]?.position)
        assertEquals(1, stored[first.id]?.position)
    }

    @Test
    fun `deleteProfile removes it`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.deleteProfile(profile)
        assertTrue(repository.observeProfiles().first().isEmpty())
    }

    @Test
    fun `setUse24HourTime round-trips true and false`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.setUse24HourTime(profile, true)
        assertEquals(true, repository.observeProfiles().first().single().use24HourTime)
        repository.setUse24HourTime(profile, false)
        assertEquals(false, repository.observeProfiles().first().single().use24HourTime)
    }

    @Test
    fun `updateOverridingClock sets all fields and flag together`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.updateOverridingClock(
            profile = profile,
            overriding = true,
            templateId = ClockTemplateId.VERTICAL_STACK_BOLD_HOUR,
            fontOption = ClockFontOption.MANROPE,
            colorOption = ClockColorOption.INK,
            use24HourTime = true,
            showMeridiem = true
        )
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideClock)
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, stored.clockTemplateId)
        assertEquals(ClockFontOption.MANROPE, stored.clockFontOption)
        assertEquals(ClockColorOption.INK, stored.clockColorOption)
        assertEquals(true, stored.use24HourTime)
        assertEquals(true, stored.clockShowMeridiem)
    }

    @Test
    fun `updateOverridingApps sets all fields and flag together`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.updateOverridingApps(
            profile = profile,
            overriding = true,
            mode = ListContentMode.MOST_USED,
            count = 7,
            overridingFavorites = true
        )
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideApps)
        assertEquals(ListContentMode.MOST_USED, stored.listContentMode)
        assertEquals(7, stored.appsToShowCount)
        assertEquals(true, stored.overridingFavorites)
    }

    @Test
    fun `setAppsToShowCount coerces into the 4 to 8 range`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.setAppsToShowCount(profile, 6)
        assertEquals(6, repository.observeProfiles().first().single().appsToShowCount)
        repository.setAppsToShowCount(profile, 1)
        assertEquals(4, repository.observeProfiles().first().single().appsToShowCount)
        repository.setAppsToShowCount(profile, 99)
        assertEquals(8, repository.observeProfiles().first().single().appsToShowCount)
    }

    @Test
    fun `updateOverridingCalendar sets fields and flag together`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.updateOverridingCalendar(profile, overriding = true, showAllDayEvents = false)
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideCalendar)
        assertEquals(false, stored.showAllDayEvents)
    }
}
