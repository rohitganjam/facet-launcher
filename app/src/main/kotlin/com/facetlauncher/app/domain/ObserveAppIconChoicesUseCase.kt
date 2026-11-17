package com.facetlauncher.app.domain

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.PackIconMatch
import com.facetlauncher.app.data.model.matchApps
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** One app's icon from one installed pack, as the Change icon page offers it. */
data class PackIconChoice(val pack: IconPackInfo, val match: PackIconMatch, val icon: ImageBitmap)

/** Which icon the Change icon page marks as the app's current one. */
sealed interface IconChoiceKey {
    data object Own : IconChoiceKey
    data class Pack(val packPackage: String, val drawableName: String?) : IconChoiceKey
}

/** What the Change icon page shows for one app. [app] is the live app, so its icon follows a change. */
data class AppIconChoices(
    val app: AppInfo,
    /** The app's own icon, even while a pack is applied. */
    val ownIcon: AppInfo? = null,
    val packChoices: List<PackIconChoice> = emptyList(),
    /** Every installed pack, for browsing. */
    val packs: List<IconPackInfo> = emptyList(),
    /** The app's custom icon, null when it shows the default. */
    val override: IconOverride? = null,
    val selected: IconChoiceKey? = null,
    val isLoading: Boolean = true,
) {
    /** The pack [override] draws from, for the preview's caption. */
    val overridePack: IconPackInfo? get() = override?.packPackage?.let { pkg -> packs.firstOrNull { it.packageName == pkg } }
}

/**
 * The Change icon page's choices for [AppInfo]: its own icon, its entry in every installed pack that lists it, and every
 * pack to browse, with the current choice marked. Packs are read once per open; the app and its custom icon stay live.
 */
class ObserveAppIconChoicesUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val iconPackRepository: IconPackRepository,
    private val iconOverrideRepository: IconOverrideRepository,
    private val clock: Clock,
) {
    operator fun invoke(app: AppInfo): Flow<AppIconChoices> {
        val liveApp = appRepository.observeInstalledApps().map { apps -> apps.firstOrNull { it.listKey == app.listKey } ?: app }.distinctUntilChanged()
        val override = iconOverrideRepository.overrides.map { it.matchApps(listOf(app))[app.listKey] }.distinctUntilChanged()
        val packs = flow {
            emit(PackOptions(isLoading = true))
            emit(loadPackOptions(app))
        }
        return combine(liveApp, override, packs, iconPackRepository.selection) { current, custom, options, selection ->
            val appliedChoice = options.choices.firstOrNull { it.pack.packageName == selection?.packageName }
            val selected = when {
                custom?.source == IconOverrideSource.OWN -> IconChoiceKey.Own
                custom?.source == IconOverrideSource.PACK -> IconChoiceKey.Pack(custom.packPackage.orEmpty(), custom.drawableName)
                appliedChoice != null -> IconChoiceKey.Pack(appliedChoice.pack.packageName, appliedChoice.match.drawableName)
                options.isLoading -> null
                else -> IconChoiceKey.Own
            }
            AppIconChoices(current, options.ownIcon, options.choices, options.packs, custom, selected, options.isLoading)
        }
    }

    private class PackOptions(
        val ownIcon: AppInfo? = null,
        val choices: List<PackIconChoice> = emptyList(),
        val packs: List<IconPackInfo> = emptyList(),
        val isLoading: Boolean = false,
    )

    private suspend fun loadPackOptions(app: AppInfo): PackOptions = coroutineScope {
        val ownIcon = async { appRepository.withOwnIcon(app.copy(iconFromPack = true)) }
        val packs = iconPackRepository.getInstalledPacks()
        val day = LocalDate.now(clock.withZone(ZoneId.systemDefault())).dayOfMonth
        val choices = packs.map { pack ->
            async {
                val match = iconPackRepository.matchFor(pack.packageName, app.packageName, app.activityName) ?: return@async null
                val bitmap = iconPackRepository.drawIcon(pack.packageName, match.drawableFor(day))
                    ?: match.drawableName?.let { iconPackRepository.drawIcon(pack.packageName, it) }
                    ?: return@async null
                PackIconChoice(pack, match, bitmap.asImageBitmap())
            }
        }.awaitAll().filterNotNull()
        PackOptions(ownIcon.await(), choices, packs, isLoading = false)
    }
}
