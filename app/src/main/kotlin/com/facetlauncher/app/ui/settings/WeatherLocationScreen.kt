package com.facetlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.MAX_SAVED_WEATHER_CITIES
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.isSamePlaceAs
import com.facetlauncher.app.domain.PlaceCandidate
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Where the weather is for: "Use current location" (the phone's approximate position, read when Home opens; needs the
 * location permission) or "Choose a city" (cities kept in a list to switch between, found with a search over the device's
 * geocoder and the bundled city list, no permission). Launcher-wide, so it has no facet scope. Leaving while "Choose a city"
 * has no city ticked asks what to do instead.
 */
@Composable
fun WeatherLocationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeatherLocationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val requestLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.refreshPermission() }
    val useCurrentLocation = {
        viewModel.useCurrentLocation()
        if (!uiState.permissionGranted) requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    var askingToLeave by remember { mutableStateOf(false) }
    val needsCity = uiState.mode == WeatherLocationMode.CITY && uiState.city == null
    BackHandler(enabled = needsCity) { askingToLeave = true }
    WeatherLocationContent(
        uiState = uiState,
        onBack = { if (needsCity) askingToLeave = true else onBack() },
        onUseCurrentLocation = useCurrentLocation,
        onUseCityMode = viewModel::useCityMode,
        onAllowLocation = { requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onQueryChange = viewModel::onQueryChange,
        onChooseCity = viewModel::chooseCity,
        onSelectSavedCity = viewModel::selectSavedCity,
        onRemoveCity = viewModel::removeCity,
        modifier = modifier,
    )
    if (askingToLeave) {
        NoCityDialog(
            onTurnOffWeather = { viewModel.turnWeatherOff(onBack) },
            onUseCurrentLocation = {
                askingToLeave = false
                useCurrentLocation()
                if (uiState.permissionGranted) onBack()
            },
            onChooseCity = { askingToLeave = false },
        )
    }
}

@Composable
internal fun WeatherLocationContent(
    uiState: WeatherLocationUiState,
    onBack: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onUseCityMode: () -> Unit,
    onAllowLocation: () -> Unit,
    onQueryChange: (String) -> Unit,
    onChooseCity: (PlaceCandidate) -> Unit,
    onSelectSavedCity: (WeatherCity) -> Unit,
    onRemoveCity: (WeatherCity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("weather_location_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = stringResource(R.string.weather_location_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        SettingsCard(fullBleedRows = true) {
            ModeRow(
                title = stringResource(R.string.weather_location_current),
                hint = stringResource(R.string.weather_location_current_hint),
                selected = uiState.mode == WeatherLocationMode.CURRENT,
                onClick = onUseCurrentLocation,
                testTag = "weather_mode_current",
            )
            CardDivider()
            ModeRow(
                title = stringResource(R.string.weather_location_city),
                hint = stringResource(R.string.weather_location_city_hint),
                selected = uiState.mode == WeatherLocationMode.CITY,
                onClick = onUseCityMode,
                testTag = "weather_mode_city",
            )
        }
        if (uiState.mode == WeatherLocationMode.CURRENT) {
            SettingsCard(fullBleedRows = true) {
                PermissionRow(uiState.permissionGranted, onAllowLocation)
                uiState.showingFor?.let { name ->
                    CardDivider()
                    ValueRow(stringResource(R.string.weather_location_showing_for), name)
                }
            }
        } else {
            SavedCities(uiState, onSelectSavedCity, onRemoveCity)
            CitySearch(uiState, onQueryChange, onChooseCity)
        }
    }
}

/** The cities kept for switching, one ticked; or the prompt to add the first. */
@Composable
private fun SavedCities(uiState: WeatherLocationUiState, onSelect: (WeatherCity) -> Unit, onRemove: (WeatherCity) -> Unit) {
    SettingsSectionHeader(stringResource(R.string.weather_location_saved_title))
    if (uiState.savedCities.isEmpty()) {
        Text(stringResource(R.string.weather_location_pick_prompt), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.testTag("weather_city_prompt"))
        return
    }
    SettingsCard(fullBleedRows = true) {
        uiState.savedCities.forEachIndexed { index, city ->
            if (index > 0) CardDivider()
            SavedCityRow(city, selected = uiState.city?.isSamePlaceAs(city) == true, onSelect = { onSelect(city) }, onRemove = { onRemove(city) })
        }
    }
    if (uiState.city == null) {
        Text(stringResource(R.string.weather_location_tick_prompt), style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.testTag("weather_city_prompt"))
    }
}

@Composable
private fun SavedCityRow(city: WeatherCity, selected: Boolean, onSelect: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_saved_city")
            .clickable(onClick = onSelect)
            .padding(start = LocalSettingsRowInset.current, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(checked = selected, onCheckedChange = null, colors = CityCheckboxColors)
        Text(city.name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f).padding(vertical = 14.dp))
        IconButton(onClick = onRemove, modifier = Modifier.testTag("weather_saved_city_remove")) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.weather_location_remove, city.name), tint = Muted)
        }
    }
}

