package com.lumenlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole
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
        assertEquals(DrawerListItemSize.REGULAR, settings.drawerListItemSize)
        assertEquals(IconRenderMode.SYSTEM_DEFAULT, settings.iconRenderMode)
        assertEquals(0.6f, settings.drawerOpacity, 0.0001f)
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
        assertEquals(AppRowPosition.LEFT, settings.appRowPosition)
        assertEquals(AppRowPresentation.ICON_AND_TEXT, settings.appRowPresentation)
        assertEquals(ListContentMode.FAVORITES, settings.listContentMode)
        assertEquals(5, settings.appsToShowCount)
        assertEquals(ClockTemplateId.LIGHT_STACK, settings.clockTemplateId)
        assertEquals(ClockFontOption.LAUNCHER_DEFAULT, settings.clockFontOption)
        assertEquals(ClockColorOption.THEME, settings.clockColorOption)
        assertFalse(settings.clockShowMeridiem)
        assertEquals(ClockFontOption.LAUNCHER_DEFAULT, settings.calendarFontOption)
        assertEquals(ClockColorOption.THEME, settings.calendarColorOption)
        assertEquals(LauncherFontOption.SYSTEM, settings.launcherFontOption)
        assertEquals(ClockColorOption.THEME, settings.appLabelColorOption)
        assertEquals(FontWeightOption.REGULAR, settings.calendarFontWeight)
        assertEquals(FontWeightOption.REGULAR, settings.homeAppsFontWeight)
        assertEquals(ClockAlignment.LEFT, settings.clockAlignment)
        assertEquals(ClockAlignment.LEFT, settings.calendarAlignment)
        assertEquals(null, settings.clockZoneHeightDp)
        assertEquals(AppListVerticalAlignment.BOTTOM, settings.appListVerticalAlignment)
    }

    @Test
    fun `setClockAlignment and setAppListVerticalAlignment round-trip independently`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each is changed
        repository.setClockAlignment(ClockAlignment.CENTER)
        repository.setAppListVerticalAlignment(AppListVerticalAlignment.TOP)

        // Then the new values come back, independently
        val settings = repository.settings.first()
        assertEquals(ClockAlignment.CENTER, settings.clockAlignment)
        assertEquals(AppListVerticalAlignment.TOP, settings.appListVerticalAlignment)
    }

    @Test
    fun `setCalendarAlignment round-trips independently of setClockAlignment`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When the calendar's alignment is changed but the clock's is left untouched
        repository.setCalendarAlignment(ClockAlignment.RIGHT)

        // Then only the calendar's alignment changes
        val settings = repository.settings.first()
        assertEquals(ClockAlignment.RIGHT, settings.calendarAlignment)
        assertEquals(ClockAlignment.LEFT, settings.clockAlignment)
    }

    @Test
    fun `setClockZoneHeight persists and round-trips`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When a clock zone height is set
        repository.setClockZoneHeight(180.5f)

        // Then it round-trips exactly
        val settings = repository.settings.first()
        assertEquals(180.5f, settings.clockZoneHeightDp!!, 0.0001f)
    }

    @Test
    fun `resetClockZoneHeight clears the value back to null`() = runTest {
        // Given a repository with a previously-dragged clock zone height
        val repository = createRepository()
        repository.setClockZoneHeight(180.5f)

        // When resetClockZoneHeight is called
        repository.resetClockZoneHeight()

        // Then the clock and app list return to their original fixed-top, bottom-anchored layout
        val settings = repository.settings.first()
        assertEquals(null, settings.clockZoneHeightDp)
    }

    @Test
    fun `setCalendarFontWeight and setHomeAppsFontWeight round-trip independently`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each font-weight setting is changed
        repository.setCalendarFontWeight(FontWeightOption.LIGHT)
        repository.setHomeAppsFontWeight(FontWeightOption.SEMI_BOLD)

        // Then the new values come back, independently
        val settings = repository.settings.first()
        assertEquals(FontWeightOption.LIGHT, settings.calendarFontWeight)
        assertEquals(FontWeightOption.SEMI_BOLD, settings.homeAppsFontWeight)
    }

    @Test
    fun `clock and calendar style setters round-trip through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each clock/calendar style setting is changed
        repository.setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
        repository.setClockFontOption(ClockFontOption.POPPINS)
        repository.setClockColorOption(ClockColorOption.THEME_INVERTED)
        repository.setClockShowMeridiem(true)
        repository.setCalendarFontOption(ClockFontOption.MANROPE)
        repository.setCalendarColorOption(ClockColorOption.ACCENT_PRIMARY)

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, settings.clockTemplateId)
        assertEquals(ClockFontOption.POPPINS, settings.clockFontOption)
        assertEquals(ClockColorOption.THEME_INVERTED, settings.clockColorOption)
        assertEquals(true, settings.clockShowMeridiem)
        assertEquals(ClockFontOption.MANROPE, settings.calendarFontOption)
        assertEquals(ClockColorOption.ACCENT_PRIMARY, settings.calendarColorOption)
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
        repository.setLauncherFontOption(LauncherFontOption.MANROPE)
        repository.setAppLabelColorOption(ClockColorOption.THEME_INVERTED)
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
        repository.setAppRowPosition(AppRowPosition.RIGHT)
        repository.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
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
        assertEquals(LauncherFontOption.MANROPE, settings.launcherFontOption)
        assertEquals(ClockColorOption.THEME_INVERTED, settings.appLabelColorOption)
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
        assertEquals(AppRowPosition.RIGHT, settings.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, settings.appRowPresentation)
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
        assertEquals(WallpaperAccentRole.PRIMARY, settings.wallpaperAccentRole)
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

    @Test
    fun `setWallpaperAccentRole round-trips independently of the accent source`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When a wallpaper role other than the default is picked
        repository.setWallpaperAccentRole(WallpaperAccentRole.SECONDARY)

        // Then it comes back, without disturbing accentFromSystem/customAccentSwatch
        val settings = repository.settings.first()
        assertEquals(WallpaperAccentRole.SECONDARY, settings.wallpaperAccentRole)
        assertEquals(true, settings.accentFromSystem)
        assertEquals(null, settings.customAccentSwatch)
    }
}
