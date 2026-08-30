package com.lumenlauncher.app.ui.settings

import android.content.Context
import com.lumenlauncher.app.data.CalendarPermissionRepository

/**
 * Fakes the grant check with a fixed value instead of using a real OS-level permission grant.
 * Shared by [CalendarSettingsScreenTest]/[CalendarSettingsScreenGrantedTest] and
 * [PermissionsScreenTest]/[PermissionsScreenGrantedTest].
 *
 * A real grant (`GrantPermissionRule`/`UiAutomation.grantRuntimePermission`) requires a matching
 * real *revoke* before any later test class that assumes ungranted state can trust that
 * assumption — but revoking a runtime permission on this app's own package always kills this
 * process (mandatory Android platform behavior: permission checks are cached per-process), and
 * doing that from inside the process being tested races the instrumentation runner's own "test
 * finished" status report back to Orchestrator, occasionally surfacing as "instrumentation
 * process crashed" even though every assertion already passed (see IMPLEMENTATION_PLAN.md for
 * the full investigation — a deferred-revoke timing fix was tried and reverted for introducing a
 * *worse* correctness bug). Faking the grant check sidesteps all of it: no real permission is
 * ever touched, so there's nothing to revoke and no process to kill.
 */
internal class FakeCalendarPermissionRepository(
    context: Context,
    private val granted: Boolean,
) : CalendarPermissionRepository(context) {
    override fun isGranted(): Boolean = granted
}
