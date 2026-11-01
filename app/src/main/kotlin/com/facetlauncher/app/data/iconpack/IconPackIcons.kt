package com.facetlauncher.app.data.iconpack

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap

/** Pack icons are drawn at most this many pixels square: sharp at every icon size the app uses, without holding full-size pack art for every app. */
const val PACK_ICON_SIZE = 192

/** One loaded icon pack's icons. */
interface IconPackIcons {
    /** Whether the pack has its own icon for this activity (or its package). */
    fun covers(packageName: String, className: String): Boolean

    /** Whether the pack draws this activity's icon for the day of the month (a calendar app). */
    fun isDated(packageName: String, className: String): Boolean

    /** The pack's own icon for this activity on [dayOfMonth] (only a dated app's icon depends on it), or null when it has none. */
    fun iconFor(packageName: String, className: String, dayOfMonth: Int): Bitmap?

    /** [icon] themed with the pack's backing tile, mask and overlay, or null when the pack ships none of them. */
    fun themed(icon: Bitmap, packageName: String): Bitmap?
}

/** [IconPackIcons] read from an installed pack's own resources. */
internal class ResourceIconPack(
    private val resources: Resources,
    private val packPackageName: String,
    private val filter: AppFilter,
) : IconPackIcons {

    override fun covers(packageName: String, className: String): Boolean =
        filter.drawableFor(packageName, className) != null || isDated(packageName, className)

    override fun isDated(packageName: String, className: String): Boolean = filter.calendarPrefixFor(packageName, className) != null

    /** A dated app's drawable for the day first; a pack missing that day falls back to the app's plain entry. */
    override fun iconFor(packageName: String, className: String, dayOfMonth: Int): Bitmap? {
        val dated = filter.calendarPrefixFor(packageName, className)?.let { drawable("$it$dayOfMonth") }
        return (dated ?: filter.drawableFor(packageName, className)?.let(::drawable))?.let(::render)
    }

    override fun themed(icon: Bitmap, packageName: String): Bitmap? {
        if (!filter.hasFallbackArt) return null
        val size = PACK_ICON_SIZE
        val out = createBitmap(size, size)
        val canvas = Canvas(out)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val full = Rect(0, 0, size, size)
        val scaled = (size * filter.scale).toInt().coerceIn(1, size)
        val inset = (size - scaled) / 2
        canvas.drawBitmap(icon, null, Rect(inset, inset, inset + scaled, inset + scaled), paint)
        filter.iconMask?.let(::drawable)?.let { mask ->
            canvas.drawBitmap(mask.toBitmap(size, size), null, full, Paint(paint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) })
        }
        filter.iconBacks.takeIf { it.isNotEmpty() }?.let { backs -> drawable(backs[Math.floorMod(packageName.hashCode(), backs.size)]) }?.let { back ->
            canvas.drawBitmap(back.toBitmap(size, size), null, full, Paint(paint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER) })
        }
        filter.iconUpon?.let(::drawable)?.let { upon -> canvas.drawBitmap(upon.toBitmap(size, size), null, full, paint) }
        return out
    }

    private fun drawable(name: String): Drawable? = runCatching {
        // getIdentifier is the only way to look a pack's drawable up by the name its appfilter gives.
        @Suppress("DiscouragedApi")
        val id = resources.getIdentifier(name, "drawable", packPackageName).takeIf { it != 0 }
            ?: return null
        ResourcesCompat.getDrawable(resources, id, null)
    }.getOrNull()

    private fun render(drawable: Drawable): Bitmap? = runCatching {
        val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(PACK_ICON_SIZE) ?: PACK_ICON_SIZE
        val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(PACK_ICON_SIZE) ?: PACK_ICON_SIZE
        val side = maxOf(width, height)
        drawable.toBitmap(side, side)
    }.getOrNull()
}
