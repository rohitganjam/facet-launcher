package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Add to Favorites"/"Add to profile favorites" row — appends [app] to whichever
 * list [com.lumenlauncher.app.ui.components.AppContextMenu] is currently showing that row for
 * (the active profile's own Favorites when it's overriding, otherwise the launcher-wide default),
 * mirroring [ObserveQuickAddStateUseCase]'s own resolution exactly so the write always lands where
 * the row said it would.
 */
class AddAppToFavoritesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeProfileId = settingsRepository.settings.first().activeProfileId
        val profile = profileRepository.getById(activeProfileId)
        if (profile?.overridingFavorites == true) {
            val position = favoriteAppRepository.observeFavoritesForProfile(profile.id).first().size
            favoriteAppRepository.addFavorite(profile.id, app, position)
        } else {
            val position = defaultFavoriteAppRepository.observeDefaultFavorites().first().size
            defaultFavoriteAppRepository.addFavorite(app, position)
        }
    }
}
