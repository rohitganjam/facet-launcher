package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Muted

/**
 * Trailing pill naming which scope a row applies to — "Global" in a neutral [Muted] tone for the
 * launcher-wide default, or the active facet's own real name in a tonal [Accent] wash when
 * [facetName] is non-null (decided explicitly by the user, to distinguish the facet-override case
 * from the plain default without folding it into the row's own label text — see chat history).
 * `small`/8dp (CLAUDE.md's M3 shape table — chips' own token) rather than a fully-rounded pill.
 * Shared by [QuickPlacementBadge] (the app/folder context menu's Favorites/Dock rows) and the
 * clock long-press menu's "Edit clock & calendar styles" row.
 */
@Composable
fun FacetScopeBadge(facetName: String?, modifier: Modifier = Modifier) {
    val color = if (facetName != null) Accent else Muted
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), MaterialTheme.shapes.small)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(text = facetName ?: "Global", style = MaterialTheme.typography.labelSmall, color = color)
    }
}
