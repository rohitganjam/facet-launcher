package com.facetlauncher.app.ui.home.weather

import androidx.annotation.DrawableRes
import com.facetlauncher.app.R

/** The coloured picture for [kind]; there is no night cloud, fog, rain, snow or storm, so those use the day one. */
@DrawableRes
fun WeatherIconKind.coloredRes(): Int = when (this) {
    WeatherIconKind.SUN -> R.drawable.weather_color_clear_day
    WeatherIconKind.MOON -> R.drawable.weather_color_clear_night
    WeatherIconKind.PARTLY_SUN -> R.drawable.weather_color_partly_cloudy_day
    WeatherIconKind.PARTLY_MOON -> R.drawable.weather_color_partly_cloudy_night
    WeatherIconKind.CLOUD -> R.drawable.weather_color_cloudy_day
    WeatherIconKind.FOG -> R.drawable.weather_color_fog_day
    WeatherIconKind.RAIN -> R.drawable.weather_color_rain_day
    WeatherIconKind.SNOW -> R.drawable.weather_color_snow_day
    WeatherIconKind.THUNDER -> R.drawable.weather_color_thunderstorm_day
}
