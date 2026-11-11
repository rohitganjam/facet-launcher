package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * F11 — global (not per-app) app-icon rendering mode. [SYSTEM_DEFAULT] draws each app's own icon.
 * The two monochrome modes recolor it with a `BlendMode.Color` tint (see `ui/components/AppIcon.kt`
 * for why that and not the app's own monochrome layer): [MONOCHROME_BLACK_WHITE] in the theme's ink
 * tone, [MONOCHROME_ACCENT] in the accent. [GLYPH_ACCENT] and [OUTLINE_ACCENT] draw only the icon's
 * shape, as a filled silhouette or as line art, in the accent, from the app's themed monochrome layer
 * where it has one and from the plain icon otherwise (see `IconStyler`); an icon neither can handle
 * is drawn whole, in tones of the accent. [GLYPH_INK] and [OUTLINE_INK] are the same pictures in the
 * theme's ink tone, so they follow light and dark. [GLYPH_INVERTED] and [OUTLINE_INVERTED] draw them
 * in the opposite tone, `InkInverted`. The `_TILE_` modes put them on a tile in the icon shape: the tile takes
 * the colour and the picture the text tone that reads on it. The `_REVERSED_` modes swap the two: the
 * picture takes the colour and the tile the text tone. The `_GLASS_` modes put the picture, in the colour, on a translucent
 * panel in one of the [GlassStyle]s instead of a solid tile.
 *
 * The Icons screen shows a mode as an [IconStyle] and an [IconColour]; [of] and [coloursFor] map between them.
 */
enum class IconRenderMode(val style: IconStyle, val colour: IconColour?, val background: IconBackground = IconBackground.NONE) {
    /** Null colour: the default style has none of its own. */
    SYSTEM_DEFAULT(IconStyle.DEFAULT, null),
    MONOCHROME_BLACK_WHITE(IconStyle.MONOCHROME, IconColour.THEME),
    MONOCHROME_ACCENT(IconStyle.MONOCHROME, IconColour.ACCENT),
    GLYPH_ACCENT(IconStyle.GLYPH, IconColour.ACCENT),
    GLYPH_INK(IconStyle.GLYPH, IconColour.THEME),
    GLYPH_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED),
    OUTLINE_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT),
    OUTLINE_INK(IconStyle.OUTLINE, IconColour.THEME),
    OUTLINE_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED),
    GLYPH_TILE_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.TILE),
    GLYPH_TILE_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.TILE),
    GLYPH_TILE_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.TILE),
    OUTLINE_TILE_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.TILE),
    OUTLINE_TILE_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.TILE),
    OUTLINE_TILE_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.TILE),
    GLYPH_REVERSED_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.REVERSED_TILE),
    GLYPH_REVERSED_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.REVERSED_TILE),
    GLYPH_REVERSED_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.REVERSED_TILE),
    OUTLINE_REVERSED_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.REVERSED_TILE),
    OUTLINE_REVERSED_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.REVERSED_TILE),
    OUTLINE_REVERSED_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.REVERSED_TILE),
    GLYPH_GLASS_FROST_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_FROST),
    GLYPH_GLASS_FROST_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.GLASS_FROST),
    GLYPH_GLASS_FROST_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.GLASS_FROST),
    OUTLINE_GLASS_FROST_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.GLASS_FROST),
    OUTLINE_GLASS_FROST_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.GLASS_FROST),
    OUTLINE_GLASS_FROST_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_FROST),
    GLYPH_GLASS_SMOKED_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_SMOKED),
    GLYPH_GLASS_SMOKED_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.GLASS_SMOKED),
    GLYPH_GLASS_SMOKED_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.GLASS_SMOKED),
    OUTLINE_GLASS_SMOKED_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.GLASS_SMOKED),
    OUTLINE_GLASS_SMOKED_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.GLASS_SMOKED),
    OUTLINE_GLASS_SMOKED_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_SMOKED),
    GLYPH_GLASS_BRIGHT_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_BRIGHT),
    GLYPH_GLASS_BRIGHT_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.GLASS_BRIGHT),
    GLYPH_GLASS_BRIGHT_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.GLASS_BRIGHT),
    OUTLINE_GLASS_BRIGHT_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.GLASS_BRIGHT),
    OUTLINE_GLASS_BRIGHT_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.GLASS_BRIGHT),
    OUTLINE_GLASS_BRIGHT_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_BRIGHT),
    GLYPH_GLASS_CLEAR_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_CLEAR),
    GLYPH_GLASS_CLEAR_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.GLASS_CLEAR),
    GLYPH_GLASS_CLEAR_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.GLASS_CLEAR),
    OUTLINE_GLASS_CLEAR_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.GLASS_CLEAR),
    OUTLINE_GLASS_CLEAR_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.GLASS_CLEAR),
    OUTLINE_GLASS_CLEAR_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_CLEAR),
    GLYPH_GLASS_ACCENT_ACCENT(IconStyle.GLYPH, IconColour.ACCENT, IconBackground.GLASS_ACCENT),
    GLYPH_GLASS_ACCENT_INK(IconStyle.GLYPH, IconColour.THEME, IconBackground.GLASS_ACCENT),
    GLYPH_GLASS_ACCENT_INVERTED(IconStyle.GLYPH, IconColour.THEME_INVERTED, IconBackground.GLASS_ACCENT),
    OUTLINE_GLASS_ACCENT_ACCENT(IconStyle.OUTLINE, IconColour.ACCENT, IconBackground.GLASS_ACCENT),
    OUTLINE_GLASS_ACCENT_INK(IconStyle.OUTLINE, IconColour.THEME, IconBackground.GLASS_ACCENT),
    OUTLINE_GLASS_ACCENT_INVERTED(IconStyle.OUTLINE, IconColour.THEME_INVERTED, IconBackground.GLASS_ACCENT),
    ;

    companion object {
        /** The mode for [style] in [colour] on [background], each falling back to the first the style offers when it has no such one. */
        fun of(style: IconStyle, colour: IconColour, background: IconBackground = IconBackground.NONE): IconRenderMode {
            val usableColour = if (colour in coloursFor(style)) colour else coloursFor(style).firstOrNull()
            val usableBackground = if (background in backgroundsFor(style)) background else IconBackground.NONE
            return entries.first { it.style == style && it.colour == usableColour && it.background == usableBackground }
        }

        /** The colours [style] offers, none for the default style. */
        fun coloursFor(style: IconStyle): List<IconColour> = when (style) {
            IconStyle.DEFAULT -> emptyList()
            IconStyle.MONOCHROME -> listOf(IconColour.ACCENT, IconColour.THEME)
            IconStyle.GLYPH, IconStyle.OUTLINE -> IconColour.entries
        }

        /** The backgrounds [style] offers: only the glyph and outline styles can sit on a tile. */
        fun backgroundsFor(style: IconStyle): List<IconBackground> = when (style) {
            IconStyle.GLYPH, IconStyle.OUTLINE -> IconBackground.entries
            else -> listOf(IconBackground.NONE)
        }
    }
}

