package com.facetlauncher.app.ui.home.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.contentColorFor
import com.facetlauncher.app.ui.theme.homeTextShadow
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.ui.theme.resolveFontFamily
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders [templateId] using [fontOption]'s font family — the one entry point every clock
 * surface (the real [com.facetlauncher.app.ui.home.ClockBlock] wrapper, and the template
 * gallery) should call, rather than reaching for a specific template composable directly.
 * [textColor]/[mutedTextColor] are plain params (not read from theme here) so the clock's color
 * stays configurable independently of the rest of the app, per the clock/calendar template
 * design brief — see [com.facetlauncher.app.data.model.ClockColorOption].
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
    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockAccentColorOption] — only templates where [com.facetlauncher.app.data.model.usesAccentColor] is true read this. */
    accentColor: Color = textColor,
    /** Millis since epoch of the system's next alarm, or `null` when none is set — see [com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase]. */
    nextAlarmMillis: Long? = null,
    /** `null` until the first real battery reading arrives — treated the same as "nothing to show" until then. */
    batteryPercent: Int? = null,
    isCharging: Boolean = false,
    /** "Full" vs "Condensed" — see [ClockDateStyle]. */
    dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Settings → Clock & Calendar Style → "Clock alignment" — every template aligns its own time/date content to match, not just its position within Home (see [com.facetlauncher.app.ui.home.HomeScreen]). */
    clockAlignment: ClockAlignment = ClockAlignment.LEFT,
) {
    val data = ClockTemplateData(
        now = now,
        use24HourTime = use24HourTime,
        meridiem = meridiemText(now, use24HourTime, showMeridiem, locale),
        locale = locale,
        family = fontOption.resolveFontFamily(launcherFontOption),
        textColor = textColor,
        mutedTextColor = mutedTextColor,
        horizontalAlignment = clockAlignment.resolve(),
        dateStyle = dateStyle,
        accentColor = accentColor,
        nextAlarmText = nextAlarmMillis?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()).format(timeFormatter(use24HourTime, locale))
        },
        batteryPercent = batteryPercent,
        isCharging = isCharging,
    )
    ClockTemplateRenderers.getValue(templateId)(data, modifier)
}

/** One renderer per [ClockTemplateId]; `ClockTemplateRenderersTest` fails if an id is missing. */
internal val ClockTemplateRenderers: Map<ClockTemplateId, @Composable ClockTemplateData.(Modifier) -> Unit> = buildMap {
    put(ClockTemplateId.LIGHT_STACK) { LightStackTemplate(modifier = it) }
    put(ClockTemplateId.RULE_MERIDIEM) { RuleMeridiemTemplate(modifier = it) }
    put(ClockTemplateId.DATE_FORWARD) { DateForwardTemplate(modifier = it) }
    put(ClockTemplateId.WEIGHT_CONTRAST) { WeightContrastTemplate(modifier = it) }
    put(ClockTemplateId.ITALIC_ACCENT) { ItalicAccentTemplate(modifier = it) }
    put(ClockTemplateId.SPELLED_OUT) { SpelledOutTemplate(modifier = it) }
    put(ClockTemplateId.VERTICAL_STACK) { VerticalStackTemplate(boldHour = false, modifier = it) }
    put(ClockTemplateId.VERTICAL_STACK_BOLD_HOUR) { VerticalStackTemplate(boldHour = true, modifier = it) }
    put(ClockTemplateId.ROBOTO_FLEX_WIDE) { RobotoFlexWideTemplate(modifier = it) }
    put(ClockTemplateId.ROBOTO_FLEX_NARROW) { RobotoFlexNarrowTemplate(modifier = it) }
    put(ClockTemplateId.TECH_DISTORTED) { TechDistortedTemplate(modifier = it) }
    put(ClockTemplateId.VARIABLE_DIVIDER) { VariableDividerTemplate(modifier = it) }
    put(ClockTemplateId.FLUID_STACK) { FluidStackTemplate(inverted = false, accentHour = false, modifier = it) }
    put(ClockTemplateId.FLUID_STACK_INVERTED) { FluidStackTemplate(inverted = true, accentHour = false, modifier = it) }
    put(ClockTemplateId.BRACKET_MINIMAL) { BracketMinimalTemplate(modifier = it) }
    put(ClockTemplateId.TWO_LINE_DIVIDER) { TwoLineDividerTemplate(modifier = it) }
    put(ClockTemplateId.BOLD_COLON) { BoldColonTemplate(modifier = it) }
    put(ClockTemplateId.ACCENTED_FLUID_STACK) { FluidStackTemplate(inverted = false, accentHour = true, modifier = it) }
    put(ClockTemplateId.ACCENTED_FLUID_STACK_INVERTED) { FluidStackTemplate(inverted = true, accentHour = true, modifier = it) }
    put(ClockTemplateId.ACCENT_CONTRAST) { AccentContrastTemplate(modifier = it) }
    put(ClockTemplateId.ACCENT_FIELD) { AccentFieldTemplate(modifier = it) }
    put(ClockTemplateId.HOUR_TILE) { HourTileTemplate(modifier = it) }
    put(ClockTemplateId.CHIP) { ChipTemplate(modifier = it) }
    put(ClockTemplateId.DUOTONE_OVERLAP) { DuotoneOverlapTemplate(modifier = it) }
    put(ClockTemplateId.CORNER_FRAME) { CornerFrameTemplate(modifier = it) }
    put(ClockTemplateId.STUB) { StubTemplate(modifier = it) }
    put(ClockTemplateId.HALO) { HaloTemplate(modifier = it) }
    put(ClockTemplateId.DIGIT_CELLS) { DigitCellsTemplate(modifier = it) }
    put(ClockTemplateId.NEGATIVE_PANEL) { NegativePanelTemplate(modifier = it) }
    put(ClockTemplateId.HOLLOW_HOUR) { HollowHourTemplate(modifier = it) }
    put(ClockTemplateId.HIGHLIGHTER) { HighlighterTemplate(modifier = it) }
    put(ClockTemplateId.COLUMN_RULE) { ColumnRuleTemplate(modifier = it) }
    put(ClockTemplateId.COLON_MARK) { ColonMarkTemplate(modifier = it) }
    put(ClockTemplateId.PILL_PAIR) { PillPairTemplate(modifier = it) }
    put(ClockTemplateId.SHELF) { ShelfTemplate(modifier = it) }
    put(ClockTemplateId.HALF_IMMERSED) { HalfImmersedTemplate(modifier = it) }
}

