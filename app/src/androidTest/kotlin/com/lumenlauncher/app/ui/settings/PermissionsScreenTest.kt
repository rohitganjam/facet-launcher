package com.lumenlauncher.app.ui.settings

import android.app.AppOpsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.ContactPermissionRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PermissionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(onNavigateToUsageAccessExplanation: () -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "permissions-screen-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                PermissionsViewModel(
                    settingsRepository,
                    FakeCalendarPermissionRepository(context, granted = false),
                    ContactPermissionRepository(context),
                    UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                )
            }
            LumenLauncherTheme {
                PermissionsScreen(
                    onBack = {},
                    onNavigateToUsageAccessExplanation = onNavigateToUsageAccessExplanation,
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun rendersAllThreePermissionsWithTheirExplanationAndUngrantedTurnOnAction() {
        // Given none of the three permissions are granted (fake Calendar repository, real
        // ungranted defaults for the other two)
        setContent()

        // Then each row renders with its title, its "why" subtitle, and a working "Turn on" action
        composeRule.onNodeWithText("Calendar").assertExists()
        composeRule.onNodeWithText("Shows today's events on the clock.").assertExists()
        composeRule.onNodeWithTag("permission_turn_on_CALENDAR").assertExists().assertHasClickAction()

        composeRule.onNodeWithText("Contacts").assertExists()
        composeRule.onNodeWithText(
            "Lets Drawer search show matching contacts with quick call/message/WhatsApp actions.",
        ).assertExists()
        composeRule.onNodeWithTag("permission_turn_on_CONTACTS").assertExists().assertHasClickAction()

        composeRule.onNodeWithText("Usage access").assertExists()
        composeRule.onNodeWithText("Powers the Recents and Most used app lists.").assertExists()
        composeRule.onNodeWithTag("permission_turn_on_USAGE_ACCESS").assertExists().assertHasClickAction()
    }

    @Test
    fun tappingTurnOnForUsageAccessNavigatesToItsExplanationScreen() {
        // Given the permissions screen
        var navigated = false
        setContent(onNavigateToUsageAccessExplanation = { navigated = true })

        // When tapping "Turn on" for Usage access — a special-access permission with no runtime
        // dialog of its own, so this always goes to the existing explanation screen instead
        composeRule.onNodeWithTag("permission_turn_on_USAGE_ACCESS").performClick()

        // Then it navigates there
        assertEquals(true, navigated)
    }
}

class PermissionsScreenGrantedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun grantedPermissionShowsOnInsteadOfTurnOn() {
        // Given READ_CALENDAR is already granted (fake repository, not a real device grant)
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "permissions-screen-granted-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                PermissionsViewModel(
                    settingsRepository,
                    FakeCalendarPermissionRepository(context, granted = true),
                    ContactPermissionRepository(context),
                    UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                )
            }
            LumenLauncherTheme {
                PermissionsScreen(onBack = {}, onNavigateToUsageAccessExplanation = {}, viewModel = viewModel)
            }
        }

        // Then its row shows "On" rather than a "Turn on" action
        composeRule.onNodeWithTag("permission_status_CALENDAR").assertExists()
        composeRule.onNodeWithTag("permission_turn_on_CALENDAR").assertDoesNotExist()
    }
}
