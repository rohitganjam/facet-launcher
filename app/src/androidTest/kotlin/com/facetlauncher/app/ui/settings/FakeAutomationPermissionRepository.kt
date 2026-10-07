package com.facetlauncher.app.ui.settings

import android.content.Context
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.model.AutomationPermission

/** Fixed Bluetooth/location grant state, for the same reasons as [FakeCalendarPermissionRepository]. */
internal class FakeAutomationPermissionRepository(
    context: Context,
    private val granted: Boolean,
) : AutomationPermissionRepository(context) {
    override fun isGranted(permission: AutomationPermission): Boolean = granted
}
