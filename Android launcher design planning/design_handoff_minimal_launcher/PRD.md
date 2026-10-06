# PRD: Minimal Android Launcher

**Status:** Draft — requirements gathering
**Reference / inspiration:** Niagara Launcher
**Primary test device:** Samsung Galaxy M15 5G (Dimensity 6100+, 4–6GB RAM, 90Hz AMOLED) — see Non-Functional Requirements
**Language/stack (tentative):** Kotlin + Jetpack Compose, with native `AppWidgetHostView` wrapped via `AndroidView` for widget hosting

---

## 1. Overview

A minimal, fast Android home screen launcher. Prioritizes speed and low visual clutter over customization depth. Core interaction model: the home surface shows a clock, upcoming calendar events, a short curated app list (Favorites, Recents, or Most Used), and a fixed dock — not an icon grid. The full, alphabetically-sorted app list lives in a separate App Drawer (reached via swipe up), which supports both list and grid-based presentation.

## 2. Goals

- Feel instant — see performance targets in §8.
- Minimize visual noise on the home screen: no default icon grid, no forced widgets.
- Let power users configure multiple home "facets" without duplicating shared elements (dock).
- Ship without owning a wallpaper/theming engine — delegate to system intents where possible.

## 3. Non-Goals (v1)

- No cloud sync / backup of launcher config across devices (local only for v1).
- No tablet/foldable-specific layouts.
- No custom lock screen or notification shade replacement.
- No third-party icon pack support (F11) — parked as a separate future requirement; v1 ships system-default icons + monochrome overlay only.
- No OEM-level system theming support (e.g. Samsung Theme Park / Galaxy Store icon packs) — these apply icons before any third-party launcher can render them; undetectable by design.
- No web search fallback in the app drawer search — search (now in scope per F6) covers apps and contacts only.

---

## 3a. Parked for Future Consideration

Not in scope for this version, but researched and worth preserving rather than losing:

- **Third-party icon pack support (F11 v2 candidate):** a two-layer model — (1) generic detection of installed icon-pack apps via their declared intent action, then parsing their `appfilter.xml` for a component-name → icon mapping; (2) optionally, a direct-apply intent contract of our own (mirroring Niagara's `bitpit.launcher.APPLY_ICONS` pattern) that pack developers could adopt for a one-tap "apply" flow, though that only pays off with ecosystem adoption (e.g. getting added to the "Blueprint" icon-pack dashboard template's supported-launcher list). Fallback-for-uncovered-apps model: pack's own generic styling → system default → user manual override, remembered per-pack.

---

## 4. Feature Requirements

### F1. Home clock widget + calendar integration
- Custom-built clock widget (not a hosted third-party widget) shown on the home screen.
- Multiple visual style options for the clock (user-selectable), including 12h/24h format.
- Calendar integration:
  - User selects which device calendars to show events from (multi-select, supports multiple accounts).
  - Option to hide all-day events.
  - Requires `READ_CALENDAR` runtime permission — **must define fallback UI when denied or no calendars exist** (e.g., clock-only state, with a re-prompt affordance, not a blank/broken widget).
  - Tapping an event **[decided]:** opens the default calendar app directly at that specific event (not just a generic app-open) — implementable via the standard Android Calendar Provider (`ACTION_VIEW` on a `CalendarContract.Events` content URI for the event id), which works across calendar apps that read from the shared system Calendar Provider rather than requiring per-app deep-link support.
  - **Settings shell built ahead of the permission flow [revised]:** a Calendar settings screen exists with a real, persisted "Show all-day events" toggle and an inherit/override mechanism per facet — the same pattern F4 uses for 24-hour time. The `READ_CALENDAR` request, the real "calendars to display" list, and event rows on the clock are still pending; the screen shows the standard permission-denied strip in the meantime.

