package com.facetlauncher.app.ui.nativewidget

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.domain.isConfigurable
import com.facetlauncher.app.domain.tierFor
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/** The inset around a widget's content, matching the 4dp padding hosted widgets get. */
private val CONTENT_INSET = 8.dp

/** The 3-dot button's touch size, icon size and distance from the widget's corner. */
private val MENU_BUTTON_SIZE = 28.dp
private val MENU_ICON_SIZE = 18.dp
private val MENU_BUTTON_INSET = 4.dp

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
    /** Which placed widget this is, so it reads its own settings and its taps say which one: an Agenda at Medium or Large has a 3-dot button for them (Weather's is in its sheet). */
    ref: NativeWidgetRef? = null,
) {
    val tier = type.tierFor(size.width.value.toInt(), size.height.value.toInt())
    val shownState = state.withOverride(ref?.let { state.overrides[it] })
    val openWeather = { actions.onOpenWeather(ref) }
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.large)
            .background(Surface.copy(alpha = SURFACE_ALPHA))
            .testTag("native_widget_${type.name}"),
    ) {
        val configurable = ref != null && type == NativeWidgetType.AGENDA && type.isConfigurable(size.width.value.toInt(), size.height.value.toInt())
        Crossfade(targetState = tier, modifier = Modifier.fillMaxSize().padding(CONTENT_INSET), label = "nativeWidgetTier") { shown ->
            Box(modifier = Modifier.fillMaxSize().testTag("native_widget_tier_${shown.name}")) {
                NativeWidgetContent(type, shown, size, shownState, actions, openWeather)
            }
        }
        if (ref != null && configurable) {
            IconButton(onClick = { actions.onConfigure(ref) }, modifier = Modifier.align(Alignment.TopEnd).padding(MENU_BUTTON_INSET).size(MENU_BUTTON_SIZE).testTag("native_widget_menu")) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(R.string.hub_widget_configure), tint = Muted, modifier = Modifier.size(MENU_ICON_SIZE))
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
