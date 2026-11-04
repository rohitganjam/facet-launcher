package com.facetlauncher.app.ui.nativewidget

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.R
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.data.model.NativeWidgetRef
import com.facetlauncher.app.domain.NativeWidgetOverride
import com.facetlauncher.app.domain.NativeWidgetOverrides
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** What the widgets Facet draws itself are missing to show anything: the permission they read, when they need it. */
@Immutable
data class NativeWidgetAccess(
    val calendarMissing: Boolean = false,
    /** The location permission is off and the weather is for the current position. */
    val locationMissing: Boolean = false,
)

/** Whether the calendar and location permissions are on, re-read on [refresh] because neither has a grant-change callback. */
@HiltViewModel
class NativeWidgetAccessViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val locationRepository: LocationRepository,
    observeOverrides: NativeWidgetOverrides,
) : ViewModel() {

    private val recheck = MutableStateFlow(0)

    val access: StateFlow<NativeWidgetAccess> = combine(settingsRepository.settings, recheck) { settings, _ ->
        NativeWidgetAccess(
            calendarMissing = !calendarPermissionRepository.isGranted(),
            locationMissing = settings.weatherLocationMode == WeatherLocationMode.CURRENT && !locationRepository.isPermissionGranted(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NativeWidgetAccess())

    /** What each widget set to its own calendars or location shows, for Hub tiles and Home pages alike. */
    val overrides: StateFlow<ImmutableMap<NativeWidgetRef, NativeWidgetOverride>> = observeOverrides()
        .map { it.toImmutableMap() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentMapOf())

    fun refresh() {
        recheck.value += 1
    }
}

/** The permission state and the two runtime requests a widget's "Allow" button runs. */
@Immutable
data class NativeWidgetAccessControls(
    val access: NativeWidgetAccess,
    val requestCalendar: () -> Unit,
    val requestLocation: () -> Unit,
    val overrides: ImmutableMap<NativeWidgetRef, NativeWidgetOverride> = persistentMapOf(),
)

@Composable
fun rememberNativeWidgetAccess(viewModel: NativeWidgetAccessViewModel = hiltViewModel()): NativeWidgetAccessControls {
    val access by viewModel.access.collectAsStateWithLifecycle()
    val overrides by viewModel.overrides.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val calendar = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.refresh() }
    val location = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.refresh() }
    return NativeWidgetAccessControls(
        access = access,
        requestCalendar = { calendar.launch(Manifest.permission.READ_CALENDAR) },
        requestLocation = { location.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        overrides = overrides,
    )
}

/** A widget's empty state when its permission is off: what is missing, and a button that asks for it. */
@Composable
internal fun AccessPrompt(message: String, onAllow: () -> Unit, tag: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag(tag), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        TextActionButton(text = stringResource(R.string.native_widget_allow), onClick = onAllow, modifier = Modifier.testTag("${tag}_allow"))
    }
}
