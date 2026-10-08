package com.facetlauncher.app.ui.settings.automation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.AutomationStatus
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.RuleAvailability
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FacetAutomationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun item(id: Long, trigger: AutomationTrigger, availability: RuleAvailability = RuleAvailability.AVAILABLE, enabled: Boolean = true) =
        AutomationRuleItem(AutomationRule(id, 1L, trigger, enabled = enabled), targetFacetName = "Work", endFacetName = null, availability = availability)

    private fun setContent(
        state: FacetAutomationScreenState?,
        onToggle: (Long, Boolean) -> Unit = { _, _ -> },
        onAddRule: () -> Unit = {},
        onEditRule: (Long) -> Unit = {},
        onSeePro: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                FacetAutomationContent(state = state, onBack = {}, onToggleRule = onToggle, onAddRule = onAddRule, onEditRule = onEditRule, onSeePro = onSeePro)
            }
        }
    }

    @Test
    fun noRulesShowsTheEmptyStripAndItsAddAction() {
        var added = false
        setContent(FacetAutomationScreenState(), onAddRule = { added = true })

        composeRule.onNodeWithTag("automation_empty").assertExists()
        composeRule.onNodeWithTag("automation_empty_add").performClick()

        assertEquals(true, added)
    }

    @Test
    fun rulesAreListedWithAnAddRowAndTheShortcutsNote() {
        setContent(FacetAutomationScreenState(items = listOf(item(1, AutomationTrigger.Headphones()), item(2, AutomationTrigger.Wifi()))))

        composeRule.onNodeWithTag("automation_rule_row_1").assertExists()
        composeRule.onNodeWithTag("automation_rule_row_2").assertExists()
        composeRule.onNodeWithTag("automation_add_rule_row").assertExists()
        composeRule.onNodeWithTag("automation_other_apps_note").assertExists()
    }

    @Test
    fun tappingARowOpensItAndTheSwitchTogglesIt() {
        var edited: Long? = null
        var toggled: Pair<Long, Boolean>? = null
        setContent(
            FacetAutomationScreenState(items = listOf(item(7, AutomationTrigger.Headphones(), enabled = true))),
            onToggle = { id, enabled -> toggled = id to enabled },
            onEditRule = { edited = it },
        )

        composeRule.onNodeWithTag("automation_rule_switch_7").performClick()
        composeRule.onNodeWithTag("automation_rule_row_7").performClick()

        assertEquals(7L to false, toggled)
        assertEquals(7L, edited)
    }

    @Test
    fun anUnavailableRuleCannotBeSwitchedOn() {
        setContent(FacetAutomationScreenState(items = listOf(item(3, AutomationTrigger.Bluetooth("AA:BB", "Car"), RuleAvailability.NEEDS_BLUETOOTH))))

        composeRule.onNodeWithTag("automation_rule_switch_3").assertIsNotEnabled()
    }

    @Test
    fun theStatusLineShowsWhyHomeLooksTheWayItDoes() {
        val driving = item(1, AutomationTrigger.Headphones())
        setContent(FacetAutomationScreenState(items = listOf(driving), status = AutomationStatus.Driving("Work", driving)))

        composeRule.onNodeWithTag("automation_status").assertExists()
    }

    @Test
    fun theAddRowStaysAtTheLimitAndCarriesAProPill() {
        var added = false
        setContent(
            FacetAutomationScreenState(items = listOf(item(1, AutomationTrigger.Headphones())), canAddRule = false, isPro = false),
            onAddRule = { added = true },
        )

        composeRule.onNodeWithTag("automation_add_rule_row").assertExists().performClick()

        composeRule.onNodeWithText("Pro").assertExists()
        assertEquals(true, added)
    }

    @Test
    fun aProUserSeesNoFreePlanStrip() {
        setContent(FacetAutomationScreenState(items = listOf(item(1, AutomationTrigger.Headphones()))))

        composeRule.onNodeWithTag("automation_free_strip").assertDoesNotExist()
    }

    @Test
    fun aFreeUserBelowTheRuleLimitSeesNoStrip() {
        setContent(FacetAutomationScreenState(items = listOf(item(1, AutomationTrigger.Headphones())), isPro = false, canAddRule = true))

        composeRule.onNodeWithTag("automation_free_strip").assertDoesNotExist()
    }

    @Test
    fun aFreeUserSeesTheStripAndSeeProOpensTheUpgrade() {
        var opened = false
        setContent(FacetAutomationScreenState(items = listOf(item(1, AutomationTrigger.Headphones())), isPro = false, canAddRule = false), onSeePro = { opened = true })

        composeRule.onNodeWithTag("automation_free_strip").assertExists()
        composeRule.onNodeWithTag("automation_see_pro").performClick()

        assertEquals(true, opened)
    }

    @Test
    fun aRulePausedForProIsLockedAndOpensTheUpgradeWhenTapped() {
        var edited: Long? = null
        setContent(
            FacetAutomationScreenState(
                items = listOf(item(4, AutomationTrigger.Bluetooth("AA:BB", "Car"), RuleAvailability.NEEDS_PRO)),
                isPro = false,
            ),
            onEditRule = { edited = it },
        )

        composeRule.onNodeWithText("Paused. Needs Pro").assertExists()
        composeRule.onNodeWithTag("automation_rule_switch_4").assertIsNotEnabled()
        composeRule.onNodeWithTag("automation_rule_row_4").performClick()

        assertEquals(4L, edited)
    }

    @Test
    fun nothingIsShownBeforeTheStateLoads() {
        setContent(null)

        composeRule.onNodeWithTag("automation_empty").assertDoesNotExist()
        composeRule.onNodeWithTag("automation_add_rule_row").assertDoesNotExist()
    }
}
