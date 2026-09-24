package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * [LAUNCHER_DEFAULT] is facet-only (filtered out of the option list at global scope, same
 * technique as [com.facetlauncher.app.data.model.ClockFontOption]'s own sentinel) — a facet
 * carrying it inherits whatever [LauncherSettings.dockDisplayMode] currently is, live, rather
 * than being pinned to a value copied at the moment it stopped overriding. Editable from
 * Settings → Appearance now, not Dock's own screen (see chat history — dock/app "look" controls
 * moved out of their content screens into Appearance, alongside [AppRowPosition]/
 * [AppRowPresentation]/[AppListVerticalAlignment]); [com.facetlauncher.app.data.local.FacetEntity.dockDisplayMode]'s
 * resolution no longer goes through `overrideDock` (that flag still gates the dock's own app
 * list) — see [com.facetlauncher.app.data.local.resolveSentinel].
 */
enum class DockDisplayMode(@param:StringRes val displayNameRes: Int) {
    LAUNCHER_DEFAULT(R.string.dock_display_mode_launcher_default),
    ICONS(R.string.dock_display_mode_icons),
    TEXT(R.string.dock_display_mode_text),
}

enum class DrawerPresentation(@param:StringRes val displayNameRes: Int) {
    LIST(R.string.drawer_presentation_list),
    GRID(R.string.drawer_presentation_grid),
}

enum class DrawerGridSize(val columns: Int, val rows: Int) {
    FOUR_BY_FOUR(4, 4),
    FOUR_BY_FIVE(4, 5),
    FIVE_BY_FIVE(5, 5),
    FIVE_BY_SIX(5, 6),
}

/**
 * List presentation only. [extraRowPaddingDp] adds on top of [DrawerAppRow][com.facetlauncher.app.ui.drawer.DrawerAppRow]'s
 * own 8dp base vertical padding (originally the design spec's `6px 0` for App Drawer's list rows —
 * see `design_handoff_minimal_launcher/README.md`'s `1h` — rounded up to 8dp per direct request so
 * every tier's total sits on a clean 4dp-grid value: `COMPACT` 8dp, `REGULAR` 16dp, `SPACIOUS`
 * 24dp; see chat history). [iconSizeDp] grows the row's [AppIcon][com.facetlauncher.app.ui.components.AppIcon]
 * alongside it for `REGULAR`/`SPACIOUS` (also per direct request) — `COMPACT` keeps the row's
 * existing 32dp icon unchanged, so this option's own default look doesn't shift.
 */
enum class DrawerListItemSize(val extraRowPaddingDp: Int, val iconSizeDp: Int, @param:StringRes val displayNameRes: Int) {
    COMPACT(0, 32, R.string.drawer_list_item_size_compact),
    REGULAR(8, 36, R.string.drawer_list_item_size_regular),
    SPACIOUS(16, 40, R.string.drawer_list_item_size_spacious),
}

enum class SearchBarPosition(@param:StringRes val displayNameRes: Int) {
    TOP(R.string.search_bar_position_top),
    BOTTOM(R.string.search_bar_position_bottom),
}

/**
 * Settings → App Drawer → "Folders in drawer" — how the App Drawer surfaces the folder library
 * ([FolderRepository][com.facetlauncher.app.data.FolderRepository]) as browsable drawer entries,
 * independent of whether each folder is also placed on the Dock or in Favorites. `DO_NOT_SHOW`
 * (the default — today's original behavior, before this setting existed) omits folders from the
 * drawer entirely; they stay reachable only via Dock/Favorites or the long-press "Add to folder"
 * menu. `INLINE` sorts folders into the same alphabetical buckets as apps
 * ([com.facetlauncher.app.data.model.DrawerItem]). `SHOW_FIRST`/`SHOW_LAST` instead pin every
 * folder into its own section before or after the lettered app list, outside alphabetical order.
 */
enum class DrawerFolderDisplayMode(@param:StringRes val displayNameRes: Int) {
    DO_NOT_SHOW(R.string.drawer_folder_display_mode_do_not_show),
    INLINE(R.string.drawer_folder_display_mode_inline),
    SHOW_FIRST(R.string.drawer_folder_display_mode_show_first),
    SHOW_LAST(R.string.drawer_folder_display_mode_show_last),
}

