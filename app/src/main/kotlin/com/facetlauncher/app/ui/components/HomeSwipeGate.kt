package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Counts the sheets currently on screen; `HomeDrawerRoute` ignores Home's swipe gestures while it's non-zero. */
@Stable
class HomeSwipeGate {
    private var openSheets by mutableIntStateOf(0)

    val isBlocked: Boolean get() = openSheets > 0

    internal fun register() {
        openSheets++
    }

    internal fun unregister() {
        openSheets--
    }
}

/** Provided by `LauncherActivity`; null (a no-op) in previews and isolated tests, like `LocalHomePressedEvent`. */
val LocalHomeSwipeGate = staticCompositionLocalOf<HomeSwipeGate?> { null }

/** Call from inside a sheet's content — blocks Home swipes for as long as the sheet is composed. */
@Composable
fun BlockHomeSwipesWhileShown() {
    val gate = LocalHomeSwipeGate.current ?: return
    DisposableEffect(gate) {
        gate.register()
        onDispose { gate.unregister() }
    }
}
