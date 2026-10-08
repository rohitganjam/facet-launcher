package com.facetlauncher.app.domain

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.FacetSwitchSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * One facet-automation pass: refresh the rules' state, then switch to the facet they want if it
 * differs from what is showing. The switch is an [FacetSwitchSource.AUTOMATION] one, so it never
 * touches the baseline or the suppressed rules.
 */
class ApplyFacetAutomationUseCase @Inject constructor(
    private val refreshState: RefreshAutomationStateUseCase,
    private val activateFacetById: ActivateFacetByIdUseCase,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        val decision = refreshState() ?: return
        // The user may have switched by hand while the rules were being sampled — never undo that.
        val stillShowing = settingsRepository.settings.first().activeFacetId == decision.currentFacetId
        if (stillShowing && decision.desiredFacetId != decision.currentFacetId) {
            activateFacetById(decision.desiredFacetId, FacetSwitchSource.AUTOMATION)
        }
    }
}
