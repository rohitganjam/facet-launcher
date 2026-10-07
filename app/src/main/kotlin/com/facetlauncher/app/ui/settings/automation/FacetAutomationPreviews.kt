package com.facetlauncher.app.ui.settings.automation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.AutomationStatus
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.RuleAvailability
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Surface
import java.time.DayOfWeek

private fun previewItem(id: Long, trigger: AutomationTrigger, availability: RuleAvailability = RuleAvailability.AVAILABLE) =
    AutomationRuleItem(AutomationRule(id, 1L, trigger), targetFacetName = "Work", endFacetName = null, availability = availability)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetAutomationScreenPreview() {
    val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    val schedule = previewItem(1, AutomationTrigger.Schedule(weekdays, 540, 1080))
    FacetLauncherTheme {
        FacetAutomationContent(
            state = FacetAutomationScreenState(
                items = listOf(
                    schedule,
                    previewItem(2, AutomationTrigger.Bluetooth("AA:BB", "Car")),
                    previewItem(3, AutomationTrigger.Battery(false, BatteryLevelCondition(BatteryDirection.BELOW, 20)), RuleAvailability.AVAILABLE),
                    previewItem(4, AutomationTrigger.Wifi(ssid = "HomeNet"), RuleAvailability.NEEDS_LOCATION),
                ),
                status = AutomationStatus.Driving("Work", schedule),
            ),
            onBack = {},
            onToggleRule = { _, _ -> },
            onAddRule = {},
            onEditRule = {},
            onSeePro = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 600)
@Composable
private fun FacetAutomationEmptyPreview() {
    FacetLauncherTheme {
        FacetAutomationContent(
            state = FacetAutomationScreenState(),
            onBack = {},
            onToggleRule = { _, _ -> },
            onAddRule = {},
            onEditRule = {},
            onSeePro = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetAutomationFreePlanPreview() {
    val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    FacetLauncherTheme {
        FacetAutomationContent(
            state = FacetAutomationScreenState(
                items = listOf(
                    previewItem(1, AutomationTrigger.Schedule(weekdays, 540, 1080)),
                    previewItem(2, AutomationTrigger.Schedule(setOf(DayOfWeek.SATURDAY), 600, 720)),
                    previewItem(3, AutomationTrigger.Bluetooth("AA:BB", "Car"), RuleAvailability.NEEDS_PRO),
                ),
                canAddRule = false,
                isPro = false,
            ),
            onBack = {},
            onToggleRule = { _, _ -> },
            onAddRule = {},
            onEditRule = {},
            onSeePro = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 300)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UpgradeSheetPreview() {
    FacetLauncherTheme { UpgradeContent(reason = ProReason.RULE_LIMIT, onDismiss = {}, modifier = Modifier.background(Surface)) }
}
