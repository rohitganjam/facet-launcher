package com.facetlauncher.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * Sort control for the app picker screens (Favorites/Dock/Folder) — a themed dropdown for
 * [AppSortOption] plus a single arrow icon that flips 180° to show [SortDirection]. Both are
 * grouped together at the row's trailing (right) edge. Applies only to a picker's not-yet-selected
 * app list; the already-placed section is never affected (see
 * [com.facetlauncher.app.domain.SortAppsForPickerUseCase]). Anchor-width offset trick is the same
 * one [LabeledDropdownRow] uses to keep the popup right-aligned regardless of label width.
 */
@Composable
fun AppSortControl(
    sortOption: AppSortOption,
    sortDirection: SortDirection,
    onSortOptionChanged: (AppSortOption) -> Unit,
    onSortDirectionToggled: () -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    var menuWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (sortDirection == SortDirection.DESCENDING) 180f else 0f,
        label = "app_sort_direction_rotation",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.onGloballyPositioned { anchorWidthPx = it.size.width }) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    // Surface, not SurfaceContainer — this closed button sits directly on
                    // AppPickerScreen's own Surface-colored background and should blend into it
                    // rather than read as a raised chip; the open popup below keeps
                    // ThemedDropdownMenu's own default SurfaceContainer instead.
                    .background(Surface)
                    .clickable { expanded = true }
                    .testTag("${tagPrefix}_sort_menu_button")
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = stringResource(sortOption.labelRes), style = MaterialTheme.typography.bodyMedium, color = Ink)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Muted)
            }
            ThemedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = with(density) { DpOffset(x = (anchorWidthPx - menuWidthPx).toDp(), y = 0.dp) },
                modifier = Modifier.onSizeChanged { menuWidthPx = it.width },
            ) {
                AppSortOption.entries.forEach { option ->
                    ThemedDropdownMenuItem(
                        label = stringResource(option.labelRes),
                        onClick = {
                            expanded = false
                            onSortOptionChanged(option)
                        },
                        modifier = Modifier.testTag("${tagPrefix}_sort_option_${option.name}"),
                    )
                }
            }
        }
        IconButton(
            onClick = onSortDirectionToggled,
            modifier = Modifier.testTag("${tagPrefix}_sort_direction_toggle"),
        ) {
            Icon(
                // Material Symbols "list_arrow" (outlined, weight 400, rounded), vendored as
                // res/drawable/list_arrow_24.xml — not part of the classic material-icons-extended
                // set this project otherwise draws from (Google Fonts' icon-render API:
                // fonts.gstatic.com/render/v1/Material+Symbols+Outlined/24dp/list_arrow.kt).
                painter = painterResource(R.drawable.list_arrow_24),
                contentDescription = if (sortDirection == SortDirection.ASCENDING) "Sort ascending" else "Sort descending",
                tint = Ink,
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}
