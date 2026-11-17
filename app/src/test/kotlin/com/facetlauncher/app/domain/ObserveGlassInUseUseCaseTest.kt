package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.encode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveGlassInUseUseCaseTest {

    private fun facet(dock: GlassStyle = GlassStyle.OFF, list: GlassStyle = GlassStyle.OFF, stack: List<ClockStackItem> = emptyList()) =
        Facet(name = "A", position = 0, clockStack = stack, dockGlass = dock, appsListGlass = list)

    private fun item(kind: ClockStackItemKind, config: NativeWidgetConfig?) =
        ClockStackItem(id = 1, facetId = 1, position = 0, kind = kind, nativeWidgetType = NativeWidgetType.WEATHER.takeIf { kind == ClockStackItemKind.NATIVE_WIDGET }, nativeWidgetConfig = config?.encode())

    private fun placement(config: NativeWidgetConfig?) =
        WidgetPlacementEntity(appWidgetId = -1, providerPackageName = "p", providerClassName = "c", row = 0, col = 0, colSpan = 1, rowSpan = 1, nativeWidgetType = NativeWidgetType.AGENDA, nativeWidgetConfig = config?.encode())

    private fun inUse(media: GlassStyle = GlassStyle.OFF, facets: List<Facet> = emptyList(), placements: List<WidgetPlacementEntity> = emptyList()) =
        ObserveGlassInUseUseCase.glassInUse(media, facets, placements)

    @Test
    fun `nothing with glass means glass is not in use`() {
        // Given facets with every glass off and no widgets
        // Then glass is not in use
        assertFalse(inUse(facets = listOf(facet(), facet(stack = listOf(item(ClockStackItemKind.CLOCK, null))))))
    }

    @Test
    fun `glass on a facet's dock or apps list counts`() {
        // Given one facet with glass behind the dock, another behind the apps list
        // Then each counts
        assertTrue(inUse(facets = listOf(facet(), facet(dock = GlassStyle.FROST))))
        assertTrue(inUse(facets = listOf(facet(list = GlassStyle.CLEAR_GLASS))))
    }

    @Test
    fun `glass on the media row counts`() {
        // Given only the media row has glass
        assertTrue(inUse(media = GlassStyle.DARK_FROST, facets = listOf(facet())))
    }

    @Test
    fun `a clock with glass counts`() {
        // Given the facet's clock page has glass
        val clock = NativeWidgetConfig(clock = ClockWidgetConfig(glass = GlassStyle.BRIGHT))
        assertTrue(inUse(facets = listOf(facet(stack = listOf(item(ClockStackItemKind.CLOCK, clock))))))
    }

    @Test
    fun `a widget with no style of its own draws the default frost`() {
        // Given a widget page and a Hub widget that follow the default
        assertTrue(inUse(facets = listOf(facet(stack = listOf(item(ClockStackItemKind.NATIVE_WIDGET, null))))))
        assertTrue(inUse(placements = listOf(placement(null))))
    }

    @Test
    fun `a widget set to no glass does not count`() {
        // Given widgets whose glass is switched off
        val off = NativeWidgetConfig(glass = GlassStyle.OFF)
        assertFalse(inUse(facets = listOf(facet(stack = listOf(item(ClockStackItemKind.NATIVE_WIDGET, off)))), placements = listOf(placement(off))))
    }
}
