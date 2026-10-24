package com.facetlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
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
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.domain.PlaceCandidate
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Where the weather is for: "Use current location" (the phone's approximate position, read when Home opens; needs the
 * location permission) or "Choose a city" (a search over the device's geocoder and the bundled city list, no permission).
 * Launcher-wide, so it has no facet scope.
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
    WeatherLocationContent(
        uiState = uiState,
        onBack = onBack,
        onUseCurrentLocation = {
            viewModel.useCurrentLocation()
            if (!uiState.permissionGranted) requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        onAllowLocation = { requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onQueryChange = viewModel::onQueryChange,
        onChooseCity = viewModel::chooseCity,
        modifier = modifier,
    )
}

@Composable
internal fun WeatherLocationContent(
    uiState: WeatherLocationUiState,
    onBack: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onAllowLocation: () -> Unit,
    onQueryChange: (String) -> Unit,
    onChooseCity: (PlaceCandidate) -> Unit,
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
                onClick = { if (uiState.city != null) onChooseCity(uiState.city.toCandidate()) },
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
        }
        SettingsSectionHeader(stringResource(R.string.weather_location_city))
        CitySearch(uiState, onQueryChange, onChooseCity)
    }
}

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

/** The search field and what it found; picking a result saves that city and switches to it. */
@Composable
private fun CitySearch(uiState: WeatherLocationUiState, onQueryChange: (String) -> Unit, onChooseCity: (PlaceCandidate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("weather_city_search"),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.weather_location_search_hint)) },
            label = { Text(stringResource(R.string.weather_location_search)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = MaterialTheme.shapes.extraSmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Hairline,
                focusedLabelColor = Accent,
                unfocusedLabelColor = Muted,
                focusedPlaceholderColor = Muted,
                unfocusedPlaceholderColor = Muted,
                cursorColor = Accent,
            ),
        )
        when {
            uiState.isSearching -> CircularProgressIndicator(modifier = Modifier.padding(8.dp), color = Accent, strokeWidth = 2.dp)
            uiState.results.isNotEmpty() -> SettingsCard(fullBleedRows = true) {
                uiState.results.forEachIndexed { index, place ->
                    if (index > 0) CardDivider()
                    ResultRow(place, selected = uiState.city?.name == place.name && uiState.mode == WeatherLocationMode.CITY, onClick = { onChooseCity(place) })
                }
            }
            uiState.query.isNotBlank() -> Text(stringResource(R.string.weather_location_no_results), style = MaterialTheme.typography.bodyMedium, color = Muted)
            else -> uiState.city?.let { city ->
                SettingsCard(fullBleedRows = true) { ValueRow(city.name, "") }
            }
        }
    }
}

@Composable
private fun ResultRow(place: PlaceCandidate, selected: Boolean, onClick: () -> Unit) {
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
        if (selected) RadioButton(selected = true, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Accent))
    }
}

private fun com.facetlauncher.app.data.model.WeatherCity.toCandidate() = PlaceCandidate(name, null, null, latitude, longitude, timezone)

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeatherLocationScreenPreview() {
    FacetLauncherTheme {
        WeatherLocationContent(
            uiState = WeatherLocationUiState(
                mode = WeatherLocationMode.CURRENT,
                permissionGranted = true,
                showingFor = "Bengaluru",
                query = "pun",
                results = listOf(PlaceCandidate("Pune", "Maharashtra", "IN", 18.52, 73.85, "Asia/Kolkata"), PlaceCandidate("Punjab", null, "IN", 30.9, 75.8, "Asia/Kolkata")),
            ),
            onBack = {},
            onUseCurrentLocation = {},
            onAllowLocation = {},
            onQueryChange = {},
            onChooseCity = {},
        )
    }
}
