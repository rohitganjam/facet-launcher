package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** A single profile's effective preview content — everything the carousel's own preview card needs beyond clock/dock. */
data class ProfilePreviewData(
    val favorites: List<AppInfo> = emptyList(),
    /** Today's events for this profile's effective calendar settings — empty when ungranted or none selected, same as [HomeScreenState.calendarEvents]. */
    val calendarEvents: List<CalendarEvent> = emptyList(),
)

/**
 * Each profile's *effective* preview content, keyed by profile id — a profile overriding its own
 * content mode (Favorites/Recents/Most Used) or favorites shows those; one still inheriting
 * shows the launcher-wide default list instead, and the same for calendar events (gated by
 * `overrideCalendar`, mirroring [ObserveHomeScreenStateUseCase.observeCalendarEvents] exactly —
 * including that only `showAllDayEvents` actually respects the per-profile override today, not
 * `selectedCalendarIds`; see that function's own history). Spans [ProfileRepository],
 * [SettingsRepository], [FavoriteAppRepository], [DefaultFavoriteAppRepository],
 * [UsageStatsRepository], [UsageAccessRepository], [CalendarPermissionRepository] and
 * [CalendarRepository], so it's a use case rather than something either the ViewModel or a
 * composable computes directly. Unlike [ObserveHomeScreenStateUseCase], which resolves state for
 * only the single *active* profile, this fans out to every profile at once — the carousel's
 * preview cards need to show each profile's own effective content while browsing, not just
 * the active one's.
 */
class ObserveProfilePreviewsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val usageStatsRepository: UsageStatsRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<Long, ProfilePreviewData>> {
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
                combine(
                    observeAppListItems(profile, launcherSettings, ownFavorites[profile.id].orEmpty(), defaultFavorites),
                    observeCalendarEvents(profile, launcherSettings),
                ) { apps, events -> profile.id to ProfilePreviewData(apps, events) }
            }

            combine(flows) { it.toMap() }
        }
    }

    private fun observeAppListItems(
        profile: ProfileEntity,
        settings: LauncherSettings,
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

    private fun observeCalendarEvents(profile: ProfileEntity, settings: LauncherSettings): Flow<List<CalendarEvent>> {
        if (!calendarPermissionRepository.isGranted()) return flowOf(emptyList())
        val includeAllDay = if (profile.overrideCalendar) profile.showAllDayEvents else settings.showAllDayEvents
        return flow { emit(calendarRepository.getTodayEvents(settings.selectedCalendarIds, includeAllDay)) }
    }
}
