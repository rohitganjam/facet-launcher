package com.facetlauncher.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import kotlin.math.roundToInt

private val STOPS = FontScaleOption.entries

/** Mirrors [FontWeightSlider]'s own shape — a stepped [Slider] walking [FontScaleOption.entries]
 *  by index, same reasoning as that component's own doc. */
@Composable
fun FontSizeSlider(
    selected: FontScaleOption,
    onSelectedChange: (FontScaleOption) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Size",
    previewText: String = "The quick brown fox",
    /** See [FontWeightSlider.showPreview]'s own doc — same reasoning. */
    showPreview: Boolean = true,
    /** Tags the interactive [Slider] node itself — see [FontWeightSlider.sliderTestTag]. */
    sliderTestTag: String? = null,
) {
    val selectedIndex = STOPS.indexOf(selected)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = stringResource(selected.displayNameRes), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { onSelectedChange(STOPS[it.roundToInt().coerceIn(0, STOPS.lastIndex)]) },
            valueRange = 0f..(STOPS.size - 1).toFloat(),
            steps = STOPS.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = Hairline,
                activeTickColor = Surface,
                inactiveTickColor = Muted,
            ),
            modifier = sliderTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        )
        if (showPreview) {
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * selected.scale),
                color = Ink,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Drag this one in Studio's interactive preview to feel the snap-to-stop behavior live. */
@Preview(name = "Interactive — drag me", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontSizeSliderInteractivePreview() {
    var selected by remember { mutableStateOf(FontScaleOption.DEFAULT) }
    FacetLauncherTheme {
        FontSizeSlider(
            selected = selected,
            onSelectedChange = { selected = it },
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Small", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontSizeSliderSmallPreview() {
    FacetLauncherTheme {
        FontSizeSlider(selected = FontScaleOption.SMALL, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Default", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontSizeSliderDefaultPreview() {
    FacetLauncherTheme {
        FontSizeSlider(selected = FontScaleOption.DEFAULT, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Huge", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontSizeSliderHugePreview() {
    FacetLauncherTheme {
        FontSizeSlider(selected = FontScaleOption.HUGE, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Dark — Default", showBackground = true, widthDp = 360, heightDp = 160, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FontSizeSliderDarkPreview() {
    FacetLauncherTheme {
        FontSizeSlider(selected = FontScaleOption.DEFAULT, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

/** All stops stacked at once — see [FontWeightSlider]'s own equivalent for why `heightDp` is generous. */
@Preview(name = "All stops", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun FontSizeSliderAllStopsPreview() {
    FacetLauncherTheme {
        Column(modifier = Modifier.padding(24.dp)) {
            STOPS.forEach { stop ->
                FontSizeSlider(
                    selected = stop,
                    onSelectedChange = {},
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }
    }
}
