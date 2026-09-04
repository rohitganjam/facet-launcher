package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
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
    fun leftPositionRendersTheIconBeforeTheLabel() {
        // Given the default Left position
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = AppRowPosition.LEFT)
            }
        }

        // Then the icon renders before (further left than) the label — normal reading order
        val iconLeft = composeRule.onNodeWithTag("home_app_icon_com.example.app1").fetchSemanticsNode().boundsInRoot.left
        val labelLeft = composeRule.onNodeWithTag("home_app_label_com.example.app1").fetchSemanticsNode().boundsInRoot.left
        assert(iconLeft < labelLeft)
    }

    @Test
    fun rightPositionRendersTheLabelBeforeTheIconAndPacksTheRowAgainstTheRightEdge() {
        // Given Right position
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = AppRowPosition.RIGHT)
            }
        }

        // Then the internal order flips — label renders before (further left than) the icon
        val iconNode = composeRule.onNodeWithTag("home_app_icon_com.example.app1").fetchSemanticsNode()
        val labelLeft = composeRule.onNodeWithTag("home_app_label_com.example.app1").fetchSemanticsNode().boundsInRoot.left
        assert(labelLeft < iconNode.boundsInRoot.left)

        // And the icon — now the row's trailing element — sits in the right portion of the
        // screen rather than packed to the left, i.e. the whole row hugs the right edge, not
        // just a reversed order while staying left-anchored
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        assert(iconNode.boundsInRoot.right > rootWidth * 0.7f)
    }

    @Test
    fun rightPositionAlsoRightAlignsTheSectionHeading() {
        // Given Left position (default) — the "FAVORITES" heading hugs the left edge
        var appRowPosition by mutableStateOf(AppRowPosition.LEFT)
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(appListItems = apps(1), dockApps = emptyList(), onAppClick = {}, appRowPosition = appRowPosition)
            }
        }
        val rootWidth = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.right
        val leftPositionHeadingLeft = composeRule.onNodeWithText("FAVORITES").fetchSemanticsNode().boundsInRoot.left
        assert(leftPositionHeadingLeft < rootWidth * 0.3f)

        // When switching to Right position
        appRowPosition = AppRowPosition.RIGHT
        composeRule.waitForIdle()

        // Then the heading moves with the app rows, now hugging the right edge instead
        val rightPositionHeadingRight = composeRule.onNodeWithText("FAVORITES").fetchSemanticsNode().boundsInRoot.right
        assert(rightPositionHeadingRight > rootWidth * 0.7f)
    }

    @Test
    fun iconOnlyPresentationHidesTheLabelButKeepsTheIconAccessible() {
        // Given Icon Only presentation
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                )
            }
        }

        // Then the visible label text is gone, but the icon still renders and carries the app's
        // name for accessibility (mirroring the Dock's own icons-only mode)
        composeRule.onNodeWithText("App 1").assertDoesNotExist()
        composeRule.onNodeWithTag("home_app_icon_com.example.app1").assertExists()
        composeRule.onNodeWithContentDescription("App 1").assertExists()
    }

    @Test
    fun textOnlyPresentationHidesTheIconButKeepsTheLabel() {
        // Given Text Only presentation
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = {},
                    appRowPresentation = AppRowPresentation.TEXT_ONLY,
                )
            }
        }

        // Then the label still renders, but the icon does not
        composeRule.onNodeWithText("App 1").assertExists()
        composeRule.onNodeWithTag("home_app_icon_com.example.app1").assertDoesNotExist()
    }

    @Test
    fun tappingARowStillLaunchesTheAppUnderIconOnlyPresentation() {
        // Given Icon Only presentation — the row has no visible text to tap, only the icon area
        var clicked: AppInfo? = null
        composeRule.setContent {
            LumenLauncherTheme {
                HomeScreen(
                    appListItems = apps(1),
                    dockApps = emptyList(),
                    onAppClick = { clicked = it },
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                )
            }
        }

        // When tapping the icon (its own node, now the row's only visible content)
        composeRule.onNodeWithTag("home_app_icon_com.example.app1").performClick()

        // Then the click still reaches the row's own click handler
        assertEquals("com.example.app1", clicked?.packageName)
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
