package com.facetlauncher.app.ui.settings.automation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.facetlauncher.app.data.model.AutomationRule
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.AutomationStatus
import com.facetlauncher.app.domain.FacetAutomationScreenState
import com.facetlauncher.app.domain.RuleAvailability
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
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
        )
    }
}
