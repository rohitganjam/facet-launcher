package com.facetlauncher.app.data.di

import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.PlayEntitlementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Everything asks for [EntitlementRepository]; the app's answer comes from Google Play. */
@Module
@InstallIn(SingletonComponent::class)
abstract class EntitlementModule {
    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(impl: PlayEntitlementRepository): EntitlementRepository
}
