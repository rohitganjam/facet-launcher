package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WakeEventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

/**
 * Runs [ApplyFacetAutomationUseCase] whenever Home is about to be seen or the rules' inputs changed,
 * for as long as it is collected — launched once, for the app's lifetime, from
 * [com.facetlauncher.app.ui.launcher.LauncherViewModel], like [SyncFacetShortcutsUseCase].
 *
 * Triggers: screen on / unlock / clock change, a Home press ([homePresses]), any rule change, and a
 * change of the active facet (which also covers the first launch, once a facet exists). One sequential
 * collector, with bursts coalesced, so passes never overlap and no lock is needed. There are no
 * alarms: a schedule is applied when the user looks at the phone, never underneath them.
 */
class RunFacetAutomationUseCase @Inject constructor(
    private val applyFacetAutomation: ApplyFacetAutomationUseCase,
    private val ruleRepository: AutomationRuleRepository,
    private val wakeEventsRepository: WakeEventsRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(homePresses: Flow<Unit>) {
        merge(
            wakeEventsRepository.observeWakeEvents(),
            homePresses,
            ruleRepository.observeRules().map { },
            settingsRepository.settings.map { it.activeFacetId }.distinctUntilChanged().map { },
        )
            .conflate()
            .collect { applyFacetAutomation() }
    }
}
