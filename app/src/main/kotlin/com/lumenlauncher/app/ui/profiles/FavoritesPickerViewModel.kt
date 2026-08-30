package com.lumenlauncher.app.ui.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
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

data class FavoritesPickerUiState(
    /** `false` when this picker is editing the launcher-wide default Favorites list (reached from Settings) rather than one profile's own. */
    val isProfileScoped: Boolean = true,
    val query: String = "",
    /** Favorited apps at the moment this screen was opened, in their favorites order — stays
     * exactly this set/order for the rest of this visit no matter what gets checked/unchecked
     * below; only re-latches the next time the screen is freshly opened. Not reorderable here —
     * that lives in this profile's own Settings page. */
    val selectedResults: List<AppInfo> = emptyList(),
    /** Every other matching app, in the installed-app list's own (alphabetical) order. */
    val otherResults: List<AppInfo> = emptyList(),
    val favoriteComponents: Set<Pair<String, String>> = emptySet(),
    val canAddMore: Boolean = true,
)

/**
 * Favorites picker (`4j`) — also doubles as the "Default favorites" picker reached from Settings.
 * No `profileId` (or [NO_ACTIVE_PROFILE_ID]) means the launcher-wide default list
 * ([DefaultFavoriteAppRepository]) rather than one profile's own ([FavoriteAppRepository]).
 */
@HiltViewModel
class FavoritesPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) : ViewModel() {

    private val profileId: Long? = savedStateHandle.get<Long>("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val maxFavorites: Int
        get() = if (profileId != null) FavoriteAppRepository.MAX_FAVORITES else DefaultFavoriteAppRepository.MAX_FAVORITES

    private fun observeFavorites(): Flow<List<AppInfo>> =
        profileId?.let { favoriteAppRepository.observeFavoritesForProfile(it) } ?: defaultFavoriteAppRepository.observeDefaultFavorites()

    /** Latched from the favorites' live order the first time it's observed; never updated after — see [FavoritesPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<FavoritesPickerUiState> = combine(
        query,
        installedApps,
        observeFavorites(),
        loadOrder,
    ) { query, installed, favorites, frozenOrder ->
        val liveOrder = favorites.map { it.packageName to it.activityName }
        if (frozenOrder == null) loadOrder.value = liveOrder
        val order = frozenOrder ?: liveOrder

        val filtered = installed.filter { it.label.contains(query, ignoreCase = true) }
        val (selected, other) = filtered.partition { (it.packageName to it.activityName) in order }
        FavoritesPickerUiState(
            isProfileScoped = profileId != null,
            query = query,
            selectedResults = selected.sortedBy { order.indexOf(it.packageName to it.activityName) },
            otherResults = other,
            favoriteComponents = liveOrder.toSet(),
            canAddMore = favorites.size < maxFavorites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun toggleFavorite(app: AppInfo) {
        val state = uiState.value
        val isFavorite = (app.packageName to app.activityName) in state.favoriteComponents
        viewModelScope.launch {
            if (profileId != null) {
                if (isFavorite) {
                    favoriteAppRepository.removeFavorite(profileId, app)
                } else if (state.canAddMore) {
                    favoriteAppRepository.addFavorite(profileId, app, position = state.favoriteComponents.size)
                }
            } else {
                if (isFavorite) {
                    defaultFavoriteAppRepository.removeFavorite(app)
                } else if (state.canAddMore) {
                    defaultFavoriteAppRepository.addFavorite(app, position = state.favoriteComponents.size)
                }
            }
        }
    }
}
