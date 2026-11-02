package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest

/** One installed pack as the Icons screen shows it. [coveredApps] is known only for the applied pack, once it has loaded. */
data class IconPackOption(val pack: IconPackInfo, val coveredApps: Int? = null, val isLoading: Boolean = false)

/** What the Icons screen's style and pack cards show. [builtInSamples] always carry the apps' own icons, even while a pack is applied. */
data class IconPickerState(
    val builtInSamples: List<AppInfo> = emptyList(),
    val packs: List<IconPackOption> = emptyList(),
    val totalApps: Int = 0,
    val isLoading: Boolean = true,
)

/**
 * The Icons screen's cards: the built-in styles' samples and every installed pack, by its own app icon. No pack is
 * read here except the applied one, for its coverage, so opening the screen costs only a package query.
 */
class ObserveIconPickerUseCase @Inject constructor(
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val appRepository: AppRepository,
    private val iconPackRepository: IconPackRepository,
    private val defaultAppRepository: DefaultAppRepository,
    private val selectIconSamples: SelectIconSamplesUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<IconPickerState> = combine(
        observePlacedItems.items(PlacementTarget.GlobalFavorites),
        observePlacedItems.items(PlacementTarget.GlobalDock),
        getInstalledApps.observe(),
        iconPackRepository.selection,
    ) { favorites, dock, installed, selection -> Triple((favorites + dock).singleApps(), installed, selection?.packageName) }
        .transformLatest { (homeApps, installed, applied) ->
            val builtIn = selectIconSamples(homeApps, installed, defaultAppRepository.getDefaultAppPackages()).map { appRepository.withOwnIcon(it) }
            val packs = iconPackRepository.getInstalledPacks()
            val totalApps = installed.distinctBy { it.packageName }.size
            fun state(coverage: (IconPackInfo) -> IconPackOption) = IconPickerState(builtIn, packs.map(coverage), totalApps, isLoading = false)
            // The cards first, the applied pack loading, then its coverage once it has been read.
            emit(state { IconPackOption(it, isLoading = it.packageName == applied) })
            if (applied != null && packs.any { it.packageName == applied }) {
                val icons = iconPackRepository.load(applied, installed.mapTo(HashSet()) { it.packageName })
                val covered = icons?.let { pack -> installed.distinctBy { it.packageName }.count { pack.covers(it.packageName, it.activityName) } }
                emit(state { IconPackOption(it, coveredApps = covered.takeIf { _ -> it.packageName == applied }) })
            }
        }

    private fun List<PlacedItem>.singleApps(): List<AppInfo> = filterIsInstance<PlacedItem.SingleApp>().map { it.app }
}
