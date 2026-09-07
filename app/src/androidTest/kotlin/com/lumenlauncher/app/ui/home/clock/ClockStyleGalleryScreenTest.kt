package com.lumenlauncher.app.ui.home.clock

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockStyleGalleryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}, profileId: Long? = null): Pair<SettingsRepository, ProfileRepository> {
        lateinit var settingsRepository: SettingsRepository
        lateinit var profileRepository: ProfileRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                profileRepository = ProfileRepository(database.profileDao())
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "clock-style-gallery-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                if (profileId != null) {
                    runBlocking { profileRepository.addProfile() } // seeds id 1L, matching profileId below
                }
                val savedStateHandle = if (profileId != null) SavedStateHandle(mapOf("profileId" to profileId)) else SavedStateHandle()
                ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, profileRepository)
            }
            LumenLauncherTheme {
                ClockStyleGalleryRoute(onBack = onBack, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return settingsRepository to profileRepository
    }

    @Test
    fun headerStaysVisibleAfterScrollingThroughTheTemplateList() {
        // Given the gallery, scrolled all the way to the last template card
        setContent()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.entries.last().name}"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Clock & Calendar Style").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun tappingATemplateCardPersistsItAsTheChosenTemplate() {
        // Given the gallery, Light stack applied by default — scrolled into view since the new
        // Position section (alignment picker + reset row) above it pushes the template list lower
        // than the initially-composed viewport on some screen sizes.
        val (settingsRepository, _) = setContent()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.LIGHT_STACK.name}"))
        composeRule.onNodeWithTag("clock_template_card_${ClockTemplateId.LIGHT_STACK.name}").assertExists()

        // When tapping a different template's card
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.VERTICAL_STACK_BOLD_HOUR.name}"))
        composeRule.onNodeWithTag("clock_template_card_${ClockTemplateId.VERTICAL_STACK_BOLD_HOUR.name}").performClick()

        // Then it's persisted as the new choice
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockTemplateId == ClockTemplateId.VERTICAL_STACK_BOLD_HOUR }
        }
    }

    @Test
    fun toggling24HourTimePersistsAndDisablesTheMeridiemToggle() {
        // Given the gallery, 24-hour time off by default
        val (settingsRepository, _) = setContent()
        composeRule.onNodeWithTag("clock_use_24_hour_time_toggle").assertIsOff()
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").assertIsOff()

        // When turning 24-hour time on
        composeRule.onNodeWithTag("clock_use_24_hour_time_toggle").performClick()

        // Then it persists, and the meridiem toggle becomes disabled (24-hour time has no AM/PM)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().use24HourTime }
        }
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").assertIsNotEnabled()
    }

    @Test
    fun togglingShowMeridiemPersists() {
        // Given the gallery, meridiem off by default
        val (settingsRepository, _) = setContent()

        // When turning it on
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").performClick()

        // Then it persists
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockShowMeridiem }
        }
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").assertIsOn()
    }

    @Test
    fun globalClockAlignmentSelectionPersistsThroughSettingsRepository() {
        // Given the global (non-profile-scoped) entry point, Left by default
        val (settingsRepository, _) = setContent()

        // When picking Center alignment
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_alignment_row"))
        composeRule.onNodeWithTag("clock_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_alignment_row_option_CENTER").performClick()

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockAlignment == com.lumenlauncher.app.data.model.ClockAlignment.CENTER }
        }
    }

    @Test
    fun globalCalendarAlignmentSelectionPersistsThroughSettingsRepositoryIndependentlyOfClockAlignment() {
        // Given the global (non-profile-scoped) entry point, Left by default for both
        val (settingsRepository, _) = setContent()

        // When picking Right alignment for the calendar only
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("calendar_alignment_row"))
        composeRule.onNodeWithTag("calendar_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_alignment_row_option_RIGHT").performClick()

        // Then it's persisted to the real repository without touching the clock's own alignment
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().calendarAlignment == com.lumenlauncher.app.data.model.ClockAlignment.RIGHT }
        }
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, runBlocking { settingsRepository.settings.first().clockAlignment })
    }

    @Test
    fun calendarPreviewMovesLiveWhenCalendarAlignmentChanges() {
        // Given the gallery, the calendar preview's own event row initially left-packed (default)
        val (_, _) = setContent()
        val leftBefore = composeRule.onNodeWithTag("clock_event_row_1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left

        // When selecting Right calendar alignment
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("calendar_alignment_row"))
        composeRule.onNodeWithTag("calendar_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_alignment_row_option_RIGHT").performClick()
        composeRule.waitForIdle()

        // Then the preview's own event row shifts further right — it moves live with the setting,
        // not just on the real Home screen
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_event_row_1"))
        val leftAfter = composeRule.onNodeWithTag("clock_event_row_1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        org.junit.Assert.assertTrue(leftAfter > leftBefore)
    }

    @Test
    fun tappingResetClockWidgetPositionClearsTheZoneHeightAndBothAlignments() {
        // Given a previously-dragged clock zone height and non-default alignments, both persisted
        val (settingsRepository, _) = setContent()
        runBlocking {
            settingsRepository.setClockZoneHeight(180f)
            settingsRepository.setClockAlignment(com.lumenlauncher.app.data.model.ClockAlignment.RIGHT)
            settingsRepository.setCalendarAlignment(com.lumenlauncher.app.data.model.ClockAlignment.CENTER)
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockZoneHeightDp != null }
        }

        // When "Reset clock widget position" is tapped
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("reset_clock_position_row"))
        composeRule.onNodeWithTag("reset_clock_position_row").performClick()
        composeRule.waitForIdle()

        // Then the zone height clears AND both alignments return to Left — the whole widget's
        // position resets, not just its height
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockZoneHeightDp == null }
        }
        val settings = runBlocking { settingsRepository.settings.first() }
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, settings.clockAlignment)
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, settings.calendarAlignment)
    }

    @Test
    fun profileScopedClockAlignmentPersistsToTheProfileDirectly() {
        // Given a profile-scoped entry point — alignment is now part of the same Clock+Calendar
        // design bundle as font/color/template, so it's profile-overridable the same way (see
        // chat history: this used to be global-only and hidden entirely on this variant).
        val (settingsRepository, profileRepository) = setContent(profileId = 1L)

        // When picking Center alignment for the clock
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_alignment_row"))
        composeRule.onNodeWithTag("clock_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_alignment_row_option_CENTER").performClick()

        // Then it's persisted to this profile's own row, not the launcher-wide global setting
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking {
                profileRepository.observeProfiles().first().single().clockAlignment == com.lumenlauncher.app.data.model.ClockAlignment.CENTER
            }
        }
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, runBlocking { settingsRepository.settings.first().clockAlignment })
    }

    @Test
    fun profileScopedCalendarAlignmentPersistsToTheProfileDirectly() {
        // Given a profile-scoped entry point
        val (settingsRepository, profileRepository) = setContent(profileId = 1L)

        // When picking Right alignment for the calendar
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("calendar_alignment_row"))
        composeRule.onNodeWithTag("calendar_alignment_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_alignment_row_option_RIGHT").performClick()

        // Then it's persisted to this profile's own row, not the launcher-wide global setting
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking {
                profileRepository.observeProfiles().first().single().calendarAlignment == com.lumenlauncher.app.data.model.ClockAlignment.RIGHT
            }
        }
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, runBlocking { settingsRepository.settings.first().calendarAlignment })
    }

    @Test
    fun profileScopedResetClockWidgetPositionClearsTheProfilesOwnHeightAndBothAlignments() {
        // Given a profile-scoped entry point with a previously-dragged height and non-default alignments
        val (_, profileRepository) = setContent(profileId = 1L)
        runBlocking {
            // Each setter re-fetches the current row rather than reusing one stale snapshot —
            // otherwise the next call's copy() would silently clobber the previous field back to
            // its original value (see chat history: this exact bug bit this test's first draft).
            profileRepository.setClockZoneHeight(profileRepository.observeProfiles().first().single(), 180f)
            profileRepository.setClockAlignment(profileRepository.observeProfiles().first().single(), com.lumenlauncher.app.data.model.ClockAlignment.RIGHT)
            profileRepository.setCalendarAlignment(profileRepository.observeProfiles().first().single(), com.lumenlauncher.app.data.model.ClockAlignment.CENTER)
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().single().clockZoneHeightDp != null }
        }

        // When "Reset clock widget position" is tapped
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("reset_clock_position_row"))
        composeRule.onNodeWithTag("reset_clock_position_row").performClick()
        composeRule.waitForIdle()

        // Then this profile's own height and both alignments reset — the global default is untouched
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().single().clockZoneHeightDp == null }
        }
        val profile = runBlocking { profileRepository.observeProfiles().first().single() }
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, profile.clockAlignment)
        assertEquals(com.lumenlauncher.app.data.model.ClockAlignment.LEFT, profile.calendarAlignment)
    }

    @Test
    fun calendarAndClockSectionsAreSeparatedByADivider() {
        // Given the gallery
        setContent()

        // Then a divider sits between the Calendar section (font/color + preview) and the Clock section
        composeRule.onNodeWithTag("calendar_clock_section_divider").assertIsDisplayed()
    }

    @Test
    fun globalCalendarStyleFontSelectionPersistsThroughSettingsRepository() {
        // Given the global (non-profile-scoped) entry point
        val (settingsRepository, _) = setContent()

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
    fun profileScopedCalendarStyleFontPersistsToTheProfileDirectly() {
        // Given a profile-scoped entry point — this gallery has no per-row Inherit/Override
        // gating of its own; reaching it scoped to a profile always edits that profile directly
        // (the Inherit/Override choice lives on ProfileSettingsScreen's own card instead).
        val (settingsRepository, profileRepository) = setContent(profileId = 1L)

        // When picking a calendar-style font other than the default
        composeRule.onNodeWithTag("calendar_style_font_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_style_font_row_option_POPPINS").performClick()

        // Then it's persisted to this profile's own row, not the launcher-wide global setting
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking {
                profileRepository.observeProfiles().first().single().calendarFontOption == com.lumenlauncher.app.data.model.ClockFontOption.POPPINS
            }
        }
        val settings = runBlocking { settingsRepository.settings.first() }
        assert(settings.calendarFontOption != com.lumenlauncher.app.data.model.ClockFontOption.POPPINS)
    }

    @Test
    fun globalCalendarWeightSelectionPersistsThroughSettingsRepository() {
        // Given the global (non-profile-scoped) entry point, Regular weight by default
        val (settingsRepository, _) = setContent()

        // When dragging the calendar weight slider to its last stop (Semi Bold)
        composeRule.onNodeWithTag("calendar_style_weight_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontWeightOption.entries.size - 1).toFloat()) }

        // Then it's persisted to the real repository
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().calendarFontWeight == FontWeightOption.SEMI_BOLD }
        }
    }

    @Test
    fun profileScopedCalendarWeightPersistsToTheProfileDirectly() {
        // Given a profile-scoped entry point (same reasoning as the font/color tests above)
        val (settingsRepository, profileRepository) = setContent(profileId = 1L)

        // When dragging the calendar weight slider to its last stop (Semi Bold)
        composeRule.onNodeWithTag("calendar_style_weight_slider_control")
            .performSemanticsAction(SemanticsActions.SetProgress) { it((FontWeightOption.entries.size - 1).toFloat()) }

        // Then it's persisted to this profile's own row, not the launcher-wide global setting
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepository.observeProfiles().first().single().calendarFontWeight == FontWeightOption.SEMI_BOLD }
        }
        val settings = runBlocking { settingsRepository.settings.first() }
        assert(settings.calendarFontWeight != FontWeightOption.SEMI_BOLD)
    }
}