### F2. Home screen — Favorites / Recents / Most Used
- User chooses which "mode" populates the home list: Favorites (manually curated), Recents (top 3–5), or Most Used.
- **Favorites:** user explicitly adds/removes/orders apps.
- **Recents [decided]:** backed by system-wide usage stats via `UsageStatsManager` (not self-tracked launch events) — same data source and permission requirement as Most Used below.
- **Most Used:** requires `PACKAGE_USAGE_STATS` special-access permission, which cannot be requested via a normal runtime dialog — user must be sent to system Settings (`ACTION_USAGE_ACCESS_SETTINGS`). Needs an onboarding explanation screen and a graceful "not yet granted" empty state. **This permission is now required for both Recents and Most Used**, since both are usage-stats-backed.
- Behavior when a favorited/pinned app is uninstalled: auto-remove and collapse the list (no dead entries or placeholder tiles), across favorites, dock, and all facets.

### F3. Dock
- **0–5 apps, user-configured count and content [decided, revised]:** no minimum — every app can be removed. When the dock is empty, the dock row is omitted entirely (not shown as an empty row), on both Home and the facet carousel's preview cards.
- **Shared across all facets** (single dock, not per-facet).
- Same uninstall-handling rule as F2 applies here.
- **Display mode [new, decided]:** dock apps can be shown as **Icons** (default) or **Text** (app name labels instead of icons) — a single launcher-wide setting, not per-app.

### F4. Facets
- Multiple home "facets," each with its own Favorites/Recents/Most-Used configuration and list content.
- **Per-facet overrides extend beyond favorites [revised]:** a facet's settings screen has a Clock card (clock style, 24-hour time, Calendar) governed by a single Inherit/Override switch — under Inherit it reflects and follows the launcher-wide default; under Override, this facet's own value is used and edits here never affect other facets. 24-hour time and the Calendar's "show all-day events" setting are real, independently-stored overrides; clock style stays inherit-only until multiple clock styles exist (see the Known Gap in `IMPLEMENTATION_PLAN.md`).
- Dock is shared (per F3) — not duplicated per facet.
- **Facet switching is a swipe gesture on the home screen itself [decided, revised again per user feedback].** The empty-space long-press sheet was removed — it was too hard to land in the gaps between the clock, app list and dock. **A left swipe on Home** now opens the Switch Facets carousel directly, as a permanent **follow-finger panel** to Home's right (mirroring the Hub panel to Home's left, opened by a right swipe) — not a `NavHost` destination, no separate slide-in transition: the same drag that opens it live-tracks the finger, and a rightward swipe (in empty space, or on the first facet's card, where the pager has nowhere left to browse) closes it back to Home. The carousel itself still behaves like a recent-apps switcher: horizontal swipe browses (clamped at both ends, no infinite scroll), and **tapping any visible card — centered or peeking — applies it immediately and closes**. There is no separate confirm step. Backing out (system back, or that rightward swipe) without tapping a card leaves the active facet unchanged.
- **Carousel preview cards are a live snapshot, not a placeholder [decided, revised]:** each card actually renders that facet's clock and its own real favorite apps (with icons), plus the shared dock (per F3 — same content on every card, since the dock isn't per-facet) — not a static name/label shell — since the carousel is meant to preview what selecting a facet will look like.
- **First facet is not special [decided, revised]:** there's a "first" facet positionally (created by default on first launch), but it carries no protected status — it can be deleted like any other facet. The only hard rule is that **at least one facet must always exist**; deleting the last remaining facet isn't allowed.
- **Facet count [decided, revised]:** the launcher starts with exactly 1 facet by default. A maximum of **3 facets** applies for now — implemented as a single hardcoded constant, not a user-facing setting, so it's a trivial one-line change to raise later rather than something requiring UI/settings work. No sane-ceiling question remains since it isn't user-adjustable in this version.
- **Empty-state / creation model [decided]:** each new facet starts empty and is presented as a setup prompt. **Three entry points to add a facet, all decided:**
  1. The trailing "add facet" placeholder page after the last real facet in normal swipe-through navigation, up to the constant max.
  2. Launcher Settings → Facets → an explicit "add facet" action.
  3. Within the facet carousel (see below), scrolling right past the last page surfaces the same add-facet button in place.
  Once the max (3) is reached, none of these three affordances is shown.
