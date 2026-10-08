package com.facetlauncher.app.ui.settings.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.ObserveFacetAutomationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FacetAutomationViewModel @Inject constructor(
    observeFacetAutomation: ObserveFacetAutomationUseCase,
    private val ruleRepository: AutomationRuleRepository,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    /** Null until the first emission, so the screen doesn't flash its empty state while loading. */
    val uiState: StateFlow<FacetAutomationScreenState?> = observeFacetAutomation(refreshTick)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Re-reads the permission grants, which have no change callback; called on every resume. */
    fun refresh() {
        refreshTick.value++
    }

    fun setEnabled(ruleId: Long, enabled: Boolean) {
        viewModelScope.launch { ruleRepository.setEnabled(ruleId, enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
