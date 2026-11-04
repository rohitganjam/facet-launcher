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
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class UpdateClockConfigUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val useCase = UpdateClockConfigUseCase(settingsRepository, facetRepository, clockStackRepository)

    private fun page(id: Long, kind: ClockStackItemKind, config: String? = null) =
        ClockStackItem(id = id, facetId = 1, position = 0, kind = kind, nativeWidgetConfig = config)

    private suspend fun given(facet: Facet?, settings: LauncherSettings = LauncherSettings()) {
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        `when`(facetRepository.getById(1)).thenReturn(facet)
    }

    @Test
    fun `the change is applied to the clock's own settings and stored on its page`() = runTest {
        // Given a clock with its own settings
        val own = ClockWidgetConfig(use24HourTime = true, showWeather = true)
        given(Facet(id = 1, name = "Home", position = 0, clockStack = listOf(page(4, ClockStackItemKind.CLOCK, NativeWidgetConfig(clock = own).encode()))))

        // When one setting changes
        useCase(1) { it.copy(showAlarm = false) }

        // Then the page stores the clock with just that change
        verify(clockStackRepository).setConfig(4, NativeWidgetConfig(clock = own.copy(showAlarm = false)))
    }

    @Test
    fun `a clock with nothing stored yet keeps what it shows when it is first changed`() = runTest {
        // Given a facet that follows the launcher's clock, and a clock page with no settings
        val launcher = LauncherSettings(clockTemplateId = ClockTemplateId.entries.last(), use24HourTime = true)
        given(Facet(id = 1, name = "Home", position = 0, clockStack = listOf(page(4, ClockStackItemKind.CLOCK))), launcher)

        // When a setting changes
        useCase(1) { it.copy(showBattery = false) }

        // Then the launcher's look the clock was showing is stored with the change, not the built-in look
        verify(clockStackRepository).setConfig(4, NativeWidgetConfig(clock = legacyClockConfig(Facet(id = 1, name = "Home", position = 0), launcher).copy(showBattery = false)))
    }

    @Test
    fun `nothing is stored for a facet that is missing, has no clock page, or only the unsaved stand-in`() = runTest {
        // Given a missing facet, a facet with only a widget, and a facet whose only page is the unsaved stand-in clock
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.getById(1)).thenReturn(null)
        `when`(facetRepository.getById(2)).thenReturn(Facet(id = 2, name = "Widgets", position = 1, clockStack = listOf(page(7, ClockStackItemKind.NATIVE_WIDGET).copy(nativeWidgetType = NativeWidgetType.WEATHER))))
        `when`(facetRepository.getById(3)).thenReturn(Facet(id = 3, name = "Bare", position = 2, clockStack = listOf(page(0, ClockStackItemKind.CLOCK))))

        // When changing each
        useCase(1) { it.copy(showAlarm = false) }
        useCase(2) { it.copy(showAlarm = false) }
        useCase(3) { it.copy(showAlarm = false) }

        // Then no page is written
        verify(clockStackRepository, never()).setConfig(0, NativeWidgetConfig(clock = ClockWidgetConfig(showAlarm = false)))
        verify(clockStackRepository, never()).setConfig(7, NativeWidgetConfig(clock = ClockWidgetConfig(showAlarm = false)))
    }
}
