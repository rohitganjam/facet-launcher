package com.lumenlauncher.app.ui.dock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DockAppPickerUiState(
    /** `false` when this picker is editing the launcher-wide default dock (reached from Settings) rather than one profile's own. */
    val isProfileScoped: Boolean = false,
    val query: String = "",
    /** In-dock apps at the moment this screen was opened, in their dock order — stays exactly
     * this set/order for the rest of this visit no matter what gets checked/unchecked below;
     * only re-latches the next time the screen is freshly opened. Not reorderable here — that
     * lives in Settings' own Dock card. */
    val selectedResults: List<AppInfo> = emptyList(),
    /** Every other matching app, in the installed-app list's own (alphabetical) order. */
    val otherResults: List<AppInfo> = emptyList(),
    val dockPackageComponents: Set<Pair<String, String>> = emptySet(),
    val canAddMore: Boolean = true,
    val canRemove: Boolean = true,
)

/**
 * Dock app picker (`4k`) — also doubles as one profile's own dock picker (reached from that
 * profile's settings). No `profileId` (or [NO_ACTIVE_PROFILE_ID]) means the launcher-wide default
 * dock ([DockAppRepository]) rather than one profile's own ([ProfileDockAppRepository]) — the
 * same split [com.lumenlauncher.app.ui.profiles.FavoritesPickerViewModel] uses for favorites.
 */
@HiltViewModel
class DockAppPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val dockAppRepository: DockAppRepository,
    private val profileDockAppRepository: ProfileDockAppRepository,
) : ViewModel() {

    private val profileId: Long? = savedStateHandle.get<Long>("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    private fun observeDockApps(): Flow<List<AppInfo>> =
        profileId?.let { profileDockAppRepository.observeDockAppsForProfile(it) } ?: dockAppRepository.observeDockApps()

    /** Latched from the dock's live order the first time it's observed; never updated after — see [DockAppPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<DockAppPickerUiState> = combine(
        query,
        installedApps,
        observeDockApps(),
        loadOrder,
    ) { query, installed, dockApps, frozenOrder ->
        val liveOrder = dockApps.map { it.packageName to it.activityName }
        if (frozenOrder == null) loadOrder.value = liveOrder
        val order = frozenOrder ?: liveOrder

        val filtered = installed.filter { it.label.contains(query, ignoreCase = true) }
        val (selected, other) = filtered.partition { (it.packageName to it.activityName) in order }
        DockAppPickerUiState(
            isProfileScoped = profileId != null,
            query = query,
            selectedResults = selected.sortedBy { order.indexOf(it.packageName to it.activityName) },
            otherResults = other,
            dockPackageComponents = liveOrder.toSet(),
            canAddMore = dockApps.size < DockAppRepository.MAX_APPS,
            canRemove = dockApps.size > DockAppRepository.MIN_APPS,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockAppPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun toggleDockApp(app: AppInfo) {
        val state = uiState.value
        val isInDock = (app.packageName to app.activityName) in state.dockPackageComponents
        viewModelScope.launch {
            if (profileId != null) {
                if (isInDock) {
                    if (state.canRemove) profileDockAppRepository.removeDockApp(profileId, app)
                } else if (state.canAddMore) {
                    profileDockAppRepository.addDockApp(profileId, app, position = state.dockPackageComponents.size)
                }
            } else {
                if (isInDock) {
                    if (state.canRemove) dockAppRepository.removeDockApp(app)
                } else if (state.canAddMore) {
                    dockAppRepository.addDockApp(app, position = state.dockPackageComponents.size)
                }
            }
        }
    }
}
