package com.facetlauncher.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The whole long-press sheet at the text sizes a user can reach: the system font scale (up to 2x)
 * times the app's own Text size (up to 1.45x). At every size each action must still be reachable
 * and the header buttons must keep a full tap target.
 */
class AppContextMenuScalingTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val systemFontScale by lazy { SystemFontScale(composeRule.activity) }

    @After
    fun restoreSystemFontScale() {
        composeRule.runOnUiThread { systemFontScale.restore() }
    }

    private val app = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Messages", icon = null, profile = AppProfile.WORK)
    private val screenHeightDp: Float
        get() = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.let { it.heightPixels / it.density }

    private fun notifications(count: Int) = (1..count).map {
        NotificationInfo(
            packageName = app.packageName,
            silent = false,
            key = "k$it",
            title = "Sender $it",
            text = "A message that is long enough to wrap onto a second line when the text is large, number $it",
            postTime = 1L,
        )
    }

    private fun setContent(
        systemFontScaleValue: Float = 1f,
        appTextSize: FontScaleOption = FontScaleOption.DEFAULT,
        notificationCount: Int = 6,
        app: AppInfo = this.app,
        folderCandidates: List<Folder>? = emptyList(),
        onFavoritesAction: () -> Unit = {},
    ) {
        composeRule.runOnUiThread { systemFontScale.set(systemFontScaleValue) }
        composeRule.setContent {
            FacetLauncherTheme(fontScaleOption = appTextSize) {
                AppContextMenu(
                    app = app,
                    expanded = true,
                    onDismissRequest = {},
                    onRequestQuickAddState = {
                        QuickAddState(
                            favoritesAction = QuickPlacementAction.Add(facetName = null),
                            dockAction = QuickPlacementAction.Remove(facetName = "Work"),
                        )
                    },
                    onFavoritesAction = { _, _ -> onFavoritesAction() },
                    folderCandidates = folderCandidates?.toImmutableList(),
                    appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState(notifications(notificationCount))) }),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("app_context_menu_add_to_dock").assertExists() }.isSuccess
        }
        composeRule.waitForIdle()
    }

    private fun assertReachable(vararg tags: String) {
        tags.forEach { tag -> composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed() }
    }

    private val everyAction = arrayOf(
        "app_context_menu_app_info",
        "app_context_menu_uninstall",
        "app_context_menu_add_to_favorites",
        "app_context_menu_add_to_dock",
        "app_context_menu_add_to_folder",
    )

    private fun tilesAreSideBySide(): Boolean {
        val favorites = composeRule.onNodeWithTag("app_context_menu_add_to_favorites").getBoundsInRoot()
        val dock = composeRule.onNodeWithTag("app_context_menu_add_to_dock").getBoundsInRoot()
        return favorites.top == dock.top
    }

    @Test
    fun atDefaultTextEverythingIsReachableAndTheActionsAreTiles() {
        setContent()

        assertReachable(*everyAction)
        assertTrue(tilesAreSideBySide())
    }

    @Test
    fun atTheSmallestInAppTextTheActionsStayTiles() {
        setContent(appTextSize = FontScaleOption.SMALL)

        assertReachable(*everyAction)
        assertTrue(tilesAreSideBySide())
    }

    @Test
    fun atLargeInAppTextEverythingIsStillReachable() {
        setContent(appTextSize = FontScaleOption.LARGE)

        assertReachable(*everyAction)
    }

    @Test
    fun atExtraLargeInAppTextEverythingIsStillReachable() {
        setContent(appTextSize = FontScaleOption.EXTRA_LARGE)

        assertReachable(*everyAction)
    }

    @Test
    fun atTheLargestInAppTextEverythingIsStillReachable() {
        // Whether this is tiles or rows depends on the phone's width; the measured check decides, so only reachability is fixed
        setContent(appTextSize = FontScaleOption.HUGE)

        assertReachable(*everyAction)
    }

    @Test
    fun atTheLargestSystemFontScaleTheActionsBecomeRowsAndStayReachable() {
        setContent(systemFontScaleValue = 2f)

        assertReachable(*everyAction)
        assertTrue(!tilesAreSideBySide())
    }

    @Test
    fun withBothScalesAtTheirMaximumEverythingIsStillReachable() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE)

        assertReachable(*everyAction)
        assertTrue(!tilesAreSideBySide())
    }

    @Test
    fun withBothScalesAtTheirMaximumAndNoNotificationsEverythingIsStillReachable() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE, notificationCount = 0)

        assertReachable(*everyAction)
    }

    @Test
    fun theNotificationWindowNeverTakesMoreThanAThirdOfTheScreen() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE)

        composeRule.onNodeWithTag("app_notifications_list").performScrollTo()
        val height = composeRule.onNodeWithTag("app_notifications_list").getBoundsInRoot().height.value
        assertTrue("window $height dp is over a third of $screenHeightDp dp", height <= screenHeightDp * 0.33f + 1f)
    }

    private fun lineCount(tag: String): Int {
        val result = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(result)
        return result.first().lineCount
    }

    @Test
    fun theAppNameStaysOnOneLineAtTheLargestInAppText() {
        setContent(appTextSize = FontScaleOption.HUGE)

        assertEquals(1, lineCount("app_context_menu_name"))
    }

    @Test
    fun theAppNameStaysOnOneLineAtOnePointFiveSystemScale() {
        setContent(systemFontScaleValue = 1.5f)

        assertEquals(1, lineCount("app_context_menu_name"))
    }

    @Test
    fun scopePillsAreCappedSoTheyStayCompactAtTheLargestText() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE)

        // The Dock action's "Work" pill, asked for at 2.9x, is held to about 1.3x
        val pill = composeRule.onNodeWithText("Work", useUnmergedTree = true).performScrollTo().getBoundsInRoot()
        assertTrue("work pill is ${pill.height} tall", pill.height.value <= 30f)
    }

    @Test
    fun theNotificationsHeadingStaysOnOneLineAtTheLargestText() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE)

        composeRule.onNodeWithText("Notifications · 6").assertExists()
        val result = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        composeRule.onNodeWithText("Notifications · 6").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(result)
        assertEquals(1, result.first().lineCount)
    }

    @Test
    fun theInfoAndUninstallButtonsAreAtLeast48dpAtDefaultText() {
        setContent()

        assertTrue(composeRule.onNodeWithTag("app_context_menu_app_info").getBoundsInRoot().height.value >= 48f)
        assertTrue(composeRule.onNodeWithTag("app_context_menu_uninstall").getBoundsInRoot().height.value >= 48f)
    }

    @Test
    fun theInfoAndUninstallButtonsAreAtLeast48dpAtTheLargestText() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE)

        assertTrue(composeRule.onNodeWithTag("app_context_menu_app_info").getBoundsInRoot().height.value >= 48f)
        assertTrue(composeRule.onNodeWithTag("app_context_menu_uninstall").getBoundsInRoot().height.value >= 48f)
    }

    @Test
    fun aVeryLongAppNameStillLeavesBothHeaderButtonsOnScreen() {
        setContent(
            systemFontScaleValue = 2f,
            appTextSize = FontScaleOption.HUGE,
            app = app.copy(label = "An application with a really long name that keeps going and going"),
        )

        composeRule.onNodeWithTag("app_context_menu_app_info").assertIsDisplayed()
        composeRule.onNodeWithTag("app_context_menu_uninstall").assertIsDisplayed()
    }

    @Test
    fun theFolderPageStillWorksAtTheLargestText() {
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE, folderCandidates = listOf(Folder(id = 1L, name = "Games", apps = emptyList())))

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performScrollTo().performClick()

        composeRule.onNodeWithTag("app_context_menu_create_folder").assertIsDisplayed()
        composeRule.onNodeWithText("Games").assertIsDisplayed()
    }

    @Test
    fun theFolderMenuStillReachesRenameAndBothActionsAtTheLargestText() {
        composeRule.runOnUiThread { systemFontScale.set(2f) }
        composeRule.setContent {
            FacetLauncherTheme(fontScaleOption = FontScaleOption.HUGE) {
                FolderTileContextMenu(
                    folder = Folder(id = 7L, name = "A folder with a rather long name indeed", apps = emptyList()),
                    expanded = true,
                    onDismissRequest = {},
                    onRename = { _, _ -> },
                    onRequestQuickAddState = {
                        QuickAddState(
                            favoritesAction = QuickPlacementAction.Add(facetName = null),
                            dockAction = QuickPlacementAction.Remove(facetName = "Work"),
                        )
                    },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeRule.onNodeWithTag("folder_tile_context_menu_add_to_dock").assertExists() }.isSuccess
        }

        listOf("folder_tile_context_menu_rename", "folder_tile_context_menu_add_to_favorites", "folder_tile_context_menu_add_to_dock").forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun tappingAnActionAtTheLargestTextStillFiresIt() {
        var fired = 0
        setContent(systemFontScaleValue = 2f, appTextSize = FontScaleOption.HUGE, onFavoritesAction = { fired++ })

        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").performScrollTo().performClick()

        assertEquals(1, fired)
    }
}
