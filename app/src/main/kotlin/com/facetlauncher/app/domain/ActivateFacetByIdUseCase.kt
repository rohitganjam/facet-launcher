package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import javax.inject.Inject

/**
 * Switches the active facet from an externally-supplied [facetId] — a deep link
 * (`facetlauncher://facet/{id}`) or a dynamic shortcut's launch `Intent`, both funneled here by
 * [com.facetlauncher.app.LauncherActivity]. Spans [FacetRepository] (existence check) and
 * [SettingsRepository] (`setActiveFacetId`), so per CLAUDE.md's layering rule this is a use case
 * rather than either the `Activity` or a `ViewModel` calling both directly.
 *
 * A no-op when [facetId] doesn't resolve to a real facet — a stale shortcut for a since-deleted
 * facet, a hand-typed bad id, or a race with a delete must do nothing rather than crash or
 * silently create a facet (mirrors [EnsureActiveFacetUseCase]'s own "don't create on a bad id"
 * reasoning, just without the fallback-creation step that only makes sense at startup).
 */
class ActivateFacetByIdUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(facetId: Long) {
        if (facetRepository.getById(facetId) == null) return
        settingsRepository.setActiveFacetId(facetId)
    }
}
