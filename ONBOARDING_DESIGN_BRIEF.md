# Facet Launcher — Onboarding & Coach Marks: Design Brief

For mocking the first-run flow and post-onboarding coach marks. Self-contained — you don't need
repo access. Pairs with the existing design handoff
(`Android launcher design planning/design_handoff_minimal_launcher/`, screens `4f`–`4h`), which
this extends. Flow logic and rationale: [`ONBOARDING_FLOW.md`](ONBOARDING_FLOW.md).

All copy below is **draft** — revise freely.

---

## 1. Product context

Facet is a minimal Android launcher. The home screen is **not an icon grid** — it's a large
clock, the next calendar events, a short list of app *names*, and a fixed **dock** at the bottom.
All apps live in one alphabetical drawer reached by swiping **up**. Widgets live on a separate
"Hub" screen reached by swiping **right**. Users can keep up to **3 profiles** — separate home
layouts (clock style, list content, favorites) that all **share one dock**.

**Design language:** quiet, typographic, generous whitespace, full-bleed wallpaper behind
everything. Material 3 components (sheets, dialogs, switches, checkboxes) but stripped back. No
heavy illustration, no mascot, no bright marketing color blocking. The wallpaper is always
visible behind app surfaces — surfaces are either opaque cards or translucent overlays, never a
flat colored background.

**What onboarding must accomplish, in priority order:**
1. Get the user to set Facet as their default launcher.
2. Convey the mental model (no grid; swipe up for apps; the dock).
3. Let them seed favorites (and glance at the auto-filled dock).
4. Explain **profiles** — the most differentiating feature — as a concept, not a footnote.

It must **not** request any runtime permission (calendar, contacts, usage, notifications) — those
are asked later, only when a feature needs them. A footnote on the last screen says so.

**Flow:** 4 screens — but only screen 2 takes input. Screens 1 and 3 are read-only ("Next"),
screen 2 is skippable, screen 4 is the action. Then 2 coach marks appear on the real app.

---

## 2. Design tokens

Mock in **both light and dark**. The app follows the system theme.

### Light
| Token | Value | Use |
|---|---|---|
| Wallpaper | `#e9ecf2` | placeholder wallpaper behind everything |
| Surface | `#ffffff` | cards, sheets, the favorites list |
| Scrim | `rgba(2,8,23,.28)` | dim behind the set-default sheet |
| Ink | `#020817` | primary text |
| Muted | `rgba(2,8,23,.45)` | secondary text |
| Faint | `rgba(2,8,23,.30)` | tertiary text, inactive dots, section headers |
| Hairline | `rgba(2,8,23,.07)` | card borders, dividers |
| Icon tile | `#1e293b` | app-icon placeholder (square, white monogram letter) |
| Accent | `#2563eb` | primary actions, active dot, checkboxes |
| Success | `#16a34a` | "default launcher active" confirmation |

### Dark
| Token | Value |
|---|---|
| Wallpaper | `#14171d` |
| Surface | `#171a21` |
| Scrim | `rgba(0,0,0,.58)` |
| Ink | `#e7eaf0` |
| Muted | `rgba(226,232,240,.5)` |
| Faint | `rgba(226,232,240,.3)` |
| Hairline | `rgba(226,232,240,.07)` |
| Icon tile | `#39424f` |
| Accent | `#2563eb` (mock with the default) |
| Success | `#7fd493` |

### Type
System sans-serif (Inter-like), Material 3 type scale. Sizes below are the handoff's own values
(px ≈ dp/sp). The clock uses a custom thin weight.

### Shape (Material 3 scale)
- Bottom sheets / dialogs: **28dp** top corners
- Cards / callouts: **12dp**
- Buttons, segmented controls: **fully rounded** (pill)
- App-icon placeholder tiles: 10dp (favorites list), 15dp (dock) — image masks, not M3 components

### Device frame
Portrait phone, ~390 × 844. Status bar visible. Bottom actions clear the gesture area
(`padding-bottom: 34dp`). Full-bleed wallpaper behind onboarding content — no card wraps the whole
screen; content sits directly on the wallpaper for steps 1–3.

---

## 3. Canvas plan (artboards)

