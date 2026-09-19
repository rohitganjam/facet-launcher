package com.facetlauncher.app.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.PrivateSpaceRepository
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
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
) : ViewModel() {

    val privateSpaceState: StateFlow<PrivateSpaceState> = privateSpaceRepository.observePrivateSpaceState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrivateSpaceState.NotConfigured)

    private val apps: StateFlow<List<AppInfo>> = privateSpaceRepository.observePrivateSpaceApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val query = MutableStateFlow("")

    val filteredApps: StateFlow<List<AppInfo>> = combine(apps, query) { apps, query ->
        rankBySearchRelevance(apps, query) { it.label }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }
}
