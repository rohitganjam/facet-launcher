package com.facetlauncher.app.domain

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

/** A single facet's effective preview content — everything the carousel's own preview card needs beyond the clock. */
data class FacetPreviewData(
    val favorites: List<PlacedItem> = emptyList(),
    /** This facet's own dock. */
    val dockApps: List<PlacedItem> = emptyList(),
    /** Today's events for this facet's effective calendar settings — empty when ungranted or none selected, same as [HomeScreenState.calendarEvents]. */
    val calendarEvents: List<CalendarEvent> = emptyList(),
)

/**
 * Each facet's preview content, keyed by facet id — its own content mode (Favorites/Recents/Most Used)
 * and favorites, its own dock, and calendar events from the facet's clock's own calendar choices, as
 * [ObserveHomeScreenStateUseCase.observeCalendarEvents] reads them. Spans [FacetRepository],
 * [SettingsRepository], [ObservePlacedItemsUseCase],
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
    private val observePlacedItems: ObservePlacedItemsUseCase,
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
        val ownFavoritesByFacet = facetIds.flatMapLatest { observePlacedItems.itemsForFacets(PlacementList.FAVORITES, it) }
        val ownDockByFacet = facetIds.flatMapLatest { observePlacedItems.itemsForFacets(PlacementList.DOCK, it) }

        return combine(facets, settings, ownFavoritesByFacet, ownDockByFacet) { facetList, launcherSettings, ownFavorites, ownDock ->
            Inputs(facetList, launcherSettings, ownFavorites, ownDock)
        }.flatMapLatest { inputs ->
            val flows = inputs.facets.map { facet ->
                combine(
                    observeAppListItems(facet, inputs.ownFavorites[facet.id].orEmpty()),
                    observeCalendarEvents(facet, inputs.settings),
                ) { apps, events -> facet.id to FacetPreviewData(apps, inputs.ownDock[facet.id].orEmpty(), events) }
            }

            combine(flows) { it.toMap() }
        }
    }

    private data class Inputs(
        val facets: List<Facet>,
        val settings: LauncherSettings,
        val ownFavorites: Map<Long, List<PlacedItem>>,
        val ownDock: Map<Long, List<PlacedItem>>,
    )

    private fun observeAppListItems(facet: Facet, facetFavorites: List<PlacedItem>): Flow<List<PlacedItem>> {
        val mode = facet.listContentMode
        val appsToShowCount = facet.appsToShowCount
        return when {
            mode == ListContentMode.FAVORITES -> flow { emit(facetFavorites) }
            !usageAccessRepository.isGranted() -> flow { emit(emptyList()) }
            mode == ListContentMode.RECENTS -> flow { emit(usageStatsRepository.getRecentApps(appsToShowCount).map { PlacedItem.SingleApp(it) }) }
            else -> flow { emit(usageStatsRepository.getMostUsedApps(appsToShowCount).map { PlacedItem.SingleApp(it) }) }
        }
    }

    private fun observeCalendarEvents(facet: Facet, settings: LauncherSettings): Flow<List<CalendarEvent>> {
        if (!calendarPermissionRepository.isGranted()) return flowOf(emptyList())
        val calendar = effectiveClockConfig(facet, settings).calendar
        return flow { emit(calendarRepository.getUpcomingEvents(calendar.calendarIds, calendar.showAllDay)) }
    }
}
