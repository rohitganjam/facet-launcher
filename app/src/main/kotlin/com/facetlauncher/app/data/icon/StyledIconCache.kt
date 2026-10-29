package com.facetlauncher.app.data.icon

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.facetlauncher.app.data.model.AppInfo
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The [picture] of one app icon that is applied, the other being left out. Where it is null, [fallback] (the whole icon, as its own lightness) is there to draw instead. */
class StyledIcon(val picture: StyledPicture, val glyph: ImageBitmap?, val outline: ImageBitmap?, val fallback: ImageBitmap?, private val fallbackTones: FloatArray?) {
    /** Where the [fallback]'s own lightness should sit against an accent of [accentLightness]; see [IconStyler.anchorFor]. */
    fun fallbackAnchor(accentLightness: Float): Float = fallbackTones?.let { IconStyler.anchorFor(it, accentLightness) } ?: 0.5f
}

/**
 * Makes [StyledIcon]s off the main thread, on demand or ahead of it ([warm]), keeping only the picture that is applied:
 * each is held for as long as the icon it came from is alive, keyed by the icon bitmap itself and held weakly, so a rescan that
 * decodes fresh icons simply recomputes what is on screen and the old entries are collected. Asking for the other picture
 * replaces the one held.
 */
@Singleton
class StyledIconCache @Inject constructor() {
    private val cache = WeakHashMap<ImageBitmap, StyledIcon>()

    /** The [picture] already made for [icon], without waiting for anything. */
    fun cached(icon: ImageBitmap, picture: StyledPicture): StyledIcon? = synchronized(cache) { cache[icon] }?.takeIf { it.picture == picture }

    /** Forgets every picture, for when the applied style draws none. */
    fun clear() = synchronized(cache) { cache.clear() }

    /** Makes the [picture] for every app's icon that has none yet, one after another, so they are ready before the app is scrolled to. */
    suspend fun warm(apps: List<AppInfo>, picture: StyledPicture) {
        apps.forEach { app ->
            val icon = app.icon ?: return@forEach
            if (cached(icon, picture) == null) styled(icon, app.monochromeIcon, picture)
        }
    }

    suspend fun styled(icon: ImageBitmap, themedCoverage: ImageBitmap?, picture: StyledPicture): StyledIcon {
        cached(icon, picture)?.let { return it }
        val styled = withContext(Dispatchers.Default) {
            val themed = themedCoverage?.asAndroidBitmap()?.copy(Bitmap.Config.ARGB_8888, false)?.pixels()
            val masks = IconStyler.style(icon.asAndroidBitmap().pixels(), themed, ICON_MASK_SIZE, only = picture)
            StyledIcon(
                picture = picture,
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
