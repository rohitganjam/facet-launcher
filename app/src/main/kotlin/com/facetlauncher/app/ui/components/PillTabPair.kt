package com.facetlauncher.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

private const val PILL_TAB_TRANSITION_MS = 220

/**
 * Two tabs in a pill track with an accent indicator that slides between them — the Apps/Folders switch
 * in the pickers, as a reusable component. Full shape (M3's segmented-control shape). Exactly two
 * options, so callers pass a boolean instead of a list.
 */
@Composable
fun PillTabPair(
    startLabel: String,
    endLabel: String,
    endSelected: Boolean,
    onSelect: (endSelected: Boolean) -> Unit,
    startTag: String,
    endTag: String,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var rowSizePx by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(SurfaceContainer)
            .padding(4.dp)
            .onSizeChanged { rowSizePx = it },
    ) {
        if (rowSizePx.width > 0) {
            val tabWidth = with(density) { (rowSizePx.width / 2).toDp() }
            val tabHeight = with(density) { rowSizePx.height.toDp() }
            val indicatorOffset by animateDpAsState(
                targetValue = if (endSelected) tabWidth else 0.dp,
                animationSpec = tween(PILL_TAB_TRANSITION_MS),
                label = "pill_tab_indicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(width = tabWidth, height = tabHeight)
                    .clip(CircleShape)
                    .background(Accent),
            )
        }
        Row(modifier = Modifier.selectableGroup()) {
            PillTab(startLabel, selected = !endSelected, tag = startTag, onClick = { onSelect(false) }, modifier = Modifier.weight(1f))
            PillTab(endLabel, selected = endSelected, tag = endTag, onClick = { onSelect(true) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PillTab(label: String, selected: Boolean, tag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .testTag(tag)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (selected) Surface else Ink)
    }
}
