package com.facetlauncher.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppNotificationsSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dismissCalls = mutableListOf<List<String>>()
    private var state by mutableStateOf(AppNotificationsState())

    private fun notification(key: String, clearable: Boolean = true) =
        NotificationInfo(packageName = "p", silent = false, key = key, title = "Title $key", text = "Body $key", postTime = 1L, isClearable = clearable)

    /** [accept] stands in for the listener service: when true it removes the keys from the list, like `onNotificationRemoved`. */
    private fun setContent(notifications: List<NotificationInfo>, accept: Boolean = true) {
        state = AppNotificationsState(notifications)
        composeRule.setContent {
            FacetLauncherTheme {
                AppNotificationsSection(
                    state = state,
                    onOpen = {},
                    onDismiss = { keys ->
                        dismissCalls += keys
                        if (accept) state = state.copy(notifications = state.notifications.filter { it.key !in keys })
                        accept
                    },
                    onEnableAccess = {},
                )
            }
        }
    }

    private fun clearAll() {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_clear_all").assertIsDisplayed() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notifications_clear_all").performClick()
    }

    private fun waitForEmptyState() = composeRule.waitUntil(timeoutMillis = 5_000) {
        runCatching { composeRule.onNodeWithTag("app_notifications_empty").assertExists() }.isSuccess
    }

    @Test
    fun clearAllSwipesTheCardsAwayAndDismissesEachOnce() {
        // Given two clearable notifications
        setContent(listOf(notification("k1"), notification("k2")))

        // When Clear all is tapped
        clearAll()
        waitForEmptyState()

        // Then each key reached the listener exactly once and the list gave way to the empty state
        assertEquals(listOf("k1", "k2"), dismissCalls.flatten().sorted())
        composeRule.onNodeWithTag("app_notifications_empty").assertIsDisplayed()
    }

    @Test
    fun clearAllDismissesCardsBelowTheWindowToo() {
        // Given more notifications than the window shows
        val all = (1..6).map { notification("k$it") }
        setContent(all)

        // When Clear all is tapped
        clearAll()
        waitForEmptyState()

        // Then every key was dismissed once, whether it swiped or was off-screen
        assertEquals(all.map { it.key }, dismissCalls.flatten().sorted())
    }

    @Test
    fun clearAllLeavesAPersistentNotificationInTheList() {
        // Given a persistent notification between two clearable ones
        setContent(listOf(notification("k1"), notification("ongoing", clearable = false), notification("k2")))

        // When Clear all is tapped
        clearAll()
        composeRule.waitUntil(timeoutMillis = 5_000) { state.notifications.size == 1 }

        // Then only the persistent one remains
        assertEquals(listOf("ongoing"), state.notifications.map { it.key })
        composeRule.onNodeWithTag("app_notification_ongoing").assertIsDisplayed()
    }

    @Test
    fun aCardComesBackWhenTheListenerRefusesTheDismiss() {
        // Given a listener that refuses every dismiss
        setContent(listOf(notification("k1")), accept = false)

        // When Clear all is tapped
        clearAll()
        composeRule.waitUntil(timeoutMillis = 5_000) { dismissCalls.isNotEmpty() }
        composeRule.waitForIdle()

        // Then the card is back on screen rather than left swiped away
        composeRule.onNodeWithTag("app_notification_k1").assertIsDisplayed()
    }

    @Test
    fun theListShrinksToTheEmptyStateAsTheLastCardIsRemoved() {
        // Given one notification
        setContent(listOf(notification("k1")))

        // When the listener removes it
        composeRule.runOnIdle { state = state.copy(notifications = emptyList()) }
        waitForEmptyState()

        // Then the empty state shows and the card is gone
        composeRule.onNodeWithTag("app_notifications_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("app_notification_k1").assertDoesNotExist()
    }
}
