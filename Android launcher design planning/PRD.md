# PRD: Minimal Android Launcher

**Status:** Draft — requirements gathering
**Reference / inspiration:** Niagara Launcher
**Primary test device:** Samsung Galaxy M15 5G (Dimensity 6100+, 4–6GB RAM, 90Hz AMOLED) — see Non-Functional Requirements
**Language/stack (tentative):** Kotlin + Jetpack Compose, with native `AppWidgetHostView` wrapped via `AndroidView` for widget hosting

---

## 1. Overview

A minimal, fast, list-first Android home screen launcher. Prioritizes speed and low visual clutter over customization depth. Core interaction model: a scrollable alphabetical app list (not a grid) as the primary home surface, favorites-first, with grid-based options available where explicitly configured (app drawer).

## 2. Goals

- Feel instant — see performance targets in §8.
- Minimize visual noise on the home screen: no default icon grid, no forced widgets.
- Let power users configure multiple home "profiles" without duplicating shared elements (dock).
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

### F2. Home screen — Favorites / Recents / Most Used
- User chooses which "mode" populates the home list: Favorites (manually curated), Recents (top 3–5), or Most Used.
- **Favorites:** user explicitly adds/removes/orders apps.
- **Recents [decided]:** backed by system-wide usage stats via `UsageStatsManager` (not self-tracked launch events) — same data source and permission requirement as Most Used below.
- **Most Used:** requires `PACKAGE_USAGE_STATS` special-access permission, which cannot be requested via a normal runtime dialog — user must be sent to system Settings (`ACTION_USAGE_ACCESS_SETTINGS`). Needs an onboarding explanation screen and a graceful "not yet granted" empty state. **This permission is now required for both Recents and Most Used**, since both are usage-stats-backed.
- Behavior when a favorited/pinned app is uninstalled: auto-remove and collapse the list (no dead entries or placeholder tiles), across favorites, dock, and all profiles.

### F3. Dock
- 3–5 apps, user-configured count and content.
- **Shared across all profiles** (single dock, not per-profile).
- Same uninstall-handling rule as F2 applies here.

### F4. Profiles
- Multiple home "profiles," each with its own Favorites/Recents/Most-Used configuration and list content.
- Dock is shared (per F3) — not duplicated per profile.
- Swipe right to switch between profiles.
- **First profile is not special [decided, revised]:** there's a "first" profile positionally (created by default on first launch), but it carries no protected status — it can be deleted like any other profile. The only hard rule is that **at least one profile must always exist**; deleting the last remaining profile isn't allowed.
- **Profile count [decided, revised]:** the launcher starts with exactly 1 profile by default. A maximum of **3 profiles** applies for now — implemented as a single hardcoded constant, not a user-facing setting, so it's a trivial one-line change to raise later rather than something requiring UI/settings work. No sane-ceiling question remains since it isn't user-adjustable in this version.
- **Empty-state / creation model [decided]:** each new profile starts empty and is presented as a setup prompt. **Three entry points to add a profile, all decided:**
  1. The trailing "add profile" placeholder page after the last real profile in normal swipe-through navigation, up to the constant max.
  2. Launcher Settings → Profiles → an explicit "add profile" action.
  3. Within the zoomed-out profile management view (see below), scrolling right past the last page surfaces the same add-profile button in place.
  Once the max (3) is reached, none of these three affordances is shown.
- **Deletion [decided]:** deleting a profile removes that page outright; remaining profile pages re-order to fill the gap. The delete action must be disabled/blocked when it's the last remaining profile, since at least one must always exist. **A confirmation dialog is required before deletion completes** [decided] — resolves the earlier open question.
- **Renaming & reordering [decided]:** profiles can be renamed and manually reordered — resolves the earlier open question.
- **Access paths for profile management [decided], two entry points:**
  1. **Settings list view:** Launcher Settings → Profiles opens a draggable list of all profiles for reordering, with rename accessible per-item (e.g. tap to edit name).
  2. **Zoomed-out visual overview:** long-pressing on an **empty area of the home screen** (not an app icon — see disambiguation below) opens a zoomed-out view of all profile pages side-by-side, similar to Nova Launcher's home-screen page management. From here the user can drag pages to reorder them directly, visually remapping profile position rather than working from a list.
  - **Disambiguation [decided]:** this long-press target is distinct from F12's long-press-on-app-icon context menu — the two don't conflict since they're triggered by long-pressing different regions (empty background vs. an icon), but this needs explicit handling in the gesture-detection logic so a long-press starting near an icon's edge doesn't ambiguously trigger the wrong one.

### F5. Launcher Hub (widgets)
- Swipe left from home to reach a dedicated screen for user-added third-party widgets.
- **Layout model [decided, revised]:** single screen only (no paging). Widgets are placed on a **fixed grid** (columns × rows, same convention as F6) rather than freeform — placement snaps to grid cells, and **no two widgets may overlap**. The grid scrolls vertically if content exceeds the visible area.
- **Grid size [decided]:** configured **separately** from the App Drawer's grid setting (F6) — the hub has its own independent columns × rows configuration. **Default for now [decided]: 5 columns × 6 rows** (matching the columns × rows convention used throughout this doc — the user specified this as "6 rows × 5 columns"), within a single screen, scrolling vertically as more widgets are added beyond what fits.
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
- Needs explicit gesture-zone definition so this doesn't conflict with any left-edge system gestures (e.g. Android's back-gesture edge swipe) or with F5's swipe-left-for-hub gesture, since the app drawer is a distinct screen from the home screen where that gesture is defined.

