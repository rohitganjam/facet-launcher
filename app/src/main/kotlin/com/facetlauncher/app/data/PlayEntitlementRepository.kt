package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.data.di.EntitlementDataStore
import com.facetlauncher.app.data.model.ProQueryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pro from Google Play, remembered across launches. The last known answer is cached in the `facet_entitlement`
 * DataStore, so Pro works offline and at cold start before Play has answered. **Only an explicit answer from
 * Play changes it**: owned turns Pro on, "no purchase" (never bought, refunded, another Google account) turns it
 * off, and a pending purchase or Play being unreachable leaves it as it was. With nothing cached and no answer
 * yet, the user is Free, since nothing proves otherwise.
 */
@Singleton
class PlayEntitlementRepository @Inject constructor(
    private val billingRepository: BillingRepository,
    @EntitlementDataStore private val dataStore: DataStore<Preferences>,
    @ApplicationScope scope: CoroutineScope,
) : EntitlementRepository() {

    private val state = MutableStateFlow(false)
    private val loaded = MutableStateFlow(false)

    override val isPro: StateFlow<Boolean> = state

    init {
        scope.launch {
            state.value = dataStore.data.first()[PRO_KEY] ?: false
            loaded.value = true
            refresh()
        }
        scope.launch { billingRepository.updates.collect { apply(it) } }
    }

    override suspend fun awaitLoaded() {
        loaded.first { it }
    }

    override suspend fun refresh() {
        awaitLoaded()
        apply(billingRepository.queryPro())
    }

    private suspend fun apply(result: ProQueryResult) {
        when (result) {
            ProQueryResult.Owned -> set(true)
            ProQueryResult.NotOwned -> set(false)
            ProQueryResult.Pending, ProQueryResult.Unavailable -> Unit
        }
    }

    private suspend fun set(pro: Boolean) {
        if (state.value == pro) return
        // Persist first, then publish, so what the rest of the app sees is always what survives a restart.
        dataStore.edit { it[PRO_KEY] = pro }
        state.value = pro
    }

    private companion object {
        val PRO_KEY = booleanPreferencesKey("pro_purchased")
    }
}
