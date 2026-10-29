package com.facetlauncher.app.data.icon

/**
 * The derived pictures of one app icon, as straight ARGB: alpha is the coverage and the colour
 * bytes are a shade around mid grey (0x80 = the accent exactly). [glyph] or [outline] is null where
 * that picture isn't worth drawing; [fallback], the whole icon shaded the same way, is then there to
 * draw in its place (null when neither is missing). The fallback carries the icon's own lightness
 * rather than a shade, with [fallbackTones] to place it: see [IconStyler.anchorFor].
 */
/** Which of an icon's two pictures is being made or kept. */
enum class StyledPicture { GLYPH, OUTLINE }

class StyledMasks(val glyph: IntArray?, val outline: IntArray?, val fallback: IntArray?, val fallbackTones: FloatArray?)

/** Builds the glyph-only and outline pictures from an icon's pixels: themed layer first, then the plain icon (the outline is the edges of the glyph alone, or of the whole icon where there is no glyph), then the whole icon shaded. */
object IconStyler {
    /** How strongly an icon's own light and dark carry over around the accent. */
    const val SHADE_SPREAD = 0.9f

    /** Share of an icon that may be edges before it counts as art (a game, a photo) rather than a logo. */
    private const val ART_EDGE_COVERAGE = 0.30f

    /** Opacity (of 255) an edge pixel needs to count toward [ART_EDGE_COVERAGE]: strength about 0.33 on `IconMasks.edges`' ramp. */
    private const val ART_EDGE_OPACITY = 130

    /** A themed layer filling more than this is a block, not a glyph. */
    private const val MAX_THEMED_COVERAGE = 0.6f

    private const val MIN_THEMED_COVERAGE = 0.02f
    private const val TONE_STEPS = 20
    private const val FLAT_SHADE = 0x808080
    private const val GREY = 128
    private const val OUTLINE_DIVISOR = 48

    /**
     * [icon] is the flattened icon and [themed] the app's monochrome layer, both [size] pixels square. With [only] set just that
     * picture is made, and the whole-icon fallback only if that one is missing.
     */
    fun style(icon: IntArray, themed: IntArray?, size: Int, only: StyledPicture? = null): StyledMasks {
        val layer = themed?.let { IconMasks.fromAlpha(it, size, size) }?.takeIf { coverage(it) in MIN_THEMED_COVERAGE..MAX_THEMED_COVERAGE }
        if (layer != null) {
            val outline = if (only != StyledPicture.GLYPH) flat(IconMasks.contour(layer, size / OUTLINE_DIVISOR)) else null
            return StyledMasks(if (only != StyledPicture.OUTLINE) flat(layer) else null, outline, fallback = null, fallbackTones = null)
        }
        val plateless = IconMasks.removePlate(icon, size, size) ?: IconMasks.removePlateByEdges(icon, size, size)
        val glyph = if (only != StyledPicture.OUTLINE) plateless?.let { shaded(icon, it) } else null
        val outline = if (only != StyledPicture.GLYPH) plainOutline(icon, plateless, size) else null
        val missing = (only != StyledPicture.OUTLINE && glyph == null) || (only != StyledPicture.GLYPH && outline == null)
        return StyledMasks(glyph, outline, if (missing) lightness(icon) else null, if (missing) tones(icon) else null)
    }

    /** The outline of a plain icon: the edges of its glyph alone where it has one, else the whole icon's edges unless they are art. */
    private fun plainOutline(icon: IntArray, plateless: AlphaMask?, size: Int): IntArray? =
        if (plateless != null) {
            flat(IconMasks.edges(onGrey(icon, plateless), size, size, thickness = 0))
        } else {
            IconMasks.edges(icon, size, size, thickness = 1).takeIf { coverage(it, threshold = ART_EDGE_OPACITY) <= ART_EDGE_COVERAGE }?.let { flat(it) }
        }

    /**
     * Where along an icon's own tones the accent should sit: [tones] are its lightness at each
     * twentieth of its pixels, and the accent goes at its own lightness. A pale accent lands among
     * the icon's lightest tones, so its shadows get room to darken instead of its highlights
     * clipping to white; a dark one the other way round.
     */
    fun anchorFor(tones: FloatArray, accentLightness: Float): Float {
        val at = accentLightness.coerceIn(0f, 1f) * (tones.size - 1)
        val below = at.toInt().coerceAtMost(tones.size - 2)
        return tones[below] + (tones[below + 1] - tones[below]) * (at - below)
    }

    private fun lightnessOf(pixel: Int) = (0.299f * ((pixel shr 16) and 0xFF) + 0.587f * ((pixel shr 8) and 0xFF) + 0.114f * (pixel and 0xFF)) / 255f

    /** The icon as its own lightness, grey, at its own alpha. */
    private fun lightness(icon: IntArray) = IntArray(icon.size) {
        val grey = (lightnessOf(icon[it]) * 255).toInt()
        (icon[it] and 0xFF000000.toInt()) or (grey shl 16) or (grey shl 8) or grey
    }

    private fun tones(icon: IntArray): FloatArray {
        val sorted = icon.filter { it ushr 24 >= 128 }.map { lightnessOf(it) }.sorted()
        if (sorted.isEmpty()) return FloatArray(TONE_STEPS + 1) { 0.5f }
        return FloatArray(TONE_STEPS + 1) { sorted[(it * (sorted.size - 1)) / TONE_STEPS] }
    }

    private fun coverage(mask: AlphaMask, threshold: Int = 128) = mask.alpha.count { it >= threshold }.toFloat() / mask.alpha.size

    /** The glyph alone over mid grey, so edge detection sees its silhouette whatever its own lightness, and none of the plate. */
    private fun onGrey(icon: IntArray, glyph: AlphaMask) = IntArray(icon.size) {
        val cover = glyph.alpha[it] / 255f
        fun channel(shift: Int) = (((icon[it] shr shift) and 0xFF) * cover + GREY * (1 - cover)).toInt()
        (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    private fun flat(mask: AlphaMask) = IntArray(mask.alpha.size) { (mask.alpha[it] shl 24) or FLAT_SHADE }

    /** The icon's own light and dark, centred on the glyph's median so a single-colour logo stays the accent and only real shading shows. */
    private fun shaded(icon: IntArray, mask: AlphaMask): IntArray {
        val lightness = FloatArray(icon.size) {
            val p = icon[it]
            (0.299f * ((p shr 16) and 0xFF) + 0.587f * ((p shr 8) and 0xFF) + 0.114f * (p and 0xFF)) / 255f
        }
        val inside = lightness.filterIndexed { i, _ -> mask.alpha[i] >= 128 }.sorted()
        val median = if (inside.isEmpty()) 0.5f else inside[inside.size / 2]
        return IntArray(icon.size) {
            val grey = ((0.5f + (lightness[it] - median) * SHADE_SPREAD).coerceIn(0f, 1f) * 255).toInt()
            (mask.alpha[it] shl 24) or (grey shl 16) or (grey shl 8) or grey
        }
    }
}
