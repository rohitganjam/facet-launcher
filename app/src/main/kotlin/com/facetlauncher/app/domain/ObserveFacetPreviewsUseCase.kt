package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.selectedCalendarIds
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** A single facet's effective preview content — everything the carousel's own preview card needs beyond the clock. */
data class FacetPreviewData(
    val favorites: List<AppInfo> = emptyList(),
    /** This facet's effective dock — its own list when it overrides the dock, the launcher-wide default otherwise. */
    val dockApps: List<AppInfo> = emptyList(),
    /** Today's events for this facet's effective calendar settings — empty when ungranted or none selected, same as [HomeScreenState.calendarEvents]. */
    val calendarEvents: List<CalendarEvent> = emptyList(),
)

/**
 * Each facet's *effective* preview content, keyed by facet id — a facet overriding its own
 * content mode (Favorites/Recents/Most Used) or favorites shows those; one still inheriting
 * shows the launcher-wide default list instead, and the same for calendar events (gated by
 * `overrideCalendar`, mirroring [ObserveHomeScreenStateUseCase.observeCalendarEvents] exactly,
 * including which calendars are selected). The dock follows the same shape — a facet overriding
 * its own dock (`overrideDock`) shows that list, one still inheriting shows the launcher-wide
 * default. Spans [FacetRepository],
 * [SettingsRepository], [FavoriteAppRepository], [DefaultFavoriteAppRepository],
 * [FacetDockAppRepository], [DockAppRepository],
 * [UsageStatsRepository], [UsageAccessRepository], [CalendarPermissionRepository] and
 * [CalendarRepository], so it's a use case rather than something either the ViewModel or a
 * composable computes directly. Unlike [ObserveHomeScreenStateUseCase], which resolves state for
 * only the single *active* facet, this fans out to every facet at once — the carousel's
 * preview cards need to show each facet's own effective content while browsing, not just
 * the active one's.
 */
class ObserveFacetPreviewsUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val usageStatsRepository: UsageStatsRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<Long, FacetPreviewData>> {
        val facets = facetRepository.observeFacets().distinctUntilChanged()
        val settings = settingsRepository.settings.distinctUntilChanged()

        val facetIds = facets.map { it.map { facet -> facet.id } }.distinctUntilChanged()
        val ownFavoritesByFacet = facetIds.flatMapLatest { favoriteAppRepository.observeFavoritesForFacets(it) }
        val ownDockByFacet = facetIds.flatMapLatest { facetDockAppRepository.observeDockAppsForFacets(it) }

        return combine(
            facets,
            settings,
            ownFavoritesByFacet,
            defaultFavoriteAppRepository.observeDefaultFavorites(),
            ownDockByFacet,
            dockAppRepository.observeDockApps(),
        ) { facetList, launcherSettings, ownFavorites, defaultFavorites, ownDock, defaultDock ->
            Inputs(facetList, launcherSettings, ownFavorites, defaultFavorites, ownDock, defaultDock)
        }.flatMapLatest { inputs ->
            val flows = inputs.facets.map { facet ->
                val effectiveDock =
                    if (facet.overrideDock) inputs.ownDock[facet.id].orEmpty() else inputs.defaultDock
                combine(
                    observeAppListItems(
                        facet,
                        inputs.settings,
                        inputs.ownFavorites[facet.id].orEmpty(),
                        inputs.defaultFavorites,
                    ),
                    observeCalendarEvents(facet, inputs.settings),
                ) { apps, events -> facet.id to FacetPreviewData(apps, effectiveDock, events) }
            }

            combine(flows) { it.toMap() }
        }
    }

    private data class Inputs(
        val facets: List<FacetEntity>,
        val settings: LauncherSettings,
        val ownFavorites: Map<Long, List<AppInfo>>,
        val defaultFavorites: List<AppInfo>,
        val ownDock: Map<Long, List<AppInfo>>,
        val defaultDock: List<AppInfo>,
    )

    private fun observeAppListItems(
        facet: FacetEntity,
        settings: LauncherSettings,
        facetFavorites: List<AppInfo>,
        defaultFavorites: List<AppInfo>,
    ): Flow<List<AppInfo>> {
        val mode = if (facet.overrideApps) facet.listContentMode else settings.listContentMode
        val appsToShowCount = if (facet.overrideApps) facet.appsToShowCount else settings.appsToShowCount
        return when {
            mode == ListContentMode.FAVORITES && facet.overridingFavorites -> flow { emit(facetFavorites) }
            mode == ListContentMode.FAVORITES -> flow { emit(defaultFavorites) }
            !usageAccessRepository.isGranted() -> flow { emit(emptyList()) }
            mode == ListContentMode.RECENTS -> flow { emit(usageStatsRepository.getRecentApps(appsToShowCount)) }
            else -> flow { emit(usageStatsRepository.getMostUsedApps(appsToShowCount)) }
        }
    }

    private fun observeCalendarEvents(facet: FacetEntity, settings: LauncherSettings): Flow<List<CalendarEvent>> {
        if (!calendarPermissionRepository.isGranted()) return flowOf(emptyList())
        val includeAllDay = if (facet.overrideCalendar) facet.showAllDayEvents else settings.showAllDayEvents
        val selectedCalendarIds = if (facet.overrideCalendar) facet.selectedCalendarIds else settings.selectedCalendarIds
        return flow { emit(calendarRepository.getTodayEvents(selectedCalendarIds, includeAllDay)) }
    }
}
