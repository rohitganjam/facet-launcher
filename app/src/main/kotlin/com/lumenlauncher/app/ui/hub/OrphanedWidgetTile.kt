package com.lumenlauncher.app.ui.hub

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
import com.lumenlauncher.app.ui.components.dashedBorder
import com.lumenlauncher.app.ui.theme.ErrorColor
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.HomeAppTextColorFaint
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.homeAppLabelShadow

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
        Column {
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
    LumenLauncherTheme {
        OrphanedWidgetTile(providerLabel = "Ledger", onRemove = {}, onKeepSpace = {})
    }
}
