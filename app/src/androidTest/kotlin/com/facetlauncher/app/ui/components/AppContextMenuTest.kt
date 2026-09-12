package com.facetlauncher.app.ui.components

import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppContextMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    @Before
    fun setUp() = Intents.init()

    @After
    fun tearDown() = Intents.release()

    private fun setContent(
        onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
        onLaunchShortcut: (AppShortcut) -> Unit = {},
        onDismissRequest: () -> Unit = {},
        homePressedEvent: SharedFlow<Unit>? = null,
        addToFavoritesOverride: Boolean? = null,
        onAddToFavorites: (AppInfo) -> Unit = {},
        addToDockOverride: Boolean? = null,
        onAddToDock: (AppInfo) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    AppContextMenu(
                        app = app,
                        expanded = true,
                        onDismissRequest = onDismissRequest,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                        addToFavoritesOverride = addToFavoritesOverride,
                        onAddToFavorites = onAddToFavorites,
                        addToDockOverride = addToDockOverride,
                        onAddToDock = onAddToDock,
                    )
                }
            }
        }
    }

    @Test
    fun rendersHeaderAndCoreActionsWhenExpanded() {
        setContent()

        composeRule.onNodeWithText("Mail").assertExists()
        composeRule.onNodeWithTag("app_context_menu_app_info").assertExists()
        composeRule.onNodeWithTag("app_context_menu_uninstall").assertExists()
    }

    @Test
    fun tappingAppInfoLaunchesApplicationDetailsSettingsForThisPackage() {
        setContent()

        composeRule.onNodeWithTag("app_context_menu_app_info").performClick()

        Intents.intended(
            allOf(
                hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS),
                hasData(Uri.fromParts("package", app.packageName, null)),
            ),
        )
    }

    @Test
    fun tappingUninstallLaunchesActionDeleteForThisPackage() {
        setContent()

        composeRule.onNodeWithTag("app_context_menu_uninstall").performClick()

        Intents.intended(
            allOf(
                hasAction(android.content.Intent.ACTION_DELETE),
                hasData(Uri.fromParts("package", app.packageName, null)),
            ),
        )
    }

    @Test
    fun quickActionsSectionIsOmittedWhenTheAppPublishesNoShortcuts() {
        setContent(onRequestShortcuts = { emptyList() })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_menu_shortcut_compose").assertDoesNotExist()
    }

    @Test
    fun quickActionRowRendersAndFiresOnLaunchShortcutWhenTapped() {
        var launched: AppShortcut? = null
        val shortcut = AppShortcut(id = "compose", packageName = app.packageName, label = "Compose")
        var dismissed = false
        setContent(
            onRequestShortcuts = { listOf(shortcut) },
            onLaunchShortcut = { launched = it },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_context_menu_shortcut_compose").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Compose").assertExists()
        composeRule.onNodeWithTag("app_context_menu_shortcut_compose").performClick()

        assert(launched == shortcut)
        assert(dismissed)
    }

    @Test
    fun shortcutsBeyondTheCapAreNotShown() {
        val shortcuts = (1..5).map { AppShortcut(id = "shortcut_$it", packageName = app.packageName, label = "Shortcut $it") }
        setContent(onRequestShortcuts = { shortcuts })

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_3").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_4").assertDoesNotExist()
        composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_5").assertDoesNotExist()
    }

    @Test
    fun addToFavoritesRowIsOmittedWhenFavoritesAlreadyAtTheCap() {
        setContent(addToFavoritesOverride = null)

        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").assertDoesNotExist()
    }

    @Test
    fun addToFavoritesRowReadsAddToFavoritesAndFiresOnAddToFavoritesForTheLauncherWideDefault() {
        var added: AppInfo? = null
        var dismissed = false
        setContent(addToFavoritesOverride = false, onAddToFavorites = { added = it }, onDismissRequest = { dismissed = true })

        composeRule.onNodeWithText("Add to Favorites").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").performClick()

        assert(added == app)
        assert(dismissed)
    }

    @Test
    fun addToFavoritesRowReadsAddToProfileFavoritesWhenTheActiveProfileOverridesItsOwn() {
        setContent(addToFavoritesOverride = true)

        composeRule.onNodeWithText("Add to profile favorites").assertExists()
    }

    @Test
    fun addToDockRowIsOmittedWhenDockAlreadyAtTheCap() {
        setContent(addToDockOverride = null)

        composeRule.onNodeWithTag("app_context_menu_add_to_dock").assertDoesNotExist()
    }

    @Test
    fun addToDockRowReadsAddToDockAndFiresOnAddToDockForTheLauncherWideDefault() {
        var added: AppInfo? = null
        var dismissed = false
        setContent(addToDockOverride = false, onAddToDock = { added = it }, onDismissRequest = { dismissed = true })

        composeRule.onNodeWithText("Add to Dock").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_dock").performClick()

        assert(added == app)
        assert(dismissed)
    }

    @Test
    fun addToDockRowReadsAddToProfileDockWhenTheActiveProfileOverridesItsOwn() {
        setContent(addToDockOverride = true)

        composeRule.onNodeWithText("Add to profile dock").assertExists()
    }

    @Test
    fun systemBackDismissesTheSheet() {
        var dismissed = false
        setContent(onDismissRequest = { dismissed = true })

        Espresso.pressBack()

        assert(dismissed)
    }

    @Test
    fun homePressedEventDismissesTheSheet() {
        var dismissed = false
        val homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        setContent(onDismissRequest = { dismissed = true }, homePressedEvent = homePressedEvent)

        homePressedEvent.tryEmit(Unit)
        composeRule.waitUntil(timeoutMillis = 3_000) { dismissed }

        assert(dismissed)
    }
}