private fun timeFormatter(use24HourTime: Boolean, locale: Locale) =
    DateTimeFormatter.ofPattern(if (use24HourTime) "HH:mm" else "h:mm", locale)

private fun dateFormatter(locale: Locale, dateStyle: ClockDateStyle) = DateTimeFormatter.ofPattern(
    if (dateStyle == ClockDateStyle.CONDENSED) "EEE, d MMM" else "EEEE, d MMMM",
    locale,
)

/** `null` in 24-hour time (no meridiem exists) or when [showMeridiem] is off; "AM"/"PM" otherwise. */
private fun meridiemText(now: LocalDateTime, use24HourTime: Boolean, showMeridiem: Boolean, locale: Locale): String? =
    if (use24HourTime || !showMeridiem) null else now.format(DateTimeFormatter.ofPattern("a", locale))

@Composable
private fun MeridiemText(text: String, family: FontFamily, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = 1.sp, shadow = homeTextShadow(color)),
        color = color,
        modifier = modifier,
    )
}

/**
 * The one glyph primitive every "part" below (hour, minute, separator, date) renders through —
 * the shared place a shape-based template reaches for a differently-colored/weighted/stroked
 * digit run without duplicating [homeTextShadow]/[TextStyle] wiring. [drawStyle] is `null` for a
 * normal filled glyph, or a [Stroke] for an outline-only one (see [HollowHourTemplate]).
 */
@Composable
private fun ClockGlyphText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    family: FontFamily,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
    drawStyle: DrawStyle? = null,
    /** `false` for [NegativePanelTemplate]'s date text — its panel background is a solid fill, not the wallpaper, so the shadow tuned for wallpaper contrast just reads as unwanted blur there. */
    shadow: Boolean = true,
) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = family,
            fontWeight = fontWeight,
            fontSize = fontSize,
            letterSpacing = letterSpacing,
            shadow = if (shadow) homeTextShadow(color) else null,
            drawStyle = drawStyle,
        ),
        color = color,
        modifier = modifier,
    )
}

/** The hour digit(s) — its own part so a template can color/weight/stroke it independently of [MinuteText]. */
@Composable
private fun HourText(
    text: String,
    family: FontFamily,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
    drawStyle: DrawStyle? = null,
) = ClockGlyphText(text, color, fontSize, family, fontWeight, letterSpacing, modifier, drawStyle)

/** The minute digit(s) — its own part so a template can color/weight it independently of [HourText]. */
@Composable
private fun MinuteText(
    text: String,
    family: FontFamily,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
) = ClockGlyphText(text, color, fontSize, family, fontWeight, letterSpacing, modifier)

/** The `:` (or other) divider between [HourText] and [MinuteText] — its own part so it can carry a color/weight neither digit run does (see [ColumnRuleTemplate], [DigitCellsTemplate]). */
@Composable
private fun SeparatorText(
    text: String,
    family: FontFamily,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
) = ClockGlyphText(text, color, fontSize, family, fontWeight, letterSpacing, modifier)

/** The date line beneath the time — its own part purely for naming symmetry with the parts above; callers pass an already-`uppercase(locale)`d string where a template wants tracked caps, matching every other template's own convention. */
@Composable
private fun TemplateDateText(
    text: String,
    family: FontFamily,
    color: Color,
    fontSize: TextUnit = 19.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier,
    shadow: Boolean = true,
) = ClockGlyphText(text, color, fontSize, family, fontWeight, letterSpacing, modifier, shadow = shadow)

/**
 * Today's shipped default (README `1b`) — light 72sp time, muted date beneath. Weights are one
 * step heavier than the original design spec across every template in this file (an explicit
 * readability test, see chat history) — extralight(200) became light(300) here, etc.
 */
@Composable
private fun ClockTemplateData.LightStackTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Light,
                    fontSize = 96.sp,
                    lineHeight = 96.sp,
                    letterSpacing = (-5).sp,
                    shadow = homeTextShadow(textColor),
                ),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 12.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 22.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 22.sp)
    }
}

/** README `1c` — 68sp time, an optional meridiem baseline-aligned beside it, a hairline rule, date beneath. */
@Composable
private fun ClockTemplateData.RuleMeridiemTemplate(
    modifier: Modifier = Modifier,
) {
    val timeText = now.format(timeFormatter(use24HourTime, locale))
    // width(IntrinsicSize.Max) bounds this Column to its own widest child (the 82sp time row) —
    // required for the rule/accessory rows' own fillMaxWidth() below to resolve against that
    // content width instead of the ambient incoming constraint. Without it, since this template's
    // root carries no fillMaxWidth of its own (see the clock hit-box fix elsewhere), that ambient
    // constraint is whatever unconstrained width reaches all the way up from Home's own full
    // screen — so the rule (and, before this fix, the accessory row riding along with it) rendered
    // full-screen-wide in the real app while the Style Gallery's narrower preview card bounded it
    // differently, producing two different-looking layouts for the same template (see chat
    // history).
    Column(modifier = modifier.width(IntrinsicSize.Max), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = timeText,
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 82.sp, shadow = homeTextShadow(textColor)),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 11.dp))
        }
        Box(modifier = Modifier.padding(top = 22.dp).fillMaxWidth().height(1.dp).background(Hairline))
        // The date and the accessory row each get their own line below the rule. They used to share one row, which
        // jammed the battery against the date whenever the date was as wide as the time above it.
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 20.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 12.dp),
        )
        ClockAccessoryRow(
            nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 20.sp,
        )
    }
}

