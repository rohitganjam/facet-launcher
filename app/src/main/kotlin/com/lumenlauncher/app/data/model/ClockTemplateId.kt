package com.lumenlauncher.app.data.model

/**
 * Named clock layouts — see `ui/home/clock/ClockTemplates.kt` for the actual rendering. Lives in
 * `data/model` (not `ui/`) purely so it can be a real typed field on [LauncherSettings]/persisted
 * by `SettingsRepository`, matching [DockDisplayMode]/[NotificationBadgeStyle]'s own pattern —
 * this enum carries no Compose/Android-framework dependency itself, only a display label.
 */
enum class ClockTemplateId(val displayName: String) {
    LIGHT_STACK("Light stack"),
    RULE_MERIDIEM("Ruler"),
    DATE_FORWARD("Date forward"),
    WEIGHT_CONTRAST("Weight contrast"),
    ITALIC_ACCENT("Italics"),
    SPELLED_OUT("Spelled out"),
    VERTICAL_STACK("Vertical stack"),
    VERTICAL_STACK_BOLD_HOUR("Vertical stack (bold hour)"),
    ROBOTO_FLEX_WIDE("Flex Wide"),
    ROBOTO_FLEX_NARROW("Flex Narrow"),
    TECH_DISTORTED("Tech Distorted"),
    VARIABLE_DIVIDER("Variable Divider"),
    FLUID_STACK("Fluid Stack"),
    FLUID_STACK_INVERTED("Fluid Stack Inverted"),
    BRACKET_MINIMAL("Bracket Minimal"),
    TWO_LINE_DIVIDER("Two-Line Divider"),
    BOLD_COLON("Bold Colon"),
    ACCENTED_FLUID_STACK("Accented Fluid Stack"),
    ACCENTED_FLUID_STACK_INVERTED("Accented Fluid Stack Inverted"),
    ACCENT_CONTRAST("Accent Contrast"),

    // Shape-based templates (filled tiles, pills, borders, circles) — the first templates in this
    // enum that aren't purely typographic; see ClockTemplates.kt for the actual rendering and its
    // shared HourText/MinuteText/SeparatorText/TemplateDateText part composables.
    ACCENT_FIELD("Accent Field"),
    HOUR_TILE("Hour Tile"),
    CHIP("Chip"),
    DUOTONE_OVERLAP("Duotone Overlap"),
    CORNER_FRAME("Corner Frame"),
    STUB("Stub"),
    HALO("Halo"),
    DIGIT_CELLS("Digit Cells"),
    NEGATIVE_PANEL("Negative Panel"),
    HOLLOW_HOUR("Hollow Hour"),
    HIGHLIGHTER("Highlighter"),
    COLUMN_RULE("Column Rule"),
    COLON_MARK("Colon Mark"),
    PILL_PAIR("Pill Pair"),
    SHELF("Shelf"),
    HALF_IMMERSED("Half Immersed"),
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
