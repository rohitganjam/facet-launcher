package com.facetlauncher.app.ui.home.clock

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockStyleGalleryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onBack: () -> Unit = {}, facetId: Long? = null, onOpenClockAccessories: () -> Unit = {}): Pair<SettingsRepository, FacetRepository> {
        lateinit var settingsRepository: SettingsRepository
        lateinit var facetRepository: FacetRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "clock-style-gallery-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                if (facetId != null) {
                    runBlocking { facetRepository.addFacet() } // seeds id 1L, matching facetId below
                }
                val savedStateHandle = if (facetId != null) SavedStateHandle(mapOf("facetId" to facetId)) else SavedStateHandle()
                ClockStyleGalleryViewModel(savedStateHandle, settingsRepository, facetRepository)
            }
            FacetLauncherTheme {
                ClockStyleGalleryRoute(onBack = onBack, onOpenClockAccessories = onOpenClockAccessories, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return settingsRepository to facetRepository
    }

    @Test
    fun clockAccessoriesRowOpensTheAccessoriesScreen() {
        // Given the gallery
        var opened = false
        setContent(onOpenClockAccessories = { opened = true })

        // When tapping the Clock accessories row
        composeRule.onNodeWithTag("clock_style_gallery_list").performScrollToNode(hasTestTag("clock_accessories_row"))
        composeRule.onNodeWithTag("clock_accessories_row").performClick()

        // Then it opens the accessories screen
        assertEquals(true, opened)
    }

    @Test
    fun headerStaysVisibleAfterScrollingThroughTheTemplateList() {
        // Given the gallery, scrolled all the way to the last template card
        setContent()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.entries.last().name}"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Clock Style").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun tappingATemplateCardPersistsItAsTheChosenTemplate() {
        // Given the gallery, Light stack applied by default — scrolled into view since the sections
        // above it can push the template list lower than the initially-composed viewport on some screen sizes.
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
    fun accentColorRowIsAlwaysVisibleRegardlessOfTheSelectedTemplate() {
        // Given the gallery, Light stack applied by default — no accent element in its own design,
        // but the row still shows (rather than requiring a scroll-down-and-back-up to a
        // template that has one before it's reachable at all — see chat history)
        val (settingsRepository, _) = setContent()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_accent_color_row"))
        composeRule.onNodeWithTag("clock_accent_color_row").assertExists()

        // When switching to Accent Field, which does have one
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.ACCENT_FIELD.name}"))
        composeRule.onNodeWithTag("clock_template_card_${ClockTemplateId.ACCENT_FIELD.name}").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockTemplateId == ClockTemplateId.ACCENT_FIELD }
        }

        // Then the Accent color row is still there, unchanged
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_accent_color_row"))
        composeRule.onNodeWithTag("clock_accent_color_row").assertExists()
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

    // calendarPreviewMovesLiveWhenClockAlignmentChanges moved to AppearanceSettingsScreenTest —
    // the calendar preview itself lives there now (see chat history).

    @Test
    fun theGalleryHasNoAlignmentRowInEitherScope() {
        // Given the launcher-wide gallery
        setContent()

        // Then clock alignment is not set here: it is the facet's own and set from Home
        composeRule.onNodeWithTag("clock_alignment_row").assertDoesNotExist()
    }

    @Test
    fun theFacetScopedGalleryHasNoAlignmentRowEvenWhileOverriding() {
        // Given a facet that overrides the clock style
        val (_, facetRepository) = setContent(facetId = 1L)
        composeRule.onNodeWithTag("clock_style_override_row").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepository.observeFacets().first().any { it.overrideClock } }
        }

        // Then there is still no alignment row: the override does not cover placement
        composeRule.onNodeWithTag("clock_alignment_row").assertDoesNotExist()
    }

    @Test
    fun theLauncherWideGalleryHasNoResetPositionRow() {
        // Given the launcher-wide gallery, which has no placement of its own to reset
        setContent()

        // Then the Position section with its reset row is not there
        composeRule.onNodeWithTag("reset_clock_position_row").assertDoesNotExist()
    }

    @Test
    fun resetClockPositionGivesAFacetsOwnPlacementBackToTheLauncherFallback() {
        // Given a facet that owns a dragged placement (it does not need to override the clock style)
        val (_, facetRepository) = setContent(facetId = 1L)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("clock_style_override_row").fetchSemanticsNodes().isNotEmpty()
        }
        runBlocking {
            val id = facetRepository.observeFacets().first().single().id
            facetRepository.setClockPlacement(id, com.facetlauncher.app.data.model.ClockAlignment.RIGHT, 180f, 1.4f, com.facetlauncher.app.data.model.LandscapeOverrides())
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepository.observeFacets().first().single().clockPlacementOwn }
        }

        // When "Reset clock position" is tapped
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("reset_clock_position_row"))
        composeRule.onNodeWithTag("reset_clock_position_row").performClick()
        composeRule.waitForIdle()

        // Then the facet stops owning its placement and its own values are cleared
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { !facetRepository.observeFacets().first().single().clockPlacementOwn }
        }
        val facet = runBlocking { facetRepository.observeFacets().first().single() }
        assertEquals(com.facetlauncher.app.data.model.ClockAlignment.LEFT, facet.clockAlignment)
        assertEquals(null, facet.clockZoneHeightDp)
    }

    // The calendar preview (and its divider from the Clock section) moved to
    // AppearanceSettingsScreen — see chat history; coverage moved to
    // AppearanceSettingsScreenTest's own preview tests.

    @Test
    fun facetScopedModeShowsInheritOverrideSwitch() {
        // Given a facet-scoped entry point
        setContent(facetId = 1L)

        // The facet-scoped state resolves asynchronously (Room query behind the ViewModel's
        // StateFlow combine) — wait for the switch to actually render rather than assuming
        // setContent()'s own waitForIdle() already caught it (see chat history — this same race
        // hit other facet-scoped screens' equivalent tests earlier).
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("clock_style_override_row").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the inherit/override switch is present, defaulting to Inherit (controls disabled)
        composeRule.onNodeWithTag("clock_style_inherit_row").assertExists()
        composeRule.onNodeWithTag("clock_style_override_row").assertExists()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_date_style_row"))
        composeRule.onNodeWithTag("clock_date_style_row").assertIsNotEnabled()

        // When switching to Override
        composeRule.onNodeWithTag("clock_style_override_row").performClick()

        // Then the controls become live
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("clock_date_style_row").assertIsEnabled() }.isSuccess
        }
    }
}
