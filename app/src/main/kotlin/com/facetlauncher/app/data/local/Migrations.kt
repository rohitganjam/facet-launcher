package com.facetlauncher.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Every real Room [Migration] this app ships, wired into [FacetDatabase]'s builder via
 * [DatabaseModule][com.facetlauncher.app.data.di.DatabaseModule].
 *
 * Any change to `version` in [FacetDatabase] MUST come with a matching entry here, or Room throws
 * `IllegalStateException` on every affected device the moment it tries to open the database (by
 * design — see [DatabaseModule][com.facetlauncher.app.data.di.DatabaseModule]'s own doc comment
 * for why that's preferable to a silent destructive fallback). To add one:
 *
 * 1. Bump `version` in `FacetDatabase.kt` and make the schema change (new entity, new/changed
 *    column, etc). Building the project re-exports `app/schemas/.../<newVersion>.json`.
 * 2. Add `MIGRATION_<old>_<new> = object : Migration(<old>, <new>) { override fun migrate(db:
 *    SupportSQLiteDatabase) { db.execSQL(...) } }` below, and add it to [ALL].
 * 3. Add a test to `FacetDatabaseMigrationTest` that builds the pre-migration schema via
 *    `MigrationTestHelper.createDatabase(name, <old>)`, inserts a representative row with real
 *    SQL, runs the migration, and asserts the row/column survived — mirroring Room's own official
 *    migration-testing guide. A migration that only compiles but was never run against real data is
 *    not verified.
 */
object Migrations {
    /**
     * Adds `profiles.selectedCalendarIdsCsv` (nullable `TEXT`, no default needed since `NULL`
     * is itself a valid, meaningful value — "every calendar selected" — not a placeholder to
     * paper over). Every other v10->v11 schema change (calendarFontOption/calendarColorOption
     * moving from being gated by `overrideCalendar` to `overrideClock`) is an application-logic
     * change only — those columns already existed and aren't touched here.
     */
    val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN selectedCalendarIdsCsv TEXT")
        }
    }

    /**
     * Adds `profiles.calendarFontWeight` — part of the same Clock+Calendar design bundle as the
     * existing `calendarFontOption`/`calendarColorOption` columns, so it needs the same `NOT NULL
     * DEFAULT` treatment those got when they were first added: a real enum with a meaningful
     * default (`REGULAR`), not a placeholder `NULL`.
     */
    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN calendarFontWeight TEXT NOT NULL DEFAULT 'REGULAR'")
        }
    }

    /**
     * Adds `profiles.appListVerticalAlignment` — part of the same "Apps section" bundle as the
     * existing `appRowPosition`/`appRowPresentation`/`listContentMode` columns (all gated by
     * `overrideApps`), so it gets the same `NOT NULL DEFAULT` treatment: a real enum with a
     * meaningful default (`BOTTOM`, today's unchanged behavior), not a placeholder `NULL`.
     */
    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN appListVerticalAlignment TEXT NOT NULL DEFAULT 'BOTTOM'")
        }
    }

    /**
     * Adds `profiles.clockAlignment`/`calendarAlignment`/`clockZoneHeightDp` — the clock widget's
     * position settings move into the same Clock+Calendar design bundle (gated by `overrideClock`)
     * as `clockTemplateId`/etc, so a profile can now override its own alignment/zone-height
     * independent of the global default. The two alignment enums get the same `NOT NULL DEFAULT
     * 'LEFT'` treatment as every other enum column in this bundle; `clockZoneHeightDp` stays
     * nullable with no default, same reasoning as `selectedCalendarIdsCsv` in MIGRATION_10_11 —
     * `NULL` here is itself meaningful ("never dragged, use the block's natural default position"),
     * not a placeholder.
     */
    val MIGRATION_13_14: Migration = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN clockAlignment TEXT NOT NULL DEFAULT 'LEFT'")
            db.execSQL("ALTER TABLE profiles ADD COLUMN calendarAlignment TEXT NOT NULL DEFAULT 'LEFT'")
            db.execSQL("ALTER TABLE profiles ADD COLUMN clockZoneHeightDp REAL")
        }
    }

    /**
     * Adds the current Clock+Calendar design columns to `profiles` (all gated by `overrideClock`):
     * `clockAccentColorOption`/`clockDateStyle` (same `NOT NULL DEFAULT` treatment as every other
     * enum column in that bundle) and `clockScale` (uniform Home-clock scaling). One migration —
     * nothing shipped between v14 and v15.
     */
    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN clockAccentColorOption TEXT NOT NULL DEFAULT 'ACCENT_PRIMARY'")
            db.execSQL("ALTER TABLE profiles ADD COLUMN clockDateStyle TEXT NOT NULL DEFAULT 'FULL'")
            db.execSQL("ALTER TABLE profiles ADD COLUMN clockScale REAL NOT NULL DEFAULT 0.8")
        }
    }

    /**
     * Per-profile dock overrides. Adds `profiles.overrideDock` (the single flag gating the whole
     * Dock section, same `NOT NULL DEFAULT 0` treatment as `overrideApps`/`overrideClock`) and
     * `profiles.dockDisplayMode` (this profile's own Icons/Text style, `NOT NULL DEFAULT 'ICONS'`
     * — today's unchanged look — same treatment as every other enum column in the profile bundles),
     * plus the new `profile_dock_apps` table: the per-profile counterpart to the existing global
     * `dock_apps`, structurally identical to `favorite_apps` (profileId FK, cascade delete, unique
     * (profileId, packageName, activityName) index).
     */
    val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN overrideDock INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE profiles ADD COLUMN dockDisplayMode TEXT NOT NULL DEFAULT 'ICONS'")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `profile_dock_apps` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`profileId` INTEGER NOT NULL, " +
                    "`packageName` TEXT NOT NULL, " +
                    "`activityName` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_profile_dock_apps_profileId_packageName_activityName` " +
                    "ON `profile_dock_apps` (`profileId`, `packageName`, `activityName`)",
            )
        }
    }

    /**
     * The Profile -> Facet rename: `profiles` -> `facets`, `profile_dock_apps` -> `facet_dock_apps`,
     * and the `profileId` FK column -> `facetId` on `facet_dock_apps`/`favorite_apps` (`dock_apps`
     * has no such column — it's the launcher-wide default dock, not per-facet).
     *
     * `profiles` itself has no FK of its own, so a plain `RENAME TABLE` is enough for it. But
     * `facet_dock_apps`/`favorite_apps` each embed their parent table's name inside their own
     * FOREIGN KEY clause, and on this device's SQLite build that embedded reference does NOT get
     * rewritten by `RENAME TABLE`/`RENAME COLUMN` the way SQLite's docs describe — confirmed by two
     * separate instrumented `FacetDatabaseMigrationTest` runs on a real emulator, each catching
     * Room's schema validator still seeing `referenceTable = 'profiles'` on whichever of the two
     * tables happened to be checked first (the JVM/Robolectric unit-test run of the same migration
     * didn't surface this at all — different SQLite build, different behavior). So both are
     * recreated outright (create-copy-drop-rename) rather than renamed/altered in place, and their
     * unique indices recreated fresh under the name Room's convention expects (SQLite has no
     * `ALTER INDEX RENAME`, and recreating the table drops them anyway).
     */
    val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles RENAME TO facets")

            db.execSQL(
                "CREATE TABLE facet_dock_apps_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`facetId` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `activityName` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, FOREIGN KEY(`facetId`) REFERENCES `facets`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL(
                "INSERT INTO facet_dock_apps_new (id, facetId, packageName, activityName, position) " +
                    "SELECT id, profileId, packageName, activityName, position FROM profile_dock_apps",
            )
            db.execSQL("DROP TABLE profile_dock_apps")
            db.execSQL("ALTER TABLE facet_dock_apps_new RENAME TO facet_dock_apps")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_facet_dock_apps_facetId_packageName_activityName " +
                    "ON facet_dock_apps (facetId, packageName, activityName)",
            )

            db.execSQL(
                "CREATE TABLE favorite_apps_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`facetId` INTEGER NOT NULL, `packageName` TEXT NOT NULL, `activityName` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, FOREIGN KEY(`facetId`) REFERENCES `facets`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL(
                "INSERT INTO favorite_apps_new (id, facetId, packageName, activityName, position) " +
                    "SELECT id, profileId, packageName, activityName, position FROM favorite_apps",
            )
            db.execSQL("DROP TABLE favorite_apps")
            db.execSQL("ALTER TABLE favorite_apps_new RENAME TO favorite_apps")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_apps_facetId_packageName_activityName " +
                    "ON favorite_apps (facetId, packageName, activityName)",
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
        MIGRATION_16_17,
    )
}
