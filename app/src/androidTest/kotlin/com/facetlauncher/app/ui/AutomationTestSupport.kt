package com.facetlauncher.app.ui

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AutomationStateRepository
import java.io.File

/** A throwaway [AutomationStateRepository] for tests that hand-build ViewModels needing `ActivateFacetByIdUseCase`. */
fun testAutomationStateRepository(context: Context) = AutomationStateRepository(
    PreferenceDataStoreFactory.create(
        produceFile = { File(context.cacheDir, "automation-test-${System.nanoTime()}.preferences_pb") },
    ),
)
