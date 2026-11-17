package com.facetlauncher.app.ui.appicon

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import com.facetlauncher.app.domain.CustomIconRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CustomIconsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun row(pkg: String, label: String, packLabel: String?) = CustomIconRow(
        app = AppInfo(packageName = pkg, activityName = "$pkg.Main", label = label, icon = null),
        override = IconOverride(pkg, "$pkg.Main", 0, AppProfile.PERSONAL, if (packLabel == null) IconOverrideSource.OWN else IconOverrideSource.PACK),
        packLabel = packLabel,
    )

    private val rows = listOf(row("com.example.music", "Music", "Arcticons"), row("com.example.maps", "Maps", null))

    private fun setContent(rows: List<CustomIconRow>, onReset: (CustomIconRow) -> Unit = {}, onResetAll: () -> Unit = {}) {
        composeRule.setContent {
            FacetLauncherTheme {
                CustomIconsContent(
                    uiState = CustomIconsUiState(rows.toImmutableList(), isLoading = false),
                    onBack = {},
                    onReset = onReset,
                    onResetAll = onResetAll,
                )
            }
        }
    }

    @Test
    fun listsEachAppWithWhereItsIconComesFrom() {
        // Given two apps with custom icons, one from a pack and one showing its own icon
        setContent(rows)

        // Then both rows show, each with its icon's source
        composeRule.onNodeWithTag("custom_icons_row_com.example.music").assertIsDisplayed()
        composeRule.onNodeWithText("Arcticons").assertIsDisplayed()
        composeRule.onNodeWithText("App's own icon").assertIsDisplayed()
    }

    @Test
    fun resetResetsOnlyThatApp() {
        // Given the list
        var reset: CustomIconRow? = null
        setContent(rows, onReset = { reset = it })

        // When Maps' Reset is tapped
        composeRule.onNodeWithTag("custom_icons_reset_com.example.maps").performClick()

        // Then only Maps is reset
        composeRule.runOnIdle { assertEquals("com.example.maps", reset?.app?.packageName) }
    }

    @Test
    fun resetAllAsksFirstAndResetsOnConfirm() {
        // Given the list
        var resets = 0
        setContent(rows, onResetAll = { resets++ })

        // When Reset all is tapped
        composeRule.onNodeWithTag("custom_icons_reset_all").performClick()

        // Then it asks before resetting
        composeRule.onNodeWithTag("confirm_dialog").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, resets) }

        // When confirmed
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()

        // Then everything is reset and the dialog closes
        composeRule.runOnIdle { assertEquals(1, resets) }
        composeRule.onNodeWithTag("confirm_dialog").assertDoesNotExist()
    }

    @Test
    fun cancellingResetAllResetsNothing() {
        // Given the Reset all confirmation
        var resets = 0
        setContent(rows, onResetAll = { resets++ })
        composeRule.onNodeWithTag("custom_icons_reset_all").performClick()

        // When cancelled
        composeRule.onNodeWithTag("confirm_dialog_cancel").performClick()

        // Then nothing is reset
        composeRule.runOnIdle { assertEquals(0, resets) }
    }

    @Test
    fun withNoCustomIconsItSaysSo() {
        // Given no custom icons
        setContent(persistentListOf())

        // Then the empty message shows, with no Reset all
        composeRule.onNodeWithTag("custom_icons_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("custom_icons_reset_all").assertDoesNotExist()
    }
}
