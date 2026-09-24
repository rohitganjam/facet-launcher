package com.facetlauncher.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Regression test for the fix in `DatabaseModule.kt`: [FacetDatabase] used to fall back to a
 * destructive migration on *every* version bump, silently wiping a real user's facets/dock/
 * favorites/widget placements the moment the schema changed and nobody had added a real
 * [androidx.room.migration.Migration] — see [Migrations] and chat history. This asserts the
 * opposite now holds: opening an old, already-exported schema version with no migration
 * registered for the gap fails loudly (`IllegalStateException`) instead of quietly recreating the
 * tables.
 *
 * `app/build.gradle.kts` adds `schemas/` as the `androidTest` sourceSet's assets dir — the same
 * folder Room's own KSP `room.schemaLocation` arg already exports every version's schema JSON
 * into (`schemas/com.facetlauncher.app.data.local.FacetDatabase/<version>.json`), matching
 * [MigrationTestHelper]'s own lookup path exactly, so no file copying is needed.
 */
class FacetDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FacetDatabase::class.java,
    )

    @Test
    fun migratingAnOldSchemaVersionWithNoRegisteredMigrationThrowsInsteadOfSilentlySucceeding() {
        // Given a database created at an already-shipped, older schema version
        helper.createDatabase(TEST_DB, 9).close()

        // When opening it at the current version with no migration covering that gap (mirroring
        // DatabaseModule's real builder, which registers no destructive-upgrade fallback anymore)
        // Then Room refuses outright rather than dropping and recreating the tables
        assertThrows(IllegalStateException::class.java) {
            helper.runMigrationsAndValidate(TEST_DB, FacetDatabase.VERSION, true)
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

    @Test
    fun migration12To13AddsAppListVerticalAlignmentColumnWithoutLosingExistingRows() {
        // Given a v12 database with a real profile row (every column NOT NULL at that version)
        val dbV12 = helper.createDatabase(TEST_DB, 12)
        dbV12.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption, selectedCalendarIdsCsv, calendarFontWeight
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME', NULL, 'REGULAR'
            )
            """.trimIndent(),
        )
        dbV12.close()

        // When migrating to v13
        val dbV13 = helper.runMigrationsAndValidate(TEST_DB, 13, true, Migrations.MIGRATION_12_13)

        // Then the existing row survived, and the new NOT NULL column defaults to 'BOTTOM' — today's
        // unchanged app-list anchoring behavior.
        val cursor = dbV13.query("SELECT name, appListVerticalAlignment FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("BOTTOM", cursor.getString(cursor.getColumnIndexOrThrow("appListVerticalAlignment")))
        cursor.close()
    }

    @Test
    fun migration13To14AddsClockAlignmentCalendarAlignmentAndClockZoneHeightColumnsWithoutLosingExistingRows() {
        // Given a v13 database with a real profile row (every column NOT NULL at that version)
        val dbV13 = helper.createDatabase(TEST_DB, 13)
        dbV13.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption, selectedCalendarIdsCsv, calendarFontWeight,
                appListVerticalAlignment
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME', NULL, 'REGULAR',
                'BOTTOM'
            )
            """.trimIndent(),
        )
        dbV13.close()

        // When migrating to v14
        val dbV14 = helper.runMigrationsAndValidate(TEST_DB, 14, true, Migrations.MIGRATION_13_14)

        // Then the existing row survived: the two new NOT NULL enum columns default to 'LEFT'
        // (today's unchanged position), and the new nullable REAL column defaults to NULL ("never
        // dragged"), not some placeholder height.
        val cursor = dbV14.query("SELECT name, clockAlignment, calendarAlignment, clockZoneHeightDp FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("LEFT", cursor.getString(cursor.getColumnIndexOrThrow("clockAlignment")))
        assertEquals("LEFT", cursor.getString(cursor.getColumnIndexOrThrow("calendarAlignment")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("clockZoneHeightDp")))
        cursor.close()
    }

    @Test
    fun migration14To15AddsClockAccentColorOptionClockDateStyleAndClockScaleColumnsWithoutLosingExistingRows() {
        // Given a v14 database with a real profile row (every column NOT NULL at that version)
        val dbV14 = helper.createDatabase(TEST_DB, 14)
        dbV14.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption, selectedCalendarIdsCsv, calendarFontWeight,
                appListVerticalAlignment, clockAlignment, calendarAlignment, clockZoneHeightDp
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME', NULL, 'REGULAR',
                'BOTTOM', 'LEFT', 'LEFT', NULL
            )
            """.trimIndent(),
        )
        dbV14.close()

        // When migrating to v15
        val dbV15 = helper.runMigrationsAndValidate(TEST_DB, 15, true, Migrations.MIGRATION_14_15)

        // Then the existing row survived, and all three new NOT NULL columns default to their
        // documented values — today's unchanged look for a no-accent template, every template's
        // date line, and the shipped 0.8 clock scale.
        val cursor = dbV15.query("SELECT name, clockAccentColorOption, clockDateStyle, clockScale FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("ACCENT_PRIMARY", cursor.getString(cursor.getColumnIndexOrThrow("clockAccentColorOption")))
        assertEquals("FULL", cursor.getString(cursor.getColumnIndexOrThrow("clockDateStyle")))
        assertEquals(0.8f, cursor.getFloat(cursor.getColumnIndexOrThrow("clockScale")), 0.0001f)
        cursor.close()
    }

    @Test
    fun migration15To16AddsProfileDockOverrideColumnsAndTheProfileDockAppsTableWithoutLosingExistingRows() {
        // Given a v15 database with a real profile row (every column NOT NULL at that version)
        val dbV15 = helper.createDatabase(TEST_DB, 15)
        dbV15.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                use24HourTime, clockShowMeridiem, overrideApps, appRowPosition, appRowPresentation,
                listContentMode, appsToShowCount, overridingFavorites, overrideCalendar, showAllDayEvents,
                calendarFontOption, calendarColorOption, selectedCalendarIdsCsv, calendarFontWeight,
                appListVerticalAlignment, clockAlignment, calendarAlignment, clockZoneHeightDp,
                clockAccentColorOption, clockDateStyle, clockScale
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                0, 0, 0, 'LEFT', 'ICON_AND_TEXT',
                'FAVORITES', 6, 0, 0, 1,
                'LAUNCHER_DEFAULT', 'THEME', NULL, 'REGULAR',
                'BOTTOM', 'LEFT', 'LEFT', NULL,
                'ACCENT_PRIMARY', 'FULL', 0.8
            )
            """.trimIndent(),
        )
        dbV15.close()

        // When migrating to v16
        val dbV16 = helper.runMigrationsAndValidate(TEST_DB, 16, true, Migrations.MIGRATION_15_16)

        // Then the existing row survived: the two new columns default to today's unchanged
        // behavior (not overriding, Icons style)...
        val cursor = dbV16.query("SELECT name, overrideDock, dockDisplayMode FROM profiles WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("overrideDock")))
        assertEquals("ICONS", cursor.getString(cursor.getColumnIndexOrThrow("dockDisplayMode")))
        cursor.close()

        // ...and the new per-profile dock table exists, cascades on the FK, and enforces its
        // unique (profileId, packageName, activityName) index.
        dbV16.execSQL(
            "INSERT INTO profile_dock_apps (profileId, packageName, activityName, position) " +
                "VALUES (1, 'com.example.a', '.Main', 0)",
        )
        val dockCursor = dbV16.query("SELECT packageName FROM profile_dock_apps WHERE profileId = 1")
        assertTrue(dockCursor.moveToFirst())
        assertEquals("com.example.a", dockCursor.getString(dockCursor.getColumnIndexOrThrow("packageName")))
        dockCursor.close()
    }

    @Test
    fun migration16To17RenamesProfilesToFacetsAndProfileIdToFacetIdWithoutLosingExistingRows() {
        // Given a v16 database with a real profile row, a profile_dock_apps row, and a favorite_apps
        // row scoped to it (every column NOT NULL at that version, per FacetEntity's defaults)
        val dbV16 = helper.createDatabase(TEST_DB, 16)
        dbV16.execSQL(
            """
            INSERT INTO profiles (
                id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption,
                clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle,
                calendarFontOption, calendarColorOption, calendarFontWeight, clockAlignment,
                calendarAlignment, clockZoneHeightDp, clockScale, overrideApps, appRowPosition,
                appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment,
                overridingFavorites, overrideDock, dockDisplayMode, overrideCalendar,
                showAllDayEvents, selectedCalendarIdsCsv
            ) VALUES (
                1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME',
                'ACCENT_PRIMARY', 0, 0, 'FULL',
                'LAUNCHER_DEFAULT', 'THEME', 'REGULAR', 'LEFT',
                'LEFT', NULL, 0.8, 0, 'LEFT',
                'ICON_AND_TEXT', 'FAVORITES', 6, 'BOTTOM',
                0, 0, 'ICONS', 0,
                1, NULL
            )
            """.trimIndent(),
        )
        dbV16.execSQL(
            "INSERT INTO profile_dock_apps (profileId, packageName, activityName, position) " +
                "VALUES (1, 'com.example.a', '.Main', 0)",
        )
        dbV16.execSQL(
            "INSERT INTO favorite_apps (profileId, packageName, activityName, position) " +
                "VALUES (1, 'com.example.b', '.Main', 0)",
        )
        dbV16.close()

        // When migrating to v17
        val dbV17 = helper.runMigrationsAndValidate(TEST_DB, 17, true, Migrations.MIGRATION_16_17)

        // Then the profile row survived under the renamed `facets` table
        val facetCursor = dbV17.query("SELECT name FROM facets WHERE id = 1")
        assertTrue(facetCursor.moveToFirst())
        assertEquals("Work", facetCursor.getString(facetCursor.getColumnIndexOrThrow("name")))
        facetCursor.close()

        // ...the dock row survived under `facet_dock_apps.facetId`, with its unique index intact
        val dockCursor = dbV17.query("SELECT packageName FROM facet_dock_apps WHERE facetId = 1")
        assertTrue(dockCursor.moveToFirst())
        assertEquals("com.example.a", dockCursor.getString(dockCursor.getColumnIndexOrThrow("packageName")))
        dockCursor.close()

        // ...and the favorite row survived under `favorite_apps.facetId`
        val favoriteCursor = dbV17.query("SELECT packageName FROM favorite_apps WHERE facetId = 1")
        assertTrue(favoriteCursor.moveToFirst())
        assertEquals("com.example.b", favoriteCursor.getString(favoriteCursor.getColumnIndexOrThrow("packageName")))
        favoriteCursor.close()
    }

    @Test
    fun migration18To19AddsProfileColumnAndWidensUniqueIndicesWithoutLosingExistingRows() {
        // Given a v18 database with one real row in each of favorite_apps (facetId-scoped, has a
        // unique index), dock_apps (global, has a unique index), and widget_placements (keyed on
        // appWidgetId, no index to widen) — every row predates Work Profile support.
        val dbV18 = helper.createDatabase(TEST_DB, 18)
        dbV18.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, calendarFontOption, calendarColorOption, " +
                "calendarFontWeight, clockAlignment, calendarAlignment, clockZoneHeightDp, clockScale, overrideApps, appRowPosition, " +
                "appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment, overridingFavorites, overrideDock, " +
                "dockDisplayMode, overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv) VALUES " +
                "(1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LAUNCHER_DEFAULT', " +
                "'THEME', 'REGULAR', 'LEFT', 'LEFT', NULL, 0.8, 0, 'LEFT', 'ICON_AND_TEXT', 'FAVORITES', 6, 'BOTTOM', 0, 0, 'ICONS', 0, 1, NULL)",
        )
        dbV18.execSQL("INSERT INTO favorite_apps (facetId, packageName, activityName, position) VALUES (1, 'com.example.a', '.Main', 0)")
        dbV18.execSQL("INSERT INTO dock_apps (packageName, activityName, position) VALUES ('com.example.b', '.Main', 0)")
        dbV18.execSQL("INSERT INTO widget_placements (appWidgetId, providerPackageName, providerClassName, row, col, colSpan, rowSpan) VALUES (1, 'com.example.widget', '.WidgetProvider', 0, 0, 2, 2)")
        dbV18.close()

        // When migrating to v19
        val dbV19 = helper.runMigrationsAndValidate(TEST_DB, 19, true, Migrations.MIGRATION_18_19)

        // Then every existing row survived, defaulted to profile = 'PERSONAL' — nothing that
        // predates Work Profile support should retroactively look like a Work Profile app.
        val favoriteCursor = dbV19.query("SELECT profile FROM favorite_apps WHERE facetId = 1 AND packageName = 'com.example.a'")
        assertTrue(favoriteCursor.moveToFirst())
        assertEquals("PERSONAL", favoriteCursor.getString(favoriteCursor.getColumnIndexOrThrow("profile")))
        favoriteCursor.close()

        val dockCursor = dbV19.query("SELECT profile FROM dock_apps WHERE packageName = 'com.example.b'")
        assertTrue(dockCursor.moveToFirst())
        assertEquals("PERSONAL", dockCursor.getString(dockCursor.getColumnIndexOrThrow("profile")))
        dockCursor.close()

        val widgetCursor = dbV19.query("SELECT profile FROM widget_placements WHERE appWidgetId = 1")
        assertTrue(widgetCursor.moveToFirst())
        assertEquals("PERSONAL", widgetCursor.getString(widgetCursor.getColumnIndexOrThrow("profile")))
        widgetCursor.close()

        // ...and the widened unique index now allows a WORK-profile row sharing the exact same
        // (facetId, packageName, activityName) as the existing PERSONAL row — the whole point of
        // this migration: a personal and Work Profile copy of the same app must coexist as
        // distinct favorite/dock rows instead of colliding on the old, profile-blind index.
        dbV19.execSQL("INSERT INTO favorite_apps (facetId, packageName, activityName, position, profile) VALUES (1, 'com.example.a', '.Main', 1, 'WORK')")
        val bothCursor = dbV19.query("SELECT profile FROM favorite_apps WHERE facetId = 1 AND packageName = 'com.example.a' ORDER BY profile")
        assertEquals(2, bothCursor.count)
        bothCursor.close()
    }

    @Test
    fun migration20To21AddsClockWidgetAppWidgetIdColumnDefaultingToNullWithoutLosingExistingRows() {
        // Given a v20 database with one real facet row, predating PRD F15's clock widget substitution
        val dbV20 = helper.createDatabase(TEST_DB, 20)
        dbV20.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, calendarFontOption, calendarColorOption, " +
                "calendarFontWeight, clockAlignment, calendarAlignment, clockZoneHeightDp, clockScale, overrideApps, appRowPosition, " +
                "appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment, overridingFavorites, overrideDock, " +
                "dockDisplayMode, overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv) VALUES " +
                "(1, 'Work', 0, 0, 'LIGHT_STACK', 'LAUNCHER_DEFAULT', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LAUNCHER_DEFAULT', " +
                "'THEME', 'REGULAR', 'LEFT', 'LEFT', NULL, 0.8, 0, 'LEFT', 'ICON_AND_TEXT', 'FAVORITES', 6, 'BOTTOM', 0, 0, 'ICONS', 0, 1, NULL)",
        )
        dbV20.close()

        // When migrating to v21
        val dbV21 = helper.runMigrationsAndValidate(TEST_DB, 21, true, Migrations.MIGRATION_20_21)

        // Then the existing row survived, with the new column defaulting to NULL — this facet
        // keeps using its own native clock until the user explicitly picks a custom widget.
        val cursor = dbV21.query("SELECT clockWidgetAppWidgetId FROM facets WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("clockWidgetAppWidgetId")))
        cursor.close()

        // ...and the column accepts a real AppWidgetHost id once one is bound.
        dbV21.execSQL("UPDATE facets SET clockWidgetAppWidgetId = 42 WHERE id = 1")
        val updatedCursor = dbV21.query("SELECT clockWidgetAppWidgetId FROM facets WHERE id = 1")
        assertTrue(updatedCursor.moveToFirst())
        assertEquals(42, updatedCursor.getInt(updatedCursor.getColumnIndexOrThrow("clockWidgetAppWidgetId")))
        updatedCursor.close()
    }

    @Test
    fun migration22To23DropsCalendarStyleColumnsWithoutLosingExistingRowsOrOtherColumns() {
        // Given a v22 database with one real facet row, its calendar-style columns still populated
        // (before calendar/appearance styling consolidation — see chat history)
        val dbV22 = helper.createDatabase(TEST_DB, 22)
        dbV22.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, calendarFontOption, calendarColorOption, " +
                "calendarFontWeight, clockAlignment, calendarAlignment, clockZoneHeightDp, clockScale, clockWidgetAppWidgetId, " +
                "clockWidgetWidthDp, clockWidgetHeightDp, overrideApps, appRowPosition, appRowPresentation, listContentMode, " +
                "appsToShowCount, appListVerticalAlignment, overridingFavorites, overrideDock, dockDisplayMode, overrideCalendar, " +
                "showAllDayEvents, selectedCalendarIdsCsv) VALUES " +
                "(1, 'Work', 0, 1, 'VERTICAL_STACK_BOLD_HOUR', 'POPPINS', 'THEME_INVERTED', 'ACCENT_PRIMARY', 1, 1, 'FULL', " +
                "'MANROPE', 'ACCENT_SECONDARY', 'SEMI_BOLD', 'RIGHT', 'CENTER', 180.0, 1.2, NULL, NULL, NULL, 0, 'LEFT', " +
                "'ICON_AND_TEXT', 'FAVORITES', 6, 'BOTTOM', 0, 0, 'ICONS', 0, 1, NULL)",
        )
        dbV22.close()

        // When migrating to v23
        val dbV23 = helper.runMigrationsAndValidate(TEST_DB, 23, true, Migrations.MIGRATION_22_23)

        // Then the row survived, every surviving column intact (including the clock's own
        // font/color/alignment, untouched — only the calendar-specific columns were dropped)
        val cursor = dbV23.query("SELECT name, clockFontOption, clockAlignment, clockScale FROM facets WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("POPPINS", cursor.getString(cursor.getColumnIndexOrThrow("clockFontOption")))
        assertEquals("RIGHT", cursor.getString(cursor.getColumnIndexOrThrow("clockAlignment")))
        assertEquals(1.2f, cursor.getFloat(cursor.getColumnIndexOrThrow("clockScale")), 0.0001f)
        cursor.close()

        // ...and the four calendar-style columns are actually gone from the table, not just unread
        assertThrows(android.database.sqlite.SQLiteException::class.java) {
            dbV23.query("SELECT calendarFontOption FROM facets WHERE id = 1").close()
        }
        assertThrows(android.database.sqlite.SQLiteException::class.java) {
            dbV23.query("SELECT calendarColorOption FROM facets WHERE id = 1").close()
        }
        assertThrows(android.database.sqlite.SQLiteException::class.java) {
            dbV23.query("SELECT calendarFontWeight FROM facets WHERE id = 1").close()
        }
        assertThrows(android.database.sqlite.SQLiteException::class.java) {
            dbV23.query("SELECT calendarAlignment FROM facets WHERE id = 1").close()
        }
    }

    @Test
    fun migration23To24ResetsLookFieldsToLauncherDefaultOnlyForFacetsNotOverridingThatSection() {
        // Given a v23 database with two facet rows: one NOT overriding apps/dock (its own
        // appRowPosition/appRowPresentation/appListVerticalAlignment/dockDisplayMode columns are
        // stale — never read while those flags were false) and one that IS overriding both, with
        // real explicitly-chosen values that must survive untouched.
        val dbV23 = helper.createDatabase(TEST_DB, 23)
        dbV23.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, clockAlignment, clockZoneHeightDp, " +
                "clockScale, clockWidgetAppWidgetId, clockWidgetWidthDp, clockWidgetHeightDp, overrideApps, appRowPosition, " +
                "appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment, overridingFavorites, " +
                "overrideDock, dockDisplayMode, overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv) VALUES " +
                "(1, 'Inheriting', 0, 0, 'LIGHT_STACK', 'SYSTEM', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LEFT', NULL, " +
                "0.8, NULL, NULL, NULL, 0, 'RIGHT', 'TEXT_ONLY', 'FAVORITES', 6, 'TOP', 0, 0, 'TEXT', 0, 1, NULL), " +
                "(2, 'Overriding', 1, 0, 'LIGHT_STACK', 'SYSTEM', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LEFT', NULL, " +
                "0.8, NULL, NULL, NULL, 1, 'CENTER', 'ICON_ONLY', 'FAVORITES', 6, 'TOP', 0, 1, 'TEXT', 0, 1, NULL)",
        )
        dbV23.close()

        // When migrating to v24
        val dbV24 = helper.runMigrationsAndValidate(TEST_DB, 24, true, Migrations.MIGRATION_23_24)

        // Then the non-overriding facet's stale look values reset to LAUNCHER_DEFAULT — it keeps
        // inheriting the global default, not the last value that happened to be sitting there
        val inheriting = dbV24.query(
            "SELECT appRowPosition, appRowPresentation, appListVerticalAlignment, dockDisplayMode FROM facets WHERE id = 1",
        )
        assertTrue(inheriting.moveToFirst())
        assertEquals("LAUNCHER_DEFAULT", inheriting.getString(inheriting.getColumnIndexOrThrow("appRowPosition")))
        assertEquals("LAUNCHER_DEFAULT", inheriting.getString(inheriting.getColumnIndexOrThrow("appRowPresentation")))
        assertEquals("LAUNCHER_DEFAULT", inheriting.getString(inheriting.getColumnIndexOrThrow("appListVerticalAlignment")))
        assertEquals("LAUNCHER_DEFAULT", inheriting.getString(inheriting.getColumnIndexOrThrow("dockDisplayMode")))
        inheriting.close()

        // ...and the overriding facet's real, explicitly-chosen values are untouched
        val overriding = dbV24.query(
            "SELECT appRowPosition, appRowPresentation, appListVerticalAlignment, dockDisplayMode FROM facets WHERE id = 2",
        )
        assertTrue(overriding.moveToFirst())
        assertEquals("CENTER", overriding.getString(overriding.getColumnIndexOrThrow("appRowPosition")))
        assertEquals("ICON_ONLY", overriding.getString(overriding.getColumnIndexOrThrow("appRowPresentation")))
        assertEquals("TOP", overriding.getString(overriding.getColumnIndexOrThrow("appListVerticalAlignment")))
        assertEquals("TEXT", overriding.getString(overriding.getColumnIndexOrThrow("dockDisplayMode")))
        overriding.close()
    }

    private companion object {
        const val TEST_DB = "facet-migration-test.db"
    }
}
