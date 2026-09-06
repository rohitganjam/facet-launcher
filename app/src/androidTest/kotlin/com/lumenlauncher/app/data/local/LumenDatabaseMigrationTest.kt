package com.lumenlauncher.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Regression test for the fix in `DatabaseModule.kt`: [LumenDatabase] used to fall back to a
 * destructive migration on *every* version bump, silently wiping a real user's profiles/dock/
 * favorites/widget placements the moment the schema changed and nobody had added a real
 * [androidx.room.migration.Migration] — see [Migrations] and chat history. This asserts the
 * opposite now holds: opening an old, already-exported schema version with no migration
 * registered for the gap fails loudly (`IllegalStateException`) instead of quietly recreating the
 * tables.
 *
 * `app/build.gradle.kts` adds `schemas/` as the `androidTest` sourceSet's assets dir — the same
 * folder Room's own KSP `room.schemaLocation` arg already exports every version's schema JSON
 * into (`schemas/com.lumenlauncher.app.data.local.LumenDatabase/<version>.json`), matching
 * [MigrationTestHelper]'s own lookup path exactly, so no file copying is needed.
 */
class LumenDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LumenDatabase::class.java,
    )

    @Test
    fun migratingAnOldSchemaVersionWithNoRegisteredMigrationThrowsInsteadOfSilentlySucceeding() {
        // Given a database created at an already-shipped, older schema version
        helper.createDatabase(TEST_DB, 9).close()

        // When opening it at the current version with no migration covering that gap (mirroring
        // DatabaseModule's real builder, which registers no destructive-upgrade fallback anymore)
        // Then Room refuses outright rather than dropping and recreating the tables
        assertThrows(IllegalStateException::class.java) {
            helper.runMigrationsAndValidate(TEST_DB, LumenDatabase.VERSION, true)
        }
    }

    @Test
    fun migration10To11AddsSelectedCalendarIdsCsvColumnWithoutLosingExistingRows() {
        // Given a v10 database with a real profile row (every column NOT NULL at that version)
        val dbV10 = helper.createDatabase(TEST_DB, 10)
        dbV10.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME'
            )
            """.trimIndent(),
        )
        dbV10.close()

        // When migrating to v11
        val dbV11 = helper.runMigrationsAndValidate(TEST_DB, 11, true, Migrations.MIGRATION_10_11)

        // Then the existing row survived, and the new nullable column defaults to NULL ("every
        // calendar selected") rather than an empty string or some other placeholder.
        val cursor = dbV11.query("SELECT name, selectedCalendarIdsCsv FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("selectedCalendarIdsCsv")))
        cursor.close()
    }

    @Test
    fun migration11To12AddsCalendarFontWeightColumnWithoutLosingExistingRows() {
        // Given a v11 database with a real profile row (every column NOT NULL at that version)
        val dbV11 = helper.createDatabase(TEST_DB, 11)
        dbV11.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption, selectedCalendarIdsCsv
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME', NULL
            )
            """.trimIndent(),
        )
        dbV11.close()

        // When migrating to v12
        val dbV12 = helper.runMigrationsAndValidate(TEST_DB, 12, true, Migrations.MIGRATION_11_12)

        // Then the existing row survived, and the new NOT NULL column defaults to 'REGULAR' —
        // matching Converters.kt's own never-return-null discipline for this enum.
        val cursor = dbV12.query("SELECT name, calendarFontWeight FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("REGULAR", cursor.getString(cursor.getColumnIndexOrThrow("calendarFontWeight")))
        cursor.close()
    }

    private companion object {
        const val TEST_DB = "lumen-migration-test.db"
    }
}