/** README `1d` — semibold 34sp date leads, time demoted to a muted light 44sp beneath. */
@Composable
private fun ClockTemplateData.DateForwardTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = horizontalAlignment) {
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 41.sp, lineHeight = 48.sp, shadow = homeTextShadow(textColor)),
            color = textColor,
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Light, fontSize = 53.sp, shadow = homeTextShadow(mutedTextColor)),
                color = mutedTextColor,
            )
            if (meridiem != null)
                MeridiemText(meridiem, family, mutedTextColor, Modifier.padding(bottom = 7.dp))
        }
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor)
    }
}

/** Bold hour / regular minute on one tightly-kerned line, small caps-style label date beneath. */
@Composable
private fun ClockTemplateData.WeightContrastTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            HourText(now.format(hourFormatter), family, textColor, 84.sp, FontWeight.ExtraBold, letterSpacing = (-2.4).sp)
            SeparatorText(now.format(DateTimeFormatter.ofPattern(":mm", locale)), family, textColor, 84.sp, FontWeight.Normal, letterSpacing = (-2.4).sp)
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 10.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)).uppercase(locale), family, mutedTextColor, 16.sp, FontWeight.SemiBold, letterSpacing = 1.3.sp)
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Hour and minute both italic; italic date beneath; optional italic meridiem beside the time. */
@Composable
private fun ClockTemplateData.ItalicAccentTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 89.sp, shadow = homeTextShadow(textColor)),
                color = textColor,
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 12.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 20.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 20.sp)
    }
}

/**
 * Hour on its own row, minute (with an optional meridiem beside it) on the next — a true vertical
 * stack, not a single time string. [boldHour] is [ClockTemplateId.VERTICAL_STACK_BOLD_HOUR]'s
 * only difference from [ClockTemplateId.VERTICAL_STACK] — same tight two-line spacing either way.
 */
@Composable
private fun ClockTemplateData.VerticalStackTemplate(
    boldHour: Boolean,
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    val rowStyle = TextStyle(
        fontFamily = family,
        fontSize = 74.sp,
        letterSpacing = (-2.4).sp,
        shadow = homeTextShadow(textColor),
    )
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        // A tighter lineHeight/Trim.Both on each Text individually stopped closing this gap once
        // the minute row also had to carry an optional meridiem beside it (that split hour/minute
        // into two independent Texts instead of one two-line one, and Compose's line trimming
        // doesn't reach across separate composables) — an explicit negative spacedBy between them
        // is the reliable fix regardless of font size, measured directly against a device
        // screenshot (see chat history) rather than assumed from the line-height math.
        Column(verticalArrangement = Arrangement.spacedBy((-24).dp), horizontalAlignment = horizontalAlignment) {
            Text(
                text = now.format(hourFormatter),
                style = rowStyle.copy(fontWeight = if (boldHour) FontWeight.Black else FontWeight.Light),
                color = textColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = now.format(minuteFormatter),
                    style = rowStyle.copy(fontWeight = FontWeight.Normal),
                    color = textColor,
                )
                if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp))
            }
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 22.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 12.dp),
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 22.sp, fontWeight = if (boldHour) FontWeight.Black else FontWeight.Medium)
    }
}

/** [timeInWords] instead of digits — "Eleven Twenty Two" — always spoken 12-hour, see that function's doc. No meridiem slot: the toggle has no effect here. */
@Composable
private fun ClockTemplateData.SpelledOutTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp), horizontalAlignment = horizontalAlignment) {
        Text(
            text = timeInWords(now, locale),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 46.sp, lineHeight = 50.sp, shadow = homeTextShadow(textColor)),
            color = textColor,
        )
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 19.sp, shadow = homeTextShadow(mutedTextColor)),
            color = mutedTextColor,
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 19.sp)
    }
}

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexWide = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(180f),
            FontVariation.weight(840)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexNarrow = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(30f),
            FontVariation.weight(300),
            // "auto" optical sizing (CSS's font-optical-sizing: auto default) tracks the rendered
            // point size — this template's digits render at 96.sp, so opsz follows that directly
            // rather than a fixed value, unlike the fixed axis values above.
            FontVariation.Setting("opsz", 96f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val TechDistorted = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("GRAD", 180f),
            FontVariation.Setting("XTRA", 540f),
            FontVariation.weight(600)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexWideThin = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(180f),
            FontVariation.weight(120)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexNarrowBlack = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(30f),
            FontVariation.weight(1000)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexLightCondensed = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(90f),
            FontVariation.weight(360)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexUltraLight = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(120f),
            FontVariation.weight(120)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
private val RobotoFlexBlack = FontFamily(
    androidx.compose.ui.text.font.Font(
        com.facetlauncher.app.R.font.roboto_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.width(120f),
            FontVariation.weight(1000)
        )
    )
)

@Composable
private fun ClockTemplateData.BracketMinimalTemplate(
    modifier: Modifier = Modifier,
) {
    val timeFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH : mm" else "h : mm", locale)
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.clock_template_bracket_open),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 77.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor
            )
            Text(
                text = now.format(timeFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexLightCondensed,
                    fontSize = 77.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Text(
                text = stringResource(R.string.clock_template_bracket_close),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 77.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)).uppercase(locale),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                letterSpacing = 2.4.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 10.dp)
        )
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(text = stringResource(R.string.clock_template_bracket_open), style = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraLight, fontSize = 17.sp, shadow = homeTextShadow(mutedTextColor)), color = mutedTextColor)
                ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 17.sp)
                Text(text = stringResource(R.string.clock_template_bracket_close), style = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraLight, fontSize = 17.sp, shadow = homeTextShadow(mutedTextColor)), color = mutedTextColor)
            }
        }
    }
}

