package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.facetlauncher.app.ui.theme.LocalIsDarkTheme
import com.facetlauncher.app.ui.theme.LocalLightStatusBarIcons
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface

/**
 * The one bottom sheet every Home action menu uses ([AppContextMenu], [FolderTileContextMenu], the
 * clock adjust menu) — a themed M3 [ModalBottomSheet] (see `CLAUDE.md`'s "theme every Material3
 * component explicitly"), so its surface runs behind the system bars and swipe-down and Back
 * dismiss it the same way everywhere. It's showing for exactly as long as it's composed; callers
 * gate it on their own `expanded`/mode state. Composing it blocks Home's swipes and makes Home
 * close it ([BlockHomeSwipesWhileShown], [DismissOnHomePress]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    /** `true` opens straight to the content's full height instead of clamping at half the screen — for menus short enough to always show whole. */
    skipPartiallyExpanded: Boolean = false,
    /** The sheet surface; [Surface] normally, the dimmer [com.facetlauncher.app.ui.theme.SurfaceContainer] for a sheet of [SettingsCard]s. */
    containerColor: Color = Surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    DismissOnHomePress(onDismiss = onDismissRequest)
    BlockHomeSwipesWhileShown()
    // The sheet is its own window: its surface sits under the nav bar, so nav icons follow the theme;
    // the status bar keeps whatever Home is showing (null outside LauncherActivity = inherit).
    val statusIconsLight = LocalLightStatusBarIcons.current
    val isDarkTheme = LocalIsDarkTheme.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = if (statusIconsLight != null) {
            ModalBottomSheetProperties(
                isAppearanceLightStatusBars = !statusIconsLight,
                isAppearanceLightNavigationBars = !isDarkTheme,
            )
        } else {
            ModalBottomSheetDefaults.properties
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        shape = SHEET_SHAPE,
        containerColor = containerColor,
        scrimColor = Scrim,
        dragHandle = { AppContextMenuDragHandle() },
        content = content,
    )
}
