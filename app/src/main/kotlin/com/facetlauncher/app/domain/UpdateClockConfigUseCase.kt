package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetConfig
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Changes one setting of a facet's clock: [change] is applied to the clock's settings in force (so a clock that has none stored yet
 * keeps what it shows) and the result is stored on its page. Does nothing for a facet with no clock.
 */
class UpdateClockConfigUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val clockStackRepository: ClockStackRepository,
) {
    suspend operator fun invoke(facetId: Long, change: (ClockWidgetConfig) -> ClockWidgetConfig) {
        val facet = facetRepository.getById(facetId) ?: return
        val page = facet.clockStack.firstOrNull { it.kind == ClockStackItemKind.CLOCK && it.id != 0L } ?: return
        val current = effectiveClockConfig(facet, settingsRepository.settings.first())
        clockStackRepository.setConfig(page.id, NativeWidgetConfig(clock = change(current)))
    }
}
