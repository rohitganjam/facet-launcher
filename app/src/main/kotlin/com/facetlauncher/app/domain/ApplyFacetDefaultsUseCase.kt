package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** The parts of a facet's Home that a launcher-wide default can seed. */
enum class FacetDefaultsScope {
    /** The facet's Dock list. */
    DOCK,

    /** The facet's Favorites list, content mode and count. */
    APPS_LIST,

    /** Dock display mode and the app list's look, landscape twins included. */
    LOOK,
}

/**
 * Copies the launcher-wide defaults into a facet: once when it is created, and again on "Reset to
 * default". A facet owns what it was given, so later edits to the defaults never reach it.
 */
class ApplyFacetDefaultsUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val placementRepository: PlacementRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(facetId: Long, scope: FacetDefaultsScope) {
        when (scope) {
            FacetDefaultsScope.DOCK -> copyList(PlacementList.DOCK, facetId)
            FacetDefaultsScope.APPS_LIST -> {
                copyList(PlacementList.FAVORITES, facetId)
                facetRepository.applyAppsContentDefaults(facetId, settingsRepository.settings.first())
            }
            FacetDefaultsScope.LOOK -> facetRepository.applyLookDefaults(facetId, settingsRepository.settings.first())
        }
    }

    /** Every scope: what a brand-new facet starts from. */
    suspend fun applyAll(facetId: Long) {
        FacetDefaultsScope.entries.forEach { invoke(facetId, it) }
    }

    private suspend fun copyList(list: PlacementList, facetId: Long) {
        placementRepository.copyList(PlacementTarget(list), PlacementTarget(list, facetId))
    }
}
