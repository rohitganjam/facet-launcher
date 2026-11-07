package com.facetlauncher.app.ui.nativewidget

import kotlinx.collections.immutable.ImmutableList
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.res.pluralStringResource
import com.facetlauncher.app.ui.hub.picker.WIDGET_TILE_HEIGHT
import com.facetlauncher.app.ui.hub.picker.WIDGET_TILE_SPACING
import com.facetlauncher.app.ui.theme.Hairline
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import com.facetlauncher.app.ui.home.ClockBlock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.defaultSizeDp
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/** The widgets Facet draws itself, offered above the installed apps' widgets in both pickers; each tile shows the widget at its default size with sample data, like an installed app's. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NativeWidgetPickerSection(
    types: ImmutableList<NativeWidgetType>,
    tileWidth: Dp,
    onSelect: (NativeWidgetType) -> Unit,
    modifier: Modifier = Modifier,
    /** Offered first, in the Home pickers only and when the facet has no clock; the Hub never takes one. */
    showClock: Boolean = false,
    onClockSelect: () -> Unit = {},
) {
    if (types.isEmpty() && !showClock) return
    val count = types.size + if (showClock) 1 else 0
    Column(modifier = modifier.padding(top = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.native_widget_picker_section), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(
                text = pluralStringResource(R.plurals.hub_widget_picker_option_count, count, count),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = 11.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WIDGET_TILE_SPACING),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            if (showClock) {
                NativeTile("CLOCK", R.string.native_widget_clock, tileWidth, DpSize(150.dp, 70.dp), onClockSelect) {
                    ClockBlock(clockScale = 0.5f, modifier = Modifier.size(width = 150.dp, height = 70.dp))
                }
            }
            types.forEach { type ->
                val (w, h) = type.defaultSizeDp()
                NativeTile(type.name, type.labelRes(), tileWidth, DpSize(w.dp, h.dp), { onSelect(type) }) { NativeWidgetPreviewBox(type, w, h) }
            }
        }
    }
}

/** A bordered tile like an installed widget's, with the name under it; [content] is the sample and its natural size, scaled down to fit. */
@Composable
private fun NativeTile(key: String, labelRes: Int, tileWidth: Dp, naturalSize: DpSize, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.width(tileWidth).testTag("native_widget_option_$key")) {
        val shape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier.fillMaxWidth().height(WIDGET_TILE_HEIGHT).clip(shape).border(1.dp, Hairline, shape),
            contentAlignment = Alignment.Center,
        ) {
            // The sample is live, so it would take the tap itself: the click layer on top catches it for the picker.
            val fit = minOf((tileWidth - 16.dp) / naturalSize.width, (WIDGET_TILE_HEIGHT - 16.dp) / naturalSize.height, 1f)
            Box(modifier = Modifier.requiredSize(naturalSize).scale(fit)) { content() }
            Box(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
        }
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NativeWidgetPickerSectionPreview() {
    FacetLauncherTheme { NativeWidgetPickerSection(types = ALL_NATIVE_WIDGETS, tileWidth = 170.dp, onSelect = {}, modifier = Modifier.padding(16.dp).size(358.dp, 400.dp)) }
}
