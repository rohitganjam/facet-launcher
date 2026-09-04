package com.lumenlauncher.app.ui.drawer

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.lumenlauncher.app.data.model.ConnectionDetail
import com.lumenlauncher.app.data.model.ConnectionOption
import com.lumenlauncher.app.data.model.ContactConnection
import com.lumenlauncher.app.data.model.ContactConnectionType
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Rule
import org.junit.Test

class ContactConnectionsSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val callConnection = ContactConnection(
        ContactConnectionType.CALL,
        "Call",
        null,
        ConnectionDetail.Multiple(
            listOf(
                ConnectionOption("555-1234", "Mobile", Intent(Intent.ACTION_DIAL, Uri.parse("tel:555-1234"))),
                ConnectionOption("555-5678", "Work", Intent(Intent.ACTION_DIAL, Uri.parse("tel:555-5678"))),
            ),
        ),
    )

    private fun setContent() {
        composeRule.setContent {
            LumenLauncherTheme {
                ContactConnectionsSheet(
                    contactName = "Jane Doe",
                    connections = listOf(callConnection),
                    onConnectionClick = {},
                    onViewContactClick = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun tappingAMultipleOptionsRowOpensTheDisambiguationPage() {
        // Given the sheet with a Call connection that has two numbers
        setContent()

        // When tapping the Call row (its subtitle reads "Multiple options available")
        composeRule.onNodeWithTag("contact_connection_call_call").performClick()

        // Then the disambiguation page slides in — its own back chevron and both options render,
        // each keyed by its type label (Mobile/Work), not the raw number
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_back").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Mobile").assertExists()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun systemBackWhileDisambiguatingReturnsToTheMainListInsteadOfClosingTheSheet() {
        // Given the sheet, drilled into the Call connection's disambiguation page
        setContent()
        composeRule.onNodeWithTag("contact_connection_call_call").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_back").assertExists() }.isSuccess
        }

        // When pressing the system back button
        Espresso.pressBack()

        // Then it returns to the main connections list — the disambiguation page's own back
        // chevron is gone, and the contact's own rows (name, Call) are visible again — the sheet
        // itself was never dismissed, unlike a back press with no open disambiguation would do
        // one level further up (see chat history: previously this fell through to the launcher's
        // own back handling instead of being caught here).
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("contact_connections_back").assertDoesNotExist() }.isSuccess
        }
        composeRule.onNodeWithText("Jane Doe").assertExists()
        composeRule.onNodeWithTag("contact_connection_call_call").assertExists()
    }
}
