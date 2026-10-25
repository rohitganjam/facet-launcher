package com.facetlauncher.app.ui.home.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.ui.home.weather.WeatherIcon
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.theme.homeIconShadow
import com.facetlauncher.app.ui.theme.homeTextShadow

/**
 * The weather item of the clock's accessory row: a picture and the temperature, nothing else. [contentDescription] is the whole
 * spoken text ("27°, Light rain. Opens the weather details"). Tapping it calls [onClick], which opens the sheet.
 */
@Immutable
data class ClockWeatherAccessory(
    val iconKind: WeatherIconKind,
    val temperatureText: String,
    val contentDescription: String,
    val iconStyle: WeatherIconStyle = WeatherIconStyle.MONOCHROME,
    val onClick: () -> Unit,
)

/**
 * The weather the clock's accessory rows show, or `null` for none. A local rather than a parameter of every template, so
 * the three accessories sit together without each of the template composables passing a fourth value along.
 */
@Suppress("CompositionLocalAllowlist") // Read by every template's accessory row; threading it as a parameter would touch all of them.
internal val LocalClockWeather = compositionLocalOf<ClockWeatherAccessory?> { null }

/** The weather picture leaves about a sixth of its box empty, so its box is that much larger to look as big as the alarm and battery glyphs. */
private val WeatherIconBoxSize = ClockAccessoryIconSize * 1.2f

/**
 * The weather accessory in [color] at the row's text size, for the places that lay the accessories out one by one. A
 * no-op without weather. The touch area is just the item, the same size as the alarm and battery items: a bigger one would
 * change the pills and push the clock's other rows around, so it also answers TalkBack's double tap wherever it is focused.
 */
@Composable
fun WeatherAccessoryContent(
    color: Color,
    family: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
) {
    val weather = LocalClockWeather.current ?: return
    Row(
        modifier = modifier
            .clickable(role = Role.Button, onClick = weather.onClick)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = weather.contentDescription
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        WeatherIcon(weather.iconKind, color, Modifier.size(WeatherIconBoxSize).homeIconShadow(color).clearAndSetSemantics { }, weather.iconStyle)
        Text(
            text = weather.temperatureText,
            style = TextStyle(fontFamily = family, fontWeight = fontWeight, fontSize = fontSize, shadow = homeTextShadow(color)),
            color = color,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
