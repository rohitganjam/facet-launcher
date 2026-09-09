package com.lumenlauncher.app.data.model

/**
 * How verbose the clock's date line reads — [FULL] ("Thursday, 27 August", every template's only
 * behavior before this existed) or [CONDENSED] ("Thu, 27 Aug"). One global setting applied to
 * every template that renders its date line through `ClockTemplates.kt`'s shared `dateFormatter()`
 * helper; the handful of templates with their own bespoke, already-abbreviated date layout (e.g.
 * a two-line "EEE / d MMM" stack, or a split day-of-week/day-of-month stub) aren't affected, since
 * they don't call that shared helper. Lives in `data/model` (no Compose deps) so it can be a real
 * typed field on [LauncherSettings]/persisted by `SettingsRepository`, matching every other clock
 * option enum's own pattern (see [ClockColorOption]/[ClockAlignment]).
 */
enum class ClockDateStyle(val displayName: String) {
    FULL("Full"),
    CONDENSED("Condensed"),
}
