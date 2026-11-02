package com.facetlauncher.app.data.icon

import kotlin.math.sqrt

/** One coverage byte (0..255) per pixel, row-major. The glyph and outline icon sets draw it flat in a single colour. */
class AlphaMask(val width: Int, val height: Int, val alpha: IntArray)

object IconMasks {
    private const val PLATE_TOLERANCE_LOW = 30
    private const val PLATE_TOLERANCE_HIGH = 80
    private const val BORDER_TOLERANCE = 48
    private const val MIN_OPAQUE_BORDER = 0.9f
    private const val OPAQUE = 250
    private const val MIN_UNIFORM_BORDER = 0.85f
    private const val EDGE_BAND_INNER_DIVISOR = 96
    private const val EDGE_BAND_OUTER_PER_192 = 7
    private const val MIN_EDGE_BAND = 50
    private const val EDGE_WALL = 14f
    private const val MIN_REMOVED_BY_EDGES = 0.5f
    private const val MAX_REMOVED_BY_EDGES = 0.99f
    private const val MIN_GLYPH = 0.01f
    private const val MIN_PLATE_SOLIDITY = 0.65f
    private const val THIN_GLYPH = 0.08f
    private const val MIN_MAIN_PLATE_SHARE = 0.78f
    private const val MIN_SPECK = 30
    private const val RING_SPAN = 0.85f
    private const val RING_FILL = 0.25f
    private const val HOLE_DISTANCE = 45
    private const val MIN_REMOVED = 0.25f
    private const val MAX_REMOVED = 0.95f
    private const val EDGE_FLOOR = 0.15f
    private const val EDGE_FULL = 0.5f
    private const val PEAK_PERCENTILE = 0.95f

    /** The icon's own alpha, e.g. a themed monochrome layer. */
    fun fromAlpha(argb: IntArray, width: Int, height: Int) =
        AlphaMask(width, height, IntArray(argb.size) { argb[it] ushr 24 })

    /**
     * Drops the plate: every pixel becomes transparent in proportion to how close it is to the
     * plate's colour, so negative space inside the glyph (a white ring, a play triangle) becomes
     * a hole too. The plate's colour is read from the image border, or from a band just inside the
     * icon's own edge when the icon is a shape with transparent corners. Null when there is no
     * plain plate to drop (gradient or photo background, or almost nothing or almost everything
     * was removed).
     */
    fun removePlate(argb: IntArray, width: Int, height: Int): AlphaMask? {
        val sample = plateSample(argb, width, height) ?: return null
        val bg = intArrayOf(
            median(sample.map { (argb[it] shr 16) and 0xFF }),
            median(sample.map { (argb[it] shr 8) and 0xFF }),
            median(sample.map { argb[it] and 0xFF }),
        )
        fun distance(pixel: Int): Int {
            val dr = ((pixel shr 16) and 0xFF) - bg[0]
            val dg = ((pixel shr 8) and 0xFF) - bg[1]
            val db = (pixel and 0xFF) - bg[2]
            return sqrt((dr * dr + dg * dg + db * db).toFloat()).toInt()
        }
        if (sample.count { distance(argb[it]) <= BORDER_TOLERANCE } < sample.size * MIN_UNIFORM_BORDER) return null

        val out = IntArray(argb.size)
        var inside = 0
        var removed = 0
        for (i in argb.indices) {
            val keep = ((distance(argb[i]) - PLATE_TOLERANCE_LOW) * 255 / (PLATE_TOLERANCE_HIGH - PLATE_TOLERANCE_LOW)).coerceIn(0, 255)
            out[i] = if (argb[i] ushr 24 < OPAQUE) 0 else (argb[i] ushr 24) * keep / 255
            if (argb[i] ushr 24 >= 128) {
                inside++
                if (out[i] < 128) removed++
            }
        }
        val fraction = removed.toFloat() / inside.coerceAtLeast(1)
        return if (fraction in MIN_REMOVED..MAX_REMOVED) AlphaMask(width, height, out) else null
    }

    /** Where the icon is not opaque, and where its colour changes sharply (the walls a fill cannot cross). */
    private class Field(val outside: BooleanArray, val wall: BooleanArray)

