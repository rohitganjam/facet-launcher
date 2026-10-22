package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 * Joins stored Dock/Favorites rows against the live installed-app and folder lists: an entry whose
 * app is no longer installed (or whose folder is gone) is dropped, live, so an uninstall collapses
 * the list immediately.
 */
class ObservePlacedItemsUseCase @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val folderRepository: FolderRepository,
    private val appRepository: AppRepository,
) {

    /** The target's apps only, in position order. */
    fun apps(target: PlacementTarget): Flow<List<AppInfo>> =
        combine(placementRepository.observeAppEntities(target), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }

    /**
     * [list] for every facet at once, keyed by facet id — for the facet carousel's preview cards.
     * Subscribes to the live installed-app list once and reuses it across all [facetIds].
     */
    fun appsForFacets(list: PlacementList, facetIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        return combine(placementRepository.observeAppEntitiesForFacets(list, facetIds), appRepository.observeInstalledApps()) { perFacet, installed ->
            facetIds.indices.associate { index -> facetIds[index] to hydrate(perFacet[index], installed) }
        }
    }

    /** The target's full ordered content, apps and folders interleaved. A 0-app folder placed here is still emitted. */
    fun items(target: PlacementTarget): Flow<List<PlacedItem>> =
        combine(
            placementRepository.observeAppEntities(target),
            placementRepository.observeFolderPlacements(target),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
            mergeItems(appEntities, placements, folders, installed)
        }

    /** Batched counterpart of [items] for [list], mirroring [appsForFacets]. */
    fun itemsForFacets(list: PlacementList, facetIds: List<Long>): Flow<Map<Long, List<PlacedItem>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        return combine(
            placementRepository.observeAppEntitiesForFacets(list, facetIds),
            placementRepository.observeFolderPlacementsForFacets(list, facetIds),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { perFacetApps, perFacetPlacements, folders, installed ->
            facetIds.indices.associate { index ->
                facetIds[index] to mergeItems(perFacetApps[index], perFacetPlacements[index], folders, installed)
            }
        }
    }

    private fun mergeItems(
        appEntities: List<AppPlacementEntity>,
        placements: List<FolderPlacementEntity>,
        folders: List<Folder>,
        installed: List<AppInfo>,
    ): List<PlacedItem> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
        val foldersById = folders.associateBy { it.id }

        val appItems = appEntities.mapNotNull { entity ->
            installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)]?.let { app ->
                entity.position to PlacedItem.SingleApp(app)
            }
        }
        val folderItems = placements.mapNotNull { placement ->
            foldersById[placement.folderId]?.let { folder ->
                placement.position to PlacedItem.FolderItem(folder)
            }
        }
        return (appItems + folderItems).sortedBy { it.first }.map { it.second }
    }

    private fun hydrate(entities: List<AppPlacementEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)] }
    }
}
