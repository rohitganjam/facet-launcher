package com.facetlauncher.app.data.local

/** Which Home list a placement row belongs to. Stored by name in `app_placements`/`folder_placements`. */
enum class PlacementList { DOCK, FAVORITES }

/** [AppPlacementEntity.ownerKey]/[FolderPlacementEntity.ownerKey] for the launcher-wide default list (no facet). */
const val GLOBAL_OWNER_KEY = 0L
