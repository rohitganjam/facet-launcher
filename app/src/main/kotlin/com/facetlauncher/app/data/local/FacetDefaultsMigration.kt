package com.facetlauncher.app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.facetlauncher.app.data.model.GLOBAL_OWNER_KEY
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacementList

private const val SENTINEL = "LAUNCHER_DEFAULT"

/**
 * 44→45 changes no schema: a facet that inherited the launcher's dock, favorites, content mode or look gets today's
 * values written into its own rows and columns, so the 45→46 column drop loses nothing and nothing visibly changes.
 * [launcher] is the launcher-wide settings, which the database cannot read on its own.
 */
internal fun materializeFacetDefaults(db: SupportSQLiteDatabase, launcher: LauncherSettings) {
    copyListToInheritingFacets(db, PlacementList.DOCK, flagColumn = "overrideDock")
    copyListToInheritingFacets(db, PlacementList.FAVORITES, flagColumn = "overridingFavorites")

    db.execSQL(
        "UPDATE facets SET listContentMode = ?, appsToShowCount = ? WHERE overrideApps = 0",
        arrayOf<Any?>(launcher.listContentMode.name, launcher.appsToShowCount),
    )

    // Landscape first: a null twin on a facet whose portrait value was inherited followed the launcher's landscape value.
    launcher.landscape.appRowPosition?.let { fillLandscape(db, "landscapeAppRowPosition", "appRowPosition", it.name) }
    launcher.landscape.appListVerticalAlignment?.let { fillLandscape(db, "landscapeAppListVerticalAlignment", "appListVerticalAlignment", it.name) }
    launcher.landscape.appListColumnAlignment?.let { fillLandscape(db, "landscapeAppListColumnAlignment", "appListColumnAlignment", it.name) }

    mapOf(
        "dockDisplayMode" to launcher.dockDisplayMode.name,
        "appRowPosition" to launcher.appRowPosition.name,
        "appRowPresentation" to launcher.appRowPresentation.name,
        "appListVerticalAlignment" to launcher.appListVerticalAlignment.name,
        "appListLayout" to launcher.appListLayout.name,
        "appListColumnAlignment" to launcher.appListColumnAlignment.name,
        "appListGridColumns" to launcher.appListGridColumns.name,
        "appListGridDisplayMode" to launcher.appListGridDisplayMode.name,
        "appListShrinkToFit" to launcher.appListShrinkToFit.name,
    ).forEach { (column, value) ->
        db.execSQL("UPDATE facets SET $column = ? WHERE $column = '$SENTINEL'", arrayOf<Any?>(value))
    }
}

private fun fillLandscape(db: SupportSQLiteDatabase, landscapeColumn: String, portraitColumn: String, value: String) {
    db.execSQL(
        "UPDATE facets SET $landscapeColumn = ? WHERE $landscapeColumn IS NULL AND $portraitColumn = '$SENTINEL'",
        arrayOf<Any?>(value),
    )
}

/** Replaces the own rows of every facet whose [flagColumn] is off (stale, if it ever overrode) with a copy of the launcher-wide list. */
private fun copyListToInheritingFacets(db: SupportSQLiteDatabase, list: PlacementList, flagColumn: String) {
    val inheriting = "SELECT id FROM facets WHERE $flagColumn = 0"
    db.execSQL("DELETE FROM app_placements WHERE list = ? AND ownerKey IN ($inheriting)", arrayOf<Any?>(list.name))
    db.execSQL("DELETE FROM folder_placements WHERE list = ? AND ownerKey IN ($inheriting)", arrayOf<Any?>(list.name))
    db.execSQL(
        "INSERT INTO app_placements (list, facetId, ownerKey, packageName, activityName, position, profile, userId) " +
            "SELECT p.list, f.id, f.id, p.packageName, p.activityName, p.position, p.profile, p.userId " +
            "FROM app_placements p, facets f WHERE p.list = ? AND p.ownerKey = $GLOBAL_OWNER_KEY AND f.$flagColumn = 0",
        arrayOf<Any?>(list.name),
    )
    db.execSQL(
        "INSERT INTO folder_placements (list, facetId, ownerKey, folderId, position) " +
            "SELECT p.list, f.id, f.id, p.folderId, p.position " +
            "FROM folder_placements p, facets f WHERE p.list = ? AND p.ownerKey = $GLOBAL_OWNER_KEY AND f.$flagColumn = 0",
        arrayOf<Any?>(list.name),
    )
}
