package com.facetlauncher.app.ui.home.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.homeIconShadow
import com.facetlauncher.app.ui.theme.homeTextShadow

/**
 * The next-alarm accessory item: [AlarmClockIcon] plus its time, in [color]. Renders nothing when
 * [text] is `null` (no alarm set) — a true layout drop, not a hidden placeholder, so a caller's
 * `Row` collapses around whatever items are actually present.
 */
@Composable
fun AlarmAccessoryContent(
    text: String?,
    color: Color,
    family: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    iconAtEnd: Boolean = false,
) {
    if (text == null) return
    IconAndText(
        modifier = modifier,
        iconAtEnd = iconAtEnd,
        icon = { AlarmClockIcon(tint = color, modifier = Modifier.size(ClockAccessoryIconSize).homeIconShadow(color)) },
        text = { Text(text = text, style = TextStyle(fontFamily = family, fontWeight = fontWeight, fontSize = fontSize, shadow = homeTextShadow(color)), color = color) },
    )
}

/** What tapping the battery item does (opens Settings > Battery); a local so the templates' accessory rows need no extra parameter. */
@Suppress("CompositionLocalAllowlist") // Read by every template's accessory row; threading it as a parameter would touch all of them.
internal val LocalBatteryClick = compositionLocalOf<() -> Unit> { {} }

/** An accessory's icon and its text side by side: the icon first, or after the text when [iconAtEnd] (a right-aligned layout). */
@Composable
internal fun IconAndText(icon: @Composable () -> Unit, text: @Composable () -> Unit, iconAtEnd: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val first = if (iconAtEnd) text else icon
        val second = if (iconAtEnd) icon else text
        first()
        second()
    }
}

/**
 * The battery accessory item: [BatteryIcon] plus a bare percentage, always in [mutedColor] — only
 * the icon's own glyph (see [batteryIconState]) changes with [isCharging]/[percent], not the
 * color of either the icon or the text. [accentColor] is accepted but unused here, kept purely so
 * every existing caller's argument list (`ClockAccessoryRow` and the many shape templates in
 * `ClockTemplates.kt`) keeps compiling unchanged — battery deliberately went back to a flat,
 * un-accented look (see chat history). Renders nothing when [percent] is `null` — same true-drop
 * behavior as [AlarmAccessoryContent].
 */
@Composable
fun BatteryAccessoryContent(
    percent: Int?,
    isCharging: Boolean,
    mutedColor: Color,
    accentColor: Color,
    family: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    iconAtEnd: Boolean = false,
) {
    if (percent == null) return
    IconAndText(
        modifier = modifier.clickable(role = Role.Button, onClick = LocalBatteryClick.current),
        iconAtEnd = iconAtEnd,
        icon = { BatteryIcon(tint = mutedColor, state = batteryIconState(percent, isCharging), modifier = Modifier.size(BatteryIconSize).homeIconShadow(mutedColor)) },
        text = { Text(text = stringResource(R.string.percent_format, percent), style = TextStyle(fontFamily = family, fontWeight = fontWeight, fontSize = fontSize, shadow = homeTextShadow(mutedColor)), color = mutedColor) },
    )
}

/**
 * The plain middot-joined accessory line used by every typographic template (and by a few
 * shape templates that prefix their own small decorative mark before calling this) — see
 * `ClockTemplates.kt`'s per-template docs for exactly which. Renders nothing at all when the
 * alarm, the battery and the weather ([LocalClockWeather]) are all absent.
 */
@Composable
fun ClockAccessoryRow(
    nextAlarmText: String?,
    batteryPercent: Int?,
    isCharging: Boolean,
    family: FontFamily,
    mutedColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
) {
    val hasWeather = LocalClockWeather.current != null
    if (nextAlarmText == null && batteryPercent == null && !hasWeather) return
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AlarmAccessoryContent(nextAlarmText, mutedColor, family, fontSize = fontSize, fontWeight = fontWeight)
        if (nextAlarmText != null && batteryPercent != null) {
            AccessoryDot(family, fontWeight, fontSize, mutedColor)
        }
        BatteryAccessoryContent(batteryPercent, isCharging, mutedColor, accentColor, family, fontSize = fontSize, fontWeight = fontWeight)
        if ((nextAlarmText != null || batteryPercent != null) && hasWeather) {
            AccessoryDot(family, fontWeight, fontSize, mutedColor)
        }
        WeatherAccessoryContent(mutedColor, family, fontSize = fontSize, fontWeight = fontWeight)
    }
}

@Composable
private fun AccessoryDot(family: FontFamily, fontWeight: FontWeight, fontSize: TextUnit, color: Color) {
    Text(text = stringResource(R.string.clock_accessory_separator_dot), style = TextStyle(fontFamily = family, fontWeight = fontWeight, fontSize = fontSize, shadow = homeTextShadow(color)), color = color)
}
