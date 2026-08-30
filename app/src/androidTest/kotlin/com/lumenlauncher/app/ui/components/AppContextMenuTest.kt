package com.lumenlauncher.app.ui.components

import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
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
    ) {
        composeRule.setContent {
            LumenLauncherTheme {
                AppContextMenu(
                    app = app,
                    expanded = true,
                    onDismissRequest = onDismissRequest,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                )
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
}
