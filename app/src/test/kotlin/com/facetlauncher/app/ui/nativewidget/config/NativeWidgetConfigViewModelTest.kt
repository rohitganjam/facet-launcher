package com.facetlauncher.app.ui.nativewidget.config

import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ObserveNativeWidgetSettingsUseCase
import com.facetlauncher.app.domain.SetNativeWidgetConfigUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class NativeWidgetConfigViewModelTest {

    private val permission = mock(CalendarPermissionRepository::class.java)
    private val calendars = mock(CalendarRepository::class.java)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `reloading after the permission is granted lists the calendars`() = runTest {
        // Given a ViewModel created while calendar access was off
        val settings = mock(SettingsRepository::class.java)
        `when`(settings.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(permission.isGranted()).thenReturn(false)
        `when`(calendars.getSelectableCalendars()).thenReturn(listOf(CalendarInfo(id = "1", displayName = "Home", accountName = "me")))
        val viewModel = NativeWidgetConfigViewModel(
            settings, calendars, permission, mock(ObserveNativeWidgetSettingsUseCase::class.java), mock(SetNativeWidgetConfigUseCase::class.java),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        assertEquals(emptyList<CalendarInfo>(), viewModel.uiState.value.calendars)

        // When access is granted and the sheet reopens
        `when`(permission.isGranted()).thenReturn(true)
        viewModel.reloadCalendars()
        runCurrent()

        // Then the calendar shows up
        assertEquals(listOf("1"), viewModel.uiState.value.calendars.map { it.id })
    }
}
