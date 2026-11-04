package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Gives every clock page that has no settings of its own the ones it shows today ([legacyClockConfig]), so a clock stops depending on
 * the launcher-wide clock and calendar settings and on its facet's override switches. One pass, idempotent: a clock that has settings is left
 * alone, so it also covers a facet restored from an older backup and any clock page created before this ran.
 */
@Singleton
class MaterializeClockConfigsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val clockStackRepository: ClockStackRepository,
) {
    suspend operator fun invoke() {
        val settings = settingsRepository.settings.first()
        facetRepository.observeFacets().first().forEach { facet ->
            facet.clockStack
                .filter { it.id != 0L && it.kind == ClockStackItemKind.CLOCK && decodeNativeWidgetConfig(it.nativeWidgetConfig).clock == null }
                .forEach { page -> clockStackRepository.setConfig(page.id, NativeWidgetConfig(clock = legacyClockConfig(facet, settings))) }
        }
    }
}
