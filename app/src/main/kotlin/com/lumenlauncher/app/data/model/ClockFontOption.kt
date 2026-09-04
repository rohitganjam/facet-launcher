package com.lumenlauncher.app.data.model

/**
 * A clock/calendar font choice. Lives in `data/model` so it can be a real typed field on
 * [LauncherSettings] — the actual bundled [androidx.compose.ui.text.font.FontFamily] each option
 * resolves to (`ui/theme/ClockFonts.kt`'s `ClockFontOption.resolveFontFamily`) stays in the UI
 * layer, since [androidx.compose.ui.text.font.FontFamily]/font resource ids aren't data-layer
 * concerns.
 *
 * [LAUNCHER_DEFAULT] — the default for both [LauncherSettings.clockFontOption] and
 * [LauncherSettings.calendarFontOption] — doesn't pick a font of its own; it resolves to whatever
 * [LauncherFontOption] Settings → Appearance → Font is currently set to (propagating [SYSTEM] if
 * that's what the launcher font itself is), so a fresh install's clock/calendar follow the
 * launcher-wide font choice rather than being pinned to the system font independently of it.
 */
enum class ClockFontOption(val displayName: String) {
    LAUNCHER_DEFAULT("Default launcher font"),
    SYSTEM("System"),
    ROBOTO_FLEX("Roboto Flex"),
    NOTO_SANS("Noto Sans"),
    MANROPE("Manrope"),
    POPPINS("Poppins"),
}
