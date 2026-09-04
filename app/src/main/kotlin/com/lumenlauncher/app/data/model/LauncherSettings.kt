package com.lumenlauncher.app.data.model

enum class DockDisplayMode {
    ICONS,
    TEXT,
}

enum class DrawerPresentation {
    LIST,
    GRID,
}

enum class DrawerGridSize(val columns: Int, val rows: Int) {
    FOUR_BY_FOUR(4, 4),
    FOUR_BY_FIVE(4, 5),
    FIVE_BY_FIVE(5, 5),
    FIVE_BY_SIX(5, 6),
}

/**
 * List presentation only. [extraRowPaddingDp] adds on top of [DrawerAppRow][com.lumenlauncher.app.ui.drawer.DrawerAppRow]'s
 * existing base vertical padding — `COMPACT` (0) reproduces that unchanged existing height; each
 * step up adds 8dp more, per direct request (see chat history). [iconSizeDp] grows the row's
 * [AppIcon][com.lumenlauncher.app.ui.components.AppIcon] alongside it for `REGULAR`/`SPACIOUS`
 * (also per direct request) — `COMPACT` keeps the row's existing 32dp icon unchanged, so this
 * option's own default look doesn't shift.
 */
enum class DrawerListItemSize(val extraRowPaddingDp: Int, val iconSizeDp: Int) {
    COMPACT(0, 32),
    REGULAR(8, 36),
    SPACIOUS(16, 40),
}

enum class SearchBarPosition {
    TOP,
    BOTTOM,
}

/** F11 — an explicit in-app override; `SYSTEM` (the default) follows the device's own light/dark setting. */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

/** F13 — README specs a dot-only badge; `COUNT` is a deliberate departure from that, offered as a user choice rather than replacing the spec's default outright. */
enum class NotificationBadgeStyle {
    DOT,
    COUNT,
}

/**
 * Alignment of each app-list row (Home's Favorites/Recents/Most-used list — see [ListContentMode]).
 * `LEFT` (the default) is today's unchanged layout — icon then label, normal reading order,
 * packed against the row's start edge. `RIGHT` both reverses the internal order (label then icon,
 * icon landing on the row's trailing edge) and packs the whole row's content against the
 * available width's end, so the row visually hugs the right edge of the screen.
 */
enum class AppRowPosition {
    LEFT,
    RIGHT,
}

/** What renders per app-list row — independent of [AppRowPosition]. `ICON_AND_TEXT` is the default, unchanged look. */
enum class AppRowPresentation {
    ICON_ONLY,
    ICON_AND_TEXT,
    TEXT_ONLY,
}

/** No profile has been created/selected yet — [ProfileRepository][com.lumenlauncher.app.data.ProfileRepository]'s ids start at 1. */
const val NO_ACTIVE_PROFILE_ID = 0L

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
    val activeProfileId: Long = NO_ACTIVE_PROFILE_ID,
    /** Global Calendar setting (`4l`) — whether all-day events render on the clock. */
    val showAllDayEvents: Boolean = true,
    /** [CalendarContract.Calendars][android.provider.CalendarContract.Calendars] ids to show events from. `null` means "not yet initialized" — distinct from an empty set (user explicitly deselected everything) so a first-time grant can default to all calendars. */
    val selectedCalendarIds: Set<String>? = null,
    /**
     * Per-calendar accent bar color, keyed by [CalendarInfo.id][com.lumenlauncher.app.data.model.CalendarInfo.id]
     * — one of `AccentSwatch`'s entries, as its enum name (kept as a plain string here, same
     * reason as [customAccentSwatch]). Assigned automatically (see `AssignCalendarColorsUseCase`)
     * the first time each calendar is seen, then stable — never reassigned once set.
     */
    val calendarColors: Map<String, String> = emptyMap(),
    /** F6 — whether the App Drawer search includes a contacts section. The toggle itself triggers the `READ_CONTACTS` request; denial reverts this back to `false`. */
    val searchContactsEnabled: Boolean = false,
    /** F11 — Settings → Theme → "Select launcher theme". */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** F11 — "Wallpaper colors" (Material You) by default; `false` selects "Basic colors" — [customAccentSwatch]'s fixed pick. */
    val accentFromSystem: Boolean = true,
    /** The user's "Basic colors" pick when [accentFromSystem] is `false`, as an `AccentSwatch` enum name (kept as a plain string here — the enum itself is a `ui/theme` type, out of reach for this data-layer class). `null` until they've picked one. */
    val customAccentSwatch: String? = null,
    /** F11 — Settings → Theme → "Icons"; global (not per-app) app-icon rendering mode. */
    val iconRenderMode: IconRenderMode = IconRenderMode.SYSTEM_DEFAULT,
    /**
     * Settings → Appearance → "Font" — the base font for every text role app-wide except the
     * clock/calendar, which pick their own font independently (see [ClockFontOption]'s
     * `LAUNCHER_DEFAULT`, which follows this value when selected there). Global only — not
     * profile-overridable, same as [themeMode]/[accentFromSystem]/[iconRenderMode].
     */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /**
     * Settings → Appearance → "App label color" — the app-list row and dock text color, reusing
     * [ClockFontOption]'s sibling color enum ([ClockColorOption]) since it's the same "text over
     * the home/wallpaper surface" choice the clock/calendar already offer. Global only, same as
     * [launcherFontOption] — the dock itself isn't per-profile, so this can't be either.
     */
    val appLabelColorOption: ClockColorOption = ClockColorOption.INK,
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
    /** Settings → "Apps list" section — every profile's own [ProfileEntity][com.lumenlauncher.app.data.local.ProfileEntity]
     * inherits [appRowPosition], [appRowPresentation], and [listContentMode] unless it sets its own override. */
    val appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    /** Only meaningful when [listContentMode] isn't [ListContentMode.FAVORITES]. Range 4…8 per README's `3d` spec. */
    val appsToShowCount: Int = 5,
    /** Global default clock look — see the clock template gallery, reached from Settings' Clock card. */
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val clockColorOption: ClockColorOption = ClockColorOption.INK,
    /** Ignored (no AM/PM to show) whenever [use24HourTime] is on, regardless of this value. */
    val clockShowMeridiem: Boolean = false,
    /** Calendar events block (`ui/home/clock/CalendarEventsBlock`) — configured independently of the clock's own font/color. */
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.INK,
)
