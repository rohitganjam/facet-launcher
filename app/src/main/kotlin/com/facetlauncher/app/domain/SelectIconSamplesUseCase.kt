package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject

/** How many apps each icon style or pack card on the Icons screen previews. */
const val ICON_SAMPLE_COUNT = 4

/**
 * The apps an Icons screen card previews: the user's own Home apps first (Favorites, then Dock), then this
 * device's default apps (see [SelectPreviewAppsUseCase]), then the rest of the drawer, so a card is full even
 * with an empty Home. [accept] narrows the candidates, for a pack card that only shows apps the pack covers.
 */
class SelectIconSamplesUseCase @Inject constructor(private val selectPreviewApps: SelectPreviewAppsUseCase) {
    operator fun invoke(
        homeApps: List<AppInfo>,
        installedApps: List<AppInfo>,
        defaultPackages: List<String>,
        count: Int = ICON_SAMPLE_COUNT,
        accept: (AppInfo) -> Boolean = { true },
    ): List<AppInfo> =
        (homeApps + selectPreviewApps(installedApps, defaultPackages, installedApps.size))
            .asSequence()
            .filter { it.icon != null }
            .distinctBy { it.packageName }
            .filter(accept)
            .take(count)
            .toList()
}
