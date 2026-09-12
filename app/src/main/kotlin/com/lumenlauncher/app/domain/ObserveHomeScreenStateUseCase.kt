package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.NotificationAccessRepository
import com.lumenlauncher.app.data.NotificationBadgeRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.selectedCalendarIds
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

data class HomeScreenState(
    val settings: LauncherSettings,
    val dockApps: List<AppInfo>,
    val appListItems: List<AppInfo>,
    val profiles: List<ProfileEntity>,
    /** Only meaningful when the active profile's `listContentMode` isn't `FAVORITES`. */
    val usageAccessGranted: Boolean,
    /** Today's events for the active profile's effective calendar settings — empty when ungranted or none selected. */
    val calendarEvents: List<CalendarEvent>,
    /** F13 — notification count per package, empty unless both the "Notification badges" setting is on and access is granted. */
    val badgeCounts: Map<String, Int>,
    /** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
    val clockAccessories: ClockAccessoryState,
)

/**
 * Combines [SettingsRepository], [DockAppRepository], [FavoriteAppRepository]/[UsageStatsRepository]
 * (scoped to the active profile's [ListContentMode]), [CalendarRepository], and [ProfileRepository]
 * into the Home screen's state — spans more than one repository, so it's a UseCase rather than
 * being called directly from [com.lumenlauncher.app.ui.home.HomeViewModel].
 */
class ObserveHomeScreenStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    private val profileDockAppRepository: ProfileDockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val profileRepository: ProfileRepository,
    private val usageStatsRepository: UsageStatsRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
    private val notificationBadgeRepository: NotificationBadgeRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
    private val observeClockAccessories: ObserveClockAccessoriesUseCase,
) {
    // PACKAGE_USAGE_STATS/READ_CALENDAR have no grant-change callback — the only way to grant
    // either is a system redirect/dialog, so callers (Home, on resume) call refresh() to force
    // re-evaluation; neither profile nor settings changing wouldn't otherwise re-trigger below.
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HomeScreenState> {
        val activeProfile = combine(settingsRepository.settings, profileRepository.observeProfiles()) { settings, profiles ->
            profiles.find { it.id == settings.activeProfileId }
        }

        val appListItems = combine(activeProfile, settingsRepository.settings, refreshTrigger) { profile, settings, _ ->
            profile to settings
        }.flatMapLatest { (profile, settings) -> observeAppListItems(profile, settings) }

        val dockApps = activeProfile.flatMapLatest { profile ->
            if (profile?.overrideDock == true) {
                profileDockAppRepository.observeDockAppsForProfile(profile.id)
            } else {
                dockAppRepository.observeDockApps()
            }
        }

        val calendarEvents = combine(activeProfile, settingsRepository.settings, refreshTrigger) { profile, settings, _ ->
            profile to settings
        }.flatMapLatest { (profile, settings) -> observeCalendarEvents(profile, settings) }

        // Gated on both the "Notification badges" setting and the real grant — the listener
        // repository keeps running regardless, so this is where "off" actually takes effect.
        val badgeCounts = combine(settingsRepository.settings, notificationBadgeRepository.badgeCounts) { settings, counts ->
            if (settings.notificationDotsEnabled && notificationAccessRepository.isGranted()) counts else emptyMap()
        }

        // Paired with badgeCounts (rather than added as a 7th argument below) purely to stay
        // within the combine() overload's supported arity — unrelated to badgeCounts otherwise.
        val badgeCountsAndAccessories = combine(badgeCounts, observeClockAccessories()) { badges, accessories -> badges to accessories }

        return combine(
            settingsRepository.settings,
            dockApps,
            appListItems,
            profileRepository.observeProfiles(),
            calendarEvents,
            badgeCountsAndAccessories,
        ) { settings, dockApps, items, profiles, events, (badges, accessories) ->
            HomeScreenState(
                settings = settings,
                dockApps = dockApps,
                appListItems = items,
                profiles = profiles,
                usageAccessGranted = usageAccessRepository.isGranted(),
                calendarEvents = events,
                badgeCounts = badges,
                clockAccessories = accessories,
            )
        }
    }

    private fun observeAppListItems(profile: ProfileEntity?, settings: LauncherSettings): Flow<List<AppInfo>> {
        if (profile == null) return flowOf(emptyList())
        val mode = if (profile.overrideApps) profile.listContentMode else settings.listContentMode
        val appsToShowCount = if (profile.overrideApps) profile.appsToShowCount else settings.appsToShowCount
        return when {
            mode == ListContentMode.FAVORITES && profile.overridingFavorites -> favoriteAppRepository.observeFavoritesForProfile(profile.id)
            mode == ListContentMode.FAVORITES -> defaultFavoriteAppRepository.observeDefaultFavorites()
            !usageAccessRepository.isGranted() -> flowOf(emptyList())
            mode == ListContentMode.RECENTS -> flow { emit(usageStatsRepository.getRecentApps(appsToShowCount)) }
            else -> flow { emit(usageStatsRepository.getMostUsedApps(appsToShowCount)) }
        }
    }

    private fun observeCalendarEvents(profile: ProfileEntity?, settings: LauncherSettings): Flow<List<CalendarEvent>> {
        if (!calendarPermissionRepository.isGranted()) return flowOf(emptyList())
        val includeAllDay = if (profile?.overrideCalendar == true) profile.showAllDayEvents else settings.showAllDayEvents
        val selectedCalendarIds = if (profile?.overrideCalendar == true) profile.selectedCalendarIds else settings.selectedCalendarIds
        return flow { emit(calendarRepository.getTodayEvents(selectedCalendarIds, includeAllDay)) }
    }
}
