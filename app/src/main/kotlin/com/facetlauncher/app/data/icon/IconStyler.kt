package com.facetlauncher.app.data.icon

/**
 * The derived pictures of one app icon, as straight ARGB: alpha is the coverage and the colour
 * bytes are a shade around mid grey (0x80 = the accent exactly). [glyph] or [outline] is null where
 * that picture isn't worth drawing; [fallback], the whole icon shaded the same way, is then there to
 * draw in its place (null when neither is missing). The fallback carries the icon's own lightness
 * rather than a shade, with [fallbackTones] to place it: see [IconStyler.anchorFor].
 */
class StyledMasks(val glyph: IntArray?, val outline: IntArray?, val fallback: IntArray?, val fallbackTones: FloatArray?)

/** Builds the glyph-only and outline pictures from an icon's pixels: themed layer first, then the plain icon, then the whole icon shaded. */
object IconStyler {
    /** How strongly an icon's own light and dark carry over around the accent. */
    const val SHADE_SPREAD = 0.9f

    /** Share of an icon that may be edges before it counts as art (a game, a photo) rather than a logo. */
    private const val ART_EDGE_COVERAGE = 0.30f

    /** A themed layer filling more than this is a block, not a glyph. */
    private const val MAX_THEMED_COVERAGE = 0.6f

    private const val MIN_THEMED_COVERAGE = 0.02f
    private const val TONE_STEPS = 20
    private const val FLAT_SHADE = 0x808080
    private const val OUTLINE_DIVISOR = 48

    /** [icon] is the flattened icon and [themed] the app's monochrome layer, both [size] pixels square. */
    fun style(icon: IntArray, themed: IntArray?, size: Int): StyledMasks {
        val layer = themed?.let { IconMasks.fromAlpha(it, size, size) }?.takeIf { coverage(it) in MIN_THEMED_COVERAGE..MAX_THEMED_COVERAGE }
        if (layer != null) {
            return StyledMasks(flat(layer), flat(IconMasks.contour(layer, size / OUTLINE_DIVISOR)), fallback = null, fallbackTones = null)
        }
        val edges = IconMasks.edges(icon, size, size, thickness = 1)
        val glyph = (IconMasks.removePlate(icon, size, size) ?: IconMasks.removePlateByEdges(icon, size, size))?.let { shaded(icon, it) }
        val outline = if (coverage(edges, threshold = 100) <= ART_EDGE_COVERAGE) flat(edges) else null
        val needsFallback = glyph == null || outline == null
        return StyledMasks(glyph, outline, if (needsFallback) lightness(icon) else null, if (needsFallback) tones(icon) else null)
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