@Composable
private fun ClockTemplateData.TwoLineDividerTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(
            text = now.format(hourFormatter),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 72.sp,
                shadow = homeTextShadow(textColor)
            ),
            color = textColor
        )
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(1.dp)
                .background(textColor.copy(alpha = 0.4f))
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Light,
                    fontSize = 72.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 19.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 12.dp)
        )
        if (nextAlarmText != null || batteryPercent != null) {
            Box(modifier = Modifier.padding(top = 10.dp).width(48.dp).height(1.dp).background(textColor.copy(alpha = 0.4f)))
            ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, modifier = Modifier.padding(top = 10.dp), fontSize = 19.sp)
        }
    }
}

@Composable
private fun ClockTemplateData.BoldColonTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexUltraLight,
                    fontSize = 96.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Text(
                text = stringResource(R.string.clock_template_colon),
                style = TextStyle(
                    fontFamily = RobotoFlexBlack,
                    fontSize = 96.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexUltraLight,
                    fontSize = 96.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 5.dp)
        )
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 5.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 20.sp)
                if (nextAlarmText != null && batteryPercent != null) {
                    Text(text = stringResource(R.string.clock_template_colon), style = TextStyle(fontFamily = RobotoFlexBlack, fontSize = 20.sp, shadow = homeTextShadow(textColor)), color = textColor)
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun ClockTemplateData.VariableDividerTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)

    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(19.dp)) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexWideThin,
                    fontSize = 96.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(72.dp)
                    .background(textColor.copy(alpha = 0.5f))
            )
            Text(
                text = now.format(minuteFormatter),
                style = TextStyle(
                    fontFamily = RobotoFlexNarrowBlack,
                    fontSize = 96.sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) {
                MeridiemText(meridiem, family, textColor)
            }
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 5.dp)
        )
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 5.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 22.sp)
                if (nextAlarmText != null && batteryPercent != null) {
                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(mutedTextColor.copy(alpha = 0.5f)))
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 22.sp)
            }
        }
    }
}

/**
 * Hour and minute each on their own tightly-overlapped line (-12dp). [inverted] swaps which one
 * gets the thin/wide treatment vs the bold/narrow one — [ClockTemplateId.FLUID_STACK] (hour
 * thin/wide, minute bold/narrow) vs [ClockTemplateId.FLUID_STACK_INVERTED] (the reverse), the
 * same shared-boolean-variant shape [VerticalStackTemplate]'s own `boldHour` already uses.
 * [accentHour] additionally fixes the hour to [accentColor] regardless of the user's chosen
 * [textColor] — see [AccentContrastTemplate]'s own doc for why — backing
 * [ClockTemplateId.ACCENTED_FLUID_STACK]/[ClockTemplateId.ACCENTED_FLUID_STACK_INVERTED].
 */
@Composable
private fun ClockTemplateData.FluidStackTemplate(
    inverted: Boolean,
    accentHour: Boolean,
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    val hourFontFamily = if (inverted) RobotoFlexNarrowBlack else RobotoFlexWideThin
    val minuteFontFamily = if (inverted) RobotoFlexWideThin else RobotoFlexNarrowBlack
    val hourColor = if (accentHour) accentColor else textColor

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy((-14).dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = now.format(hourFormatter),
                style = TextStyle(
                    fontFamily = hourFontFamily,
                    fontSize = 108.sp,
                    shadow = homeTextShadow(hourColor)
                ),
                color = hourColor
            )
            if (meridiem != null) {
                MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 19.dp, start = 10.dp))
            }
        }
        Text(
            text = now.format(minuteFormatter),
            style = TextStyle(
                fontFamily = minuteFontFamily,
                fontSize = 108.sp,
                shadow = homeTextShadow(textColor)
            ),
            color = textColor
        )
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 19.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(top = 19.dp)
        )
        // Both branches below compensate for this Column's own Arrangement.spacedBy((-14).dp)
        // (needed to tighten the huge 108sp digit rows' line-height gaps, but applying between
        // EVERY child, including this one) with their own top padding — 16.dp nets a tight ~2dp
        // gap below the date line, deliberately closer than the date's own 19.dp/~5dp gap above
        // it, without regressing to the negative/overlapping gap this used to have (see chat
        // history).
        if (accentHour) {
            // `8r`/`8s` — accent hour, accent alarm time; nothing else takes colour.
            if (nextAlarmText != null || batteryPercent != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 16.dp)) {
                    AlarmAccessoryContent(nextAlarmText, accentColor, family, fontSize = 19.sp)
                    if (nextAlarmText != null && batteryPercent != null) {
                        Text(text = stringResource(R.string.clock_accessory_separator_dot), style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 19.sp, shadow = homeTextShadow(mutedTextColor)), color = mutedTextColor)
                    }
                    BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 19.sp)
                }
            }
        } else {
            // `8m`/`8n` — shares the date's own light, quiet block under the digits.
            ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 19.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun ClockTemplateData.RobotoFlexWideTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = RobotoFlexWide,
                    fontSize = 86.sp,
                    letterSpacing = (-2.4).sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) MeridiemText(meridiem, RobotoFlexWide, textColor, Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)).uppercase(locale),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                letterSpacing = 2.4.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 17.sp)
    }
}

