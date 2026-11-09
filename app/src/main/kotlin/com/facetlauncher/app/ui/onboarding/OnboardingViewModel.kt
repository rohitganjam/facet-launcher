package com.facetlauncher.app.ui.onboarding

import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.SearchBarPosition

/**
 * Backs the whole onboarding flow (`ui/onboarding/OnboardingScreen.kt`) — one instance shared
 * across all three steps (obtained once at the host level, same as [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]
 * obtains its child ViewModels once and passes state down as stateless parameters). Only the
 * home-setup step (step 2) mutates anything, and only the dock/favorites reordering and content
 * mode — adding/removing apps happens on the full-screen pickers reused from Settings, which own
 * their own ViewModels and write to the same lists, the first facet's own.
 * The "make Facet your home screen" prompt (formerly a fourth step here) now lives on the real
 * Home screen instead — see [com.facetlauncher.app.ui.home.HomeViewModel]'s own default-launcher
 * handling and [SetDefaultLauncherSheet]'s doc for why.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val settingsRepository: SettingsRepository,
    private val applyFacetDefaults: ApplyFacetDefaultsUseCase,
    appRepository: AppRepository,
) : ViewModel() {

    /** The first facet, which `LauncherViewModel` makes active at launch, before onboarding shows. */
    private val facetId: Flow<Long?> = settingsRepository.settings
        .map { it.activeFacetId.takeIf { id -> id != NO_ACTIVE_FACET_ID } }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun apps(list: PlacementList): Flow<List<AppInfo>> =
        facetId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else observePlacedItems.apps(PlacementTarget(list, id)) }

    private suspend fun target(list: PlacementList): PlacementTarget? =
        facetId.first()?.let { PlacementTarget(list, it) }

    val uiState: StateFlow<OnboardingUiState> = combine(
        apps(PlacementList.FAVORITES),
        apps(PlacementList.DOCK),
        settingsRepository.settings,
        appRepository.observeInstalledApps(),
    ) { favorites, dockApps, settings, installedApps ->
        OnboardingUiState(
            facetId = settings.activeFacetId.takeIf { it != NO_ACTIVE_FACET_ID },
            dockApps = dockApps.toImmutableList(),
            favoriteApps = favorites.toImmutableList(),
            listContentMode = settings.listContentMode,
            appsToShowCount = settings.appsToShowCount,
            drawerPresentation = settings.drawerPresentation,
            searchBarPosition = settings.searchBarPosition,
            searchContactsEnabled = settings.searchContactsEnabled,
            searchSettingsEnabled = settings.searchSettingsEnabled,
            drawerFolderDisplayMode = settings.drawerFolderDisplayMode,
            installedApps = installedApps.toImmutableList(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    fun setListContentMode(mode: ListContentMode) {
        viewModelScope.launch { settingsRepository.setListContentMode(mode) }
    }

    fun setAppsToShowCount(count: Int) {
        viewModelScope.launch { settingsRepository.setAppsToShowCount(count) }
    }

    fun setDrawerPresentation(presentation: DrawerPresentation) {
        viewModelScope.launch { settingsRepository.setDrawerPresentation(presentation) }
    }

    fun setSearchBarPosition(position: SearchBarPosition) {
        viewModelScope.launch { settingsRepository.setSearchBarPosition(position) }
    }

    fun setSearchContactsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSearchContactsEnabled(enabled) }
    }

    fun setSearchSettingsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSearchSettingsEnabled(enabled) }
    }

    fun setDrawerFolderDisplayMode(mode: DrawerFolderDisplayMode) {
        viewModelScope.launch { settingsRepository.setDrawerFolderDisplayMode(mode) }
    }

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { target(PlacementList.DOCK)?.let { placementRepository.reorderItems(it, orderedApps.map { app -> PlacedItem.SingleApp(app) }) } }
    }

    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { target(PlacementList.FAVORITES)?.let { placementRepository.reorderItems(it, orderedApps.map { app -> PlacedItem.SingleApp(app) }) } }
    }

    /** Clears the favorites list in one action, instead of unchecking every app in the picker. */
    fun clearFavorites() {
        viewModelScope.launch { target(PlacementList.FAVORITES)?.let { placementRepository.deleteAll(it) } }
    }

    /** Clears the dock in one action, instead of unchecking every app in the picker. */
    fun clearDockApps() {
        viewModelScope.launch { target(PlacementList.DOCK)?.let { placementRepository.deleteAll(it) } }
    }

    /**
     * The lists are the facet's own already; its content mode and count are copied from the launcher settings
     * chosen above, before [onDone] leaves onboarding.
     */
    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            facetId.first()?.let { applyFacetDefaults(it, FacetDefaultsScope.APPS_LIST) }
            onDone()
        }
    }
}