    /**
     * Drops a plate that is not one flat colour, a gradient for instance: flood-fills from where
     * the plate shows (see [plateSample]), with strong colour edges as walls, so the fill follows
     * the plate however its colour drifts and stops at the glyph's outline. What the fill cannot
     * reach is the glyph, except specks, the icon's own border ring, and enclosed areas that look
     * like the plate. Null when the fill does not remove most of the icon, which means the picture
     * is art, not a logo on a plate. When that leaves almost no glyph, the fill is tried again from
     * only the plate's main colour: a dark region that also touches the border, such as ground under
     * a figure, is then kept as part of the glyph instead of being swallowed with the plate. If that
     * does not help and the full-bleed picture's border is not mostly one colour, it is a scene with
     * no plate and the tiny glyph left would be a fragment of it, so there is none.
     */
    fun removePlateByEdges(argb: IntArray, width: Int, height: Int): AlphaMask? {
        val sample = plateSample(argb, width, height) ?: return null
        val field = fieldOf(argb, width, height)
        val all = glyphByFill(argb, sample, field, width, height)
        if (all != null && coverage(all) >= THIN_GLYPH) return all
        val main = dominantPlate(argb, sample)
        val dominant = glyphByFill(argb, main, field, width, height)
        return when {
            dominant != null && (all == null || coverage(dominant) > coverage(all)) -> dominant
            main.size < sample.size * MIN_MAIN_PLATE_SHARE && hasOpaqueBorder(argb, width, height) -> null
            else -> all
        }
    }

    private fun coverage(mask: AlphaMask) = mask.alpha.count { it >= 128 }.toFloat() / mask.alpha.size

    private fun fieldOf(argb: IntArray, width: Int, height: Int): Field {
        val outside = BooleanArray(argb.size) { argb[it] ushr 24 < OPAQUE }
        val edges = colourEdges(extendIntoOutside(argb, outside, width, height), width, height)
        val grown = rank(IntArray(argb.size) { if (edges[it] > EDGE_WALL) 255 else 0 }, width, height, radius = 1, max = true)
        return Field(outside, BooleanArray(argb.size) { grown[it] > 0 })
    }

    /** The icon with each transparent pixel given the colour of the nearest opaque one, so the icon's own rim is not an edge. */
    private fun extendIntoOutside(argb: IntArray, outside: BooleanArray, width: Int, height: Int): IntArray {
        val out = argb.copyOf()
        val known = BooleanArray(argb.size) { !outside[it] }
        val queue = IntArray(argb.size)
        var head = 0
        var tail = 0
        for (i in argb.indices) if (known[i]) queue[tail++] = i
        while (head < tail) {
            val i = queue[head++]
            for (next in neighboursOf(i, width, height)) {
                if (next >= 0 && !known[next]) {
                    known[next] = true
                    out[next] = out[i]
                    queue[tail++] = next
                }
            }
        }
        return out
    }

    private fun glyphByFill(argb: IntArray, seeds: List<Int>, field: Field, width: Int, height: Int): AlphaMask? {
        if (seeds.isEmpty()) return null
        val n = argb.size
        val reached = fillAround(seeds, field.wall, field.outside, width, height)
        val grown = rank(IntArray(n) { if (reached[it]) 255 else 0 }, width, height, radius = 1, max = true)
        val background = BooleanArray(n) { reached[it] || field.outside[it] || (grown[it] > 0 && field.wall[it]) }
        val inside = (0 until n).count { argb[it] ushr 24 >= 128 }.coerceAtLeast(1)
        val removedByFill = (0 until n).count { background[it] && argb[it] ushr 24 >= 128 }.toFloat() / inside
        if (removedByFill !in MIN_REMOVED_BY_EDGES..MAX_REMOVED_BY_EDGES) return null
        claimLeftovers(argb, background, reached, field.wall, width, height)
        if ((0 until n).count { !background[it] && argb[it] ushr 24 >= 128 } < inside * MIN_GLYPH) return null
        val soft = boxAverage(IntArray(n) { if (background[it]) 255 else 0 }, width, height)
        return AlphaMask(width, height, IntArray(n) { (255 - soft[it]) * (argb[it] ushr 24) / 255 })
    }

