package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.selectedCalendarIds
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
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
    val facets: List<FacetEntity>,
    /** Only meaningful when the active facet's `listContentMode` isn't `FAVORITES`. */
    val usageAccessGranted: Boolean,
    /** Today's events for the active facet's effective calendar settings — empty when ungranted or none selected. */
    val calendarEvents: List<CalendarEvent>,
    /** F13 — notification count per package, empty unless both the "Notification badges" setting is on and access is granted. */
    val badgeCounts: Map<String, Int>,
    /** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
    val clockAccessories: ClockAccessoryState,
)

/**
 * Combines [SettingsRepository], [DockAppRepository], [FavoriteAppRepository]/[UsageStatsRepository]
 * (scoped to the active facet's [ListContentMode]), [CalendarRepository], and [FacetRepository]
 * into the Home screen's state — spans more than one repository, so it's a UseCase rather than
 * being called directly from [com.facetlauncher.app.ui.home.HomeViewModel].
 */
class ObserveHomeScreenStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val facetRepository: FacetRepository,
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
    // re-evaluation; neither facet nor settings changing wouldn't otherwise re-trigger below.
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HomeScreenState> {
        val activeFacet = combine(settingsRepository.settings, facetRepository.observeFacets()) { settings, facets ->
            facets.find { it.id == settings.activeFacetId }
        }

        val appListItems = combine(activeFacet, settingsRepository.settings, refreshTrigger) { facet, settings, _ ->
            facet to settings
        }.flatMapLatest { (facet, settings) -> observeAppListItems(facet, settings) }

        val dockApps = activeFacet.flatMapLatest { facet ->
            if (facet?.overrideDock == true) {
                facetDockAppRepository.observeDockAppsForFacet(facet.id)
            } else {
                dockAppRepository.observeDockApps()
            }
        }

        val calendarEvents = combine(activeFacet, settingsRepository.settings, refreshTrigger) { facet, settings, _ ->
            facet to settings
        }.flatMapLatest { (facet, settings) -> observeCalendarEvents(facet, settings) }

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
            facetRepository.observeFacets(),
            calendarEvents,
            badgeCountsAndAccessories,
        ) { settings, dockApps, items, facets, events, (badges, accessories) ->
            HomeScreenState(
                settings = settings,
                dockApps = dockApps,
                appListItems = items,
                facets = facets,
                usageAccessGranted = usageAccessRepository.isGranted(),
                calendarEvents = events,
                badgeCounts = badges,
                clockAccessories = accessories,
            )
        }
    }

    private fun observeAppListItems(facet: FacetEntity?, settings: LauncherSettings): Flow<List<AppInfo>> {
        if (facet == null) return flowOf(emptyList())
        val mode = if (facet.overrideApps) facet.listContentMode else settings.listContentMode
        val appsToShowCount = if (facet.overrideApps) facet.appsToShowCount else settings.appsToShowCount
        return when {
            mode == ListContentMode.FAVORITES && facet.overridingFavorites -> favoriteAppRepository.observeFavoritesForFacet(facet.id)
            mode == ListContentMode.FAVORITES -> defaultFavoriteAppRepository.observeDefaultFavorites()
            !usageAccessRepository.isGranted() -> flowOf(emptyList())
            mode == ListContentMode.RECENTS -> flow { emit(usageStatsRepository.getRecentApps(appsToShowCount)) }
            else -> flow { emit(usageStatsRepository.getMostUsedApps(appsToShowCount)) }
        }
    }

    private fun observeCalendarEvents(facet: FacetEntity?, settings: LauncherSettings): Flow<List<CalendarEvent>> {
        if (!calendarPermissionRepository.isGranted()) return flowOf(emptyList())
        val includeAllDay = if (facet?.overrideCalendar == true) facet.showAllDayEvents else settings.showAllDayEvents
        val selectedCalendarIds = if (facet?.overrideCalendar == true) facet.selectedCalendarIds else settings.selectedCalendarIds
        return flow { emit(calendarRepository.getTodayEvents(selectedCalendarIds, includeAllDay)) }
    }
}
