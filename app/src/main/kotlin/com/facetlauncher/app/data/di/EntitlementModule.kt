package com.facetlauncher.app.data.di

import com.facetlauncher.app.data.EntitlementOverride
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.PlayEntitlementRepository
import com.facetlauncher.app.ui.settings.DebugSettingsEntry
import dagger.BindsOptionalOf
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Everything asks for [EntitlementRepository]; the app's answer comes from Google Play. The two optional
 * bindings are provided only by the debug source set (a way to force Pro or Free without Play); a release or
 * benchmark build has neither, and nothing in it can reach one.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EntitlementModule {
    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(impl: PlayEntitlementRepository): EntitlementRepository

    @BindsOptionalOf
    abstract fun optionalEntitlementOverride(): EntitlementOverride

    @BindsOptionalOf
    abstract fun optionalDebugSettingsEntry(): DebugSettingsEntry
}
