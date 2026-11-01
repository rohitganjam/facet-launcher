package com.facetlauncher.app.data

import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.model.LandscapeOverrides
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.data.model.whatsNewSeenId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `a grid size stored by an older version loads as the nearest density`() = runTest {
        // Given a DataStore that still holds the retired column by row value
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("legacy-${System.nanoTime()}.preferences_pb") },
        )
        dataStore.edit { it[androidx.datastore.preferences.core.stringPreferencesKey("drawer_grid_size")] = "FOUR_BY_FIVE" }

        // When the settings are read
        val settings = SettingsRepository(dataStore).settings.first()

        // Then it maps to Regular
        assertEquals(DrawerGridDensity.REGULAR, settings.drawerGridDensity)
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
        assertEquals(DrawerGridDensity.COMPACT, settings.drawerGridDensity)
        assertEquals(DrawerListItemSize.REGULAR, settings.drawerListItemSize)
        assertEquals(IconRenderMode.SYSTEM_DEFAULT, settings.iconRenderMode)
        assertEquals(IconShape.SQUIRCLE, settings.iconShape)
        assertEquals(0.6f, settings.drawerOpacity, 0.0001f)
        assertEquals(true, settings.notificationDotsEnabled)
        assertEquals(NotificationBadgeStyle.DOT, settings.notificationBadgeStyle)
        assertEquals(false, settings.showMediaPlayerOnHome)
        assertEquals(true, settings.showMediaSkipButtons)
        assertEquals(true, settings.showNotificationsInAppMenu)
        assertEquals(true, settings.showDrawerIcons)
        assertEquals(true, settings.showDrawerLabels)
        assertEquals(SearchBarPosition.TOP, settings.searchBarPosition)
        assertEquals(DrawerFolderDisplayMode.DO_NOT_SHOW, settings.drawerFolderDisplayMode)
        assertEquals(RecentlyInstalledPosition.SHOW_FIRST, settings.recentlyInstalledPosition)
        assertEquals(NO_ACTIVE_FACET_ID, settings.activeFacetId)
        assertEquals(true, settings.showAllDayEvents)
        assertEquals(true, settings.showCalendarIndicator)
        assertEquals(3, settings.calendarItemsToShow)
        assertEquals(null, settings.selectedCalendarIds)
        assertEquals(emptyMap<String, String>(), settings.calendarColors)
        assertEquals(false, settings.searchContactsEnabled)
        assertEquals(false, settings.searchSettingsEnabled)
        assertEquals(AppRowPosition.LEFT, settings.appRowPosition)
        assertEquals(AppRowPresentation.ICON_AND_TEXT, settings.appRowPresentation)
        assertEquals(ListContentMode.FAVORITES, settings.listContentMode)
        assertEquals(6, settings.appsToShowCount)
        assertEquals(ClockTemplateId.LIGHT_STACK, settings.clockTemplateId)
        assertEquals(ClockFontOption.LAUNCHER_DEFAULT, settings.clockFontOption)
        assertEquals(ClockColorOption.THEME, settings.clockColorOption)
        assertFalse(settings.clockShowMeridiem)
        assertEquals(LauncherFontOption.SYSTEM, settings.launcherFontOption)
        assertEquals(FontScaleOption.DEFAULT, settings.fontScaleOption)
        assertEquals(ClockColorOption.THEME, settings.appLabelColorOption)
        assertEquals(FontWeightOption.REGULAR, settings.homeAppsFontWeight)
        assertEquals(ClockAlignment.LEFT, settings.clockAlignment)
        assertEquals(null, settings.clockZoneHeightDp)
        assertEquals(0.8f, settings.clockScale, 0.0001f)
        assertEquals(AppListVerticalAlignment.BOTTOM, settings.appListVerticalAlignment)
        assertEquals(AppListLayout.SINGLE_COLUMN, settings.appListLayout)
        assertEquals(AppListColumnAlignment.BOTH_LEFT, settings.appListColumnAlignment)
        assertEquals(AppListGridColumns.FOUR, settings.appListGridColumns)
        assertEquals(AppListGridDisplayMode.ICONS, settings.appListGridDisplayMode)
        assertFalse(settings.onboardingCompleted)
        assertFalse(settings.defaultsSeeded)
        assertEquals(emptySet<String>(), settings.coachMarksSeen)
    }

    @Test
    fun `setAppListGridColumns round-trips three columns`() = runTest {
        // Given a repository at its default of four columns
        val repository = createRepository()

        // When three columns is chosen
        repository.setAppListGridColumns(AppListGridColumns.THREE)

        // Then it round-trips with a column count of 3
        val settings = repository.settings.first()
        assertEquals(AppListGridColumns.THREE, settings.appListGridColumns)
        assertEquals(3, settings.appListGridColumns.columns)
    }

    @Test
    fun `setOnboardingCompleted and setDefaultsSeeded round-trip independently`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When only one is set
        repository.setOnboardingCompleted(true)

        // Then it round-trips and the other stays at its default
        val settings = repository.settings.first()
        assertEquals(true, settings.onboardingCompleted)
        assertFalse(settings.defaultsSeeded)
    }

    @Test
    fun `completing onboarding marks this build's whats new as seen, and un-completing does not`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When onboarding is completed
        repository.setOnboardingCompleted(true)

        // Then a fresh install has nothing new to announce
        assertTrue(whatsNewSeenId(BuildConfig.VERSION_NAME) in repository.settings.first().coachMarksSeen)

        // And resetting onboarding for another repository leaves the set empty
        assertEquals(emptySet<String>(), createRepository().also { it.setOnboardingCompleted(false) }.settings.first().coachMarksSeen)
    }

    @Test
    fun `markCoachMarkSeen is additive`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When two different coach marks are marked seen, one after the other
        repository.markCoachMarkSeen("HOME_GESTURES")
        repository.markCoachMarkSeen("SOME_OTHER_MARK")

        // Then both persist, neither overwriting the other
        val settings = repository.settings.first()
        assertEquals(setOf("HOME_GESTURES", "SOME_OTHER_MARK"), settings.coachMarksSeen)
    }

    @Test
    fun `clearCoachMarkSeen removes only that id`() = runTest {
        val repository = createRepository()
        repository.markCoachMarkSeen("HOME_GESTURES")
        repository.markCoachMarkSeen("SOME_OTHER_MARK")

        repository.clearCoachMarkSeen("HOME_GESTURES")

        assertEquals(setOf("SOME_OTHER_MARK"), repository.settings.first().coachMarksSeen)
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
    fun `setClockAccentColorOption and setClockDateStyle round-trip independently`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each is changed
        repository.setClockAccentColorOption(ClockColorOption.ACCENT_SECONDARY)
        repository.setClockDateStyle(ClockDateStyle.CONDENSED)

        // Then the new values come back, independently of each other and of the main color option
        val settings = repository.settings.first()
        assertEquals(ClockColorOption.ACCENT_SECONDARY, settings.clockAccentColorOption)
        assertEquals(ClockDateStyle.CONDENSED, settings.clockDateStyle)
        assertEquals(ClockColorOption.THEME, settings.clockColorOption)
    }

    @Test
    fun `setClockScale persists and round-trips`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When the clock scale is set
        repository.setClockScale(1.5f)

        // Then it round-trips exactly
        assertEquals(1.5f, repository.settings.first().clockScale, 0.0001f)
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
    fun `setHomeAppsFontWeight persists and round-trips`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When the font-weight setting is changed
        repository.setHomeAppsFontWeight(FontWeightOption.SEMI_BOLD)

        // Then the new value comes back
        val settings = repository.settings.first()
        assertEquals(FontWeightOption.SEMI_BOLD, settings.homeAppsFontWeight)
    }

    @Test
    fun `clock style setters round-trip through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each clock style setting is changed
        repository.setClockTemplateId(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR)
        repository.setClockFontOption(ClockFontOption.POPPINS)
        repository.setClockColorOption(ClockColorOption.THEME_INVERTED)
        repository.setClockShowMeridiem(true)

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR, settings.clockTemplateId)
        assertEquals(ClockFontOption.POPPINS, settings.clockFontOption)
        assertEquals(ClockColorOption.THEME_INVERTED, settings.clockColorOption)
        assertEquals(true, settings.clockShowMeridiem)
    }

    @Test
    fun `each setter round-trips through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When each setting is changed
        repository.setUse24HourTime(true)
        repository.setDockDisplayMode(DockDisplayMode.TEXT)
        repository.setDrawerPresentation(DrawerPresentation.GRID)
        repository.setDrawerGridDensity(DrawerGridDensity.SPACIOUS)
        repository.setDrawerListItemSize(DrawerListItemSize.SPACIOUS)
        repository.setIconRenderMode(IconRenderMode.MONOCHROME_ACCENT)
        repository.setIconShape(IconShape.ROUNDED)
        repository.setLauncherFontOption(LauncherFontOption.MANROPE)
        repository.setFontScaleOption(FontScaleOption.LARGE)
        repository.setAppLabelColorOption(ClockColorOption.THEME_INVERTED)
        repository.setDrawerOpacity(0.5f)
        repository.setNotificationDotsEnabled(false)
        repository.setNotificationBadgeStyle(NotificationBadgeStyle.COUNT)
        repository.setShowMediaPlayerOnHome(true)
        repository.setShowMediaSkipButtons(false)
        repository.setShowNotificationsInAppMenu(false)
        repository.setShowDrawerIcons(false)
        repository.setShowDrawerLabels(false)
        repository.setSearchBarPosition(SearchBarPosition.BOTTOM)
        repository.setDrawerFolderDisplayMode(DrawerFolderDisplayMode.INLINE)
        repository.setRecentlyInstalledPosition(RecentlyInstalledPosition.SHOW_LAST)
        repository.setActiveFacetId(7L)
        repository.setShowAllDayEvents(false)
        repository.setShowCalendarIndicator(false)
        repository.setCalendarItemsToShow(2)
        repository.setSelectedCalendarIds(setOf("cal-1", "cal-2"))
        repository.setCalendarColors(mapOf("cal-1" to "BLUE", "cal-2" to "TEAL"))
        repository.setSearchContactsEnabled(true)
        repository.setSearchSettingsEnabled(true)
        repository.setAppRowPosition(AppRowPosition.RIGHT)
        repository.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        repository.setAppListLayout(AppListLayout.GRID)
        repository.setAppListColumnAlignment(AppListColumnAlignment.MIRRORED)
        repository.setAppListGridColumns(AppListGridColumns.SIX)
        repository.setAppListGridDisplayMode(AppListGridDisplayMode.TEXT)
        repository.setListContentMode(ListContentMode.MOST_USED)
        repository.setAppsToShowCount(6)

        // Then the new values come back
        val settings = repository.settings.first()
        assertEquals(true, settings.use24HourTime)
        assertEquals(DockDisplayMode.TEXT, settings.dockDisplayMode)
        assertEquals(DrawerPresentation.GRID, settings.drawerPresentation)
        assertEquals(DrawerGridDensity.SPACIOUS, settings.drawerGridDensity)
        assertEquals(DrawerListItemSize.SPACIOUS, settings.drawerListItemSize)
        assertEquals(IconRenderMode.MONOCHROME_ACCENT, settings.iconRenderMode)
        assertEquals(IconShape.ROUNDED, settings.iconShape)
        assertEquals(LauncherFontOption.MANROPE, settings.launcherFontOption)
        assertEquals(FontScaleOption.LARGE, settings.fontScaleOption)
        assertEquals(ClockColorOption.THEME_INVERTED, settings.appLabelColorOption)
        assertEquals(0.5f, settings.drawerOpacity, 0.0001f)
        assertEquals(false, settings.notificationDotsEnabled)
        assertEquals(NotificationBadgeStyle.COUNT, settings.notificationBadgeStyle)
        assertEquals(true, settings.showMediaPlayerOnHome)
        assertEquals(false, settings.showMediaSkipButtons)
        assertEquals(false, settings.showNotificationsInAppMenu)
        assertEquals(false, settings.showDrawerIcons)
        assertEquals(false, settings.showDrawerLabels)
        assertEquals(SearchBarPosition.BOTTOM, settings.searchBarPosition)
        assertEquals(DrawerFolderDisplayMode.INLINE, settings.drawerFolderDisplayMode)
        assertEquals(RecentlyInstalledPosition.SHOW_LAST, settings.recentlyInstalledPosition)
        assertEquals(7L, settings.activeFacetId)
        assertEquals(false, settings.showAllDayEvents)
        assertEquals(false, settings.showCalendarIndicator)
        assertEquals(2, settings.calendarItemsToShow)
        assertEquals(setOf("cal-1", "cal-2"), settings.selectedCalendarIds)
        assertEquals(mapOf("cal-1" to "BLUE", "cal-2" to "TEAL"), settings.calendarColors)
        assertEquals(true, settings.searchContactsEnabled)
        assertEquals(true, settings.searchSettingsEnabled)
        assertEquals(AppRowPosition.RIGHT, settings.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, settings.appRowPresentation)
        assertEquals(AppListLayout.GRID, settings.appListLayout)
        assertEquals(AppListColumnAlignment.MIRRORED, settings.appListColumnAlignment)
        assertEquals(AppListGridColumns.SIX, settings.appListGridColumns)
        assertEquals(AppListGridDisplayMode.TEXT, settings.appListGridDisplayMode)
        assertEquals(ListContentMode.MOST_USED, settings.listContentMode)
        assertEquals(6, settings.appsToShowCount)
    }

    @Test
    fun `default apps-to-show count is coerced into the 3 to 12 range`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When set below the floor, it's coerced up to 3
        repository.setAppsToShowCount(1)
        assertEquals(3, repository.settings.first().appsToShowCount)

        // When set above the ceiling, it's coerced down to 12
        repository.setAppsToShowCount(99)
        assertEquals(12, repository.settings.first().appsToShowCount)
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
    fun `an icon pack is applied, its fallback kept, and cleared back to the built-in style`() = runTest {
        // Given a repository on a built-in glyph style
        val repository = createRepository()
        repository.setIconRenderMode(IconRenderMode.GLYPH_ACCENT)
        assertNull(repository.settings.first().iconPackPackage)
        assertEquals(IconPackFallback.MATCH_PACK, repository.settings.first().iconPackFallback)

        // When applying a pack that keeps missed apps' own icons
        repository.setIconPack("com.example.pack")
        repository.setIconPackFallback(IconPackFallback.KEEP_DEFAULT)

        // Then both are stored, and icons draw as the pack has them
        val applied = repository.settings.first()
        assertEquals("com.example.pack", applied.iconPackPackage)
        assertEquals(IconPackFallback.KEEP_DEFAULT, applied.iconPackFallback)
        assertEquals(IconRenderMode.SYSTEM_DEFAULT, applied.effectiveIconRenderMode)

        // And clearing the pack brings the stored glyph style back
        repository.setIconPack(null)
        val cleared = repository.settings.first()
        assertNull(cleared.iconPackPackage)
        assertEquals(IconRenderMode.GLYPH_ACCENT, cleared.effectiveIconRenderMode)
    }

    @Test
    fun `system bar icon style defaults to matching the theme`() = runTest {
        // Given a fresh repository with nothing written yet
        val repository = createRepository()

        // Then the bar icons follow the theme
        assertEquals(SystemBarIconStyle.MATCH_THEME, repository.settings.first().systemBarIconStyle)
    }

    @Test
    fun `system bar icon style round-trips through the settings flow`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When forcing light icons, then dark icons
        repository.setSystemBarIconStyle(SystemBarIconStyle.LIGHT)
        assertEquals(SystemBarIconStyle.LIGHT, repository.settings.first().systemBarIconStyle)
        repository.setSystemBarIconStyle(SystemBarIconStyle.DARK)

        // Then each value comes back
        assertEquals(SystemBarIconStyle.DARK, repository.settings.first().systemBarIconStyle)
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

    @Test
    fun `the permission-requested flags all default to false`() = runTest {
        val settings = createRepository().settings.first()

        assertFalse(settings.calendarPermissionRequested)
        assertFalse(settings.contactsPermissionRequested)
        assertFalse(settings.bluetoothPermissionRequested)
        assertFalse(settings.locationPermissionRequested)
    }

    @Test
    fun `setBluetoothPermissionRequested and setLocationPermissionRequested round-trip independently`() = runTest {
        // Given a repository
        val repository = createRepository()

        // When only the Bluetooth request is recorded
        repository.setBluetoothPermissionRequested(true)

        // Then it comes back without touching location or the other flags
        val afterBluetooth = repository.settings.first()
        assertEquals(true, afterBluetooth.bluetoothPermissionRequested)
        assertEquals(false, afterBluetooth.locationPermissionRequested)
        assertEquals(false, afterBluetooth.contactsPermissionRequested)

        // And recording location then leaves Bluetooth set
        repository.setLocationPermissionRequested(true)
        val afterLocation = repository.settings.first()
        assertEquals(true, afterLocation.bluetoothPermissionRequested)
        assertEquals(true, afterLocation.locationPermissionRequested)
    }

    @Test
    fun `allow landscape defaults to on and persists when turned off`() = runTest {
        val repository = createRepository()
        assertEquals(true, repository.settings.first().allowLandscape)

        repository.setAllowLandscape(false)

        assertEquals(false, repository.settings.first().allowLandscape)
    }

    @Test
    fun `landscape values are saved apart from the portrait ones and default to none`() = runTest {
        val repository = createRepository()
        assertEquals(LandscapeOverrides(), repository.settings.first().landscape)

        // When the landscape values are written
        val written = LandscapeOverrides(
            clockAlignment = ClockAlignment.CENTER,
            clockZoneHeightDp = 310f,
            clockScale = 1.2f,
            appRowPosition = AppRowPosition.CENTER,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            appListColumnAlignment = AppListColumnAlignment.MIRRORED,
        )
        repository.updateLandscape { written }

        // Then they read back, and the portrait values are untouched
        val saved = repository.settings.first()
        assertEquals(written, saved.landscape)
        assertEquals(ClockAlignment.LEFT, saved.clockAlignment)
        assertEquals(null, saved.clockZoneHeightDp)
        assertEquals(0.8f, saved.clockScale, 0.001f)
        assertEquals(AppRowPosition.LEFT, saved.appRowPosition)
    }

    @Test
    fun `updateLandscape changes only the fields it names and a null clears one`() = runTest {
        val repository = createRepository()
        repository.updateLandscape { it.copy(clockScale = 1.2f, appRowPosition = AppRowPosition.RIGHT) }

        repository.updateLandscape { it.copy(appRowPosition = null) }

        val landscape = repository.settings.first().landscape
        assertEquals(1.2f, landscape.clockScale!!, 0.001f)
        assertEquals(null, landscape.appRowPosition)
    }

    @Test
    fun `resetting the clock height clears its landscape value too`() = runTest {
        val repository = createRepository()
        repository.updateLandscape { it.copy(clockZoneHeightDp = 310f, clockScale = 1.2f, clockAlignment = ClockAlignment.CENTER) }

        repository.resetClockZoneHeight()

        val landscape = repository.settings.first().landscape
        assertEquals(null, landscape.clockZoneHeightDp)
        assertEquals(ClockAlignment.CENTER, landscape.clockAlignment)
    }
}
