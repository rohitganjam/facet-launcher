package com.facetlauncher.app.ui.nativewidget

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NativeWidgetMenuButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(type: NativeWidgetType, size: DpSize, ref: NativeWidgetRef?, onConfigure: (NativeWidgetRef) -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                NativeWidget(type = type, size = size, state = NativeWidgetState(), actions = NativeWidgetActions(onConfigure = onConfigure), ref = ref)
            }
        }
    }

    @Test
    fun aMediumAgendaHasAMenuButtonThatOpensItsSettings() {
        // Given a medium agenda on the Hub
        var opened: NativeWidgetRef? = null
        show(NativeWidgetType.AGENDA, DpSize(300.dp, 160.dp), NativeWidgetRef.Home(3), onConfigure = { opened = it })

        // When tapping its 3-dot button
        composeRule.onNodeWithTag("native_widget_menu").performClick()

        // Then its settings open
        assertEquals(NativeWidgetRef.Home(3), opened)
    }

    @Test
    fun aSmallAgendaHasNoMenuButton() {
        show(NativeWidgetType.AGENDA, DpSize(300.dp, 80.dp), NativeWidgetRef.Hub(-1))

        composeRule.onNodeWithTag("native_widget_menu").assertDoesNotExist()
    }

    @Test
    fun aWeatherWidgetHasNoMenuButtonOfItsOwn() {
        // Its settings are in the weather sheet a tap opens
        show(NativeWidgetType.WEATHER, DpSize(300.dp, 160.dp), NativeWidgetRef.Hub(-1))

        composeRule.onNodeWithTag("native_widget_menu").assertDoesNotExist()
    }

    @Test
    fun anAgendaOffTheHubHasNoMenuButton() {
        show(NativeWidgetType.AGENDA, DpSize(300.dp, 160.dp), ref = null)

        composeRule.onNodeWithTag("native_widget_menu").assertDoesNotExist()
    }
}
