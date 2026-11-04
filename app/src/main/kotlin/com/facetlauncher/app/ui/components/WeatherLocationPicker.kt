package com.facetlauncher.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.isSamePlaceAs
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import kotlinx.collections.immutable.ImmutableList

/**
 * Where a Weather widget, or a facet's clock, shows the weather for: the same as Weather settings, the current location, or one of the
 * cities saved there. One control for both, so they read and behave alike. [location] `null` is "same as Weather settings".
 * Test tags are `<tagPrefix>_follow`, `_current` and `_city_<name>`.
 */
@Composable
fun WeatherLocationPicker(
    location: WidgetWeatherLocation?,
    savedCities: ImmutableList<WeatherCity>,
    onChange: (WidgetWeatherLocation?) -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        LocationRow(stringResource(R.string.hub_widget_config_use_launcher), location == null, "${tagPrefix}_follow", enabled) { onChange(null) }
        LocationRow(stringResource(R.string.hub_widget_config_current_location), location == WidgetWeatherLocation.Current, "${tagPrefix}_current", enabled) {
            onChange(WidgetWeatherLocation.Current)
        }
        Text(
            text = stringResource(R.string.hub_widget_config_location),
            style = MaterialTheme.typography.labelLarge,
            color = Muted,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (savedCities.isEmpty()) {
            Text(text = stringResource(R.string.hub_widget_config_no_cities), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        savedCities.forEach { city ->
            val selected = (location as? WidgetWeatherLocation.City)?.city?.isSamePlaceAs(city) == true
            LocationRow(city.name, selected, "${tagPrefix}_city_${city.name}", enabled) { onChange(WidgetWeatherLocation.City(city)) }
        }
    }
}

@Composable
private fun LocationRow(label: String, selected: Boolean, tag: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = ROW_VERTICAL_PADDING).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled, colors = RadioButtonDefaults.colors(selectedColor = Accent))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) Ink else Muted,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/** Space above and below each choice, so the list is easy to read and to tap in a settings card and in a widget's sheet alike. */
private val ROW_VERTICAL_PADDING = 10.dp
