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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.ClockTemplateId
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

    private fun setContent(onBack: () -> Unit = {}): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "clock-style-gallery-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                ClockStyleGalleryViewModel(settingsRepository)
            }
            LumenLauncherTheme {
                ClockStyleGalleryRoute(onBack = onBack, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return settingsRepository
    }

    @Test
    fun headerStaysVisibleAfterScrollingThroughTheTemplateList() {
        // Given the gallery, scrolled all the way to the last template card
        setContent()
        composeRule.onNodeWithTag("clock_style_gallery_list")
            .performScrollToNode(hasTestTag("clock_template_card_${ClockTemplateId.entries.last().name}"))

        // Then the pinned header (title + back button) is still on screen, not scrolled away
        composeRule.onNodeWithText("Clock style").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun tappingATemplateCardPersistsItAsTheChosenTemplate() {
        // Given the gallery, Light stack applied by default
        val settingsRepository = setContent()
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
        val settingsRepository = setContent()
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
        val settingsRepository = setContent()

        // When turning it on
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").performClick()

        // Then it persists
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().clockShowMeridiem }
        }
        composeRule.onNodeWithTag("clock_show_meridiem_toggle").assertIsOn()
    }
}
