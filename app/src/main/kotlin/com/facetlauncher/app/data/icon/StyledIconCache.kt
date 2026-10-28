package com.facetlauncher.app.data.icon

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The glyph-only and outline pictures of one app icon. Where one is null, [fallback] (the whole icon, as its own lightness) is there to draw instead. */
class StyledIcon(val glyph: ImageBitmap?, val outline: ImageBitmap?, val fallback: ImageBitmap?, private val fallbackTones: FloatArray?) {
    /** Where the [fallback]'s own lightness should sit against an accent of [accentLightness]; see [IconStyler.anchorFor]. */
    fun fallbackAnchor(accentLightness: Float): Float = fallbackTones?.let { IconStyler.anchorFor(it, accentLightness) } ?: 0.5f
}

/**
 * Makes [StyledIcon]s on demand, off the main thread, and keeps each for as long as the icon it came
 * from is alive: keyed by the icon bitmap itself, held weakly, so a rescan that decodes fresh icons
 * simply recomputes what is on screen and the old entries are collected.
 */
@Singleton
class StyledIconCache @Inject constructor() {
    private val cache = WeakHashMap<ImageBitmap, StyledIcon>()

    suspend fun styled(icon: ImageBitmap, themedCoverage: ImageBitmap?): StyledIcon {
        synchronized(cache) { cache[icon] }?.let { return it }
        val styled = withContext(Dispatchers.Default) {
            val masks = IconStyler.style(icon.asAndroidBitmap().pixels(), themedCoverage?.asAndroidBitmap()?.copy(Bitmap.Config.ARGB_8888, false)?.pixels(), ICON_MASK_SIZE)
            StyledIcon(
                glyph = masks.glyph?.let { styledBitmap(it).asImageBitmap() },
                outline = masks.outline?.let { styledBitmap(it).asImageBitmap() },
                fallback = masks.fallback?.let { styledBitmap(it).asImageBitmap() },
                fallbackTones = masks.fallbackTones,
            )
        }
        synchronized(cache) { cache[icon] = styled }
        return styled
    }
}
