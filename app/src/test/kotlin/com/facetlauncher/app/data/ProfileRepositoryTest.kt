package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.ProfileDao
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.FontWeightOption
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

    override suspend fun insert(profile: ProfileEntity): Long {
        val id = if (profile.id != 0L) profile.id else nextId++
        val stored = profile.copy(id = id)
        state.value = state.value.filterNot { it.id == id } + stored
        return id
    }

    override suspend fun update(profile: ProfileEntity) {
        state.value = state.value.map { if (it.id == profile.id) profile else it }
    }

    override suspend fun delete(profile: ProfileEntity) {
        state.value = state.value.filterNot { it.id == profile.id }
    }

    override suspend fun getById(id: Long): ProfileEntity? = state.value.find { it.id == id }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
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
            colorOption = ClockColorOption.THEME,
            use24HourTime = true,
            showMeridiem = true,
            calendarFontOption = ClockFontOption.POPPINS,
            calendarColorOption = ClockColorOption.ACCENT_SECONDARY,
            calendarFontWeight = FontWeightOption.SEMI_BOLD,
            clockAlignment = ClockAlignment.CENTER,
            calendarAlignment = ClockAlignment.RIGHT,
            clockZoneHeightDp = 180f,
            clockScale = 1.2f,
        )
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideClock)
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, stored.clockTemplateId)
        assertEquals(ClockFontOption.MANROPE, stored.clockFontOption)
        assertEquals(ClockColorOption.THEME, stored.clockColorOption)
        assertEquals(true, stored.use24HourTime)
        assertEquals(true, stored.clockShowMeridiem)
        assertEquals(ClockFontOption.POPPINS, stored.calendarFontOption)
        assertEquals(ClockColorOption.ACCENT_SECONDARY, stored.calendarColorOption)
        assertEquals(FontWeightOption.SEMI_BOLD, stored.calendarFontWeight)
        assertEquals(ClockAlignment.CENTER, stored.clockAlignment)
        assertEquals(ClockAlignment.RIGHT, stored.calendarAlignment)
        assertEquals(180f, stored.clockZoneHeightDp)
        assertEquals(1.2f, stored.clockScale, 0.001f)
    }

    @Test
    fun `setClockAccentColorOption and setClockDateStyle round-trip independently`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setClockAccentColorOption(profile, ClockColorOption.ACCENT_SECONDARY)

        val stored = repository.observeProfiles().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, stored.clockAccentColorOption)
        assertEquals(ClockDateStyle.FULL, stored.clockDateStyle)

        repository.setClockDateStyle(stored, ClockDateStyle.CONDENSED)

        val restored = repository.observeProfiles().first().single()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, restored.clockAccentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, restored.clockDateStyle)
    }

    @Test
    fun `setClockAlignment and setCalendarAlignment round-trip independently`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setClockAlignment(profile, ClockAlignment.CENTER)

        val stored = repository.observeProfiles().first().single()
        assertEquals(ClockAlignment.CENTER, stored.clockAlignment)
        assertEquals(ClockAlignment.LEFT, stored.calendarAlignment)

        repository.setCalendarAlignment(stored, ClockAlignment.RIGHT)

        val restored = repository.observeProfiles().first().single()
        assertEquals(ClockAlignment.CENTER, restored.clockAlignment)
        assertEquals(ClockAlignment.RIGHT, restored.calendarAlignment)
    }

    @Test
    fun `setClockZoneHeight and resetClockZoneHeight round-trip`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setClockZoneHeight(profile, 220f)
        assertEquals(220f, repository.observeProfiles().first().single().clockZoneHeightDp)

        repository.resetClockZoneHeight(repository.observeProfiles().first().single())
        assertNull(repository.observeProfiles().first().single().clockZoneHeightDp)
    }

    @Test
    fun `setClockScale and resetClockPosition round-trip`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setClockScale(profile, 1.5f)
        assertEquals(1.5f, repository.observeProfiles().first().single().clockScale, 0.001f)

        repository.resetClockPosition(repository.observeProfiles().first().single())
        assertEquals(0.8f, repository.observeProfiles().first().single().clockScale, 0.001f)
    }

    @Test
    fun `resetClockPosition clears the zone height, both alignments, and scale together`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.setClockZoneHeight(profile, 220f)
        repository.setClockAlignment(profile, ClockAlignment.CENTER)
        repository.setCalendarAlignment(profile, ClockAlignment.RIGHT)
        repository.setClockScale(profile, 1.5f)
        val dragged = repository.observeProfiles().first().single()

        repository.resetClockPosition(dragged)

        val stored = repository.observeProfiles().first().single()
        assertNull(stored.clockZoneHeightDp)
        assertEquals(ClockAlignment.LEFT, stored.clockAlignment)
        assertEquals(ClockAlignment.LEFT, stored.calendarAlignment)
        assertEquals(0.8f, stored.clockScale, 0.001f)
    }

    @Test
    fun `updateOverridingApps sets all fields and flag together`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.updateOverridingApps(
            profile = profile,
            overriding = true,
            position = AppRowPosition.RIGHT,
            presentation = AppRowPresentation.TEXT_ONLY,
            mode = ListContentMode.MOST_USED,
            count = 6,
            overridingFavorites = true
        )
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideApps)
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, stored.appRowPresentation)
        assertEquals(ListContentMode.MOST_USED, stored.listContentMode)
        assertEquals(6, stored.appsToShowCount)
        assertEquals(true, stored.overridingFavorites)
    }

    @Test
    fun `setAppRowPosition and setAppRowPresentation round-trip independently`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setAppRowPosition(profile, AppRowPosition.RIGHT)
        val afterPosition = repository.observeProfiles().first().single()
        assertEquals(AppRowPosition.RIGHT, afterPosition.appRowPosition)

        // Re-fetch before the second setter — each setter copies from the entity it's given and
        // fully replaces the stored row, so acting on the original (now-stale) `profile` here
        // would silently discard the position update just made above.
        repository.setAppRowPresentation(afterPosition, AppRowPresentation.ICON_ONLY)
        val stored = repository.observeProfiles().first().single()
        assertEquals(AppRowPosition.RIGHT, stored.appRowPosition)
        assertEquals(AppRowPresentation.ICON_ONLY, stored.appRowPresentation)
    }

    @Test
    fun `setAppListVerticalAlignment round-trips`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setAppListVerticalAlignment(profile, AppListVerticalAlignment.TOP)

        assertEquals(AppListVerticalAlignment.TOP, repository.observeProfiles().first().single().appListVerticalAlignment)
    }

    @Test
    fun `setAppsToShowCount coerces into the 3 to 6 range`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.setAppsToShowCount(profile, 5)
        assertEquals(5, repository.observeProfiles().first().single().appsToShowCount)
        repository.setAppsToShowCount(profile, 1)
        assertEquals(3, repository.observeProfiles().first().single().appsToShowCount)
        repository.setAppsToShowCount(profile, 99)
        assertEquals(6, repository.observeProfiles().first().single().appsToShowCount)
    }

    @Test
    fun `updateOverridingCalendar sets selection fields and flag together, design fields untouched`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()
        repository.updateOverridingCalendar(
            profile,
            overriding = true,
            showAllDayEvents = false,
            selectedCalendarIds = setOf("1", "2"),
        )
        val stored = repository.observeProfiles().first().single()
        assertEquals(true, stored.overrideCalendar)
        assertEquals(false, stored.showAllDayEvents)
        assertEquals(setOf("1", "2"), stored.selectedCalendarIds)
        // Design fields (font/color) belong to overrideClock's group now, not overrideCalendar's — untouched here.
        assertEquals(ClockFontOption.LAUNCHER_DEFAULT, stored.calendarFontOption)
        assertEquals(ClockColorOption.THEME, stored.calendarColorOption)
    }

    @Test
    fun `setSelectedCalendarIds round-trips, null means every calendar selected`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setSelectedCalendarIds(profile, setOf("10", "20", "30"))
        assertEquals(setOf("10", "20", "30"), repository.observeProfiles().first().single().selectedCalendarIds)

        repository.setSelectedCalendarIds(repository.observeProfiles().first().single(), null)
        assertEquals(null, repository.observeProfiles().first().single().selectedCalendarIds)
    }

    @Test
    fun `setCalendarFontOption and setCalendarColorOption round-trip independently`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setCalendarFontOption(profile, ClockFontOption.MANROPE)
        val afterFont = repository.observeProfiles().first().single()
        assertEquals(ClockFontOption.MANROPE, afterFont.calendarFontOption)

        repository.setCalendarColorOption(afterFont, ClockColorOption.THEME_INVERTED)
        val stored = repository.observeProfiles().first().single()
        assertEquals(ClockFontOption.MANROPE, stored.calendarFontOption)
        assertEquals(ClockColorOption.THEME_INVERTED, stored.calendarColorOption)
    }

    @Test
    fun `setCalendarFontWeight round-trips`() = runTest {
        val dao = FakeProfileDao()
        val repository = ProfileRepository(dao)
        val profile = repository.addProfile()

        repository.setCalendarFontWeight(profile, FontWeightOption.LIGHT)
        assertEquals(FontWeightOption.LIGHT, repository.observeProfiles().first().single().calendarFontWeight)
    }
}
