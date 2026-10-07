package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.model.AutomationState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AutomationStateRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(): AutomationStateRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") },
        )
        return AutomationStateRepository(dataStore)
    }

    @Test
    fun `a fresh repository holds the empty state`() = runTest {
        val repository = createRepository()

        assertEquals(AutomationState(), repository.state.first())
    }

    @Test
    fun `update round-trips the baseline, the ordered active rules and the suppressed rules`() = runTest {
        val repository = createRepository()
        val written = AutomationState(baselineFacetId = 3L, activeRuleIds = listOf(12L, 10L, 11L), suppressedRuleIds = setOf(10L, 12L))

        repository.update { written }

        assertEquals(written, repository.get())
    }

    @Test
    fun `update transforms the stored state, not a blank one`() = runTest {
        val repository = createRepository()
        repository.update { it.copy(baselineFacetId = 1L, activeRuleIds = listOf(10L)) }

        repository.update { it.afterManualSwitch(2L) }

        val state = repository.get()
        assertEquals(2L, state.baselineFacetId)
        assertEquals(listOf(10L), state.activeRuleIds)
        assertEquals(setOf(10L), state.suppressedRuleIds)
    }

    @Test
    fun `clearing the baseline removes it`() = runTest {
        val repository = createRepository()
        repository.update { it.copy(baselineFacetId = 5L) }

        repository.update { it.copy(baselineFacetId = null) }

        assertNull(repository.get().baselineFacetId)
    }
}
