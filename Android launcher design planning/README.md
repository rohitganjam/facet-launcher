# Handoff: Minimal Android Launcher

## Overview

A minimal Android home-screen replacement. Instead of an icon grid, the home surface shows a large clock, the next calendar events, a short list of app names, and a fixed dock. All apps live in one alphabetical drawer reached by swiping up. Users can keep up to three **profiles** — separate home configurations (clock style, list content, favorites) that share one dock.

This bundle covers two surfaces in full (Home, App Drawer) plus the Launcher Hub, first-run, settings, profile management, pickers, and permission-denied states. Source requirements are in `PRD.md`.

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
| `3a` | Profile carousel | Current page 70%, neighbours 60% |
| `3b` | Profile reorder (drag state) | |
| `3c` | **Launcher settings** | Canonical settings screen |
| `3d` | **Per-profile settings** | |
| `3e` | Clock style page (default) | |
| `3f` | Clock style page (profile override) | |
| `4a`–`4e` | Launcher Hub: populated, empty, add picker, at capacity, orphaned widget | |
| `4f`–`4h` | First run, v1 | **Superseded by `5a`–`5g`** — kept for reference only |
| `5a` | New first run, clickable | Interactive: Next / Skip / Back, live favorites and dock |
| `5b` | Step 1 — intro with Home diagram | |
| `5c` | Step 2 — home setup: seeded dock + favorites | |
| `5d` | Step 2 alternate — favorites only | Open question |
| `5e` | Step 2 at the cap (8 of 8) | |
| `5f` | Step 3 — profiles teaser | |
| `5g` | Step 4 — set as default over the real Home | |
| `5h`–`5i` | Coach mark: gesture hints — arrows vs chevrons | Open question |
| `5j` | Coach mark: profiles callout | |
| `5k` | Coach mark: Hub callout | Optional |
| `4i` | App long-press menu | |
| `4j`–`4l` | Pickers: favorites, dock, calendars | |
| `4m` | Rename dialog | App labels and profile names |
| `4n`–`4o` | Add profile: placeholder page, then created | |
| `4p` | Permission-denied and empty states | |
| `4q` | Search with no results | |

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
4. Section label — `500 10px`, `letter-spacing: .1em`, `rgba(2,8,23,.34)`, followed by a 1px hairline and the profile dots. Text is the list mode: `FAVORITES` / `RECENTS` / `MOST USED`.
5. App list — rows of `padding: 8px 0`, `gap: 14px`. Icon `34×34`, `border-radius: 10px`. Name `400 18px/1`, `letter-spacing: -.2px`. Notification dot `6px` circle in the accent, trailing the name.
6. Dock — `justify-content: space-between`, `padding: 0 4px`. Icons `50×50`, `border-radius: 15px`. Badge is a `9px` accent dot at `top:-1px; right:-1px` with a `2px` wallpaper-coloured ring.
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

### Long-press sheet (`2a`)

Long-press empty space on the home surface for **420ms**; movement over 8px cancels. The home layer dims under `rgba(2,8,23,.28)`; the sheet rises from the bottom, `border-radius: 20px 20px 0 0`, `transform .3s cubic-bezier(.32,.72,0,1)`, `box-shadow: 0 -8px 24px rgba(2,8,23,.1)`. A `34×4` grab handle sits centred above the rows.

Three rows, each `13px 24px` with `1px` separators, title `400 16px` and subtitle `400 11.5px`:

| Row | Subtitle | Destination |
|---|---|---|
| Switch profile | `Currently Focus — 2 of 3` | `3a` |
| Edit profile | `Clock style, list content, favorites` | `3d` |
| Change wallpaper | `Opens the system picker` | System picker |

### Profile carousel (`3a`, `3b`, `4n`, `4o`)

**Purpose.** Switch the active profile, reorder profiles, reach per-profile settings.

**Header.** Back chevron + `500 15px` "Profiles" left, `500 12px` accent **Select** right. Sub-line `400 11.5px`: `Long press to reorder · 2 of 3`.

**Name.** Centred above the carousel, `500 17px` with a pencil glyph at 42% opacity — tap to rename (`4m`).

