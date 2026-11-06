package com.facetlauncher.app.ui.home.stack

import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.RemoveClockStackItemUseCase
import com.facetlauncher.app.ui.FakeEntitlementRepository
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * The stack screen's Add action over a real [ClockStackViewModel] and an in-memory database: the upsell at the
 * plan's limit, and the chooser when the launcher clock is not in the stack. The widget picker it leads to is
 * Hilt-injected, so it is covered by `ClockWidgetPickerViewModelTest` instead.
 */
class ClockStackScreenAddFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
    private val settings = SettingsRepository(
        PreferenceDataStoreFactory.create(produceFile = { File(context.cacheDir, "test-stack-${System.nanoTime()}.preferences_pb") }),
    )
    private val facets = FacetRepository(database.facetDao(), database.clockStackItemDao())
    private val stack = ClockStackRepository(database, database.clockStackItemDao())
    private val opened = mutableListOf<ProReason>()
    private val styleFacets = mutableListOf<Long>()
    private var shownFacetId = 0L

    /** A facet with its launcher clock, active; [withClock] false removes the clock so the stack is empty. */
    private fun show(isPro: Boolean, withClock: Boolean) {
        val entitlement = FakeEntitlementRepository(initial = isPro)
        val facetId = runBlocking {
            val id = facets.addFacet().id
            settings.setActiveFacetId(id)
            if (!withClock) stack.getAll(id).forEach { stack.remove(it.id) }
            id
        }
        shownFacetId = facetId
        val appWidgets = mock(AppWidgetRepository::class.java)
        val viewModel = ClockStackViewModel(
            SavedStateHandle(), settings, stack, appWidgets, RemoveClockStackItemUseCase(stack, appWidgets), AddClockStackItemUseCase(stack, entitlement), entitlement, Dispatchers.IO,
        )
        composeRule.setContent {
            FacetLauncherTheme {
                ClockStackScreen(onBack = {}, onOpenFacetPro = { opened += it }, onOpenClockStyle = { styleFacets += it }, viewModel = viewModel)
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { stack.getAll(facetId).size } == (if (withClock) 1 else 0) &&
                composeRule.onAllNodesWithTagExists("clock_stack_add_row")
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTagExists(tag: String) =
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `configuring the launcher clock offers clock styles and calendar settings and styles opens that screen`() {
        // Given a Pro facet whose stack holds the launcher clock
        show(isPro = true, withClock = true)
        val clockId = runBlocking { stack.getAll(shownFacetId).single().id }

        // When the clock row's Configure is tapped
        composeRule.onNodeWithTag("clock_stack_configure_$clockId").performClick()

        // Then a sheet offers both settings and nothing has navigated yet
        composeRule.onNodeWithTag("clock_stack_clock_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_style_sheet_calendar").assertIsDisplayed()
        assertEquals(emptyList<Long>(), styleFacets)

        // When Clock styles is chosen
        composeRule.onNodeWithTag("clock_style_sheet_styles").performClick()

        // Then the style gallery opens for that facet and the sheet is gone
        composeRule.waitForIdle()
        assertEquals(listOf(shownFacetId), styleFacets)
        composeRule.onAllNodesWithTag("clock_stack_clock_sheet").assertCountEquals(0)
    }

    @Test
    fun `a free stack at its limit sends Add to the Facet Pro screen`() {
        // Given a Free facet whose one page is the launcher clock
        show(isPro = false, withClock = true)

        // When Add is tapped
        composeRule.onNodeWithTag("clock_stack_add_row").performClick()
        composeRule.waitForIdle()

        // Then the Facet Pro screen opens for the widget stack
        composeRule.waitUntil(timeoutMillis = 5_000) { opened.isNotEmpty() }
        assertEquals(listOf(ProReason.WIDGET_STACK), opened)
    }

    @Test
    fun `a Pro stack without the clock offers the chooser and the launcher clock adds a clock page`() {
        // Given a Pro facet whose stack is empty
        show(isPro = true, withClock = false)

        // When Add is tapped
        composeRule.onNodeWithTag("clock_stack_add_row").performClick()

        // Then the chooser lists the launcher clock and a custom widget
        composeRule.onNodeWithTag("clock_stack_chooser").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_stack_chooser_widget").assertIsDisplayed()

        // When the launcher clock is chosen
        composeRule.onNodeWithTag("clock_stack_chooser_clock").performClick()

        // Then a clock page is in the stack and no upsell opened
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { stack.getAll(runBlocking { settings.settings.first().activeFacetId }).any { it.kind == ClockStackItemKind.CLOCK } }
        }
        assertEquals(emptyList<ProReason>(), opened)
    }

}
