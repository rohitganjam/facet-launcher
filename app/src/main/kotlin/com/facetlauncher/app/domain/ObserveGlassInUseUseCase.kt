package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Whether any glass panel is switched on anywhere: a facet's dock or apps list, the media row, a clock, or a widget (Facet's own widgets
 * draw glass unless set to off). Only then does a missing or out-of-date wallpaper copy matter, since glass is what samples it.
 */
class ObserveGlassInUseUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
) {
    operator fun invoke(): Flow<Boolean> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        widgetPlacementRepository.observeAll(),
    ) { settings, facets, placements -> glassInUse(settings.mediaPlayerGlass, facets, placements) }.distinctUntilChanged()

    internal companion object {
        fun glassInUse(mediaPlayerGlass: GlassStyle, facets: List<Facet>, placements: List<WidgetPlacementEntity>): Boolean =
            mediaPlayerGlass != GlassStyle.OFF ||
                facets.any { facet ->
                    facet.dockGlass != GlassStyle.OFF || facet.appsListGlass != GlassStyle.OFF ||
                        facet.clockStack.any { item -> decodeNativeWidgetConfig(item.nativeWidgetConfig).drawsGlass(item.kind == ClockStackItemKind.NATIVE_WIDGET) }
                } ||
                placements.any { it.nativeWidgetType != null && decodeNativeWidgetConfig(it.nativeWidgetConfig).drawsGlass(isNativeWidget = true) }

        /** A native widget's panel defaults to frost when it has no style of its own; a clock's glass is off unless chosen. */
        private fun NativeWidgetConfig.drawsGlass(isNativeWidget: Boolean): Boolean =
            (clock?.glass ?: GlassStyle.OFF) != GlassStyle.OFF || (isNativeWidget && (glass ?: NativeWidgetConfig.DEFAULT_GLASS) != GlassStyle.OFF)
    }
}
