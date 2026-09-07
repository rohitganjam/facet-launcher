package com.lumenlauncher.app.data.model

/**
 * Named clock layouts — see `ui/home/clock/ClockTemplates.kt` for the actual rendering. Lives in
 * `data/model` (not `ui/`) purely so it can be a real typed field on [LauncherSettings]/persisted
 * by `SettingsRepository`, matching [DockDisplayMode]/[NotificationBadgeStyle]'s own pattern —
 * this enum carries no Compose/Android-framework dependency itself, only a display label.
 */
enum class ClockTemplateId(val displayName: String) {
    LIGHT_STACK("Light stack"),
    RULE_MERIDIEM("Rule & meridiem"),
    DATE_FORWARD("Date forward"),
    WEIGHT_CONTRAST("Weight contrast"),
    ITALIC_ACCENT("Italic accent"),
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
}
