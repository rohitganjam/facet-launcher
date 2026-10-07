package com.facetlauncher.app.domain

import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.FacetLimits
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface AddFacetResult {
    data class Added(val facet: FacetEntity) : AddFacetResult

    /** The user is at their limit; nothing was added. */
    data object LimitReached : AddFacetResult
}

/** Adds a facet unless the user is at their limit ([FacetLimits]: 3 free, 10 with Pro). */
class AddFacetUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val entitlementRepository: EntitlementRepository,
) {
    suspend operator fun invoke(): AddFacetResult {
        val count = facetRepository.observeFacets().first().size
        return if (FacetLimits.canAdd(count, entitlementRepository.isPro.value)) {
            AddFacetResult.Added(facetRepository.addFacet())
        } else {
            AddFacetResult.LimitReached
        }
    }
}
