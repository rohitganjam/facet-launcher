package com.facetlauncher.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** An entitlement tests can flip; a free user by passing `false`. */
class FakeEntitlementRepository(initial: Boolean = true) : EntitlementRepository() {
    val proFlow = MutableStateFlow(initial)
    override val isPro: StateFlow<Boolean> = proFlow
}
