package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * Named clock layouts — see `ui/home/clock/ClockTemplates.kt` for the actual rendering. Lives in
 * `data/model` (not `ui/`) purely so it can be a real typed field on [LauncherSettings]/persisted
 * by `SettingsRepository`, matching [DockDisplayMode]/[NotificationBadgeStyle]'s own pattern —
 * this enum carries no Compose/Android-framework dependency itself, only a display label.
 */
enum class ClockTemplateId(@param:StringRes val displayNameRes: Int) {
    LIGHT_STACK(R.string.clock_template_light_stack),
    RULE_MERIDIEM(R.string.clock_template_rule_meridiem),
    DATE_FORWARD(R.string.clock_template_date_forward),
    WEIGHT_CONTRAST(R.string.clock_template_weight_contrast),
    ITALIC_ACCENT(R.string.clock_template_italic_accent),
    SPELLED_OUT(R.string.clock_template_spelled_out),
    VERTICAL_STACK(R.string.clock_template_vertical_stack),
    VERTICAL_STACK_BOLD_HOUR(R.string.clock_template_vertical_stack_bold_hour),
    ROBOTO_FLEX_WIDE(R.string.clock_template_roboto_flex_wide),
    ROBOTO_FLEX_NARROW(R.string.clock_template_roboto_flex_narrow),
    TECH_DISTORTED(R.string.clock_template_tech_distorted),
    VARIABLE_DIVIDER(R.string.clock_template_variable_divider),
    FLUID_STACK(R.string.clock_template_fluid_stack),
    FLUID_STACK_INVERTED(R.string.clock_template_fluid_stack_inverted),
    BRACKET_MINIMAL(R.string.clock_template_bracket_minimal),
    TWO_LINE_DIVIDER(R.string.clock_template_two_line_divider),
    BOLD_COLON(R.string.clock_template_bold_colon),
    ACCENTED_FLUID_STACK(R.string.clock_template_accented_fluid_stack),
    ACCENTED_FLUID_STACK_INVERTED(R.string.clock_template_accented_fluid_stack_inverted),
    ACCENT_CONTRAST(R.string.clock_template_accent_contrast),

    // Shape-based templates (filled tiles, pills, borders, circles) — the first templates in this
    // enum that aren't purely typographic; see ClockTemplates.kt for the actual rendering and its
    // shared HourText/MinuteText/SeparatorText/TemplateDateText part composables.
    ACCENT_FIELD(R.string.clock_template_accent_field),
    HOUR_TILE(R.string.clock_template_hour_tile),
    CHIP(R.string.clock_template_chip),
    DUOTONE_OVERLAP(R.string.clock_template_duotone_overlap),
    CORNER_FRAME(R.string.clock_template_corner_frame),
    STUB(R.string.clock_template_stub),
    HALO(R.string.clock_template_halo),
    DIGIT_CELLS(R.string.clock_template_digit_cells),
    NEGATIVE_PANEL(R.string.clock_template_negative_panel),
    HOLLOW_HOUR(R.string.clock_template_hollow_hour),
    HIGHLIGHTER(R.string.clock_template_highlighter),
    COLUMN_RULE(R.string.clock_template_column_rule),
    COLON_MARK(R.string.clock_template_colon_mark),
    PILL_PAIR(R.string.clock_template_pill_pair),
    SHELF(R.string.clock_template_shelf),
    HALF_IMMERSED(R.string.clock_template_half_immersed),
}

/**
 * Whether this template renders one glyph/shape in the app's resolved accent color — the Clock
 * Style Gallery's "Accent color" row (see `ui/home/clock/ClockStyleGalleryScreen.kt`) only shows
 * for these, since it would otherwise have no visible effect. See `ClockTemplates.kt`'s own
 * `accentColor` parameter on each of these templates for exactly what element it colors.
 */
val ClockTemplateId.usesAccentColor: Boolean
    get() = when (this) {
        ClockTemplateId.ACCENT_CONTRAST,
        ClockTemplateId.ACCENTED_FLUID_STACK,
        ClockTemplateId.ACCENTED_FLUID_STACK_INVERTED,
        ClockTemplateId.ACCENT_FIELD,
        ClockTemplateId.CHIP,
        ClockTemplateId.DUOTONE_OVERLAP,
        ClockTemplateId.CORNER_FRAME,
        ClockTemplateId.STUB,
        ClockTemplateId.HALO,
        ClockTemplateId.DIGIT_CELLS,
        ClockTemplateId.NEGATIVE_PANEL,
        ClockTemplateId.HIGHLIGHTER,
        ClockTemplateId.COLUMN_RULE,
        ClockTemplateId.COLON_MARK,
        ClockTemplateId.PILL_PAIR,
        ClockTemplateId.HALF_IMMERSED,
        -> true
        ClockTemplateId.LIGHT_STACK,
        ClockTemplateId.RULE_MERIDIEM,
        ClockTemplateId.DATE_FORWARD,
        ClockTemplateId.WEIGHT_CONTRAST,
        ClockTemplateId.ITALIC_ACCENT,
        ClockTemplateId.SPELLED_OUT,
        ClockTemplateId.VERTICAL_STACK,
        ClockTemplateId.VERTICAL_STACK_BOLD_HOUR,
        ClockTemplateId.ROBOTO_FLEX_WIDE,
        ClockTemplateId.ROBOTO_FLEX_NARROW,
        ClockTemplateId.TECH_DISTORTED,
        ClockTemplateId.VARIABLE_DIVIDER,
        ClockTemplateId.FLUID_STACK,
        ClockTemplateId.FLUID_STACK_INVERTED,
        ClockTemplateId.BRACKET_MINIMAL,
        ClockTemplateId.TWO_LINE_DIVIDER,
        ClockTemplateId.BOLD_COLON,
        ClockTemplateId.HOUR_TILE,
        ClockTemplateId.HOLLOW_HOUR,
        ClockTemplateId.SHELF,
        -> false
    }
