package com.lumenlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return SettingsRepository(dataStore)
    }

    @Test
    fun `defaults match the design spec before anything is written`() = runTest {
        // Given a fresh repository with nothing written yet
        val repository = createRepository()

        // Then the emitted settings match the documented defaults
        val settings = repository.settings.first()
        assertFalse(settings.use24HourTime)
        assertEquals(DockDisplayMode.ICONS, settings.dockDisplayMode)
        assertEquals(DrawerPresentation.LIST, settings.drawerPresentation)
        assertEquals(DrawerGridSize.FIVE_BY_SIX, settings.drawerGridSize)
        assertEquals(DrawerListItemSize.COMPACT, settings.drawerListItemSize)
        assertEquals(IconRenderMode.SYSTEM_DEFAULT, settings.iconRenderMode)
        assertEquals(0.88f, settings.drawerOpacity, 0.0001f)
        assertEquals(true, settings.notificationDotsEnabled)
        assertEquals(NotificationBadgeStyle.DOT, settings.notificationBadgeStyle)
        assertEquals(true, settings.showDrawerIcons)
        assertEquals(true, settings.showDrawerLabels)
        assertEquals(SearchBarPosition.TOP, settings.searchBarPosition)
        assertEquals(NO_ACTIVE_PROFILE_ID, settings.activeProfileId)
        assertEquals(true, settings.showAllDayEvents)
        assertEquals(null, settings.selectedCalendarIds)
        assertEquals(emptyMap<String, String>(), settings.calendarColors)
        assertEquals(false, settings.searchContactsEnabled)
        assertEquals(ListContentMode.FAVORITES, settings.listContentMode)
        assertEquals(5, settings.appsToShowCount)
        assertEquals(ClockTemplateId.LIGHT_STACK, settings.clockTemplateId)
        assertEquals(ClockFontOption.SYSTEM, settings.clockFontOption)
        assertEquals(ClockColorOption.INK, settings.clockColorOption)
        assertFalse(settings.clockShowMeridiem)
        assertEquals(ClockFontOption.SYSTEM, settings.calendarFontOption)
        assertEquals(ClockColorOption.INK, settings.calendarColorOption)
    }

    @Test
    fun `clock and calendar style setters round-trip through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each clock/calendar style setting is changed
        repository.setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
        repository.setClockFontOption(ClockFontOption.POPPINS)
        repository.setClockColorOption(ClockColorOption.WHITE)
        repository.setClockShowMeridiem(true)
        repository.setCalendarFontOption(ClockFontOption.MANROPE)
        repository.setCalendarColorOption(ClockColorOption.WALLPAPER_PRIMARY)

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, settings.clockTemplateId)
        assertEquals(ClockFontOption.POPPINS, settings.clockFontOption)
        assertEquals(ClockColorOption.WHITE, settings.clockColorOption)
        assertEquals(true, settings.clockShowMeridiem)
        assertEquals(ClockFontOption.MANROPE, settings.calendarFontOption)
        assertEquals(ClockColorOption.WALLPAPER_PRIMARY, settings.calendarColorOption)
    }

    @Test
    fun `each setter round-trips through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each setting is changed
        repository.setUse24HourTime(true)
        repository.setDockDisplayMode(DockDisplayMode.TEXT)
        repository.setDrawerPresentation(DrawerPresentation.GRID)
        repository.setDrawerGridSize(DrawerGridSize.FOUR_BY_FOUR)
        repository.setDrawerListItemSize(DrawerListItemSize.SPACIOUS)
        repository.setIconRenderMode(IconRenderMode.MONOCHROME_ACCENT)
        repository.setDrawerOpacity(0.5f)
        repository.setNotificationDotsEnabled(false)
        repository.setNotificationBadgeStyle(NotificationBadgeStyle.COUNT)
        repository.setShowDrawerIcons(false)
        repository.setShowDrawerLabels(false)
        repository.setSearchBarPosition(SearchBarPosition.BOTTOM)
        repository.setActiveProfileId(7L)
        repository.setShowAllDayEvents(false)
        repository.setSelectedCalendarIds(setOf("cal-1", "cal-2"))
        repository.setCalendarColors(mapOf("cal-1" to "BLUE", "cal-2" to "TEAL"))
        repository.setSearchContactsEnabled(true)
        repository.setListContentMode(ListContentMode.MOST_USED)
        repository.setAppsToShowCount(7)

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(true, settings.use24HourTime)
        assertEquals(DockDisplayMode.TEXT, settings.dockDisplayMode)
        assertEquals(DrawerPresentation.GRID, settings.drawerPresentation)
        assertEquals(DrawerGridSize.FOUR_BY_FOUR, settings.drawerGridSize)
        assertEquals(DrawerListItemSize.SPACIOUS, settings.drawerListItemSize)
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, settings.iconRenderMode)
        assertEquals(0.5f, settings.drawerOpacity, 0.0001f)
        assertEquals(false, settings.notificationDotsEnabled)
        assertEquals(NotificationBadgeStyle.COUNT, settings.notificationBadgeStyle)
        assertEquals(false, settings.showDrawerIcons)
        assertEquals(false, settings.showDrawerLabels)
        assertEquals(SearchBarPosition.BOTTOM, settings.searchBarPosition)
        assertEquals(7L, settings.activeProfileId)
        assertEquals(false, settings.showAllDayEvents)
        assertEquals(setOf("cal-1", "cal-2"), settings.selectedCalendarIds)
        assertEquals(mapOf("cal-1" to "BLUE", "cal-2" to "TEAL"), settings.calendarColors)
        assertEquals(true, settings.searchContactsEnabled)
        assertEquals(ListContentMode.MOST_USED, settings.listContentMode)
        assertEquals(7, settings.appsToShowCount)
    }

    @Test
    fun `default apps-to-show count is coerced into the 4 to 8 range`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When set below the floor, it's coerced up to 4
        repository.setAppsToShowCount(1)
        assertEquals(4, repository.settings.first().appsToShowCount)

        // When set above the ceiling, it's coerced down to 8
        repository.setAppsToShowCount(99)
        assertEquals(8, repository.settings.first().appsToShowCount)
    }

    @Test
    fun `drawer opacity is coerced into the 0 to 1 range`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When an out-of-range opacity is set
        repository.setDrawerOpacity(1.5f)

        // Then it is clamped to 1.0
        assertEquals(1f, repository.settings.first().drawerOpacity, 0.0001f)
    }

    @Test
    fun `theme and accent settings default to system theme and Material You`() = runTest {
        // Given a fresh repository with nothing written yet
        val repository = createRepository()

        // Then it defaults to following the system theme with a wallpaper-derived accent
        val settings = repository.settings.first()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(true, settings.accentFromSystem)
        assertEquals(null, settings.customAccentSwatch)
    }

    @Test
    fun `theme and accent setters round-trip through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When the theme is forced to dark and a fixed accent swatch is picked
        repository.setThemeMode(ThemeMode.DARK)
        repository.setAccentFromSystem(false)
        repository.setCustomAccentSwatch("TEAL")

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(false, settings.accentFromSystem)
        assertEquals("TEAL", settings.customAccentSwatch)
    }
}