- **Deletion [decided]:** deleting a facet removes that page outright; remaining facet pages re-order to fill the gap. The delete action must be disabled/blocked when it's the last remaining facet, since at least one must always exist. **A confirmation dialog is required before deletion completes** [decided] — resolves the earlier open question.
- **Renaming & reordering [decided]:** facets can be renamed and manually reordered — resolves the earlier open question.
- **Access paths for facet management [decided, revised again per user feedback], two entry points:**
  1. **Settings list view:** Launcher Settings → Facets opens the facet carousel directly — tapping a card applies it, same as the swipe path below.
  2. **Left swipe on Home → carousel:** a left swipe on an empty area of Home opens the Switch Facets panel directly (see the gesture description above) — no intermediate sheet. Swipe horizontally within it to browse (clamped at both ends), tap any card to apply it and close, or swipe right / system back to return without applying.
  - **Superseded [decided]:** an earlier design routed this through an empty-space long-press sheet with three rows — Switch facet, Edit facet, Change wallpaper. That sheet is removed; **Edit facet** now lives on the carousel's per-card gear icon (Facet settings, `3d`), **Change wallpaper** moved to a row in Launcher Settings → Appearance, and **Switch facet** became the left-swipe gesture itself.
  - **Reordering and rename moved out of the carousel [decided, revised]:** since tapping any carousel card now applies it immediately, long-press-to-reorder-in-place no longer has a safe home there. A **Reorder** link in the carousel's header swaps the carousel for a plain reorderable list — one row per facet (drag handle, name, an overflow menu with **Facet settings** and **Delete**), plus a pinned **Add facet** row. Rename lives inside **Facet settings** (`3d`), reached from either the carousel's gear icon or this list's overflow menu — there's no longer a direct tap-the-name-to-rename affordance on the carousel card itself.
  - **Disambiguation no longer needed [resolved]:** the earlier concern was distinguishing an empty-space long-press from F12's long-press-on-app-icon context menu. Since empty-space long-press on Home no longer triggers anything (it's a plain no-op — facet switching moved to the left-swipe gesture instead), that ambiguity is gone; F12's long-press-on-app-icon menu is unaffected.

### F5. Launcher Hub (widgets)
- **Swipe right from home** to reach a dedicated screen for user-added third-party widgets, positioned to the left of the main screen; swipe left from the hub returns to home.
- **Layout model [decided, revised]:** single screen only (no paging). Widgets are placed on a **fixed grid** (columns × rows, same convention as F6) rather than freeform — placement snaps to grid cells, and **no two widgets may overlap**. The grid scrolls vertically if content exceeds the visible area.
- **Grid size [decided, revised]:** the hub's **column count mirrors the App Drawer's grid column setting** (F6) — 4 or 5 columns, matching whichever of 4×4 / 4×5 / 5×5 / 5×6 is currently selected for the App Drawer. **Default is 5 columns**, and if the App Drawer is set to **List** presentation (no grid column value in effect), the hub still defaults to 5 columns. **Row count is not independently fixed** — the grid grows vertically as widgets are added, up to a maximum of **50 rows**, scrolling beyond what fits on one screen.
- **Capacity [decided]:** up to 20 widgets per hub.
- **Required capabilities (full set, since this is new ground):**
  - **Add:** system widget picker flow (`AppWidgetManager.ACTION_APPWIDGET_PICK` or an in-app picker listing `AppWidgetProviderInfo` per installed app) → `allocateAppWidgetId` → bind. Some providers require the system to show its own **bind permission dialog** (`bindAppWidgetIdIfAllowed` returns false) — this is a system UI the launcher doesn't control, and the flow must handle both the granted and cancelled outcomes.
  - **Configure:** some widgets declare a configuration `Activity` (`AppWidgetProviderInfo.configure`) that must be launched immediately after binding, before the widget is considered placed — a widget add flow that skips this will show broken/default-state widgets for any provider that needs it.
  - **Resize:** respect each widget's declared `minWidth`/`minHeight`/`minResizeWidth`/`minResizeHeight`, snapped to the hub's grid cell size — free-form resize below a provider's stated minimum isn't supported by the widget itself and needs to be prevented in the UI, not just visually clamped. Resizing must also respect the no-overlap constraint against neighboring widgets.
  - **Reposition/reorder:** drag within the grid, snapping to cells; must respect no-overlap.
  - **Remove:** long-press or explicit delete action; must call `AppWidgetHost.deleteAppWidgetId` to release the id, not just remove it from the visible layout, or ids leak.
  - **Persistence:** widget ids, provider component names, and grid cell positions/sizes must survive process death and reboot — this is local state the launcher owns (not something `AppWidgetHost` persists for you across a fresh process).
  - **Provider lifecycle events:** handle `AppWidgetHostView`/`AppWidgetHost` callbacks for when a providing app is updated (`onProviderChanged`) or uninstalled (widget becomes orphaned — needs a "no longer available" placeholder or auto-removal, consistent with the uninstall-handling principle used elsewhere).
  - `AppWidgetHost.startListening()` scoped to only run while this screen is visible, to avoid unnecessary background IPC/battery cost.
  - **Empty state:** "no widgets added yet" with a clear add-widget affordance, not a blank screen.
  - **At-capacity state:** once 20 widgets are placed, the add-widget affordance should communicate the cap has been reached rather than silently failing.

