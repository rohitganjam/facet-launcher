package com.facetlauncher.app.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "facet_settings")
private val Context.automationDataStore: DataStore<Preferences> by preferencesDataStore(name = "facet_automation")
private val Context.entitlementDataStore: DataStore<Preferences> by preferencesDataStore(name = "facet_entitlement")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    @AutomationDataStore
    fun provideAutomationDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.automationDataStore

    @Provides
    @Singleton
    @EntitlementDataStore
    fun provideEntitlementDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.entitlementDataStore
}
