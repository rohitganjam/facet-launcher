package com.facetlauncher.app.debug

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.facetlauncher.app.data.EntitlementOverride
import com.facetlauncher.app.data.di.EntitlementDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** What a debug build treats the entitlement as. */
enum class DebugEntitlementMode(val label: String) {
    FOLLOW_PLAY("Follow Google Play"),
    FORCE_PRO("Force Pro"),
    FORCE_FREE("Force Free"),
}

/** Lets a debug build force Pro or Free (it can't buy anything: it isn't installed from Play). Persisted across launches. */
@Singleton
class DebugEntitlementOverride @Inject constructor(
    @EntitlementDataStore private val dataStore: DataStore<Preferences>,
) : EntitlementOverride {

    val mode: Flow<DebugEntitlementMode> = dataStore.data
        .map { prefs -> prefs[MODE_KEY]?.let { name -> DebugEntitlementMode.entries.firstOrNull { it.name == name } } ?: DebugEntitlementMode.FOLLOW_PLAY }
        .distinctUntilChanged()

    override val forced: Flow<Boolean?> = mode.map { mode ->
        when (mode) {
            DebugEntitlementMode.FOLLOW_PLAY -> null
            DebugEntitlementMode.FORCE_PRO -> true
            DebugEntitlementMode.FORCE_FREE -> false
        }
    }

    suspend fun setMode(mode: DebugEntitlementMode) {
        dataStore.edit { it[MODE_KEY] = mode.name }
    }

    private companion object {
        val MODE_KEY = stringPreferencesKey("debug_entitlement_mode")
    }
}
