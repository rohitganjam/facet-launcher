package com.facetlauncher.app.debug

import com.facetlauncher.app.data.EntitlementOverride
import com.facetlauncher.app.ui.settings.DebugSettingsEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Debug builds only: fills the optional bindings declared in `EntitlementModule`. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DebugBindingsModule {
    @Binds
    abstract fun bindEntitlementOverride(impl: DebugEntitlementOverride): EntitlementOverride

    @Binds
    abstract fun bindDebugSettingsEntry(impl: DebugSettingsEntryImpl): DebugSettingsEntry
}
