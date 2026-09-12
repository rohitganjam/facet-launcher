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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.HomeAppTextColor
import com.facetlauncher.app.ui.theme.HomeAppTextColorFaint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/**
 * Shared title + live-count-subtitle header, used by every canvas surface (Home/Hub/carousel —
 * reached by a gesture, not a submenu navigation) that needs its own bold identifying title: the
 * Hub ("Facet Hub") and the Switch Profiles carousel ("Switch Profiles") both use this exact
 * shape — title, a `labelSmall` count subtitle beneath it, and an optional trailing action —
 * rather than each hand-building its own copy (see chat history).
 *
 * [onWallpaper] governs legibility treatment: `true` (the Hub) renders directly on the raw
 * transparent wallpaper/Home layer with no backing of its own, so both lines get
 * [homeAppLabelShadow] and [HomeAppTextColor]/[HomeAppTextColorFaint]; `false` (the carousel,
 * the default) is for a screen that already sits on its own opaque-ish scrim or `Surface`, which
 * reads fine with this app's plain [Ink]/[Muted] text — matching every other text element already
 * on that kind of screen.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onWallpaper: Boolean = false,
    trailingAction: @Composable () -> Unit = {},
) {
    val titleColor = if (onWallpaper) HomeAppTextColor else Ink
    val subtitleColor = if (onWallpaper) HomeAppTextColorFaint else Muted
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
                    shadow = if (onWallpaper) homeAppLabelShadow(titleColor) else null,
                ),
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = if (onWallpaper) homeAppLabelShadow(subtitleColor) else null,
                ),
                color = subtitleColor,
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
            title = "Screen title",
            subtitle = "N things available",
            trailingAction = { TonalButton(text = "Action", onClick = {}) },
        )
    }
}
