package com.facetlauncher.app.data.model

/** A screen's size in pixels in its natural orientation, so rotating the phone does not make a new screen. A folded and an unfolded foldable are two. */
data class ScreenSize(val width: Int, val height: Int) {
    /** `true` when the screen is wider than tall in its natural orientation, as a foldable's inner display is. */
    val isLandscape: Boolean get() = width > height

    /** The shape, width over height. */
    val aspect: Float get() = width.toFloat() / height.coerceAtLeast(1)

    val area: Long get() = width.toLong() * height

    /** Stable text for a file name or a stored list: `1080x2092`. */
    val key: String get() = "${width}x$height"

    companion object {
        fun parse(key: String): ScreenSize? {
            val (w, h) = key.split('x').takeIf { it.size == 2 }?.map { it.toIntOrNull() ?: return null } ?: return null
            return ScreenSize(w, h).takeIf { w > 0 && h > 0 }
        }
    }
}

/** The key under which a "no thanks" to adding a copy for this screen is remembered. */
val ScreenSize.promptKey: String get() = "screen:$key"

/** One screen Facet has run on: whether it has a wallpaper copy of its own that matches the wallpaper now, or one that is [outdated]. */
data class ScreenCopy(val size: ScreenSize, val hasCopy: Boolean, val outdated: Boolean = false)
