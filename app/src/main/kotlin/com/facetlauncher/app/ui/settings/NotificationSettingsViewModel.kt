package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val isAccessGranted: Boolean = false,
    /** What the preview's Home list and Dock render: the user's own placements, or sample apps when a list is empty. */
    val homePreviewApps: List<PlacedItem> = emptyList(),
    val dockPreviewApps: List<PlacedItem> = emptyList(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
)

private const val HOME_SAMPLE_COUNT = 3
private const val DOCK_SAMPLE_COUNT = 4

/**
 * F13's own settings page, reached from Settings' NOTIFICATIONS card — a real screen rather than
 * an inline toggle, per direct feedback, since it now governs both whether badges show at all
 * and which style they render in. Turning the toggle on when notification listener access isn't
 * granted yet routes through [NotificationAccessExplanationScreen] instead of flipping the
 * setting directly — see [NotificationSettingsScreen]'s own `onEnabledChange` wiring.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
    observePlacedItems: ObservePlacedItemsUseCase,
    private val wallpaperRepository: WallpaperRepository,
    private val defaultAppRepository: DefaultAppRepository,
    getInstalledApps: GetInstalledAppsUseCase,
    private val selectPreviewApps: SelectPreviewAppsUseCase,
) : ViewModel() {

    private val isAccessGranted = MutableStateFlow(notificationAccessRepository.isGranted())
    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)
    private val preferredPreviewPackages = MutableStateFlow<List<String>>(emptyList())

    private val previewInputs = combine(
        observePlacedItems.items(PlacementTarget.GlobalFavorites),
        observePlacedItems.items(PlacementTarget.GlobalDock),
        getInstalledApps.observe(),
        preferredPreviewPackages,
    ) { favorites, dock, installed, preferred ->
        val sample = selectPreviewApps(installed, preferred, HOME_SAMPLE_COUNT + DOCK_SAMPLE_COUNT).map { PlacedItem.SingleApp(it) }
        Triple(favorites, dock, sample)
        // Seeded so the toggles aren't held back by the preview's slower placement / installed-app queries.
    }.onStart { emit(Triple(emptyList(), emptyList(), emptyList())) }

    val uiState: StateFlow<NotificationSettingsUiState> = combine(
        settingsRepository.settings,
        isAccessGranted,
        previewInputs,
        homeWallpaper,
    ) { settings, granted, (favorites, dock, sample), wallpaper ->
        val homeApps = favorites.takeIf { settings.listContentMode == ListContentMode.FAVORITES && it.isNotEmpty() }
            ?: sample.take(HOME_SAMPLE_COUNT)
        NotificationSettingsUiState(
            settings = settings,
            isAccessGranted = granted,
            homePreviewApps = homeApps,
            dockPreviewApps = dock.ifEmpty { sample.drop(HOME_SAMPLE_COUNT) },
            homeWallpaper = wallpaper,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationSettingsUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
        viewModelScope.launch { preferredPreviewPackages.value = defaultAppRepository.getDefaultAppPackages() }
    }

    /** No grant-change callback exists — call on `onResume` so returning from the explanation screen's Settings redirect is picked up. */
    fun refreshAccessGranted() {
        isAccessGranted.value = notificationAccessRepository.isGranted()
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationDotsEnabled(enabled) }
    }

    fun setBadgeStyle(style: NotificationBadgeStyle) {
        viewModelScope.launch { settingsRepository.setNotificationBadgeStyle(style) }
    }

    fun setShowMediaPlayer(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowMediaPlayerOnHome(enabled) }
    }

    fun setShowSkipButtons(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowMediaSkipButtons(enabled) }
    }

    fun setShowNotificationsInAppMenu(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowNotificationsInAppMenu(enabled) }
    }
}
