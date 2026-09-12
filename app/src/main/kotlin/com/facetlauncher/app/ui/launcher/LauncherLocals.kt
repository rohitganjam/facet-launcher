package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.SharedFlow

/**
 * [LauncherViewModel.homePressedEvent], threaded down from [com.facetlauncher.app.LauncherActivity]'s
 * top-level `CompositionLocalProvider` so any transient UI state that should close when the user
 * returns Home — not just [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]'s own Drawer/Hub/Facet
 * axes, but e.g. [com.facetlauncher.app.ui.components.AppContextMenu]'s bottom sheet — can subscribe
 * directly without every intermediate composable threading it through as a parameter. Same shape as
 * `ui/theme/ThemeLocals.kt`'s own locals. `null` wherever [com.facetlauncher.app.LauncherActivity]
 * hasn't provided it (`@Preview`s, unit tests), where nothing should close rather than crash.
 */
val LocalHomePressedEvent = staticCompositionLocalOf<SharedFlow<Unit>?> { null }
