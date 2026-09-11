package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppListLimits
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Backs the "Add to Favorites"/"Add to Dock" rows in [com.lumenlauncher.app.ui.components.AppContextMenu].
 * A `null` field hides its row entirely (that list is already at its cap); a non-null [Boolean]
 * shows it and says whether it reads "Add to profile favorites/dock" (the active profile is
 * overriding its own) or "Add to Favorites/Dock" (the launcher-wide default).
 */
data class QuickAddState(
    val favoritesOverride: Boolean? = null,
    val dockOverride: Boolean? = null,
)

/**
 * Spans [ProfileRepository]/[SettingsRepository] (to resolve the active profile and whether it's
 * overriding Favorites/Dock) and both the profile-scoped and launcher-wide default Favorites/Dock
 * repositories, so it's a UseCase rather than living directly in a ViewModel — mirrors
 * [ObserveHomeScreenStateUseCase]'s own override-resolution branching for [HomeScreenState.dockApps]/
 * [HomeScreenState.appListItems].
 */
class ObserveQuickAddStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val profileDockAppRepository: ProfileDockAppRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<QuickAddState> {
        val activeProfile = combine(settingsRepository.settings, profileRepository.observeProfiles()) { settings, profiles ->
            profiles.find { it.id == settings.activeProfileId }
        }

        val favorites = activeProfile.flatMapLatest { profile ->
            if (profile?.overridingFavorites == true) {
                favoriteAppRepository.observeFavoritesForProfile(profile.id).map { it.size to true }
            } else {
                defaultFavoriteAppRepository.observeDefaultFavorites().map { it.size to false }
            }
        }

        val dock = activeProfile.flatMapLatest { profile ->
            if (profile?.overrideDock == true) {
                profileDockAppRepository.observeDockAppsForProfile(profile.id).map { it.size to true }
            } else {
                dockAppRepository.observeDockApps().map { it.size to false }
            }
        }

        return combine(favorites, dock) { (favoritesCount, favoritesOverride), (dockCount, dockOverride) ->
            QuickAddState(
                favoritesOverride = favoritesOverride.takeIf { favoritesCount < AppListLimits.MAX_FAVORITES },
                dockOverride = dockOverride.takeIf { dockCount < DockAppRepository.MAX_APPS },
            )
        }
    }
}