### F6. App Drawer
- Opened via swipe up.
- Configurable presentation: **list** or **grid**.
- Grid sizes: 4×4, 4×5, 5×5, 5×6, expressed as **columns × rows** (matches the convention used by Nova Launcher and stock Android grid settings — e.g. "5×5" = 5 columns wide, 5 rows tall).
- **Search [decided, in scope]:** text search-as-you-type, filtering the app list by name.
  - **Contextual quick actions:** when the search query matches a contact rather than (or in addition to) an app, surface quick actions — call, message (SMS), WhatsApp — directly from the result, rather than requiring the user to open a separate contacts/dialer app.
  - Requires `READ_CONTACTS` runtime permission, requested contextually when this specific capability is first used/enabled (see §7 permissions principle).
  - **Result ordering [decided]:** apps first, then contacts, then contextual actions (call/message/WhatsApp) — a consistent, predictable order rather than interleaved relevance ranking.
  - WhatsApp action requires checking whether WhatsApp is installed (`PackageManager` check) and must degrade gracefully (hide the action, not show a broken one) if it isn't.
- **Close behavior [decided]:**
  - Swipe down closes the drawer **only if the list is scrolled to the top**; otherwise it's a normal scroll gesture.
  - System back button / back gesture also closes the drawer.

### F7. Alphabet Rail
- Right-edge rail lets the user jump directly to apps starting with a given letter.
- Must be responsive with no perceptible lag (target: sub-frame response to drag, see §8).
- **Bucketing/ordering mechanism [decided]:** use `android.icu.text.AlphabeticIndex` — the same platform component the system Contacts app uses for its letter-jump index — rather than a hand-rolled digit/letter/symbol scheme. It handles locale-correct alphabetic ordering (e.g. Swedish Å/Ä/Ö sorting after Z as distinct letters, Spanish Ñ as distinct from N) and transliterates non-Latin scripts into an appropriate bucket (e.g. grouping a Japanese app name phonetically rather than dumping it into a generic "other" bucket), avoiding the hardcoded-ordering complaints users have raised against launchers that rolled their own rules.

### F8. Left-edge Rail Access
- The same letter-jump interaction must also work by dragging from the **left edge** of the app list, not just the right rail — for easier one-handed reach depending on hand/grip.
- Needs explicit gesture-zone definition so this doesn't conflict with any left-edge system gestures (e.g. Android's back-gesture edge swipe) or with F5's swipe-right-for-hub gesture, since the app drawer is a distinct screen from the home screen where that gesture is defined.

### F9. Backgrounds
- No custom wallpaper engine. Launch the system `ACTION_SET_WALLPAPER` (or equivalent) intent so the user sets wallpaper via the OS's own picker.
- Launcher reads the current system wallpaper (via `WallpaperManager`) to render behind its own UI.
- **Entry point [decided, revised]:** a **Change wallpaper** row in Launcher Settings → Appearance (see F4 — this moved off the now-removed long-press sheet).

### F10. App Drawer Overlay
- App drawer is presented as a configurable-opacity overlay on top of the home background (not a separate flat background) — reinforces "minimal" visual identity, avoids maintaining a second background asset.

### F11. Label Customization (icon customization parked)
- **Icon sources [decided, scope reduced]:** two rendering modes for v1, applied globally (not per-app):
  1. System-default icons, as provided by each app.
  2. Monochrome overlay mode: user picks a single accent color, applied as a themed overlay across all icons.
