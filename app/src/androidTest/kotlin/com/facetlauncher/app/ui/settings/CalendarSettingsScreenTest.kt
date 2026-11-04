package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.domain.AssignCalendarColorsUseCase
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import androidx.compose.ui.test.assertIsEnabled
import com.facetlauncher.app.domain.effectiveClockConfig
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/** The clock's settings in force for the one facet. */
private fun clockOf(settingsRepository: SettingsRepository, facetRepository: FacetRepository): ClockWidgetConfig = runBlocking {
    effectiveClockConfig(facetRepository.observeFacets().first().single(), settingsRepository.settings.first())
}

class CalendarSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(): Pair<SettingsRepository, FacetRepository> {
        lateinit var settingsRepository: SettingsRepository
        lateinit var facetRepository: FacetRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "calendar-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                runBlocking { facetRepository.addFacet() } // seeds id 1L with its clock page
                CalendarSettingsViewModel(
                    SavedStateHandle(mapOf("facetId" to 1L)),
                    settingsRepository,
                    facetRepository,
                    FakeCalendarPermissionRepository(context, granted = false),
                    CalendarRepository(context.contentResolver),
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    AssignCalendarColorsUseCase(),
                    UpdateClockConfigUseCase(settingsRepository, facetRepository, ClockStackRepository(database, database.clockStackItemDao())),
                )
            }
            FacetLauncherTheme {
                CalendarSettingsScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return settingsRepository to facetRepository
    }

    @Test
    fun theAllDayToggleIsStoredOnTheClock() {
        // Given a clock showing all-day events by default
        val (settingsRepository, facetRepository) = setContent()
        composeRule.onNodeWithTag("show_all_day_events_toggle").assertIsOn()

        // When toggling it off
        composeRule.onNodeWithTag("show_all_day_events_toggle").performClick()

        // Then the clock stores the change
        composeRule.waitUntil(timeoutMillis = 3_000) { !clockOf(settingsRepository, facetRepository).calendar.showAllDay }
    }

    @Test
    fun previewStripShowsAboveTheControls() {
        // Given the global entry point, When composed
        setContent()

        // Then the wallpaper preview strip is present
        composeRule.onNodeWithTag("calendar_preview_strip").assertExists()
    }

    @Test
    fun previewStripShowsNoEventsWithoutCalendarAccess() {
        // Given calendar access is not granted, When composed
        setContent()

        // Then the strip carries no mock events and no sample note
        composeRule.onNodeWithText("Team standup").assertDoesNotExist()
        composeRule.onNodeWithTag("calendar_preview_sample_note").assertDoesNotExist()
    }

    @Test
    fun indicatorToggleAndItemsDropdownAreStoredOnTheClock() {
        // Given a clock with the indicator on and 3 items by default
        val (settingsRepository, facetRepository) = setContent()
        composeRule.onNodeWithTag("show_calendar_indicator_toggle").performScrollTo().assertIsOn()

        // When turning the indicator off and picking 2 items
        composeRule.onNodeWithTag("show_calendar_indicator_toggle").performClick()
        composeRule.onNodeWithTag("calendar_items_to_display_row").performScrollTo().performClick()
        composeRule.onNodeWithText("2").performClick()

        // Then the clock stores both changes
        composeRule.waitUntil(timeoutMillis = 3_000) {
            val calendar = clockOf(settingsRepository, facetRepository).calendar
            !calendar.showIndicator && calendar.itemsToShow == 2
        }
    }

    @Test
    fun thereIsNoInheritOverrideSwitchAndTheControlsAreLive() {
        // Given a clock's calendar settings
        setContent()

        // Then there is no switch to follow the launcher, and the toggle is live
        composeRule.onNodeWithTag("calendar_inherit_row").assertDoesNotExist()
        composeRule.onNodeWithTag("calendar_override_row").assertDoesNotExist()
        composeRule.onNodeWithTag("show_all_day_events_toggle").assertIsOn().assertIsEnabled()
    }

    @Test
    fun permissionDeniedStripRendersAndIsClickableWhenUngranted() {
        // Given the screen, READ_CALENDAR not granted (fake repository, not real device state)
        setContent()

        // Then the calendars-to-display card shows the permission-denied strip, wired to a real click action
        composeRule.onNodeWithTag("calendar_permission_strip").assertExists().assertHasClickAction()
    }
}

/**
 * Fakes calendar *data* entirely — no real Calendar Provider reads/writes.
 */
private class FakeCalendarRepository(
    contentResolver: android.content.ContentResolver,
    private val calendars: List<CalendarInfo>,
    private val todayEvents: List<CalendarEvent> = emptyList(),
) : CalendarRepository(contentResolver) {
    override suspend fun getCalendars(): List<CalendarInfo> = calendars
    override suspend fun getUpcomingEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = todayEvents
}

class CalendarSettingsScreenGrantedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(calendars: List<CalendarInfo>, todayEvents: List<CalendarEvent> = emptyList()): Pair<SettingsRepository, FacetRepository> {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var settingsRepository: SettingsRepository
        lateinit var facets: FacetRepository
        composeRule.setContent {
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "calendar-settings-granted-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao()).also { facets = it }
                runBlocking { facetRepository.addFacet() } // seeds id 1L with its clock page
                CalendarSettingsViewModel(
                    SavedStateHandle(mapOf("facetId" to 1L)),
                    settingsRepository,
                    facetRepository,
                    FakeCalendarPermissionRepository(context, granted = true),
                    FakeCalendarRepository(context.contentResolver, calendars, todayEvents),
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    AssignCalendarColorsUseCase(),
                    UpdateClockConfigUseCase(settingsRepository, facetRepository, ClockStackRepository(database, database.clockStackItemDao())),
                )
            }
            FacetLauncherTheme { CalendarSettingsScreen(onBack = {}, viewModel = viewModel) }
        }
        return settingsRepository to facets
    }

    @Test
    fun previewStripShowsLabelledSamplesWhenTodayHasNoEvents() {
        // Given access granted but nothing on today
        setContent(listOf(CalendarInfo(id = "1", displayName = "Facet Test Calendar", accountName = "facet-test@example.com")))

        // Then the sample events appear with the sample note
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("calendar_preview_sample_note").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Team standup").assertExists()
    }

    @Test
    fun previewStripShowsRealEventsWithoutTheSampleNote() {
        // Given access granted and a real event today
        val now = System.currentTimeMillis()
        val event = CalendarEvent(id = 1, calendarId = "1", title = "Real dentist", startTimeMillis = now + 3_600_000, endTimeMillis = now + 7_200_000, isAllDay = false)
        setContent(listOf(CalendarInfo(id = "1", displayName = "Facet Test Calendar", accountName = "facet-test@example.com")), listOf(event))

        // Then the real event shows in the strip and no samples or note do
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithText("Real dentist").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Team standup").assertDoesNotExist()
        composeRule.onNodeWithTag("calendar_preview_sample_note").assertDoesNotExist()
    }

    @Test
    fun grantedStateShowsRealCalendarsAndPersistsSelection() {
        // Given a fake calendar and access granted
        val (settingsRepository, facetRepository) = setContent(listOf(CalendarInfo(id = "1", displayName = "Facet Test Calendar", accountName = "facet-test@example.com")))

        // Then the permission strip is gone and the calendar renders, checked by default
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onNodeWithText("Facet Test Calendar", substring = true).let {
                runCatching { it.assertExists() }.isSuccess
            }
        }
        composeRule.onNodeWithTag("calendar_permission_strip").assertDoesNotExist()
        composeRule.onNodeWithTag("calendar_picker_row_1").assertIsOn()

        // When unchecking it
        composeRule.onNodeWithTag("calendar_picker_row_1").performClick()

        // Then the clock stores the deselection
        composeRule.waitUntil(timeoutMillis = 3_000) { clockOf(settingsRepository, facetRepository).calendar.calendarIds?.contains("1") == false }
    }

    @Test
    fun eachCalendarIsAssignedAndPersistedADistinctColor() {
        // Given two fake calendars
        val (settingsRepository, _) = setContent(
            listOf(
                CalendarInfo(id = "1", displayName = "Work Calendar", accountName = "facet-test@example.com"),
                CalendarInfo(id = "2", displayName = "Family Calendar", accountName = "facet-test@example.com"),
            ),
        )

        // Then both render, and the settings that back their color dots persist a distinct
        // AccentSwatch name for each — not just "some color", but two different ones
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking {
                val colors = settingsRepository.settings.first().calendarColors
                colors["1"] != null && colors["2"] != null
            }
        }
        val colors = runBlocking { settingsRepository.settings.first().calendarColors }
        org.junit.Assert.assertNotEquals(colors["1"], colors["2"])
    }

    @Test
    fun manyCalendarsOverflowingTheScreenAreScrollable() {
        // Given more fake calendars than fit in one screen height
        val calendars = (1..12).map { CalendarInfo(id = it.toString(), displayName = "Calendar Number $it", accountName = "facet-test@example.com") }
        setContent(calendars)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onNodeWithText("Calendar Number 12", substring = true).let {
                runCatching { it.assertExists() }.isSuccess
            }
        }

        // When scrolling the last one into view
        composeRule.onNodeWithText("Calendar Number 12", substring = true).performScrollTo()

        // Then it's actually visible on screen — not just present off-screen in a Column
        // that never scrolled at all
        composeRule.onNodeWithText("Calendar Number 12", substring = true).assertIsDisplayed()

        // And the pinned header stayed on screen the whole time, not scrolled away with the list
        composeRule.onNodeWithText("Calendar settings").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }
}
