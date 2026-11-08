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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.StickyHeaderScrollColumn
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue

/** Settings → Gestures: every Home gesture in one place, with a switch for each optional one. */
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
        onSwipeLeftEnabledChange = viewModel::setSwipeLeftEnabled,
        onSwipeRightEnabledChange = viewModel::setSwipeRightEnabled,
        onSwipeDownEnabledChange = viewModel::setSwipeDownEnabled,
        onSwapSwipeSidesChange = viewModel::setSwapSwipeSides,
        onDoubleTapToLockChange = { checked ->
            viewModel.setDoubleTapToLock(checked)
            if (checked && !uiState.isAccessibilityGranted) onNavigateToAccessibilityAccessExplanation()
        },
        modifier = modifier,
    )
}

@Composable
private fun SwipeSwitchesCard(
    uiState: GesturesUiState,
    onLeftChange: (Boolean) -> Unit,
    onRightChange: (Boolean) -> Unit,
    onDownChange: (Boolean) -> Unit,
    onSwapChange: (Boolean) -> Unit,
) {
    val swapped = uiState.swapSwipeSides
    SettingsCard {
        SettingsToggleRow(
            title = stringResource(R.string.gesture_swipe_left_title),
            subtitle = stringResource(if (swapped) R.string.gesture_swipe_right_subtitle else R.string.gesture_swipe_left_subtitle),
            checked = uiState.swipeLeftEnabled,
            onCheckedChange = onLeftChange,
            testTag = "gesture_swipe_left_toggle",
        )
        CardDivider()
        SettingsToggleRow(
            title = stringResource(R.string.gesture_swipe_right_title),
            subtitle = stringResource(if (swapped) R.string.gesture_swipe_left_subtitle else R.string.gesture_swipe_right_subtitle),
            checked = uiState.swipeRightEnabled,
            onCheckedChange = onRightChange,
            testTag = "gesture_swipe_right_toggle",
        )
        CardDivider()
        SettingsToggleRow(
            title = stringResource(R.string.gesture_swipe_down_title),
            subtitle = stringResource(R.string.gesture_swipe_down_subtitle),
            checked = uiState.swipeDownEnabled,
            onCheckedChange = onDownChange,
            testTag = "gesture_swipe_down_toggle",
        )
        CardDivider()
        SettingsToggleRow(
            title = stringResource(R.string.gesture_swap_sides_title),
            subtitle = stringResource(R.string.gesture_swap_sides_subtitle),
            checked = swapped,
            onCheckedChange = onSwapChange,
            testTag = "gesture_swap_sides_toggle",
        )
    }
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
    onSwipeLeftEnabledChange: (Boolean) -> Unit = {},
    onSwipeRightEnabledChange: (Boolean) -> Unit = {},
    onSwipeDownEnabledChange: (Boolean) -> Unit = {},
    onSwapSwipeSidesChange: (Boolean) -> Unit = {},
) {
    StickyHeaderScrollColumn(
        title = stringResource(R.string.settings_gestures_title),
        onBack = onBack,
        testTag = "gestures_screen",
        modifier = modifier,
    ) {
        SettingsSectionHeader(stringResource(R.string.gestures_section_built_in))
        SettingsCard {
            GestureInfoRow(Icons.Outlined.ArrowUpward, R.string.gesture_swipe_up_title, R.string.gesture_swipe_up_subtitle, "gesture_swipe_up")
            CardDivider()
            GestureInfoRow(Icons.Outlined.East, R.string.gesture_swipe_right_title, if (uiState.swapSwipeSides) R.string.gesture_swipe_left_subtitle else R.string.gesture_swipe_right_subtitle, "gesture_swipe_right")
            CardDivider()
            GestureInfoRow(Icons.Outlined.West, R.string.gesture_swipe_left_title, if (uiState.swapSwipeSides) R.string.gesture_swipe_right_subtitle else R.string.gesture_swipe_left_subtitle, "gesture_swipe_left")
            CardDivider()
            GestureInfoRow(Icons.Outlined.ArrowDownward, R.string.gesture_swipe_down_title, R.string.gesture_swipe_down_subtitle, "gesture_swipe_down")
            CardDivider()
            GestureInfoRow(Icons.Outlined.TouchApp, R.string.gesture_long_press_title, R.string.gesture_long_press_subtitle, "gesture_long_press")
        }

        SettingsSectionHeader(stringResource(R.string.gestures_section_swipes))
        SwipeSwitchesCard(
            uiState = uiState,
            onLeftChange = onSwipeLeftEnabledChange,
            onRightChange = onSwipeRightEnabledChange,
            onDownChange = onSwipeDownEnabledChange,
            onSwapChange = onSwapSwipeSidesChange,
        )

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
