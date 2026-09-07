package com.lumenlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.homeTextShadow
import com.lumenlauncher.app.ui.theme.resolve
import com.lumenlauncher.app.ui.theme.resolveFontFamily
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
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Settings → Clock & Calendar Style → "Clock alignment" — every template aligns its own time/date content to match, not just its position within Home (see [com.lumenlauncher.app.ui.home.HomeScreen]). */
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
) {
    val family = fontOption.resolveFontFamily(launcherFontOption)
    val meridiem = meridiemText(now, use24HourTime, showMeridiem, locale)
    val horizontalAlignment = clockAlignment.resolve()
    when (templateId) {
        ClockTemplateId.LIGHT_STACK -> LightStackTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.RULE_MERIDIEM -> RuleMeridiemTemplate(now, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.DATE_FORWARD -> DateForwardTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.WEIGHT_CONTRAST -> WeightContrastTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.ITALIC_ACCENT -> ItalicAccentTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.SPELLED_OUT -> SpelledOutTemplate(now, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.VERTICAL_STACK -> VerticalStackTemplate(now, use24HourTime, meridiem, locale, family, boldHour = false, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.VERTICAL_STACK_BOLD_HOUR -> VerticalStackTemplate(now, use24HourTime, meridiem, locale, family, boldHour = true, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.ROBOTO_FLEX_WIDE -> RobotoFlexWideTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.ROBOTO_FLEX_NARROW -> RobotoFlexNarrowTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.TECH_DISTORTED -> TechDistortedTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.VARIABLE_DIVIDER -> VariableDividerTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.FLUID_STACK -> FluidStackTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.BRACKET_MINIMAL -> BracketMinimalTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.TWO_LINE_DIVIDER -> TwoLineDividerTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
        ClockTemplateId.BOLD_COLON -> BoldColonTemplate(now, use24HourTime, meridiem, locale, family, textColor, mutedTextColor, modifier, horizontalAlignment)
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
        style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.84.sp, shadow = homeTextShadow(color)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Light,
                    fontSize = 80.sp,
                    lineHeight = 80.sp,
                    letterSpacing = (-4).sp,
                    shadow = homeTextShadow(textColor),
                ),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 18.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourMinuteFormatter = DateTimeFormatter.ofPattern("h:mm", locale)
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = now.format(hourMinuteFormatter),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 68.sp, shadow = homeTextShadow(textColor)),
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
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 17.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = horizontalAlignment) {
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, shadow = homeTextShadow(textColor)),
            color = textColor,
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Light, fontSize = 44.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val baseStyle = TextStyle(fontSize = 70.sp, letterSpacing = (-2).sp, shadow = homeTextShadow(textColor))
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = horizontalAlignment) {
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
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.1.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 74.sp, shadow = homeTextShadow(textColor)),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 17.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    val rowStyle = TextStyle(
        fontFamily = family,
        fontSize = 62.sp,
        letterSpacing = (-2).sp,
        shadow = homeTextShadow(textColor),
    )
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        // A tighter lineHeight/Trim.Both on each Text individually stopped closing this gap once
        // the minute row also had to carry an optional meridiem beside it (that split hour/minute
        // into two independent Texts instead of one two-line one, and Compose's line trimming
        // doesn't reach across separate composables) — an explicit negative spacedBy between them
        // is the reliable fix regardless of font size, measured directly against a device
        // screenshot (see chat history) rather than assumed from the line-height math.
        Column(verticalArrangement = Arrangement.spacedBy((-20).dp), horizontalAlignment = horizontalAlignment) {
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
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 18.sp, shadow = homeTextShadow(mutedTextColor)),
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
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = horizontalAlignment) {
        Text(
            text = timeInWords(now),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, lineHeight = 42.sp, shadow = homeTextShadow(textColor)),
            color = textColor,
        )
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 16.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexWide = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(150f),
            FontVariation.weight(700)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexNarrow = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(25f),
            FontVariation.weight(400)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val TechDistorted = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("GRAD", 150f),
            FontVariation.Setting("XTRA", 450f),
            FontVariation.weight(500)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexWideThin = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(150f),
            FontVariation.weight(100)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexNarrowBlack = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(25f),
            FontVariation.weight(900)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexLightCondensed = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(75f),
            FontVariation.weight(300)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexUltraLight = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(100f),
            FontVariation.weight(100)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexBlack = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.lumenlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(100f),
            FontVariation.weight(900)
        )
    )
)

