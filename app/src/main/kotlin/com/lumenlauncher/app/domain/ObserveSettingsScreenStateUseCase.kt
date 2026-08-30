package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SettingsScreenState(
    val settings: LauncherSettings,
    val dockApps: List<AppInfo>,
    val defaultFavorites: List<AppInfo>,
)

/**
 * Combines [SettingsRepository], [DockAppRepository] and [DefaultFavoriteAppRepository] into one
 * observable state for the Settings screen — the one place in Phase 2 where logic genuinely spans
 * multiple repositories, so it earns a UseCase rather than being called directly from a ViewModel.
 */
class ObserveSettingsScreenStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    operator fun invoke(): Flow<SettingsScreenState> =
        combine(
            settingsRepository.settings,
            dockAppRepository.observeDockApps(),
            defaultFavoriteAppRepository.observeDefaultFavorites(),
        ) { settings, dockApps, defaultFavorites ->
            SettingsScreenState(settings = settings, dockApps = dockApps, defaultFavorites = defaultFavorites)
        }
}
