package com.facetlauncher.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.facetlauncher.app.data.model.SystemBarIconStyle

/** What sits behind the status and nav bars on the current screen — this decides which icon color stays legible. */
enum class SystemBarsBackdrop {
    /** An opaque launcher screen drawn in the theme's surface colors (Settings, pickers, onboarding). */
    THEME_SURFACE,

    /** Home, Hub, carousel and Drawer, which show the wallpaper through them. */
    WALLPAPER,

    /** A surface that's always dark regardless of theme (Private Space). */
    DARK_SURFACE,

    /** Dark behind the status bar but the theme's surface behind the navigation bar (the Facet Pro screen's hero over a pinned bar). */
    DARK_TOP,
}

/** `true` for light (white) bar icons. Only [SystemBarsBackdrop.WALLPAPER] honors [style]; the other backdrops have a known color. */
fun useLightSystemBarIcons(style: SystemBarIconStyle, backdrop: SystemBarsBackdrop, isDarkTheme: Boolean): Boolean =
    when (backdrop) {
        SystemBarsBackdrop.THEME_SURFACE -> isDarkTheme
        SystemBarsBackdrop.DARK_SURFACE, SystemBarsBackdrop.DARK_TOP -> true
        SystemBarsBackdrop.WALLPAPER -> when (style) {
            SystemBarIconStyle.MATCH_THEME -> isDarkTheme
            SystemBarIconStyle.LIGHT -> true
            SystemBarIconStyle.DARK -> false
        }
    }

/** Like [useLightSystemBarIcons], but for the navigation bar, which [SystemBarsBackdrop.DARK_TOP] leaves on the theme's surface. */
fun useLightNavigationBarIcons(style: SystemBarIconStyle, backdrop: SystemBarsBackdrop, isDarkTheme: Boolean): Boolean =
    if (backdrop == SystemBarsBackdrop.DARK_TOP) isDarkTheme else useLightSystemBarIcons(style, backdrop, isDarkTheme)

/** The current screen's [SystemBarsBackdrop], written by whichever screen knows what's behind the bars. */
@Stable
class SystemBarsState {
    var backdrop by mutableStateOf(SystemBarsBackdrop.THEME_SURFACE)
}

/** Provided by `LauncherActivity`; null (a no-op) in previews and isolated tests. */
val LocalSystemBarsState = staticCompositionLocalOf<SystemBarsState?> { null }

/** Whether the status bar currently shows light icons — read by sheet windows, which set their own bar appearance. Null outside `LauncherActivity`. */
val LocalLightStatusBarIcons = staticCompositionLocalOf<Boolean?> { null }

/**
 * Declares [backdrop] for as long as this is composed, falling back to [SystemBarsBackdrop.THEME_SURFACE]
 * when it leaves — so a screen only has to say what it is, not clean up after itself.
 */
@Composable
fun SystemBarsBackdropEffect(backdrop: SystemBarsBackdrop) {
    val state = LocalSystemBarsState.current ?: return
    DisposableEffect(state, backdrop) {
        state.backdrop = backdrop
        onDispose { state.backdrop = SystemBarsBackdrop.THEME_SURFACE }
    }
}

/**
 * Applies the resolved icon color to the activity window's status and nav bars and exposes the
 * status bar's value via [LocalLightStatusBarIcons]. Call inside [FacetLauncherTheme], which owns
 * [LocalIsDarkTheme]. Android's "light bars" flag means *dark* icons, hence the negation.
 */
@Composable
fun ProvideSystemBars(
    style: SystemBarIconStyle,
    state: SystemBarsState,
    content: @Composable () -> Unit,
) {
    val lightIcons = useLightSystemBarIcons(style, state.backdrop, LocalIsDarkTheme.current)
    val lightNavIcons = useLightNavigationBarIcons(style, state.backdrop, LocalIsDarkTheme.current)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !lightIcons
            controller.isAppearanceLightNavigationBars = !lightNavIcons
        }
    }
    CompositionLocalProvider(
        LocalSystemBarsState provides state,
        LocalLightStatusBarIcons provides lightIcons,
        content = content,
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
