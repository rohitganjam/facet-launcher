package com.lumenlauncher.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Every real Room [Migration] this app ships, wired into [LumenDatabase]'s builder via
 * [DatabaseModule][com.lumenlauncher.app.data.di.DatabaseModule].
 *
 * Any change to `version` in [LumenDatabase] MUST come with a matching entry here, or Room throws
 * `IllegalStateException` on every affected device the moment it tries to open the database (by
 * design — see [DatabaseModule][com.lumenlauncher.app.data.di.DatabaseModule]'s own doc comment
 * for why that's preferable to a silent destructive fallback). To add one:
 *
 * 1. Bump `version` in `LumenDatabase.kt` and make the schema change (new entity, new/changed
 *    column, etc). Building the project re-exports `app/schemas/.../<newVersion>.json`.
 * 2. Add `MIGRATION_<old>_<new> = object : Migration(<old>, <new>) { override fun migrate(db:
 *    SupportSQLiteDatabase) { db.execSQL(...) } }` below, and add it to [ALL].
 * 3. Add a test to `LumenDatabaseMigrationTest` that builds the pre-migration schema via
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

    val ALL: Array<Migration> = arrayOf(MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
}