@Composable
private fun ClockTemplateData.RobotoFlexNarrowTemplate(
    modifier: Modifier = Modifier,
) {
    // This template's own two columns (hour/minute digits, then meridiem+abbreviated date) sit
    // side by side rather than stacked, so there's no single Column to hand horizontalAlignment
    // to — Arrangement.spacedBy(space, alignment) is the Row equivalent, positioning the whole
    // two-column unit within the available width the same way horizontalAlignment does elsewhere.
    // At Right alignment the two columns also swap order — a straight slide-right of the same
    // Time-then-Date reading order would land the date, not the time, on the actual right edge;
    // swapping mirrors the Left-alignment layout instead, so the time digits are always the
    // element hugging the aligned edge. The date column's own internal alignment follows suit
    // (Start normally; End when mirrored) so its text hugs that same edge, not just the column's
    // outer position.
    val isRightAligned = horizontalAlignment == Alignment.End
    val timeColumn: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)),
                style = TextStyle(fontFamily = RobotoFlexNarrow, fontSize = 96.sp, lineHeight = 96.sp, shadow = homeTextShadow(textColor)),
                color = textColor
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern("mm", locale)),
                style = TextStyle(fontFamily = RobotoFlexNarrow, fontSize = 96.sp, lineHeight = 96.sp, shadow = homeTextShadow(textColor)),
                color = textColor
            )
        }
    }
    val dateColumn: @Composable () -> Unit = {
        Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start) {
            if (meridiem != null) {
                MeridiemText(meridiem, RobotoFlexNarrow, textColor)
            }
            Text(
                text = now.format(DateTimeFormatter.ofPattern("EEE\nd MMM", locale)).uppercase(locale),
                style = TextStyle(
                    fontFamily = family,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    shadow = homeTextShadow(mutedTextColor)
                ),
                color = mutedTextColor,
                textAlign = if (isRightAligned) TextAlign.End else TextAlign.Start,
            )
            // One accessory per line (not the shared ClockAccessoryRow's single horizontal line)
            // so the column grows downward and stays narrow, matching this template's own design.
            Column(
                horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 7.dp),
            ) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 19.sp)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 19.sp)
            }
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(19.dp, horizontalAlignment),
    ) {
        if (isRightAligned) {
            dateColumn()
            timeColumn()
        } else {
            timeColumn()
            dateColumn()
        }
    }
}

@Composable
private fun ClockTemplateData.TechDistortedTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = now.format(timeFormatter(use24HourTime, locale)),
                style = TextStyle(
                    fontFamily = TechDistorted,
                    fontSize = 108.sp,
                    letterSpacing = (-6).sp,
                    shadow = homeTextShadow(textColor)
                ),
                color = textColor
            )
            if (meridiem != null) {
                Text(
                    text = meridiem,
                    style = TextStyle(
                        fontFamily = TechDistorted,
                        fontSize = 29.sp,
                        shadow = homeTextShadow(textColor)
                    ),
                    color = textColor,
                    modifier = Modifier.padding(bottom = 14.dp, start = 5.dp)
                )
            }
        }
        Text(
            text = now.format(dateFormatter(locale, dateStyle)),
            style = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Light,
                fontSize = 24.sp,
                shadow = homeTextShadow(mutedTextColor)
            ),
            color = mutedTextColor,
            modifier = Modifier.padding(start = 2.4.dp)
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, TechDistorted, mutedTextColor, accentColor, modifier = Modifier.padding(start = 2.4.dp), fontSize = 24.sp)
    }
}

/**
 * [WeightContrastTemplate]'s own single-line hour+minute, but the hour is always rendered in
 * [accentColor] rather than the user's chosen [textColor] — a fixed accent color for the hour paired
 * with user-choice color for the minute and date, per direct request (see chat history). Hour and
 * minute keep that template's weight contrast (Black vs Light here) on top of the new color
 * contrast. [FluidStackTemplate]'s own `accentHour` param applies this same idea to that
 * template's shape instead ([ClockTemplateId.ACCENTED_FLUID_STACK]/`_INVERTED`).
 */
@Composable
private fun ClockTemplateData.AccentContrastTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            HourText(now.format(hourFormatter), family, accentColor, 84.sp, FontWeight.Black, letterSpacing = (-2.4).sp)
            SeparatorText(now.format(DateTimeFormatter.ofPattern(":mm", locale)), family, textColor, 84.sp, FontWeight.Light, letterSpacing = (-2.4).sp)
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 10.dp))
        }
        TemplateDateText(
            now.format(dateFormatter(locale, dateStyle)).uppercase(locale),
            family,
            mutedTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.3.sp,
        )
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmAccessoryContent(nextAlarmText, accentColor, family, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (nextAlarmText != null && batteryPercent != null) {
                    Text(text = stringResource(R.string.clock_accessory_separator_dot), style = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, shadow = homeTextShadow(mutedTextColor)), color = mutedTextColor)
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** `6a` — the hour sits in an [accentColor]-filled rounded block, knocked out in [Surface] tone (the same knockout convention `Theme.kt`'s own `onPrimary = Surface` uses for text on an accentColor fill); the minute stays plain [textColor] beside it. The first template in this file to use a shape (`clip`/`background`) rather than pure typography. */
@Composable
private fun ClockTemplateData.AccentFieldTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(accentColor, RoundedCornerShape(22.dp))
                    .padding(start = 19.dp, end = 19.dp, top = 7.dp, bottom = 12.dp),
            ) {
                HourText(now.format(hourFormatter), family, Surface, 77.sp, FontWeight.SemiBold)
            }
            // Same 12dp as the block's bottom padding (and the meridiem below) — the hour sits that far above the row's bottom edge, so the minute must too or its baseline lands lower.
            MinuteText(now.format(minuteFormatter), family, textColor, 77.sp, FontWeight.ExtraLight, modifier = Modifier.padding(bottom = 12.dp))
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(bottom = 12.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AlarmAccessoryContent(
                    nextAlarmText, Surface, family,
                    modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(accentColor, RoundedCornerShape(7.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                )
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family)
            }
        }
    }
}

/** `6b` — hour in a filled square-ish tile, knocked out in [contentColorFor]'s derived color on a [textColor] fill (flips with the tile across `THEME`/`THEME_INVERTED`; the inverse knockout of [AccentFieldTemplate]); minute unbounded beside it. */
@Composable
private fun ClockTemplateData.HourTileTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    val tileContentColor = contentColorFor(textColor)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(textColor, RoundedCornerShape(24.dp))
                    .padding(17.dp),
                contentAlignment = Alignment.Center,
            ) {
                HourText(now.format(hourFormatter), family, tileContentColor, 55.sp, FontWeight.SemiBold)
            }
            MinuteText(now.format(minuteFormatter), family, textColor, 72.sp, FontWeight.Light)
            if (meridiem != null) MeridiemText(meridiem, family, textColor)
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)).uppercase(locale), family, mutedTextColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.3.sp)
        if (nextAlarmText != null || batteryPercent != null) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 14.sp)
                if (nextAlarmText != null && batteryPercent != null) {
                    Box(modifier = Modifier.width(40.dp).height(1.dp).background(Hairline))
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 14.sp)
            }
        }
    }
}