@Composable
private fun BracketMinimalTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    val timeFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH : mm" else "h : mm", locale)
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "[ ",
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 64.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor
            )
            Text(
                text = now.format(timeFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexLightCondensed,
                    fontSize = 64.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Text(
                text = " ]",
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 64.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 8.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)).uppercase(locale),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun TwoLineDividerTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Text(
            text = now.format(hourFormatter),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 60.sp,
                shadow = homeTextShadow(textColor)
            ),
            color = textColor
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(textColor.copy(alpha = 0.4f))
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Light,
                    fontSize = 60.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 6.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun BoldColonTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexUltraLight,
                    fontSize = 80.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Text(
                text = ":",
                style = TextStyle(
                    fontFamily = RobotoFlexBlack,
                    fontSize = 80.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexUltraLight,
                    fontSize = 80.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 8.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun VariableDividerTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexWideThin,
                    fontSize = 80.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(textColor.copy(alpha = 0.5f))
            )
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexNarrowBlack,
                    fontSize = 80.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) {
                MeridiemText(meridiem, family, textColor)
            }
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FluidStackTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy((-12).dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexWideThin,
                    fontSize = 90.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) {
                MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 16.dp, start = 8.dp))
            }
        }
        Text(
            text = now.format(minuteFormatter),
            style = TextStyle(
                fontFamily = RobotoFlexNarrowBlack,
                fontSize = 90.sp,
                shadow = homeTextShadow(textColor)
            ),
            color = textColor
        )
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun RobotoFlexWideTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = RobotoFlexWide,
                    fontSize = 72.sp,
                    letterSpacing = (-2).sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, RobotoFlexWide, textColor, Modifier.padding(bottom = 8.dp))
        }
        Text(
            text = now.format(dateFormatter(locale)).uppercase(locale),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor
        )
    }
}

@Composable
private fun RobotoFlexNarrowTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    // This template's own two columns (hour/minute digits, then meridiem+abbreviated date) sit
    // side by side rather than stacked, so there's no single Column to hand horizontalAlignment
    // to — Arrangement.spacedBy(space, alignment) is the Row equivalent, positioning the whole
    // two-column unit within the available width the same way horizontalAlignment does elsewhere.
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, horizontalAlignment),
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)),
                style = TextStyle(fontFamily = RobotoFlexNarrow, fontSize = 80.sp, lineHeight = 80.sp, shadow = homeTextShadow(textColor)),
                color = textColor
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern("mm", locale)),
                style = TextStyle(fontFamily = RobotoFlexNarrow, fontSize = 80.sp, lineHeight = 80.sp, shadow = homeTextShadow(textColor)),
                color = textColor
            )
        }
        Column {
            if (meridiem != null) {
                MeridiemText(meridiem, RobotoFlexNarrow, textColor)
            }
            Text(
                text = now.format(DateTimeFormatter.ofPattern("EEE\nd MMM", locale)).uppercase(locale),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor
            )
        }
    }
}

@Composable
private fun TechDistortedTemplate(
    now: LocalDateTime,
    use24HourTime: Boolean,
    meridiem: String?,
    locale: Locale,
    family: FontFamily,
    textColor: Color,
    mutedTextColor: Color,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = TechDistorted,
                    fontSize = 90.sp,
                    letterSpacing = (-5).sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) {
                Text(
                    text = meridiem,
                    style = TextStyle(
                        fontFamily = TechDistorted,
                        fontSize = 24.sp,
                        shadow = homeTextShadow(textColor)
                    ),
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }
        }
        Text(
            text = now.format(dateFormatter(locale)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
