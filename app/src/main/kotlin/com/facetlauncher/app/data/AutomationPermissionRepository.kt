package com.facetlauncher.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.requiredPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether a device trigger's runtime permission is currently granted. Read live every time, since the
 * user can revoke a permission in system Settings at any moment; a rule whose permission is missing is
 * simply unusable (never met) until it is granted again.
 *
 * `open` so instrumented tests can substitute a fixed answer, like [CalendarPermissionRepository].
 */
@Singleton
open class AutomationPermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    open fun isGranted(permission: AutomationPermission): Boolean =
        ContextCompat.checkSelfPermission(context, permission.manifestName()) == PackageManager.PERMISSION_GRANTED

    /** True when [trigger] needs no permission, or the one it needs is granted. */
    fun isUsable(trigger: AutomationTrigger): Boolean = trigger.requiredPermission()?.let(::isGranted) ?: true
}

private fun AutomationPermission.manifestName(): String = when (this) {
    AutomationPermission.BLUETOOTH_CONNECT -> Manifest.permission.BLUETOOTH_CONNECT
    AutomationPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
}
