package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** The parts of a facet's Home that a launcher-wide default can seed. */
enum class FacetDefaultsScope {
    /** The facet's Dock (the device's default apps) and display style. */
    DOCK,

    /** The facet's Favorites content mode and count. */
    APPS_LIST,

    /** The app list's look, landscape twins included. */
    LOOK,
}

/**
 * Gives a facet its starting point: once when it is created, and again on "Reset to default". The Dock gets
 * the device's default apps (Favorites start empty); the look and Favorites settings are copied from the
 * launcher settings. A facet owns what it was given, so later edits to the defaults never reach it.
 */
class ApplyFacetDefaultsUseCase @Inject constructor(
    private val facetRepository: FacetRepository,
    private val seedFacetDefaults: SeedFacetDefaultsUseCase,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(facetId: Long, scope: FacetDefaultsScope) {
        when (scope) {
            FacetDefaultsScope.DOCK -> {
                seedFacetDefaults(facetId, replace = true)
                facetRepository.applyDockDefaults(facetId, settingsRepository.settings.first())
            }
            FacetDefaultsScope.APPS_LIST -> {
                facetRepository.applyAppsContentDefaults(facetId, settingsRepository.settings.first())
            }
            FacetDefaultsScope.LOOK -> facetRepository.applyLookDefaults(facetId, settingsRepository.settings.first())
        }
    }

    /** Every scope: what a brand-new facet starts from. */
    suspend fun applyAll(facetId: Long) {
        FacetDefaultsScope.entries.forEach { invoke(facetId, it) }
    }
}
