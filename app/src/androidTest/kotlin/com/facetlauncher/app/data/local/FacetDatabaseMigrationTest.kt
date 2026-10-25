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

    @Test
    fun migration24To25AddsAppListLayoutColumnsDefaultingToLauncherDefault() {
        // Given a v24 database with one real facet row (every column NOT NULL at that version)
        val dbV24 = helper.createDatabase(TEST_DB, 24)
        dbV24.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, clockAlignment, clockZoneHeightDp, " +
                "clockScale, clockWidgetAppWidgetId, clockWidgetWidthDp, clockWidgetHeightDp, overrideApps, appRowPosition, " +
                "appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment, overridingFavorites, " +
                "overrideDock, dockDisplayMode, overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv) VALUES " +
                "(1, 'Work', 0, 0, 'LIGHT_STACK', 'SYSTEM', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LEFT', NULL, " +
                "0.8, NULL, NULL, NULL, 0, 'RIGHT', 'TEXT_ONLY', 'FAVORITES', 6, 'TOP', 0, 0, 'TEXT', 0, 1, NULL)",
        )
        dbV24.close()

        // When migrating to v25
        val dbV25 = helper.runMigrationsAndValidate(TEST_DB, 25, true, Migrations.MIGRATION_24_25)

        // Then the existing row survived (unrelated columns untouched), and the 4 new columns
        // default to LAUNCHER_DEFAULT — every pre-existing facet keeps inheriting the global
        // layout/column-alignment/grid settings rather than landing on some arbitrary real value.
        val cursor = dbV25.query(
            "SELECT name, appRowPosition, appListLayout, appListColumnAlignment, appListGridColumns, appListGridDisplayMode FROM facets WHERE id = 1",
        )
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("RIGHT", cursor.getString(cursor.getColumnIndexOrThrow("appRowPosition")))
        assertEquals("LAUNCHER_DEFAULT", cursor.getString(cursor.getColumnIndexOrThrow("appListLayout")))
        assertEquals("LAUNCHER_DEFAULT", cursor.getString(cursor.getColumnIndexOrThrow("appListColumnAlignment")))
        assertEquals("LAUNCHER_DEFAULT", cursor.getString(cursor.getColumnIndexOrThrow("appListGridColumns")))
        assertEquals("LAUNCHER_DEFAULT", cursor.getString(cursor.getColumnIndexOrThrow("appListGridDisplayMode")))
        cursor.close()
    }

    @Test
    fun migration25To26CreatesAutomationRulesTableWithItsForeignKeys() {
        // Given a v25 database with two facets
        val dbV25 = helper.createDatabase(TEST_DB, 25)
        insertFacetV25(dbV25, id = 1, name = "Work")
        insertFacetV25(dbV25, id = 2, name = "Home")
        dbV25.close()

        // When migrating to v26 (Room also validates the result against the exported v26 schema)
        val dbV26 = helper.runMigrationsAndValidate(TEST_DB, 26, true, Migrations.MIGRATION_25_26)

        // Then the existing facets survived and the new table accepts a rule that references them
        val facets = dbV26.query("SELECT COUNT(*) FROM facets")
        assertTrue(facets.moveToFirst())
        assertEquals(2, facets.getInt(0))
        facets.close()
        dbV26.execSQL(
            "INSERT INTO automation_rules (id, position, enabled, targetFacetId, endBehavior, endFacetId, triggerType, " +
                "negated, scheduleDays, scheduleStartMinute, scheduleEndMinute, deviceAddress, deviceName, wifiSsid, batteryThreshold, " +
                "batteryDirection) " +
                "VALUES (1, 0, 1, 1, 'SWITCH_TO', 2, 'SCHEDULE', 0, 31, 540, 1080, NULL, NULL, NULL, NULL, NULL)",
        )

        // And deleting the end facet clears it, while deleting the target facet removes the rule
        // (MigrationTestHelper's database leaves foreign keys off, unlike the Room-built one)
        dbV26.execSQL("PRAGMA foreign_keys = ON")
        dbV26.execSQL("DELETE FROM facets WHERE id = 2")
        val afterEndFacetDelete = dbV26.query("SELECT endFacetId FROM automation_rules WHERE id = 1")
        assertTrue(afterEndFacetDelete.moveToFirst())
        assertTrue(afterEndFacetDelete.isNull(0))
        afterEndFacetDelete.close()
        dbV26.execSQL("DELETE FROM facets WHERE id = 1")
        val remaining = dbV26.query("SELECT COUNT(*) FROM automation_rules")
        assertTrue(remaining.moveToFirst())
        assertEquals(0, remaining.getInt(0))
        remaining.close()
    }

    @Test
    fun migration26To27AddsCalendarDisplayColumnsDefaultingToIndicatorOnAndThreeItems() {
        // Given a v26 database with an existing facet (v26 only added a table, so the v25 row shape is valid)
        val dbV26 = helper.createDatabase(TEST_DB, 26)
        insertFacetV25(dbV26, id = 1, name = "Work")
        dbV26.close()

        // When migrating to v27 (Room also validates the result against the exported v27 schema)
        val dbV27 = helper.runMigrationsAndValidate(TEST_DB, 27, true, Migrations.MIGRATION_26_27)

        // Then the existing facet keeps its data and gets the old behaviour: indicator on, 3 items
        val cursor = dbV27.query("SELECT name, showCalendarIndicator, calendarItemsToShow FROM facets WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("Work", cursor.getString(0))
        assertEquals(1, cursor.getInt(1))
        assertEquals(3, cursor.getInt(2))
        cursor.close()
    }

    @Test
    fun migration27To28FoldsTheEightPlacementTablesIntoTwoWithoutLosingOrMisfilingAnyRow() {
        // Given a v27 database with facets 1 and 2, folders 10 and 11, and rows in all eight old tables
        val dbV27 = helper.createDatabase(TEST_DB, 27)
        insertFacetV27(dbV27, id = 1, name = "Work")
        insertFacetV27(dbV27, id = 2, name = "Home")
        dbV27.execSQL("INSERT INTO folders (id, name) VALUES (10, 'Social'), (11, 'Tools')")
        // (id, pkg, activity, position, profile, userId) — a work-profile row (userId 10) is included on purpose
        dbV27.execSQL("INSERT INTO dock_apps (packageName, activityName, position, profile, userId) VALUES ('d.one', '.A', 0, 'PERSONAL', 0), ('d.work', '.A', 1, 'WORK', 10)")
        dbV27.execSQL("INSERT INTO facet_dock_apps (facetId, packageName, activityName, position, profile, userId) VALUES (1, 'fd.one', '.A', 0, 'PERSONAL', 0), (2, 'fd.two', '.A', 0, 'PERSONAL', -1)")
        dbV27.execSQL("INSERT INTO favorite_apps (facetId, packageName, activityName, position, profile, userId) VALUES (1, 'f.one', '.A', 0, 'PERSONAL', 0), (1, 'f.two', '.A', 1, 'WORK', 10)")
        dbV27.execSQL("INSERT INTO default_favorite_apps (packageName, activityName, position, profile, userId) VALUES ('df.one', '.A', 0, 'PERSONAL', 0), ('df.two', '.A', 1, 'PERSONAL', 0)")
        dbV27.execSQL("INSERT INTO dock_folder_placements (folderId, position) VALUES (10, 2)")
        dbV27.execSQL("INSERT INTO facet_dock_folder_placements (facetId, folderId, position) VALUES (1, 10, 1)")
        dbV27.execSQL("INSERT INTO favorite_folder_placements (facetId, folderId, position) VALUES (1, 11, 2), (2, 10, 0)")
        dbV27.execSQL("INSERT INTO default_favorite_folder_placements (folderId, position) VALUES (11, 2)")
        dbV27.close()

        // When migrating to v28 (Room also validates the result against the exported v28 schema)
        val dbV28 = helper.runMigrationsAndValidate(TEST_DB, 28, true, Migrations.MIGRATION_27_28)

        // Then every app row is in the right list and owner, with its fields and order intact
        fun apps(): List<String> {
            val cursor = dbV28.query("SELECT list, facetId, ownerKey, packageName, activityName, position, profile, userId FROM app_placements ORDER BY list, ownerKey, position")
            val rows = mutableListOf<String>()
            while (cursor.moveToNext()) {
                val facet = if (cursor.isNull(1)) "null" else cursor.getLong(1).toString()
                rows += "${cursor.getString(0)}|$facet|${cursor.getLong(2)}|${cursor.getString(3)}|${cursor.getString(4)}|${cursor.getInt(5)}|${cursor.getString(6)}|${cursor.getInt(7)}"
            }
            cursor.close()
            return rows
        }
        assertEquals(
            listOf(
                "DOCK|null|0|d.one|.A|0|PERSONAL|0",
                "DOCK|null|0|d.work|.A|1|WORK|10",
                "DOCK|1|1|fd.one|.A|0|PERSONAL|0",
                "DOCK|2|2|fd.two|.A|0|PERSONAL|-1",
                "FAVORITES|null|0|df.one|.A|0|PERSONAL|0",
                "FAVORITES|null|0|df.two|.A|1|PERSONAL|0",
                "FAVORITES|1|1|f.one|.A|0|PERSONAL|0",
                "FAVORITES|1|1|f.two|.A|1|WORK|10",
            ),
            apps(),
        )

        // And every folder placement is in the right list and owner
        val folderCursor = dbV28.query("SELECT list, facetId, ownerKey, folderId, position FROM folder_placements ORDER BY list, ownerKey, folderId")
        val folderRows = mutableListOf<String>()
        while (folderCursor.moveToNext()) {
            val facet = if (folderCursor.isNull(1)) "null" else folderCursor.getLong(1).toString()
            folderRows += "${folderCursor.getString(0)}|$facet|${folderCursor.getLong(2)}|${folderCursor.getLong(3)}|${folderCursor.getInt(4)}"
        }
        folderCursor.close()
        assertEquals(
            listOf("DOCK|null|0|10|2", "DOCK|1|1|10|1", "FAVORITES|null|0|11|2", "FAVORITES|1|1|11|2", "FAVORITES|2|2|10|0"),
            folderRows,
        )

        // And the eight old tables are gone
        val tables = dbV28.query("SELECT name FROM sqlite_master WHERE type = 'table'")
        val names = mutableSetOf<String>()
        while (tables.moveToNext()) names += tables.getString(0)
        tables.close()
        assertTrue(names.intersect(OLD_PLACEMENT_TABLES).isEmpty())

        // And deleting a facet still cascades to its rows, leaving the launcher-wide ones alone
        dbV28.execSQL("PRAGMA foreign_keys = ON")
        dbV28.execSQL("DELETE FROM facets WHERE id = 1")
        assertEquals(listOf("DOCK|null|0|d.one|.A|0|PERSONAL|0", "DOCK|null|0|d.work|.A|1|WORK|10", "DOCK|2|2|fd.two|.A|0|PERSONAL|-1", "FAVORITES|null|0|df.one|.A|0|PERSONAL|0", "FAVORITES|null|0|df.two|.A|1|PERSONAL|0"), apps())
    }

    @Test
    fun migration28To29AddsAnEmptyLandscapeOrderToEveryWidgetWithoutChangingThem() {
        // Given a v28 database with two placed widgets (one a work-profile widget)
        val dbV28 = helper.createDatabase(TEST_DB, 28)
        dbV28.execSQL(
            "INSERT INTO widget_placements (appWidgetId, providerPackageName, providerClassName, row, col, colSpan, rowSpan, profile, userId) " +
                "VALUES (1, 'com.example.a', '.A', 0, 0, 2, 2, 'PERSONAL', 0), (2, 'com.example.b', '.B', 2, 1, 3, 1, 'WORK', 10)",
        )
        dbV28.close()

        // When migrating to v29 (Room also validates the result against the exported v29 schema)
        val dbV29 = helper.runMigrationsAndValidate(TEST_DB, 29, true, Migrations.MIGRATION_28_29)

        // Then both widgets are unchanged and neither has a landscape order yet
        val cursor = dbV29.query("SELECT appWidgetId, row, col, colSpan, rowSpan, profile, userId, landscapeOrder FROM widget_placements ORDER BY appWidgetId")
        val rows = mutableListOf<String>()
        while (cursor.moveToNext()) {
            rows += "${cursor.getInt(0)}|${cursor.getInt(1)}|${cursor.getInt(2)}|${cursor.getInt(3)}|${cursor.getInt(4)}|${cursor.getString(5)}|${cursor.getInt(6)}|${cursor.isNull(7)}"
        }
        cursor.close()
        assertEquals(listOf("1|0|0|2|2|PERSONAL|0|true", "2|2|1|3|1|WORK|10|true"), rows)

        // And an order can be written afterwards
        dbV29.execSQL("UPDATE widget_placements SET landscapeOrder = 4 WHERE appWidgetId = 2")
        val ordered = dbV29.query("SELECT landscapeOrder FROM widget_placements WHERE appWidgetId = 2")
        ordered.moveToFirst()
        assertEquals(4, ordered.getInt(0))
        ordered.close()
    }

    @Test
    fun migration29To30AddsEmptyLandscapeClockWidgetSizesToEveryFacetWithoutChangingThem() {
        // Given a v29 database with a facet that has a portrait clock widget size
        val dbV29 = helper.createDatabase(TEST_DB, 29)
        insertFacetV27(dbV29, id = 1, name = "Work")
        dbV29.execSQL("UPDATE facets SET clockWidgetAppWidgetId = 7, clockWidgetWidthDp = 180, clockWidgetHeightDp = 90 WHERE id = 1")
        dbV29.close()

        // When migrating to v30 (Room also validates the result against the exported v30 schema)
        val dbV30 = helper.runMigrationsAndValidate(TEST_DB, 30, true, Migrations.MIGRATION_29_30)

        // Then the portrait size is kept and there is no landscape size yet
        val cursor = dbV30.query("SELECT clockWidgetWidthDp, clockWidgetHeightDp, landscapeClockWidgetWidthDp, landscapeClockWidgetHeightDp FROM facets WHERE id = 1")
        cursor.moveToFirst()
        assertEquals(180, cursor.getInt(0))
        assertEquals(90, cursor.getInt(1))
        assertTrue(cursor.isNull(2) && cursor.isNull(3))
        cursor.close()
    }

    @Test
    fun migration30To31AddsEmptyLandscapeClockPlacementToEveryFacetWithoutChangingIt() {
        // Given a v30 database with a facet that has a portrait clock placement
        val dbV30 = helper.createDatabase(TEST_DB, 30)
        insertFacetV27(dbV30, id = 1, name = "Work")
        dbV30.execSQL("UPDATE facets SET clockAlignment = 'RIGHT', clockZoneHeightDp = 240.0, clockScale = 1.3 WHERE id = 1")
        dbV30.close()

        // When migrating to v31 (Room also validates the result against the exported v31 schema)
        val dbV31 = helper.runMigrationsAndValidate(TEST_DB, 31, true, Migrations.MIGRATION_30_31)

        // Then the portrait placement is kept and the landscape one is empty
        val cursor = dbV31.query("SELECT clockAlignment, clockZoneHeightDp, clockScale, landscapeClockAlignment, landscapeClockZoneHeightDp, landscapeClockScale FROM facets WHERE id = 1")
        cursor.moveToFirst()
        assertEquals("RIGHT", cursor.getString(0))
        assertEquals(240.0, cursor.getDouble(1), 0.001)
        assertEquals(1.3, cursor.getDouble(2), 0.001)
        assertTrue(cursor.isNull(3) && cursor.isNull(4) && cursor.isNull(5))
        cursor.close()
    }

    @Test
    fun migration31To32AddsEmptyLandscapeListAlignmentToEveryFacetWithoutChangingIt() {
        // Given a v31 database with a facet that has a portrait list alignment
        val dbV31 = helper.createDatabase(TEST_DB, 31)
        insertFacetV27(dbV31, id = 1, name = "Work")
        dbV31.execSQL("UPDATE facets SET appRowPosition = 'RIGHT', appListVerticalAlignment = 'TOP' WHERE id = 1")
        dbV31.close()

        // When migrating to v32 (Room also validates the result against the exported v32 schema)
        val dbV32 = helper.runMigrationsAndValidate(TEST_DB, 32, true, Migrations.MIGRATION_31_32)

        // Then the portrait alignment is kept and the landscape one is empty
        val cursor = dbV32.query("SELECT appRowPosition, appListVerticalAlignment, landscapeAppRowPosition, landscapeAppListVerticalAlignment, landscapeAppListColumnAlignment FROM facets WHERE id = 1")
        cursor.moveToFirst()
        assertEquals("RIGHT", cursor.getString(0))
        assertEquals("TOP", cursor.getString(1))
        assertTrue(cursor.isNull(2) && cursor.isNull(3) && cursor.isNull(4))
        cursor.close()
    }

    @Test
    fun migration32To33KeepsFacetsLookingTheSameAndAddsEmptyWeatherTables() {
        // Given a v32 database with a facet
        val dbV32 = helper.createDatabase(TEST_DB, 32)
        insertFacetV27(dbV32, id = 1, name = "Work")
        dbV32.close()

        // When migrating to v33 (Room also validates the result against the exported v33 schema)
        val dbV33 = helper.runMigrationsAndValidate(TEST_DB, 33, true, Migrations.MIGRATION_32_33)

        // Then the facet shows the alarm and battery and not weather, as before
        val facet = dbV33.query("SELECT clockShowAlarm, clockShowBattery, clockShowWeather FROM facets WHERE id = 1")
        facet.moveToFirst()
        assertEquals(1, facet.getInt(0))
        assertEquals(1, facet.getInt(1))
        assertEquals(0, facet.getInt(2))
        facet.close()

        // And both weather tables exist and are empty
        for (table in listOf("weather_hours", "weather_meta")) {
            val rows = dbV33.query("SELECT COUNT(*) FROM $table")
            rows.moveToFirst()
            assertEquals(0, rows.getInt(0))
            rows.close()
        }
    }

    /** A v27 facet row — the v25 shape plus the two calendar-display columns, which a freshly created database has no defaults for. */
    private fun insertFacetV27(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long, name: String) {
        insertFacetV25Columns(db, id, name, extraColumns = ", showCalendarIndicator, calendarItemsToShow", extraValues = ", 1, 3")
    }

    private fun insertFacetV25(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long, name: String) {
        insertFacetV25Columns(db, id, name, extraColumns = "", extraValues = "")
    }

    private fun insertFacetV25Columns(db: androidx.sqlite.db.SupportSQLiteDatabase, id: Long, name: String, extraColumns: String, extraValues: String) {
        db.execSQL(
            "INSERT INTO facets (id, name, position, overrideClock, clockTemplateId, clockFontOption, clockColorOption, " +
                "clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, clockAlignment, clockZoneHeightDp, " +
                "clockScale, clockWidgetAppWidgetId, clockWidgetWidthDp, clockWidgetHeightDp, overrideApps, appRowPosition, " +
                "appRowPresentation, listContentMode, appsToShowCount, appListVerticalAlignment, overridingFavorites, " +
                "overrideDock, dockDisplayMode, overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv, appListLayout, " +
                "appListColumnAlignment, appListGridColumns, appListGridDisplayMode$extraColumns) VALUES " +
                "($id, '$name', $id, 0, 'LIGHT_STACK', 'SYSTEM', 'THEME', 'ACCENT_PRIMARY', 0, 0, 'FULL', 'LEFT', NULL, " +
                "0.8, NULL, NULL, NULL, 0, 'LAUNCHER_DEFAULT', 'LAUNCHER_DEFAULT', 'FAVORITES', 6, 'LAUNCHER_DEFAULT', 0, 0, " +
                "'LAUNCHER_DEFAULT', 0, 1, NULL, 'LAUNCHER_DEFAULT', 'LAUNCHER_DEFAULT', 'LAUNCHER_DEFAULT', 'LAUNCHER_DEFAULT'$extraValues)",
        )
    }

    private companion object {
        const val TEST_DB = "facet-migration-test.db"
        val OLD_PLACEMENT_TABLES = setOf(
            "dock_apps", "facet_dock_apps", "favorite_apps", "default_favorite_apps",
            "dock_folder_placements", "facet_dock_folder_placements", "favorite_folder_placements", "default_favorite_folder_placements",
        )
    }
}
