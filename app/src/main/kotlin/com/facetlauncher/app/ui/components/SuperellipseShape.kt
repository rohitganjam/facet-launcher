package com.facetlauncher.app.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.facetlauncher.app.data.model.IconShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/** Superellipse |x|^n + |y|^n = 1; n=2 is a circle, larger n squares the corners. */
class SuperellipseShape(private val exponent: Double) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val halfW = size.width / 2f
        val halfH = size.height / 2f
        val exp = 2.0 / exponent
        val path = Path()
        for (i in 0..STEPS) {
            val t = 2.0 * PI * i / STEPS
            val c = cos(t)
            val s = sin(t)
            val x = halfW + halfW * (sign(c) * abs(c).pow(exp)).toFloat()
            val y = halfH + halfH * (sign(s) * abs(s).pow(exp)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }

    private companion object {
        const val STEPS = 72
    }
}

val SquircleShape = SuperellipseShape(exponent = 4.0)
val RoundedShape = SuperellipseShape(exponent = 2.6)

fun IconShape.toComposeShape(): Shape = when (this) {
    IconShape.SQUIRCLE -> SquircleShape
    IconShape.ROUNDED -> RoundedShape
    IconShape.CIRCLE -> CircleShape
    IconShape.SQUARE -> RectangleShape
}