| # | Artboard | Theme |
|---|---|---|
| 1 | Onboarding 1 — Intro (with Home diagram) | light + dark |
| 2 | Onboarding 2 — Home setup: dock (seeded) + favorites (3 of 8) | light + dark |
| 3 | Onboarding 2 — favorites at cap (8 of 8, unchecked rows dimmed) | light |
| 4 | Onboarding 3 — Profiles teaser (3 preview cards) | light + dark |
| 5 | Onboarding 4 — Set as default (sheet over dimmed Home) | light + dark |
| 6 | Onboarding 4 — already-default variant | light |
| 7 | Coach mark — Home gesture hint overlay | light + dark |
| 8 | Coach mark — Profiles callout (over the real carousel) | light + dark |
| 9 | Coach mark — Hub callout *(optional)* | light |

---

### Artboard 1 — Step 1: Intro (`4f`)

**Purpose:** one-breath framing + name the three parts of Home (so "dock" isn't a surprise in
step 2). No interaction beyond "Next".

**Layout:** content `padding: 96dp 32dp 0`. Wallpaper behind, no scrim.

- **Headline** — `200 (thin) 44sp/1.1`, Ink: *"A home screen that stays quiet."*
- **Body** — `400 14sp/1.65`, Muted, `margin-top: 20dp`:
  *"No icon grid. Just your clock, what's next, and a few apps you actually open."*
- **Home diagram** — `margin-top: 32dp`. A small, simplified sketch of the Home layout with three
  callout labels (`400 12sp`, Muted, thin leader lines):
  - **Clock** (top-left area)
  - **Your apps** (middle list of 3–4 name rows)
  - **Dock** (bottom row of 4 icon tiles)
  Keep it diagrammatic, not a pixel-perfect Home render — it's a legend.
- **Three concept lines** — `margin-top: 32dp`, `gap: 20dp`. Small line-art glyph (~20dp, Faint) +
  label `400 14sp` Ink. Glyphs decorative.
  1. ↑ — *"Swipe up any time for all your apps."*
  2. → — *"Swipe right for your widgets."*
  3. ⊙ / press glyph — *"Press and hold the home screen for profiles and settings."*

**Bottom bar** (`0 24dp 34dp`): dots 1/4 (active `16×5` bar Accent, inactive `5dp` Faint) · **Next**
(text, Accent, `500 15sp`). No Skip.

---

### Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended)

**Purpose:** seed favorites (deliberate pick) and confirm/tweak the dock (pre-filled). One screen,
two sections.

**Layout:** `padding-top: 56dp`, horizontal `24dp`.

- **Title** — `500 24sp`, Ink: *"Set up your home screen"*
- **Subtitle** — `400 13sp/1.5`, Muted: *"Pick the apps you want on Home. You can change all of
  this later."*

**DOCK section** — `margin-top: 24dp`:
- Header row: *"DOCK"* `600 10sp/.14em` Faint, and `{n} of 5` right-aligned Muted.
- A single row of dock icon tiles `44 × 44`, 15dp corners, Icon-tile fill + white monogram —
  pre-filled with 4 seeded defaults (label them Phone / Messages / Browser / Camera style), plus a
  trailing dashed `+` tile. `justify-content: space-between`.
- Hint `400 11.5sp` Muted: *"Shared by every profile. Tap to remove, + to add."*

**FAVORITES section** — `margin-top: 24dp`:
- Header: *"FAVORITES"* + `{n} of 8` right-aligned.
- Search field — M3 outlined, pill or 8dp corners, `400 15sp`, leading search glyph, placeholder
  *"Search apps"*. Themed (Surface fill, Hairline border, Accent focus).
- Checkbox list, scrollable to the bottom bar. Rows `padding: 12dp 0`, `gap: 12dp`: icon tile
  `30 × 30` / 10dp, name `400 15.5sp` Ink, trailing M3 **Checkbox** (Accent when checked). Plain
  alphabetical. Invented app names — no real products.

**Artboard 3 — at cap:** favorites subtitle *"8 of 8 chosen"*; every *unchecked* row → ~35%
opacity, non-interactive. (Same treatment applies to the dock `+` at 5 of 5.)

**Bottom bar:** dots 2/4 · **Skip** (text, Muted) and **Next** (text, Accent), `gap: 20dp`. Both
→ step 3.

---

### Artboard 4 — Step 3: Profiles teaser (new)

**Purpose:** teach what a profile *is*, using the real preview card as the visual. Zero
interaction — "Next" only.

