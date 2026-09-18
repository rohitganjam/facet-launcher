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
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.resolve
import kotlin.math.roundToInt

private val STOPS = FontWeightOption.entries

@Composable
fun FontWeightSlider(
    selected: FontWeightOption,
    onSelectedChange: (FontWeightOption) -> Unit,
    modifier: Modifier = Modifier,
    previewText: String = "The quick brown fox",
    /** Tags the interactive [Slider] node itself (not this composable's own outer block) — tests
     *  drive it via the `SetProgress` semantics action, which only the [Slider] exposes. */
    sliderTestTag: String? = null,
) {
    val selectedIndex = STOPS.indexOf(selected)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Weight", style = MaterialTheme.typography.bodyLarge, color = Ink)
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
        Text(
            text = previewText,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = selected.resolve()),
            color = Ink,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Drag this one in Studio's interactive preview to feel the snap-to-stop behavior live. */
@Preview(name = "Interactive — drag me", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontWeightSliderInteractivePreview() {
    var selected by remember { mutableStateOf(FontWeightOption.REGULAR) }
    FacetLauncherTheme {
        FontWeightSlider(
            selected = selected,
            onSelectedChange = { selected = it },
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Thin", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontWeightSliderThinPreview() {
    FacetLauncherTheme {
        FontWeightSlider(selected = FontWeightOption.THIN, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Regular", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontWeightSliderRegularPreview() {
    FacetLauncherTheme {
        FontWeightSlider(selected = FontWeightOption.REGULAR, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Semi Bold", showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun FontWeightSliderSemiBoldPreview() {
    FacetLauncherTheme {
        FontWeightSlider(selected = FontWeightOption.SEMI_BOLD, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

@Preview(name = "Dark — Regular", showBackground = true, widthDp = 360, heightDp = 160, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FontWeightSliderDarkPreview() {
    FacetLauncherTheme {
        FontWeightSlider(selected = FontWeightOption.REGULAR, onSelectedChange = {}, modifier = Modifier.padding(24.dp))
    }
}

/**
 * All 6 stops stacked at once, for a side-by-side sense of the weight steps without dragging
 * anything. `heightDp` is generous on purpose (6 * ~150dp of content) — too tight a fixed preview
 * height silently clips the last row or two instead of erroring, which read as "the option isn't
 * there" rather than "the preview canvas was too short" (see chat history).
 */
@Preview(name = "All stops", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun FontWeightSliderAllStopsPreview() {
    FacetLauncherTheme {
        FontWeightSliderAllStopsContent()
    }
}

@Preview(name = "All stops — Dark", showBackground = true, widthDp = 360, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FontWeightSliderAllStopsDarkPreview() {
    FacetLauncherTheme {
        FontWeightSliderAllStopsContent()
    }
}

@Composable
private fun FontWeightSliderAllStopsContent() {
    Column(modifier = Modifier.padding(24.dp)) {
        STOPS.forEach { stop ->
            FontWeightSlider(
                selected = stop,
                onSelectedChange = {},
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}
