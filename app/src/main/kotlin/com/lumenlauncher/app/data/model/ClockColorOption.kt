package com.lumenlauncher.app.data.model

/**
 * A clock/calendar text color choice, shared by both, independently pickable. Lives in
 * `data/model` so it can be a real typed field on [LauncherSettings] — the actual
 * [androidx.compose.ui.graphics.Color] each option resolves to (`ui/theme/ClockColors.kt`'s
 * `ClockColorOption.resolve()`) stays in the UI layer, since [INK] is theme(light/dark)-aware.
 */
enum class ClockColorOption(val displayName: String) {
    INK("Ink (default)"),
    WHITE("White"),
    BLACK("Black"),
    ACCENT("Accent"),
}
