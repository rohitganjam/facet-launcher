package com.facetlauncher.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FacetEntity::class, AppPlacementEntity::class, WidgetPlacementEntity::class,
        FolderEntity::class, FolderAppEntity::class, FolderPlacementEntity::class,
        AutomationRuleEntity::class, WeatherHourEntity::class, WeatherMetaEntity::class,
        ClockStackItemEntity::class,
    ],
    version = FacetDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
// One abstract accessor per DAO, so the count grows with every table.
@Suppress("TooManyFunctions")
abstract class FacetDatabase : RoomDatabase() {
    abstract fun facetDao(): FacetDao
    abstract fun appPlacementDao(): AppPlacementDao
    abstract fun widgetPlacementDao(): WidgetPlacementDao
    abstract fun folderDao(): FolderDao
    abstract fun folderPlacementDao(): FolderPlacementDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun weatherDao(): WeatherDao
    abstract fun clockStackItemDao(): ClockStackItemDao

    companion object {
        // A named constant, not a magic number scattered across DatabaseModule/Migrations/tests —
        // see Migrations.kt for what bumping this requires from here on.
        const val VERSION = 36
    }
}