**Layout:** `padding: 72dp 24dp 0`. Wallpaper behind.

- **Title** — `500 24sp`, Ink: *"More than one home screen"*
- **Visual** — `margin-top: 28dp`. Three **carousel preview cards** side by side: the centre card
  forward and full-opacity, the left and right cards smaller / peeking / slightly dimmed. Each
  card is a **scale-model of a profile's Home** — mini clock top-left, 2–3 app-name rows, a mini
  dock row at the bottom — on a `Surface` card with 12dp corners, hairline border, soft shadow.
  Give the three cards visibly different clocks / app lists so "separate layouts" reads
  instantly. (This is the real `ProfilePreviewPage` component from screen `3a`.)
- **Body** — `margin-top: 28dp`, `400 14sp/1.6`, Muted:
  *"Profiles are separate home layouts — each with its own clock, app list, and favorites, all
  sharing one dock. You start with one. Press and hold the home screen any time to switch
  profiles or add more, up to 3."*

**Bottom bar:** dots 3/4 · **Next** (Accent).

---

### Artboard 5 & 6 — Step 4: Set as default (`4h`)

**Purpose:** the one that matters. Bottom sheet over a **dimmed live Home** — the user sees their
actual Home behind it, including the favorites + dock from step 2.

**Background:** a real Home render — clock block top-left, *"FAVORITES"* label + 3–5 app-name
rows, dock of 4 icons at the bottom — covered by **Scrim**. If favorites were skipped: clock + a
dashed *"Nothing here yet — pick up to 8 apps · Add apps"* strip where the list would be, dock
still populated.

**Sheet:** rises from the bottom, `Surface`, **28dp** top corners, soft shadow
(`0 -8dp 24dp rgba(2,8,23,.10)`), `34 × 4` grab handle centered at top. Content
`padding: 20dp 24dp 34dp`:

- **Title** — `500 18sp`, Ink: *"Make Facet your home screen"*
- **Explanation** — `400 13sp/1.5`, Muted, `margin-top: 8dp`:
  *"Android will ask you to confirm. You can switch back to your old launcher any time from
  Settings."*
- **Home-app row** — `margin-top: 16dp`, bordered (1dp Hairline, 12dp corners, `12dp 14dp`):
  Facet icon `28dp` + *"Facet Launcher"* `400 15sp` Ink, then pushed right *"Home app"* `400 12sp`
  Muted.
- **Actions** — `margin-top: 20dp`: **Set as default** (primary filled, Accent, white label,
  pill, `500 15sp`) · **Later** (text button, Muted).
- **Footnote** — `margin-top: 14dp`, `400 11.5sp`, Faint:
  *"Permissions come later, one at a time, only when a feature needs them."*
- Pagination dots — 4/4 — on the sheet or on the screen behind (show one option).

**Artboard 6 — already default:** *(removed during implementation)* the sheet is skipped entirely when Facet already
holds the home role — it would only have asked the user to acknowledge something — and the gesture hints follow directly.

**Tone reference:** the app already has "special-permission explanation" screens (usage-access,
notification-access) — same register: plain, reassuring, one clear primary, one "later".

---

### Artboard 7 — Coach mark: Home gesture hint overlay

**When:** once, immediately after onboarding finishes, over the real Home. Auto-dismisses on the
first gesture, or via "Got it".

**Treatment:** full Home visible behind a **light scrim** (lighter than the sheet scrim — try
`rgba(2,8,23,.40)` / `rgba(0,0,0,.5)`). Three gesture affordances near where each gesture starts:

- **Swipe up** — centered low, above the dock: upward chevron/arrow motif with a short motion
  trail, label beneath `500 13sp` Ink: *"All your apps"*.
- **Swipe right** — left edge, vertically mid-screen: rightward arrow motif, label *"Widgets"*.
- **Long-press** — over the clock / upper area: concentric-ring / press motif, label
  *"Switch profiles"*.

Bottom-center: **Got it** — text button, Ink, `500 14sp`.

> **Revised during implementation:** four hints, not three — the long-press hint is now "Press and hold the clock to
> resize or move it" (ring marker, top-left under the clock); "Switch facets" (the swipe-left hint) sits on the right
> edge below it, "Widgets" on the left edge lower, "All your apps" bottom-right. The overlay is 90% opaque and
> **Got it** is a bordered, shadowed button (a flat fill disappeared against the overlay in both themes).

