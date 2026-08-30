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
 * step up adds 8dp more, per direct request (see chat history).
 */
enum class DrawerListItemSize(val extraRowPaddingDp: Int) {
    COMPACT(0),
    REGULAR(8),
    SPACIOUS(16),
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

/** No profile has been created/selected yet — [ProfileRepository][com.lumenlauncher.app.data.ProfileRepository]'s ids start at 1. */
const val NO_ACTIVE_PROFILE_ID = 0L

data class LauncherSettings(
    val use24HourTime: Boolean = false,
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    val drawerGridSize: DrawerGridSize = DrawerGridSize.FIVE_BY_SIX,
    val drawerListItemSize: DrawerListItemSize = DrawerListItemSize.COMPACT,
    val drawerOpacity: Float = 0.88f,
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
    /** Settings → "Default apps list" — every profile's own [ProfileEntity][com.lumenlauncher.app.data.local.ProfileEntity]
     * inherits this unless it sets its own override. */
    val listContentMode: ListContentMode = ListContentMode.FAVORITES,
    /** Only meaningful when [listContentMode] isn't [ListContentMode.FAVORITES]. Range 4…8 per README's `3d` spec. */
    val appsToShowCount: Int = 5,
    /** Global default clock look — see the clock template gallery, reached from Settings' Clock card. */
    val clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val clockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val clockColorOption: ClockColorOption = ClockColorOption.INK,
    /** Ignored (no AM/PM to show) whenever [use24HourTime] is on, regardless of this value. */
    val clockShowMeridiem: Boolean = false,
    /** Calendar events block (`ui/home/clock/CalendarEventsBlock`) — configured independently of the clock's own font/color. */
    val calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val calendarColorOption: ClockColorOption = ClockColorOption.INK,
)
