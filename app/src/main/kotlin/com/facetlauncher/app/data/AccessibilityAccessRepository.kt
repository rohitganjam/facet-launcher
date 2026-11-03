package com.facetlauncher.app.data

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether [FacetAccessibilityService] is switched on in system Settings
 * (`ACTION_ACCESSIBILITY_SETTINGS`). Like notification access, it has no grant-change callback, so
 * callers re-check on `onResume`.
 */
@Singleton
open class AccessibilityAccessRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    open fun isGranted(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val ours = ComponentName(context, FacetAccessibilityService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == ours }
    }
}
