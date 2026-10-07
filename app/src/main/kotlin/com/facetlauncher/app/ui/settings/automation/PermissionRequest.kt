package com.facetlauncher.app.ui.settings.automation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.facetlauncher.app.data.model.AutomationPermission

/** The system permission dialog for an automation trigger's permission, as a [PermissionRequest]. */
@Composable
internal fun rememberPermissionRequest(): PermissionRequest {
    var pending by remember { mutableStateOf<Pair<AutomationPermission, (Boolean) -> Unit>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        pending?.let { (permission, onResult) -> onResult(results[permission.manifestName()] == true) }
        pending = null
    }
    return remember(launcher) {
        { permission, onResult ->
            pending = permission to onResult
            launcher.launch(permission.requestedNames())
        }
    }
}

/** The permission whose grant counts: precise location for Wi-Fi names, since approximate can't read them. */
private fun AutomationPermission.manifestName(): String = when (this) {
    AutomationPermission.BLUETOOTH_CONNECT -> Manifest.permission.BLUETOOTH_CONNECT
    AutomationPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
}

/** Android 12+ ignores a request for precise location alone, so approximate is asked for alongside it. */
private fun AutomationPermission.requestedNames(): Array<String> = when (this) {
    AutomationPermission.BLUETOOTH_CONNECT -> arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
    AutomationPermission.LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
}
