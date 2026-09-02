package com.lumenlauncher.app.ui.hub.picker

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.data.widget.LauncherAppWidgetHost
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The emulator's own set of real, addable widget providers is environment-dependent (varies by
 * API level/system image, and this app defines none of its own), so this focuses on what's
 * reliably testable regardless of what's actually installed: the screen's own chrome and
 * navigation, not the content of specific provider groups.
 */
class HubWidgetPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onDone: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                )
                HubWidgetPickerViewModel(context, appWidgetRepository, widgetPlacementRepository, PlaceWidgetUseCase())
            }
            LumenLauncherTheme {
                HubWidgetPickerScreen(onDone = onDone, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun rendersTheHeaderAndSearchField() {
        // Given the picker
        setContent()

        // Then its chrome renders — title, remaining count, search field
        composeRule.onNodeWithText("Add widget").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_widget_picker_remaining").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_widget_picker_search").assertIsDisplayed()
    }

    @Test
    fun backButtonInvokesOnDone() {
        // Given the picker
        var doneInvoked = false
        setContent(onDone = { doneInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back
        assertEquals(true, doneInvoked)
    }

    @Test
    fun typingAQueryUpdatesTheSearchField() {
        // Given the picker
        setContent()

        // When typing a query that matches nothing real on this device
        composeRule.onNodeWithTag("hub_widget_picker_search").performTextInput("zzz_no_such_widget_zzz")

        // Then the field reflects it, and the empty-results copy renders rather than a crash
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("No widgets found").assertIsDisplayed() }.isSuccess
        }
    }

    @Test
    fun aFailedAddShowsAnErrorMessageInsteadOfSilentlyDoingNothing() {
        // Given the picker content rendered with a failure message set (as it would be right
        // after AddFailed — e.g. the provider's own configure activity was canceled/incomplete)
        composeRule.setContent {
            LumenLauncherTheme {
                HubWidgetPickerContent(
                    uiState = HubWidgetPickerUiState(),
                    failureMessage = "Setup wasn't finished, so that widget wasn't added",
                    onQueryChanged = {},
                    onProviderSelected = {},
                    onBack = {},
                )
            }
        }

        // Then the error is visible, not a silent no-op
        composeRule.onNodeWithTag("hub_widget_picker_error").assertIsDisplayed()
    }
}