/** `6d` — the whole time inside one bordered/filled pill with a small [accentColor] status dot leading it; reads as a control rather than a headline. */
@Composable
private fun ClockTemplateData.ChipTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceContainer, CircleShape)
                .border(1.dp, Hairline, CircleShape)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor)) {}
            ClockGlyphText(now.format(timeFormatter(use24HourTime, locale)), textColor, 36.sp, family, FontWeight.Medium)
            if (meridiem != null) MeridiemText(meridiem, family, mutedTextColor)
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            val pillModifier = Modifier.clip(CircleShape).background(SurfaceContainer, CircleShape).border(1.dp, Hairline, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 14.sp, modifier = pillModifier)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 14.sp, modifier = pillModifier)
            }
        }
    }
}

/** `6h` — an [accentColor], heavy-weight hour overlapping a light-weight [textColor] minute on the same baseline; color does the separating that weight alone can't. */
@Composable
private fun ClockTemplateData.DuotoneOverlapTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            HourText(now.format(hourFormatter), family, accentColor, 100.sp, FontWeight.Black)
            MinuteText(now.format(minuteFormatter), family, textColor, 100.sp, FontWeight.Light, modifier = Modifier.offset(x = (-17).dp))
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 5.dp, bottom = 17.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmAccessoryContent(nextAlarmText, accentColor, family, fontSize = 19.sp)
                if (nextAlarmText != null && batteryPercent != null) {
                    Text(text = stringResource(R.string.clock_accessory_separator_dot), style = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 19.sp, shadow = homeTextShadow(mutedTextColor)), color = mutedTextColor)
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 19.sp)
            }
        }
    }
}

/** `6i` — two [accentColor] L-shaped corner rules crop the time from opposite corners; the date sits below the frame, outside it. */
@Composable
private fun ClockTemplateData.CornerFrameTemplate(
    modifier: Modifier = Modifier,
) {
    val frameColor = accentColor
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Box(
            modifier = Modifier
                .drawBehind {
                    val armLength = 24.dp.toPx()
                    val strokeWidth = 2.4.dp.toPx()
                    drawLine(frameColor, Offset(0f, 0f), Offset(armLength, 0f), strokeWidth)
                    drawLine(frameColor, Offset(0f, 0f), Offset(0f, armLength), strokeWidth)
                    drawLine(frameColor, Offset(size.width, size.height), Offset(size.width - armLength, size.height), strokeWidth)
                    drawLine(frameColor, Offset(size.width, size.height), Offset(size.width, size.height - armLength), strokeWidth)
                }
                .padding(19.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                ClockGlyphText(now.format(timeFormatter(use24HourTime, locale)), textColor, 70.sp, family, FontWeight.Light)
                if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 7.dp))
            }
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier.size(9.dp).drawBehind {
                        val strokeWidth = 1.4.dp.toPx()
                        drawLine(frameColor, Offset(0f, size.height), Offset(0f, size.height * 0.35f), strokeWidth)
                        drawLine(frameColor, Offset(0f, size.height), Offset(size.width * 0.65f, size.height), strokeWidth)
                    },
                )
                ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 19.sp)
            }
        }
    }
}

/** `6j` — a bordered/filled card with an [accentColor] spine, the time + a meridiem/day line, and (past a dashed divider) a small day-of-month stub. The most literally container-like of the shape-based templates. */
@Composable
private fun ClockTemplateData.StubTemplate(
    modifier: Modifier = Modifier,
) {
    // width(IntrinsicSize.Max) bounds this Column to its own widest child — same fix as
    // RuleMeridiemTemplate's own doc comment explains: without it, the accessory row's
    // fillMaxWidth() below resolves against Home's own full screen width instead of this card's
    // content, rendering far wider there than in the Style Gallery's own bounded preview card.
    Column(modifier = modifier.width(IntrinsicSize.Max), horizontalAlignment = horizontalAlignment) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainer, RoundedCornerShape(14.dp))
                .border(1.dp, Hairline, RoundedCornerShape(14.dp)),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 17.dp, horizontal = 19.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(3.6.dp)
                        .height(58.dp)
                        .background(accentColor, RoundedCornerShape(2.4.dp)),
                ) {}
                Column(modifier = Modifier.padding(start = 17.dp, end = 17.dp)) {
                    ClockGlyphText(now.format(timeFormatter(use24HourTime, locale)), textColor, 48.sp, family, FontWeight.Light)
                    val meridiemDayLine = now.format(DateTimeFormatter.ofPattern("EEEE", locale)).let { dayName ->
                        if (meridiem != null) "$meridiem · $dayName" else dayName
                    }
                    TemplateDateText(meridiemDayLine, family, mutedTextColor, fontSize = 14.sp)
                }
                val dividerColor = Hairline
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .drawBehind {
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
                            )
                        },
                ) {}
                Column(modifier = Modifier.padding(start = 17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TemplateDateText(now.format(DateTimeFormatter.ofPattern("MMM", locale)).uppercase(locale), family, mutedTextColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                    ClockGlyphText(now.format(DateTimeFormatter.ofPattern("d", locale)), textColor, 31.sp, family, FontWeight.Light)
                }
            }
            if (nextAlarmText != null || batteryPercent != null) StubAccessoryRow()
        }
    }
}

/** [StubTemplate]'s alarm/battery footer, set off from the card body by a dashed rule. */
@Composable
private fun ClockTemplateData.StubAccessoryRow() {
    val accessoryDividerColor = Hairline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(accessoryDividerColor, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
            }
            .padding(horizontal = 19.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlarmAccessoryContent(nextAlarmText, mutedTextColor, family)
        BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family)
    }
}

