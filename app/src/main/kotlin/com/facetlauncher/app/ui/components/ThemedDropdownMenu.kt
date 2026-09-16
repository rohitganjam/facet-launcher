package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * A [DropdownMenu] using [SurfaceContainer] rather than [com.facetlauncher.app.ui.theme.Surface] by
 * default — this popup floats as its own layer above whatever anchored it (often a
 * [com.facetlauncher.app.ui.components.SettingsCard], itself [com.facetlauncher.app.ui.theme.Surface]-colored),
 * so it reads as a separate plane rather than blending into the card underneath it (see chat
 * history — same page/card split as `SettingsCard`'s own). A caller anchored to a surface that's
 * already [com.facetlauncher.app.ui.theme.Surface]-colored (so [SurfaceContainer] would itself read
 * as the odd one out rather than SettingsCard's usual Surface-on-SurfaceContainer split) can pass
 * [containerColor] explicitly to blend in instead. Defaults to M3's own
 * Menu shape ([MaterialTheme.shapes.extraSmall], 4dp — see `CLAUDE.md`'s Material 3 shape section),
 * right for a compact anchored picker like a Settings dropdown; a caller presenting something
 * closer to a small floating card (e.g. [AppContextMenu]) passes a rounder [shape] from the same
 * M3 scale instead, rather than this component picking one shape for every kind of popup.
 */
@Composable
fun ThemedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = SurfaceContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = shape,
        containerColor = containerColor,
        content = content,
    )
}

/** A [DropdownMenuItem] styled with the app's own type/color tokens rather than Material3 defaults. */
@Composable
fun ThemedDropdownMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (!enabled) Faint else if (destructive) ErrorColor else Ink,
            )
        },
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        contentPadding = contentPadding,
    )
}
