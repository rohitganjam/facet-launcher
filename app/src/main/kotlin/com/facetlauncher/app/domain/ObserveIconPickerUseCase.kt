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
import kotlinx.coroutines.flow.mapLatest

/** One installed pack as the Icons screen shows it. [samples] carry the pack's icons; empty when it covers none of the candidates. */
data class IconPackOption(val pack: IconPackInfo, val samples: List<AppInfo>, val coveredApps: Int)

/** What the Icons screen's style and pack cards preview. [builtInSamples] always carry the apps' own icons, even while a pack is applied. */
data class IconPickerState(
    val builtInSamples: List<AppInfo> = emptyList(),
    val packs: List<IconPackOption> = emptyList(),
    val totalApps: Int = 0,
    val isLoading: Boolean = true,
)

/** The Icons screen's card previews: the built-in styles' samples and every installed pack with its own. */
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
    ) { favorites, dock, installed -> (favorites + dock).singleApps() to installed }
        .mapLatest { (homeApps, installed) ->
            val defaults = defaultAppRepository.getDefaultAppPackages()
            val builtIn = selectIconSamples(homeApps, installed, defaults).map { appRepository.withOwnIcon(it) }
            val installedPackages = installed.mapTo(HashSet()) { it.packageName }
            val packs = iconPackRepository.getInstalledPacks().map { info ->
                val icons = iconPackRepository.load(info.packageName, installedPackages)
                if (icons == null) {
                    IconPackOption(info, samples = emptyList(), coveredApps = 0)
                } else {
                    val covered = { app: AppInfo -> icons.covers(app.packageName, app.activityName) }
                    val samples = selectIconSamples(homeApps, installed, defaults, accept = covered).mapNotNull(icons::applyTo)
                    IconPackOption(info, samples, coveredApps = installed.distinctBy { it.packageName }.count(covered))
                }
            }
            IconPickerState(builtInSamples = builtIn, packs = packs, totalApps = installed.distinctBy { it.packageName }.size, isLoading = false)
        }

    private fun List<PlacedItem>.singleApps(): List<AppInfo> = filterIsInstance<PlacedItem.SingleApp>().map { it.app }
}
