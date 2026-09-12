package com.lumenlauncher.app.ui.home.clock

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Default size for [AlarmClockIcon] and [WeatherMark] — a square glyph matching the design's `viewBox="0 0 12 12"` mark. */
val ClockAccessoryIconSize = 11.dp

/** Default size for [BatteryIcon] — matches the design's `viewBox="0 0 16 11"` mark's own aspect ratio. */
val BatteryIconSize = DpSize(15.dp, 11.dp)

/**
 * The clock accessory row's alarm-clock glyph — a static decorative mark (not a time indicator
 * itself; the alarm time is the adjacent text), redrawn from the design's `viewBox="0 0 12 12"`
 * circle-plus-hands-plus-ears SVG as plain [Canvas] draws rather than a path-based `ImageVector`,
 * since every element here is a primitive circle/line.
 */
@Composable
fun AlarmClockIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 12f
        val sy = size.height / 12f
        fun pt(x: Float, y: Float) = Offset(x * sx, y * sy)
        val strokeWidth = 1.2f * sx
        drawCircle(color = tint, radius = 4.1f * sx, center = pt(6f, 6.9f), style = Stroke(width = strokeWidth))
        drawLine(tint, pt(6f, 4.8f), pt(6f, 6.9f), strokeWidth, cap = StrokeCap.Round)
        drawLine(tint, pt(6f, 6.9f), pt(7.4f, 7.8f), strokeWidth, cap = StrokeCap.Round)
        drawLine(tint, pt(1.7f, 2.4f), pt(3.3f, 1.1f), strokeWidth, cap = StrokeCap.Round)
        drawLine(tint, pt(10.3f, 2.4f), pt(8.7f, 1.1f), strokeWidth, cap = StrokeCap.Round)
    }
}

/** Battery reads "low" under this percent — the single threshold both [BatteryAccessoryContent]'s [accentColor] switch and [batteryIconState]'s sliver tier key off of, so the two never disagree about what counts as low. */
const val LOW_BATTERY_THRESHOLD = 20

/** Battery reads "full" at or above this percent — see [batteryIconState]. */
private const val FULL_BATTERY_THRESHOLD = 80

/** Which of [BatteryIcon]'s four fixed looks to draw. [CHARGING] wins outright — a charging battery reads as "topping up," not by its fill level; otherwise it's a plain 3-tier gauge on percent alone. */
enum class BatteryIconState { CHARGING, FULL, PARTIAL, LOW }

fun batteryIconState(percent: Int, isCharging: Boolean): BatteryIconState = when {
    isCharging -> BatteryIconState.CHARGING
    percent < LOW_BATTERY_THRESHOLD -> BatteryIconState.LOW
    percent < FULL_BATTERY_THRESHOLD -> BatteryIconState.PARTIAL
    else -> BatteryIconState.FULL
}

/**
 * The clock accessory row's battery glyph — the outline-plus-nub is the design's fixed
 * `viewBox="0 0 16 11"` mark in every state; what's drawn inside it changes with [state]: a bolt
 * while [BatteryIconState.CHARGING], otherwise a fill from near-full down to a thin sliver.
 */
@Composable
fun BatteryIcon(tint: Color, state: BatteryIconState = BatteryIconState.FULL, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sx = size.width / 16f
        val sy = size.height / 11f
        drawRoundRect(
            color = tint,
            topLeft = Offset(0.6f * sx, 1.7f * sy),
            size = Size(12f * sx, 7.6f * sy),
            cornerRadius = CornerRadius(2.2f * sx, 2.2f * sy),
            style = Stroke(width = 1.2f * sx),
        )
        drawLine(
            color = tint,
            start = Offset(14.5f * sx, 4.3f * sy),
            end = Offset(14.5f * sx, 6.7f * sy),
            strokeWidth = 1.6f * sx,
            cap = StrokeCap.Round,
        )
        fun drawFill(width: Float, cornerRadius: Float) = drawRoundRect(
            color = tint,
            topLeft = Offset(2.3f * sx, 3.4f * sy),
            size = Size(width * sx, 4.2f * sy),
            cornerRadius = CornerRadius(cornerRadius * sx, cornerRadius * sy),
        )
        when (state) {
            BatteryIconState.CHARGING -> {
                val bolt = Path().apply {
                    moveTo(7.4f * sx, 1.9f * sy)
                    lineTo(3.9f * sx, 6.3f * sy)
                    lineTo(6.1f * sx, 6.3f * sy)
                    lineTo(5.1f * sx, 9.3f * sy)
                    lineTo(8.9f * sx, 4.5f * sy)
                    lineTo(6.7f * sx, 4.5f * sy)
                    close()
                }
                drawPath(bolt, color = tint)
            }
            BatteryIconState.FULL -> drawFill(width = 8.4f, cornerRadius = 1f)
            BatteryIconState.PARTIAL -> drawFill(width = 6.1f, cornerRadius = 1f)
            BatteryIconState.LOW -> drawFill(width = 1.6f, cornerRadius = 0.8f)
        }
    }
}

/**
 * Reserved weather glyph — a plain outlined circle, matching the design's weather mark. Not
 * currently called anywhere: weather has no repository/data source yet (see
 * [com.lumenlauncher.app.data.model.WeatherInfo]'s own doc), so this exists only to reserve the
 * slot without wiring it up.
 */
@Composable
fun WeatherMark(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(color = tint, radius = size.minDimension / 2f - 0.75f.dp.toPx(), style = Stroke(width = 1.5f.dp.toPx()))
    }
}
