package com.facetlauncher.app.ui.home.stack

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClockStackScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clockRow = ClockStackRow(id = 1, kind = ClockStackItemKind.CLOCK, appWidgetId = null, providerName = null, canConfigure = true)
    private val gmailRow = ClockStackRow(id = 2, kind = ClockStackItemKind.WIDGET, appWidgetId = 7, providerName = "Gmail", canConfigure = true)
    private val plainRow = ClockStackRow(id = 3, kind = ClockStackItemKind.WIDGET, appWidgetId = 8, providerName = "Notes", canConfigure = false)

    private fun setContent(
        uiState: ClockStackUiState,
        onRemove: (Long) -> Unit = {},
        onConfigure: (ClockStackRow) -> Unit = {},
        onAdd: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                ClockStackContent(uiState = uiState, onBack = {}, onReorder = {}, onRemove = onRemove, onConfigure = onConfigure, onAdd = onAdd)
            }
        }
    }

    @Test
    fun rowsShowTheClockAndEachWidgetByItsProviderName() {
        setContent(ClockStackUiState(rows = listOf(clockRow, gmailRow, plainRow)))

        composeRule.onNode(hasTestTag("clock_stack_row_1") and hasAnyDescendant(hasText("Launcher clock")), useUnmergedTree = true).assertExists()
        composeRule.onNode(hasTestTag("clock_stack_row_2") and hasAnyDescendant(hasText("Gmail")), useUnmergedTree = true).assertExists()
        composeRule.onNode(hasTestTag("clock_stack_row_3") and hasAnyDescendant(hasText("Notes")), useUnmergedTree = true).assertExists()
    }

    @Test
    fun aWidgetWhoseAppIsGoneIsListedAsUnavailable() {
        val orphan = gmailRow.copy(providerName = null)
        setContent(ClockStackUiState(rows = listOf(orphan)))

        composeRule.onNode(hasTestTag("clock_stack_row_2") and hasAnyDescendant(hasText("Widget unavailable")), useUnmergedTree = true).assertExists()
    }

    @Test
    fun theCounterShowsThePagesAgainstThePlansLimit() {
        setContent(ClockStackUiState(rows = listOf(clockRow, gmailRow), isPro = true))

        composeRule.onNode(hasTestTag("clock_stack_counter") and hasText("2 of 5"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun aFreeCounterReadsAgainstOne() {
        setContent(ClockStackUiState(rows = listOf(clockRow), isPro = false))

        composeRule.onNode(hasTestTag("clock_stack_counter") and hasText("1 of 1"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun configureIsOnlyOnRowsThatHaveSomethingToConfigure() {
        setContent(ClockStackUiState(rows = listOf(clockRow, gmailRow, plainRow)))

        composeRule.onNodeWithTag("clock_stack_configure_1").assertExists()
        composeRule.onNodeWithTag("clock_stack_configure_2").assertExists()
        composeRule.onNodeWithTag("clock_stack_configure_3").assertDoesNotExist()
    }

    @Test
    fun tappingConfigureAndRemoveReportTheirRow() {
        var configured: ClockStackRow? = null
        var removed: Long? = null
        setContent(ClockStackUiState(rows = listOf(clockRow, gmailRow)), onRemove = { removed = it }, onConfigure = { configured = it })

        composeRule.onNodeWithTag("clock_stack_configure_2").performClick()
        composeRule.onNodeWithTag("clock_stack_remove_1").performClick()

        assertEquals(gmailRow, configured)
        assertEquals(1L, removed)
    }

    @Test
    fun anEmptyStackSaysTheClockAreaStaysBlankAndStillOffersAdd() {
        setContent(ClockStackUiState(rows = emptyList(), isPro = false))

        composeRule.onNode(hasTestTag("clock_stack_hint") and hasText("Nothing here, so the clock area on Home stays blank until you add something."), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("clock_stack_add_row").assertIsDisplayed()
    }

    @Test
    fun aFullFreeStackShowsAddWithAProPillAndTheHowToHint() {
        setContent(ClockStackUiState(rows = listOf(clockRow), isPro = false))

        composeRule.onNode(hasTestTag("clock_stack_add_row") and hasAnyDescendant(hasText("Pro")), useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("clock_stack_free_hint").assertExists()
    }

    @Test
    fun aProStackWithRoomShowsAddWithoutAPillOrTheFreeHint() {
        setContent(ClockStackUiState(rows = listOf(clockRow, gmailRow), isPro = true))

        composeRule.onNodeWithTag("clock_stack_add_row").assertIsDisplayed()
        composeRule.onNode(hasTestTag("clock_stack_add_row") and hasAnyDescendant(hasText("Pro")), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("clock_stack_free_hint").assertDoesNotExist()
    }

    @Test
    fun aFullProStackHidesAdd() {
        val full = List(5) { gmailRow.copy(id = (10 + it).toLong()) }
        setContent(ClockStackUiState(rows = full, isPro = true))

        composeRule.onNodeWithTag("clock_stack_add_row").assertDoesNotExist()
    }

    @Test
    fun tappingAddReportsIt() {
        var added = 0
        setContent(ClockStackUiState(rows = listOf(clockRow), isPro = true), onAdd = { added++ })

        composeRule.onNodeWithTag("clock_stack_add_row").performClick()

        assertEquals(1, added)
    }
}
