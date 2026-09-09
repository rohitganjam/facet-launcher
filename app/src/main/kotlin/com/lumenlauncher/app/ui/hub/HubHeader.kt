package com.lumenlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.domain.HUB_MAX_WIDGETS
import com.lumenlauncher.app.ui.components.TonalButton
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.HomeAppTextColorFaint
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.homeAppLabelShadow

/**
 * "Lumen Hub" — bold, [HomeAppTextColor], legible via [homeAppLabelShadow] rather than a
 * background panel, since the Hub (like Home) sits directly on the transparent wallpaper/Home
 * layer, not an opaque `Surface` the way every Settings-style screen does (see chat history).
 *
 * Deliberately bolder than Settings/Permissions/Backup & restore's own plain (unbolded)
 * `headlineSmall` titles — those are menu/utility screens the user passes through, whereas the
 * Hub, like Home, *is* a page of the launcher itself (part of the canvas, reached by swiping, not
 * navigating into a submenu), and reads as one accordingly (see chat history — this was briefly
 * flattened to match the menu screens' own weight, then reverted once that distinction was made
 * explicit; don't re-flatten it for "consistency" without that context).
 *
 * Only rendered here (as part of the pinned header) once at least one widget exists — the empty
 * state's own Add button is centered in the middle of the screen instead.
 */
@Composable
fun HubHeader(widgetCount: Int, columns: Int, isAtCapacity: Boolean, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "Lumen Hub",
                // headlineSmall to match Settings' own header size (see chat history) — was
                // titleMedium, which read small next to every other screen's title.
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = homeAppLabelShadow(HomeAppTextColor),
                ),
                color = HomeAppTextColor,
            )
            Text(
                text = "$widgetCount of $HUB_MAX_WIDGETS widgets",
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(HomeAppTextColorFaint)),
                color = Muted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        TonalButton(
            text = "Add",
            enabled = !isAtCapacity,
            onClick = onAddClick,
            modifier = Modifier.testTag("hub_add_button"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 120)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 120, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HubHeaderPreview() {
    LumenLauncherTheme {
        HubHeader(widgetCount = 6, columns = 5, isAtCapacity = false, onAddClick = {})
    }
}

@Preview(name = "At capacity", showBackground = true, widthDp = 390, heightDp = 120)
@Composable
private fun HubHeaderAtCapacityPreview() {
    LumenLauncherTheme {
        HubHeader(widgetCount = 20, columns = 5, isAtCapacity = true, onAddClick = {})
    }
}
