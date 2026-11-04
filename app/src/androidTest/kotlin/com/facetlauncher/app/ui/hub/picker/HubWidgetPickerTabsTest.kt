package com.facetlauncher.app.ui.hub.picker

import android.content.ComponentName
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.WidgetProviderOption
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HubWidgetPickerTabsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val options = listOf(
        WidgetProviderOption(ComponentName("pkg.clock", ".W"), "Clock", "Clock widget", 2, 2),
        WidgetProviderOption(ComponentName("pkg.cal", ".W"), "Calendar", "Month", 4, 4, profile = AppProfile.WORK),
    )

    private fun show(uiState: HubWidgetPickerUiState, onTabChange: (WidgetPickerTab) -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                HubWidgetPickerContent(uiState = uiState, onQueryChange = {}, onProviderSelect = {}, onBack = {}, onTabChange = onTabChange)
            }
        }
    }

    @Test
    fun theTabRowShowsOnlyWhenAWorkProfileHasWidgets() {
        // Given a picker with only personal widgets
        show(pickerState(options.take(1), "", WidgetPickerTab.PERSONAL, 3))

        // Then there is no tab row
        composeRule.onNodeWithTag("hub_widget_picker_tab_WORK").assertDoesNotExist()
    }

    @Test
    fun choosingTheWorkTabAsksForIt() {
        // Given a picker with personal and work widgets
        var chosen: WidgetPickerTab? = null
        show(pickerState(options, "", WidgetPickerTab.PERSONAL, 3), onTabChange = { chosen = it })

        // When tapping the Work tab
        composeRule.onNodeWithTag("hub_widget_picker_tab_WORK").performClick()

        // Then the Work tab is asked for
        assertEquals(WidgetPickerTab.WORK, chosen)
    }

    @Test
    fun theWorkTabListsOnlyWorkWidgets() {
        // Given the Work tab of a picker with both
        show(pickerState(options, "", WidgetPickerTab.WORK, 3))

        // Then the work Calendar lists and the personal Clock does not
        composeRule.onNodeWithTag("hub_widget_option_work_badge", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Calendar").assertExists()
        composeRule.onNodeWithText("Clock").assertDoesNotExist()
    }
}
