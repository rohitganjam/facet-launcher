package com.facetlauncher.app.ui.nativewidget

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test

class NativeWidgetMenuButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noWidgetDrawsAMenuButtonAnyMoreAtAnySizeOnTheHubOrHome() {
        // Given every widget at several sizes, with each kind of reference (Hub, Home and none)
        composeRule.setContent {
            FacetLauncherTheme {
                Column {
                    NativeWidgetType.entries.forEach { type ->
                        listOf(DpSize(300.dp, 80.dp), DpSize(300.dp, 160.dp), DpSize(300.dp, 300.dp)).forEach { size ->
                            listOf<NativeWidgetRef?>(NativeWidgetRef.Hub(-1), NativeWidgetRef.Home(3), null).forEach { ref ->
                                NativeWidget(type = type, size = size, state = NativeWidgetState(), actions = NativeWidgetActions(), ref = ref)
                            }
                        }
                    }
                }
            }
        }

        // Then none has a 3-dot button: a widget's settings are in its long-press menu (Home's widget sheet, the Hub's context menu)
        composeRule.onAllNodesWithTag("native_widget_menu").assertCountEquals(0)
    }
}
