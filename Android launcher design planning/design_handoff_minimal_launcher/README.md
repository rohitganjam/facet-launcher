# Handoff: Minimal Android Launcher

## Overview

A minimal Android home-screen replacement. Instead of an icon grid, the home surface shows a large clock, the next calendar events, a short list of app names, and a fixed dock. All apps live in one alphabetical drawer reached by swiping up. Users can keep up to three **facets** — separate home configurations (clock style, list content, favorites) that share one dock.

This bundle covers two surfaces in full (Home, App Drawer) plus the Launcher Hub, first-run, settings, facet management, pickers, and permission-denied states. Source requirements are in `PRD.md`.

## About the Design Files

`Launcher.dc.html` and `Launcher Dark.dc.html` are **design references created in HTML** — prototypes showing intended look and behavior. They are not production code to copy.

The task is to **recreate these designs in the target codebase's environment**. For a real launcher that means native Android: Kotlin + Jetpack Compose, using `LauncherApps`, `AppWidgetHost`, `NotificationListenerService`, `UsageStatsManager`, and a `HOME`-category activity. Nothing in these files should be ported literally — treat them as the visual and behavioral spec.

Open either file in a browser. Both are single self-contained pages laid out as a **design canvas**: turns stacked newest-first, each option carrying a visible id badge (`1a`, `2c`, `3d`, `4f`…). References below use those ids.

`support.js` is the preview runtime for these files. It has no role in the implementation.

## Fidelity

**High-fidelity.** Final colors, typography, spacing, and interaction model. Recreate pixel-for-pixel where the platform allows, substituting Material 3 components where they'd behave better than a hand-built equivalent (switches, dialogs, bottom sheets).

Two deliberate placeholders:

- **App icons are monogram tiles** (`#1e293b` square, white letter). This doubles as the monochrome icon mode described in the PRD. In the real launcher, "System default" mode loads the actual adaptive icons; "Monochrome" tints them to a flat single colour.
- **Large hub widgets are striped placeholder blocks.** Real widgets render through `AppWidgetHostView`. Small widgets in the mocks use generic chrome (label + text bars) purely to show scale.

App names are invented. No real products are depicted.

---

## Screen index

