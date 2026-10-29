package com.facetlauncher.app.data.icon

import java.util.Random
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IconStylerTest {
    private val side = 48
    private val plate = 0xFF3366CC.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val flatShade = 0x808080

    private fun solid(argb: Int) = IntArray(side * side) { argb }

    private fun IntArray.fill(x0: Int, x1: Int, y0: Int, y1: Int, argb: Int) {
        for (y in y0 until y1) for (x in x0 until x1) this[y * side + x] = argb
    }

    private fun IntArray.at(x: Int, y: Int) = this[y * side + x]

    private fun logo() = solid(plate).also { it.fill(16, 32, 16, 32, white) }

    private fun themedBlock(from: Int, to: Int) = IntArray(side * side) { if (it % side in from until to && it / side in from until to) 0xFF000000.toInt() else 0 }

    private fun shadeOf(pixel: Int) = pixel and 0xFF

    @Test
    fun `a themed layer gives a flat glyph and an outline`() {
        // Given a plain logo and a small themed layer
        val themed = themedBlock(14, 34)

        // When styling
        val masks = IconStyler.style(logo(), themed, side)

        // Then the glyph is the layer in the flat shade and the outline is its rim
        assertEquals((255 shl 24) or flatShade, masks.glyph!!.at(24, 24))
        assertNotNull(masks.outline)
        assertEquals(0, masks.outline!!.at(24, 24) ushr 24)
        assertTrue(masks.outline!!.at(14, 24) ushr 24 > 0)
    }

    @Test
    fun `a themed layer that fills the icon is ignored`() {
        // Given a themed layer that is one solid block over the whole icon
        val themed = themedBlock(0, side)

        // When styling
        val masks = IconStyler.style(logo(), themed, side)

        // Then the plain icon is used: its plate is dropped instead of the block being drawn
        assertEquals(0, masks.glyph!!.at(1, 1) ushr 24)
        assertTrue(masks.glyph!!.at(24, 24) ushr 24 > 200)
    }

    @Test
    fun `without a themed layer the plate is dropped from the plain icon`() {
        // When styling a logo that has no themed layer
        val masks = IconStyler.style(logo(), null, side)

        // Then only the glyph is left
        assertEquals(0, masks.glyph!!.at(1, 1) ushr 24)
        assertTrue(masks.glyph!!.at(24, 24) ushr 24 > 200)
    }

    @Test
    fun `a single colour glyph is drawn in the accent itself`() {
        // When styling a logo whose glyph is one colour
        val masks = IconStyler.style(logo(), null, side)

        // Then its shade is mid grey, which the accent filter turns into the accent
        assertTrue(abs(shadeOf(masks.glyph!!.at(24, 24)) - 128) <= 1)
    }

    @Test
    fun `lighter and darker parts of a glyph keep their shading around the accent`() {
        // Given a glyph that is mostly white with a darker patch
        val icon = solid(plate).also {
            it.fill(12, 36, 12, 36, white)
            it.fill(12, 20, 12, 36, 0xFF808080.toInt())
        }

        // When styling
        val glyph = IconStyler.style(icon, null, side).glyph!!

        // Then the main colour sits on the accent and the darker patch is shaded below it
        assertTrue(abs(shadeOf(glyph.at(30, 24)) - 128) <= 1)
        assertTrue(shadeOf(glyph.at(15, 24)) < 100)
    }

    @Test
    fun `a logo gets an outline from its edges`() {
        // When styling a logo with no themed layer
        val masks = IconStyler.style(logo(), null, side)

        // Then the outline is drawn along the glyph's boundary
        assertNotNull(masks.outline)
        assertTrue(masks.outline!!.at(16, 24) ushr 24 > 200)
    }

    @Test
    fun `an icon with no plain plate gets no glyph`() {
        // Given noise, which has no single background colour
        val random = Random(1)
        val noise = IntArray(side * side) { (0xFF shl 24) or random.nextInt(0x1000000) }

        // When styling
        val masks = IconStyler.style(noise, null, side)

        // Then there is nothing to draw as a glyph
        assertNull(masks.glyph)
    }

    @Test
    fun `an art heavy icon gets no outline`() {
        // Given noise, which is edges everywhere
        val random = Random(1)
        val noise = IntArray(side * side) { (0xFF shl 24) or random.nextInt(0x1000000) }

        // When styling
        val masks = IconStyler.style(noise, null, side)

        // Then it is left to the monochrome fallback
        assertNull(masks.outline)
    }

    @Test
    fun `a striped logo gets a hollow outline of its glyph and needs no fallback`() {
        // Given a logo made of many thin stripes
        val striped = solid(plate)
        for (x in 3 until side - 3 step 6) striped.fill(x, x + 2, 3, side - 3, white)

        // When styling
        val masks = IconStyler.style(striped, null, side)

        // Then it still gets an outline, hollow inside a stripe, and needs no fallback
        val outline = masks.outline
        assertNotNull(outline)
        assertNull(masks.fallback)
        assertTrue(outline!!.any { it ushr 24 > 0 })
    }

    @Test
    fun `an icon that gets no glyph or outline gets its whole self shaded as a fallback`() {
        // Given noise, which has neither a plain plate nor tidy edges
        val random = Random(1)
        val noise = IntArray(side * side) { (0xFF shl 24) or random.nextInt(0x1000000) }

        // When styling
        val fallback = IconStyler.style(noise, null, side).fallback!!

        // Then the whole icon is there, opaque, shaded around the accent
        assertEquals(255, fallback.at(10, 10) ushr 24)
        assertEquals(255, fallback.at(30, 30) ushr 24)
    }

    @Test
    fun `an icon that gets both pictures needs no fallback`() {
        // When styling a plain logo, and a themed one
        val plain = IconStyler.style(logo(), null, side)
        val themed = IconStyler.style(logo(), themedBlock(14, 34), side)

        // Then neither has a fallback
        assertNull(plain.fallback)
        assertNull(themed.fallback)
    }

    @Test
    fun `a shaped icon has its plate dropped`() {
        // Given a plate with a white glyph inside a transparent border
        val icon = solid(0).also {
            it.fill(6, 42, 6, 42, plate)
            it.fill(16, 32, 16, 32, white)
        }

        // When styling
        val glyph = IconStyler.style(icon, null, side).glyph!!

        // Then the glyph stays and the plate is gone
        assertTrue(glyph.at(24, 24) ushr 24 > 200)
        assertEquals(0, glyph.at(9, 9) ushr 24)
    }

    @Test
    fun `the fallback carries the icon's own lightness`() {
        // Given an icon of a white half and a black half, with no plain plate
        val random = Random(2)
        val icon = IntArray(side * side) { if (it % side < side / 2) 0xFFFFFFFF.toInt() else 0xFF000000.toInt() }
        icon[0] = (0xFF shl 24) or random.nextInt(0x1000000)

        // When styling
        val masks = IconStyler.style(icon, null, side)

        // Then the fallback keeps the white and the black as light and dark greys
        assertEquals(0xFF, masks.fallback!!.at(5, 20) and 0xFF)
        assertEquals(0, masks.fallback!!.at(40, 20) and 0xFF)
        assertNotNull(masks.fallbackTones)
    }

    @Test
    fun `the accent is anchored at its own lightness among the icon's tones`() {
        // Given tones rising evenly from black to white
        val tones = FloatArray(21) { it / 20f }

        // Then a pale accent lands among the lightest tones, a dark one among the darkest, and the ends are clamped
        assertEquals(0.8f, IconStyler.anchorFor(tones, 0.8f), 0.001f)
        assertEquals(0.2f, IconStyler.anchorFor(tones, 0.2f), 0.001f)
        assertEquals(0f, IconStyler.anchorFor(tones, -1f), 0.001f)
        assertEquals(1f, IconStyler.anchorFor(tones, 2f), 0.001f)
    }

    @Test
    fun `a gradient plate still gets a glyph through the edge fill`() {
        // Given a white glyph on a gradient plate
        val icon = IntArray(side * side) { (0xFF shl 24) or ((220 - 3 * (it / side)) shl 16) or 0x2020 }.also { it.fill(16, 32, 16, 32, white) }

        // When styling
        val masks = IconStyler.style(icon, null, side)

        // Then the glyph is found and no fallback is needed for it
        assertTrue(masks.glyph!!.at(24, 24) ushr 24 > 200)
        assertEquals(0, masks.glyph!!.at(3, 3) ushr 24)
    }
}