- **Third-party icon pack support is parked, out of scope for this version.** The two-layer detection/parsing/direct-apply model researched for this doc (generic `appfilter.xml` parsing + a Niagara-style direct-apply intent contract) is real and workable, but is being tracked as a **separate future requirement** rather than built now. Revisit F11 v2 for that scope if/when prioritized.
- **No per-app icon override in this version [decided]:** since icon packs aren't supported, there's no per-app icon replacement or per-app overlay toggle — the monochrome mode, if enabled, applies uniformly across all icons. Per-app customization is limited to label editing only.
- **Per-app label editing [decided, in scope]:** user can rename an app's displayed label without affecting the actual app name. This is the only per-app customization in this version.
- **Min SDK simplification:** with min SDK set to Android 13 (API 33), there's no need to support pre-13 OS-version fallbacks. The custom monochrome-overlay engine is still needed for any app whose own icon asset doesn't include a monochrome variant (`Icon.getMonochrome()` returns null) — that's a per-app packaging choice, not an OS capability gap.

---

### F12. Long-Press Context Menu [decided, in scope]
Long-pressing an app icon (dock, favorites, or drawer) surfaces: app info, uninstall, a "widgets" shortcut for that specific app's available widgets, and the rename flow from F11 (label editing only — no per-app icon selection in this version).

