package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.ProQueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Whether the user has Pro, and the one place every Pro gate reads it from. This base class is "everyone is
 * Pro" and is what tests and previews use; the app injects [PlayEntitlementRepository], which answers from
 * Google Play. `open` so tests can substitute a free user.
 */
open class EntitlementRepository {
    open val isPro: StateFlow<Boolean> = MutableStateFlow(true)

    /** Suspends until the stored value has been read, so a cold start never decides anything from a placeholder. */
    open suspend fun awaitLoaded() = Unit

    /** Asks Play again (startup, every resume, Restore purchases) and returns what Play said. */
    open suspend fun refresh(): ProQueryResult = ProQueryResult.Owned
}
