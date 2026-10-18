package com.facetlauncher.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

private val BASE_ICON_SIZE = 24.dp
private val BADGE_SIZE = 16.dp

/** The tonal container: an accent wash with an accent outline when the app is already in the list. */
@Composable
internal fun Modifier.placementContainer(action: PlacementActionUi): Modifier {
    val shape = MaterialTheme.shapes.medium
    return if (action.isRemove) {
        this.clip(shape).background(Accent.copy(alpha = 0.14f), shape).border(BorderStroke(1.dp, Accent.copy(alpha = 0.4f)), shape)
    } else {
        this.clip(shape).background(SurfaceContainer, shape)
    }
}

@Composable
internal fun placementTextColor(action: PlacementActionUi): Color = if (action.isRemove) Accent else Ink

@Composable
internal fun placementDescription(action: PlacementActionUi): String {
    val scope = action.scope ?: return action.label
    return stringResource(R.string.placement_action_description, action.label, scope.facetName ?: stringResource(R.string.facet_scope_global))
}

/** The action's icon with its + or - badge. The icon grows with text up to [MAX_CHROME_ICON_SCALE]; the badge keeps its size. */
@Composable
internal fun PlacementIcon(action: PlacementActionUi) {
    Box(modifier = Modifier.testTag("${action.tag}_icon"), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = if (action.isRemove) Accent else Muted,
            modifier = Modifier.size(BASE_ICON_SIZE * rememberChromeIconScale()),
        )
        PlacementBadge(isRemove = action.isRemove, modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp))
    }
}

@Composable
internal fun PlacementBadge(isRemove: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(BADGE_SIZE).background(if (isRemove) Accent else Ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isRemove) Icons.Outlined.Remove else Icons.Outlined.Add,
            contentDescription = null,
            tint = Surface,
            modifier = Modifier.size(12.dp),
        )
    }
}
