package com.facetlauncher.app.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/**
 * Shared title + live-count-subtitle header, used by every canvas surface (Hub/carousel — reached
 * by a gesture, not a submenu navigation) that needs its own bold identifying title: the Hub
 * ("Facet Hub") and the Switch Facets carousel ("Switch Facets") both use this exact shape —
 * title, a `labelSmall` count subtitle beneath it, and an optional trailing action.
 *
 * One look on both screens: [Ink] title, [Muted] subtitle, and the [homeAppLabelShadow] on both
 * lines, so the text stays legible whatever wallpaper shows through the screen's own scrim.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingAction: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = homeAppLabelShadow(Ink),
                ),
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(shadow = homeAppLabelShadow(Muted)),
                color = Muted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        trailingAction()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 120)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 120, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScreenHeaderPreview() {
    FacetLauncherTheme {
        ScreenHeader(
            title = stringResource(R.string.settings_header_title),
            subtitle = stringResource(R.string.settings_facets_subtitle),
            trailingAction = { TonalButton(text = stringResource(R.string.action_save), onClick = {}) },
        )
    }
}
