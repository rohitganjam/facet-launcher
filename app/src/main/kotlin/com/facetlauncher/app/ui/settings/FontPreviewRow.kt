package com.facetlauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.LauncherScaledTypography
import com.facetlauncher.app.ui.theme.resolve

/**
 * Sample text in the three styles Home uses (app row, dock text, grid label) with the chosen font style, size, weight and
 * color, as the last row of the Fonts card. Settings text itself ignores those options, so this is where their effect shows.
 */
@Composable
internal fun FontPreviewRow(settings: LauncherSettings, modifier: Modifier = Modifier) {
    val color = settings.appLabelColorOption.resolve()
    val weight = settings.homeAppsFontWeight.resolve()
    LauncherScaledTypography {
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 13.dp).testTag("font_preview_card"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.font_preview_row),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = weight),
                color = color,
            )
            Text(text = stringResource(R.string.font_preview_body), style = MaterialTheme.typography.bodyMedium, color = color)
            Text(
                text = stringResource(R.string.font_preview_label),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = weight),
                color = color,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun FontPreviewRowPreview() {
    FacetLauncherTheme {
        SettingsCard(modifier = Modifier.padding(24.dp)) { FontPreviewRow(settings = LauncherSettings()) }
    }
}
