package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.ListContentMode
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Each profile's *effective* apps list items, keyed by profile id — a profile overriding its own
 * content mode (Favorites/Recents/Most Used) or favorites shows those; one still inheriting
 * shows the launcher-wide default list instead. Spans [ProfileRepository], [SettingsRepository],
 * [FavoriteAppRepository], [DefaultFavoriteAppRepository], [UsageStatsRepository] and
 * [UsageAccessRepository], so it's a use case rather than something either the ViewModel or a
 * composable computes directly. Unlike [ObserveHomeScreenStateUseCase], which resolves items for
 * only the single *active* profile, this fans out to every profile at once — the carousel's
 * preview cards need to show each profile's own effective apps list while browsing, not just
 * the active one's.
 */
class ObserveProfilePreviewsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val usageStatsRepository: UsageStatsRepository,
    private val usageAccessRepository: UsageAccessRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<Long, List<AppInfo>>> {
        val profiles = profileRepository.observeProfiles().distinctUntilChanged()
        val settings = settingsRepository.settings.distinctUntilChanged()

        val ownFavoritesByProfile = profiles
            .map { it.map { profile -> profile.id } }
            .distinctUntilChanged()
            .flatMapLatest { profileIds -> favoriteAppRepository.observeFavoritesForProfiles(profileIds) }

        return combine(
            profiles,
            settings,
            ownFavoritesByProfile,
            defaultFavoriteAppRepository.observeDefaultFavorites()
        ) { profileList, launcherSettings, ownFavorites, defaultFavorites ->
            profileList to launcherSettings to ownFavorites to defaultFavorites
        }.flatMapLatest { (triple, defaultFavorites) ->
            val (pair, ownFavorites) = triple
            val (profileList, launcherSettings) = pair

            val flows = profileList.map { profile ->
                observeAppListItems(profile, launcherSettings, ownFavorites[profile.id].orEmpty(), defaultFavorites)
                    .map { profile.id to it }
            }

            combine(flows) { it.toMap() }
        }
    }

    private fun observeAppListItems(
        profile: com.lumenlauncher.app.data.local.ProfileEntity,
        settings: com.lumenlauncher.app.data.model.LauncherSettings,
        profileFavorites: List<AppInfo>,
        defaultFavorites: List<AppInfo>,
    ): Flow<List<AppInfo>> {
        val mode = if (profile.overrideApps) profile.listContentMode else settings.listContentMode
        val appsToShowCount = if (profile.overrideApps) profile.appsToShowCount else settings.appsToShowCount
        return when {
            mode == ListContentMode.FAVORITES && profile.overridingFavorites -> flow { emit(profileFavorites) }
            mode == ListContentMode.FAVORITES -> flow { emit(defaultFavorites) }
            !usageAccessRepository.isGranted() -> flow { emit(emptyList()) }
            mode == ListContentMode.RECENTS -> flow { emit(usageStatsRepository.getRecentApps(appsToShowCount)) }
            else -> flow { emit(usageStatsRepository.getMostUsedApps(appsToShowCount)) }
        }
    }
}
