package com.facetlauncher.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDao
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationState
import com.facetlauncher.app.data.model.FacetSwitchSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

/** In-memory fake — mirrors [com.facetlauncher.app.data.FacetRepositoryTest]'s. Named distinctly from
 *  [EnsureActiveFacetUseCaseTest]'s own private `FakeFacetDao` — top-level private classes are
 *  file-scoped for access but still occupy the package's class-file namespace, so two files in the
 *  same package can't both be named `FakeFacetDao`. */
private class ActivateFacetByIdFakeFacetDao : FacetDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetEntity>>(emptyList())

    override fun observeAll(): Flow<List<FacetEntity>> = state

    override suspend fun insert(facet: FacetEntity): Long {
        val id = if (facet.id != 0L) facet.id else nextId++
        state.value = state.value.filterNot { it.id == id } + facet.copy(id = id)
        return id
    }

    override suspend fun update(facet: FacetEntity) {
        state.value = state.value.map { if (it.id == facet.id) facet else it }
    }

    override suspend fun delete(facet: FacetEntity) {
        state.value = state.value.filterNot { it.id == facet.id }
    }

    override suspend fun getById(id: Long): FacetEntity? = state.value.find { it.id == id }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

class ActivateFacetByIdUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createSettingsRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return SettingsRepository(dataStore)
    }

    private fun activate(
        facetRepository: FacetRepository,
        settingsRepository: SettingsRepository,
        automationState: AutomationStateRepository,
        refresh: RefreshAutomationStateUseCase = mock(RefreshAutomationStateUseCase::class.java),
    ) = ActivateFacetByIdUseCase(facetRepository, settingsRepository, automationState, refresh)

    private fun createAutomationStateRepository(): AutomationStateRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("automation-${System.nanoTime()}.preferences_pb") },
        )
        return AutomationStateRepository(dataStore)
    }

    @Test
    fun `invoke activates a facet that exists`() = runTest {
        // Given a second facet that isn't currently active
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val first = facetRepository.addFacet()
        val second = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(first.id)

        // When activating it by id (e.g. via a deep link or shortcut)
        activate(facetRepository, settingsRepository, createAutomationStateRepository())(second.id)

        // Then it becomes the active facet
        assertEquals(second.id, settingsRepository.settings.first().activeFacetId)
    }

    @Test
    fun `a manual switch makes the facet the baseline and suppresses every active rule`() = runTest {
        // Given two rules the evaluator has marked active
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val automationState = createAutomationStateRepository()
        val first = facetRepository.addFacet()
        val second = facetRepository.addFacet()
        automationState.update { it.copy(baselineFacetId = first.id, activeRuleIds = listOf(10L, 11L)) }

        // When the user switches by id (the default source)
        activate(facetRepository, createSettingsRepository(), automationState)(second.id)

        // Then the choice becomes the baseline and both rules are overridden
        val state = automationState.get()
        assertEquals(second.id, state.baselineFacetId)
        assertEquals(setOf(10L, 11L), state.suppressedRuleIds)
    }

    @Test
    fun `a manual switch re-samples the rules before recording itself`() = runTest {
        // Given a facet to switch to
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val refresh = mock(RefreshAutomationStateUseCase::class.java)
        val facet = facetRepository.addFacet()

        // When switching by hand
        activate(facetRepository, createSettingsRepository(), createAutomationStateRepository(), refresh)(facet.id)

        // Then the rules were sampled, so one that became true since the last evaluation is suppressed too
        verify(refresh).invoke()
    }

    @Test
    fun `an automation switch does not re-sample the rules`() = runTest {
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val refresh = mock(RefreshAutomationStateUseCase::class.java)
        val facet = facetRepository.addFacet()

        activate(facetRepository, createSettingsRepository(), createAutomationStateRepository(), refresh)(facet.id, FacetSwitchSource.AUTOMATION)

        verifyNoInteractions(refresh)
    }

    @Test
    fun `a switch to a facet that does not exist does not re-sample the rules`() = runTest {
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val refresh = mock(RefreshAutomationStateUseCase::class.java)

        activate(facetRepository, createSettingsRepository(), createAutomationStateRepository(), refresh)(999L)

        verifyNoInteractions(refresh)
    }

    @Test
    fun `an automation switch changes the active facet but leaves the automation state alone`() = runTest {
        // Given an active rule and a baseline
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val automationState = createAutomationStateRepository()
        val first = facetRepository.addFacet()
        val second = facetRepository.addFacet()
        val before = AutomationState(baselineFacetId = first.id, activeRuleIds = listOf(10L))
        automationState.update { before }

        // When the evaluator switches facets
        activate(facetRepository, settingsRepository, automationState)(second.id, FacetSwitchSource.AUTOMATION)

        // Then the facet changes but the baseline and suppressed rules are untouched
        assertEquals(second.id, settingsRepository.settings.first().activeFacetId)
        assertEquals(before, automationState.get())
    }

    @Test
    fun `an automation switch to a deleted facet is a no-op`() = runTest {
        // Given a facet showing, and a rule that wants a facet which has since been deleted
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val automationState = createAutomationStateRepository()
        val facet = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(facet.id)
        val before = AutomationState(baselineFacetId = facet.id, activeRuleIds = listOf(10L))
        automationState.update { before }

        // When the evaluator asks to switch to the missing facet
        activate(facetRepository, settingsRepository, automationState)(facet.id + 999, FacetSwitchSource.AUTOMATION)

        // Then nothing changes and nothing throws
        assertEquals(facet.id, settingsRepository.settings.first().activeFacetId)
        assertEquals(before, automationState.get())
    }

    @Test
    fun `invoke is a no-op for an id that doesn't resolve to a real facet`() = runTest {
        // Given a facet that's currently active
        val facetRepository = FacetRepository(ActivateFacetByIdFakeFacetDao())
        val settingsRepository = createSettingsRepository()
        val facet = facetRepository.addFacet()
        settingsRepository.setActiveFacetId(facet.id)

        // When activating an id that doesn't exist (stale shortcut, hand-typed bad id, deleted facet)
        val automationState = createAutomationStateRepository()
        activate(facetRepository, settingsRepository, automationState)(facet.id + 999)

        // Then the currently-active facet and the automation state are left untouched
        assertEquals(facet.id, settingsRepository.settings.first().activeFacetId)
        assertNotEquals(facet.id + 999, settingsRepository.settings.first().activeFacetId)
        assertEquals(AutomationState(), automationState.get())
    }
}
