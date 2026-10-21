package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.resolveFacetStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomePreviewStateTest {

    private val settings = LauncherSettings(
        activeFacetId = 1L,
        clockAlignment = ClockAlignment.RIGHT,
        clockScale = 1.2f,
        clockZoneHeightDp = 300f,
        appRowPosition = AppRowPosition.CENTER,
        appListVerticalAlignment = AppListVerticalAlignment.TOP,
        landscape = LandscapeOverrides(clockScale = 2.0f, appRowPosition = AppRowPosition.RIGHT),
    )

    private fun previewOf(facet: Facet?) = resolveFacetStyle(facet, settings).toPreviewState(
        favorites = emptyList(), dockItems = emptyList(), calendarEvents = emptyList(), calendarColors = emptyMap(),
        launcherFontOption = LauncherFontOption.SYSTEM, appLabelColorOption = ClockColorOption.THEME, homeAppsFontWeight = FontWeightOption.REGULAR,
    )

    @Test
    fun `a preview shows the same portrait clock and list placement as Home for every kind of facet`() {
        // Given facets that inherit, override the clock, override only some list fields and have never dragged the divider
        val facets = listOf(
            null,
            Facet(id = 1L, name = "Inherits", position = 0),
            Facet(id = 1L, name = "Overrides", position = 0, overrideClock = true, clockAlignment = ClockAlignment.LEFT, clockScale = 0.5f, clockZoneHeightDp = 120f),
            Facet(id = 1L, name = "Never dragged", position = 0, overrideClock = true, clockZoneHeightDp = null),
            Facet(id = 1L, name = "List", position = 0, appRowPosition = AppRowPosition.LEFT, appListVerticalAlignment = AppListVerticalAlignment.BOTTOM),
        )

        for (facet in facets) {
            // When Home resolves the same facet for portrait
            val home = HomeUiState(settings = settings, facets = listOfNotNull(facet))
            val clock = home.clockPlacement(HomeLayoutMode.PORTRAIT)
            val list = home.listAlignment(HomeLayoutMode.PORTRAIT)
            val preview = previewOf(facet)

            // Then the preview matches it field for field
            assertEquals(clock.alignment, preview.clockAlignment)
            assertEquals(clock.scale, preview.clockScale, 0f)
            assertEquals(clock.zoneHeightDp, preview.clockZoneHeightDp)
            assertEquals(list.rowPosition, preview.appRowPosition)
            assertEquals(list.verticalAlignment, preview.appListVerticalAlignment)
            assertEquals(list.columnAlignment, preview.appListColumnAlignment)
        }
    }

    @Test
    fun `a preview carries the facet's hosted clock widget and its saved size`() {
        val facet = Facet(id = 1L, name = "Widget", position = 0, clockWidgetAppWidgetId = 41, clockWidgetWidthDp = 160, clockWidgetHeightDp = 90)

        val widget = previewOf(facet).clockWidget

        assertEquals(PreviewClockWidget(41, 160 to 90), widget)
    }

    @Test
    fun `a facet without a hosted widget previews the native clock`() {
        assertNull(previewOf(Facet(id = 1L, name = "Native", position = 0)).clockWidget)
    }

    @Test
    fun `a widget that was never resized takes the provider's default size`() {
        val box = previewWidgetBox(PreviewClockWidget(41, null), defaultSizeDp = { 250 to 110 }, maxWidth = 400.dp)

        assertEquals(PreviewWidgetBox(41, 250.dp, 110.dp), box)
    }

    @Test
    fun `a saved widget size wins over the provider's default`() {
        val box = previewWidgetBox(PreviewClockWidget(41, 160 to 90), defaultSizeDp = { 250 to 110 }, maxWidth = 400.dp)

        assertEquals(PreviewWidgetBox(41, 160.dp, 90.dp), box)
    }

    @Test
    fun `a widget wider than the screen is kept within it`() {
        val box = previewWidgetBox(PreviewClockWidget(41, 600 to 90), defaultSizeDp = { null }, maxWidth = 324.dp)

        assertEquals(324.dp, box?.widthDp)
    }

    @Test
    fun `an orphaned widget with no saved size falls back to the native clock`() {
        assertNull(previewWidgetBox(PreviewClockWidget(41, null), defaultSizeDp = { null }, maxWidth = 400.dp))
        assertNull(previewWidgetBox(null, defaultSizeDp = { 250 to 110 }, maxWidth = 400.dp))
    }
}
