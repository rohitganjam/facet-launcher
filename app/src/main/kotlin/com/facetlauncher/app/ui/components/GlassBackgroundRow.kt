package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * A "background" dropdown over every [GlassStyle]. Picking a glass style while there is no wallpaper copy also shows a short sheet
 * explaining that glass needs one, with *Choose an image* and a remembered *Not now*; the style is applied either way.
 */
@Composable
fun GlassBackgroundRow(
    title: String,
    selected: GlassStyle,
    onSelect: (GlassStyle) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
    /** Also offer the solid Theme and Theme inverted fills, which only widgets do. */
    includeSolid: Boolean = false,
) {
    val prompt = LocalGlassBackgroundPrompt.current
    var showPrompt by remember { mutableStateOf(false) }
    LabeledDropdownRow(
        title = title,
        options = if (includeSolid) GlassStyle.entries else GlassStyle.glassOnly,
        selected = selected,
        label = { stringResource(it.displayNameRes) },
        onSelect = {
            onSelect(it)
            if (it != GlassStyle.OFF && !it.isSolid && prompt?.needsCopy == true) showPrompt = true
        },
        modifier = modifier,
        enabled = enabled,
        testTag = testTag,
    )
    if (showPrompt && prompt != null) {
        ThemedModalBottomSheet(onDismissRequest = { showPrompt = false }, skipPartiallyExpanded = true) {
            Column(modifier = Modifier.padding(24.dp).testTag("glass_prompt"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.glass_prompt_title), style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(text = stringResource(R.string.glass_prompt_body), style = MaterialTheme.typography.bodyMedium, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryButton(
                        text = stringResource(R.string.wallpaper_offer_accept),
                        onClick = {
                            showPrompt = false
                            prompt.onChooseImage()
                        },
                        modifier = Modifier.testTag("glass_prompt_choose"),
                    )
                    TextActionButton(
                        text = stringResource(R.string.wallpaper_offer_decline),
                        onClick = {
                            showPrompt = false
                            prompt.onDecline()
                        },
                        modifier = Modifier.testTag("glass_prompt_decline"),
                    )
                }
            }
        }
    }
}
