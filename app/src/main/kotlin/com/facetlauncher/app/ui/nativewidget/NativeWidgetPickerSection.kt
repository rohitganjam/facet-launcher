package com.facetlauncher.app.ui.nativewidget

import kotlinx.collections.immutable.ImmutableList
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/** The widgets Facet draws itself, offered above the installed apps' widgets in both pickers; each card is the widget at its default size with sample data. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NativeWidgetPickerSection(types: ImmutableList<NativeWidgetType>, onSelect: (NativeWidgetType) -> Unit, modifier: Modifier = Modifier) {
    if (types.isEmpty()) return
    Column(modifier = modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.native_widget_picker_section), style = MaterialTheme.typography.titleSmall, color = Muted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            types.forEach { type -> NativeWidgetOption(type, onSelect) }
        }
    }
}

@Composable
private fun NativeWidgetOption(type: NativeWidgetType, onSelect: (NativeWidgetType) -> Unit) {
    val (w, h) = type.defaultSizeDp()
    Column(modifier = Modifier.testTag("native_widget_option_${type.name}"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box {
            NativeWidgetPreviewBox(type, w, h)
            // The sample is live, so it would take the tap itself: this layer catches it for the picker.
            Box(modifier = Modifier.matchParentSize().clickable { onSelect(type) })
        }
        Text(stringResource(type.labelRes()), style = MaterialTheme.typography.labelLarge, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NativeWidgetPickerSectionPreview() {
    FacetLauncherTheme { NativeWidgetPickerSection(types = ALL_NATIVE_WIDGETS, onSelect = {}, modifier = Modifier.padding(16.dp).size(358.dp, 400.dp)) }
}
