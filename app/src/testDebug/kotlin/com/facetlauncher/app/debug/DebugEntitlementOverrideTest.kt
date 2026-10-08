package com.facetlauncher.app.debug

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DebugEntitlementOverrideTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("debug-${System.nanoTime()}.preferences_pb") })
    }

    @Test
    fun `a debug build follows play until told otherwise`() = runTest {
        val override = DebugEntitlementOverride(dataStore)

        assertEquals(DebugEntitlementMode.FOLLOW_PLAY, override.mode.first())
        assertNull(override.forced.first())
    }

    @Test
    fun `force pro and force free map to a forced value`() = runTest {
        val override = DebugEntitlementOverride(dataStore)

        override.setMode(DebugEntitlementMode.FORCE_PRO)
        assertEquals(true, override.forced.first())

        override.setMode(DebugEntitlementMode.FORCE_FREE)
        assertEquals(false, override.forced.first())

        override.setMode(DebugEntitlementMode.FOLLOW_PLAY)
        assertNull(override.forced.first())
    }

    @Test
    fun `the chosen mode survives a restart`() = runTest {
        DebugEntitlementOverride(dataStore).setMode(DebugEntitlementMode.FORCE_PRO)

        assertEquals(DebugEntitlementMode.FORCE_PRO, DebugEntitlementOverride(dataStore).mode.first())
    }
}
