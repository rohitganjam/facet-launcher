package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Muted

/**
 * A small tonal pill naming a scope/tag a row applies to — `small`/8dp (CLAUDE.md's M3 shape
 * table — chips' own token) rather than a fully-rounded pill. [FacetScopeBadge] and
 * [WorkScopeBadge] both build on this exact construction rather than each rolling their own, so
 * every such pill in the app reads as one visual family.
 */
@Composable
private fun ScopeBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), MaterialTheme.shapes.small)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/**
 * Trailing pill naming which scope a row applies to — "Global" in a neutral [Muted] tone for the
 * launcher-wide default, or the active facet's own real name in a tonal [Accent] wash when
 * [facetName] is non-null (decided explicitly by the user, to distinguish the facet-override case
 * from the plain default without folding it into the row's own label text — see chat history).
 * Shared by [QuickPlacementBadge] (the app/folder context menu's Favorites/Dock rows) and the
 * clock long-press menu's "Edit clock & calendar styles" row.
 */
@Composable
fun FacetScopeBadge(facetName: String?, modifier: Modifier = Modifier) {
    val color = if (facetName != null) Accent else Muted
    ScopeBadge(label = facetName ?: stringResource(R.string.facet_scope_global), color = color, modifier = modifier)
}

/** Marks a row as belonging to a Work Profile app — [AppContextMenu]'s header, alongside the app's name. */
@Composable
fun WorkScopeBadge(modifier: Modifier = Modifier) {
    ScopeBadge(label = stringResource(R.string.facet_scope_work), color = Accent, modifier = modifier)
}

/**
 * Marks a row as belonging to an [AppProfile.OTHER][com.facetlauncher.app.data.model.AppProfile]
 * app — a profile that exists but can't be positively identified as a Work Profile or Private
 * Space (an OEM dual-app/clone profile, most commonly, or any non-primary profile at all below
 * API 35). Sibling to [WorkScopeBadge] — same shape, [Muted] tone since it's informational
 * ("there's a second copy of this") rather than a specific category name the way "Work" is.
 */
@Composable
fun OtherScopeBadge(modifier: Modifier = Modifier) {
    ScopeBadge(label = stringResource(R.string.facet_scope_other_profile), color = Muted, modifier = modifier)
}
