package com.facetlauncher.app.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.facetlauncher.app.data.model.IconShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SuperellipseShapeTest {

    @Test
    fun `each icon shape maps to its own compose shape`() {
        // Given / When / Then
        assertSame(SquircleShape, IconShape.SQUIRCLE.toComposeShape())
        assertSame(RoundedShape, IconShape.ROUNDED.toComposeShape())
        assertSame(CircleShape, IconShape.CIRCLE.toComposeShape())
        assertSame(RectangleShape, IconShape.SQUARE.toComposeShape())
    }

    @Test
    fun `superellipse outline spans exactly its bounds for both exponents`() {
        // Given a 100x100 box
        val size = Size(100f, 100f)

        listOf(SquircleShape, RoundedShape).forEach { shape ->
            // When creating the outline
            val outline = shape.createOutline(size, LayoutDirection.Ltr, Density(1f)) as Outline.Generic
            val bounds = outline.path.getBounds()

            // Then it touches all four edges and stays inside the box
            assertEquals(0f, bounds.left, 0.01f)
            assertEquals(0f, bounds.top, 0.01f)
            assertEquals(100f, bounds.right, 0.01f)
            assertEquals(100f, bounds.bottom, 0.01f)
        }
    }
}
