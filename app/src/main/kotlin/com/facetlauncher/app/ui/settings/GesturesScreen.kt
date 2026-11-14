package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.East
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.West
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.SwipeAction
import com.facetlauncher.app.data.model.SwipeActions
import com.facetlauncher.app.data.model.SwipeDirection
import com.facetlauncher.app.ui.components.ThemedModalBottomSheet
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.StickyHeaderScrollColumn
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue

/** Settings → Gestures: every Home gesture in one place; each swipe is tapped to pick its action. */
@Composable
fun GesturesScreen(
    onBack: () -> Unit,
    onNavigateToAccessibilityAccessExplanation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GesturesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccess()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GesturesContent(
        uiState = uiState,
        onBack = onBack,
        onHomeOpensSearchChange = viewModel::setHomeOpensSearch,
        onSwipeActionChange = viewModel::setSwipeAction,
        onDoubleTapToLockChange = { checked ->
            viewModel.setDoubleTapToLock(checked)
            if (checked && !uiState.isAccessibilityGranted) onNavigateToAccessibilityAccessExplanation()
        },
        modifier = modifier,
    )
}

// onDoubleTapToLockChange is unused while the double-tap to lock switch is commented out.
@Suppress("UnusedParameter")
@Composable
private fun GesturesContent(
    uiState: GesturesUiState,
    onBack: () -> Unit,
    onHomeOpensSearchChange: (Boolean) -> Unit,
    onDoubleTapToLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSwipeActionChange: (SwipeDirection, SwipeAction) -> Unit = { _, _ -> },
) {
    var editing by rememberSaveable { mutableStateOf<SwipeDirection?>(null) }
    StickyHeaderScrollColumn(
        title = stringResource(R.string.settings_gestures_title),
        onBack = onBack,
        testTag = "gestures_screen",
        modifier = modifier,
    ) {
        SettingsSectionHeader(stringResource(R.string.gestures_section_swipes))
        SettingsCard(fullBleedRows = true) {
            SwipeDirection.entries.forEachIndexed { index, direction ->
                if (index > 0) CardDivider()
                ClickableRow(
                    title = stringResource(direction.titleRes),
                    subtitle = stringResource(uiState.swipeActions.actionFor(direction).labelRes),
                    onClick = { editing = direction },
                    testTag = "gesture_swipe_${direction.tagName}",
                    leadingIcon = iconBadge(direction.icon, SettingsSectionHue.HOME_APPS),
                )
            }
        }

        SettingsSectionHeader(stringResource(R.string.gestures_section_built_in))
        SettingsCard {
            GestureInfoRow(Icons.Outlined.TouchApp, R.string.gesture_long_press_title, R.string.gesture_long_press_subtitle, "gesture_long_press")
        }

        SettingsSectionHeader(stringResource(R.string.gestures_section_optional))
        SettingsCard {
            SettingsToggleRow(
                title = stringResource(R.string.gesture_home_opens_search_title),
                subtitle = stringResource(R.string.gesture_home_opens_search_subtitle),
                checked = uiState.homeOpensSearch,
                onCheckedChange = onHomeOpensSearchChange,
                testTag = "gesture_home_opens_search_toggle",
            )
            // Double-tap to lock is disabled until the Play AccessibilityService declaration is approved.
            // CardDivider()
            // SettingsToggleRow(
            //     title = stringResource(R.string.gesture_double_tap_lock_title),
            //     subtitle = stringResource(
            //         if (uiState.isAccessibilityGranted) R.string.gesture_double_tap_lock_subtitle else R.string.gesture_double_tap_lock_needs_access,
            //     ),
            //     checked = uiState.doubleTapToLock,
            //     onCheckedChange = onDoubleTapToLockChange,
            //     testTag = "gesture_double_tap_lock_toggle",
            // )
        }
    }

    // An action another swipe already has is confirmed first.
    var pendingDuplicate by remember { mutableStateOf<Pair<SwipeDirection, SwipeAction>?>(null) }
    editing?.let { direction ->
        SwipeActionSheet(
            direction = direction,
            actions = uiState.swipeActions,
            onPick = { action ->
                editing = null
                if (uiState.swipeActions.otherSwipesWith(action, direction).isEmpty()) {
                    onSwipeActionChange(direction, action)
                } else {
                    pendingDuplicate = direction to action
                }
            },
            onDismiss = { editing = null },
        )
    }
    pendingDuplicate?.let { (direction, action) ->
        val holder = uiState.swipeActions.otherSwipesWith(action, direction).firstOrNull()
        if (holder != null) {
            ConfirmDialog(
                title = stringResource(R.string.gesture_duplicate_title),
                message = stringResource(R.string.gesture_duplicate_message, stringResource(holder.titleRes), stringResource(action.labelRes)),
                confirmLabel = stringResource(R.string.gesture_duplicate_confirm),
                onConfirm = {
                    onSwipeActionChange(direction, action)
                    pendingDuplicate = null
                },
                onDismiss = { pendingDuplicate = null },
                destructive = false,
            )
        }
    }
}

