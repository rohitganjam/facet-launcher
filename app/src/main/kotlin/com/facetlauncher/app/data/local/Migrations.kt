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

    /**
     * Folders (F-Folders): a folder is a first-class, independent, global entity (`folders` +
     * its membership `folder_apps`) with no position of its own — it can be placed into any of
     * the four existing Dock/Favorites slot lists via four purely-additive placement tables,
     * each just a (folder, position) pointer merged in Kotlin against its own list's app rows.
     * A 0-app folder is valid and persists — nothing here auto-deletes on empty membership.
     */
    val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `folders` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `folder_apps` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`folderId` INTEGER NOT NULL, " +
                    "`packageName` TEXT NOT NULL, " +
                    "`activityName` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_folder_apps_folderId_packageName_activityName` " +
                    "ON `folder_apps` (`folderId`, `packageName`, `activityName`)",
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `dock_folder_placements` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`folderId` INTEGER NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_dock_folder_placements_folderId` " +
                    "ON `dock_folder_placements` (`folderId`)",
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `default_favorite_folder_placements` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`folderId` INTEGER NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_default_favorite_folder_placements_folderId` " +
                    "ON `default_favorite_folder_placements` (`folderId`)",
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `facet_dock_folder_placements` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`facetId` INTEGER NOT NULL, " +
                    "`folderId` INTEGER NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`facetId`) REFERENCES `facets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_facet_dock_folder_placements_facetId_folderId` " +
                    "ON `facet_dock_folder_placements` (`facetId`, `folderId`)",
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `favorite_folder_placements` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`facetId` INTEGER NOT NULL, " +
                    "`folderId` INTEGER NOT NULL, " +
                    "`position` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`facetId`) REFERENCES `facets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_favorite_folder_placements_facetId_folderId` " +
                    "ON `favorite_folder_placements` (`facetId`, `folderId`)",
            )
        }
    }

    /**
     * Android Work Profile support: every place an app is identified by (packageName,
     * activityName) also needs to know *which* Android user it came from, or a personal and a
     * Work Profile copy of the same app (identical package + activity, different user) collide as
     * the same row. Adds a `profile` column (`'PERSONAL'`/`'WORK'`, `NOT NULL DEFAULT 'PERSONAL'`
     * — every existing row predates Work Profile support, so it's unambiguously personal) to every
     * table keyed that way, and widens their unique indices to include it. `widget_placements` is
     * keyed on the real system `appWidgetId` instead, so it only needs the plain column, no index
     * change.
     */
    val MIGRATION_18_19: Migration = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE favorite_apps ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_favorite_apps_facetId_packageName_activityName")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_apps_facetId_packageName_activityName_profile " +
                    "ON favorite_apps (facetId, packageName, activityName, profile)",
            )

            db.execSQL("ALTER TABLE facet_dock_apps ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_facet_dock_apps_facetId_packageName_activityName")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_facet_dock_apps_facetId_packageName_activityName_profile " +
                    "ON facet_dock_apps (facetId, packageName, activityName, profile)",
            )

            db.execSQL("ALTER TABLE dock_apps ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_dock_apps_packageName_activityName")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_dock_apps_packageName_activityName_profile " +
                    "ON dock_apps (packageName, activityName, profile)",
            )

            db.execSQL("ALTER TABLE default_favorite_apps ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_default_favorite_apps_packageName_activityName")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_default_favorite_apps_packageName_activityName_profile " +
                    "ON default_favorite_apps (packageName, activityName, profile)",
            )

            db.execSQL("ALTER TABLE folder_apps ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_folder_apps_folderId_packageName_activityName")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_folder_apps_folderId_packageName_activityName_profile " +
                    "ON folder_apps (folderId, packageName, activityName, profile)",
            )

            db.execSQL("ALTER TABLE widget_placements ADD COLUMN profile TEXT NOT NULL DEFAULT 'PERSONAL'")
        }
    }

    /**
     * `profile` (PERSONAL/WORK/PRIVATE/OTHER) turned out to not be a safe identity key on its
     * own: two distinct real Android user profiles can land on the same `profile` value (e.g. a
     * genuine Work Profile and an OEM clone/dual-app profile both classify as WORK below API 35,
     * where `LauncherApps.getLauncherUserInfo` doesn't exist to tell them apart) — so favoriting
     * one could silently collide with the other in these unique indices, and launch routing could
     * resolve to the wrong one. Adds `userId` (the real `UserHandle.hashCode()` — AOSP's own
     * implementation returns its internal per-user int id verbatim, and this is the standard
     * workaround third-party launchers use since `UserHandle.getIdentifier()` itself is hidden
     * API, not part of the public SDK) as the actual identity key, moving the unique indices from
     * `(..., profile)` to `(..., userId)`; `profile` stays as a plain column for display/backup.
     * The calling (primary) process's own `UserHandle` hashes to `0` (`UserHandle.USER_SYSTEM`)
     * — this launcher never runs inside a Work Profile/clone profile itself — so every existing
     * `'PERSONAL'` row can be backfilled deterministically here.
     * Non-PERSONAL rows are left at the `-1` sentinel: their real historical `UserHandle` was
     * never stored pre-v20, so pure SQL can't recover it — see `RepairOrphanedProfileRowsUseCase`
     * for the one-time startup backfill against whichever live handle currently matches.
     */
    val MIGRATION_19_20: Migration = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE favorite_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE favorite_apps SET userId = 0 WHERE profile = 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_favorite_apps_facetId_packageName_activityName_profile")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_apps_facetId_packageName_activityName_userId " +
                    "ON favorite_apps (facetId, packageName, activityName, userId)",
            )

            db.execSQL("ALTER TABLE facet_dock_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE facet_dock_apps SET userId = 0 WHERE profile = 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_facet_dock_apps_facetId_packageName_activityName_profile")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_facet_dock_apps_facetId_packageName_activityName_userId " +
                    "ON facet_dock_apps (facetId, packageName, activityName, userId)",
            )

            db.execSQL("ALTER TABLE dock_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE dock_apps SET userId = 0 WHERE profile = 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_dock_apps_packageName_activityName_profile")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_dock_apps_packageName_activityName_userId " +
                    "ON dock_apps (packageName, activityName, userId)",
            )

            db.execSQL("ALTER TABLE default_favorite_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE default_favorite_apps SET userId = 0 WHERE profile = 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_default_favorite_apps_packageName_activityName_profile")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_default_favorite_apps_packageName_activityName_userId " +
                    "ON default_favorite_apps (packageName, activityName, userId)",
            )

            db.execSQL("ALTER TABLE folder_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE folder_apps SET userId = 0 WHERE profile = 'PERSONAL'")
            db.execSQL("DROP INDEX IF EXISTS index_folder_apps_folderId_packageName_activityName_profile")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_folder_apps_folderId_packageName_activityName_userId " +
                    "ON folder_apps (folderId, packageName, activityName, userId)",
            )

            db.execSQL("ALTER TABLE widget_placements ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            db.execSQL("UPDATE widget_placements SET userId = 0 WHERE profile = 'PERSONAL'")
        }
    }

    /** Adds `facets.clockWidgetAppWidgetId` (nullable `INTEGER`) — PRD F15, see [FacetEntity.clockWidgetAppWidgetId]'s own doc. `NULL` is itself the meaningful default ("this facet uses its native clock"), not a placeholder. */
    val MIGRATION_20_21: Migration = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE facets ADD COLUMN clockWidgetAppWidgetId INTEGER DEFAULT NULL")
        }
    }

    /** Adds `facets.clockWidgetWidthDp`/`clockWidgetHeightDp` (nullable `INTEGER`) — PRD F15's interactive resize, see [FacetEntity.clockWidgetWidthDp]'s own doc. `NULL` means "not yet resized — use the provider's own declared minimum", not a placeholder. */
    val MIGRATION_21_22: Migration = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE facets ADD COLUMN clockWidgetWidthDp INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE facets ADD COLUMN clockWidgetHeightDp INTEGER DEFAULT NULL")
        }
    }

    /**
     * Removes `facets.calendarFontOption`/`calendarColorOption`/`calendarFontWeight`/
     * `calendarAlignment` — the calendar events strip no longer has independent styling; it reads
     * Appearance's home-text settings and the clock's own `clockAlignment` instead (see chat
     * history: calendar/appearance styling consolidation). SQLite `DROP COLUMN` needs 3.35+, not
     * reliably available across this app's minSdk 31 devices, so — same
     * create-copy-drop-rename pattern MIGRATION_16_17 used, and for the same reason — the table is
     * recreated without the four columns rather than altered in place. `facets` has no indices or
     * declared foreign keys of its own to recreate; the child tables that reference it by name
     * (`facet_dock_apps`, `favorite_apps`, etc.) are unaffected since the table's *name* doesn't
     * change, only its column set — unlike MIGRATION_16_17's rename, where the name itself changed.
     */
    val MIGRATION_22_23: Migration = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE facets_new (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `position` INTEGER NOT NULL, " +
                    "`overrideClock` INTEGER NOT NULL, `clockTemplateId` TEXT NOT NULL, `clockFontOption` TEXT NOT NULL, " +
                    "`clockColorOption` TEXT NOT NULL, `clockAccentColorOption` TEXT NOT NULL, `use24HourTime` INTEGER NOT NULL, " +
                    "`clockShowMeridiem` INTEGER NOT NULL, `clockDateStyle` TEXT NOT NULL, `clockAlignment` TEXT NOT NULL, " +
                    "`clockZoneHeightDp` REAL, `clockScale` REAL NOT NULL, `clockWidgetAppWidgetId` INTEGER, " +
                    "`clockWidgetWidthDp` INTEGER, `clockWidgetHeightDp` INTEGER, `overrideApps` INTEGER NOT NULL, " +
                    "`appRowPosition` TEXT NOT NULL, `appRowPresentation` TEXT NOT NULL, `listContentMode` TEXT NOT NULL, " +
                    "`appsToShowCount` INTEGER NOT NULL, `appListVerticalAlignment` TEXT NOT NULL, `overridingFavorites` INTEGER NOT NULL, " +
                    "`overrideDock` INTEGER NOT NULL, `dockDisplayMode` TEXT NOT NULL, `overrideCalendar` INTEGER NOT NULL, " +
                    "`showAllDayEvents` INTEGER NOT NULL, `selectedCalendarIdsCsv` TEXT)",
            )
            db.execSQL(
                "INSERT INTO facets_new (id, name, position, overrideClock, clockTemplateId, clockFontOption, " +
                    "clockColorOption, clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, " +
                    "clockAlignment, clockZoneHeightDp, clockScale, clockWidgetAppWidgetId, clockWidgetWidthDp, " +
                    "clockWidgetHeightDp, overrideApps, appRowPosition, appRowPresentation, listContentMode, " +
                    "appsToShowCount, appListVerticalAlignment, overridingFavorites, overrideDock, dockDisplayMode, " +
                    "overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv) " +
                    "SELECT id, name, position, overrideClock, clockTemplateId, clockFontOption, " +
                    "clockColorOption, clockAccentColorOption, use24HourTime, clockShowMeridiem, clockDateStyle, " +
                    "clockAlignment, clockZoneHeightDp, clockScale, clockWidgetAppWidgetId, clockWidgetWidthDp, " +
                    "clockWidgetHeightDp, overrideApps, appRowPosition, appRowPresentation, listContentMode, " +
                    "appsToShowCount, appListVerticalAlignment, overridingFavorites, overrideDock, dockDisplayMode, " +
                    "overrideCalendar, showAllDayEvents, selectedCalendarIdsCsv FROM facets",
            )
            db.execSQL("DROP TABLE facets")
            db.execSQL("ALTER TABLE facets_new RENAME TO facets")
        }
    }

    /**
     * Decouples `facets.appRowPosition`/`appRowPresentation`/`appListVerticalAlignment`/
     * `dockDisplayMode` from the whole-block `overrideApps`/`overrideDock` gate — these four
     * "look" fields are edited from Settings → Appearance now, independently of Dock's/Apps-list's
     * own content (see chat history: dock/favorites display controls moved to Appearance with a
     * per-field `LAUNCHER_DEFAULT` sentinel, mirroring `clockFontOption`'s existing one). No column
     * type changes — `LAUNCHER_DEFAULT` is just a new valid string for these already-`TEXT NOT NULL`
     * columns — but a data migration is still needed: a facet that was NOT overriding apps/dock had
     * its `appRowPosition`/etc columns sitting at whatever stale default they were created with
     * (unread, since resolution ignored them while `overrideApps`/`overrideDock` was `false`).
     * Leaving that stale literal value in place after this change would make the facet silently
     * start "overriding" with that value instead of continuing to inherit — so every row where the
     * matching flag is `0` gets reset to `'LAUNCHER_DEFAULT'` here; a row that *was* overriding
     * keeps its real chosen value untouched.
     */
    val MIGRATION_23_24: Migration = object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "UPDATE facets SET appRowPosition = 'LAUNCHER_DEFAULT', appRowPresentation = 'LAUNCHER_DEFAULT', " +
                    "appListVerticalAlignment = 'LAUNCHER_DEFAULT' WHERE overrideApps = 0",
            )
            db.execSQL("UPDATE facets SET dockDisplayMode = 'LAUNCHER_DEFAULT' WHERE overrideDock = 0")
        }
    }

    /**
     * Adds Home app list layout (single column/two columns/grid) and its two mode-specific "look"
     * fields — brand-new columns, so (unlike [MIGRATION_23_24]) there's no prior stored value to
     * reset: every existing row gets `'LAUNCHER_DEFAULT'` straight away, same as any other
     * `LAUNCHER_DEFAULT`-sentinel field's first-ever add (see [MIGRATION_15_16]'s own `dockDisplayMode`
     * add for the shape, though that one predates the sentinel convention and used a concrete default).
     */
    val MIGRATION_24_25: Migration = object : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE facets ADD COLUMN appListLayout TEXT NOT NULL DEFAULT 'LAUNCHER_DEFAULT'")
            db.execSQL("ALTER TABLE facets ADD COLUMN appListColumnAlignment TEXT NOT NULL DEFAULT 'LAUNCHER_DEFAULT'")
            db.execSQL("ALTER TABLE facets ADD COLUMN appListGridColumns TEXT NOT NULL DEFAULT 'LAUNCHER_DEFAULT'")
            db.execSQL("ALTER TABLE facets ADD COLUMN appListGridDisplayMode TEXT NOT NULL DEFAULT 'LAUNCHER_DEFAULT'")
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
        MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22,
        MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
    )
}