/** `6k` — a large thin [accentColor]-stroked circle bleeding past the time's own bounds, drawn behind it via [drawBehind] (so the bleed is decorative only and never grows this template's own measured/tappable size). */
@Composable
private fun ClockTemplateData.HaloTemplate(
    modifier: Modifier = Modifier,
) {
    val haloColor = accentColor
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            ClockGlyphText(
                now.format(timeFormatter(use24HourTime, locale)),
                textColor,
                70.sp,
                family,
                FontWeight.Light,
                modifier = Modifier.drawBehind {
                    val radius = maxOf(size.width, size.height) / 2f + 29.dp.toPx()
                    drawCircle(color = haloColor, radius = radius, style = Stroke(width = 1.8.dp.toPx()))
                },
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 7.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 19.sp)
    }
}

/** `6m` — each digit of the time in its own bordered/filled cell via the local [DigitCell]; the colon between cells renders plain, in [accentColor], not inside a cell. Mechanical counterpart to [FluidStackTemplate]'s fluid stacks. */
@Composable
private fun ClockTemplateData.DigitCellsTemplate(
    modifier: Modifier = Modifier,
) {
    val hourDigits = now.format(DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)).toList()
    val minuteDigits = now.format(DateTimeFormatter.ofPattern("mm", locale)).toList()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            hourDigits.forEach { digit -> DigitCell(digit.toString(), family, textColor) }
            SeparatorText(stringResource(R.string.clock_template_colon), family, accentColor, 38.sp, FontWeight.Bold)
            minuteDigits.forEach { digit -> DigitCell(digit.toString(), family, textColor) }
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)).uppercase(locale), family, mutedTextColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.3.sp)
        if (nextAlarmText != null || batteryPercent != null) {
            val cellModifier = Modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceContainer, RoundedCornerShape(8.dp)).border(1.dp, Hairline, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 6.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 14.sp, modifier = cellModifier)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 14.sp, modifier = cellModifier)
            }
        }
    }
}

@Composable
private fun DigitCell(digit: String, family: FontFamily, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainer, RoundedCornerShape(12.dp))
            .border(1.dp, Hairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        ClockGlyphText(digit, color, 41.sp, family, FontWeight.Light)
    }
}

/** `7a` — the whole clock sits on one filled [textColor]-tone panel, digits/accessory row knocked out in [contentColorFor]'s derived color (flips with the panel across `THEME`/`THEME_INVERTED`), date knocked out in [accentColor]. The inverse of every other template. */
@Composable
private fun ClockTemplateData.NegativePanelTemplate(
    modifier: Modifier = Modifier,
) {
    val panelContentColor = contentColorFor(textColor)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(textColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 19.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            ClockGlyphText(now.format(timeFormatter(use24HourTime, locale)), panelContentColor, 55.sp, family, FontWeight.Light)
            if (meridiem != null) MeridiemText(meridiem, family, panelContentColor, Modifier.padding(start = 7.dp, bottom = 7.dp))
        }
        TemplateDateText(
            now.format(dateFormatter(locale, dateStyle)).uppercase(locale),
            family,
            accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.3.sp,
            modifier = Modifier.padding(top = 10.dp),
            shadow = false,
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, panelContentColor.copy(alpha = 0.62f), accentColor, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp)
    }
}

/** `7b` — the hour rendered as a stroked outline glyph via [HourText]'s `drawStyle`, against a solid [MinuteText]; weight contrast done with outline instead of mass. */
@Composable
private fun ClockTemplateData.HollowHourTemplate(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            HourText(
                now.format(hourFormatter),
                family,
                textColor,
                100.sp,
                FontWeight.Black,
                drawStyle = Stroke(width = with(density) { 1.9.dp.toPx() }),
            )
            MinuteText(now.format(minuteFormatter), family, textColor, 100.sp, FontWeight.Normal)
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 17.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            val boxModifier = Modifier.clip(RoundedCornerShape(9.dp)).border(1.dp, Hairline, RoundedCornerShape(9.dp)).padding(horizontal = 11.dp, vertical = 5.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 14.sp, modifier = boxModifier)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 14.sp, modifier = boxModifier)
            }
        }
    }
}

/** `7c` — a translucent [accentColor] band drawn behind the lower half of the time digits via [drawBehind], like a marker stroke. */
@Composable
private fun ClockTemplateData.HighlighterTemplate(
    modifier: Modifier = Modifier,
) {
    val bandColor = accentColor.copy(alpha = 0.28f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            ClockGlyphText(
                now.format(timeFormatter(use24HourTime, locale)),
                textColor,
                70.sp,
                family,
                FontWeight.Light,
                modifier = Modifier.drawBehind {
                    val bandTop = size.height * 0.55f
                    drawRoundRect(
                        color = bandColor,
                        topLeft = Offset(-5.dp.toPx(), bandTop),
                        size = Size(size.width + 10.dp.toPx(), size.height - bandTop),
                        cornerRadius = CornerRadius(5.dp.toPx()),
                    )
                },
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 7.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            val bandModifier = Modifier.drawBehind {
                val bandTop = size.height * 0.55f
                drawRoundRect(color = bandColor, topLeft = Offset(-2.dp.toPx(), bandTop), size = Size(size.width + 4.dp.toPx(), size.height - bandTop), cornerRadius = CornerRadius(2.dp.toPx()))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, modifier = bandModifier, fontSize = 19.sp)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, modifier = bandModifier, fontSize = 19.sp)
            }
        }
    }
}

/** `7d` — a thin [accentColor] rule beside a two-line hour/minute vertical stack (tight negative line spacing, matching [VerticalStackTemplate]'s own convention). Smallest amount of shape that still changes the read. */
@Composable
private fun ClockTemplateData.ColumnRuleTemplate(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(19.dp)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(2.4.dp))
                    .background(accentColor),
            ) {}
            ColumnRuleDigits()
        }
        TemplateDateText(
            now.format(dateFormatter(locale, dateStyle)).uppercase(locale),
            family,
            mutedTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.3.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
    }
}

