package com.facetlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.facetlauncher.app.domain.HUB_MAX_WIDGETS
import com.facetlauncher.app.ui.components.ScreenHeader
import com.facetlauncher.app.ui.components.TonalButton
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

/**
 * "Facet Hub" — [ScreenHeader] with `onWallpaper = true`, since the Hub (like Home) sits directly
 * on the transparent wallpaper/Home layer, not an opaque `Surface` the way every Settings-style
 * screen does (see chat history).
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
    ScreenHeader(
        title = "Widgets",
        subtitle = "$widgetCount of $HUB_MAX_WIDGETS widgets",
        modifier = modifier,
        onWallpaper = true,
        trailingAction = {
            TonalButton(
                text = "Add",
                enabled = !isAtCapacity,
                onClick = onAddClick,
                modifier = Modifier.testTag("hub_add_button"),
            )
        },
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 120)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 120, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HubHeaderPreview() {
    FacetLauncherTheme {
        HubHeader(widgetCount = 6, columns = 5, isAtCapacity = false, onAddClick = {})
    }
}

@Preview(name = "At capacity", showBackground = true, widthDp = 390, heightDp = 120)
@Composable
private fun HubHeaderAtCapacityPreview() {
    FacetLauncherTheme {
        HubHeader(widgetCount = 20, columns = 5, isAtCapacity = true, onAddClick = {})
    }
}
