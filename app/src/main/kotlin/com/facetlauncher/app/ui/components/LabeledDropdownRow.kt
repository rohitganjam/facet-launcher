package com.facetlauncher.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * A settings row whose value is chosen from a fixed list via a themed dropdown — title, then a
 * label + chevron "options area" that alone is tappable to open a [ThemedDropdownMenu]. Generic
 * over [T] so one composable serves every enum-backed selector (dock display mode, drawer
 * presentation/grid size, a facet's app-list-content mode) instead of a bespoke pill row.
 *
 * The menu is anchored to the options-area [Box] itself — not the whole row, and not nested
 * inside the clickable modifier's own subtree, which breaks click dispatch on the menu's items.
 * Material3's [androidx.compose.material3.DropdownMenu] only right-aligns with a narrow anchor
 * like this one when left-aligning it would overflow the window, which made the popup drift
 * off-position depending on how wide the *currently selected* label happened to be (e.g.
 * "Favorites" vs "Most used"). So the menu's own rendered width is tracked and fed back in as an
 * explicit offset, right-aligning it with this row's trailing edge deterministically regardless
 * of either width.
 */
@Composable
fun <T> LabeledDropdownRow(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    optionEnabled: (T) -> Boolean = { true },
    /** Optional content after an option's label in the menu, such as a Pro pill. */
    optionTrailing: (@Composable (T) -> Unit)? = null,
    testTag: String? = null,
    /** Optional content before the title, such as a radio button; [titleModifier] makes that title area clickable or selectable. */
    leading: (@Composable () -> Unit)? = null,
    titleModifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    var menuWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp).alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = titleModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
        Box(modifier = Modifier.onGloballyPositioned { anchorWidthPx = it.size.width }) {
            Row(
                modifier = Modifier
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                    .clickable(enabled = enabled) { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // Ink (primary text), not Muted — this is the row's own answer, not a
                // de-emphasized label; at Muted's alpha it read as disabled (see chat history).
                Text(text = label(selected), style = MaterialTheme.typography.bodyMedium, color = Ink)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Muted)
            }
            ThemedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = with(density) { DpOffset(x = (anchorWidthPx - menuWidthPx).toDp(), y = 0.dp) },
                modifier = Modifier.onSizeChanged { menuWidthPx = it.width },
            ) {
                options.forEach { option ->
                    ThemedDropdownMenuItem(
                        label = label(option),
                        enabled = optionEnabled(option),
                        trailingIcon = optionTrailing?.let { trailing -> { trailing(option) } },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                        modifier = if (testTag != null) Modifier.testTag("${testTag}_option_$option") else Modifier,
                    )
                }
            }
        }
    }
}
