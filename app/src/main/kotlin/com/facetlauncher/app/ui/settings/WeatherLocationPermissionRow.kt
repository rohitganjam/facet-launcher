package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R

/**
 * The Location row of the weather settings screens: the Permissions screen's own row, with what weather uses it for.
 * Tapping it opens Settings → Permissions, where the permission is asked for (or, once Android stops asking, App info).
 */
@Composable
internal fun WeatherLocationPermissionRow(granted: Boolean, onOpenPermissions: () -> Unit, modifier: Modifier = Modifier) {
    PermissionRow(
        permission = PermissionRowState(
            kind = PermissionKind.LOCATION,
            title = stringResource(R.string.permission_location_title),
            subtitle = stringResource(R.string.weather_permission_location_subtitle),
            isGranted = granted,
        ),
        onTurnOnClick = onOpenPermissions,
        modifier = modifier,
    )
}
