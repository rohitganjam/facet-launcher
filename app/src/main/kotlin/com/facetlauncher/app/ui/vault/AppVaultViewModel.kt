package com.facetlauncher.app.ui.vault

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.AppVaultRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.PinCheck
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.data.model.VAULT_PIN_LENGTH
import com.facetlauncher.app.data.model.vaultKey
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.MoveToVaultUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.domain.matchesSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which page of the vault is showing. [LOCKED] is the closed vault: nothing of it is shown. */
enum class VaultStage { LOCKED, INTRO, SET_PIN, CONFIRM_PIN, UNLOCK, LIST, PICKER }

/** What the PIN pad says about the last PIN typed. */
enum class PinFeedback { NONE, MISMATCH, WRONG, LOCKED_OUT }

/** One-off things the screen reacts to. */
sealed interface AppVaultUiEvent {
    data class AppRemoved(val app: AppInfo) : AppVaultUiEvent

    /** The vault was turned off: leave it. */
    data object Close : AppVaultUiEvent
}

@Immutable
data class AppVaultUiState(
    val stage: VaultStage = VaultStage.LOCKED,
    /** How many digits of the PIN are typed (the digits themselves never leave the view model). */
    val digitsEntered: Int = 0,
    val feedback: PinFeedback = PinFeedback.NONE,
    val attemptsLeft: Int = 0,
    val lockoutSecondsLeft: Int = 0,
    /** Setting a new PIN from inside the vault, so Back returns to the list. */
    val changingPin: Boolean = false,
    /** The vaulted apps, A to Z. */
    val apps: ImmutableList<AppInfo> = persistentListOf(),
    val pickerQuery: String = "",
    val pickerResults: ImmutableList<AppInfo> = persistentListOf(),
    val pickerSelected: ImmutableSet<String> = persistentSetOf(),
    val pickerSortOption: AppSortOption = AppSortOption.ALPHABETICAL,
    val pickerSortDirection: SortDirection = SortDirection.ASCENDING,
    /** Apps ticked in the picker that are on Home or in a folder, while their removal waits for a yes; `null` when not asking. */
    val confirmPlacedCount: Int? = null,
    val confirmTurnOff: Boolean = false,
    val confirmReset: Boolean = false,
)

private data class Core(
    val stage: VaultStage = VaultStage.LOCKED,
    val digits: Int = 0,
    val feedback: PinFeedback = PinFeedback.NONE,
    val attemptsLeft: Int = 0,
    val lockoutSecondsLeft: Int = 0,
    val changingPin: Boolean = false,
    val pickerQuery: String = "",
    val pickerSelected: Set<String> = emptySet(),
    val sortOption: AppSortOption = AppSortOption.ALPHABETICAL,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val confirmPlacedCount: Int? = null,
    val confirmTurnOff: Boolean = false,
    val confirmReset: Boolean = false,
)