/** How the Icons screen groups [IconRenderMode]: what is drawn, apart from the colour. */
enum class IconStyle(@param:StringRes val displayNameRes: Int) {
    DEFAULT(R.string.icon_style_default),
    MONOCHROME(R.string.icon_style_monochrome),
    GLYPH(R.string.icon_style_glyph),
    OUTLINE(R.string.icon_style_outline),
}

/** The colour half of an [IconRenderMode]. */
enum class IconColour(@param:StringRes val displayNameRes: Int) {
    ACCENT(R.string.icon_colour_accent),
    THEME(R.string.icon_colour_theme),
    THEME_INVERTED(R.string.icon_colour_theme_inverted),
}

/** What an icon sits on, the third half of an [IconRenderMode]. A glass background carries its [glass] style. */
enum class IconBackground(@param:StringRes val displayNameRes: Int, val glass: GlassStyle? = null) {
    NONE(R.string.icon_background_none),
    TILE(R.string.icon_background_tile),
    REVERSED_TILE(R.string.icon_background_reversed_tile),
    GLASS_FROST(R.string.glass_style_frost, GlassStyle.FROST),
    GLASS_SMOKED(R.string.glass_style_smoked, GlassStyle.SMOKED),
    GLASS_BRIGHT(R.string.glass_style_bright, GlassStyle.BRIGHT),
    GLASS_CLEAR(R.string.glass_style_clear_glass, GlassStyle.CLEAR_GLASS),
    GLASS_ACCENT(R.string.glass_style_accent, GlassStyle.ACCENT),
}
