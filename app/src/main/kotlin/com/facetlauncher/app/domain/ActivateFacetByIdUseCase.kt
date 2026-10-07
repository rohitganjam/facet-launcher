package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.FacetSwitchSource
import javax.inject.Inject

/**
 * Switches the active facet to [facetId] — the single choke point for switches. It serves deep
 * links (`facetlauncher://facet/{id}`) and shortcut intents (via [com.facetlauncher.app.LauncherActivity])
 * as well as the in-app carousel and Facet settings. Spans [SelectableFacetsUseCase] (existence and entitlement check),
 * [AutomationStateRepository] and [SettingsRepository], so per CLAUDE.md's layering rule this is a
 * use case, not the `Activity` or a `ViewModel`.
 *
 * A [FacetSwitchSource.MANUAL] switch (the default) makes [facetId] the automation baseline and
 * suppresses every rule active right now — rules are re-sampled first ([RefreshAutomationStateUseCase])
 * so one that became true since the last evaluation is covered too. Only the automation runner passes
 * [FacetSwitchSource.AUTOMATION], which changes the active facet and nothing else.
 *
 * A no-op when [facetId] isn't a facet the user may switch to ([SelectableFacetsUseCase]): a stale shortcut
 * for a since-deleted facet, a hand-typed bad id, a race with a delete, or a facet disabled on the free
 * plan must do nothing rather than crash or silently create a facet (mirrors [EnsureActiveFacetUseCase]'s own "don't create on a bad id"
 * reasoning, just without the fallback-creation step that only makes sense at startup).
 */
class ActivateFacetByIdUseCase @Inject constructor(
    private val selectableFacets: SelectableFacetsUseCase,
    private val settingsRepository: SettingsRepository,
    private val automationStateRepository: AutomationStateRepository,
    private val refreshAutomationState: RefreshAutomationStateUseCase,
) {
    suspend operator fun invoke(facetId: Long, source: FacetSwitchSource = FacetSwitchSource.MANUAL) {
        if (facetId !in selectableFacets()) return
        if (source == FacetSwitchSource.MANUAL) {
            refreshAutomationState()
            // State first: an evaluation between the two writes then sees the override, not a stale rule.
            automationStateRepository.update { it.afterManualSwitch(facetId) }
        }
        settingsRepository.setActiveFacetId(facetId)
    }
}
