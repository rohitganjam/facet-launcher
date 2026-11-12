package com.facetlauncher.app.ui.nativewidget

import com.facetlauncher.app.ui.theme.LocalIsDarkTheme
import com.facetlauncher.app.data.model.WidgetTextTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.domain.isConfigurable
import com.facetlauncher.app.domain.tierFor
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.components.glassBackground

/** The inset around a widget's content, matching the 4dp padding hosted widgets get. */
private val CONTENT_INSET = 8.dp

/** Whether a native widget has settings to open: every one has at least its background. */
val NativeWidgetType.hasSettings: Boolean get() = true

@StringRes
fun NativeWidgetType.labelRes(): Int = when (this) {
    NativeWidgetType.WEATHER -> R.string.native_widget_weather
    NativeWidgetType.AGENDA -> R.string.native_widget_agenda
    NativeWidgetType.NOW_PLAYING -> R.string.native_widget_now_playing
}

/**
 * A widget Facet draws itself, filling [size]. The Hub tile and the Home page both call this with the box they
 * have, and the layout follows from the box alone ([tierFor]), so a size gives the same widget on either.
 */
@Composable
fun NativeWidget(
    type: NativeWidgetType,
    size: DpSize,
    state: NativeWidgetState,
    actions: NativeWidgetActions,
    modifier: Modifier = Modifier,
    /** Which placed widget this is, so it reads its own settings and its taps say which one. */
    ref: NativeWidgetRef? = null,
) {
    val tier = type.tierFor(size.width.value.toInt(), size.height.value.toInt())
    val shownState = state.withOverride(ref?.let { state.overrides[it] })
    val openWeather = { actions.onOpenWeather(ref) }
    val glass = ref?.let { state.overrides[it]?.glass } ?: NativeWidgetConfig.DEFAULT_GLASS
    // The text colours follow the theme's light or dark; a widget can force either without touching the panel behind it.
    val themeIsDark = LocalIsDarkTheme.current
    val isDarkTheme = when (ref?.let { state.overrides[it]?.textTheme }) {
        WidgetTextTheme.LIGHT_TEXT -> true
        WidgetTextTheme.DARK_TEXT -> false
        // The inverted fill is the theme's text colour, so on it the text flips to stay readable.
        else -> if (glass == GlassStyle.THEME_INVERTED) !themeIsDark else themeIsDark
    }
    Box(
        modifier = modifier
            .size(size)
            .glassBackground(glass, MaterialTheme.shapes.large, sampleWallpaper = true)
            .testTag("native_widget_${type.name}"),
    ) {
        CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
            Crossfade(targetState = tier, modifier = Modifier.fillMaxSize().padding(CONTENT_INSET), label = "nativeWidgetTier") { shown ->
                Box(modifier = Modifier.fillMaxSize().testTag("native_widget_tier_${shown.name}")) {
                    NativeWidgetContent(type, shown, size, shownState, actions, openWeather)
                }
            }
        }
    }
}

@Composable
private fun NativeWidgetContent(type: NativeWidgetType, tier: WidgetTier, size: DpSize, state: NativeWidgetState, actions: NativeWidgetActions, onOpenWeather: () -> Unit) {
    val inner = DpSize(size.width - CONTENT_INSET * 2, size.height - CONTENT_INSET * 2)
    when (type) {
        NativeWidgetType.WEATHER -> WeatherNativeWidget(tier, inner, state.weather, onOpenWeather, state.access.locationMissing, actions.onRequestLocationAccess)
        NativeWidgetType.AGENDA -> AgendaNativeWidget(tier, state.calendar, actions.onOpenEvent, state.access.calendarMissing, actions.onRequestCalendarAccess)
        NativeWidgetType.NOW_PLAYING -> NowPlayingNativeWidget(tier, inner, state.media, actions.media)
    }
}
