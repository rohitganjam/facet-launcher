package com.facetlauncher.app.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.junit.Rule
import org.junit.Test

class FolderContentsSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = AppInfo(packageName = "com.example.app1", activityName = ".Main", label = "App 1", icon = null)
    private val folder = Folder(id = 1L, name = "Folder", apps = listOf(app))

    private fun setContent(
        presentation: DrawerPresentation,
        onDismissRequest: () -> Unit = {},
        homePressedEvent: SharedFlow<Unit>? = null,
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    FolderContentsSheet(
                        folder = folder,
                        onDismissRequest = onDismissRequest,
                        onAppClick = {},
                        presentation = presentation,
                        onRemoveFromFolder = { _, _ -> },
                    )
                }
            }
        }
    }

    @Test
    fun homePressedEventDismissesTheSheet() {
        // Given the folder sheet is showing
        var dismissed = false
        val homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        setContent(DrawerPresentation.LIST, onDismissRequest = { dismissed = true }, homePressedEvent = homePressedEvent)

        // When Home is pressed
        homePressedEvent.tryEmit(Unit)

        // Then the sheet asks to be dismissed
        composeRule.waitUntil(timeoutMillis = 3_000) { dismissed }
    }

    @Test
    fun longPressingAnAppRowInAFolderOpensTheContextMenu() {
        // Given List presentation
        setContent(DrawerPresentation.LIST)
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When the app row is long-pressed
        composeRule.onNodeWithTag("folder_contents_app_${app.packageName}", useUnmergedTree = true).performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }

    @Test
    fun theMenuOpensAtTheLongPressThresholdWithoutWaitingForRelease() {
        // Given List presentation
        setContent(DrawerPresentation.LIST)
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When the row is held past the long-press threshold without lifting the finger
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("folder_contents_app_${app.packageName}", useUnmergedTree = true).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        // Then the menu has already opened, without the finger having lifted
        composeRule.onNodeWithTag("app_context_menu").assertExists()

        composeRule.onNodeWithTag("folder_contents_app_${app.packageName}", useUnmergedTree = true).performTouchInput { up() }
    }

    @Test
    fun longPressingAGridTileInAFolderOpensTheContextMenu() {
        // Given Grid presentation
        setContent(DrawerPresentation.GRID)
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()

        // When the tile is long-pressed
        composeRule.onNodeWithTag("folder_contents_grid_tile_${app.packageName}", useUnmergedTree = true).performTouchInput { longClick() }

        // Then the context menu opens
        composeRule.onNodeWithTag("app_context_menu").assertExists()
    }
}
