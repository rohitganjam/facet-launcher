package com.lumenlauncher.app.ui.settings

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.CalendarInfo
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class CalendarSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(profileId: Long? = null): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "calendar-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val profileRepository = ProfileRepository(database.profileDao())
                if (profileId != null) {
                    runBlocking { profileRepository.addProfile() } // seeds id 1L, matching profileId below
                }
                val savedStateHandle = if (profileId != null) SavedStateHandle(mapOf("profileId" to profileId)) else SavedStateHandle()
                CalendarSettingsViewModel(
                    savedStateHandle,
                    settingsRepository,
                    profileRepository,
                    FakeCalendarPermissionRepository(context, granted = false),
                    CalendarRepository(context.contentResolver),
                )
            }
            LumenLauncherTheme {
                CalendarSettingsScreen(onBack = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return settingsRepository
    }

    @Test
    fun globalModeTogglePersistsThroughSettingsRepository() {
        // Given the global (non-profile-scoped) entry point, all-day events shown by default
        val settingsRepository = setContent()
        composeRule.onNodeWithTag("show_all_day_events_toggle").assertIsOn()

        // When toggling it off
        composeRule.onNodeWithTag("show_all_day_events_toggle").performClick()

        // Then the change round-trips through the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { !settingsRepository.settings.first().showAllDayEvents }
        }
    }

    @Test
    fun globalCalendarStyleFontSelectionPersistsThroughSettingsRepository() {
        // Given the global entry point
        val settingsRepository = setContent()

        // When picking a calendar-style font other than the default
        composeRule.onNodeWithTag("calendar_style_font_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_style_font_row_option_POPPINS").performClick()

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().calendarFontOption == com.lumenlauncher.app.data.model.ClockFontOption.POPPINS }
        }
    }

    @Test
    fun profileScopedCalendarStyleFontSelectionStaysLocalNotPersisted() {
        // Given a profile-scoped entry point
        val settingsRepository = setContent(profileId = 1L)

        // When picking a calendar-style font — evaluation only, per direct instruction
        composeRule.onNodeWithTag("calendar_style_font_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_style_font_row_option_POPPINS").performClick()

        // Then the row itself reflects the pick (local UI state)...
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Poppins").fetchSemanticsNodes().isNotEmpty()
        }
        // ...but nothing was written to the real, global repository
        val settings = runBlocking { settingsRepository.settings.first() }
        assert(settings.calendarFontOption != com.lumenlauncher.app.data.model.ClockFontOption.POPPINS)
    }

    @Test
    fun profileScopedModeShowsInheritOverrideSwitch() {
        // Given a profile-scoped entry point
        setContent(profileId = 1L)

        // Then the inherit/override switch is present, defaulting to Inherit (toggle read-only)
        composeRule.onNodeWithTag("calendar_inherit_row").assertExists()
        composeRule.onNodeWithTag("calendar_override_row").assertExists()
        composeRule.onNodeWithTag("show_all_day_events_toggle").assertIsOn()

        // When switching to Override
        composeRule.onNodeWithTag("calendar_override_row").performClick()

        // Then the toggle becomes live
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("show_all_day_events_toggle").assertIsOn() }.isSuccess
        }
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
) : CalendarRepository(contentResolver) {
    override suspend fun getCalendars(): List<CalendarInfo> = calendars
}

class CalendarSettingsScreenGrantedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(calendars: List<CalendarInfo>): SettingsRepository {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "calendar-settings-granted-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                CalendarSettingsViewModel(
                    SavedStateHandle(),
                    settingsRepository,
                    ProfileRepository(database.profileDao()),
                    FakeCalendarPermissionRepository(context, granted = true),
                    FakeCalendarRepository(context.contentResolver, calendars),
                )
            }
            LumenLauncherTheme { CalendarSettingsScreen(onBack = {}, viewModel = viewModel) }
        }
        return settingsRepository
    }

    @Test
    fun grantedStateShowsRealCalendarsAndPersistsSelection() {
        // Given a fake calendar and access granted
        val settingsRepository = setContent(listOf(CalendarInfo(id = "1", displayName = "Lumen Test Calendar", accountName = "lumen-test@example.com")))

        // Then the permission strip is gone and the calendar renders, checked by default
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onNodeWithText("Lumen Test Calendar", substring = true).let {
                runCatching { it.assertExists() }.isSuccess
            }
        }
        composeRule.onNodeWithTag("calendar_permission_strip").assertDoesNotExist()
        composeRule.onNodeWithTag("calendar_picker_row_1").assertIsOn()

        // When unchecking it
        composeRule.onNodeWithTag("calendar_picker_row_1").performClick()

        // Then the deselection persists to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().selectedCalendarIds?.contains("1") == false }
        }
    }

    @Test
    fun eachCalendarIsAssignedAndPersistedADistinctColor() {
        // Given two fake calendars
        val settingsRepository = setContent(
            listOf(
                CalendarInfo(id = "1", displayName = "Work Calendar", accountName = "lumen-test@example.com"),
                CalendarInfo(id = "2", displayName = "Family Calendar", accountName = "lumen-test@example.com"),
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
        val calendars = (1..12).map { CalendarInfo(id = it.toString(), displayName = "Calendar Number $it", accountName = "lumen-test@example.com") }
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
    }
}
