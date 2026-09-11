package com.lumenlauncher.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ProfileEntity::class, FavoriteAppEntity::class, DockAppEntity::class, ProfileDockAppEntity::class, DefaultFavoriteAppEntity::class, WidgetPlacementEntity::class],
    version = LumenDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteAppDao(): FavoriteAppDao
    abstract fun dockAppDao(): DockAppDao
    abstract fun profileDockAppDao(): ProfileDockAppDao
    abstract fun defaultFavoriteAppDao(): DefaultFavoriteAppDao
    abstract fun widgetPlacementDao(): WidgetPlacementDao

    companion object {
        // A named constant, not a magic number scattered across DatabaseModule/Migrations/tests —
        // see Migrations.kt for what bumping this requires from here on.
        const val VERSION = 16
    }
}
