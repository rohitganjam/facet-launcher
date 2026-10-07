package com.facetlauncher.app.ui.settings.automation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.RuleEndBehavior
import com.facetlauncher.app.domain.AutomationRuleItem
import com.facetlauncher.app.domain.RuleAvailability
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private val WEEKDAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
private val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
private const val MINUTES_PER_HOUR = 60

/** "Weekdays", "Weekends", "Every day", or the short day names ("Mon, Wed"). The three labels come from string resources. */
internal fun formatDays(days: Set<DayOfWeek>, weekdays: String, weekends: String, everyDay: String): String = when {
    days == WEEKDAYS -> weekdays
    days == WEEKEND -> weekends
    days.size == DayOfWeek.entries.size -> everyDay
    else -> days.sortedBy { it.value }.joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}

/** A minute of the day in the phone's own time format ("9:00 AM" or "09:00"). */
internal fun minuteText(minuteOfDay: Int): String =
    LocalTime.of(minuteOfDay / MINUTES_PER_HOUR, minuteOfDay % MINUTES_PER_HOUR)
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()))

/** The derived rule title: rules have no name of their own, so the trigger describes them. */
@Composable
internal fun AutomationTrigger.titleText(): String = when (this) {
    is AutomationTrigger.Schedule -> {
        val daysLabel = formatDays(
            days,
            weekdays = stringResource(R.string.automation_days_weekdays),
            weekends = stringResource(R.string.automation_days_weekends),
            everyDay = stringResource(R.string.automation_days_every_day),
        )
        stringResource(R.string.automation_schedule_title, daysLabel, minuteText(startMinute), minuteText(endMinute))
    }
    is AutomationTrigger.Bluetooth -> connectionText(deviceName, negated)
    is AutomationTrigger.Wifi -> connectionText(ssid ?: stringResource(R.string.automation_wifi_any), negated)
    is AutomationTrigger.Headphones ->
        stringResource(if (negated) R.string.automation_headphones_out else R.string.automation_headphones_in)
    is AutomationTrigger.Battery -> stringResource(
        R.string.automation_battery_title,
        stringResource(if (whileCharging) R.string.automation_battery_charging else R.string.automation_battery_not_charging),
        stringResource(if (level.direction == BatteryDirection.BELOW) R.string.automation_level_below else R.string.automation_level_above),
        level.thresholdPercent,
    )
}

@Composable
private fun connectionText(name: String, negated: Boolean): String =
    stringResource(if (negated) R.string.automation_bluetooth_not_connected else R.string.automation_bluetooth_connected, name)

/** What a rule does and how it ends, or what it needs when it can't run. */
@Composable
internal fun AutomationRuleItem.subtitleText(): String = when (availability) {
    RuleAvailability.NEEDS_PRO -> stringResource(R.string.automation_paused_needs_pro)
    RuleAvailability.NEEDS_BLUETOOTH -> stringResource(R.string.automation_needs_bluetooth)
    RuleAvailability.NEEDS_LOCATION -> stringResource(R.string.automation_needs_location)
    RuleAvailability.AVAILABLE -> when (rule.endBehavior) {
        RuleEndBehavior.ReturnToBaseline -> stringResource(R.string.automation_subtitle_return, targetFacetName)
        is RuleEndBehavior.SwitchTo -> stringResource(R.string.automation_subtitle_switch_to, targetFacetName, endFacetName.orEmpty())
        RuleEndBehavior.Stay -> stringResource(R.string.automation_subtitle_stay, targetFacetName)
    }
}
