package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.encode
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class MaterializeClockConfigsUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val useCase = MaterializeClockConfigsUseCase(settingsRepository, facetRepository, clockStackRepository)

    private val launcherSettings = LauncherSettings(clockTemplateId = ClockTemplateId.entries.last(), use24HourTime = true)

    private fun page(id: Long, config: String? = null, kind: ClockStackItemKind = ClockStackItemKind.CLOCK) =
        ClockStackItem(id = id, facetId = 1, position = 0, kind = kind, nativeWidgetConfig = config)

    private suspend fun given(vararg facets: Facet) {
        `when`(settingsRepository.settings).thenReturn(flowOf(launcherSettings))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(facets.toList()))
    }

    @Test
    fun `a clock with no settings of its own gets the ones its facet shows today`() = runTest {
        // Given a facet that inherits the launcher's clock, with a clock page that has no settings
        given(Facet(id = 1, name = "Home", position = 0, clockStack = listOf(page(7))))

        // When the pass runs
        useCase()

        // Then the page is written the launcher's clock, as it shows now
        verify(clockStackRepository).setConfig(7, NativeWidgetConfig(clock = legacyClockConfig(Facet(id = 1, name = "Home", position = 0), launcherSettings)))
    }

    @Test
    fun `a clock that already has settings is left alone`() = runTest {
        // Given a clock page that already carries its own settings
        val own = NativeWidgetConfig(clock = ClockWidgetConfig(use24HourTime = true)).encode()
        given(Facet(id = 1, name = "Home", position = 0, clockStack = listOf(page(7, own))))

        // When the pass runs again
        useCase()

        // Then nothing is written
        verify(clockStackRepository, never()).setConfig(7, NativeWidgetConfig())
    }

    @Test
    fun `a facet with its own clock keeps its own look`() = runTest {
        // Given a facet that overrides the clock with a different template
        val facet = Facet(
            id = 1, name = "Work", position = 0, overrideClock = true, clockTemplateId = ClockTemplateId.entries.first(),
            clockStack = listOf(page(3)),
        )
        given(facet)

        // When the pass runs
        useCase()

        // Then its page gets its own template, not the launcher's
        verify(clockStackRepository).setConfig(3, NativeWidgetConfig(clock = legacyClockConfig(facet, launcherSettings)))
        assertEquals(ClockTemplateId.entries.first(), legacyClockConfig(facet, launcherSettings).templateId)
    }

    @Test
    fun `widget pages and the stand-in clock of a facet with no rows are not touched`() = runTest {
        // Given a facet with a Weather page, and one whose only page is the unsaved stand-in clock (id 0)
        given(
            Facet(id = 1, name = "Home", position = 0, clockStack = listOf(page(4, kind = ClockStackItemKind.NATIVE_WIDGET).copy(nativeWidgetType = NativeWidgetType.WEATHER))),
            Facet(id = 2, name = "Bare", position = 1, clockStack = listOf(page(0))),
        )

        // When the pass runs
        useCase()

        // Then no setting is written anywhere
        verify(clockStackRepository, never()).setConfig(4, NativeWidgetConfig())
        verify(clockStackRepository, never()).setConfig(0, NativeWidgetConfig())
    }
}
