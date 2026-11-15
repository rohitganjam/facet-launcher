package com.facetlauncher.app.domain

import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.PackIconMatch
import com.facetlauncher.app.data.model.matchApps
import javax.inject.Inject

/** Sets or clears one app's custom icon. A pack icon is snapshotted as it is picked, so it outlives the pack. */
class ChangeAppIconUseCase @Inject constructor(
    private val iconPackRepository: IconPackRepository,
    private val iconOverrideRepository: IconOverrideRepository,
) {
    /** [app] takes pack [PackIconMatch.packPackage]'s icon [match]. */
    suspend fun usePackIcon(app: AppInfo, match: PackIconMatch) {
        val snapshot = match.drawableName?.let { iconPackRepository.drawIcon(match.packPackage, it) }
            ?: match.calendarPrefix?.let { iconPackRepository.drawIcon(match.packPackage, "${it}1") }
        iconOverrideRepository.setPackIcon(app, match.packPackage, match.drawableName, match.calendarPrefix, snapshot)
    }

    /** [app] shows its own icon, whatever pack is applied. */
    suspend fun useOwnIcon(app: AppInfo) = iconOverrideRepository.setOwnIcon(app)

    /** [app] goes back to the icon the launcher would draw for it. */
    suspend fun reset(app: AppInfo) {
        iconOverrideRepository.getAll().matchApps(listOf(app))[app.listKey]?.let { iconOverrideRepository.reset(it) }
    }

    suspend fun reset(override: IconOverride) = iconOverrideRepository.reset(override)

    suspend fun resetAll() = iconOverrideRepository.resetAll()
}