### F9. Backgrounds
- No custom wallpaper engine. Launch the system `ACTION_SET_WALLPAPER` (or equivalent) intent so the user sets wallpaper via the OS's own picker.
- Launcher reads the current system wallpaper (via `WallpaperManager`) to render behind its own UI.

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
- Show badges on the home screen, next to the app name in the favorites list — a **dot indicator, not a numeric count** [decided].
- **No per-app opt-out [decided].** Badge visibility follows the same suppression rules the system notification shade already applies — a notification that the system itself treats as silent (e.g. posted to a low-importance/silent notification channel) does not surface a badge, consistent with what the user already expects from that app's shade behavior rather than an independent always-on count.
- Requires **notification listener special access** — a heavier permission flow than usage stats: the user must grant it via a system settings screen (`ACTION_NOTIFICATION_LISTENER_SETTINGS`), and it's a broad grant (the listener sees notification metadata for all apps, not just badge counts), which typically prompts more user hesitation than other permissions here.
- Requested just-in-time when the user enables badges in settings, consistent with the permissions principle in §7.
- **Remaining implementation detail (not a design open question):** exact mapping from `NotificationListenerService`'s active notification list to "show a dot" — filtering by `NotificationChannel` importance (excluding `IMPORTANCE_NONE`/silent channels) and any per-notification silent flags, deduped appropriately per app.
- Dock badge visibility not yet addressed — dock is icon-only (no app name shown), so the "next to app name" placement doesn't directly apply there; worth a follow-up decision on whether dock icons get a dot-only variant.

### F14. Backup & Restore [decided, in scope]
- **Format:** local JSON export, with import to reconstruct settings from that file.
- **Scope [decided]:** not limited to favorites/profiles/dock — also includes widget hub state (widget ids, provider component names, grid positions/sizes), icon/label customization (F11), and other user settings (drawer grid size, hub grid size, gesture-related toggles if any become configurable, etc.). Effectively a full settings snapshot, not a partial one.
- **Not yet addressed:** widget ids and bindings are inherently tied to the specific device/OS state (`AppWidgetHost` ids aren't portable across installs or devices) — a restored JSON can reasonably reconstruct *which* widgets were placed and where, but re-binding them (going through the bind/configure flow again per widget) likely can't be fully automatic and may need a guided re-add step during import. Worth flagging as a real implementation constraint, not a simple file copy.
- No cloud sync in this version (per §3 Non-Goals) — export/import is a manual, user-initiated local file operation (e.g. share sheet / file picker), not automatic background backup.

## 5. Gesture Map

Consolidated so overlapping swipe directions across screens don't get implemented ad hoc per feature:

| Screen | Gesture | Behavior |
|---|---|---|
| Home | Swipe right | Switch to the next profile |
| Home | Swipe left | Open the Launcher Hub (F5) |
| Home | Swipe up | Open the App Drawer (F6) |
| Home | Swipe down | **[decided, corrected]** Swipe down always opens the notification shade — but if the favorites list is not already scrolled to the top, the swipe first scrolls the list back to the top; only once the list is at the top does a further swipe-down open the shade. Swiping up always scrolls the list down without ever leaving the home screen. The rest of the home screen (clock/calendar area, dock) does not scroll independently. |
| Home | Long-press on empty area | **[decided]** Opens the zoomed-out profile management view (F4) for visual reordering. Distinct from long-pressing an app icon, which opens F12's context menu instead. |
| App Drawer | Swipe down | Closes the drawer only if the list is already scrolled to the top; otherwise behaves as a normal scroll. |
| App Drawer | Back button / back gesture | Closes the drawer. |
| App Drawer | Alphabet rail drag | Right edge (F7) or left edge (F8) — jumps to the letter under the finger. |

**Resolved:** the home swipe-down mechanic follows standard scroll physics — swipe down scrolls the favorites list toward the top; once at the top, a further swipe-down opens the shade. Swipe up always scrolls the list further down and never triggers the shade.

## 6. Additional Considerations Still Open

- **Empty states** — zero favorites on a fresh profile (beyond the profile-creation flow itself), empty widget hub (F5), at-capacity widget hub (F5), calendar with all events hidden, usage-access not yet granted for Recents/Most Used, notification-listener access not yet granted for badges (F13). Needs explicit design per case.
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

- UI: Kotlin + Jetpack Compose for list/grid/rail/profile UI.
- **Min SDK: Android 13 (API 33) [decided].**
- Widget hosting (F5): `AppWidgetHostView` has no native Compose equivalent — wrap via `AndroidView`.
- App list source: `LauncherApps` (preferred over raw `PackageManager` queries for correct multi-user/work-profile behavior, if ever in scope).
- Alphabet Rail (F7): `android.icu.text.AlphabeticIndex` for locale-aware letter bucketing — same component used by the system Contacts app.
- Drag/reorder (favorites, dock, widget hub): native View drag-and-drop APIs are more mature than Compose's for complex reordering-with-feedback; may warrant a Compose+View hybrid for these specific interactions.

---

## 10. Open Questions / Decisions Needed

1. Backup & Restore (F14): widget re-binding on import isn't fully automatic (ids/bindings aren't portable) — needs a defined guided-reimport UX, not just "restore the file."
2. Permission revocation/denial handling (§7): principle is decided (hide feature + explain), but exact per-feature detection and messaging still needs individual design passes.
3. Notification Badges (F13): dock icons are icon-only (no app name), so the "dot next to app name" placement doesn't directly apply there — needs a follow-up decision on whether dock gets a dot-only variant.