Airy, not a takeover. Arrows animate subtly on-device — mock a mid-motion frame, plus a second
frame at ~50% fade (the dismiss transition). Glyphs decorative; labels carry meaning.

---

### Artboard 8 — Coach mark: Profiles callout

**When:** first time the user opens the profile switcher (long-press Home → the carousel). Once
only. The *concept* was covered in onboarding step 3, so this is **operational** guidance only.

**Context behind it:** the **Switch Profiles carousel** as it exists today — a translucent overlay
(≈60% Surface over a dimmed Home), one profile preview card centered, a dashed **"Add profile"**
card peeking right, page dots below, a right-aligned **Reorder** button, a **Launcher settings**
row pinned at the bottom. (Handoff screen `3a`.)

**The callout:** a `Surface` card, 12dp corners, 1dp Hairline border, soft shadow, at the **top of
the overlay**, above the card. `padding: 14dp 16dp`:
- Body `400 12.5sp/1.45` Ink/Muted: *"Swipe to browse. Tap a card to switch to it. Use Reorder to
  rename, add, or remove."*
- **Got it** — text button, Accent, `500 12.5sp`, bottom-right.

Optionally sketch a translucent arrow over the "Add profile" card (*"Add another"*).

---

### Artboard 9 — Coach mark: Hub callout *(optional)*

Same callout component as Artboard 8, over the Hub's empty state. Body: *"Your widgets live here.
Add up to 20. Swipe left to go back home."* + **Got it**.

---

## 4. Component inventory (reused across artboards)

| Component | Spec |
|---|---|
| Pagination dots | active `16 × 5` rounded bar Accent, inactive `5dp` circle Faint, `gap: 6dp` |
| App-icon placeholder | square, Icon-tile fill, white monogram; 10dp (list), 15dp (dock), corners |
| Checkbox row | 30dp icon + `400 15.5sp` name + trailing M3 checkbox |
| Dock tile row | `44 × 44` tiles, 15dp corners, `justify-content: space-between`, trailing dashed `+` |
| Section header | `600 10sp / .14em`, Faint, + right-aligned `{n} of {max}` Muted |
| Text button | `500 15sp`, Accent (primary) or Muted (secondary), no container |
| Primary button | Accent fill, white label, pill, `500 15sp` |
| Callout card | Surface, 12dp corners, 1dp Hairline, shadow, `14dp 16dp` padding |
| Bottom sheet | Surface, 28dp top corners, `34 × 4` grab handle, `0 -8dp 24dp rgba(2,8,23,.1)` shadow |
| Profile preview card | Surface, 12dp corners, hairline + shadow; contains mini clock + name rows + mini dock |
| Dashed strip (empty state) | 1dp dashed `rgba(2,8,23,.16)`, 10dp corners, `11dp 13dp`; message `400 11.5sp` Muted + action `500 11.5sp` Accent |

---

## 5. Out of scope for these mocks

- Any permission-request screen (calendar / contacts / usage / notifications) — already exist,
  untouched by onboarding.
- Full Home / App Drawer / Settings / carousel internals — already in the handoff; render only as
  **context** behind sheets/overlays, or diagrammatically (step 1).
- A multi-step product tour or spotlight-with-cutout treatment — deliberately not doing this.
- Illustrations / hero art / mascot.

---

## 6. Open design questions to explore in the mocks

1. **Gesture-hint visual style** — animated arrows + motion trails, a hand/touch glyph, or minimal
   chevrons + text? Show 2 directions if quick.
2. **Step 2 weight** — favorites + dock on one screen (this brief). Does it feel too busy? A
   fallback is favorites-only with the dock taught purely by the step-1 diagram + its presence on
   Home. Sketch the favorites-only version too if it reads calmer.
3. **Profiles teaser visual** — 3 static cards (this brief), or the centre card mid-swipe to imply
   the gesture?
4. **Set-as-default dots placement** — on the sheet vs. on the screen behind it.
5. **Favorites list ordering** — plain alphabetical vs. selected apps pinned to the top.
6. **Intro concept lines** — 3 is the target. Propose swaps if one is weak.
7. **Skip on step 1** — currently none (Next only). Does the flow feel trapped?
8. **Gesture-hint density** — all three hints at once, or two (drop "widgets") for calm?
