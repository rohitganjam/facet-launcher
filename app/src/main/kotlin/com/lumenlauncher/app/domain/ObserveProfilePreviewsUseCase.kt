package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Each profile's *effective* favorites, keyed by profile id — a profile overriding its own
 * favorites (see [com.lumenlauncher.app.data.local.ProfileEntity.overridingFavorites]) shows
 * those; one still inheriting shows the launcher-wide default list instead. Spans
 * [ProfileRepository], [FavoriteAppRepository] and [DefaultFavoriteAppRepository], so it's a use
 * case rather than something either the ViewModel or a composable computes directly. Unlike
 * [ObserveHomeScreenStateUseCase], which resolves favorites for only the single *active*
 * profile, this fans out to every profile at once — the carousel's preview cards need to show
 * each profile's own effective favorites while browsing, not just the active one's.
 */
class ObserveProfilePreviewsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<Long, List<AppInfo>>> {
        val profiles = profileRepository.observeProfiles().distinctUntilChanged()
        val ownFavoritesByProfile = profiles
            .map { it.map { profile -> profile.id } }
            .distinctUntilChanged()
            .flatMapLatest { profileIds -> favoriteAppRepository.observeFavoritesForProfiles(profileIds) }

        return combine(profiles, ownFavoritesByProfile, defaultFavoriteAppRepository.observeDefaultFavorites()) { profileList, ownFavorites, defaultFavorites ->
            profileList.associate { profile ->
                profile.id to if (profile.overridingFavorites) ownFavorites[profile.id].orEmpty() else defaultFavorites
            }
        }
    }
}
