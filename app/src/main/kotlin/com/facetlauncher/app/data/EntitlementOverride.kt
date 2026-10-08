package com.facetlauncher.app.data

import kotlinx.coroutines.flow.Flow

/**
 * A debug-only way to force the entitlement without Play: `true` forces Pro, `false` forces Free, `null`
 * follows Play. Only the debug source set provides one; release and benchmark builds have none, so there is
 * nothing to reach (see `EntitlementModule`).
 */
interface EntitlementOverride {
    val forced: Flow<Boolean?>
}
