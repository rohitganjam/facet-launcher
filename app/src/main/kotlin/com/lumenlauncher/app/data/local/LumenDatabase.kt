package com.lumenlauncher.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ProfileEntity::class, FavoriteAppEntity::class, DockAppEntity::class, DefaultFavoriteAppEntity::class],
    version = 6,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteAppDao(): FavoriteAppDao
    abstract fun dockAppDao(): DockAppDao
    abstract fun defaultFavoriteAppDao(): DefaultFavoriteAppDao
}
