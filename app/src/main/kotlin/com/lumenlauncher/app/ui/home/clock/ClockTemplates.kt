package com.lumenlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.HomeTextShadow
import com.lumenlauncher.app.ui.theme.fontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders [templateId] using [fontOption]'s font family — the one entry point every clock
 * surface (the real [com.lumenlauncher.app.ui.home.ClockBlock] wrapper, and the template
 * gallery) should call, rather than reaching for a specific template composable directly.
 * [textColor]/[mutedTextColor] are plain params (not read from theme here) so the clock's color
 * stays configurable independently of the rest of the app, per the clock/calendar template
 * design brief — see [com.lumenlauncher.app.data.model.ClockColorOption].
 *
 * [showMeridiem] is honored by every template except [ClockTemplateId.SPELLED_OUT] (which speaks
 * a 12-hour time implicitly via words, with no separate AM/PM slot to toggle) — always ignored
 * when [use24HourTime] is on, since there's no meridiem in 24-hour time regardless of the toggle.
 */
@Composable
fun ClockDisplay(
    templateId: ClockTemplateId,
    fontOption: ClockFontOption,
    now: LocalDateTime,
    use24HourTime: Boolean,
    showMeridiem: Boolean,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
) {
    val family = fontOption.fontFamily
    val meridiem = meridiemText(now, use24HourTime, showMeridiem, locale)
    when (templateId) {
        ClockTemplateId.LIGHT_STACK -> LightStackTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.RULE_MERIDIEM -> RuleMeridiemTemplate(now, meridiem, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.DATE_FORWARD -> DateForwardTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.WEIGHT_CONTRAST -> WeightContrastTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.ITALIC_ACCENT -> ItalicAccentTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.SPELLED_OUT -> SpelledOutTemplate(now, locale, family, textColor, mutedTextColor, modifier)
        ClockTemplateId.VERTICAL_STACK -> VerticalStackTemplate(now, use24HourTime, meridiem, locale, family, boldHour = false, textColor, mutedTextColor, modifier)
        ClockTemplateId.VERTICAL_STACK_BOLD_HOUR -> VerticalStackTemplate(now, use24HourTime, meridiem, locale, family, boldHour = true, textColor, mutedTextColor, modifier)
    }
}

private fun timeFormatter(use24HourTime: Boolean, locale: Locale) =
    DateTimeFormatter.ofPattern(if (use24HourTime) "HH:mm" else "h:mm", locale)

private fun dateFormatter(locale: Locale) = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)

/** `null` in 24-hour time (no meridiem exists) or when [showMeridiem] is off; "AM"/"PM" otherwise. */
private fun meridiemText(now: LocalDateTime, use24HourTime: Boolean, showMeridiem: Boolean, locale: Locale): String? =
    if (use24HourTime || !showMeridiem) null else now.format(DateTimeFormatter.ofPattern("a", locale))

@Composable
private fun MeridiemText(text: String, family: FontFamily, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.84.sp, shadow = HomeTextShadow),
        color = color,
        modifier = modifier,
    )
}

/**
 * Today's shipped default (README `1b`) — light 72sp time, muted date beneath. Weights are one
 * step heavier than the original design spec across every template in this file (an explicit
 * readability test, see chat history) — extralight(200) became light(300) here, etc.
 */
@Composable
private fun LightStackTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Light,
                    fontSize = 80.sp,
                    lineHeight = 80.sp,
                    letterSpacing = (-4).sp,
                    shadow = HomeTextShadow,
                ),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 18.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
        )
    }
}

/** README `1c` — 68sp time, an optional meridiem baseline-aligned beside it, a hairline rule, date beneath. */
@Composable
private fun RuleMeridiemTemplate(
    now: LocalDateTime,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    val hourMinuteFormatter = DateTimeFormatter.ofPattern("h:mm", locale)
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = now.format(hourMinuteFormatter),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 68.sp, shadow = HomeTextShadow),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 9.dp))
        }
        Column(
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Hairline),
        ) {}
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 17.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/** README `1d` — semibold 34sp date leads, time demoted to a muted light 44sp beneath. */
@Composable
private fun DateForwardTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 39.sp, shadow = HomeTextShadow),
            color = textColor,
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Light, fontSize = 44.sp, shadow = HomeTextShadow),
                color = mutedTextColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, mutedTextColor, Modifier.padding(bottom = 6.dp))
        }
    }
}

/** Bold hour / regular minute on one tightly-kerned line, small caps-style label date beneath. */
@Composable
private fun WeightContrastTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val baseStyle = TextStyle(fontSize = 70.sp, letterSpacing = (-2).sp, shadow = HomeTextShadow)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = now.format(hourFormatter),
                style = baseStyle.copy(fontFamily = family, fontWeight = FontWeight.ExtraBold),
                color = textColor,
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern(":mm", locale)),
                style = baseStyle.copy(fontFamily = family, fontWeight = FontWeight.Normal),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 6.dp, bottom = 8.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)).uppercase(locale),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.1.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
        )
    }
}

/** Hour and minute both italic; italic date beneath; optional italic meridiem beside the time. */
@Composable
private fun ItalicAccentTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 74.sp, shadow = HomeTextShadow),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 17.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
        )
    }
}

/**
 * Hour on its own row, minute (with an optional meridiem beside it) on the next — a true vertical
 * stack, not a single time string. [boldHour] is [ClockTemplateId.VERTICAL_STACK_BOLD_HOUR]'s
 * only difference from [ClockTemplateId.VERTICAL_STACK] — same tight two-line spacing either way.
 */
@Composable
private fun VerticalStackTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    boldHour: Boolean,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    val rowStyle = TextStyle(
        fontFamily = family,
        fontSize = 62.sp,
        letterSpacing = (-2).sp,
        shadow = HomeTextShadow,
    )
    Column(modifier = modifier) {
        // A tighter lineHeight/Trim.Both on each Text individually stopped closing this gap once
        // the minute row also had to carry an optional meridiem beside it (that split hour/minute
        // into two independent Texts instead of one two-line one, and Compose's line trimming
        // doesn't reach across separate composables) — an explicit negative spacedBy between them
        // is the reliable fix regardless of font size, measured directly against a device
        // screenshot (see chat history) rather than assumed from the line-height math.
        Column(verticalArrangement = Arrangement.spacedBy((-20).dp)) {
            Text(
                text = now.format(hourFormatter),
                style = rowStyle.copy(fontWeight = if (boldHour) FontWeight.ExtraBold else FontWeight.Light),
                color = textColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = now.format(minuteFormatter),
                    style = rowStyle.copy(fontWeight = FontWeight.Normal),
                    color = textColor,
                )
                if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 6.dp))
            }
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 18.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/** [timeInWords] instead of digits — "Eleven Twenty Two" — always spoken 12-hour, see that function's doc. No meridiem slot: the toggle has no effect here. */
@Composable
private fun SpelledOutTemplate(
    now: LocalDateTime,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = timeInWords(now),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, lineHeight = 42.sp, shadow = HomeTextShadow),
            color = textColor,
        )
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 16.sp, shadow = HomeTextShadow),
            color = mutedTextColor,
        )
    }
}
