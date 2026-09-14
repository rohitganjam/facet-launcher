package com.facetlauncher.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the whole onboarding flow (`ui/onboarding/OnboardingScreen.kt`) — one instance shared
 * across all three steps (obtained once at the host level, same as [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]
 * obtains its child ViewModels once and passes state down as stateless parameters). Only the
 * home-setup step (step 2) mutates anything, and only the dock/favorites reordering and content
 * mode — adding/removing apps happens on the full-screen pickers reused from Settings, which own
 * their own ViewModels and write directly to [dockAppRepository]/[defaultFavoriteAppRepository].
 * The "make Facet your home screen" prompt (formerly a fourth step here) now lives on the real
 * Home screen instead — see [com.facetlauncher.app.ui.home.HomeViewModel]'s own default-launcher
 * handling and [SetDefaultLauncherSheet]'s doc for why.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<OnboardingUiState> = combine(
        defaultFavoriteAppRepository.observeDefaultFavorites(),
        dockAppRepository.observeDockApps(),
        settingsRepository.settings,
    ) { favorites, dockApps, settings ->
        OnboardingUiState(
            dockApps = dockApps,
            favoriteApps = favorites,
            listContentMode = settings.listContentMode,
            appsToShowCount = settings.appsToShowCount,
            drawerPresentation = settings.drawerPresentation,
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

    fun reorderDockApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { dockAppRepository.reorderDockApps(orderedApps) }
    }

    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { defaultFavoriteAppRepository.reorderItems(orderedApps.map { PlacedItem.SingleApp(it) }) }
    }

    /** Clears the default favorites list in one action, instead of unchecking every app in the picker. */
    fun clearFavorites() {
        viewModelScope.launch { defaultFavoriteAppRepository.deleteAllDefaultFavorites() }
    }

    /** Clears the default dock in one action, instead of unchecking every app in the picker. */
    fun clearDockApps() {
        viewModelScope.launch { dockAppRepository.deleteAllDockApps() }
    }
}
