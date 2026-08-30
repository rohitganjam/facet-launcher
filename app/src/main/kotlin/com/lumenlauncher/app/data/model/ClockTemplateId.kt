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
}
