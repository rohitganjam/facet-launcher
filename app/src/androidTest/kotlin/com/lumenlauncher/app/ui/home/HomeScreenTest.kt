package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun apps(count: Int) = (1..count).map {
        AppInfo(packageName = "com.example.app$it", activityName = ".Main", label = "App $it", icon = null)
    }

    @Test
    fun showsFirstFiveAppsAsFavoritesAndDockAppsSeparately() {
        // Given 9 apps, the first 5 as favorites and the rest as the (independently-sourced) dock
        val all = apps(9)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = all.take(5), dockApps = all.drop(5), onAppClick = {})
            }
        }

        // Then App 1..5 render as favorites rows (visible label text)
        for (i in 1..5) {
            composeRule.onNodeWithText("App $i").assertExists()
        }
        // And App 6..9 render as dock icons (content description, no visible label text)
        for (i in 6..9) {
            composeRule.onNodeWithContentDescription("App $i").assertExists()
        }
    }

    @Test
    fun tappingAFavoriteRowReportsThatApp() {
        // Given a favorite row for "App 2"
        var clicked: AppInfo? = null
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(5), dockApps = emptyList(), onAppClick = { clicked = it })
            }
        }

        // When it is tapped
        composeRule.onNodeWithText("App 2").performClick()

        // Then the callback receives that exact AppInfo
        assertEquals("com.example.app2", clicked?.packageName)
    }

    @Test
    fun emptyDockIsOmittedEntirelyRatherThanShownAsAnEmptyRow() {
        // Given no dock apps (the dock floor was removed — 0 is now a valid dock size)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(3), dockApps = emptyList(), onAppClick = {})
            }
        }

        // Then the dock row isn't rendered at all
        composeRule.onNodeWithTag("home_dock_row").assertDoesNotExist()
    }

    @Test
    fun dockTextModeRendersLabelsInsteadOfIcons() {
        // Given a dock in Text display mode
        val dockApps = apps(4)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = emptyList(),
                    dockApps = dockApps,
                    dockDisplayMode = DockDisplayMode.TEXT,
                    onAppClick = {},
                )
            }
        }

        // Then dock apps render as visible text labels, not just icon content descriptions
        dockApps.forEach { app -> composeRule.onNodeWithText(app.label).assertExists() }
    }

    @Test
    fun sectionLabelReflectsTheActiveListContentMode() {
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = emptyList(), dockApps = emptyList(), onAppClick = {}, listContentMode = ListContentMode.RECENTS)
            }
        }

        composeRule.onNodeWithText("RECENTS").assertExists()
        composeRule.onNodeWithText("FAVORITES").assertDoesNotExist()
    }

    @Test
    fun usageAccessPromptReplacesTheListAndFiresItsCallbackOnTap() {
        // Given Recents mode with usage access not yet granted
        var clicked = false
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(3),
                    dockApps = emptyList(),
                    onAppClick = {},
                    listContentMode = ListContentMode.RECENTS,
                    showUsageAccessPrompt = true,
                    onUsageAccessPromptClick = { clicked = true },
                )
            }
        }

        // Then the strip renders instead of any app rows
        composeRule.onNodeWithTag("home_usage_access_strip").assertExists()
        composeRule.onNodeWithText("App 1").assertDoesNotExist()

        // And tapping it fires the callback
        composeRule.onNodeWithTag("home_usage_access_strip").performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun longPressingAFavoriteRowOpensTheContextMenu() {
        // Given a favorite row
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {})
            }
        }
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When it's long-pressed (F12)
        composeRule.onNodeWithText("App 1").performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }

    @Test
    fun rendersWithoutCrashingUnderDarkMode() {
        // Given the system is in dark mode (Phase 5's dark color scheme)
        val all = apps(9)
        composeRule.setContent {
            val darkConfiguration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
            }
            CompositionLocalProvider(LocalConfiguration provides darkConfiguration) {
                LumenLauncherTheme {
                    HomeScreen(appListItems = all.take(5), dockApps = all.drop(5), onAppClick = {})
                }
            }
        }

        // Then it renders its content normally — no crash, real content present
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithContentDescription("App 6").assertExists()
    }
}
