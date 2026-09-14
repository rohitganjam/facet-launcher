package com.facetlauncher.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FacetEntity::class, FavoriteAppEntity::class, DockAppEntity::class, FacetDockAppEntity::class,
        DefaultFavoriteAppEntity::class, WidgetPlacementEntity::class,
        FolderEntity::class, FolderAppEntity::class,
        DockFolderPlacementEntity::class, FacetDockFolderPlacementEntity::class,
        FavoriteFolderPlacementEntity::class, DefaultFavoriteFolderPlacementEntity::class,
    ],
    version = FacetDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FacetDatabase : RoomDatabase() {
    abstract fun facetDao(): FacetDao
    abstract fun favoriteAppDao(): FavoriteAppDao
    abstract fun dockAppDao(): DockAppDao
    abstract fun facetDockAppDao(): FacetDockAppDao
    abstract fun defaultFavoriteAppDao(): DefaultFavoriteAppDao
    abstract fun widgetPlacementDao(): WidgetPlacementDao
    abstract fun folderDao(): FolderDao
    abstract fun dockFolderPlacementDao(): DockFolderPlacementDao
    abstract fun facetDockFolderPlacementDao(): FacetDockFolderPlacementDao
    abstract fun favoriteFolderPlacementDao(): FavoriteFolderPlacementDao
    abstract fun defaultFavoriteFolderPlacementDao(): DefaultFavoriteFolderPlacementDao

    companion object {
        // A named constant, not a magic number scattered across DatabaseModule/Migrations/tests —
        // see Migrations.kt for what bumping this requires from here on.
        const val VERSION = 18
    }
}
