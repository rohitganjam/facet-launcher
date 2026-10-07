package com.facetlauncher.app.ui.settings.automation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AutomationRuleError
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.PairedBluetoothDevice
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek

class RuleEditorSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val facets = listOf(FacetEntity(id = 1, name = "Personal", position = 0), FacetEntity(id = 2, name = "Work", position = 1))
    private var current by mutableStateOf(RuleEditorState.forNew(facets, activeFacetId = 1))
    private var saved = 0
    private var deleted = 0
    private var dismissed = 0

    private var choices = DeviceChoices(bluetooth = listOf(PairedBluetoothDevice("AA:BB", "Car")), networks = listOf("Home", "Cafe"))
    private var changedTrigger: RuleEditorState? = null
    private var pro = true

    private fun show(state: RuleEditorState = current) {
        current = state
        composeRule.setContent {
            FacetLauncherTheme {
                RuleEditorSheet(
                    state = current,
                    choices = choices,
                    isPro = pro,
                    onChange = { transform -> current = current.edited { transform(this) } },
                    onChangeTrigger = { candidate ->
                        changedTrigger = candidate
                        current = candidate
                    },
                    onSave = { saved++ },
                    onDelete = { deleted++ },
                    onDismiss = { dismissed++ },
                )
            }
        }
    }

    @Test
    fun aNewRuleShowsWeekdaysSelectedAndTheDefaultEnding() {
        show()

        composeRule.onNodeWithTag("rule_editor_day_MONDAY").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_day_SATURDAY").assertIsNotSelected()
        composeRule.onNodeWithTag("rule_editor_end_RETURN").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_delete").assertDoesNotExist()
    }

    @Test
    fun tappingADayTogglesIt() {
        show()

        composeRule.onNodeWithTag("rule_editor_day_SATURDAY").performClick()
        composeRule.onNodeWithTag("rule_editor_day_MONDAY").performClick()

        composeRule.waitForIdle()
        val days = (current.trigger as AutomationTrigger.Schedule).days
        assertEquals(true, DayOfWeek.SATURDAY in days)
        assertEquals(false, DayOfWeek.MONDAY in days)
    }

    @Test
    fun noDaysSelectedShowsThePickADayMessage() {
        show(current.copy(errors = listOf(AutomationRuleError.NO_SCHEDULE_DAYS)))

        composeRule.onNodeWithTag("rule_editor_error_days").assertExists()
    }

    @Test
    fun theSwitchToRowHoldsItsFacetDropdownAndPickingAFacetSelectsIt() {
        show()
        composeRule.onNodeWithTag("rule_editor_end_RETURN").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_end_facet").assertExists()

        composeRule.onNodeWithTag("rule_editor_end_facet").performClick()
        // The current value and the menu option both read "Personal"; the option is the last match.
        composeRule.onAllNodesWithText("Personal").onLast().performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rule_editor_end_SWITCH").assertIsSelected()
        assertEquals(EndKind.SWITCH, current.endKind)
    }

    @Test
    fun theStartTimeIsPickedFromADialog() {
        show()

        composeRule.onNodeWithTag("rule_editor_from").performClick()
        composeRule.onNodeWithTag("rule_editor_time_dialog").assertExists()
        composeRule.onNodeWithTag("rule_editor_time_ok").performClick()

        composeRule.onNodeWithTag("rule_editor_time_dialog").assertDoesNotExist()
    }

    @Test
    fun saveAndCancelReportBackAndNothingIsSavedByCancel() {
        show()

        composeRule.onNodeWithTag("rule_editor_cancel").performClick()
        composeRule.onNodeWithTag("rule_editor_save").performClick()

        composeRule.waitForIdle()
        assertEquals(1, dismissed)
        assertEquals(1, saved)
    }

    @Test
    fun deletingAnExistingRuleAsksFirst() {
        show(current.copy(ruleId = 9L))

        composeRule.onNodeWithTag("rule_editor_delete").performClick()
        assertEquals(0, deleted)
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()

        composeRule.waitForIdle()
        assertEquals(1, deleted)
    }

    @Test
    fun aMissingFacetMessageIsShown() {
        show(current.copy(facetMissing = true))

        composeRule.onNodeWithTag("rule_editor_error_facet").assertExists()
    }

    private fun showAs(kind: TriggerKind) = show(current.withKind(kind))

    @Test
    fun theTypeDropdownOffersEveryTriggerAndReportsTheChoice() {
        show()

        composeRule.onNodeWithTag("rule_editor_when").performClick()
        composeRule.onNodeWithText("Headphones").performClick()

        composeRule.waitForIdle()
        assertEquals(TriggerKind.HEADPHONES, changedTrigger!!.trigger.kind())
    }

    @Test
    fun aBluetoothRuleShowsPolarityAndAChoosePlaceholder() {
        showAs(TriggerKind.BLUETOOTH)

        composeRule.onNodeWithTag("rule_editor_polarity_on").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_device").assertExists()
        composeRule.onNodeWithText("Choose a device").assertExists()
    }

    @Test
    fun pickingADeviceFillsTheRule() {
        showAs(TriggerKind.BLUETOOTH)

        composeRule.onNodeWithTag("rule_editor_device").performClick()
        composeRule.onNodeWithText("Car").performClick()

        composeRule.waitForIdle()
        assertEquals("AA:BB", (current.trigger as AutomationTrigger.Bluetooth).deviceAddress)
    }

    @Test
    fun noPairedDevicesExplainsWhatToDo() {
        choices = DeviceChoices()
        showAs(TriggerKind.BLUETOOTH)

        composeRule.onNodeWithText("No paired devices", substring = true).assertExists()
    }

    @Test
    fun aMissingDeviceShowsItsError() {
        showAs(TriggerKind.BLUETOOTH)
        current = current.copy(errors = listOf(AutomationRuleError.NO_BLUETOOTH_DEVICE))

        composeRule.onNodeWithTag("rule_editor_error_device").assertExists()
    }

    @Test
    fun polarityCanBeFlipped() {
        showAs(TriggerKind.HEADPHONES)

        composeRule.onNodeWithTag("rule_editor_polarity_off").performClick()

        composeRule.waitForIdle()
        assertEquals(true, (current.trigger as AutomationTrigger.Headphones).negated)
    }

    @Test
    fun wifiStartsOnAnyNetworkAndNamedRevealsThePicker() {
        showAs(TriggerKind.WIFI)
        composeRule.onNodeWithTag("rule_editor_wifi_any").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_network").assertDoesNotExist()

        composeRule.onNodeWithTag("rule_editor_wifi_named").performClick()

        composeRule.onNodeWithTag("rule_editor_network").assertExists()
        composeRule.onNodeWithText("Choose a network").assertExists()
    }

    @Test
    fun pickingANetworkFillsTheName() {
        showAs(TriggerKind.WIFI)
        current = current.withWifiScope(named = true)

        composeRule.onNodeWithTag("rule_editor_network").performClick()
        composeRule.onNodeWithText("Cafe").performClick()

        composeRule.waitForIdle()
        assertEquals("Cafe", (current.trigger as AutomationTrigger.Wifi).ssid)
    }

    @Test
    fun batteryOffersChargingStateDirectionAndALevel() {
        showAs(TriggerKind.BATTERY)

        composeRule.onNodeWithTag("rule_editor_charging_off").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_direction_on").assertIsSelected()
        composeRule.onNodeWithTag("rule_editor_battery_value").assertTextEquals("20%")
        composeRule.onNodeWithTag("rule_editor_battery_slider").assertExists()
    }

    @Test
    fun switchingToAboveKeepsTheLevelOnAStop() {
        showAs(TriggerKind.BATTERY)

        composeRule.onNodeWithTag("rule_editor_direction_off").performClick()
        composeRule.onNodeWithTag("rule_editor_charging_on").performClick()

        composeRule.waitForIdle()
        val battery = current.trigger as AutomationTrigger.Battery
        assertEquals(BatteryDirection.ABOVE, battery.level.direction)
        assertEquals(true, battery.whileCharging)
        assertEquals(20, battery.level.thresholdPercent)
    }

    @Test
    fun aRefusedPermissionShowsTheNoteWithAnOpenSettingsAction() {
        show(current.copy(permissionDenied = AutomationPermission.BLUETOOTH_CONNECT))

        composeRule.onNodeWithTag("rule_editor_permission_note").assertExists()
        composeRule.onNodeWithTag("rule_editor_open_settings").assertExists()
    }

    @Test
    fun anOvernightScheduleSaysItEndsTheNextDay() {
        show()
        composeRule.onNodeWithTag("rule_editor_ends_next_day").assertDoesNotExist()

        current = current.withEndMinute(6 * 60)

        composeRule.onNodeWithTag("rule_editor_ends_next_day").assertExists()
    }

    @Test
    fun aFreeUserSeesProPillsOnTheDeviceTriggers() {
        pro = false
        show()

        composeRule.onNodeWithTag("rule_editor_when").performClick()

        composeRule.onAllNodesWithText("Pro").assertCountEquals(4)
    }

    @Test
    fun aProUserSeesNoPills() {
        show()

        composeRule.onNodeWithTag("rule_editor_when").performClick()

        composeRule.onAllNodesWithText("Pro").assertCountEquals(0)
    }

    @Test
    fun theUpgradeSheetShowsWhenProIsRequiredAndDismissClearsIt() {
        pro = false
        show(current.copy(proRequired = ProReason.TRIGGER))
        composeRule.onNodeWithTag("automation_upgrade_sheet").assertExists()

        composeRule.onNodeWithTag("automation_upgrade_dismiss").performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("automation_upgrade_sheet").assertDoesNotExist()
    }
}
