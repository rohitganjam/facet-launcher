package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.domain.SwitchFacetToNativeClockUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ClockWidgetFacetControllerTest {

    @Test
    fun `switchToNativeClock delegates to the use case with the given facet`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val switchFacetToNativeClock = mock(SwitchFacetToNativeClockUseCase::class.java)
        val controller = ClockWidgetFacetController(facetRepository, switchFacetToNativeClock)
        val facet = FacetEntity(id = 1L, name = "Work", position = 0, clockWidgetAppWidgetId = 42)

        controller.switchToNativeClock(facet)

        verify(switchFacetToNativeClock).invoke(facet)
    }

    @Test
    fun `commitSize delegates straight to the repository`() = runTest {
        val facetRepository = mock(FacetRepository::class.java)
        val switchFacetToNativeClock = mock(SwitchFacetToNativeClockUseCase::class.java)
        val controller = ClockWidgetFacetController(facetRepository, switchFacetToNativeClock)
        val facet = FacetEntity(id = 1L, name = "Work", position = 0, clockWidgetAppWidgetId = 42)

        controller.commitSize(facet, 180, 90)

        verify(facetRepository).setClockWidgetSize(facet, 180, 90)
    }
}