/** F11 — an explicit in-app override; `SYSTEM` (the default) follows the device's own light/dark setting. */
enum class ThemeMode(@param:StringRes val displayNameRes: Int) {
    LIGHT(R.string.theme_mode_light),
    DARK(R.string.theme_mode_dark),
    SYSTEM(R.string.theme_mode_system),
}

/** F13 — README specs a dot-only badge; `COUNT` is a deliberate departure from that, offered as a user choice rather than replacing the spec's default outright. */
enum class NotificationBadgeStyle(@param:StringRes val displayNameRes: Int) {
    DOT(R.string.notification_badge_style_dot),
    COUNT(R.string.notification_badge_style_count),
}

/**
 * Alignment of each app-list row (Home's Favorites/Recents/Most-used list — see [ListContentMode]).
 * `LEFT` (the default) is today's unchanged layout — icon then label, normal reading order,
 * packed against the row's start edge. `RIGHT` both reverses the internal order (label then icon,
 * icon landing on the row's trailing edge) and packs the whole row's content against the
 * available width's end, so the row visually hugs the right edge of the screen. `CENTER` keeps
 * `LEFT`'s normal reading order (icon then label) but centers that group within the row's full
 * width instead of packing it to either edge.
 */
/** [LAUNCHER_DEFAULT] is facet-only — see [DockDisplayMode]'s own doc for the sentinel's exact shape. */
enum class AppRowPosition(@param:StringRes val displayNameRes: Int) {
    LAUNCHER_DEFAULT(R.string.app_row_position_launcher_default),
    LEFT(R.string.app_row_position_left),
    CENTER(R.string.app_row_position_center),
    RIGHT(R.string.app_row_position_right),
}

/** What renders per app-list row — independent of [AppRowPosition]. `ICON_AND_TEXT` is the default, unchanged look. [LAUNCHER_DEFAULT] is facet-only — see [DockDisplayMode]'s own doc for the sentinel's exact shape. */
enum class AppRowPresentation(@param:StringRes val displayNameRes: Int) {
    LAUNCHER_DEFAULT(R.string.app_row_presentation_launcher_default),
    ICON_ONLY(R.string.app_row_presentation_icon_only),
    ICON_AND_TEXT(R.string.app_row_presentation_icon_and_text),
    TEXT_ONLY(R.string.app_row_presentation_text_only),
}

/** Home clock's horizontal placement within its zone. `LEFT` is today's unchanged default — the clock's containing Column has never applied any centering. */
enum class ClockAlignment(@param:StringRes val displayNameRes: Int) {
    LEFT(R.string.app_row_position_left),
    CENTER(R.string.app_row_position_center),
    RIGHT(R.string.app_row_position_right),
}

/** Home app list's vertical anchor. `BOTTOM` is today's unchanged behavior (list sits right above the dock). `TOP` anchors it immediately below the clock's grab handle instead. [LAUNCHER_DEFAULT] is facet-only — see [DockDisplayMode]'s own doc for the sentinel's exact shape. */
enum class AppListVerticalAlignment(@param:StringRes val displayNameRes: Int) {
    LAUNCHER_DEFAULT(R.string.app_list_vertical_alignment_launcher_default),
    TOP(R.string.app_list_vertical_alignment_top),
    BOTTOM(R.string.app_list_vertical_alignment_bottom),
}

/**
 * Which of the wallpaper's three Material You tonal roles (system_accent1/2/3) backs the accent
 * color when [LauncherSettings.accentFromSystem] is `true`. Only meaningful in that case, the
 * mirror image of [LauncherSettings.customAccentSwatch]'s "only meaningful when accentFromSystem
 * is false" scoping. `PRIMARY` is today's only-ever behavior before this setting existed, so it's
 * the default — nothing changes for anyone who hasn't touched the new picker.
 */
enum class WallpaperAccentRole {
    PRIMARY,
    SECONDARY,
    TERTIARY,
}

/** No facet has been created/selected yet — [FacetRepository][com.facetlauncher.app.data.FacetRepository]'s ids start at 1. */
const val NO_ACTIVE_FACET_ID = 0L