/** The App Vault: PIN entry, the hidden apps, and adding to them. The PIN's digits are held here only while typed. */
@HiltViewModel
class AppVaultViewModel @Inject constructor(
    private val vaultRepository: AppVaultRepository,
    private val appRepository: AppRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val moveToVault: MoveToVaultUseCase,
    private val sortAppsForPicker: SortAppsForPickerUseCase,
) : ViewModel() {

    private val core = MutableStateFlow(Core())
    private val entered = StringBuilder()
    private var firstPin: String? = null
    private var busy = false
    private var lockoutJob: Job? = null

    private val _events = MutableSharedFlow<AppVaultUiEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<AppVaultUiEvent> = _events.asSharedFlow()

    private val vaultedApps = appRepository.observeVaultedApps()
    private val candidates = getInstalledApps.observe()

    val uiState: StateFlow<AppVaultUiState> = combine(core, vaultedApps, candidates) { core, vaulted, candidates ->
        val results = if (core.stage == VaultStage.PICKER) {
            sortAppsForPicker(candidates.filter { matchesSearch(it.label, core.pickerQuery) }, core.sortOption, core.sortDirection)
        } else {
            emptyList()
        }
        AppVaultUiState(
            stage = core.stage,
            digitsEntered = core.digits,
            feedback = core.feedback,
            attemptsLeft = core.attemptsLeft,
            lockoutSecondsLeft = core.lockoutSecondsLeft,
            changingPin = core.changingPin,
            apps = vaulted.sortedBy { it.label.lowercase() }.toImmutableList(),
            pickerQuery = core.pickerQuery,
            pickerResults = results.toImmutableList(),
            pickerSelected = core.pickerSelected.toImmutableSet(),
            pickerSortOption = core.sortOption,
            pickerSortDirection = core.sortDirection,
            confirmPlacedCount = core.confirmPlacedCount,
            confirmTurnOff = core.confirmTurnOff,
            confirmReset = core.confirmReset,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppVaultUiState())

    /** The vault was opened: asks for a PIN, or explains the vault first when there is none yet. */
    fun onOpened() {
        viewModelScope.launch {
            clearEntry()
            firstPin = null
            if (vaultRepository.hasPinNow()) {
                core.value = Core(stage = VaultStage.UNLOCK)
                startLockoutCountdown()
            } else {
                core.value = Core(stage = VaultStage.INTRO)
            }
        }
    }

    /** The vault closed or the launcher went to the background: everything is forgotten and it must be unlocked again. */
    fun lock() {
        lockoutJob?.cancel()
        clearEntry()
        firstPin = null
        busy = false
        core.value = Core()
    }

    fun onStartSetPin() {
        clearEntry()
        core.update { it.copy(stage = VaultStage.SET_PIN, feedback = PinFeedback.NONE) }
    }

    fun onDigit(digit: Int) {
        val state = core.value
        if (busy || state.stage !in PIN_STAGES || state.feedback == PinFeedback.LOCKED_OUT || entered.length >= VAULT_PIN_LENGTH) return
        entered.append(digit)
        core.update { it.copy(digits = entered.length, feedback = PinFeedback.NONE) }
        if (entered.length == VAULT_PIN_LENGTH) submit()
    }

    fun onBackspace() {
        if (busy || entered.isEmpty()) return
        entered.deleteCharAt(entered.length - 1)
        core.update { it.copy(digits = entered.length) }
    }

    private fun submit() {
        val pin = entered.toString()
        val stage = core.value.stage
        busy = true
        viewModelScope.launch {
            try {
                when (stage) {
                    VaultStage.SET_PIN -> {
                        firstPin = pin
                        clearEntry()
                        core.update { it.copy(stage = VaultStage.CONFIRM_PIN, feedback = PinFeedback.NONE) }
                    }
                    VaultStage.CONFIRM_PIN -> confirmNewPin(pin)
                    VaultStage.UNLOCK -> checkPin(pin)
                    else -> clearEntry()
                }
            } finally {
                busy = false
            }
        }
    }

    private suspend fun confirmNewPin(pin: String) {
        if (pin == firstPin) {
            vaultRepository.setPin(pin)
            firstPin = null
            clearEntry()
            core.update { it.copy(stage = VaultStage.LIST, feedback = PinFeedback.NONE, changingPin = false) }
        } else {
            firstPin = null
            clearEntry()
            core.update { it.copy(stage = VaultStage.SET_PIN, feedback = PinFeedback.MISMATCH) }
        }
    }

    private suspend fun checkPin(pin: String) {
        clearEntry()
        when (val check = vaultRepository.verifyPin(pin)) {
            PinCheck.Success -> core.update { it.copy(stage = VaultStage.LIST, feedback = PinFeedback.NONE) }
            is PinCheck.Wrong -> core.update { it.copy(feedback = PinFeedback.WRONG, attemptsLeft = check.attemptsLeft) }
            is PinCheck.LockedOut -> startLockoutCountdown(check.remainingMillis)
        }
    }

    private fun startLockoutCountdown(knownMillis: Long? = null) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            var remaining = knownMillis ?: vaultRepository.lockoutRemainingMillis()
            if (remaining <= 0L) return@launch
            while (remaining > 0L) {
                core.update { it.copy(feedback = PinFeedback.LOCKED_OUT, lockoutSecondsLeft = ((remaining + 999) / 1000).toInt()) }
                delay(COUNTDOWN_TICK_MS)
                remaining -= COUNTDOWN_TICK_MS
            }
            core.update { it.copy(feedback = PinFeedback.NONE, lockoutSecondsLeft = 0) }
        }
    }

    /** Back inside the vault: returns `true` when it went one step back within it, `false` when the vault should close. */
    fun onBack(): Boolean {
        val state = core.value
        return when {
            state.confirmPlacedCount != null || state.confirmTurnOff || state.confirmReset -> {
                core.update { it.copy(confirmPlacedCount = null, confirmTurnOff = false, confirmReset = false) }
                true
            }
            state.stage == VaultStage.PICKER -> {
                core.update { it.copy(stage = VaultStage.LIST, pickerQuery = "", pickerSelected = emptySet()) }
                true
            }
            state.stage == VaultStage.SET_PIN && state.changingPin -> {
                clearEntry()
                core.update { it.copy(stage = VaultStage.LIST, changingPin = false, feedback = PinFeedback.NONE) }
                true
            }
            state.stage == VaultStage.CONFIRM_PIN -> {
                firstPin = null
                clearEntry()
                core.update { it.copy(stage = VaultStage.SET_PIN, feedback = PinFeedback.NONE) }
                true
            }
            else -> false
        }
    }

    // ---- Forgot PIN ----

    /** The phone's own screen lock was confirmed: the apps stay hidden and a new PIN is chosen. */
    fun onDeviceCredentialConfirmed() {
        viewModelScope.launch {
            vaultRepository.clearPin()
            lockoutJob?.cancel()
            clearEntry()
            firstPin = null
            core.update { it.copy(stage = VaultStage.SET_PIN, feedback = PinFeedback.NONE, lockoutSecondsLeft = 0) }
        }
    }

    /** The phone has no screen lock to confirm with: the only way out is turning the vault off. */
    fun onRequestReset() = core.update { it.copy(confirmReset = true) }

    fun onConfirmReset() {
        viewModelScope.launch {
            vaultRepository.reset()
            lockoutJob?.cancel()
            clearEntry()
            core.value = Core(stage = VaultStage.INTRO)
        }
    }

    // ---- The vault list ----

    fun onChangePin() {
        clearEntry()
        firstPin = null
        core.update { it.copy(stage = VaultStage.SET_PIN, changingPin = true, feedback = PinFeedback.NONE) }
    }

    fun onRequestTurnOff() = core.update { it.copy(confirmTurnOff = true) }

    fun onConfirmTurnOff() {
        viewModelScope.launch {
            vaultRepository.reset()
            core.update { it.copy(confirmTurnOff = false) }
            _events.emit(AppVaultUiEvent.Close)
        }
    }

    fun onDismissConfirm() = core.update { it.copy(confirmPlacedCount = null, confirmTurnOff = false, confirmReset = false) }

    fun onAppInfo(app: AppInfo) = appRepository.openAppDetails(app)

    fun onRemoveFromVault(app: AppInfo) {
        viewModelScope.launch {
            vaultRepository.remove(app.vaultKey)
            _events.emit(AppVaultUiEvent.AppRemoved(app))
        }
    }

    fun onUndoRemove(app: AppInfo) {
        viewModelScope.launch { vaultRepository.add(listOf(app.vaultKey)) }
    }

    // ---- The Add apps picker ----

    fun onAddApps() = core.update { it.copy(stage = VaultStage.PICKER, pickerQuery = "", pickerSelected = emptySet()) }

    fun onPickerQueryChange(query: String) = core.update { it.copy(pickerQuery = query) }

    fun onPickerSortOptionChange(option: AppSortOption) = core.update { it.copy(sortOption = option) }

    fun onPickerSortDirectionToggle() = core.update {
        it.copy(sortDirection = if (it.sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING)
    }

    fun onPickerToggle(app: AppInfo) = core.update {
        val key = app.listKey
        it.copy(pickerSelected = if (key in it.pickerSelected) it.pickerSelected - key else it.pickerSelected + key)
    }

    /** Done in the picker: asks first when a ticked app is on Home or in a folder, since it will be taken off them. */
    fun onPickerDone() {
        viewModelScope.launch {
            val chosen = chosenApps()
            if (chosen.isEmpty()) {
                core.update { it.copy(stage = VaultStage.LIST, pickerQuery = "", pickerSelected = emptySet()) }
                return@launch
            }
            val placed = moveToVault.placedCount(chosen)
            if (placed > 0) core.update { it.copy(confirmPlacedCount = placed) } else moveChosen(chosen)
        }
    }

    fun onConfirmMove() {
        viewModelScope.launch { moveChosen(chosenApps()) }
    }

    private suspend fun chosenApps(): List<AppInfo> {
        val selected = core.value.pickerSelected
        return candidates.first().filter { it.listKey in selected }
    }

    private suspend fun moveChosen(apps: List<AppInfo>) {
        moveToVault(apps)
        core.update { it.copy(stage = VaultStage.LIST, pickerQuery = "", pickerSelected = emptySet(), confirmPlacedCount = null) }
    }

    private fun clearEntry() {
        entered.setLength(0)
        core.update { it.copy(digits = 0) }
    }

    private companion object {
        val PIN_STAGES = setOf(VaultStage.SET_PIN, VaultStage.CONFIRM_PIN, VaultStage.UNLOCK)
        const val COUNTDOWN_TICK_MS = 1_000L
    }
}
