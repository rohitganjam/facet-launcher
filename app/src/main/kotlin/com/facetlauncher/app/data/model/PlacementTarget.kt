package com.facetlauncher.app.data.model

/**
 * One Dock or Favorites list: the launcher-wide default when [facetId] is null, else that facet's
 * own overriding list. Every placement read and write names its list with one of these.
 */
data class PlacementTarget(val list: PlacementList, val facetId: Long? = null) {
    /** The stored `ownerKey` for this list (see `AppPlacementEntity`). */
    val ownerKey: Long get() = facetId ?: GLOBAL_OWNER_KEY

    val isGlobal: Boolean get() = facetId == null

    companion object {
        val GlobalDock = PlacementTarget(PlacementList.DOCK)
        val GlobalFavorites = PlacementTarget(PlacementList.FAVORITES)
    }
}
