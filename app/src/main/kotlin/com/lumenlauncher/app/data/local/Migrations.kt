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

    val ALL: Array<Migration> = arrayOf(MIGRATION_10_11, MIGRATION_11_12)
}
