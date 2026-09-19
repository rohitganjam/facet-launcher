package com.facetlauncher.app.ui.settings

import android.app.AppOpsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PermissionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onNavigateToUsageAccessExplanation: () -> Unit = {},
        onNavigateToNotificationAccessExplanation: () -> Unit = {},
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "permissions-screen-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                PermissionsViewModel(
                    context,
                    settingsRepository,
                    FakeCalendarPermissionRepository(context, granted = false),
                    ContactPermissionRepository(context),
                    UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                    NotificationAccessRepository(context),
                )
            }
            FacetLauncherTheme {
                PermissionsScreen(
                    onBack = {},
                    onNavigateToUsageAccessExplanation = onNavigateToUsageAccessExplanation,
                    onNavigateToNotificationAccessExplanation = onNavigateToNotificationAccessExplanation,
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun headerRendersOutsideTheScrollingCard() {
        // Given the permissions screen — only 3 rows, too short to actually overflow the screen,
        // so this just confirms the pinned header (StickyHeaderLayout) renders correctly rather
        // than exercising a real scroll-regression case the way the longer Settings screen does
        setContent()

        // Then the header's title and back button are both present
        composeRule.onNodeWithText("Permissions").assertExists()
        composeRule.onNodeWithTag("back_button").assertExists()
    }

    @Test
    fun rendersAllFourPermissionsWithTheirExplanationAndUngrantedTurnOnAction() {
        // Given none of the four permissions are granted (fake Calendar repository, real
        // ungranted defaults for the other three — a fresh test/emulator instance never has
        // notification listener access granted either)
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

        composeRule.onNodeWithText("Notification access").assertExists()
        composeRule.onNodeWithText("Shows a dot or count badge on apps with active notifications.").assertExists()
        composeRule.onNodeWithTag("permission_turn_on_NOTIFICATION_ACCESS").assertExists().assertHasClickAction()
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

    @Test
    fun tappingTurnOnForNotificationAccessNavigatesToItsExplanationScreen() {
        // Given the permissions screen — Notification access is likewise a special-access
        // permission with no runtime dialog, reusing the same explanation screen Notification
        // Settings already routes to (see chat history)
        var navigated = false
        setContent(onNavigateToNotificationAccessExplanation = { navigated = true })

        // When tapping "Turn on" for Notification access
        composeRule.onNodeWithTag("permission_turn_on_NOTIFICATION_ACCESS").performClick()

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
                    context,
                    settingsRepository,
                    FakeCalendarPermissionRepository(context, granted = true),
                    ContactPermissionRepository(context),
                    UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                    NotificationAccessRepository(context),
                )
            }
            FacetLauncherTheme {
                PermissionsScreen(
                    onBack = {},
                    onNavigateToUsageAccessExplanation = {},
                    onNavigateToNotificationAccessExplanation = {},
                    viewModel = viewModel,
                )
            }
        }

        // Then its row shows "On" rather than a "Turn on" action
        composeRule.onNodeWithTag("permission_status_CALENDAR").assertExists()
        composeRule.onNodeWithTag("permission_turn_on_CALENDAR").assertDoesNotExist()
    }
}
