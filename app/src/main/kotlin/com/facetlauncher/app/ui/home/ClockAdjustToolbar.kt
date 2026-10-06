package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/**
 * The floating "Alignment" pill shown during [ClockAdjustMode.ADJUST], just below the height handle:
 * left / center / right for the clock (or hosted widget), applied immediately. It swallows taps on its
 * own surface, enabled or not — Home's root treats any tap that reaches it unconsumed as "empty space" and
 * exits adjust mode, which must not happen from tapping the heading, the padding, or the dimmed pill.
 */
@Composable
fun ClockAdjustToolbar(
    alignment: ClockAlignment,
    onAlignmentChange: (ClockAlignment) -> Unit,
    modifier: Modifier = Modifier,
    /** `false` greys out the three options (they stop reacting) — the pill still absorbs taps either way. */
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .testTag("clock_adjust_toolbar")
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(Surface, CircleShape)
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.clock_adjust_alignment_heading),
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        Box(modifier = Modifier.padding(horizontal = 12.dp).size(width = 1.dp, height = 24.dp).background(Hairline))
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AlignmentButton(ClockAlignment.LEFT, Icons.Filled.FormatAlignLeft, R.string.clock_adjust_align_left, "clock_align_left", alignment, enabled, onAlignmentChange)
            AlignmentButton(ClockAlignment.CENTER, Icons.Filled.FormatAlignCenter, R.string.clock_adjust_align_center, "clock_align_center", alignment, enabled, onAlignmentChange)
            AlignmentButton(ClockAlignment.RIGHT, Icons.Filled.FormatAlignRight, R.string.clock_adjust_align_right, "clock_align_right", alignment, enabled, onAlignmentChange)
        }
    }
}

@Composable
private fun AlignmentButton(
    target: ClockAlignment,
    icon: ImageVector,
    labelRes: Int,
    testTag: String,
    current: ClockAlignment,
    enabled: Boolean,
    onAlignmentChange: (ClockAlignment) -> Unit,
) {
    val selected = current == target
    val label = stringResource(labelRes)
    // 48dp touch target (clipped, then selectable, so the ripple follows the shape) around a 40dp visible chip.
    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag(testTag)
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = { onAlignmentChange(target) })
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(if (selected) Accent else Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (selected) InkInverted else Muted, modifier = Modifier.size(22.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockAdjustToolbarPreview() {
    FacetLauncherTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            ClockAdjustToolbar(alignment = ClockAlignment.CENTER, onAlignmentChange = {})
        }
    }
}
