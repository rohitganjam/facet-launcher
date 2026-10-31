package com.facetlauncher.app.ui.home.stack

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.AddClockStackItemResult
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.RemoveClockStackItemUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ClockStackViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: FacetDatabase
    private lateinit var repository: ClockStackRepository
    private lateinit var appWidgetRepository: AppWidgetRepository
    private val entitlement = FakeEntitlementRepository(initial = true)
    private var facetId = 0L

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ClockStackRepository(database, database.clockStackItemDao())
        facetId = database.facetDao().insert(FacetEntity(name = "Home", position = 0))
        appWidgetRepository = mock(AppWidgetRepository::class.java)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun viewModel(): ClockStackViewModel {
        val settings = mock(SettingsRepository::class.java)
        `when`(settings.settings).thenReturn(flowOf(LauncherSettings(activeFacetId = facetId)))
        return ClockStackViewModel(
            settings, repository, appWidgetRepository,
            RemoveClockStackItemUseCase(repository, appWidgetRepository),
            AddClockStackItemUseCase(repository, entitlement),
            entitlement, dispatcher,
        )
    }

    /** Collects the state so the `WhileSubscribed` flow is live, then returns the current one. */
    private suspend fun ClockStackViewModel.current(): ClockStackUiState = uiState.first { it.facetId == facetId }

    @Test
    fun `the state lists the stack in order with each widget's provider name`() = runTest {
        // Given a clock and a widget whose provider is Gmail
        repository.addClock(facetId)
        repository.addWidget(facetId, 7)
        `when`(appWidgetRepository.getProviderLabel(7)).thenReturn("Gmail")

        // When the screen's state is read
        val state = viewModel().current()

        // Then the rows follow the stack and the widget carries its provider's name
        assertEquals(listOf(ClockStackItemKind.CLOCK, ClockStackItemKind.WIDGET), state.rows.map { it.kind })
        assertEquals("Gmail", state.rows[1].providerName)
        assertNull(state.rows[0].providerName)
        assertEquals(7, state.rows[1].appWidgetId)
    }

    @Test
    fun `a widget whose app is gone has no provider name`() = runTest {
        repository.addWidget(facetId, 7)
        `when`(appWidgetRepository.getProviderLabel(7)).thenReturn(null)

        assertNull(viewModel().current().rows.single().providerName)
    }

    @Test
    fun `the clock can always be configured and a widget only when its provider declares a configure screen`() = runTest {
        // Given the clock and two widgets, only one of which has a configure screen
        repository.addClock(facetId)
        repository.addWidget(facetId, 7)
        repository.addWidget(facetId, 8)
        val info = mock(android.appwidget.AppWidgetProviderInfo::class.java)
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(info)
        `when`(appWidgetRepository.createConfigureIntentSender(7, info)).thenReturn(mock(android.content.IntentSender::class.java))

        val rows = viewModel().current().rows

        // Then the clock and the configurable widget offer it, the other does not
        assertTrue(rows[0].canConfigure)
        assertTrue(rows[1].canConfigure)
        assertFalse(rows[2].canConfigure)
    }

    @Test
    fun `reordering saves the new order`() = runTest {
        // Given three widgets
        listOf(10, 11, 12).forEach { repository.addWidget(facetId, it) }
        val viewModel = viewModel()
        val ids = viewModel.current().rows.map { it.id }

        // When they are reordered (waiting for the state to show the new order)
        viewModel.reorder(listOf(ids[2], ids[0], ids[1]))
        val reordered = viewModel.uiState.first { state -> state.rows.map { it.appWidgetId } == listOf(12, 10, 11) }

        // Then the stack is stored in that order
        assertEquals(listOf(12, 10, 11), reordered.rows.map { it.appWidgetId })
        assertEquals(listOf(12, 10, 11), repository.getAll(facetId).map { it.appWidgetId })
    }

    @Test
    fun `removing a row releases its widget and may empty the stack`() = runTest {
        // Given a stack of one widget
        repository.addWidget(facetId, 7)
        val viewModel = viewModel()
        val id = viewModel.current().rows.single().id

        // When it is removed (the write runs on Room's executor, so wait for the state to show it gone)
        viewModel.remove(id)
        viewModel.uiState.first { it.facetId == facetId && it.rows.isEmpty() }

        // Then the widget id was released and nothing is left
        verify(appWidgetRepository).deleteAppWidgetId(7)
        assertTrue(repository.getAll(facetId).isEmpty())
    }

    @Test
    fun `hasRoom follows the plan`() = runTest {
        // Given a stack of one page
        repository.addClock(facetId)
        val viewModel = viewModel()

        // Then a Pro user has room, a free user does not
        assertTrue(viewModel.hasRoom())
        entitlement.proFlow.value = false
        assertFalse(viewModel.hasRoom())
    }

    @Test
    fun `adding the clock reports the limit, and a clock that is already there`() = runTest {
        // Given a free user with the clock already on the stack
        entitlement.proFlow.value = false
        repository.addClock(facetId)
        val viewModel = viewModel()

        // Then adding is turned away by the limit
        assertEquals(AddClockStackItemResult.LIMIT_REACHED, viewModel.addClock())

        // And a Pro user is told the clock is already there
        entitlement.proFlow.value = true
        assertEquals(AddClockStackItemResult.CLOCK_ALREADY_IN_STACK, viewModel.addClock())
    }

    @Test
    fun `the state follows Pro so a lapsed stack keeps its rows but loses its room`() = runTest {
        // Given a Pro stack of two pages
        repository.addClock(facetId)
        repository.addWidget(facetId, 7)
        val viewModel = viewModel()
        assertFalse(viewModel.current().addNeedsPro)

        // When Pro lapses
        entitlement.proFlow.value = false

        // Then both rows are still listed but adding needs Pro
        val state = viewModel.uiState.first { !it.isPro }
        assertEquals(2, state.rows.size)
        assertTrue(state.addNeedsPro)
    }
}