/** Asked when leaving "Choose a city" with no city ticked. */
@Composable
internal fun NoCityDialog(onTurnOffWeather: () -> Unit, onUseCurrentLocation: () -> Unit, onChooseCity: () -> Unit) {
    AlertDialog(
        onDismissRequest = onChooseCity,
        modifier = Modifier.testTag("weather_no_city_dialog"),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = Surface,
        title = { Text(stringResource(R.string.weather_leave_title), style = MaterialTheme.typography.titleMedium, color = Ink) },
        text = { Text(stringResource(R.string.weather_leave_message), style = MaterialTheme.typography.bodyMedium, color = Muted) },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                TextActionButton(stringResource(R.string.weather_leave_choose_city), onChooseCity, modifier = Modifier.testTag("weather_leave_choose_city"))
                TextActionButton(stringResource(R.string.weather_leave_use_current), onUseCurrentLocation, modifier = Modifier.testTag("weather_leave_use_current"))
                TextActionButton(stringResource(R.string.weather_leave_turn_off), onTurnOffWeather, modifier = Modifier.testTag("weather_leave_turn_off"))
            }
        },
    )
}

private val CityCheckboxColors
    @Composable get() = CheckboxDefaults.colors(checkedColor = Accent, uncheckedColor = Muted, checkmarkColor = Surface)

@Composable
private fun ModeRow(title: String, hint: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Accent, unselectedColor = Muted))
    }
}

@Composable
private fun ValueRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = LocalSettingsRowInset.current, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

/** The search field and what it found; picking a result adds that city to the list and ticks it. */
@Composable
private fun CitySearch(uiState: WeatherLocationUiState, onQueryChange: (String) -> Unit, onChooseCity: (PlaceCandidate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSectionHeader(stringResource(R.string.weather_location_search))
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("weather_city_search"),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.weather_location_search_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = MaterialTheme.shapes.extraSmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Hairline,
                focusedPlaceholderColor = Muted,
                unfocusedPlaceholderColor = Muted,
                cursorColor = Accent,
            ),
        )
        if (uiState.cityLimitHit) {
            Text(stringResource(R.string.weather_location_limit, MAX_SAVED_WEATHER_CITIES), style = MaterialTheme.typography.bodyMedium, color = ErrorColor, modifier = Modifier.testTag("weather_city_limit"))
        }
        when {
            uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.padding(8.dp), color = Accent, strokeWidth = 2.dp)
            uiState.results.isNotEmpty() -> SettingsCard(fullBleedRows = true) {
                uiState.results.forEachIndexed { index, place ->
                    if (index > 0) CardDivider()
                    val saved = place.toWeatherCity().let { city -> uiState.savedCities.any { it.isSamePlaceAs(city) } }
                    ResultRow(place, saved = saved, onClick = { onChooseCity(place) })
                }
            }
            uiState.query.isNotBlank() -> Text(stringResource(R.string.weather_location_no_results), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
private fun ResultRow(place: PlaceCandidate, saved: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_city_result")
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(place.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
            val detail = listOfNotNull(place.admin, place.countryCode).joinToString(", ")
            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        if (saved) Checkbox(checked = true, onCheckedChange = null, colors = CityCheckboxColors)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherLocationScreenPreview() {
    FacetLauncherTheme {
        WeatherLocationContent(
            uiState = WeatherLocationUiState(
                mode = WeatherLocationMode.CITY,
                city = WeatherCity("Pune", 18.52, 73.85, "Asia/Kolkata"),
                savedCities = listOf(WeatherCity("Pune", 18.52, 73.85, "Asia/Kolkata"), WeatherCity("London", 51.51, -0.13, "Europe/London")),
                permissionGranted = true,
                query = "pun",
                results = listOf(PlaceCandidate("Pune", "Maharashtra", "IN", 18.52, 73.85, "Asia/Kolkata"), PlaceCandidate("Punjab", null, "IN", 30.9, 75.8, "Asia/Kolkata")),
            ),
            onBack = {},
            onUseCurrentLocation = {},
            onUseCityMode = {},
            onAllowLocation = {},
            onQueryChange = {},
            onChooseCity = {},
            onSelectSavedCity = {},
            onRemoveCity = {},
        )
    }
}