**Carousel.** Current page `252×444` at `border-radius: 20px`, `box-shadow: 0 10px 30px rgba(2,8,23,.14)` — roughly 70% of the phone frame. Neighbours `204×368` (≈60%), offset `top: 38px`, `opacity: .72`, peeking in from `left: -92px` and `left: 348px`. Each page miniaturises its own profile: clock at `200 44px`, list-mode label at `500 7.5px`, 4 app rows, dock row.

**Add page.** Sits after the last profile, up to 3 total. Dashed `rgba(2,8,23,.26)` border, centred `+` at `300 34px`, then `Add profile` and `Copies this profile's settings. Favorites start empty.` (`4n`).

**Actions.** Gear and trash glyphs centred *below* the page, above the indicator — `gap: 26px`, 19px, 55%/50% opacity. No full-width buttons.

**Indicator.** Page dots below the actions; active dot is a `16×5` rounded bar, inactive are `5px` circles.

**Behavior.**
- Horizontal drag over 55px moves one page. **No infinite scroll** — clamps at both ends.
- Tapping a neighbour also moves to it.
- **Select** applies the centred profile and closes. Back (nav bar or gesture) returns to wherever the carousel was opened from — home or settings — without applying.
- Long press lifts the page (`3b`): `rotate(-1.4deg)`, `box-shadow: 0 22px 44px rgba(2,8,23,.22)`, `1.5px` accent border. The target slot shows a dashed accent outline reading `DROP HERE — POSITION 1`. Backdrop darkens to `#d3d8e1`. Status line under the indicator reads `Release to place · position 1 of 2`. Ends of the run show no target.
- **Add profile** (`4n` → `4o`): silently duplicates the previous profile as `Profile N`, **copying its settings but not its favorites**. The new page becomes the centred one; its favorites area shows the empty strip. Gear and trash are dimmed to 22% on the placeholder page and become active once created.

### Launcher settings (`3c`)

Canonical settings screen. Scrolls well past one viewport. Title `500 26px`, `letter-spacing: -.6px`. Section headers `600 10px`, `letter-spacing: .14em`, `rgba(2,8,23,.32)`. Rows `12–14px 24px` with `1px rgba(2,8,23,.07)` separators; title `400 14px`, subtitle `400 11.5px`.

**Every option that changes appearance shows the actual result, not a label** — clock styles render as miniature clocks, list/grid as miniature layouts, grid sizes as dot matrices, icon modes as coloured vs monochrome tiles.

| Section | Contents |
|---|---|
| **PROFILES** | View profiles → `3a` (`Opens the profile carousel — long press to reorder`) |
| **CLOCK** | Default clock style → `3e`, with a 52×40 preview and chevron. Note: every profile inherits this unless it overrides. |
| **SHARED ACROSS PROFILES** | 24-hour time (on), Calendar events (on, `2 calendars · all-day hidden`), Notification dots (on, `Follows the shade — silent channels stay quiet`) |
| **DOCK** | `4 apps · shared across profiles`; icon row + `+` slot → `4k`; `Drag to reorder · long-press to remove · 3 to 5 apps`; **Show apps as** Icons (default) / Text, shown as two 62px previews |
| **APP DRAWER** | Presentation: List (default) / Grid as 96px layout previews. Grid size: 4×4 / 4×5 / 5×5 / 5×6 (default) as dot matrices. **Show icons** (on — applies to list). **Show labels** (on — applies to grid). Search contacts (on). |
| **APPEARANCE** | Icons: System default / Monochrome (default) as 78px tile previews. **Accent from system** switch (on) — Material You, follows the wallpaper — with the wallpaper-derived swatch row beneath and `Turn this off to pick a fixed colour instead. Monochrome icons use the same accent.` Drawer opacity slider at 88%. |
| **(bottom)** | Backup & restore (`Export settings as a file · widgets need re-adding on import`), Set as default launcher (`Active`, green) |

### Per-profile settings (`3d`)

Reached from the sheet's **Edit profile** or the carousel's gear. Header shows the profile name.

