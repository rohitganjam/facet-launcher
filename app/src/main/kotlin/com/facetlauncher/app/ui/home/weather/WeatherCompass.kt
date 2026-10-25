package com.facetlauncher.app.ui.home.weather

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R

private const val COMPASS_POINTS = 8
private const val DEGREES_PER_POINT = 45.0

@StringRes
private fun compassRes(point: Int): Int = when (point) {
    0 -> R.string.weather_direction_n
    1 -> R.string.weather_direction_ne
    2 -> R.string.weather_direction_e
    3 -> R.string.weather_direction_se
    4 -> R.string.weather_direction_s
    5 -> R.string.weather_direction_sw
    6 -> R.string.weather_direction_w
    else -> R.string.weather_direction_nw
}

/** The compass point the wind blows *from* (MET's `wind_from_direction`): 0° is north, 90° east. */
@Composable
internal fun compassPoint(fromDegrees: Double): String = stringResource(compassRes(compassIndex(fromDegrees)))

internal fun compassIndex(fromDegrees: Double): Int =
    (((fromDegrees % 360.0) + 360.0) % 360.0 / DEGREES_PER_POINT + 0.5).toInt() % COMPASS_POINTS
