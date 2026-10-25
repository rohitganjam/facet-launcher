package com.facetlauncher.app.ui.home.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.domain.WeatherCondition

/** The few pictures weather needs: [WeatherCondition] and day or night pick one. */
enum class WeatherIconKind { SUN, MOON, PARTLY_SUN, PARTLY_MOON, CLOUD, FOG, RAIN, SNOW, THUNDER }

fun weatherIconKind(condition: WeatherCondition?, night: Boolean): WeatherIconKind = when (condition) {
    WeatherCondition.CLEAR -> if (night) WeatherIconKind.MOON else WeatherIconKind.SUN
    WeatherCondition.MOSTLY_CLEAR, WeatherCondition.PARTLY_CLOUDY -> if (night) WeatherIconKind.PARTLY_MOON else WeatherIconKind.PARTLY_SUN
    WeatherCondition.FOG -> WeatherIconKind.FOG
    WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN, WeatherCondition.SLEET -> WeatherIconKind.RAIN
    WeatherCondition.SNOW -> WeatherIconKind.SNOW
    WeatherCondition.THUNDERSTORM -> WeatherIconKind.THUNDER
    WeatherCondition.CLOUDY, null -> WeatherIconKind.CLOUD
}

internal const val ICON_GRID = 24f
internal const val ICON_STROKE = 1.8f
private const val RAY_COUNT = 8

/**
 * A weather picture. Monochrome ([style] `MONOCHROME`) is drawn in [tint] on a 24 by 24 grid scaled to the box, so it is
 * crisp at any size; coloured ignores [tint]. Decorative: callers describe the weather in words.
 */
@Composable
fun WeatherIcon(kind: WeatherIconKind, tint: Color, modifier: Modifier = Modifier, style: WeatherIconStyle = WeatherIconStyle.MONOCHROME) {
    if (style == WeatherIconStyle.COLORED) {
        Image(painter = painterResource(kind.coloredRes()), contentDescription = null, modifier = modifier)
        return
    }
    Canvas(modifier = modifier) {
        val factor = size.minDimension / ICON_GRID
        scale(factor, factor, pivot = Offset.Zero) { drawKind(kind, tint) }
    }
}

private fun DrawScope.drawKind(kind: WeatherIconKind, tint: Color) {
    when (kind) {
        WeatherIconKind.SUN -> drawSun(Offset(12f, 12f), 4.2f, 7f, 10f, tint)
        WeatherIconKind.MOON -> drawMoon(Offset(12f, 12f), 8f, tint)
        WeatherIconKind.PARTLY_SUN -> drawPartly(tint) { drawSun(Offset(9f, 9f), 3.2f, 5.4f, 7.6f, tint) }
        WeatherIconKind.PARTLY_MOON -> drawPartly(tint) { drawMoon(Offset(9.5f, 9.5f), 6f, tint) }
        WeatherIconKind.CLOUD -> drawPath(cloud(dy = 0.5f), tint, style = Fill)
        WeatherIconKind.FOG -> drawFog(tint)
        WeatherIconKind.RAIN -> drawRain(tint)
        WeatherIconKind.SNOW -> drawSnow(tint)
        WeatherIconKind.THUNDER -> drawThunder(tint)
    }
}

/** A cloud (three lobes on a flat base) as one filled shape, moved by [dx]/[dy] and scaled by [size] about the grid's centre. */
private fun cloud(dx: Float = 0f, dy: Float = 0f, size: Float = 1f, grow: Float = 0f): Path {
    fun circle(cx: Float, cy: Float, r: Float) = Path().apply {
        addOval(Rect(Offset(dx + (12f + (cx - 12f) * size), dy + (12f + (cy - 12f) * size)), (r * size) + grow))
    }
    val base = Path().apply {
        val left = dx + 12f + (5.2f - 12f) * size - grow
        val right = dx + 12f + (20.2f - 12f) * size + grow
        val top = dy + 12f + (13.8f - 12f) * size - grow
        val bottom = dy + 12f + (18.6f - 12f) * size + grow
        addRoundRect(RoundRect(left, top, right, bottom, CornerRadius(2.4f * size + grow)))
    }
    return listOf(circle(8.4f, 14.6f, 3.6f), circle(12.8f, 11.6f, 4.8f), circle(17f, 14.8f, 3.4f))
        .fold(base) { acc, part -> Path.combine(PathOperation.Union, acc, part) }
}

private fun DrawScope.drawSun(centre: Offset, radius: Float, rayFrom: Float, rayTo: Float, tint: Color) {
    drawCircle(tint, radius, centre, style = Stroke(ICON_STROKE))
    for (i in 0 until RAY_COUNT) {
        val angle = Math.toRadians(i * 360.0 / RAY_COUNT)
        val dx = Math.cos(angle).toFloat()
        val dy = Math.sin(angle).toFloat()
        drawLine(tint, centre + Offset(dx * rayFrom, dy * rayFrom), centre + Offset(dx * rayTo, dy * rayTo), ICON_STROKE, StrokeCap.Round)
    }
}

private fun DrawScope.drawMoon(centre: Offset, radius: Float, tint: Color) {
    val bite = Path().apply { addOval(Rect(centre + Offset(radius * 0.45f, -radius * 0.45f), radius * 0.85f)) }
    clipPath(bite, ClipOp.Difference) { drawCircle(tint, radius, centre, style = Fill) }
}

/** A sun or moon behind a cloud, with a gap cut around the cloud so the two read as separate shapes. */
private fun DrawScope.drawPartly(tint: Color, behind: DrawScope.() -> Unit) {
    val front = cloud(dx = 2.6f, dy = 3.2f, size = 0.86f)
    clipPath(cloud(dx = 2.6f, dy = 3.2f, size = 0.86f, grow = 1.4f), ClipOp.Difference) { behind() }
    drawPath(front, tint, style = Fill)
}

private fun DrawScope.drawFog(tint: Color) {
    listOf(7.5f to (4f to 20f), 12f to (6f to 18f), 16.5f to (4f to 20f)).forEach { (y, span) ->
        drawLine(tint, Offset(span.first, y), Offset(span.second, y), ICON_STROKE * 1.2f, StrokeCap.Round)
    }
}

private fun DrawScope.drawRain(tint: Color) {
    drawPath(cloud(dy = -3.2f, size = 0.94f), tint, style = Fill)
    listOf(8.2f, 12.2f, 16.2f).forEach { x ->
        drawLine(tint, Offset(x + 0.8f, 17.6f), Offset(x - 0.8f, 21.4f), ICON_STROKE, StrokeCap.Round)
    }
}

private fun DrawScope.drawSnow(tint: Color) {
    drawPath(cloud(dy = -3.2f, size = 0.94f), tint, style = Fill)
    listOf(8f to 18.4f, 12f to 21f, 16f to 18.4f).forEach { (x, y) -> drawCircle(tint, 1.25f, Offset(x, y)) }
}

private fun DrawScope.drawThunder(tint: Color) {
    drawPath(cloud(dy = -3.4f, size = 0.94f), tint, style = Fill)
    val bolt = Path().apply {
        moveTo(13.4f, 15.6f)
        lineTo(9.6f, 20.4f)
        lineTo(12.2f, 20.4f)
        lineTo(10.8f, 23.4f)
        lineTo(15.2f, 18.4f)
        lineTo(12.6f, 18.4f)
        close()
    }
    drawPath(bolt, tint, style = Fill)
}
