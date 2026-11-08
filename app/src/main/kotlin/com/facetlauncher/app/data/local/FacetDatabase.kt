package com.facetlauncher.app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
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
    autoMigrations = [
        AutoMigration(from = 43, to = 44, spec = DropFacetClockColumns::class),
        AutoMigration(from = 45, to = 46, spec = DropFacetOverrideFlags::class),
        AutoMigration(from = 46, to = 47),
    ],
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
        const val VERSION = 48
    }
}

/**
 * 43→44: a facet no longer keeps clock or calendar settings (a clock holds its own, on its page in `clock_stack_items`, copied there by
 * the 42→43 step, [materializeClockConfigs]) and the hosted-clock columns that migration 34→35 already emptied go too. Room rebuilds
 * the table, since SQLite on minSdk 30 cannot drop a column.
 */
@DeleteColumn.Entries(
    DeleteColumn(tableName = "facets", columnName = "overrideClock"),
    DeleteColumn(tableName = "facets", columnName = "clockTemplateId"),
    DeleteColumn(tableName = "facets", columnName = "clockFontOption"),
    DeleteColumn(tableName = "facets", columnName = "clockColorOption"),
    DeleteColumn(tableName = "facets", columnName = "clockAccentColorOption"),
    DeleteColumn(tableName = "facets", columnName = "use24HourTime"),
    DeleteColumn(tableName = "facets", columnName = "clockShowMeridiem"),
    DeleteColumn(tableName = "facets", columnName = "clockDateStyle"),
    DeleteColumn(tableName = "facets", columnName = "clockShowAlarm"),
    DeleteColumn(tableName = "facets", columnName = "clockShowBattery"),
    DeleteColumn(tableName = "facets", columnName = "clockShowWeather"),
    DeleteColumn(tableName = "facets", columnName = "clockWeatherIconStyle"),
    DeleteColumn(tableName = "facets", columnName = "clockWeatherLocation"),
    DeleteColumn(tableName = "facets", columnName = "overrideCalendar"),
    DeleteColumn(tableName = "facets", columnName = "showAllDayEvents"),
    DeleteColumn(tableName = "facets", columnName = "showCalendarIndicator"),
    DeleteColumn(tableName = "facets", columnName = "calendarItemsToShow"),
    DeleteColumn(tableName = "facets", columnName = "selectedCalendarIdsCsv"),
    DeleteColumn(tableName = "facets", columnName = "clockWidgetAppWidgetId"),
    DeleteColumn(tableName = "facets", columnName = "clockWidgetWidthDp"),
    DeleteColumn(tableName = "facets", columnName = "clockWidgetHeightDp"),
    DeleteColumn(tableName = "facets", columnName = "landscapeClockWidgetWidthDp"),
    DeleteColumn(tableName = "facets", columnName = "landscapeClockWidgetHeightDp"),
    DeleteColumn(tableName = "facets", columnName = "clockScale"),
    DeleteColumn(tableName = "facets", columnName = "landscapeClockScale"),
)
class DropFacetClockColumns : AutoMigrationSpec

/**
 * 45→46: a facet owns its dock, favorites and look outright, so the flags that chose between its own and the launcher's go. The 44→45 step
 * ([materializeFacetDefaults]) has already copied the launcher's values into every facet that inherited them.
 */
@DeleteColumn.Entries(
    DeleteColumn(tableName = "facets", columnName = "overrideApps"),
    DeleteColumn(tableName = "facets", columnName = "overridingFavorites"),
    DeleteColumn(tableName = "facets", columnName = "overrideDock"),
)
class DropFacetOverrideFlags : AutoMigrationSpec