| Row | Control |
|---|---|
| App list content | Dropdown — Favorites / Recents / Most used |
| Apps to show | Dropdown — 4…8. Only present for Recents / Most used. Subtitle notes usage access. |
| Favorite apps | `5 of 8` + chevron → `4j`, with the chosen icons shown as a 30px row plus a dashed `+` |
| Clock style | `Inherits default · Light stack` or `Overridden · Rule`, with a 52×40 preview and chevron → `3f` |
| Rename profile | → `4m` |

Footnote: dock, icons, accent, drawer layout and notification dots are launcher-wide. A new profile copies the previous profile's settings; favorites start empty.

### Clock style page (`3e` default, `3f` profile override)

Options live **above** the style previews, per the PRD's ordering: Show calendar events → Select calendars → 24-hour time, then the styles.

Each style is a **full-width live preview** (`border-radius: 14px`, wallpaper background, `padding: 18px 18px 20px`) with its name below and `Selected` in the accent on the active one. Selected preview gets a `1.5px` accent border; others `1px rgba(2,8,23,.12)`.

`3f` adds the inherit/override choice at the top as two radio rows:

- **Inherit default** — `Light stack · calendar on`, with a mini preview
- **Override for this profile** — `Changes here don't affect other profiles`

Under **Override**, the three option rows are tinted `rgba(37,99,235,.04)` to mark what the override owns, and the style list applies to this profile only. Under **Inherit**, those rows grey out and display the default's values. Values are retained when switching back to Inherit — stored but not applied.

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
3. **`4h` Set as default** — bottom sheet over the dimmed home surface. `500 18px` title, explanation that Android will confirm and the choice is reversible, a bordered row showing this launcher as the home app, then **Set as default** / **Later**. Footnote: `Permissions come later, one at a time, only when a feature needs them.`

Pagination dots bottom-left, primary action bottom-right, `padding-bottom: 34px`.

### App long-press menu (`4i`)

Anchored card, `border-radius: 14px`, `box-shadow: 0 16px 40px rgba(2,8,23,.22)`, `left/right: 20px`. Header row: 34px icon, app name at `400 15px`, `2 notifications` beneath. Then four rows at `14px 18px`, `400 14.5px`:

- App info
- **Uninstall** — in the error colour
- Widgets — `2 available`, chevron
- Rename label — chevron → `4m`

Same menu from the dock, favorites list, and drawer. Renaming changes the launcher's label only.

### Pickers (`4j`–`4l`) and rename (`4m`)

- **`4j` Favorites** — full-screen, searchable, checkboxes, `5 of 8 chosen`. At 8, unchecked rows grey out until one is removed. Reordering happens on the profile page, not here.
- **`4k` Dock** — same shape; current dock shown as a 44px icon row with a dashed `+`. `3 to 5 apps`, shared across every profile. Removing collapses the row — no empty slot.
- **`4l` Calendars** — account rows with email beneath, checkboxes, plus a **Hide all-day events** switch. Footnote: tapping an event opens the calendar app *at that event*.
- **`4m` Rename** — centred dialog, `border-radius: 14px`. Title, explanation, then the value on a `1.5px` accent underline with a **Reset** affordance, then Cancel / Save. Used for both app labels and profile names.

### Permission-denied and empty states (`4p`)

**One pattern everywhere:** an inline strip replaces the content it would have shown, carrying the control that fixes it. Never a full-surface takeover, never a silently empty section.

Strip: `padding: 11px 13px`, `1px dashed rgba(2,8,23,.16)`, `border-radius: 10px`, message at `400 11.5px/1.4` muted, action at `500 11.5px` in the accent.

| Context | Message | Action |
|---|---|---|
| Clock, calendar denied | Calendar access off — events hidden. | Turn on |
| Home list, usage access missing | Most used needs usage access from system settings. | Open settings |
| Fresh profile, no favorites | Nothing here yet — pick up to 8 apps. | Add apps |
| Search, contacts denied | Contact results need contacts access. | Turn on |

---

## Interactions & Behavior

