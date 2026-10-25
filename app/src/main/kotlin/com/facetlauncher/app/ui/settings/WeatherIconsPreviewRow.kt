package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.home.weather.WeatherIcon
import com.facetlauncher.app.ui.home.weather.WeatherIconKind
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink

private val PREVIEW_ICON_SIZE = 28.dp

/** Every weather picture in [style], so choosing monochrome or coloured shows what Home will use. */
@Composable
internal fun WeatherIconsPreviewRow(style: WeatherIconStyle, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("weather_icons_preview_${style.name}"),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        WeatherIconKind.entries.forEach { kind ->
            WeatherIcon(kind = kind, tint = Ink, style = style, modifier = Modifier.size(PREVIEW_ICON_SIZE))
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherIconsPreviewRowPreview() {
    FacetLauncherTheme {
        SettingsCard(modifier = Modifier.padding(24.dp)) {
            WeatherIconStyle.entries.forEach { WeatherIconsPreviewRow(style = it) }
        }
    }
}
