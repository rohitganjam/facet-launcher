package com.facetlauncher.app.ui.facets

import com.facetlauncher.app.data.FakeClockStackItemDao
import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.RefreshAutomationStateUseCase
import com.facetlauncher.app.domain.SelectableFacetsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.AppListLimits

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FacetSettingsViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // --- FacetSettingsUiState: pure derived-state logic, no ViewModel/repositories involved ---

    @Test
    fun `isActive is true only when this facet is the active one`() {
        val active = FacetSettingsUiState(facet = Facet(id = 1L, name = "Work", position = 0), activeFacetId = 1L)
        val inactive = FacetSettingsUiState(facet = Facet(id = 2L, name = "Home", position = 0), activeFacetId = 1L)

        assertEquals(true, active.isActive)
        assertEquals(false, inactive.isActive)
    }

    @Test
    fun `clock template falls back to the global default when not overriding`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideClock = false, clockTemplateId = ClockTemplateId.RULE_MERIDIEM),
            settings = LauncherSettings(clockTemplateId = ClockTemplateId.LIGHT_STACK),
        )

        assertEquals(ClockTemplateId.LIGHT_STACK, state.clockTemplateId)
    }

    @Test
    fun `clock template uses this facet's own value when overriding`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideClock = true, clockTemplateId = ClockTemplateId.RULE_MERIDIEM),
            settings = LauncherSettings(clockTemplateId = ClockTemplateId.LIGHT_STACK),
        )

        assertEquals(ClockTemplateId.RULE_MERIDIEM, state.clockTemplateId)
    }

    @Test
    fun `app row position resolves via its own sentinel, independent of overrideApps`() {
        val notOverridingButExplicit = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideApps = false, appRowPosition = AppRowPosition.RIGHT),
            settings = LauncherSettings(appRowPosition = AppRowPosition.LEFT),
        )
        val overridingButDefaultSentinel = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideApps = true, appRowPosition = AppRowPosition.LAUNCHER_DEFAULT),
            settings = LauncherSettings(appRowPosition = AppRowPosition.LEFT),
        )

        // A facet's own explicit position wins even without overrideApps — it's a look field,
        // not gated by the apps-list content override.
        assertEquals(AppRowPosition.RIGHT, notOverridingButExplicit.appRowPosition)
        // The LAUNCHER_DEFAULT sentinel falls back to the global value even with overrideApps set.
        assertEquals(AppRowPosition.LEFT, overridingButDefaultSentinel.appRowPosition)
    }

    @Test
    fun `app row presentation resolves via its own sentinel, independent of overrideApps`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideApps = false, appRowPresentation = AppRowPresentation.TEXT_ONLY),
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.ICON_AND_TEXT),
        )

        assertEquals(AppRowPresentation.TEXT_ONLY, state.appRowPresentation)
    }

    @Test
    fun `app list layout, column alignment, grid columns and grid display resolve via their own sentinel, independent of overrideApps`() {
        val notOverridingButExplicit = FacetSettingsUiState(
            facet = Facet(
                id = 1L,
                name = "Work",
                position = 0,
                overrideApps = false,
                appListLayout = AppListLayout.GRID,
                appListColumnAlignment = AppListColumnAlignment.BOTH_RIGHT,
                appListGridColumns = AppListGridColumns.SIX,
                appListGridDisplayMode = AppListGridDisplayMode.TEXT,
            ),
            settings = LauncherSettings(appListLayout = AppListLayout.SINGLE_COLUMN, appListColumnAlignment = AppListColumnAlignment.BOTH_LEFT, appListGridColumns = AppListGridColumns.FOUR, appListGridDisplayMode = AppListGridDisplayMode.ICONS),
        )
        val overridingButDefaultSentinel = FacetSettingsUiState(
            facet = Facet(
                id = 1L,
                name = "Work",
                position = 0,
                overrideApps = true,
                appListLayout = AppListLayout.LAUNCHER_DEFAULT,
                appListColumnAlignment = AppListColumnAlignment.LAUNCHER_DEFAULT,
                appListGridColumns = AppListGridColumns.LAUNCHER_DEFAULT,
                appListGridDisplayMode = AppListGridDisplayMode.LAUNCHER_DEFAULT,
            ),
            settings = LauncherSettings(appListLayout = AppListLayout.SINGLE_COLUMN, appListColumnAlignment = AppListColumnAlignment.BOTH_LEFT, appListGridColumns = AppListGridColumns.FOUR, appListGridDisplayMode = AppListGridDisplayMode.ICONS),
        )

        // A facet's own explicit values win even without overrideApps — look fields, not gated by
        // the apps-list content override.
        assertEquals(AppListLayout.GRID, notOverridingButExplicit.appListLayout)
        assertEquals(AppListColumnAlignment.BOTH_RIGHT, notOverridingButExplicit.appListColumnAlignment)
        assertEquals(AppListGridColumns.SIX, notOverridingButExplicit.appListGridColumns)
        assertEquals(AppListGridDisplayMode.TEXT, notOverridingButExplicit.appListGridDisplayMode)
        // The LAUNCHER_DEFAULT sentinel falls back to the global value even with overrideApps set.
        assertEquals(AppListLayout.SINGLE_COLUMN, overridingButDefaultSentinel.appListLayout)
        assertEquals(AppListColumnAlignment.BOTH_LEFT, overridingButDefaultSentinel.appListColumnAlignment)
        assertEquals(AppListGridColumns.FOUR, overridingButDefaultSentinel.appListGridColumns)
        assertEquals(AppListGridDisplayMode.ICONS, overridingButDefaultSentinel.appListGridDisplayMode)
    }

    @Test
    fun `list content mode and apps-to-show count fall back to global defaults when not overriding apps`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideApps = false, listContentMode = ListContentMode.RECENTS, appsToShowCount = 8),
            settings = LauncherSettings(listContentMode = ListContentMode.FAVORITES, appsToShowCount = 5),
        )

        assertEquals(ListContentMode.FAVORITES, state.listContentMode)
        assertEquals(5, state.appsToShowCount)
    }

    @Test
    fun `list content mode and apps-to-show count use this facet's own values when overriding apps`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS, appsToShowCount = 8),
            settings = LauncherSettings(listContentMode = ListContentMode.FAVORITES, appsToShowCount = 5),
        )

        assertEquals(ListContentMode.RECENTS, state.listContentMode)
        assertEquals(8, state.appsToShowCount)
    }

    @Test
    fun `effective favorites and their label reflect the global default while not overriding favorites`() {
        val defaultFavorites = (1..2).map { PlacedItem.SingleApp(appInfo(it)) }
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overridingFavorites = false),
            favorites = (1..4).map { PlacedItem.SingleApp(appInfo(it)) },
            defaultFavorites = defaultFavorites,
        )

        assertEquals(defaultFavorites, state.effectiveFavorites)
        assertEquals("2 of ${AppListLimits.MAX_FAVORITES}", state.favoritesLabel)
    }

    @Test
    fun `effective favorites and their label reflect this facet's own list while overriding favorites`() {
        val ownFavorites = (1..4).map { PlacedItem.SingleApp(appInfo(it)) }
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overridingFavorites = true),
            favorites = ownFavorites,
            defaultFavorites = (1..2).map { PlacedItem.SingleApp(appInfo(it)) },
        )

        assertEquals(ownFavorites, state.effectiveFavorites)
        assertEquals("4 of ${AppListLimits.MAX_FAVORITES}", state.favoritesLabel)
    }

    @Test
    fun `effective dock apps and their label reflect the global default while not overriding the dock`() {
        val defaultDockApps = (1..3).map { PlacedItem.SingleApp(appInfo(it)) }
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideDock = false),
            dockApps = (1..1).map { PlacedItem.SingleApp(appInfo(it)) },
            defaultDockApps = defaultDockApps,
        )

        assertEquals(defaultDockApps, state.effectiveDockApps)
        assertEquals("3 of ${AppListLimits.MAX_DOCK_APPS}", state.dockLabel)
    }

    @Test
    fun `effective dock apps and their label reflect this facet's own list while overriding the dock`() {
        val ownDockApps = (1..1).map { PlacedItem.SingleApp(appInfo(it)) }
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideDock = true),
            dockApps = ownDockApps,
            defaultDockApps = (1..3).map { PlacedItem.SingleApp(appInfo(it)) },
        )

        assertEquals(ownDockApps, state.effectiveDockApps)
        assertEquals("1 of ${AppListLimits.MAX_DOCK_APPS}", state.dockLabel)
    }

    @Test
    fun `dock display mode resolves via its own sentinel, independent of overrideDock`() {
        val state = FacetSettingsUiState(
            facet = Facet(id = 1L, name = "Work", position = 0, overrideDock = false, dockDisplayMode = DockDisplayMode.TEXT),
            settings = LauncherSettings(dockDisplayMode = DockDisplayMode.ICONS),
        )

        assertEquals(DockDisplayMode.TEXT, state.dockDisplayMode)
    }

    // --- FacetSettingsViewModel: real behavior (applyFacet / renameFacet), against a real FacetRepository ---

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private fun createViewModel(
        facetId: Long,
        facetRepository: FacetRepository,
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings()))
        },
        observePlacedItems: ObservePlacedItemsUseCase = mock(ObservePlacedItemsUseCase::class.java).also {
            listOf(
                PlacementTarget(PlacementList.FAVORITES, facetId),
                PlacementTarget.GlobalFavorites,
                PlacementTarget(PlacementList.DOCK, facetId),
                PlacementTarget.GlobalDock,
            ).forEach { target -> `when`(it.items(target)).thenReturn(flowOf(emptyList())) }
        },
        automationStateRepository: AutomationStateRepository = mock(AutomationStateRepository::class.java),
    ) = FacetSettingsViewModel(
        SavedStateHandle(mapOf("facetId" to facetId)),
        facetRepository,
        settingsRepository,
        observePlacedItems,
        ActivateFacetByIdUseCase(SelectableFacetsUseCase(facetRepository, FakeEntitlementRepository()), settingsRepository, automationStateRepository, mock(RefreshAutomationStateUseCase::class.java)),
    )

    /** In-memory fake — enough of [com.facetlauncher.app.data.local.FacetDao] for [FacetRepository]'s needs. */
    private class FakeFacetDao : com.facetlauncher.app.data.local.FacetDao {
        private var nextId = 1L
        private val state = MutableStateFlow<List<FacetEntity>>(emptyList())

        override fun observeAll() = state
        override suspend fun insert(facet: FacetEntity): Long {
            val id = if (facet.id != 0L) facet.id else nextId++
            state.value = (state.value.filterNot { it.id == id } + facet.copy(id = id)).sortedBy { it.position }
            return id
        }
        override suspend fun update(facet: FacetEntity) {
            state.value = state.value.map { if (it.id == facet.id) facet else it }.sortedBy { it.position }
        }
        override suspend fun delete(facet: FacetEntity) {
            state.value = state.value.filterNot { it.id == facet.id }
        }
        override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }
        override suspend fun deleteAll() { state.value = emptyList() }
    }

    @Test
    fun `uiState resolves the facet matching facetId out of every observed facet`() = runTest {
        val facetRepository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        facetRepository.addFacet()
        val second = facetRepository.addFacet()
        val viewModel = createViewModel(facetId = second.id, facetRepository = facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(second.id, viewModel.uiState.value.facet?.id)
    }

    @Test
    fun `applyFacet activates this facet when it isn't already active`() = runTest {
        val facetRepository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = facetRepository.addFacet()
        val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = 0L)))
        }
        val viewModel = createViewModel(facetId = facet.id, facetRepository = facetRepository, settingsRepository = settingsRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.applyFacet()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setActiveFacetId(facet.id)
    }

    @Test
    fun `applyFacet records a manual switch in the automation state`() = runTest {
        val facetRepository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = facetRepository.addFacet()
        val automationState = AutomationStateRepository(
            androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
                // Same dispatcher as the ViewModel's Main, so advanceUntilIdle() also finishes DataStore's IO.
                scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
                produceFile = { tempFolder.newFile("automation-${System.nanoTime()}.preferences_pb") },
            ),
        )
        automationState.update { it.copy(activeRuleIds = listOf(10L)) }
        val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = 0L)))
        }
        val viewModel = createViewModel(
            facetId = facet.id,
            facetRepository = facetRepository,
            settingsRepository = settingsRepository,
            automationStateRepository = automationState,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.applyFacet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(facet.id, automationState.get().baselineFacetId)
        assertEquals(setOf(10L), automationState.get().suppressedRuleIds)
    }

    @Test
    fun `applyFacet is a no-op when this facet is already active`() = runTest {
        val facetRepository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = facetRepository.addFacet()
        val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings(activeFacetId = facet.id)))
        }
        val viewModel = createViewModel(facetId = facet.id, facetRepository = facetRepository, settingsRepository = settingsRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.applyFacet()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository, org.mockito.Mockito.never()).setActiveFacetId(anyLong())
    }

    @Test
    fun `renameFacet persists the new name via the repository`() = runTest {
        val facetRepository = FacetRepository(FakeFacetDao(), FakeClockStackItemDao())
        val facet = facetRepository.addFacet()
        val viewModel = createViewModel(facetId = facet.id, facetRepository = facetRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.renameFacet("Renamed")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Renamed", viewModel.uiState.value.facet?.name)
    }
}
