package com.facetlauncher.app.data.di

import javax.inject.Qualifier

/** The `facet_entitlement` DataStore: the cached Pro state. Separate from `facet_settings` so it never travels in a backup. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EntitlementDataStore
