package com.facetlauncher.app.ui

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WakeEventsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.ApplyFacetAutomationUseCase
import com.facetlauncher.app.domain.EvaluateFacetAutomationUseCase
import com.facetlauncher.app.domain.RefreshAutomationStateUseCase
import com.facetlauncher.app.domain.RunFacetAutomationUseCase
import java.io.File
import java.time.Clock

/** A throwaway [AutomationStateRepository] for tests that hand-build ViewModels needing `ActivateFacetByIdUseCase`. */
fun testAutomationStateRepository(context: Context) = AutomationStateRepository(
    PreferenceDataStoreFactory.create(
        produceFile = { File(context.cacheDir, "automation-test-${System.nanoTime()}.preferences_pb") },
    ),
)

/** The automation use cases a hand-built ViewModel needs, wired over the test's own database and settings. */
class TestAutomation(val activate: ActivateFacetByIdUseCase, val run: RunFacetAutomationUseCase)

fun testAutomation(
    context: Context,
    database: FacetDatabase,
    facetRepository: FacetRepository,
    settingsRepository: SettingsRepository,
): TestAutomation {
    val rules = AutomationRuleRepository(database.automationRuleDao(), database.facetDao())
    val state = testAutomationStateRepository(context)
    val refresh = RefreshAutomationStateUseCase(rules, state, facetRepository, settingsRepository, EvaluateFacetAutomationUseCase(), Clock.systemDefaultZone())
    val activate = ActivateFacetByIdUseCase(facetRepository, settingsRepository, state, refresh)
    val run = RunFacetAutomationUseCase(
        ApplyFacetAutomationUseCase(refresh, activate, settingsRepository),
        rules,
        WakeEventsRepository(context),
        settingsRepository,
    )
    return TestAutomation(activate, run)
}
