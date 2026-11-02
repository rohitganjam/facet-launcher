package com.facetlauncher.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.facetlauncher.app.data.model.SystemBarIconStyle
import kotlinx.coroutines.delay

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

/** How long a swipe-down reveal keeps a hidden status bar on screen before it hides again. */
internal const val STATUS_BAR_REVEAL_MILLIS = 3_000L

/** Only the wallpaper screens (Home, Hub, Drawer) hide the status bar, and only until a swipe [revealed] it. */
internal fun isStatusBarHidden(hideSetting: Boolean, backdrop: SystemBarsBackdrop, revealed: Boolean): Boolean =
    hideSetting && backdrop == SystemBarsBackdrop.WALLPAPER && !revealed

/** The current screen's [SystemBarsBackdrop], written by whichever screen knows what's behind the bars. */
@Stable
class SystemBarsState {
    var backdrop by mutableStateOf(SystemBarsBackdrop.THEME_SURFACE)

    /** Mirrors the "hide status bar" setting; written by [ProvideSystemBars]. */
    var hideStatusBar by mutableStateOf(false)
        internal set

    /** `true` for [STATUS_BAR_REVEAL_MILLIS] after a swipe-down brought a hidden status bar back. */
    var statusBarRevealed by mutableStateOf(false)
        internal set

    /** Whether the system is already showing the bar, e.g. transiently after an edge swipe; written by [ProvideSystemBars]. */
    internal var systemStatusBarVisible: () -> Boolean = { false }

    /** Reveals the hidden status bar and returns `true`, or `false` when it is already showing (or never hidden), so the swipe should open the shade instead. */
    fun revealStatusBar(): Boolean {
        if (!isStatusBarHidden(hideStatusBar, backdrop, statusBarRevealed) || systemStatusBarVisible()) return false
        statusBarRevealed = true
        return true
    }
}

/** `true` while the "hide status bar" setting is on; Home, Hub and Drawer then keep their top padding fixed whether or not the bar shows. */
val LocalHideStatusBar = staticCompositionLocalOf { false }

/** `true` while the status bar is hidden right now, for windows of their own (sheets, dialogs) that must hide it too. */
val LocalStatusBarHidden = staticCompositionLocalOf { false }

/**
 * Insets for Home, Hub and Drawer. With the status bar hidden by setting, the top is only the display cutout, so
 * revealing the bar overlays the content instead of pushing it down.
 */
@Composable
fun launcherSystemBarInsets(): WindowInsets =
    if (LocalHideStatusBar.current) {
        WindowInsets.systemBars.exclude(WindowInsets.statusBars).union(WindowInsets.displayCutout.only(WindowInsetsSides.Top))
    } else {
        WindowInsets.systemBars
    }

/** For a sheet or dialog window: follows [LocalStatusBarHidden], which a separate window would otherwise reset by showing the bar. */
@Composable
fun MatchStatusBarVisibility() {
    val hidden = LocalStatusBarHidden.current
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window ?: return
    SideEffect {
        val controller = WindowCompat.getInsetsController(window, view)
        if (hidden) controller.hide(WindowInsetsCompat.Type.statusBars()) else controller.show(WindowInsetsCompat.Type.statusBars())
    }
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
    hideStatusBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    val lightIcons = useLightSystemBarIcons(style, state.backdrop, LocalIsDarkTheme.current)
    val lightNavIcons = useLightNavigationBarIcons(style, state.backdrop, LocalIsDarkTheme.current)
    val statusBarHidden = isStatusBarHidden(hideStatusBar, state.backdrop, state.statusBarRevealed)
    val view = LocalView.current
    // The system re-shows bars after an app switch or a dialog, so the hide is re-applied on resume and window focus.
    var reapply by remember { mutableIntStateOf(0) }
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !lightIcons
            controller.isAppearanceLightNavigationBars = !lightNavIcons
            state.hideStatusBar = hideStatusBar
            state.systemStatusBarVisible = {
                ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
            }
        }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { reapply++ }
        DisposableEffect(view) {
            val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus -> if (hasFocus) reapply++ }
            view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
            onDispose { view.viewTreeObserver.removeOnWindowFocusChangeListener(listener) }
        }
        LaunchedEffect(statusBarHidden, reapply) {
            val window = view.context.findActivity()?.window ?: return@LaunchedEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (statusBarHidden) controller.hide(WindowInsetsCompat.Type.statusBars()) else controller.show(WindowInsetsCompat.Type.statusBars())
        }
        LaunchedEffect(state.statusBarRevealed) {
            if (state.statusBarRevealed) {
                delay(STATUS_BAR_REVEAL_MILLIS)
                state.statusBarRevealed = false
            }
        }
    }
    CompositionLocalProvider(
        LocalSystemBarsState provides state,
        LocalLightStatusBarIcons provides lightIcons,
        LocalHideStatusBar provides hideStatusBar,
        LocalStatusBarHidden provides statusBarHidden,
        content = content,
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