/** [ColumnRuleTemplate]'s two-line hour/minute stack, with the meridiem beside the minutes. */
@Composable
private fun ClockTemplateData.ColumnRuleDigits() {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(verticalArrangement = Arrangement.spacedBy((-24).dp)) {
        HourText(now.format(hourFormatter), family, textColor, 74.sp, FontWeight.Light, letterSpacing = (-2.4).sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MinuteText(now.format(minuteFormatter), family, textColor, 74.sp, FontWeight.Normal, letterSpacing = (-2.4).sp)
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp))
        }
    }
}

/** `7e`— accent applied to nothing but the colon, in a small filled [accentColor] badge between the digits, knocked out in [Surface]. */
@Composable
private fun ClockTemplateData.ColonMarkTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HourText(now.format(hourFormatter), family, textColor, 72.sp, FontWeight.Light)
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor, RoundedCornerShape(11.dp))
                    .padding(7.dp),
                contentAlignment = Alignment.Center,
            ) {
                SeparatorText(stringResource(R.string.clock_template_colon), family, Surface, 26.sp, FontWeight.Bold)
            }
            MinuteText(now.format(minuteFormatter), family, textColor, 72.sp, FontWeight.Light)
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 19.sp)
                if (nextAlarmText != null && batteryPercent != null) {
                    Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(1.dp)).background(accentColor))
                }
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 19.sp)
            }
        }
    }
}

/** `7g` — hour and minute each in their own bordered/filled pill via the local [TimePill], with a small [accentColor]-filled meridiem pill trailing (omitted entirely, like every other template's [MeridiemText], when there's no meridiem to show). */
@Composable
private fun ClockTemplateData.PillPairTemplate(
    modifier: Modifier = Modifier,
) {
    val hourFormatter = DateTimeFormatter.ofPattern(if (use24HourTime) "HH" else "h", locale)
    val minuteFormatter = DateTimeFormatter.ofPattern("mm", locale)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimePill(now.format(hourFormatter), family, textColor)
            TimePill(now.format(minuteFormatter), family, textColor)
            if (meridiem != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    ClockGlyphText(meridiem, Surface, 16.sp, family, FontWeight.SemiBold, letterSpacing = 1.sp)
                }
            }
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            val pillModifier = Modifier.clip(CircleShape).background(SurfaceContainer, CircleShape).border(1.dp, Hairline, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family, fontSize = 14.sp, modifier = pillModifier)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family, fontSize = 14.sp, modifier = pillModifier)
            }
        }
    }
}

@Composable
private fun TimePill(text: String, family: FontFamily, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceContainer, CircleShape)
            .border(1.dp, Hairline, CircleShape)
            .padding(horizontal = 19.dp, vertical = 10.dp),
    ) {
        ClockGlyphText(text, color, 46.sp, family, FontWeight.Light)
    }
}

/** `7h` — one wide bordered/filled row: time leading, a stacked day-of-week/day-of-month pair trailing, inside one shared card. The most horizontal/row-like of the shape-based templates — deliberately wrap-content rather than edge-to-edge (a full-width shelf would reintroduce a large, mostly-empty tappable area, exactly the hit-box bug this file's templates were all fixed to avoid — see [com.facetlauncher.app.ui.home.HomeScreen]'s own doc on the clock's tap/long-press box). No separate date line beneath, since the day info already lives inside the row. */
@Composable
private fun ClockTemplateData.ShelfTemplate(
    modifier: Modifier = Modifier,
) {
    // width(IntrinsicSize.Max) bounds this Column to its own widest child — same fix as
    // RuleMeridiemTemplate's own doc comment explains: without it, the accessory row's
    // fillMaxWidth() below resolves against Home's own full screen width instead of this card's
    // content, rendering far wider there than in the Style Gallery's own bounded preview card.
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, Hairline, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(34.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                ClockGlyphText(now.format(timeFormatter(use24HourTime, locale)), textColor, 55.sp, family, FontWeight.Light)
                if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 5.dp, bottom = 5.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                TemplateDateText(now.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase(locale), family, mutedTextColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.3.sp)
                ClockGlyphText(now.format(DateTimeFormatter.ofPattern("d", locale)), textColor, 26.sp, family, FontWeight.Light)
            }
        }
        if (nextAlarmText != null || batteryPercent != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlarmAccessoryContent(nextAlarmText, mutedTextColor, family)
                BatteryAccessoryContent(batteryPercent, isCharging, mutedTextColor, accentColor, family)
            }
        }
    }
}

/** `7j` — the time digits sit half-submerged in an [accentColor] field that multiplies over their own lower half, via a real [BlendMode.Multiply] composited in an offscreen [graphicsLayer] (so it blends against the glyph's own rasterized pixels, not the wallpaper behind them). */
@Composable
private fun ClockTemplateData.HalfImmersedTemplate(
    modifier: Modifier = Modifier,
) {
    val immersionColor = accentColor
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(17.dp), horizontalAlignment = horizontalAlignment) {
        Row(verticalAlignment = Alignment.Bottom) {
            ClockGlyphText(
                now.format(timeFormatter(use24HourTime, locale)),
                textColor,
                77.sp,
                family,
                FontWeight.Normal,
                modifier = Modifier
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        val bandTop = size.height * 0.55f
                        drawRect(
                            color = immersionColor,
                            topLeft = Offset(0f, bandTop),
                            size = Size(size.width, size.height - bandTop),
                            blendMode = BlendMode.Hardlight,
                        )
                    },
            )
            if (meridiem != null) MeridiemText(meridiem, family, textColor, Modifier.padding(start = 7.dp, bottom = 10.dp))
        }
        TemplateDateText(now.format(dateFormatter(locale, dateStyle)), family, mutedTextColor)
        if (nextAlarmText != null || batteryPercent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(width = 10.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(accentColor))
                ClockAccessoryRow(nextAlarmText, batteryPercent, isCharging, family, mutedTextColor, accentColor, fontSize = 19.sp)
            }
        }
    }
}