/** The swipes other than [direction] that already trigger [action]. */
private fun SwipeActions.otherSwipesWith(action: SwipeAction, direction: SwipeDirection) = directionsOf(action) - direction

/** Picks what [direction] does. An action another swipe has says so; on the app drawer's only swipe, nothing else can be picked. */
@Composable
private fun SwipeActionSheet(direction: SwipeDirection, actions: SwipeActions, onPick: (SwipeAction) -> Unit, onDismiss: () -> Unit) {
    ThemedModalBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("swipe_action_sheet")) {
            Text(
                text = stringResource(direction.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
            )
            SwipeAction.entries.forEach { action ->
                val holder = actions.otherSwipesWith(action, direction).firstOrNull()
                val enabled = actions.canAssign(direction, action)
                SwipeActionOption(
                    label = stringResource(action.labelRes),
                    note = when {
                        !enabled -> stringResource(R.string.gesture_action_drawer_required)
                        holder != null -> stringResource(R.string.gesture_action_already_on, stringResource(holder.titleRes))
                        else -> null
                    },
                    selected = actions.actionFor(direction) == action,
                    enabled = enabled,
                    onClick = { onPick(action) },
                    testTag = "swipe_action_${action.name.lowercase()}",
                )
            }
        }
    }
}

@Composable
private fun SwipeActionOption(label: String, note: String?, selected: Boolean, enabled: Boolean, onClick: () -> Unit, testTag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = Accent, unselectedColor = Muted, disabledUnselectedColor = Muted),
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (note != null) Text(text = note, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

private const val DISABLED_ALPHA = 0.5f

private val SwipeDirection.titleRes: Int
    get() = when (this) {
        SwipeDirection.UP -> R.string.gesture_swipe_up_title
        SwipeDirection.DOWN -> R.string.gesture_swipe_down_title
        SwipeDirection.LEFT -> R.string.gesture_swipe_left_title
        SwipeDirection.RIGHT -> R.string.gesture_swipe_right_title
    }

private val SwipeDirection.icon: ImageVector
    get() = when (this) {
        SwipeDirection.UP -> Icons.Outlined.ArrowUpward
        SwipeDirection.DOWN -> Icons.Outlined.ArrowDownward
        SwipeDirection.LEFT -> Icons.Outlined.West
        SwipeDirection.RIGHT -> Icons.Outlined.East
    }

private val SwipeDirection.tagName: String get() = name.lowercase()

private val SwipeAction.labelRes: Int
    get() = when (this) {
        SwipeAction.APP_DRAWER -> R.string.swipe_action_app_drawer
        SwipeAction.HUB -> R.string.swipe_action_hub
        SwipeAction.SWITCH_FACETS -> R.string.swipe_action_switch_facets
        SwipeAction.NOTIFICATION_SHADE -> R.string.swipe_action_notification_shade
        SwipeAction.NONE -> R.string.swipe_action_none
    }

@Composable
private fun GestureInfoRow(icon: ImageVector, titleRes: Int, subtitleRes: Int, testTag: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp).testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBadge(icon, SettingsSectionHue.HOME_APPS)
        Column {
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = stringResource(subtitleRes), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GesturesScreenPreview() {
    FacetLauncherTheme {
        GesturesContent(
            uiState = GesturesUiState(homeOpensSearch = true, doubleTapToLock = false, isAccessibilityGranted = false),
            onBack = {},
            onHomeOpensSearchChange = {},
            onDoubleTapToLockChange = {},
        )
    }
}
