package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * A clock font choice. Lives in `data/model` so it can be a real typed field on
 * [LauncherSettings] — the actual bundled [androidx.compose.ui.text.font.FontFamily] each option
 * resolves to (`ui/theme/ClockFonts.kt`'s `ClockFontOption.resolveFontFamily`) stays in the UI
 * layer, since [androidx.compose.ui.text.font.FontFamily]/font resource ids aren't data-layer
 * concerns.
 *
 * [LAUNCHER_DEFAULT] — the default for [LauncherSettings.clockFontOption] — doesn't pick a font of
 * its own; it resolves to whatever [LauncherFontOption] Settings → Appearance → Font is currently
 * set to (propagating [SYSTEM] if that's what the launcher font itself is), so a fresh install's
 * clock follows the launcher-wide font choice rather than being pinned to the system font
 * independently of it. The calendar events strip no longer has its own font choice — it reads
 * this same [LauncherFontOption] directly (see chat history: calendar/appearance styling
 * consolidation).
 */
enum class ClockFontOption(@param:StringRes val displayNameRes: Int) {
    LAUNCHER_DEFAULT(R.string.clock_font_launcher_default),
    SYSTEM(R.string.font_name_system),
    ROBOTO_FLEX(R.string.font_name_roboto_flex),
    NOTO_SANS(R.string.font_name_noto_sans),
    MANROPE(R.string.font_name_manrope),
    POPPINS(R.string.font_name_poppins),
}
