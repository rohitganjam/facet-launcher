package com.lumenlauncher.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.lumenlauncher.app.R
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.LauncherFontOption

/**
 * Fonts bundled specifically for the clock/calendar template system (`ui/home/clock/`) — kept
 * separate from [lumenTypography]'s app-wide font since clock/calendar
 * font choice is deliberately independent of the rest of the app's UI (see the clock template
 * gallery). All four are Google Fonts, OFL-licensed, bundled locally under `res/font/` so they
 * render identically with no network dependency — see `THIRD_PARTY_FONT_LICENSES/` at the repo
 * root for the license text each family ships under.
 *
 * [RobotoFlexFamily] and [ManropeFamily] are each a single variable-font file (weight-axis
 * interpolation handled by the platform from one `Font()` entry — no italic instance exists for
 * either upstream, so requesting [FontStyle.Italic] against them falls back to upright).
 * [NotoSansFamily] ships true upright and italic variable files. [PoppinsFamily] is a classic
 * static family — one file per weight/style combination actually bundled.
 */
private val RobotoFlexFamily = FontFamily(Font(R.font.roboto_flex_variable))

private val ManropeFamily = FontFamily(Font(R.font.manrope_variable))

private val NotoSansFamily = FontFamily(
    Font(R.font.noto_sans_variable, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.noto_sans_italic_variable, FontWeight.Normal, FontStyle.Italic),
)

private val PoppinsFamily = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_medium_italic, FontWeight.Medium, FontStyle.Italic),
)

/**
 * Resolves a [LauncherFontOption] to its real [FontFamily] — reuses the same four bundled
 * families [ClockFontOption] draws from, so picking a launcher-wide font never needs new assets.
 */
val LauncherFontOption.fontFamily: FontFamily
    get() = when (this) {
        LauncherFontOption.SYSTEM -> FontFamily.SansSerif
        LauncherFontOption.ROBOTO_FLEX -> RobotoFlexFamily
        LauncherFontOption.NOTO_SANS -> NotoSansFamily
        LauncherFontOption.MANROPE -> ManropeFamily
        LauncherFontOption.POPPINS -> PoppinsFamily
    }

/**
 * Resolves a [ClockFontOption] to its real [FontFamily] — every option must render every clock
 * template and [com.lumenlauncher.app.ui.home.clock.CalendarEventsBlock] correctly (a
 * weight/style request a template doesn't have an exact bundled instance for just falls back to
 * the platform's nearest match), which is what makes templates and fonts freely combinable.
 * [ClockFontOption.LAUNCHER_DEFAULT] has no font of its own — it defers to [launcherFontOption],
 * the live Settings → Appearance → Font value, which is why this takes it as a parameter rather
 * than being a plain stateless property the way [LauncherFontOption.fontFamily] above is.
 */
fun ClockFontOption.resolveFontFamily(launcherFontOption: LauncherFontOption): FontFamily =
    when (this) {
        ClockFontOption.LAUNCHER_DEFAULT -> launcherFontOption.fontFamily
        ClockFontOption.SYSTEM -> FontFamily.SansSerif
        ClockFontOption.ROBOTO_FLEX -> RobotoFlexFamily
        ClockFontOption.NOTO_SANS -> NotoSansFamily
        ClockFontOption.MANROPE -> ManropeFamily
        ClockFontOption.POPPINS -> PoppinsFamily
    }