| id | Screen | Notes |
|---|---|---|
| `1a` | Home + drawer prototype | Interactive: swipe up, long-press, search, rail |
| `1b`–`1d` | Clock style variants | Light stack / Rule / Date-forward |
| `1e`–`1g` | Favorites density variants | Airy / Compact / No-icon |
| `1h`–`1i` | Drawer list vs grid | |
| `1j` | Search with contact quick actions | |
| `2a` | Long-press sheet | |
| `2b`, `2c` | Superseded by `3a` / `3c` | Kept for reference only — do not implement |
| `2d` | Clock style as its own page | Adopted; see `3e` |
| `3a` | Facet carousel | **Revised:** recent-apps style — swipe browses, tap any card (centered or peeking) applies it immediately. Uniform card scale (~85%), no separate centered/neighbour tiers. |
| `3b` | Facet reorder list | **Revised:** no longer an in-carousel long-press-drag state. A "Reorder" header link swaps the carousel for a plain list — drag handle, name, overflow menu (Facet settings / Delete) per row, plus a pinned Add-facet row. |
| `3c` | **Launcher settings** | Canonical settings screen. **Restyled:** a Pro card, then four tonal cards (Facets, Appearance, Home & apps, System) of icon-badge rows — 28px corners, no shadow or border, no chevrons. |
| `3d` | **Per-facet settings** | **Restyled:** the same card layout — a Rename card, an Appearance card (holds the facet's Clock Style), and a Home & apps card (Apps list, Dock, Calendars). |
| `3e` | Clock style page (default) | Built as the **clock style gallery** (`ClockStyleGalleryScreen`): a card per template with a live preview, plus font, colour, accent, 24-hour, meridiem, date and alignment options. Reached from the Appearance screen's **Clock Style** row. |
| `3f` | Clock style page (facet override) | The same gallery scoped to a facet, behind the Inherit/Override switch (two radio rows). |
| — | **Calendar settings** [new screen] | Reached from the Settings **Calendars to display** row or a facet's Calendars row. "Show all-day events" toggle plus the calendar picker once `READ_CALENDAR` is granted (the `4p` permission-denied strip until then — see `4l`). |
| `4a`–`4e` | Launcher Hub: populated, empty, add picker, at capacity, orphaned widget | |
| `4f`–`4h` | First run: what it is → pick favorites → set as default | |
| `4i` | App long-press menu | |
| `4j`–`4l` | Pickers: favorites, dock, calendars | |
| `4m` | Rename dialog | App labels and facet names |
| `4n`–`4o` | Add facet: placeholder page, then created | |
| `4p` | Permission-denied and empty states | |
| `4q` | Search with no results | |
| `fa1`–`fa9` | Facet automation: settings entry, rule list, rule editor, trigger types, permission and empty states | In `facet-automation.html` |

---

## Screens

### Home (`1a`)

**Purpose.** Glanceable time and next commitments; launch a handful of apps without scanning icons.

**Layout.** Single column, `padding: 0 24px 22px`, full-bleed wallpaper behind. Vertical order:

1. Status row — `padding: 14px 24px 0`, `500 11.5px`, `rgba(2,8,23,.55)`. Time left, signal/battery glyphs right.
2. Clock block — `padding-top: 52px`.
   - Time: `200 72px/1`, `letter-spacing: -4px`, `#020817`.
   - Date: `400 13px`, `rgba(2,8,23,.5)`, `margin-top: 10px`. Format `Wednesday, 26 August`.
   - Event rows: `gap: 9px`, `margin-top: 20px`. Each row is `[2×13px rule] [time, min-width 48px] [title]`, `400 12.5px`. Next event's rule is the accent; later ones `rgba(2,8,23,.25)`. Time column `rgba(2,8,23,.42)`, title `rgba(2,8,23,.62)`.
3. Flex spacer — pushes the rest to the bottom.
4. Section label — `500 10px`, `letter-spacing: .1em`, `rgba(2,8,23,.34)`, followed by a 1px hairline and the facet dots. Text is the list mode: `FAVORITES` / `RECENTS` / `MOST USED`.
5. App list — rows of `padding: 10px 0` (revised from `8px 0` for more breathing room between rows), `gap: 14px`. Icon `34×34`, `border-radius: 10px`. Name `400 18px/1`, `letter-spacing: -.2px`. Notification badge trailing the name — **[revised]** a per-user choice between a `6px` dot circle and a numeric count pill (capped at `9+`), both in the accent color; see F13/Notification Settings. A fixed `16px` spacer follows the list, before the dock (or the screen's own bottom padding if the dock is empty).
6. Dock — **0–5 apps, user-configured [revised]:** no floor — removing every app is allowed, and the dock row is omitted entirely (not shown empty) when it has zero apps. `justify-content: space-between`, `padding: 0 4px`. Icons `50×50`, `border-radius: 15px`. Badge is the same dot/count choice as the favorites list, at `top:-1px; right:-1px`. **Known gap:** the `2px` wallpaper-coloured separator ring around the dock badge is spec'd but not yet implemented (Material3's `BadgedBox` renders the badge with no ring today).
7. Gesture bar — `104×4`, `border-radius: 2px`, `rgba(2,8,23,.18)`, `margin-top: 16px`.

**Minimum hit target is 44px** — the 18px name rows meet this via padding; verify at large system font scales.

#### Clock variants

- **`1b` Light stack** (default) — as above.
- **`1c` Rule and meridiem** — `300 62px` time with a `500 14px` `AM` baseline-aligned beside it, `letter-spacing: .06em`. A 1px hairline `margin: 18px 0 0`, then next event left / date right at `400 12.5px`, then `+2 more today`.
- **`1d` Date-forward** — `500 30px/1.15` weekday + date, time demoted to `200 40px` at `rgba(2,8,23,.42)`. Shown here with the calendar-denied strip.

#### Favorites variants

- **`1e` Airy** (default) — 5 apps, 34px icons, 18px names, dot trails the name.
- **`1f` Compact** — 8 apps, 26px icons, `400 15.5px` names, `6px 0` rows, dot leads in a 5px gutter so names stay flush.
- **`1g` No icons** — 4 apps, `300 25px` names on `10px 0` rows with `1px` bottom hairlines, `7px` dot on the right edge.

### App Drawer (`1a`, `1h`, `1i`)

**Presentation.** Translucent overlay on the wallpaper — `rgba(255,255,255,.88)` light, `rgba(20,23,29,.92)` dark. **Not blurred**: the home layer fades to 0 and scales to `.97` as the drawer rises, so only the wallpaper tone shows through. Transition `transform .34s cubic-bezier(.32,.72,0,1)`, opacity `.24s ease`.

**Search bar.** `padding: 56px 24px 12px`. A 14px ring glyph, then the field at `400 15px`, then the List/Grid segmented control (`2px` padding, `border-radius: 7px`, active pill white with `500 10.5px` label). Bottom hairline `rgba(2,8,23,.1)`, becoming a `1.5px` accent rule when focused (`1j`).

**List layout (`1h`).** Content `padding: 4px 44px 40px 24px` — the right inset clears the rail. Letter headers `600 10px`, `letter-spacing: .14em`, `rgba(2,8,23,.3)`, `padding: 14px 0 4px`. App rows `6px 0`, 32px icons, `400 16px` names.

**Grid layout (`1i`).** `repeat(5, 1fr)`, `gap: 20px 8px`. Icons `44×44`, `border-radius: 13px`. Labels `400 9.5px/1.2`, centred, `max-width: 56px`, ellipsised.

**Alphabet rail.** 34px column pinned to the right edge, vertically centred, `gap: 1px`. Letters `500 9px`, `rgba(2,8,23,.3)`; active letter switches to the accent at weight 600. Dragging scrubs — pointer position maps linearly to the letter array and scrolls that header to `offsetTop - 8`. A `200 64px` accent letter at 16% opacity sits behind the list, left-aligned and vertically centred, as the scrub indicator. **Left-edge jump is always on**; both edges reserve space for it, so it is not a setting.

**Search results order (`1j`).** Apps → contacts → contact actions. Contact rows use a 32px circular avatar with initials. Action chips `padding: 7px 15px`, `border-radius: 8px` — primary filled, others `1px` outline. WhatsApp is hidden when not installed. Contacts section is omitted entirely when access is denied — never shown empty.

**No results (`4q`).** `400 16px` "Nothing matches "zqx"", explanatory line at `400 12.5px/1.6`, and a Clear search outline button. No web fallback.

### Long-press sheet (`2a`) — removed

> **Superseded.** This bottom sheet was deleted during implementation: three of its rows found other homes (Launcher settings → the carousel screen; Change wallpaper → a Settings row; Edit facet → the carousel's per-card menu) and the last, **Switch facet**, became the whole gesture's target — first as a direct long-press to the carousel, now as a **left swipe on Home** (the long-press was too hard to land in the gaps between the clock, app list and dock). The original spec is kept below for reference only.

Long-press empty space on the home surface for **420ms**; movement over 8px cancels. The home layer dims under `rgba(2,8,23,.28)`; the sheet rises from the bottom, `border-radius: 20px 20px 0 0`, `transform .3s cubic-bezier(.32,.72,0,1)`, `box-shadow: 0 -8px 24px rgba(2,8,23,.1)`. A `34×4` grab handle sits centred above the rows.

Four rows, each `13px 24px` with `1px` separators, title `400 16px` and subtitle `400 11.5px` — the row list below was corrected against the `2a` screenshot (`screenshots/light-turn2-sheet-and-settings.png`), which shows a "Launcher settings" row this section's prose previously omitted:

| Row | Subtitle | Destination |
|---|---|---|
| Switch facet | `Currently Focus — 2 of 3` | `3a` |
| Edit facet | `Clock style, list content, favorites` | `3d` |
| Launcher settings | `Clock, favorites, drawer, badges` | `3c` |
| Change wallpaper | `Opens the system picker` | System picker |

### Facet carousel (`3a`, `3b`, `4n`, `4o`)

**Purpose.** Switch the active facet, reorder facets, reach per-facet settings.

> **Revised from the original mock, decided during implementation:** the carousel now behaves like a recent-apps switcher rather than a browse-then-confirm picker. There's no **Select** button — tapping any visible card applies it immediately and closes. Since tapping a card now always applies it, long-press-to-reorder-in-place is gone; reordering moved to a dedicated list screen (`3b`, redefined below), reached via a **Reorder** header link instead of a sub-line hint. Tap-the-name-to-rename is also gone — rename now lives in Per-facet settings (`3d`), reached from the carousel's gear icon or the reorder list's overflow menu.

> **Revised again during implementation:** `3a` (Switch Facets) and `3b` (Manage Facets) are now two separate screens.
> - **`3a` Switch Facets** — opened by a **left swipe on Home** *(revised — was the empty-space long-press)*. **Revised once more:** no longer a `NavHost` destination at all — like the Hub, it's a permanent **follow-finger panel** to Home's right (translucent, 60% `SurfaceContainer`), tracking the finger continuously rather than committing on release-then-animating. The left swipe that opens it and the rightward swipe that closes it (in empty space, or on the first facet's card, where the pager has nowhere to browse) drive the exact same axis live — no separate "slide in" transition step any more, since there's no navigation event to transition between. Home stays put behind it. **No header** — just a right-aligned secondary **Reorder** button (border + light fill, disabled with one facet) that opens `3b`. No back button: system back, that rightward swipe, or tapping a card (which applies it and closes the panel), is the way out.
> - **`3b` Manage Facets** — a normal settings screen (sticky header "Manage Facets" + back chevron, opaque `SurfaceContainer`), reached from Settings → Facets *and* the carousel's Reorder button. The drag-to-reorder / add / per-facet-settings / delete list.

**Carousel.** Every page (facet or the trailing Add page) is a scale model of the device screen (`CAROUSEL_CARD_SCALE` of its width *and* height, so it keeps the phone's own aspect ratio), with `shadow` + hairline border. Each page is a **live snapshot of that facet**: the real `ClockBlock` plus that facet's actual favorites — or `No favorites yet`. The facet name sits above the card as a plain label; gear/trash sit below it.

**Add page.** Sits after the last facet, up to 3 total. Dashed `rgba(2,8,23,.26)` border, centred `+` at `300 34px`, then `Add facet` and `Copies this facet's settings. Favorites start empty.` (`4n`). Reserves the same footer space (gear/trash row, invisible not omitted) as a real facet page, so it renders at the identical height.

**Actions.** Gear and trash glyphs centred *below* the page, above the indicator — `gap: 26px`, 19px, 55%/50% opacity. No full-width buttons. Gear opens Per-facet settings (`3d`) for the centred facet; trash opens the delete confirmation.

**Indicator.** Page dots below the actions; active dot is a `16×5` rounded bar, inactive are `5px` circles.

**Behavior.**
- Horizontal swipe browses, clamped at both ends — **no infinite scroll**. Browsing alone never applies anything.
- **Tapping any card — centred or a peeking neighbour — applies it immediately and closes.** Back (nav bar or gesture) returns to wherever the carousel was opened from without applying anything.
- **Add facet** (`4n` → `4o`): silently duplicates the previous facet as `Facet N`, **copying its settings but not its favorites**. The new page becomes the centred one; its favorites area shows `No favorites yet`. Gear and trash are dimmed to 22% on the placeholder page and become active once created.
- **Preview cards also render the dock [revised]:** since the dock is shared across every facet (not per-facet), each preview card shows the same live dock row beneath its favorites — same `16px` gap / omit-when-empty behavior as the real Home screen.

**Reorder list (`3b`, revised).** Tapping **Reorder** swaps the whole carousel — pager, indicator dots, gear/trash row — for a plain vertical list, one row per facet: a drag-handle glyph on the left, the facet name, and an overflow (`⋮`) menu on the right with **Facet settings** and **Delete**. A non-reorderable **Add facet** row is pinned at the bottom. Dragging a row by its handle live-reorders the list (other rows animate out of the way); releasing commits the new order immediately. The header's **Reorder** link becomes **Done**, returning to the carousel; system back does the same rather than exiting the Facets screen entirely.

### Launcher settings (`3c`)

Canonical settings screen, restyled to current Android/Material settings conventions (see Design Tokens). It opens with the **Facet Pro card** (a dark upsell card, or a one-row "Pro is active" summary), then four sections, each a **tonal card**: a `Surface` fill on a dimmer page tone, **28px corners, no shadow or border**, rows separated by `1px Hairline` dividers inset past the icon. Each row has a **40px icon badge** (an outlined 22px glyph on a circle tinted at 14% in light / 24% in dark of the section's hue), a `400 14px` title and `400 11.5px` subtitle, **no chevron** (every row on these lists navigates), and a **full-width press highlight** that follows the card's rounded corners. Section headers are sentence case, `labelLarge`, in the accent colour. Screen title `500 26px`, `letter-spacing: -.6px`.

Dropdown and switch rows are deliberately **control-only**: only the value or the switch reacts, not the whole row. A row whose action is the row itself (navigation, a radio option, a permission that needs turning on) is tappable across its width.

| Section | Rows |
|---|---|
| **Facets** (blue) | **Facets** → the **Manage Facets** list (`3b`) — reorder, add/remove, reach per-facet settings. (The swipeable **Switch Facets** carousel opens on a left swipe on Home instead.) **Facet automation** → rules that switch facets by schedule or device trigger (`fa1`–`fa9`). |
| **Appearance** (violet) | **Change wallpaper** → the system picker. **Launcher Appearance** → the Appearance screen: a **Clock** card (the **Clock Style** row → `3e`), **Dock & home** (dock display style, app list layout, position, presentation and alignment), and **General** (theme, system bar icons, accent colour, icon style, **icon shape** — Squircle / Rounded / Circle / Square, the selected one drawn with a 2px accent ring and a 2px gap — launcher font, font colour, size and weight), above a live preview card. |
| **Home & apps** (teal) | **Calendars to display** → calendar settings. **Dock** → pick apps and reorder. **Home Apps List** → content mode (Favorites / Recents / Most used), apps to show, default favorites. **App Drawer** → presentation (List / Grid), grid or list size, show icons / labels, search, search bar position, drawer opacity. **Notifications** → on/off and Dot / Count badge style (routes to the access explanation if not granted). **Folders** → create, rename, reorder, delete. |
| **System** (slate) | **Permissions** → per-permission status with a **Turn on** row for Calendar, Contacts, Usage access, Notification access, Bluetooth and Location. **Backup & restore** → export or import a settings file. **Set as default launcher**, a Work profile status row when one exists, and **About**. |

### Per-facet settings (`3d`)

Reached from the Home long-press sheet's **Facet settings**, the carousel's gear, or the Manage Facets overflow menu. The header shows the facet name with an **Activate facet** button (a "✓ Active" mark when it is already the active facet). It uses the same card layout as `3c`: a **Rename facet** card (→ `4m`), an **Appearance** card (→ the Appearance screen scoped to this facet, including its **Clock Style**), then a **Home & apps** card with **Apps list settings**, **Dock settings** and **Calendars to display**. Each destination screen carries its own Inherit/Override switch (two radio rows, tappable across the row), so a facet stores only what differs from the launcher defaults.

Footnote: accent, icon style and shape, drawer layout and notification dots are launcher-wide. A new facet inherits the launcher defaults and does not copy another facet's settings.

### Clock card (`3d`) and Calendar settings (new screen, wraps `4l`)

> **Current state:** the facet's clock options no longer sit in a Clock card on the per-facet settings screen. The **Clock Style** row lives on the Appearance screen (scoped to the facet there), opens that facet's own clock style gallery, and the Inherit/Override pattern described below now sits on each destination screen. The text below is the original design history.

**Revised and expanded from the original `3e`/`3f` clock-style-only design:** the facet's **CLOCK** card now covers three rows — Clock style, 24-hour time, Calendar — behind **one shared Inherit/Override switch** (not a separate switch per row), reusing the exact `3e`/`3f` radio-row copy and tinting:

- **Inherit default** — shows the global default's current values (e.g. `Light stack · 12-hour time`), read-only
- **Override for this facet** — tints the switch's own selected row `rgba(37,99,235,.04)`; the three rows below become independently editable for this facet only

Only **24-hour time** is a real, independently-persisted override today (Clock style stays inherit-only — no second style exists yet, Known Gap). **Calendar** navigates to a **new Calendar settings screen**, reached from either the global Settings Clock card or a facet's Clock card:

- **Show all-day events** — a real toggle. From the global entry point it writes the launcher-wide default directly; from a facet's entry point, its own separate Inherit/Override switch (same two-radio-row pattern) governs whether it reads/writes the global value or this facet's own.
- **Calendars to display** — the `4p` permission-denied strip (`Calendar access off — events hidden.` / `Turn on`), since `READ_CALENDAR` isn't requested yet and there's no live calendar list to check off. Phase 4 replaces this card's contents with the real picker.

The original full clock-style live-preview page (`3e`/`3f`, mini clock renders per style) remains unbuilt — only "Light stack" exists, so the "Clock style" row inside the card stays a disabled placeholder regardless of Inherit/Override state.

### Launcher Hub (`4a`–`4e`)

**Grid geometry.** 5 fixed columns. Row height derives from the configured cell size (5×6 by default → 88px rows at 372px width), `gap: 8px`, `padding: 0 24px`. Six rows fit the screen; the grid **scrolls vertically** beyond that, up to **50 rows**. Widget cap is **20 widgets**.

**Header.** `500 17px` "Hub", sub-line `400 11.5px` `6 of 20 widgets · 5 columns`, accent **Add** right.

- **`4a` Populated** — widgets snap to cells and never overlap. Long-press to resize (respecting the provider's declared minimum) or remove.
- **`4b` Empty** — a 96×70 dashed `+` block as the centred content, `400 17px` "No widgets yet", explanation, and a filled **Add widget** button. Not a caption under a blank grid.
- **`4c` Add widget** — grouped by provider, each showing its declared sizes as labelled previews (`Month 2×2`, `Agenda 4×2`). Header count reads `14 left`. Footer notes that bind dialogs and configure activities resolve before the widget counts as added.
- **`4d` At capacity** — `20 of 20`, **Add** greyed to `rgba(2,8,23,.3)`, a dashed strip reading `Hub is full at 20 widgets — remove one to add another.` with a **Manage** action, and the grid clipped at the fold with a gradient fade to show it scrolls.
- **`4e` Orphaned widget** — provider uninstalled. The cell keeps its footprint, dashed border, and reads `Ledger widget unavailable` / `The app was uninstalled` with **Remove** and **Keep space**. Never a dead frame. On provider *update*, the widget reloads in place.

### First run (`4f`–`4h`)

Three screens. Permissions are **not** requested here — they stay just-in-time.

1. **`4f` What this launcher is** — `200 44px/1.1` headline "A home screen that stays quiet.", `400 14px/1.65` body, then three icon+label lines. `padding: 96px 32px 0`.
2. **`4g` Pick a few favorites** — `500 24px` title, `Up to 8. You can change them any time — 3 of 8 chosen.` Checkbox list, 30px icons, `400 15.5px` names. **Skip** beside **Next**.
3. **`4h` Set as default** — bottom sheet over the dimmed home surface *(revised: the shared themed modal bottom sheet, content padded 24dp under the buttons so it clears the nav bar; skipped entirely when Facet already holds the home role, going straight to the gesture hints)*. `500 18px` title, explanation that Android will confirm and the choice is reversible, a bordered row showing this launcher as the home app, then **Set as default** / **Later**. Footnote: `Permissions come later, one at a time, only when a feature needs them.`

Pagination dots bottom-left, primary action bottom-right, `padding-bottom: 34px`.

### App long-press menu (`4i`)

Anchored card, `border-radius: 14px`, `box-shadow: 0 16px 40px rgba(2,8,23,.22)`, `left/right: 20px`. Header row: 34px icon, app name at `400 15px`, `2 notifications` beneath. Then four rows at `14px 18px`, `400 14.5px`:

- App info
- **Uninstall** — in the error colour
- Widgets — `2 available`, chevron
- Rename label — chevron → `4m`

Same menu from the dock, favorites list, and drawer. Renaming changes the launcher's label only.

### Pickers (`4j`–`4l`) and rename (`4m`)

- **`4j` Favorites** — full-screen, searchable, checkboxes, `5 of 8 chosen`. At 8, unchecked rows grey out until one is removed. Reordering happens on the facet page, not here.
- **`4k` Dock** — same shape; current dock shown as a 44px icon row with a dashed `+`. **Up to 5 apps, no minimum [revised — was `3 to 5 apps`]**, shared across every facet. Removing collapses the row — no empty slot; removing the last app empties the dock entirely, and Home/the carousel preview then omit the dock row rather than showing it empty.
- **`4l` Calendars** — account rows with email beneath, checkboxes; still unbuilt (no `READ_CALENDAR` request/query yet — see the new Calendar settings screen above, whose "Calendars to display" card is where this list lands once built). Footnote: tapping an event opens the calendar app *at that event*. The **Hide all-day events** switch moved to the new Calendar settings screen as a real, independent "Show all-day events" toggle rather than living inside this picker.
- **`4m` Rename** — centred dialog, `border-radius: 14px`. Title, explanation, then the value on a `1.5px` accent underline with a **Reset** affordance, then Cancel / Save. Used for both app labels and facet names.

### Permission-denied and empty states (`4p`)

**One pattern everywhere:** an inline strip replaces the content it would have shown, carrying the control that fixes it. Never a full-surface takeover, never a silently empty section.

Strip: `padding: 11px 13px`, `1px dashed rgba(2,8,23,.16)`, `border-radius: 10px`, message at `400 11.5px/1.4` muted, action at `500 11.5px` in the accent.

| Context | Message | Action |
|---|---|---|
| Clock, calendar denied | Calendar access off — events hidden. | Turn on |
| Home list, usage access missing | Most used needs usage access from system settings. | Open settings |
| Fresh facet, no favorites | Nothing here yet — pick up to 8 apps. | Add apps |
| Search, contacts denied | Contact results need contacts access. | Turn on |
| Facet automation, no rules | Add a rule to switch facets automatically. | Add rule |
| Facet automation, rule's permission revoked | Row subtitle: Needs Bluetooth access / Needs location access | Tap the row to allow |
| Rule editor, permission refused | Bluetooth access is off, so this trigger can't be used. Allow it in Settings. | Open settings |

---

### Facet automation (`fa1`–`fa9`, `facet-automation.html`)

Rules that switch the active facet by themselves. Entry: Settings → Facets → **Facet automation**, a row under *Manage facets* (subtitle: rule count or "No rules"). Open `facet-automation.html` in a browser for all nine screens in light and dark. Built as spec'd, with the deviations listed last.

- **List (`fa2`).** A status line first, only when a rule is running: "Showing Work because of the rule …", or, after the user picks a facet by hand, "Showing Travel, the facet you chose. … is paused until it ends." Then a **Rules** card: one row per rule (trigger icon, derived title such as "Weekdays · 9:00–18:00" or "Car · connected", subtitle "Work, then previous facet", enable switch) and an accent **Add rule** row, hidden at the free limit. Then a **Use other apps** card explaining the per-facet "Switch to" shortcuts. Empty state is the dashed strip from `4p`.
- **Rule editor (`fa3`, `fa5`, `fa9`).** A bottom sheet (closes on Home press): *Switch to* (facet dropdown), *When* (trigger dropdown), the trigger's own fields, *When it ends* (radios: Return to previous facet, Stay on the target, and "Switch to" with its facet dropdown on the same row), Cancel / Save, and a Delete action only when editing.
  - **Schedule:** seven M3 filter chips (small shape, 8dp; selected = accent-tinted fill and accent border; full day names announced), From and Until time rows in the phone's 12/24-hour format. An Until before From shows "Ends the next day". No days selected blocks Save with "Pick at least one day".
  - **Bluetooth:** a two-tab pill (the Apps/Folders tab component) for while connected / while not connected, then a *Device* dropdown of paired devices. The device is picked, never typed; no device blocks Save.
  - **Wi-Fi:** a pill tab for while connected / not connected, then another for *Any network* / *Named network*. Named reveals a *Network* dropdown of networks in range; no network blocks Save.
  - **Headphones:** a pill tab for while plugged in / not plugged in.
  - **Battery:** pill tabs for *Charging / Not charging* and *Below / Above*, and a 5% slider (Above stops at 95%).
- **Permissions (`fa7`, `fa8`).** Asked when the choice that needs them is made (Bluetooth type; Named network). A refusal keeps the previous trigger and shows a note with **Open settings**. A saved rule whose permission was later revoked is dimmed with an error icon and "Needs … access"; tapping it asks for the permission.
- **Pro states (`fa4`, `fa6`).** For a free user: device triggers carry a **Pro pill** in the *When* dropdown, and choosing one opens the **Facet Pro screen** (see below) instead of asking for a permission; at the 2-rule limit the **Add rule** row carries a Pro pill and opens the same screen; a dashed strip under the Rules card reads "Free includes 2 schedule rules. Pro adds more rules and triggers…" with **Upgrade to Pro**; rules that no longer qualify are dimmed with a lock and "Paused. Needs Pro" (tapping opens the Facet Pro screen). Free keeps the first two schedule rules, in list order.
- **Shape (M3 scale).** Sheet top corners 28dp (extra large), dialogs 28dp, settings cards 28dp (extra large), other cards and the status line 12dp (medium), dashed strips 12dp (medium), chips, Pro pill and menus 8dp (small), switches, radios and slider are stock M3. Buttons are M3's default fully rounded pills (`PrimaryButton`, `TonalButton`, `SurfaceButton`, text actions).
- **Tokens.** `Surface` sheet, `Ink` / `Muted` text, `Accent` for selected chips, radios, slider and the Add rule row, `Hairline` for unselected chips and slider track, `ErrorColor` for inline errors and Delete. Dialogs 28dp (M3 extra large), buttons use `TonalButton` and `SurfaceButton`.

**Deviations from the mockups.** The trigger picker (`fa4`) is a dropdown row in the editor, not a second sheet, and the Pro pills sit in that dropdown. Everyone is treated as entitled until billing exists, so the Pro states only appear for a free user once the billing plan lands. The mockup's radii were aligned with the M3 scale (strips 12, sheet 28). A Bluetooth rule whose device was unpaired is not yet flagged "Device not found".

### Facet Pro (design turn 12, `12a`-`12d`)

Source: turn 12 of `Launcher.dc.html` and `Launcher Dark.dc.html` ("Pro, fancier"). The only new colour is **dark ink `#0A0F1D`**; the glows, the PRO mark and the primary button follow the accent, so they follow the wallpaper or swatch accent the user picked. Section 11 (including the gate sheet `11c`) is not built: every Pro limit goes straight to this screen.

- **`12a` Settings, free.** A dark card above the sections (M3 large shape, 16dp): two accent radial glows over the ink, a three-card facet fan peeking out of the top-right corner (26×44dp cards at -9°, 0°, +9°), the gradient **PRO** mark with "You're on the Free plan" beside it (so the card says where the user stands; the mockup's "PAY ONCE" is dropped), the evergreen headline "The complete Facet experience, fully unlocked." (no numbers or feature names, so it never needs editing as Pro grows), an open-lock icon and an **Upgrade to Pro** frosted button (the mockup says "See Pro"). It has the most contrast on the page so it reads first. In dark theme it gains a 1dp light inset border and a deeper shadow to separate from the page.
- **`12d` Settings, Pro.** The card shrinks to one dark row: PRO mark, "Pro is active", and a live summary ("3 facets · 2 triggers running"; just the facet count when no rule is on).
- **`12b` Upgrade.** A dark hero (two accent glows) with a back arrow (Restore purchases sits under the unlock button instead); three illustrative mini home screens fanned (Weekend, Drive, Work, each in a different clock style; the side two at 55% before purchase); two frosted trigger pills ("Office → Work", "Car → Drive"); the PRO mark and the headline "More facets, more automation, more customization" The limit that sent the user here shows as a short line under the headline (not in the mockup; kept so a gated tap explains itself). Below the hero, four feature rows (36dp icon tile on the M3 medium shape): More facets, Wi-Fi/Bluetooth/headphones/battery triggers, Custom Widgets on home screen, Future Pro features included. Pinned at the bottom on the page colour: a full-width accent-gradient **Unlock Lifetime Pro · {Play price}** button (48dp, 12dp corners, glow) with Restore purchases beneath; "Lifetime" carries the one-time, no-subscription message, so there is no separate price row. The hero is compacted (fan at 82%) so about three feature rows show above the bar, which has a soft top shadow.
- **`12c` Post-purchase.** The whole screen dark ink. The same fan fully lit with a check badge over the centre card, "You're on Pro", "Unlocked on every phone signed in to this Google account.", four rows tagged UNLOCKED (facets, Wi-Fi, Bluetooth, headphones and battery), and, at the end of the scrolling content (not pinned), a gradient **Add a facet** button (opens Manage facets) with a **Restore purchases** link under it, so a Pro user can re-check with Play; the result of a restore shows above them. The mockup's **Set up a trigger** button is dropped: Facet automation is linked from a card at the top of Manage facets instead. "Done" top right.
- **Where the app differs from the mockup copy.** "Unlimited facets" / "Free stops at two" become "More facets" / "Free includes 3. Pro goes up to 10", because those are the app's real limits; the triggers row names all four triggers; the price is whatever Google Play returns, so the mockup's `$4.99` is a placeholder (without a price the label reads "One-time purchase · no subscription"). The mini home screens are fixed examples, not the user's facets.
- **Shapes follow the app.** Every button in the app is a full pill (M3's default button shape, as `TonalButton`), matching the mockup, so the Unlock Pro, Set up a trigger and Upgrade to Pro buttons are pills (keeping the accent gradient and glow). The trigger pills use the M3 chip shape (small, 8dp).
- **System bars.** Before purchase the status bar sits over the dark hero (light icons) while the navigation bar sits over the page-coloured bottom bar (icons follow the theme); after purchase both are over dark ink.
- Debug builds add a **DEBUG** card at the bottom of Settings to force Pro or Free; release builds never contain it.

---

## Interactions & Behavior

| Gesture / event | Result |
|---|---|
| Swipe up on home (>55px) | Open drawer |
| Swipe down in drawer, `scrollTop <= 2` | Close drawer, clear query |
| Swipe left on home (past ~20% width), follow-finger | Open Switch Facets carousel (`3a`) *(revised — replaced the empty-space long-press, which was too hard to land between the clock, app list and dock; then revised again from a fling-to-navigate `NavHost` push to this follow-finger panel, mirroring the Hub; see `IMPLEMENTATION_PLAN.md`)* |
| Swipe right on the carousel (empty space, or on the first facet's card), follow-finger | Close the carousel back to Home, applying nothing — the mirror of the left swipe that opened it |
| ~~Long-press empty home space (420ms, <8px movement)~~ | ~~Long-press sheet~~ *(removed — the sheet was already gone; the long-press that replaced it is now a left swipe, above)* |
| Long-press app icon | App context menu (`4i`) |
| Drag alphabet rail | Scrub to letter, scroll its header into view, show the large letter indicator |
| Type in drawer search | Filter apps, then contacts; contact actions inline |
| Swipe carousel horizontally | Browse (clamped at both ends); does not apply anything |
| Tap a card (centred or peeking) in carousel | Apply that facet, close |
| Back / back gesture in carousel | Return to origin without applying |
| Tap **Reorder** in carousel header | Swap carousel for the reorder list (`3b`) |
| Drag a reorder-list row by its handle | Live-reorder; commits on release |
| Tap **Done** / back in reorder list | Return to the carousel |

**Timings.** Drawer `.34s cubic-bezier(.32,.72,0,1)` transform, `.24s ease` opacity. Sheet `.3s` same curve. Overlays `.22–.26s ease`. Rail indicator `.15s`. Long-press threshold 420ms.

**Home horizontal swipe [revised — this now IS assigned].** Left swipe opens the Switch Facets carousel (`3a`), follow-finger, exactly like the right swipe that opens the Hub (`F5`) — whichever one a drag's first frame engages keeps owning it for the rest of the gesture, even through a direction reversal. Releasing commits open/closed once the drag has cleared ~20% of the screen width from wherever it started (the same rule the Hub uses); short of that it springs back. Home stays composed behind either panel throughout. *(Was: "facet switching does not use swipe on the home screen" — reversed after the empty-space long-press proved too hard to target, then the carousel itself moved from a `NavHost` destination to this follow-finger panel. Within the carousel itself, swipe still only browses between facets; only a tap applies one.)*

## State Management

Prototype state, as a guide to what the implementation needs:

| Key | Type | Purpose |
|---|---|---|
| `drawer` | bool | Drawer open |
| `view` | `"list" \| "grid"` | Drawer layout |
| `q` | string | Search query |
| `facet` | int | **Applied** facet index |
| `pickIdx` | int | Facet currently centred/browsed in the carousel — purely the pager's scroll position |
| `reordering` | bool | Carousel showing the reorder list instead of the pager |
| `letter` | string \| null | Active rail letter |
| `sheet` | bool | Long-press sheet open |
| `pick` | bool | Carousel open |

*(Revised.)* `pickIdx` still exists as the pager's own browse position, but it no longer gates a separate confirm step — tapping **any** visible card (not just the centred one) applies it directly, so `facet` updates the moment a card is tapped rather than waiting on a **Select** action. The one thing that's still true from the original note: swiping alone never touches `facet`.

Data the launcher must fetch: installed app list (`LauncherApps`, live callbacks for install/uninstall/update), calendar events, usage stats for Recents/Most used, notification posts for badge dots, widget providers and their declared sizes.

## Design Tokens

### Light (`Launcher.dc.html`)

| Token | Value |
|---|---|
| Wallpaper | `#ffffff` |
| Carousel backdrop | `#dfe3ea` (`#d3d8e1` while dragging) |
| Surface | `#ffffff` |
| Page (settings screens, `SurfaceContainer`) | `#eef1f6` |
| Drawer overlay | `rgba(255,255,255,.88)` |
| Scrim | `rgba(2,8,23,.28)` |
| Ink | `#020817` |
| Muted | `rgba(2,8,23,.55)` |
| Faint | `rgba(2,8,23,.3)` |
| Hairline | `rgba(2,8,23,.07)` |
| Icon tile | `#1e293b` |
| Accent (system default) | `#2563eb` |
| Error | `#dc2626` |
| Success | `#16a34a` |

### Dark (`Launcher Dark.dc.html`)

| Token | Value |
|---|---|
| Wallpaper | `#14171d` |
| Carousel backdrop | `#101319` (`#0d1015` while dragging) |
| Surface | `#20242d` |
| Page (settings screens, `SurfaceContainer`) | `#14171d` |
| Drawer overlay | `rgba(20,23,29,.92)` |
| Scrim | `rgba(0,0,0,.58)` |
| Ink | `#e7eaf0` |
| Muted | `rgba(226,232,240,.55)` |
| Faint | `rgba(226,232,240,.3)` |
| Hairline | `rgba(226,232,240,.07)` |
| Icon tile | `#39424f` |
| Accent (system default) | `#a8c7fa` |
| Error | `#f2857f` |
| Success | `#7fd493` |

### Settings section hues

Fixed colours (independent of the user's accent) for the icon badges on settings rows: the glyph at full colour on a circle tinted at 14% (light) / 24% (dark) of it.

| Section | Light | Dark |
|---|---|---|
| Facets | `#2563eb` | `#93c5fd` |
| Appearance | `#7c3aed` | `#c4b5fd` |
| Home & apps | `#0d9488` | `#5eead4` |
| System | `#475569` | `#cbd5e1` |

### Accent handling

The accent is **one variable**, `--accent`, driving all ~100 accent usages: primary buttons, switches, selection borders on every picker, active rail letter, notification dots, links, sliders, and the carousel page indicator.

Default source is **Material You** — the launcher adopts the system's wallpaper-derived primary rather than shipping its own colour. `#2563eb` / `#a8c7fa` are stand-ins for that. Turning off *Accent from system* lets the user pick a fixed colour, which then flows through the same variable.

In Android terms: read `android.R.color.system_accent1_*` (API 31+) and expose the override as a stored preference.

### Type

Inter throughout, weights 200 / 300 / 400 / 500 / 600.

| Role | Style |
|---|---|
| Clock | `200 72px/1`, `-4px` |
| Screen title | `500 26px`, `-.6px` |
| Section title | `500 17px` |
| App name (home) | `400 18px/1`, `-.2px` |
| App name (drawer) | `400 16px` |
| Row title | `400 14px` |
| Body | `400 12.5px/1.65` |
| Row subtitle | `400 11.5px` |
| Section header | `600 10px`, `.14em` |
| Overline | `500 10px`, `.1em` |
| Rail letter | `500 9px` |

### Spacing, radius, shadow

Screen gutter 24px. Section gap 26px. Row padding 12–14px vertical. Grid gap 8px.

Radius (Material 3 scale): 4px text field · 8px chip and menu · 12px small card · 28px settings card, dialog and sheet · fully rounded (pill) buttons, switches and the search bar · 36px device frame. App icons follow the user's icon shape setting instead.

Shadow: `0 1px 2px rgba(2,8,23,.08)` raised pill · `0 10px 30px rgba(2,8,23,.14)` carousel page · `0 16px 40px rgba(2,8,23,.22)` dialog · `0 22px 44px rgba(2,8,23,.22)` dragged page (superseded — the reorder list dims a picked-up row to 20% opacity instead, no rotate/shadow lift; see `3b`) · `0 -8px 24px rgba(2,8,23,.1)` bottom sheet.

## Assets

No image assets. Icons are either CSS-drawn (chevrons, checkmarks, rings, toggles) or three Lucide SVGs loaded from unpkg in the facet carousel: `settings`, `trash-2`, `pencil`. **Implemented with `androidx.compose.material:material-icons-core`/`-extended` instead**, per the codebase's own icon convention: `Icons.Default.Settings`, `.Delete`, `.DragHandle`, `.MoreVert`, `.Add`.

Fonts load from Google Fonts. Substitute the codebase's own type stack; on Android, Roboto or the device default is the honest choice, in which case re-check the light weights — the 200-weight clock is central to the design and Roboto's Thin is a close match.

## Files

| File | Contents |
|---|---|
| `Launcher.dc.html` | Light theme — all screens, turns 1–4 |
| `Launcher Dark.dc.html` | Dark theme — same screens, plus the accent-from-system setting |
| `screenshots/` | Rendered captures of every screen, grouped by turn, both themes |
| `PRD.md` | Original product requirements |
| `support.js` | Preview runtime for the HTML files. Not part of the implementation. |

### Screenshots

Each image is a contact sheet of one turn, with the id badges visible so captures map back to the ids used throughout this document.

| File | Screens |
|---|---|
| `light-turn1-home-and-drawer.png` | `1a`–`1j` — home prototype, clock variants, favorites densities, drawer list/grid, search |
| `light-turn2-sheet-and-settings.png` | `2a`–`2d` — long-press sheet, superseded settings, clock page |
| `light-turn3-facets-and-settings.png` | `3a`–`3f` — carousel, reorder, launcher settings, facet settings, clock pages |
| `light-turn4-hub-onboarding-pickers.png` | `4a`–`4q` — hub, first run, context menu, pickers, denied states, no results |
| `dark-*.png` | The same four sheets in the dark theme |

## Open questions

Not resolved in these designs:

- **Alphabet rail at large system font scales.** 26 letters at `500 9px` fit a 780px-tall phone; at 200% font scale they will not. Needs either a condensed rail (every other letter) or a different jump affordance.
- **Guided widget re-add on restore.** Backup export is specified; the PRD notes widgets can't be restored automatically, but the guided re-add flow is undesigned.
- **Drawer at very large font scales** generally — the 5-column grid with 9.5px labels is the tightest layout in the set.
