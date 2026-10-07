package com.facetlauncher.app.ui.settings.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.AutomationRuleRepository
import com.facetlauncher.app.data.BluetoothRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WifiRepository
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.model.SaveRuleResult
import com.facetlauncher.app.data.model.requiredPermission
import com.facetlauncher.app.domain.CanUseTriggerUseCase
import com.facetlauncher.app.domain.SaveAutomationRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the device pickers offer: paired Bluetooth devices and the Wi-Fi networks in range. */
data class DeviceChoices(
    val bluetooth: List<PairedBluetoothDevice> = emptyList(),
    val networks: List<String> = emptyList(),
)

/** Asks the system for a runtime permission and reports whether it was granted. */
typealias PermissionRequest = (AutomationPermission, (granted: Boolean) -> Unit) -> Unit

/** The rule editor sheet: the draft rule, the permission gate on trigger changes, and the device pickers' choices. */
@HiltViewModel
class RuleEditorViewModel @Inject constructor(
    private val ruleRepository: AutomationRuleRepository,
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val bluetoothRepository: BluetoothRepository,
    private val wifiRepository: WifiRepository,
    private val permissionRepository: AutomationPermissionRepository,
    private val deviceStateRepository: DeviceStateRepository,
    private val entitlementRepository: EntitlementRepository,
    private val canUseTrigger: CanUseTriggerUseCase,
    private val saveAutomationRule: SaveAutomationRuleUseCase,
) : ViewModel() {

    private val _editor = MutableStateFlow<RuleEditorState?>(null)
    private val _choices = MutableStateFlow(DeviceChoices())

    /** The rule open in the editor sheet; null while it's closed. */
    val editor: StateFlow<RuleEditorState?> = _editor
    val choices: StateFlow<DeviceChoices> = _choices

    /** False for a free user: the trigger list shows Pro pills and locked types open the upgrade sheet. */
    val isPro: StateFlow<Boolean> = entitlementRepository.isPro

    fun openNewRule() {
        viewModelScope.launch {
            val facets = facetRepository.observeFacets().first()
            if (facets.isEmpty()) return@launch
            _editor.value = RuleEditorState.forNew(facets, settingsRepository.settings.first().activeFacetId)
        }
    }

    fun openRule(ruleId: Long) {
        viewModelScope.launch {
            val rule = ruleRepository.getById(ruleId) ?: return@launch
            _editor.value = RuleEditorState.from(rule, facetRepository.observeFacets().first())
            loadChoices(rule.trigger)
        }
    }

    fun editRule(transform: RuleEditorState.() -> RuleEditorState) = _editor.update { it?.edited(transform) }

    /**
     * Moves the draft to [candidate]. A Pro trigger for a free user is refused first (the upgrade sheet
     * opens and no permission is asked). Otherwise the trigger may need a permission. Granted: it takes effect.
     * Refused: the old trigger stays and the sheet explains. Asking is left to [request] (an Activity
     * result launcher), so the rest of the rule is never opened for a trigger that can't run.
     */
    fun changeTrigger(candidate: RuleEditorState, request: PermissionRequest) {
        val needed = candidate.trigger.requiredPermission()
        when {
            !canUseTrigger(candidate.trigger, isPro.value) -> _editor.update { it?.copy(proRequired = ProReason.TRIGGER) }
            needed == null || permissionRepository.isGranted(needed) -> applyTrigger(candidate)
            else -> request(needed) { granted -> onPermissionResult(needed, granted, candidate) }
        }
    }

    /** Whether the permission [trigger] needs is already granted, so a row can tell "Allow" from "open". */
    fun isGranted(permission: AutomationPermission): Boolean = permissionRepository.isGranted(permission)

    /** Records the answer and re-subscribes the device sources that were blocked by the permission. */
    fun onPermissionResult(permission: AutomationPermission, granted: Boolean, candidate: RuleEditorState? = null) {
        viewModelScope.launch {
            when (permission) {
                AutomationPermission.BLUETOOTH_CONNECT -> settingsRepository.setBluetoothPermissionRequested(true)
                AutomationPermission.LOCATION -> settingsRepository.setLocationPermissionRequested(true)
            }
        }
        deviceStateRepository.onPermissionsChanged()
        when {
            candidate == null -> Unit
            granted -> applyTrigger(candidate)
            else -> _editor.update { it?.copy(permissionDenied = permission) }
        }
    }

    fun closeEditor() {
        _editor.value = null
    }

    fun saveRule() {
        val editing = _editor.value ?: return
        viewModelScope.launch {
            when (val result = saveAutomationRule(editing.toRule(), isPro.value)) {
                is SaveRuleResult.Saved -> _editor.value = null
                is SaveRuleResult.Invalid -> _editor.update { it?.copy(errors = result.errors) }
                is SaveRuleResult.ProRequired -> _editor.update { it?.copy(proRequired = result.reason) }
                SaveRuleResult.FacetMissing -> _editor.update { it?.copy(facetMissing = true) }
            }
        }
    }

    fun deleteRule() {
        val editing = _editor.value ?: return
        viewModelScope.launch {
            ruleRepository.delete(editing.ruleId)
            _editor.value = null
        }
    }

    private fun applyTrigger(candidate: RuleEditorState) {
        _editor.value = candidate
        viewModelScope.launch { loadChoices(candidate.trigger) }
    }

    private suspend fun loadChoices(trigger: AutomationTrigger) {
        _choices.value = when (trigger) {
            is AutomationTrigger.Bluetooth -> DeviceChoices(bluetooth = bluetoothRepository.pairedDevices())
            is AutomationTrigger.Wifi -> DeviceChoices(networks = trigger.ssid?.let { wifiRepository.nearbyNetworkNames(it) }.orEmpty())
            else -> DeviceChoices()
        }
    }
}
