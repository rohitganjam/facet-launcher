package com.facetlauncher.app.ui.nativewidget

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.domain.tierFor
import com.facetlauncher.app.ui.theme.Surface

/** The inset around a widget's content, matching the 4dp padding hosted widgets get. */
private val CONTENT_INSET = 8.dp

/** Opacity of the widget's backing, the drawer's default so it reads over a busy wallpaper like a hosted tile. */
private const val SURFACE_ALPHA = 0.6f

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
) {
    val tier = type.tierFor(size.width.value.toInt(), size.height.value.toInt())
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.large)
            .background(Surface.copy(alpha = SURFACE_ALPHA))
            .testTag("native_widget_${type.name}"),
    ) {
        Crossfade(targetState = tier, modifier = Modifier.fillMaxSize().padding(CONTENT_INSET), label = "nativeWidgetTier") { shown ->
            Box(modifier = Modifier.fillMaxSize().testTag("native_widget_tier_${shown.name}")) {
                NativeWidgetContent(type, shown, size, state, actions)
            }
        }
    }
}

@Composable
private fun NativeWidgetContent(type: NativeWidgetType, tier: WidgetTier, size: DpSize, state: NativeWidgetState, actions: NativeWidgetActions) {
    val inner = DpSize(size.width - CONTENT_INSET * 2, size.height - CONTENT_INSET * 2)
    when (type) {
        NativeWidgetType.WEATHER -> WeatherNativeWidget(tier, inner, state.weather, actions.onOpenWeather, state.access.locationMissing, actions.onRequestLocationAccess)
        NativeWidgetType.AGENDA -> AgendaNativeWidget(tier, state.calendar, actions.onOpenEvent, state.access.calendarMissing, actions.onRequestCalendarAccess)
        NativeWidgetType.NOW_PLAYING -> NowPlayingNativeWidget(tier, inner, state.media, actions.media)
    }
}
