package com.facetlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.components.dashedBorder
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.HomeAppTextColor
import com.facetlauncher.app.ui.theme.HomeAppTextColorFaint
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/** README `4e` — provider uninstalled. The cell keeps its footprint, dashed border, never a dead frame. */
@Composable
fun OrphanedWidgetTile(providerLabel: String?, onRemove: () -> Unit, onKeepSpace: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("hub_orphaned_widget")
            .dashedBorder(color = HomeAppTextColorFaint, cornerRadius = 12.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // weight(1f, fill = false) — NOT the default fill = true — so this info block only ever
        // claims space it actually needs, and NEVER at the expense of the Remove/Keep space row
        // below. On a 1-row-tall orphaned tile (rowSpan=1 is a completely normal size for a
        // provider that later got uninstalled) these two lines of text can, at this typeography,
        // consume the tile's entire content height on their own — without this weight, the Row
        // below was measured with whatever was left over, which was zero, silently laying out a
        // "Remove"/"Keep space" row with zero height. That's not just a cosmetic clip: real touch
        // input (Compose's own performClick() included) hit-tests actual screen coordinates, and a
        // zero-height target can never be tapped — the widget became permanently un-removable
        // (see chat history: this is what HubScreenTest's removingAnOrphanedWidgetDeletesItsPlacement
        // was catching). Letting this Column size itself and cede space to the Row below fixes that
        // — worst case on a very short tile, this text visually clips, which is far better than an
        // orphaned widget the user can never get rid of.
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = "${providerLabel ?: "This"} widget unavailable",
                style = MaterialTheme.typography.labelMedium.copy(shadow = homeAppLabelShadow(HomeAppTextColor)),
                color = HomeAppTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "The app was uninstalled",
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(HomeAppTextColorFaint)),
                color = HomeAppTextColorFaint,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Remove",
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(ErrorColor)),
                color = ErrorColor,
                modifier = Modifier.testTag("hub_orphaned_remove").clickable(onClick = onRemove),
            )
            Text(
                text = "Keep space",
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(HomeAppTextColorFaint)),
                color = HomeAppTextColorFaint,
                modifier = Modifier.testTag("hub_orphaned_keep_space").clickable(onClick = onKeepSpace),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 220, heightDp = 176)
@Preview(name = "Dark", showBackground = true, widthDp = 220, heightDp = 176, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OrphanedWidgetTilePreview() {
    FacetLauncherTheme {
        OrphanedWidgetTile(providerLabel = "Ledger", onRemove = {}, onKeepSpace = {})
    }
}
