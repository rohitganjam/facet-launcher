package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.facetlauncher.app.data.di.AutomationDataStore
import com.facetlauncher.app.data.model.AutomationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists [AutomationState] in its own DataStore file; [update] is an atomic read-modify-write. */
@Singleton
class AutomationStateRepository @Inject constructor(
    @AutomationDataStore private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val BASELINE_FACET_ID = longPreferencesKey("baseline_facet_id")
        val ACTIVE_RULE_IDS = stringPreferencesKey("active_rule_ids")
        val SUPPRESSED_RULE_IDS = stringSetPreferencesKey("suppressed_rule_ids")
    }

    val state: Flow<AutomationState> = dataStore.data.map { it.toState() }

    suspend fun get(): AutomationState = state.first()

    suspend fun update(transform: (AutomationState) -> AutomationState) {
        dataStore.edit { preferences ->
            val next = transform(preferences.toState())
            next.baselineFacetId?.let { preferences[Keys.BASELINE_FACET_ID] = it } ?: preferences.remove(Keys.BASELINE_FACET_ID)
            preferences[Keys.ACTIVE_RULE_IDS] = next.activeRuleIds.joinToString(",")
            preferences[Keys.SUPPRESSED_RULE_IDS] = next.suppressedRuleIds.map(Long::toString).toSet()
        }
    }

    private fun Preferences.toState() = AutomationState(
        baselineFacetId = this[Keys.BASELINE_FACET_ID],
        activeRuleIds = this[Keys.ACTIVE_RULE_IDS].orEmpty().split(",").mapNotNull(String::toLongOrNull),
        suppressedRuleIds = this[Keys.SUPPRESSED_RULE_IDS].orEmpty().mapNotNull(String::toLongOrNull).toSet(),
    )
}
