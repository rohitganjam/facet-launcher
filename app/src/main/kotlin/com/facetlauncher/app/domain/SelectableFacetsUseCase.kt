package com.facetlauncher.app.domain

import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.FacetLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** The facets the user may switch to right now (see [FacetLimits.selectableIds]); the rest are disabled on the free plan. */
class SelectableFacetsUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val entitlementRepository: EntitlementRepository,
) {
    suspend operator fun invoke(): Set<Long> {
        entitlementRepository.awaitLoaded()
        return FacetLimits.selectableIds(facetRepository.observeFacets().first().map { it.id }, entitlementRepository.isPro.value)
    }

    /** The selectable facets, in list order, re-emitting when the facets or the entitlement change. */
    fun observe(): Flow<List<FacetEntity>> = flow {
        // A cold start must not reconcile facets against a placeholder entitlement.
        entitlementRepository.awaitLoaded()
        emitAll(
            combine(facetRepository.observeFacets(), entitlementRepository.isPro) { facets, isPro ->
                val ids = FacetLimits.selectableIds(facets.map { it.id }, isPro)
                facets.filter { it.id in ids }
            },
        )
    }
}
