package com.facetlauncher.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether the user has bought Pro. Billing doesn't exist yet, so everyone is entitled; the billing plan
 * replaces [isPro] with the real purchase state and nothing else changes — every Pro gate reads it from here.
 * `open` so tests can substitute a free user.
 */
@Singleton
open class EntitlementRepository @Inject constructor() {
    open val isPro: StateFlow<Boolean> = MutableStateFlow(true)
}