data class LauncherSettings(
    val use24HourTime: Boolean = false,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    val drawerGridSize: DrawerGridSize = DrawerGridSize.FIVE_BY_SIX,
    val drawerListItemSize: DrawerListItemSize = DrawerListItemSize.REGULAR,
    val drawerOpacity: Float = 0.6f,
    val notificationDotsEnabled: Boolean = true,
    /** F13 — Dot (README's spec default) vs a capped numeric count, chosen from the dedicated Notification settings screen. */
    val notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    val showDrawerIcons: Boolean = true,
    val showDrawerLabels: Boolean = true,
    val searchBarPosition: SearchBarPosition = SearchBarPosition.TOP,
    val drawerFolderDisplayMode: DrawerFolderDisplayMode = DrawerFolderDisplayMode.DO_NOT_SHOW,
    val activeFacetId: Long = NO_ACTIVE_FACET_ID,
    /** Global Calendar setting (`4l`) — whether all-day events render on the clock. */
    val showAllDayEvents: Boolean = true,
    /** [CalendarContract.Calendars][android.provider.CalendarContract.Calendars] ids to show events from. `null` means "not yet initialized" — distinct from an empty set (user explicitly deselected everything) so a first-time grant can default to all calendars. */
    val selectedCalendarIds: Set<String>? = null,
    /**
     * Per-calendar accent bar color, keyed by [CalendarInfo.id][com.facetlauncher.app.data.model.CalendarInfo.id]
     * — one of `AccentSwatch`'s entries, as its enum name (kept as a plain string here, same
     * reason as [customAccentSwatch]). Assigned automatically (see `AssignCalendarColorsUseCase`)
     * the first time each calendar is seen, then stable — never reassigned once set.
     */
    val calendarColors: Map<String, String> = emptyMap(),
    /** F6 — whether the App Drawer search includes a contacts section. The toggle itself triggers the `READ_CONTACTS` request; denial reverts this back to `false`. */
    val searchContactsEnabled: Boolean = false,
    /** Whether the App Drawer search includes a system Settings section (e.g. typing "wifi" surfaces a "Wi-Fi" result that deep-links into that Settings screen). No runtime permission needed, unlike [searchContactsEnabled]. */
    val searchSettingsEnabled: Boolean = false,
    /** F11 — Settings → Theme → "Select launcher theme". */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** F11 — "Wallpaper colors" (Material You) by default; `false` selects "Basic colors" — [customAccentSwatch]'s fixed pick. */
    val accentFromSystem: Boolean = true,
    /** The user's "Basic colors" pick when [accentFromSystem] is `false`, as an `AccentSwatch` enum name (kept as a plain string here — the enum itself is a `ui/theme` type, out of reach for this data-layer class). `null` until they've picked one. */
    val customAccentSwatch: String? = null,
    /** Settings → Theme → Accent color's wallpaper-role pick — only meaningful when [accentFromSystem] is `true`. */
    val wallpaperAccentRole: WallpaperAccentRole = WallpaperAccentRole.PRIMARY,
    /** F11 — Settings → Theme → "Icons"; global (not per-app) app-icon rendering mode. */
    val iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
    /**
     * Settings → Appearance → "Font" — the base font for every text role app-wide except the
     * clock/calendar, which pick their own font independently (see [ClockFontOption]'s
     * `LAUNCHER_DEFAULT`, which follows this value when selected there). Global only — not
     * facet-overridable, same as [themeMode]/[accentFromSystem]/[iconRenderMode].
     */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /**
     * Settings → Appearance → "Text size" — see [FontScaleOption]'s own doc. Global only, same tier
     * as [launcherFontOption]/[homeAppsFontWeight] — not facet-overridable.
     */
    val fontScaleOption: FontScaleOption = FontScaleOption.DEFAULT,
    /**
     * Settings → Appearance → "Font Color" — the app-list row and dock text color, reusing
     * [ClockFontOption]'s sibling color enum ([ClockColorOption]) since it's the same "text over
     * the home/wallpaper surface" choice the clock/calendar already offer. Global only, same as
     * [launcherFontOption] — the dock itself isn't per-facet, so this can't be either.
     */
    val appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    /**
     * Settings → Permissions has requested `READ_CALENDAR`/`READ_CONTACTS` at least once before.
     * `checkSelfPermission` alone can't tell "never asked" apart from "permanently denied" —
     * both read as ungranted with no system rationale to show — so this is the disambiguating
     * signal: once we know we've asked before, a still-ungranted permission means the user said
     * no for good, and the row should send them to this app's own system App Info screen instead
     * of firing a runtime request the system will just silently refuse to show again.
     */
    val calendarPermissionRequested: Boolean = false,
    val contactsPermissionRequested: Boolean = false,
    /** Settings → "Apps list" section — every facet's own [FacetEntity][com.facetlauncher.app.data.local.FacetEntity]
     * inherits [appRowPosition], [appRowPresentation], and [listContentMode] unless it sets its own override. */
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    /** Only meaningful when [listContentMode] isn't [ListContentMode.FAVORITES] — see [AppListLimits]. */
    val appsToShowCount: Int = AppListLimits.DEFAULT_APPS_TO_SHOW,
    /** Global default clock look — see the clock template gallery, reached from Settings' Clock card. */
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.THEME,
    /**
     * The color a template's own accent glyph/shape renders in — see [ClockTemplateId.usesAccentColor]
     * for which templates actually read this (every other template ignores it entirely). Same
     * option set as [clockColorOption], independently pickable.
     */
    val clockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    /** Ignored (no AM/PM to show) whenever [use24HourTime] is on, regardless of this value. */
    val clockShowMeridiem: Boolean = false,
    /** "Full" ("Thursday, 27 August") vs "Condensed" ("Thu, 27 Aug") — applies to every template that renders its date line through `ClockTemplates.kt`'s shared `dateFormatter()`. */
    val clockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /**
     * Settings → Appearance → "Text weight" — Home's app list/Dock/App Drawer labels apply it
     * directly (see `ui/home/HomeScreen.kt`'s `AppRow`/`DockIcon`, `ui/drawer/AppDrawerScreen.kt`'s
     * app-label `Text`s), and it also feeds `ui/theme/Type.kt`'s `facetTypography` as the app-wide
     * `bodyLarge`/`bodyMedium`/`bodySmall` weight — which is what reaches Settings' own row text
     * (`LabeledDropdownRow`, `SettingsScreen`'s list rows) without those screens threading it through
     * individually. Deliberately doesn't reach `title*`/`label*`/`headline*`/`display*` roles (dialog
     * titles, context menus, onboarding copy) — see `facetTypography`'s own doc for why. Global only,
     * same as [launcherFontOption].
     */
    val homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /**
     * Home clock's horizontal placement — the default every facet inherits unless it overrides
     * it (bundled into [FacetEntity][com.facetlauncher.app.data.local.FacetEntity]'s
     * `overrideClock`, alongside [clockTemplateId]/etc — see chat history: this and its siblings
     * below used to be global-only, like [launcherFontOption]/[themeMode], before moving into that
     * per-facet bundle).
     */
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /**
     * Y-position (dp, measured from the top of Home's content area) of the drag handle sitting
     * below the clock+calendar block — equivalently, where the app list's reserved region begins.
     * `null` until the user drags it for the first time, in which case the block renders at its
     * original fixed `top = 52.dp` position and the list bottom-anchors exactly as it always has
     * (see `HomeScreen.kt` for how the default and persisted cases resolve to one formula). Once
     * set, clamped to [the block's own measured height + 24dp, 50% of the available content
     * height]. Facet-overridable, same as [clockAlignment].
     */
    val clockZoneHeightDp: Float? = null,
    /**
     * The default every facet inherits unless it overrides it (bundled with [AppRowPosition]/
     * [AppRowPresentation]/[ListContentMode] under `overrideApps` on
     * [FacetEntity][com.facetlauncher.app.data.local.FacetEntity], same as those three) —
     * whether the app list anchors to the bottom (today's behavior, right above the dock) or the
     * top (right below the clock's grab handle).
     */
    val appListVerticalAlignment: AppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
    /**
     * Home clock's scale factor — the default every facet inherits.
     * Uniform scaling ensures proportions are preserved.
     */
    val clockScale: Float = 0.8f,
    /** Whether the first-run onboarding flow has been completed (or skipped past its final step). Gates [LauncherActivity][com.facetlauncher.app.LauncherActivity]'s onboarding branch. Install-local — excluded from the backup bundle. */
    val onboardingCompleted: Boolean = false,
    /** One-shot guard for [SeedDefaultDockUseCase][com.facetlauncher.app.domain.SeedDefaultDockUseCase] — set once the dock's default apps have been seeded, so a re-run (or a later manual empty-dock) never re-seeds. Install-local — excluded from the backup bundle. */
    val defaultsSeeded: Boolean = false,
    /** Ids of dismissed post-onboarding coach marks (e.g. `"HOME_GESTURES"`) — additive, never cleared. Install-local — excluded from the backup bundle. */
    val coachMarksSeen: Set<String> = emptySet(),
)
