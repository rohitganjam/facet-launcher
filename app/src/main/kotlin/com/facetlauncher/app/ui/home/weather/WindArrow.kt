package com.facetlauncher.app.ui.home.weather

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

/** A small arrow that points "up" (north) before [rotationDegrees] is applied, for where the wind is blowing to. */
@Composable
fun WindArrow(rotationDegrees: Float, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val factor = size.minDimension / ICON_GRID
        scale(factor, factor, pivot = Offset.Zero) {
            rotate(rotationDegrees, pivot = Offset(12f, 12f)) {
                drawLine(tint, Offset(12f, 20f), Offset(12f, 5f), ICON_STROKE * 1.4f, StrokeCap.Round)
                drawLine(tint, Offset(12f, 4.5f), Offset(6.5f, 10f), ICON_STROKE * 1.4f, StrokeCap.Round)
                drawLine(tint, Offset(12f, 4.5f), Offset(17.5f, 10f), ICON_STROKE * 1.4f, StrokeCap.Round)
            }
        }
    }
}