| Gesture / event | Result |
|---|---|
| Swipe up on home (>55px) | Open drawer |
| Swipe down in drawer, `scrollTop <= 2` | Close drawer, clear query |
| Long-press empty home space (420ms, <8px movement) | Long-press sheet |
| Long-press app icon | App context menu (`4i`) |
| Long-press profile page in carousel | Reorder mode (`3b`) |
| Drag carousel horizontally (>55px) | Move one page, clamped |
| Drag alphabet rail | Scrub to letter, scroll its header into view, show the large letter indicator |
| Type in drawer search | Filter apps, then contacts; contact actions inline |
| Tap **Select** in carousel | Apply centred profile, close |
| Back / back gesture in carousel | Return to origin without applying |

**Timings.** Drawer `.34s cubic-bezier(.32,.72,0,1)` transform, `.24s ease` opacity. Sheet `.3s` same curve. Overlays `.22–.26s ease`. Rail indicator `.15s`. Long-press threshold 420ms.

**Profile switching does not use swipe.** Horizontal swipe on the home surface is deliberately unassigned — profiles change only through the sheet → carousel → Select path.

## State Management

Prototype state, as a guide to what the implementation needs:

| Key | Type | Purpose |
|---|---|---|
| `drawer` | bool | Drawer open |
| `view` | `"list" \| "grid"` | Drawer layout |
| `q` | string | Search query |
| `profile` | int | **Applied** profile index |
| `pickIdx` | int | Profile centred in the carousel, not yet applied |
| `letter` | string \| null | Active rail letter |
| `sheet` | bool | Long-press sheet open |
| `pick` | bool | Carousel open |

The split between `profile` and `pickIdx` is the important part: browsing the carousel must not change the home screen until **Select**.

Data the launcher must fetch: installed app list (`LauncherApps`, live callbacks for install/uninstall/update), calendar events, usage stats for Recents/Most used, notification posts for badge dots, widget providers and their declared sizes.

## Design Tokens

### Light (`Launcher.dc.html`)

| Token | Value |
|---|---|
| Wallpaper | `#e9ecf2` |
| Carousel backdrop | `#dfe3ea` (`#d3d8e1` while dragging) |
| Surface | `#ffffff` |
| Drawer overlay | `rgba(255,255,255,.88)` |
| Scrim | `rgba(2,8,23,.28)` |
| Ink | `#020817` |
| Muted | `rgba(2,8,23,.45)` |
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
| Surface | `#171a21` |
| Drawer overlay | `rgba(20,23,29,.92)` |
| Scrim | `rgba(0,0,0,.58)` |
| Ink | `#e7eaf0` |
| Muted | `rgba(226,232,240,.5)` |
| Hairline | `rgba(226,232,240,.07)` |
| Icon tile | `#39424f` |
| Accent (system default) | `#a8c7fa` |
| Error | `#f2857f` |
| Success | `#7fd493` |

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

Radius: 5px checkbox · 8px chip · 9px button · 10–14px card · 15px dock icon · 20px sheet · 36px device frame.

Shadow: `0 1px 2px rgba(2,8,23,.08)` raised pill · `0 10px 30px rgba(2,8,23,.14)` carousel page · `0 16px 40px rgba(2,8,23,.22)` dialog · `0 22px 44px rgba(2,8,23,.22)` dragged page · `0 -8px 24px rgba(2,8,23,.1)` bottom sheet.

## Assets

No image assets. Icons are either CSS-drawn (chevrons, checkmarks, rings, toggles) or three Lucide SVGs loaded from unpkg in the profile carousel: `settings`, `trash-2`, `pencil`. Replace these with the codebase's icon set — Material Symbols is the natural fit.

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
| `light-turn3-profiles-and-settings.png` | `3a`–`3f` — carousel, reorder, launcher settings, profile settings, clock pages |
| `light-turn4-hub-onboarding-pickers.png` | `4a`–`4q` — hub, first run, context menu, pickers, denied states, no results |
| `dark-*.png` | The same four sheets in the dark theme |

## Open questions

Not resolved in these designs:

- **Alphabet rail at large system font scales.** 26 letters at `500 9px` fit a 780px-tall phone; at 200% font scale they will not. Needs either a condensed rail (every other letter) or a different jump affordance.
- **Guided widget re-add on restore.** Backup export is specified; the PRD notes widgets can't be restored automatically, but the guided re-add flow is undesigned.
- **Drawer at very large font scales** generally — the 5-column grid with 9.5px labels is the tightest layout in the set.
