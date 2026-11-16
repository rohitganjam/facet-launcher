package com.facetlauncher.app.data.di

import javax.inject.Qualifier

/** The `facet_vault` DataStore: the App Vault's hidden-app list, PIN hash and lockout state. Never travels in a backup or a device transfer. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VaultDataStore