    /** Of the pixels that show the plate, those near its main colour: a dark region that also touches the border, such as ground under a figure, is part of the glyph. */
    private fun dominantPlate(argb: IntArray, sample: List<Int>): List<Int> {
        val main = intArrayOf(16, 8, 0).map { shift -> median(sample.map { (argb[it] shr shift) and 0xFF }) }.toIntArray()
        return sample.filter { colourDistance(argb[it], main) <= PLATE_TOLERANCE_HIGH }
    }

    /** Colour change per pixel: the Sobel gradient of red, green and blue together. */
    private fun colourEdges(argb: IntArray, width: Int, height: Int): FloatArray {
        val out = FloatArray(argb.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0f
                for (shift in intArrayOf(16, 8, 0)) {
                    fun at(dx: Int, dy: Int) = ((argb[(y + dy).coerceIn(0, height - 1) * width + (x + dx).coerceIn(0, width - 1)] shr shift) and 0xFF).toFloat()
                    val gx = at(1, -1) + 2 * at(1, 0) + at(1, 1) - at(-1, -1) - 2 * at(-1, 0) - at(-1, 1)
                    val gy = at(-1, 1) + 2 * at(0, 1) + at(1, 1) - at(-1, -1) - 2 * at(0, -1) - at(1, -1)
                    sum += gx * gx + gy * gy
                }
                out[y * width + x] = sqrt(sum) / 4f
            }
        }
        return out
    }

    private fun neighboursOf(i: Int, width: Int, height: Int): IntArray {
        val x = i % width
        val y = i / width
        return intArrayOf(if (x > 0) i - 1 else -1, if (x < width - 1) i + 1 else -1, if (y > 0) i - width else -1, if (y < height - 1) i + width else -1)
    }

    /** Every pixel the fill from [seeds] reaches without crossing a [wall] or leaving the icon. */
    private fun fillAround(seeds: List<Int>, wall: BooleanArray, outside: BooleanArray, width: Int, height: Int): BooleanArray {
        val blocked = BooleanArray(wall.size) { wall[it] || outside[it] }
        val reached = BooleanArray(wall.size)
        val queue = IntArray(wall.size)
        var head = 0
        var tail = 0
        for (i in seeds) {
            if (!blocked[i] && !reached[i]) {
                reached[i] = true
                queue[tail++] = i
            }
        }
        while (head < tail) {
            for (next in neighboursOf(queue[head++], width, height)) {
                if (next >= 0 && !reached[next] && !blocked[next]) {
                    reached[next] = true
                    queue[tail++] = next
                }
            }
        }
        return reached
    }

    /** A connected piece of the icon the fill did not reach: its pixels and the box around them. */
    private class Region(val pixels: IntArray, val width: Int, val height: Int)

    private fun regionFrom(start: Int, free: BooleanArray, seen: BooleanArray, queue: IntArray, width: Int, height: Int): Region {
        var head = 0
        var tail = 0
        seen[start] = true
        queue[tail++] = start
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        while (head < tail) {
            val i = queue[head++]
            minX = minOf(minX, i % width)
            maxX = maxOf(maxX, i % width)
            minY = minOf(minY, i / width)
            maxY = maxOf(maxY, i / width)
            for (next in neighboursOf(i, width, height)) {
                if (next >= 0 && !seen[next] && free[next]) {
                    seen[next] = true
                    queue[tail++] = next
                }
            }
        }
        return Region(queue.copyOf(tail), maxX - minX + 1, maxY - minY + 1)
    }

    /**
     * Of what the fill left, takes back specks and the icon's own border ring, then the areas
     * inside the glyph that show the plate's colour (judged without the edge pixels around them,
     * which belong to the glyph's outline).
     */
    private fun claimLeftovers(argb: IntArray, background: BooleanArray, reached: BooleanArray, wall: BooleanArray, width: Int, height: Int) {
        val reachedPixels = argb.indices.filter { reached[it] }
        if (reachedPixels.isEmpty()) return
        val plate = intArrayOf(16, 8, 0).map { shift -> median(reachedPixels.map { (argb[it] shr shift) and 0xFF }) }.toIntArray()
        claimRegions(argb, background, wall, width, height, includeWalls = true) { isLeftover(it, argb, plate, width, height) }
        claimRegions(argb, background, wall, width, height, includeWalls = false) { it.pixels.size >= MIN_SPECK && meanDistance(it, argb, plate) < HOLE_DISTANCE }
        val taken = rank(IntArray(argb.size) { if (background[it]) 255 else 0 }, width, height, radius = 2, max = true)
        for (i in argb.indices) if (wall[i] && taken[i] > 0 && argb[i] ushr 24 >= 250) background[i] = true
    }

    /** Marks as background each connected piece of the icon, left after the fill, that [claim] says to take. Edge pixels ([wall]) join the pieces only when [includeWalls] is set. */
    private fun claimRegions(argb: IntArray, background: BooleanArray, wall: BooleanArray, width: Int, height: Int, includeWalls: Boolean, claim: (Region) -> Boolean) {
        val free = BooleanArray(argb.size) { !background[it] && argb[it] ushr 24 >= 128 && (includeWalls || !wall[it]) }
        val seen = BooleanArray(argb.size)
        val queue = IntArray(argb.size)
        for (start in argb.indices) {
            if (!free[start] || seen[start]) continue
            val region = regionFrom(start, free, seen, queue, width, height)
            if (claim(region)) region.pixels.forEach { background[it] = true }
        }
    }

    private fun isLeftover(region: Region, argb: IntArray, plate: IntArray, width: Int, height: Int): Boolean {
        val spansIcon = region.width >= width * RING_SPAN && region.height >= height * RING_SPAN
        val ring = spansIcon && region.pixels.size.toFloat() / (region.width * region.height) < RING_FILL
        return region.pixels.size < MIN_SPECK || ring || meanDistance(region, argb, plate) < HOLE_DISTANCE
    }

    private fun meanDistance(region: Region, argb: IntArray, plate: IntArray): Long =
        region.pixels.sumOf { colourDistance(argb[it], plate).toLong() } / region.pixels.size

    private fun colourDistance(pixel: Int, colour: IntArray): Int {
        val dr = ((pixel shr 16) and 0xFF) - colour[0]
        val dg = ((pixel shr 8) and 0xFF) - colour[1]
        val db = (pixel and 0xFF) - colour[2]
        return sqrt((dr * dr + dg * dg + db * db).toFloat()).toInt()
    }

    /** A 3 by 3 average, to soften the edge of a mask. */
    private fun boxAverage(src: IntArray, width: Int, height: Int): IntArray = IntArray(src.size) {
        val x = it % width
        val y = it / width
        var sum = 0
        for (dy in -1..1) for (dx in -1..1) sum += src[(y + dy).coerceIn(0, height - 1) * width + (x + dx).coerceIn(0, width - 1)]
        sum / 9
    }

    /** How much of the box around the opaque pixels they fill: near 1 for a plate (a circle, a squircle), low for a glyph with no plate behind it. */
    private fun solidity(opaque: IntArray, width: Int, height: Int): Float {
        var minX = width
        var maxX = -1
        var minY = height
        var maxY = -1
        var count = 0
        for (i in opaque.indices) {
            if (opaque[i] == 0) continue
            count++
            minX = minOf(minX, i % width)
            maxX = maxOf(maxX, i % width)
            minY = minOf(minY, i / width)
            maxY = maxOf(maxY, i / width)
        }
        return if (count == 0) 0f else count.toFloat() / ((maxX - minX + 1) * (maxY - minY + 1))
    }

    /** Pixels that show the plate: the opaque image border, else the band just inside a shaped icon's edge; null when there is neither. */
    private fun hasOpaqueBorder(argb: IntArray, width: Int, height: Int): Boolean {
        val border = borderIndices(width, height)
        return border.count { argb[it] ushr 24 >= OPAQUE } >= border.size * MIN_OPAQUE_BORDER
    }

    private fun plateSample(argb: IntArray, width: Int, height: Int): List<Int>? {
        val border = borderIndices(width, height)
        if (hasOpaqueBorder(argb, width, height)) return border.filter { argb[it] ushr 24 >= OPAQUE }
        val opaque = IntArray(argb.size) { if (argb[it] ushr 24 >= OPAQUE) 255 else 0 }
        if (solidity(opaque, width, height) < MIN_PLATE_SOLIDITY) return null
        val innerRadius = (width / EDGE_BAND_INNER_DIVISOR).coerceAtLeast(1)
        val shallow = rank(opaque, width, height, innerRadius, max = false)
        val deep = rank(opaque, width, height, (width * EDGE_BAND_OUTER_PER_192 / 192).coerceAtLeast(innerRadius + 1), max = false)
        val band = argb.indices.filter { shallow[it] > 0 && deep[it] == 0 }
        return band.takeIf { it.size >= MIN_EDGE_BAND }
    }

    /** The shape's rim, [radius] pixels wide, inside its edge. */
    fun contour(mask: AlphaMask, radius: Int): AlphaMask {
        val eroded = rank(mask.alpha, mask.width, mask.height, radius, max = false)
        return AlphaMask(mask.width, mask.height, IntArray(mask.alpha.size) { (mask.alpha[it] - eroded[it]).coerceAtLeast(0) })
    }

    /** Line art of the icon's own detail: Sobel edges, scaled to the icon's strongest edges so faint icons still draw. */
    fun edges(argb: IntArray, width: Int, height: Int, thickness: Int): AlphaMask {
        val lum = FloatArray(argb.size) {
            val p = argb[it]
            val l = 0.299f * ((p shr 16) and 0xFF) + 0.587f * ((p shr 8) and 0xFF) + 0.114f * (p and 0xFF)
            l * (p ushr 24) / 255f
        }
        val mag = FloatArray(argb.size)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x
                val gx = lum[i - width + 1] + 2 * lum[i + 1] + lum[i + width + 1] - lum[i - width - 1] - 2 * lum[i - 1] - lum[i + width - 1]
                val gy = lum[i + width - 1] + 2 * lum[i + width] + lum[i + width + 1] - lum[i - width - 1] - 2 * lum[i - width] - lum[i - width + 1]
                mag[i] = sqrt(gx * gx + gy * gy)
            }
        }
        val scale = mag.sortedArray()[(mag.size * 0.98f).toInt().coerceAtMost(mag.size - 1)].coerceAtLeast(1f)
        val raw = IntArray(argb.size) { (((mag[it] / scale - EDGE_FLOOR) / (EDGE_FULL - EDGE_FLOOR)).coerceIn(0f, 1f) * 255).toInt() }
        return AlphaMask(width, height, rank(raw, width, height, thickness, max = true))
    }

    /** [mask] kept only where [shape] covers, in proportion to its coverage. */
    fun clipTo(mask: AlphaMask, shape: AlphaMask) =
        AlphaMask(mask.width, mask.height, IntArray(mask.alpha.size) { mask.alpha[it] * shape.alpha[it] / 255 })

    /** Scales every opacity by one factor so the 95th percentile of the drawn pixels reaches full, keeping the gradient's shape. */
    fun normalizePeak(mask: AlphaMask): AlphaMask {
        val drawn = mask.alpha.filter { it > 0 }.sorted()
        if (drawn.isEmpty()) return mask
        val peak = drawn[((drawn.size - 1) * PEAK_PERCENTILE).toInt()]
        return AlphaMask(mask.width, mask.height, IntArray(mask.alpha.size) { (mask.alpha[it] * 255f / peak).toInt().coerceAtMost(255) })
    }

    private fun borderIndices(width: Int, height: Int): IntArray {
        val out = ArrayList<Int>()
        for (x in 0 until width) { out += x; out += (height - 1) * width + x }
        for (y in 1 until height - 1) { out += y * width; out += y * width + width - 1 }
        return out.toIntArray()
    }

    private fun median(values: List<Int>) = values.sorted()[values.size / 2]

    /** Min or max over a square window of [radius] pixels, clamped at the edges. */
    private fun rank(src: IntArray, width: Int, height: Int, radius: Int, max: Boolean): IntArray {
        if (radius <= 0) return src.copyOf()
        fun pick(a: Int, b: Int) = if (max) maxOf(a, b) else minOf(a, b)
        val horizontal = IntArray(src.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var v = src[y * width + x]
                for (dx in -radius..radius) v = pick(v, src[y * width + (x + dx).coerceIn(0, width - 1)])
                horizontal[y * width + x] = v
            }
        }
        val out = IntArray(src.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var v = horizontal[y * width + x]
                for (dy in -radius..radius) v = pick(v, horizontal[(y + dy).coerceIn(0, height - 1) * width + x])
                out[y * width + x] = v
            }
        }
        return out
    }
}
