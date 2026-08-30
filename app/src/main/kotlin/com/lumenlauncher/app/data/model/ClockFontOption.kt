package com.lumenlauncher.app.data.model

/**
 * A clock/calendar font choice. Lives in `data/model` so it can be a real typed field on
 * [LauncherSettings] — the actual bundled [androidx.compose.ui.text.font.FontFamily] each option
 * resolves to (`ui/theme/ClockFonts.kt`'s `ClockFontOption.fontFamily`) stays in the UI layer,
 * since [androidx.compose.ui.text.font.FontFamily]/font resource ids aren't data-layer concerns.
 */
enum class ClockFontOption(val displayName: String) {
    SYSTEM("System"),
    ROBOTO_FLEX("Roboto Flex"),
    NOTO_SANS("Noto Sans"),
    MANROPE("Manrope"),
    POPPINS("Poppins"),
}
