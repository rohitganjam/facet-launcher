package com.facetlauncher.app.benchmark

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID
import com.facetlauncher.app.ui.components.HOME_SET_DEFAULT_PROMPT_ID
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Benchmark builds only. `am broadcast -n .../.benchmark.BenchmarkSeedReceiver --ei facets N` finishes
 * onboarding, dismisses the first-run prompts and tops the launcher up to [N] facets, each with its own
 * favorites, so a run starts from a realistic Home instead of onboarding. Safe to send again: it only adds.
 */
class BenchmarkSeedReceiver : BroadcastReceiver() {

    /** Hilt cannot inject a Kotlin receiver (its generated base class has an abstract `onReceive`), so the repositories come from an entry point. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Repositories {
        fun settingsRepository(): SettingsRepository

        fun facetRepository(): FacetRepository

        fun placementRepository(): PlacementRepository

        fun appRepository(): AppRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val facets = intent.getIntExtra(EXTRA_FACETS, 1).coerceAtLeast(1)
        val repositories = EntryPointAccessors.fromApplication(context.applicationContext, Repositories::class.java)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                seed(repositories, facets)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun seed(repositories: Repositories, facetCount: Int) {
        val settingsRepository = repositories.settingsRepository()
        val facetRepository = repositories.facetRepository()
        val placementRepository = repositories.placementRepository()
        val appRepository = repositories.appRepository()
        settingsRepository.setOnboardingCompleted(true)
        settingsRepository.setDefaultsSeeded(true)
        settingsRepository.markCoachMarkSeen(HOME_GESTURES_COACH_MARK_ID)
        settingsRepository.markCoachMarkSeen(HOME_SET_DEFAULT_PROMPT_ID)

        val apps = appRepository.getInstalledApps()
        seedGlobalLists(placementRepository, apps)
        while (facetRepository.observeFacets().first().size < facetCount) facetRepository.addFacet()
        val facets = facetRepository.observeFacets().first()
        settingsRepository.setActiveFacetId(facets.first().id)
        facets.forEachIndexed { index, facet ->
            val target = PlacementTarget(PlacementList.FAVORITES, facet.id)
            if (placementRepository.observeItems(target).first().isEmpty()) {
                apps.rotated(index * FAVORITES_PER_FACET).take(FAVORITES_PER_FACET).forEachIndexed { position, app ->
                    placementRepository.addApp(target, app, position)
                }
            }
            facetRepository.setOverridingFavorites(facet.id, true)
        }
    }

    private suspend fun seedGlobalLists(placementRepository: PlacementRepository, apps: List<AppInfo>) {
        if (placementRepository.observeItems(PlacementTarget.GlobalFavorites).first().isEmpty()) {
            apps.take(FAVORITES_PER_FACET).forEachIndexed { position, app -> placementRepository.addApp(PlacementTarget.GlobalFavorites, app, position) }
        }
        if (placementRepository.observeItems(PlacementTarget.GlobalDock).first().isEmpty()) {
            apps.take(DOCK_APPS).forEachIndexed { position, app -> placementRepository.addApp(PlacementTarget.GlobalDock, app, position) }
        }
    }

    private fun <T> List<T>.rotated(by: Int): List<T> = if (isEmpty()) this else drop(by % size) + take(by % size)

    private companion object {
        const val EXTRA_FACETS = "facets"
        const val FAVORITES_PER_FACET = 6
        const val DOCK_APPS = 5
    }
}
