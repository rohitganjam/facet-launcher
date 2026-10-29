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
 * picture takes the colour and the tile the text tone.
 *
 * The Icons screen shows a mode as an [IconStyle] and an [IconColour]; [of] and [coloursFor] map between them.
 */
enum class IconRenderMode(@param:StringRes val displayNameRes: Int) {
    SYSTEM_DEFAULT(R.string.icon_render_system_default),
    MONOCHROME_BLACK_WHITE(R.string.icon_render_monochrome_black_white),
    MONOCHROME_ACCENT(R.string.icon_render_monochrome_accent),
    GLYPH_ACCENT(R.string.icon_render_glyph_accent),
    OUTLINE_ACCENT(R.string.icon_render_outline_accent),
    GLYPH_INK(R.string.icon_render_glyph_ink),
    OUTLINE_INK(R.string.icon_render_outline_ink),
    GLYPH_INVERTED(R.string.icon_render_glyph_inverted),
    OUTLINE_INVERTED(R.string.icon_render_outline_inverted),
    GLYPH_TILE_ACCENT(R.string.icon_render_glyph_tile_accent),
    GLYPH_TILE_INK(R.string.icon_render_glyph_tile_ink),
    GLYPH_TILE_INVERTED(R.string.icon_render_glyph_tile_inverted),
    OUTLINE_TILE_ACCENT(R.string.icon_render_outline_tile_accent),
    OUTLINE_TILE_INK(R.string.icon_render_outline_tile_ink),
    OUTLINE_TILE_INVERTED(R.string.icon_render_outline_tile_inverted),
    GLYPH_REVERSED_ACCENT(R.string.icon_render_glyph_reversed_accent),
    GLYPH_REVERSED_INK(R.string.icon_render_glyph_reversed_ink),
    GLYPH_REVERSED_INVERTED(R.string.icon_render_glyph_reversed_inverted),
    OUTLINE_REVERSED_ACCENT(R.string.icon_render_outline_reversed_accent),
    OUTLINE_REVERSED_INK(R.string.icon_render_outline_reversed_ink),
    OUTLINE_REVERSED_INVERTED(R.string.icon_render_outline_reversed_inverted),
    ;

    val style: IconStyle
        get() = when (this) {
            SYSTEM_DEFAULT -> IconStyle.DEFAULT
            MONOCHROME_BLACK_WHITE, MONOCHROME_ACCENT -> IconStyle.MONOCHROME
            GLYPH_ACCENT, GLYPH_INK, GLYPH_INVERTED, GLYPH_TILE_ACCENT, GLYPH_TILE_INK, GLYPH_TILE_INVERTED,
            GLYPH_REVERSED_ACCENT, GLYPH_REVERSED_INK, GLYPH_REVERSED_INVERTED -> IconStyle.GLYPH
            OUTLINE_ACCENT, OUTLINE_INK, OUTLINE_INVERTED, OUTLINE_TILE_ACCENT, OUTLINE_TILE_INK, OUTLINE_TILE_INVERTED,
            OUTLINE_REVERSED_ACCENT, OUTLINE_REVERSED_INK, OUTLINE_REVERSED_INVERTED -> IconStyle.OUTLINE
        }

    /** Null for [SYSTEM_DEFAULT], which has no colour of its own. */
    val colour: IconColour?
        get() = when (this) {
            SYSTEM_DEFAULT -> null
            MONOCHROME_ACCENT, GLYPH_ACCENT, OUTLINE_ACCENT, GLYPH_TILE_ACCENT, OUTLINE_TILE_ACCENT,
            GLYPH_REVERSED_ACCENT, OUTLINE_REVERSED_ACCENT -> IconColour.ACCENT
            MONOCHROME_BLACK_WHITE, GLYPH_INK, OUTLINE_INK, GLYPH_TILE_INK, OUTLINE_TILE_INK,
            GLYPH_REVERSED_INK, OUTLINE_REVERSED_INK -> IconColour.THEME
            GLYPH_INVERTED, OUTLINE_INVERTED, GLYPH_TILE_INVERTED, OUTLINE_TILE_INVERTED,
            GLYPH_REVERSED_INVERTED, OUTLINE_REVERSED_INVERTED -> IconColour.THEME_INVERTED
        }

    val background: IconBackground
        get() = when (this) {
            GLYPH_TILE_ACCENT, GLYPH_TILE_INK, GLYPH_TILE_INVERTED, OUTLINE_TILE_ACCENT, OUTLINE_TILE_INK, OUTLINE_TILE_INVERTED -> IconBackground.TILE
            GLYPH_REVERSED_ACCENT, GLYPH_REVERSED_INK, GLYPH_REVERSED_INVERTED,
            OUTLINE_REVERSED_ACCENT, OUTLINE_REVERSED_INK, OUTLINE_REVERSED_INVERTED,
            -> IconBackground.REVERSED_TILE
            else -> IconBackground.NONE
        }

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

/** What an icon sits on, the third half of an [IconRenderMode]. */
enum class IconBackground(@param:StringRes val displayNameRes: Int) {
    NONE(R.string.icon_background_none),
    TILE(R.string.icon_background_tile),
    REVERSED_TILE(R.string.icon_background_reversed_tile),
}
