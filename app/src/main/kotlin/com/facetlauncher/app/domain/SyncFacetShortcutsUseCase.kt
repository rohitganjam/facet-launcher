package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetShortcutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

/**
 * Keeps [FacetShortcutRepository]'s published dynamic shortcuts in lockstep with the facets the
 * user may switch to ([SelectableFacetsUseCase]) — every add/rename/reorder/delete, or a change of
 * entitlement, re-publishes the full shortcut set, so no manual "resync" step is needed anywhere a facet is mutated. Runs for as long as it's
 * collected — launched once, for the app's lifetime, from
 * [com.facetlauncher.app.ui.launcher.LauncherViewModel], mirroring
 * [CleanUpUninstalledAppsUseCase]'s own "runs forever" shape.
 */
class SyncFacetShortcutsUseCase @Inject constructor(
    private val selectableFacets: SelectableFacetsUseCase,
    private val facetShortcutRepository: FacetShortcutRepository,
) {
    suspend operator fun invoke() {
        selectableFacets.observe().collect { facets -> facetShortcutRepository.syncShortcuts(facets) }
    }
}
