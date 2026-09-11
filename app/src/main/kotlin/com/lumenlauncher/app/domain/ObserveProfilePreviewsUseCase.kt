package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.selectedCalendarIds
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** A single profile's effective preview content — everything the carousel's own preview card needs beyond the clock. */
data class ProfilePreviewData(
    val favorites: List<AppInfo> = emptyList(),
    /** This profile's effective dock — its own list when it overrides the dock, the launcher-wide default otherwise. */
    val dockApps: List<AppInfo> = emptyList(),
    /** Today's events for this profile's effective calendar settings — empty when ungranted or none selected, same as [HomeScreenState.calendarEvents]. */
    val calendarEvents: List<CalendarEvent> = emptyList(),
)

/**
 * Each profile's *effective* preview content, keyed by profile id — a profile overriding its own
 * content mode (Favorites/Recents/Most Used) or favorites shows those; one still inheriting
 * shows the launcher-wide default list instead, and the same for calendar events (gated by
 * `overrideCalendar`, mirroring [ObserveHomeScreenStateUseCase.observeCalendarEvents] exactly,
 * including which calendars are selected). The dock follows the same shape — a profile overriding
 * its own dock (`overrideDock`) shows that list, one still inheriting shows the launcher-wide
 * default. Spans [ProfileRepository],
 * [SettingsRepository], [FavoriteAppRepository], [DefaultFavoriteAppRepository],
 * [ProfileDockAppRepository], [DockAppRepository],
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
    private val profileDockAppRepository: ProfileDockAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val usageStatsRepository: UsageStatsRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<Long, ProfilePreviewData>> {
        val profiles = profileRepository.observeProfiles().distinctUntilChanged()
        val settings = settingsRepository.settings.distinctUntilChanged()

        val profileIds = profiles.map { it.map { profile -> profile.id } }.distinctUntilChanged()
        val ownFavoritesByProfile = profileIds.flatMapLatest { favoriteAppRepository.observeFavoritesForProfiles(it) }
        val ownDockByProfile = profileIds.flatMapLatest { profileDockAppRepository.observeDockAppsForProfiles(it) }

        return combine(
            profiles,
            settings,
            ownFavoritesByProfile,
            defaultFavoriteAppRepository.observeDefaultFavorites(),
            ownDockByProfile,
            dockAppRepository.observeDockApps(),
        ) { profileList, launcherSettings, ownFavorites, defaultFavorites, ownDock, defaultDock ->
            Inputs(profileList, launcherSettings, ownFavorites, defaultFavorites, ownDock, defaultDock)
        }.flatMapLatest { inputs ->
            val flows = inputs.profiles.map { profile ->
                val effectiveDock =
                    if (profile.overrideDock) inputs.ownDock[profile.id].orEmpty() else inputs.defaultDock
                combine(
                    observeAppListItems(
                        profile,
                        inputs.settings,
                        inputs.ownFavorites[profile.id].orEmpty(),
                        inputs.defaultFavorites,
                    ),
                    observeCalendarEvents(profile, inputs.settings),
                ) { apps, events -> profile.id to ProfilePreviewData(apps, effectiveDock, events) }
            }

            combine(flows) { it.toMap() }
        }
    }

    private data class Inputs(
        val profiles: List<ProfileEntity>,
        val settings: LauncherSettings,
        val ownFavorites: Map<Long, List<AppInfo>>,
        val defaultFavorites: List<AppInfo>,
        val ownDock: Map<Long, List<AppInfo>>,
        val defaultDock: List<AppInfo>,
    )

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
        val selectedCalendarIds = if (profile.overrideCalendar) profile.selectedCalendarIds else settings.selectedCalendarIds
        return flow { emit(calendarRepository.getTodayEvents(selectedCalendarIds, includeAllDay)) }
    }
}
