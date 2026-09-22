package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.facetlauncher.app.data.widget.AppWidgetRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ClockWidgetHostControllerTest {

    @Test
    fun `defaultSizeDp reads the provider's own declared minWidth and minHeight`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val info = AppWidgetProviderInfo().apply { minWidth = 180; minHeight = 90 }
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(info)
        val controller = ClockWidgetHostController(appWidgetRepository)

        assertEquals(180 to 90, controller.defaultSizeDp(7))
    }

    @Test
    fun `defaultSizeDp is null for an orphaned widget id`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(null)
        val controller = ClockWidgetHostController(appWidgetRepository)

        assertNull(controller.defaultSizeDp(7))
    }

    @Test
    fun `createHostView returns the repository's host view for a resolvable provider`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val info = AppWidgetProviderInfo()
        val context = mock(Context::class.java)
        val hostView = mock(AppWidgetHostView::class.java)
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(info)
        `when`(appWidgetRepository.createHostView(context, 7, info)).thenReturn(hostView)
        val controller = ClockWidgetHostController(appWidgetRepository)

        val result = controller.createHostView(context, 7)

        // Same instance the repository handed back — confirms the pass-through, not a copy/wrap.
        assertEquals(hostView, result)
    }

    @Test
    fun `createHostView is null for an orphaned widget id`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(null)
        val controller = ClockWidgetHostController(appWidgetRepository)

        assertNull(controller.createHostView(mock(Context::class.java), 7))
    }

    @Test
    fun `updateSize delegates straight to the repository`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val controller = ClockWidgetHostController(appWidgetRepository)

        controller.updateSize(7, 180, 90)

        verify(appWidgetRepository).updateWidgetSize(7, 180, 90)
    }

    @Test
    fun `resizeFloorDp prefers the provider's own declared minResizeWidth and minResizeHeight`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        val info = AppWidgetProviderInfo().apply {
            minWidth = 180; minHeight = 90
            minResizeWidth = 90; minResizeHeight = 40
        }
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(info)
        val controller = ClockWidgetHostController(appWidgetRepository)

        assertEquals(90 to 40, controller.resizeFloorDp(7))
    }

    @Test
    fun `resizeFloorDp is null for an orphaned widget id`() {
        val appWidgetRepository = mock(AppWidgetRepository::class.java)
        `when`(appWidgetRepository.getAppWidgetInfo(7)).thenReturn(null)
        val controller = ClockWidgetHostController(appWidgetRepository)

        assertNull(controller.resizeFloorDp(7))
    }
}

class ClockWidgetResizeFloorDpTest {

    @Test
    fun `prefers minResizeWidth and minResizeHeight when both are declared`() {
        assertEquals(90 to 40, clockWidgetResizeFloorDp(minWidth = 180, minHeight = 90, minResizeWidth = 90, minResizeHeight = 40))
    }

    @Test
    fun `falls back to minWidth and minHeight when minResize values are undeclared (zero)`() {
        assertEquals(180 to 90, clockWidgetResizeFloorDp(minWidth = 180, minHeight = 90, minResizeWidth = 0, minResizeHeight = 0))
    }

    @Test
    fun `falls back per-axis independently, not all-or-nothing`() {
        // A provider that only declared a resize floor for width, not height.
        assertEquals(90 to 90, clockWidgetResizeFloorDp(minWidth = 180, minHeight = 90, minResizeWidth = 90, minResizeHeight = 0))
    }
}
