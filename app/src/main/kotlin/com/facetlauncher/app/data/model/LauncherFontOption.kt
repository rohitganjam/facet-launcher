package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * Settings → Appearance → "Font" — the base font for every text role in the app except the
 * clock/calendar (`ClockFontOption`), which pick their own font independently. `SYSTEM` (the
 * default) keeps the platform's own font, matching this app's original shipped look. The actual
 * bundled [androidx.compose.ui.text.font.FontFamily] each option resolves to
 * (`ui/theme/ClockFonts.kt`) stays in the UI layer, same split as [ClockFontOption].
 */
enum class LauncherFontOption(@param:StringRes val displayNameRes: Int) {
    SYSTEM(R.string.font_name_system),
    ROBOTO_FLEX(R.string.font_name_roboto_flex),
    NOTO_SANS(R.string.font_name_noto_sans),
    MANROPE(R.string.font_name_manrope),
    POPPINS(R.string.font_name_poppins),
    INTER(R.string.font_name_inter),
    MONTSERRAT(R.string.font_name_montserrat),
    LATO(R.string.font_name_lato),
}
