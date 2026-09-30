package com.facetlauncher.app.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.PrivateSpaceRepository
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Private Space's own, deliberately minimal drawer state — browse + launch + search only (v1
 * scope). Unlike [DrawerViewModel], this has no folders/favorites/dock/shortcuts/contacts: none
 * of that is offered from [PrivateSpaceScreen], and its search is scoped only to Private Space's
 * own apps, never merged with the main Drawer's search.
 */
@HiltViewModel
class PrivateSpaceViewModel @Inject constructor(
    private val privateSpaceRepository: PrivateSpaceRepository,
    private val rankBySearchRelevance: RankBySearchRelevanceUseCase,
    private val recentlyInstalledAppsUseCase: RecentlyInstalledAppsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val privateSpaceState: StateFlow<PrivateSpaceState> = privateSpaceRepository.observePrivateSpaceState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrivateSpaceState.NotConfigured)

    private val apps: StateFlow<List<AppInfo>> = privateSpaceRepository.observePrivateSpaceApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val query = MutableStateFlow("")

    val filteredApps: StateFlow<List<AppInfo>> = combine(apps, query) { apps, query ->
        rankBySearchRelevance(apps, query) { it.label }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Same global "Recently installed" position setting the main Drawer uses — see `AppDrawerScreen`'s own field for the reasoning (one shared setting, not two). Independent of [filteredApps]/[query]: hidden while searching, same as the main Drawer's own item. */
    val recentlyInstalledApps: StateFlow<List<AppInfo>> = combine(apps, query, settingsRepository.settings) { apps, query, settings ->
        if (settings.recentlyInstalledPosition != RecentlyInstalledPosition.DO_NOT_SHOW && query.isBlank()) {
            recentlyInstalledAppsUseCase(apps)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }
}
