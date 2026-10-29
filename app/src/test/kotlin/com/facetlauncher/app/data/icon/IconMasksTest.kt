package com.facetlauncher.app.data.icon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IconMasksTest {
    private val side = 48
    private val plate = 0xFF3366CC.toInt()
    private val white = 0xFFFFFFFF.toInt()

    private fun solid(argb: Int) = IntArray(side * side) { argb }

    private fun IntArray.fill(from: Int, to: Int, argb: Int) {
        for (y in from until to) for (x in from until to) this[y * side + x] = argb
    }

    private fun AlphaMask.at(x: Int, y: Int) = alpha[y * width + x]

    @Test
    fun `fromAlpha reads the alpha channel`() {
        // Given pixels with different alpha
        val pixels = intArrayOf(0x00FFFFFF, 0x80000000.toInt(), 0xFF123456.toInt(), 0x40FF0000)

        // When taking the mask
        val mask = IconMasks.fromAlpha(pixels, 2, 2)

        // Then it is the alpha bytes
        assertEquals(listOf(0, 128, 255, 64), mask.alpha.toList())
    }

    @Test
    fun `removePlate keeps the glyph and drops the plate`() {
        // Given a white glyph on a blue plate
        val icon = solid(plate).also { it.fill(16, 32, white) }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)!!

        // Then the glyph stays and the plate is gone
        assertTrue(mask.at(24, 24) > 200)
        assertEquals(0, mask.at(1, 1))
    }

    @Test
    fun `removePlate also drops negative space enclosed by the glyph`() {
        // Given a white ring on a blue plate whose centre shows the plate colour
        val icon = solid(plate).also {
            it.fill(12, 36, white)
            it.fill(20, 28, plate)
        }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)!!

        // Then the enclosed plate colour is a hole and the ring stays
        assertTrue(mask.at(24, 24) < 50)
        assertTrue(mask.at(14, 24) > 200)
    }

    @Test
    fun `removePlate drops the plate of a shaped icon`() {
        // Given a plate with a white glyph inside a transparent border, like a round or squircle icon
        val icon = solid(0).also {
            it.fill(6, 42, plate)
            it.fill(16, 32, white)
        }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)!!

        // Then the glyph stays, and the plate and the transparent corners are gone
        assertTrue(mask.at(24, 24) > 200)
        assertEquals(0, mask.at(9, 9))
        assertEquals(0, mask.at(1, 1))
    }

    @Test
    fun `removePlate drops the soft rim around a shaped icon`() {
        // Given a shaped icon with a half transparent dark rim of anti-aliasing and shadow around its plate
        val icon = solid(0).also {
            it.fill(6, 42, plate)
            it.fill(16, 32, white)
            for (k in 5..42) {
                it[5 * side + k] = 0x80101010.toInt(); it[42 * side + k] = 0x80101010.toInt()
                it[k * side + 5] = 0x80101010.toInt(); it[k * side + 42] = 0x80101010.toInt()
            }
        }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)!!

        // Then the rim is gone as well as the plate, and the glyph stays
        assertEquals(0, mask.at(5, 20))
        assertTrue(mask.at(24, 24) > 200)
    }

    @Test
    fun `removePlate gives up on a shaped icon that is only plate`() {
        // Given an opaque shape inside a transparent border with nothing drawn on it
        val icon = solid(0).also { it.fill(8, 40, plate) }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)

        // Then nothing is left to keep
        assertNull(mask)
    }

    @Test
    fun `removePlate gives up when the border is not one colour`() {
        // Given a background that is red on the left and blue on the right
        val icon = IntArray(side * side) { if (it % side < side / 2) 0xFFCC3333.toInt() else plate }

        // When the plate is removed
        val mask = IconMasks.removePlate(icon, side, side)

        // Then it is not a plain plate
        assertNull(mask)
    }

    @Test
    fun `removePlate gives up when nothing stands out from the plate`() {
        // Given a plain plate and no glyph
        val mask = IconMasks.removePlate(solid(plate), side, side)

        // Then everything would be removed, so it gives up
        assertNull(mask)
    }

    @Test
    fun `contour keeps only the rim of a shape`() {
        // Given a filled square
        val square = AlphaMask(side, side, IntArray(side * side) { if (it % side in 16..31 && it / side in 16..31) 255 else 0 })

        // When taking its contour
        val rim = IconMasks.contour(square, radius = 2)

        // Then the edge is drawn, the inside and outside are not
        assertTrue(rim.at(16, 24) > 0)
        assertEquals(0, rim.at(24, 24))
        assertEquals(0, rim.at(5, 5))
    }

    @Test
    fun `edges draw along a boundary and not inside flat areas`() {
        // Given a white square on black
        val icon = solid(0xFF000000.toInt()).also { it.fill(16, 32, white) }

        // When detecting edges
        val edges = IconMasks.edges(icon, side, side, thickness = 1)

        // Then the boundary is drawn and the flat areas are not
        assertTrue(edges.at(16, 24) > 200)
        assertEquals(0, edges.at(24, 24))
        assertEquals(0, edges.at(3, 3))
    }

    @Test
    fun `edges of a flat icon is empty`() {
        // Given one flat colour
        val edges = IconMasks.edges(solid(plate), side, side, thickness = 1)

        // Then nothing is drawn
        assertNotNull(edges)
        assertEquals(0, edges.alpha.max())
    }

    private fun gradientPlate() = IntArray(side * side) { (0xFF shl 24) or ((220 - 3 * (it / side)) shl 16) or 0x2020 }

    @Test
    fun `removePlate cannot handle a gradient plate but removePlateByEdges can`() {
        // Given a white glyph on a plate that fades from light to dark red
        val icon = gradientPlate().also { it.fill(16, 32, white) }

        // When removing the plate both ways
        val flat = IconMasks.removePlate(icon, side, side)
        val byEdges = IconMasks.removePlateByEdges(icon, side, side)

        // Then only the edge fill finds the glyph
        assertNull(flat)
        assertTrue(byEdges!!.at(24, 24) > 200)
        assertEquals(0, byEdges.at(3, 3))
        assertEquals(0, byEdges.at(44, 44))
    }

    @Test
    fun `removePlateByEdges takes back a hole that shows the plate`() {
        // Given a wide white ring on a gradient plate whose centre shows the plate
        val icon = gradientPlate().also {
            it.fill(12, 36, white)
            for (y in 18 until 30) for (x in 18 until 30) it[y * side + x] = (0xFF shl 24) or ((220 - 3 * y) shl 16) or 0x2020
        }

        // When removing the plate by edges
        val mask = IconMasks.removePlateByEdges(icon, side, side)!!

        // Then the enclosed plate colour is a hole and the ring stays
        assertTrue(mask.at(24, 24) < 50)
        assertTrue(mask.at(14, 24) > 200)
    }

    @Test
    fun `removePlateByEdges gives up on busy art`() {
        // Given noise, where every pixel is an edge and the fill goes nowhere
        val random = java.util.Random(3)
        val noise = IntArray(side * side) { (0xFF shl 24) or random.nextInt(0x1000000) }

        // When removing the plate by edges
        val mask = IconMasks.removePlateByEdges(noise, side, side)

        // Then it is not a logo on a plate
        assertNull(mask)
    }

    @Test
    fun `removePlateByEdges drops the soft rim of a shaped icon`() {
        // Given a shaped icon, a gradient plate and a white glyph, with a half transparent rim of anti-aliasing around it
        val icon = solid(0).also {
            for (y in 5 until 43) for (x in 5 until 43) it[y * side + x] = (0xFF shl 24) or ((220 - 3 * y) shl 16) or 0x2020
            it.fill(16, 32, white)
            for (k in 4 until 44) {
                it[4 * side + k] = 0x80AA2020.toInt(); it[43 * side + k] = 0x80AA2020.toInt()
                it[k * side + 4] = 0x80AA2020.toInt(); it[k * side + 43] = 0x80AA2020.toInt()
            }
        }

        // When removing the plate by edges
        val mask = IconMasks.removePlateByEdges(icon, side, side)!!

        // Then the rim is gone and the glyph stays
        assertEquals(0, mask.at(4, 20))
        assertTrue(mask.at(24, 24) > 200)
    }

    @Test
    fun `removePlateByEdges keeps ground that touches the border as part of the glyph`() {
        // Given a light sky over a black ground that touches the border, with a few white stars in the sky
        val sky = 0xFF78C8E6.toInt()
        val icon = IntArray(side * side) { if (it / side >= 32) 0xFF000000.toInt() else sky }
        for ((x, y) in listOf(8 to 6, 20 to 10, 36 to 5)) for (dy in 0 until 2) for (dx in 0 until 2) icon[(y + dy) * side + x + dx] = white

        // When removing the plate by edges
        val mask = IconMasks.removePlateByEdges(icon, side, side)!!

        // Then the ground stays and the sky goes, instead of everything but the stars going
        assertTrue(mask.at(24, 40) > 200)
        assertEquals(0, mask.at(24, 15))
    }

    @Test
    fun `a glyph with no plate behind it is not mistaken for a plate`() {
        // Given thin orange bars in a cross on a transparent background, an icon with no plate
        val orange = 0xFFF28A1E.toInt()
        val icon = solid(0).also {
            for (y in 8 until 40) for (x in 20 until 28) it[y * side + x] = orange
            for (y in 20 until 28) for (x in 8 until 40) it[y * side + x] = orange
        }

        // Then neither plate removal treats the bars as plate
        assertNull(IconMasks.removePlate(icon, side, side))
        assertNull(IconMasks.removePlateByEdges(icon, side, side))
    }

    @Test
    fun `removePlateByEdges gives up on a scene that has no plate`() {
        // Given sky above and green ground below, both touching the border, with two small white dots on the ground
        val icon = IntArray(side * side) { if (it / side < 24) 0xFF78C8E6.toInt() else 0xFF3FA34D.toInt() }
        for (x in intArrayOf(14, 30)) for (dy in 0 until 2) for (dx in 0 until 2) icon[(34 + dy) * side + x + dx] = white

        // When removing the plate by edges
        val mask = IconMasks.removePlateByEdges(icon, side, side)

        // Then the dots left over are no glyph
        assertNull(mask)
    }
}
