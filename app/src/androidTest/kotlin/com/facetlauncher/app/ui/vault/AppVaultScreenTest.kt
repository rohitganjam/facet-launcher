package com.facetlauncher.app.ui.vault

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppVaultScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val chat = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Chat", icon = null)
    private val maps = AppInfo(packageName = "com.example.maps", activityName = ".Main", label = "Maps", icon = null)

    private fun show(state: AppVaultUiState, callbacks: AppVaultCallbacks = AppVaultCallbacks(), screenLock: Boolean = true) {
        composeRule.setContent {
            FacetLauncherTheme {
                AppVaultScreen(state, callbacks, SnackbarHostState(), screenLockAvailable = screenLock)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun theLockedVaultShowsNothing() {
        show(AppVaultUiState(stage = VaultStage.LOCKED))

        composeRule.onNodeWithTag("app_vault_intro").assertDoesNotExist()
        composeRule.onNodeWithTag("app_vault_pin_page").assertDoesNotExist()
        composeRule.onNodeWithTag("app_vault_list_page").assertDoesNotExist()
    }

    @Test
    fun theIntroOffersToSetAPin() {
        // Given the intro
        var started = false
        show(AppVaultUiState(stage = VaultStage.INTRO), AppVaultCallbacks(onStartSetPin = { started = true }))

        // When tapping Set PIN
        composeRule.onNodeWithTag("app_vault_set_pin").performClick()

        // Then the PIN flow starts
        assertEquals(true, started)
    }

    @Test
    fun tappingTheKeysSendsTheDigitsAndTheDotsShowTheCount() {
        // Given the PIN pad with two digits typed
        val typed = mutableListOf<Int>()
        show(AppVaultUiState(stage = VaultStage.UNLOCK, digitsEntered = 2), AppVaultCallbacks(onDigit = { typed += it }))

        // When tapping 4, 2 and 7
        listOf(4, 2, 7).forEach { composeRule.onNodeWithTag("app_vault_key_$it").performClick() }

        // Then those digits are reported, and the dots say 2 of 4
        assertEquals(listOf(4, 2, 7), typed)
        composeRule.onNodeWithTag("app_vault_pin_dots").assertContentDescriptionEquals("2/4")
    }

    @Test
    fun theUnlockPadOffersForgotPin() {
        var forgot = false
        show(AppVaultUiState(stage = VaultStage.UNLOCK), AppVaultCallbacks(onForgotPin = { forgot = true }))

        composeRule.onNodeWithTag("app_vault_forgot_pin").performClick()

        assertEquals(true, forgot)
    }

    @Test
    fun aLockedOutPadDisablesTheKeys() {
        // Given a pad locked out for 30 seconds
        val typed = mutableListOf<Int>()
        show(
            AppVaultUiState(stage = VaultStage.UNLOCK, feedback = PinFeedback.LOCKED_OUT, lockoutSecondsLeft = 30),
            AppVaultCallbacks(onDigit = { typed += it }),
        )

        // When tapping a key
        composeRule.onNodeWithTag("app_vault_key_1").performClick()

        // Then nothing is sent
        assertEquals(emptyList<Int>(), typed)
    }

    @Test
    fun theVaultListShowsItsAppsAndEachRowMenuHasOnlyAppInfoAndRemove() {
        // Given a vault with two apps
        var removed: AppInfo? = null
        var infoFor: AppInfo? = null
        show(
            AppVaultUiState(stage = VaultStage.LIST, apps = persistentListOf(chat, maps)),
            AppVaultCallbacks(onRemove = { removed = it }, onAppInfo = { infoFor = it }),
        )
        composeRule.onNodeWithText("Chat").assertExists()
        composeRule.onNodeWithText("Maps").assertExists()

        // When opening Chat's menu
        composeRule.onNodeWithTag("app_vault_row_menu_com.example.chat").performClick()

        // Then App info and Remove are offered, and Remove reports the app
        composeRule.onNodeWithTag("app_vault_app_info").assertExists()
        composeRule.onNodeWithTag("app_vault_remove").performClick()
        assertEquals(chat, removed)
        assertEquals(null, infoFor)
    }

    @Test
    fun anEmptyVaultSaysSoAndOffersAddApps() {
        var added = false
        show(AppVaultUiState(stage = VaultStage.LIST), AppVaultCallbacks(onAddApps = { added = true }))

        composeRule.onNodeWithTag("app_vault_empty").assertExists()
        composeRule.onNodeWithTag("app_vault_add_apps").performClick()

        assertEquals(true, added)
    }

    @Test
    fun theOverflowMenuOffersChangePinAndTurnOff() {
        var changed = false
        var turnedOff = false
        show(
            AppVaultUiState(stage = VaultStage.LIST),
            AppVaultCallbacks(onChangePin = { changed = true }, onTurnOff = { turnedOff = true }),
        )

        composeRule.onNodeWithTag("app_vault_overflow").performClick()
        composeRule.onNodeWithTag("app_vault_change_pin").performClick()
        composeRule.onNodeWithTag("app_vault_overflow").performClick()
        composeRule.onNodeWithTag("app_vault_turn_off").performClick()

        assertEquals(true, changed)
        assertEquals(true, turnedOff)
    }
}