### F13. Notification Badges [decided, in scope]
- Show badges on the home screen, next to the app name in the favorites list, and in the App Drawer. **[Revised post-launch, see implementation notes]** originally decided as a dot-only indicator; the user later asked for a numeric count option, so badge style is a **user-selectable choice between Dot and Count** (`NotificationBadgeStyle`), not a fixed dot. Count mode uses Material3's standard badge treatment and caps its label at `9+` for anything above 9, rather than rendering an arbitrarily wide number.
- **No per-app opt-out [decided].** Badge visibility follows the same suppression rules the system notification shade already applies — a notification that the system itself treats as silent (e.g. posted to a low-importance/silent notification channel) does not surface a badge, consistent with what the user already expects from that app's shade behavior rather than an independent always-on count.
- Requires **notification listener special access** — a heavier permission flow than usage stats: the user must grant it via a system settings screen (`ACTION_NOTIFICATION_LISTENER_SETTINGS`), and it's a broad grant (the listener sees notification metadata for all apps, not just badge counts), which typically prompts more user hesitation than other permissions here.
- Requested just-in-time when the user enables badges, consistent with the permissions principle in §7. **[Revised]** the setting lives on its own dedicated Notification Settings page (reached from a Settings row summarizing on/off + the current style), not an inline Settings toggle — turning it on without access already granted routes to the notification-access explanation screen instead of silently no-opping.
- **Remaining implementation detail (not a design open question):** exact mapping from `NotificationListenerService`'s active notification list to "show a badge" — filtering by per-notification importance (via `NotificationListenerService.Ranking`/`getImportance()`, excluding `IMPORTANCE_NONE`) and group-summary notifications, deduped/counted per app.
- **Dock badges [decided]:** dock icons get the same badge (dot or count, per the user's chosen style) at the icon's top-right corner. Same suppression rules as the favorites-list badge (follows system silent-channel suppression, no per-app opt-out).

### F14. Backup & Restore [decided, in scope]
- **Format:** local JSON export, with import to reconstruct settings from that file.
- **Scope [decided]:** not limited to favorites/facets/dock — also includes widget hub state (widget ids, provider component names, grid positions/sizes), icon/label customization (F11), and other user settings (drawer grid size, hub grid size, gesture-related toggles if any become configurable, etc.). Effectively a full settings snapshot, not a partial one.
- **Not yet addressed:** widget ids and bindings are inherently tied to the specific device/OS state (`AppWidgetHost` ids aren't portable across installs or devices) — a restored JSON can reasonably reconstruct *which* widgets were placed and where, but re-binding them (going through the bind/configure flow again per widget) likely can't be fully automatic and may need a guided re-add step during import. Worth flagging as a real implementation constraint, not a simple file copy.
- No cloud sync in this version (per §3 Non-Goals) — export/import is a manual, user-initiated local file operation (e.g. share sheet / file picker), not automatic background backup.

### F15. Home Clock Widget Substitution [proposed, not yet decided]

F5-adjacent: reuses the Launcher Hub's widget-hosting infrastructure rather than building a parallel one. Came out of exploring whether the home clock's existing position/scale adjustment UX could extend to an arbitrary hosted widget, not just Facet's own clock templates.

- **Concept:** let the user replace the native Home clock (the `ClockTemplateId` gallery, F1) with a single hosted third-party `AppWidget` of their own choosing — not limited to "clock" widgets, any widget the system's own widget picker exposes. Home's clock slot becomes a general single-widget host built on F5's existing `AppWidgetHost` machinery, not a second independent hosting system.
- **UI entry point [decided]:** a new row in the existing long-press `ClockAdjustSheet.kt` — **"Use custom widget"** — alongside its current "Adjust size & position" / "Edit … styles" rows, opening F5's widget picker flow (reused, not duplicated) scoped to the active facet. Once a widget is active, the sheet's rows adjust: "Edit … styles" is hidden (no `ClockTemplateId` styling applies to a hosted widget's own content), "Adjust size & position" continues to apply (now driving real widget size — see below), "Use custom widget" stays available (pick a different custom widget), and a new **"Switch to launcher clock widget"** row reverts to Facet's own native clock — framing Facet's own clock as itself a widget choice, not a fallback/removal state, so the sheet reads as choosing between two widgets (launcher's own vs. custom).
- **Position [carries over unchanged]:** the clock's existing move-handle/alignment system already operates on a generic frame, agnostic to what's rendered inside it — no changes needed to reuse it for a hosted widget. *(Alignment can now also be set directly in adjust mode, from an "Alignment" pill below the height handle; it applies to a hosted widget the same way.)*
- **Scale becomes real size, not a transform [decided in principle, differs from the native clock]:** the native clock's `clockScale` is a continuous `graphicsLayer` float multiplier (0.5x–2.0x), deliberately draw-only so dragging stays jank-free. That mechanism doesn't suit a hosted widget — a third-party `AppWidget` needs its *actual* on-screen dp width/height pushed to the provider via `updateAppWidgetOptions`/`OPTION_APPWIDGET_SIZES` (the same mechanism F5's Hub already uses for its own resize) so the widget's `RemoteViews` content reflows correctly instead of visually stretching. The user-facing interaction (drag a corner handle, live resize) stays the same as today's clock — live width/height tracks the drag in real time (cheap, a Compose layout constraint change, same as Hub's own live-resize-during-drag), and only the committed size needs pushing to the provider, on release. Resize bounds come from the provider's own declared `minWidth`/`minHeight`/`minResizeWidth`/`minResizeHeight` (`AppWidgetProviderInfo`), not a fixed multiplier range — plus the same screen-fit clamp the clock's own resize already applies.
- **Scope [decided, revised]:** facet-level only — **not** a global setting with a per-facet override. Each facet independently owns its own widget choice (its own `appWidgetId`), unset by default (unset = native clock, same visual result as any other facet's default). This is a departure from every other per-facet Clock-card setting (24h time, Calendar), which default to **Inherit** a shared global value — there's no meaningful "global hosted widget" for this feature to inherit, so a facet's substitution is inherently its own override, on by default the moment the user picks a widget from that facet's sheet, with no separate Inherit/Override switch needed. A different widget per facet is real scope (up to the existing 3-facet max concurrently bound — well within the Hub's already-proven 20-widget capacity) but not deferred, per this decision.
- **Lifecycle [decided in principle]:** reuses F5's bind/configure/orphan-handling/persistence machinery wholesale rather than a parallel implementation — the system's bind-permission dialog and any provider config `Activity` must complete before a swap is considered placed, exactly like a Hub widget add.
- **Uninstall handling [decided]:** if the hosted widget's provider is uninstalled, fall back to that facet's native clock automatically — same uninstall-handling principle already applied to favorites/dock/Hub orphans elsewhere in this PRD.
- **Facet deletion [decided]:** deleting a facet that has an active hosted widget must release its `appWidgetId` via `AppWidgetHost.deleteAppWidgetId` — same "don't leak ids" rule F5 already applies to Hub widget removal, not just clear the local reference.
- **Tradeoff [explicitly accepted, not a blocker]:** once a third-party widget occupies a facet's clock slot, Facet has no rendering control over it — no accent color, launcher font, monochrome icon theming, or calendar-event overlay. It's opaque `RemoteViews` content, same as any Hub widget. Accepted as an expected consequence of the feature, not something to design around.
- **Out of scope for v1:** compositing the calendar overlay on top of a hosted widget.

## 5. Gesture Map

Consolidated so overlapping swipe directions across screens don't get implemented ad hoc per feature:

| Screen | Gesture | Behavior |
|---|---|---|
| Home | Swipe right | **[decided, revised]** Open the Launcher Hub (F5) — the hub sits to the left of the main screen. |
| Hub | Swipe left | **[decided, revised]** Return to the home screen. |
| Home | Swipe left | **[decided, revised per user feedback]** Open the Switch Facets carousel (F4) — the panel sits to Home's right, opened/closed by live finger-tracking rather than a committed transition. |
| Facets (carousel) | Swipe right / system back | **[decided, revised per user feedback]** Return to Home without applying a facet change (also triggered by a rightward swipe on the first facet's card, where the pager has nowhere left to browse). |
| Home | Swipe up | Open the App Drawer (F6) |
| Home | Swipe down | **[decided, confirmed]** Swipe down always opens the notification shade — but if the favorites list is not already scrolled to the top, the swipe first scrolls the list back to the top; only once the list is at the top does a further swipe-down open the shade. Swiping up always scrolls the list down without ever leaving the home screen. The rest of the home screen (clock/calendar area, dock) does not scroll independently. |
| Home | Long-press on empty area | **[superseded, decided]** No-op. Previously opened a long-press sheet (Switch facet / Edit facet / Change wallpaper) — removed because it was too hard to land in the gaps between the clock, app list and dock. Switch facet is now the swipe-left gesture above; Edit facet and Change wallpaper moved to the carousel's gear icon and Launcher Settings → Appearance, respectively (see F4, F9). Distinct from long-pressing an app icon, which still opens F12's context menu. |
| App Drawer | Swipe down | Closes the drawer only if the list is already scrolled to the top; otherwise behaves as a normal scroll. |
| App Drawer | Back button / back gesture | Closes the drawer. |
| App Drawer | Alphabet rail drag | Right edge (F7) or left edge (F8) — jumps to the letter under the finger. |

**Resolved:** the home swipe-down mechanic follows standard scroll physics — swipe down scrolls the favorites list toward the top; once at the top, a further swipe-down opens the shade. Swipe up always scrolls the list further down and never triggers the shade.

**Resolved [per user feedback]:** the original spec routed facet switching through an empty-space long-press sheet (see F4's superseded note) rather than a Home swipe, to avoid a third swipe direction competing with swipe-up (drawer) and swipe-right (hub). In practice the long-press target was too hard to land precisely, so it was replaced with a left swipe on Home, symmetric with the existing right-swipe-for-hub gesture — Home now has all three cardinal swipe directions in active use (up/left/right), each owned by exactly one destination.

## 6. Additional Considerations Still Open

- **Empty states** — zero favorites on a fresh facet (beyond the facet-creation flow itself), empty widget hub (F5), at-capacity widget hub (F5), calendar with all events hidden, usage-access not yet granted for Recents/Most Used, notification-listener access not yet granted for badges (F13). Needs explicit design per case.
- **Onboarding flow** — set-as-default-launcher prompt, plus the *timing* of each permission request per the just-in-time principle in §7, and what happens if the user backs out of a given request. Per §7's revocation-handling principle, the same "hide the feature, explain why" pattern likely extends to denial-at-request-time too, but that's worth confirming explicitly rather than assuming.
- **Accessibility** — TalkBack labels for icon-only UI elements (dock, favorites, alphabet rail), support for system font-scale settings without breaking the list/rail layout.

---

## 7. Permissions Summary

**Principle [decided]:** permissions are requested just-in-time, tied to the specific setting/feature being enabled — not bundled into a single upfront onboarding request. E.g., `READ_CALENDAR` is only requested when the user turns on calendar integration in F1; `READ_CONTACTS` only when contact quick-actions are used in F6.

| Permission | Type | Used for | Requested when | Note |
|---|---|---|---|---|
| `READ_CALENDAR` | Runtime | F1 calendar integration | User enables calendar integration | Needs denial fallback UI |
| `PACKAGE_USAGE_STATS` | Special access (Settings redirect) | F2 Recents **and** Most Used (both usage-stats-backed) | User selects either "Recents" or "Most Used" as the home list mode | Cannot be requested via normal dialog; needs an explanation screen before the Settings redirect |
| `READ_CONTACTS` | Runtime | F6 contextual search actions (call/message/WhatsApp) | User first triggers a contact-matched search result, or toggles the capability on if it's user-configurable | Must degrade gracefully to app-only search results if denied |
| `QUERY_ALL_PACKAGES` / `<queries>` manifest element | Manifest declaration | App list (drawer, favorites picker) | N/A — build-time manifest declaration, not a runtime prompt | Required on Android 11+ or app list returns empty/partial |
| Notification listener access | Special access (Settings redirect) | F13 notification badges | User enables badges in settings | Broad grant — listener sees notification metadata for all apps, not just counts; expect more user hesitation than other permissions here |

**Revocation handling [decided, principle level]:** if a previously-granted permission is later revoked via system Settings, the affected feature should detect this and hide itself rather than silently failing or showing broken state — with a message telling the user the feature isn't available without that permission. This is handled **per-feature**, not via a single shared mechanism, since the right UX (what to hide, what message to show) differs by feature. Exact detection/UX detail per feature is still to be designed (see §10).

---

## 8. Non-Functional Requirements (Performance)

Primary validation device: **Samsung Galaxy M15 5G** (MediaTek Dimensity 6100+, 4–6GB RAM, 90Hz AMOLED) — treated as the performance floor, not a best case. Emulator testing is for functional correctness only; performance targets are **not considered passed** until verified on physical hardware.

| Metric | Target | Notes |
|---|---|---|
| Cold start (process not in memory) | < 400ms to first frame | `adb shell am start -W` |
| Warm start (process alive, activity destroyed) | < 150ms | |
| Hot start (resume via Home) | < 50ms, effectively instant | Most common path — measure via frame trace, not `am start` |
| Scroll frame time | ≤ 11.1ms/frame (90fps) on the M15's 90Hz panel; 16.6ms/frame (60fps) as an acceptable minimum floor | Recalibrated for 90Hz reference device |
| Search/filter latency | < 50ms keystroke → rendered | If F6 search is in scope |
| Alphabet rail drag → scroll response | < 1 frame of input latency | Applies to both right rail (F7) and left-edge access (F8) |
| Resident memory, steady state (~150 apps) | < 60–80MB | `dumpsys meminfo` |
| Icon cache footprint | Bounded, explicit cap (~15–20MB LRU) | Not unbounded growth |
| Background CPU/wake usage | Effectively zero when not foregrounded | Event-driven package/usage listeners only, no polling |

**Regression gating [decided]:** manual periodic checks for now, not CI-enforced — revisit automated Macrobenchmark-in-CI later if manual checks prove insufficient to catch regressions.

---

## 9. Technical Notes

- UI: Kotlin + Jetpack Compose for list/grid/rail/facet UI.
- **Min SDK: Android 13 (API 33) [decided].**
- Widget hosting (F5): `AppWidgetHostView` has no native Compose equivalent — wrap via `AndroidView`.
- App list source: `LauncherApps` (preferred over raw `PackageManager` queries for correct multi-user/work-facet behavior, if ever in scope).
- Alphabet Rail (F7): `android.icu.text.AlphabeticIndex` for locale-aware letter bucketing — same component used by the system Contacts app.
- Drag/reorder (favorites, dock, widget hub): native View drag-and-drop APIs are more mature than Compose's for complex reordering-with-feedback; may warrant a Compose+View hybrid for these specific interactions.

---

## 10. Open Questions / Decisions Needed

1. Backup & Restore (F14): widget re-binding on import isn't fully automatic (ids/bindings aren't portable) — needs a defined guided-reimport UX, not just "restore the file."
2. Permission revocation/denial handling (§7): principle is decided (hide feature + explain), but exact per-feature detection and messaging still needs individual design passes.
3. Home Clock Widget Substitution (F15): proposed, not yet greenlit for a phase — needs a decision on whether it ships at all before implementation work is scheduled.
