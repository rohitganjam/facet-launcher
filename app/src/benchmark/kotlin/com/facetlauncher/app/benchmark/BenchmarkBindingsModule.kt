package com.facetlauncher.app.benchmark

import com.facetlauncher.app.data.EntitlementOverride
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Benchmark builds only, never shipped: treats the user as Pro, so a run can have more than the free plan's three facets. */
@Singleton
class BenchmarkEntitlementOverride @Inject constructor() : EntitlementOverride {
    override val forced: Flow<Boolean?> = flowOf(true)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BenchmarkBindingsModule {
    @Binds
    abstract fun bindEntitlementOverride(impl: BenchmarkEntitlementOverride): EntitlementOverride
}
