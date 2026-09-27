package com.facetlauncher.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption

/**
 * Fonts bundled specifically for the clock/calendar template system (`ui/home/clock/`) — kept
 * separate from [facetTypography]'s app-wide font since clock/calendar
 * font choice is deliberately independent of the rest of the app's UI (see the clock template
 * gallery). All are Google Fonts, OFL-licensed, bundled locally under `res/font/` so they
 * render identically with no network dependency — see `THIRD_PARTY_FONT_LICENSES/` at the repo
 * root for the license text each family ships under.
 *
 * [RobotoFlexFamily], [ManropeFamily], [NotoSansFamily], [InterFamily], and [MontserratFamily] each
 * wrap a single variable-font file. A `Font()` entry declared with no `variationSettings` only ever
 * resolves to that file's default ("wght" 400) instance — Compose does **not** auto-interpolate a
 * variable font's weight axis from a bare `Font(resId)`, so every non-Regular [FontWeightOption]
 * silently rendered identical to Regular (see chat history: font-weight customization looked like
 * it did nothing from Thin up through Medium). [variableWeightInstances] fixes this by declaring
 * one `Font()` per supported stop, each pinned to its own "wght" value via [FontVariation.weight] —
 * that's what actually gets Android to pick a distinct instance per requested [FontWeight]. No
 * italic instance exists for Roboto Flex or Manrope upstream, so requesting [FontStyle.Italic]
 * against them falls back to upright; [NotoSansFamily], [InterFamily], and [MontserratFamily] each
 * ship a true italic variable file, kept at its single default weight since italic isn't wired to
 * the font-weight feature. [PoppinsFamily] and [LatoFamily] are classic static families — one file
 * per weight/style combination actually bundled ([LatoFamily] happens to bundle one file per
 * [FontWeightOption] stop, unlike [PoppinsFamily]'s partial coverage, since upstream Lato ships
 * static Thin/ExtraLight/Light/Regular/Medium/SemiBold files where Roboto Flex/Manrope/Inter/
 * Montserrat only ship as variable fonts).
 */
@OptIn(ExperimentalTextApi::class)
private fun variableWeightInstances(resId: Int, style: FontStyle = FontStyle.Normal): List<Font> =
    FontWeightOption.entries.map { option ->
        val weight = option.resolve()
        Font(resId, weight = weight, style = style, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
    }

private val RobotoFlexFamily = FontFamily(variableWeightInstances(R.font.roboto_flex_variable))

private val ManropeFamily = FontFamily(variableWeightInstances(R.font.manrope_variable))

private val NotoSansFamily = FontFamily(
    variableWeightInstances(R.font.noto_sans_variable) + Font(R.font.noto_sans_italic_variable, FontWeight.Normal, FontStyle.Italic),
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

private val InterFamily = FontFamily(
    variableWeightInstances(R.font.inter_variable) + Font(R.font.inter_italic_variable, FontWeight.Normal, FontStyle.Italic),
)

private val MontserratFamily = FontFamily(
    variableWeightInstances(R.font.montserrat_variable) + Font(R.font.montserrat_italic_variable, FontWeight.Normal, FontStyle.Italic),
)

private val LatoFamily = FontFamily(
    Font(R.font.lato_thin, FontWeight.Thin),
    Font(R.font.lato_extralight, FontWeight.ExtraLight),
    Font(R.font.lato_light, FontWeight.Light),
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_medium, FontWeight.Medium),
    Font(R.font.lato_semibold, FontWeight.SemiBold),
    Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
)

/**
 * Resolves a [LauncherFontOption] to its real [FontFamily] — reuses the same bundled families
 * [ClockFontOption] draws from, so picking a launcher-wide font never needs new assets.
 */
val LauncherFontOption.fontFamily: FontFamily
    get() = when (this) {
        LauncherFontOption.SYSTEM -> FontFamily.SansSerif
        LauncherFontOption.ROBOTO_FLEX -> RobotoFlexFamily
        LauncherFontOption.NOTO_SANS -> NotoSansFamily
        LauncherFontOption.MANROPE -> ManropeFamily
        LauncherFontOption.POPPINS -> PoppinsFamily
        LauncherFontOption.INTER -> InterFamily
        LauncherFontOption.MONTSERRAT -> MontserratFamily
        LauncherFontOption.LATO -> LatoFamily
    }

/**
 * Resolves a [ClockFontOption] to its real [FontFamily] — every option must render every clock
 * template and [com.facetlauncher.app.ui.home.clock.CalendarEventsBlock] correctly (a
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
        ClockFontOption.INTER -> InterFamily
        ClockFontOption.MONTSERRAT -> MontserratFamily
        ClockFontOption.LATO -> LatoFamily
    }
