package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.matchApps
import java.text.Collator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

/** One installed app with a custom icon. [packLabel] names the pack it comes from; null for the app's own icon. */
data class CustomIconRow(val app: AppInfo, val override: IconOverride, val packLabel: String?)

/** The Custom icons screen: installed apps with a custom icon, by name. Restored rows for apps not installed yet are left out. */
class ObserveCustomIconsUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val iconOverrideRepository: IconOverrideRepository,
    private val iconPackRepository: IconPackRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CustomIconRow>> =
        combine(appRepository.observeInstalledApps(), iconOverrideRepository.overrides) { apps, overrides -> apps to overrides }
            .mapLatest { (apps, overrides) ->
                val visible = apps.filterNot { it.packageName == FACET_LAUNCHER_PACKAGE_NAME }
                val matched = overrides.matchApps(visible)
                val labels = if (matched.values.any { it.packPackage != null }) {
                    iconPackRepository.getInstalledPacks().associate { it.packageName to it.label }
                } else {
                    emptyMap()
                }
                val collator = Collator.getInstance()
                visible.mapNotNull { app -> matched[app.listKey]?.let { CustomIconRow(app, it, it.packPackage?.let { pkg -> labels[pkg] ?: pkg }) } }
                    .sortedWith { a, b -> collator.compare(a.app.label, b.app.label) }
            }
}
