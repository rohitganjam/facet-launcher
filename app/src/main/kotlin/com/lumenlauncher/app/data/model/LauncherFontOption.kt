package com.lumenlauncher.app.data.model

/**
 * Settings → Appearance → "Font" — the base font for every text role in the app except the
 * clock/calendar (`ClockFontOption`), which pick their own font independently. `SYSTEM` (the
 * default) keeps the platform's own font, matching this app's original shipped look. The actual
 * bundled [androidx.compose.ui.text.font.FontFamily] each option resolves to
 * (`ui/theme/ClockFonts.kt`) stays in the UI layer, same split as [ClockFontOption].
 */
enum class LauncherFontOption(val displayName: String) {
    SYSTEM("System"),
    ROBOTO_FLEX("Roboto Flex"),
    NOTO_SANS("Noto Sans"),
    MANROPE("Manrope"),
    POPPINS("Poppins"),
}
