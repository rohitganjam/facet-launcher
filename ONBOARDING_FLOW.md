# Facet Launcher — Onboarding Flow

Spec for the first-run flow + post-onboarding coach marks (Phase 8 open item `4f`–`4h`) — **built,
tested, and shipped** (see §8/§9). Built on [`CAPABILITIES.md`](CAPABILITIES.md), the
[design handoff](<Android launcher design planning/design_handoff_minimal_launcher/README.md>)
(§"First run (`4f`–`4h`)"), and [PRD](<Android launcher design planning/design_handoff_minimal_launcher/PRD.md>)
§6/§7. Visual spec for mocks: [`ONBOARDING_DESIGN_BRIEF.md`](ONBOARDING_DESIGN_BRIEF.md); the
canvas mocks themselves are in `Android launcher design planning/Launcher.dc.html` turn 5.

This document now describes the shipped implementation, not a draft — §10's "open questions" are
kept for history with their resolutions noted, not because they're still open.

---

## 1. Principles

1. **Four screens, but only one asks for input.** Intro → home setup → profiles → set-as-default.
   Screens 1 and 3 are read-only ("Next" only); screen 2 is skippable; screen 4 is the action.
   Feels like "two teaching screens + one setup screen + one action sheet."
2. **Zero runtime permissions.** Onboarding requests *nothing* — not calendar, contacts, usage, or
   notification access. Those stay just-in-time (PRD §7). Explicit test assertion.
3. **The one thing that matters is set-as-default.** A launcher the user hasn't set as default is
   inert. That screen is last and gets the most care.
4. **Profiles and the dock are represented, not buried.** Profiles is the launcher's single most
   differentiating feature — it gets its own teaching screen (screen 3), not a one-liner. The dock
   is half the Home layout — it's auto-seeded with sensible defaults *and* shown/editable in
   screen 2.
5. **Survives interruption.** The dock seed and the "done" flag are both written early enough that
   a kill mid-flow, or the set-default role grant restarting the activity, still lands on a sane
   Home.
6. **Accessible from the start** (Phase 8 bar) — TalkBack labels on every control, step announced
   ("Step 2 of 4"), all content scrollable, decorative icons marked `contentDescription = null`.
7. **Reuse what exists** — favorites/dock selection is the existing picker logic; the set-default
   screen reuses the special-access explanation-screen visual pattern; the profiles teaser reuses
   the real carousel preview card.

---

## 2. Gate, seeding & architecture

### New persisted state (DataStore, on `LauncherSettings`)

| Field | Default | Purpose |
|---|---|---|
| `onboardingComplete: Boolean` | `false` | gates the onboarding branch |
| `defaultsSeeded: Boolean` | `false` | one-shot guard for the dock seed |
| `coachMarksSeen: Set<String>` | `emptySet()` | dismissed coach-mark ids (§5) |

Each gets a key + a `SettingsRepository` setter (`markCoachMarkSeen(id)` is additive). All three
are **excluded from the backup bundle** (install-local, same class as `*PermissionRequested`).

### Dock auto-seed — runs before onboarding UI

`domain/SeedDefaultDockUseCase.kt` (new) — spans `DockAppRepository` + `DefaultAppRepository` +
`AppRepository`, so it's a use case:

```
if (settings.defaultsSeeded) return
val defaults = defaultAppRepository.getDefaultAppPackages()   // browser, messaging, camera, mail, phone — installed only, in that order
val installed = appRepository.getInstalledApps()
defaults.take(DockAppRepository.MAX_APPS)
        .mapNotNull { pkg -> installed.firstOrNull { it.packageName == pkg } }
        .forEachIndexed { i, app -> dockAppRepository.addDockApp(app, position = i) }
settingsRepository.setDefaultsSeeded(true)
```

Invoked once from `LauncherViewModel.init` (alongside `ensureActiveProfile` /
`cleanUpUninstalledApps`). Because it's not tied to the onboarding UI, a fresh install has a
populated dock even if the user force-quits during onboarding. Favorites are **not** auto-seeded
— that's a deliberate personal pick in screen 2; skipping leaves them empty with the recoverable
"Nothing here yet · Add apps" Home strip.

### Branch point — not a NavHost route

Onboarding is a top-level branch in `LauncherActivity`, *outside* `FacetNavHost` (same isolation
`HomeDrawerRoute` gets; keeps it off the launcher back stack):

```
when {
  uiState.isLoading            -> Box(fillMaxSize())                // existing blank/wallpaper frame
  !uiState.onboardingComplete  -> OnboardingScreen(onFinish = viewModel::completeOnboarding)
  else                         -> FacetNavHost(apps = uiState.apps, ...)
}
```

- `LauncherUiState` gains `onboardingComplete` (folded into `LauncherViewModel.init`'s existing
  `combine`). `isLoading` already prevents any flash before the real value loads.
- `LauncherViewModel.completeOnboarding()` → `settingsRepository.setOnboardingComplete(true)` →
  `settings` re-emits → activity swaps to `FacetNavHost` at Home.

### Internal step navigation

`OnboardingScreen` owns step state (`var step by rememberSaveable { … OnboardingStep.INTRO }`).
Steps via `AnimatedContent` with slide + fade, standard easing (`CubicBezierEasing(.32,.72,0,1)`,
340 ms — matches `FacetNavHost`).

`OnboardingViewModel` (`@HiltViewModel`) owns only data-backed state:
- installed apps (`GetInstalledAppsUseCase`, one-shot),
- live favorites selection (`DefaultFavoriteAppRepository.observeDefaultFavorites()`),
- live dock selection (`DockAppRepository.observeDockApps()`),
- search query,
- `toggleFavorite(AppInfo)` (cap `DefaultFavoriteAppRepository.MAX_FAVORITES = 8`),
- `toggleDockApp(AppInfo)` (cap `DockAppRepository.MAX_APPS = 5`),
- `onQueryChanged(String)`.

Mirrors `FavoritesPickerViewModel`'s `profileId == null` branch (plus the dock equivalent). The
picker's frozen-load-order behavior is not wanted here — a fresh linear flow, order can be live.

---

## 3. Screen-by-screen

Shared chrome: pagination dots bottom-left (active `16×5` rounded bar Accent, inactive `5dp`
circle Faint), primary action bottom-right, `padding-bottom: 34dp`. Wallpaper behind every step;
steps 1–3 on bare wallpaper, step 4 dims Home behind a sheet.

**Top-right "Skip"** — a text button (`Muted`, matches the "Later"/"Back" style) shown on steps
1–3 only (`OnboardingScreen`'s own overlay, not per-page — absent whenever a full-screen picker is
open). Jumps straight to step 4, same as Next from Profiles — this is a later addition superseding
§1's original "Screens 1 and 3 are read-only... No Skip" framing for the intro screen specifically;
Home Setup's own per-page Next already let a user leave everything at its seeded default, so this
just adds a faster, uniform way out from any of the three steps rather than changing what "Next"
without picking anything already did. If `OnboardingUiState.isDefaultLauncher` is already `true`
(reinstall), Skip finishes onboarding immediately instead of landing on step 4's "already default"
variant, since that step has nothing left to offer. `testTag("onboarding_skip")`.

### Step 1 — Intro (`4f`)

| | |
|---|---|
| **Headline** | Light-weight `headlineLarge` Ink — *"A home screen that focuses on you."* — revised from the original "...that stays quiet" per direct feedback: the restraint claim only ever described Home's own surface, not the app's total capability, and undersold the latter. |
| **Body** | `400 14sp/1.65` Muted — *"No icon grid. Just your clock, what's next, and a few apps you actually open."* |
| **Home diagram** | a small labelled sketch of the Home layout — **clock**, **app list**, **dock** — so "dock" is named before screen 2 shows it. |
| **Three concept lines** | (glyphs decorative) — ↑ *"Swipe up any time for all your apps."* / → *"Swipe right for your widgets."* / ← *"Swipe left to switch profiles."* — corrected to match the real gesture map (`PRD.md` §5): the empty-space long-press sheet this draft originally described was removed and replaced by a left swipe (see [`PRD.md`](<Android launcher design planning/design_handoff_minimal_launcher/PRD.md>) §5's "Superseded" notes). |
| **Layout** | `padding: 96dp 32dp 0` |
| **Actions** | dots 1/4 · **Next** → step 2. No Skip. |
| **System back** | no-op. |

### Step 2 — Your home screen (`4g`, extended)

One screen, two sections, both mirroring their Settings counterparts (`DockSettingsScreen`,
`HomeAppsListSettingsScreen`) rather than an onboarding-only search list: a live row that supports
drag-to-reorder in place, plus a clickable row that opens the *same* full-screen picker Settings
uses (`DockAppPickerScreen`, `FavoritesPickerScreen`) for actually adding/removing apps.

| | |
|---|---|
| **Title** | `500 24sp` Ink — *"Set up your home screen"* |
| **Subtitle** | `400 13sp/1.5` Muted — *"Pick the apps you want on Home. You can change all of this later."* |
| **DOCK section** | header `600 10sp/.14em` Faint — *"DOCK"* + `{n} of 5`. A row of icon tiles (the seeded defaults) that **drag-to-reorder** in place (`DragReorderState`, horizontal). No inline `+` — a **"Manage dock apps"** link below the row opens `DockAppPickerScreen` full-screen (own search + checkbox list, uncheck to remove) as its own step within `OnboardingScreen`, then returns here. `400 11.5sp` Muted hint — *"The default dock — each profile can customize its own later. Drag to reorder."* |
| **HOME APPS section** | header *"HOME APPS"*. A **"Show"** dropdown (`LabeledDropdownRow`) picks `ListContentMode` — **Favorites** / **Recently used** / **Most used** — the same choice `HomeAppsListSettingsScreen` exposes in Settings. Only in **Favorites** mode does a **"Favorites"** row appear (`{n} of 8`, opens `FavoritesPickerScreen` full-screen the same way the dock does); once favorites are picked they render as a **drag-to-reorder list** below (vertical `DragReorderState`, same pattern as Settings' `DefaultFavoritesReorderList`). Recents/Most-used modes show no editable list here — there's nothing to pick, Home derives them live from usage stats. |
| **Writes to** | `DockAppRepository` (reorder), `DefaultFavoriteAppRepository` (reorder), `SettingsRepository.setListContentMode` — all global, no profile. Adding/removing apps writes through the reused pickers' own ViewModels directly to the same repositories. |
| **Actions** | dots 2/4 · **Skip** and **Next**, both → step 3. Skip leaves everything as it is (dock keeps its seed; favorites may be empty). |
| **System back** | closes an open picker first if one is open, otherwise → step 1. |

*Superseded:* the original inline single-search-list design (one shared searchable checkbox list
whose target toggled between "favorites" and "dock" via the dock's `+` tile) was replaced with the
above per direct feedback — no `+` button, dock and favorites each reuse Settings' own full-screen
picker instead of a second bespoke search UI, and both support drag-to-reorder in place.

### Step 3 — Profiles (mockup + animated demo, zero real interaction)

Teaches the *concept* with **illustrative mockup data**, not the user's own picks — a fresh install
only ever has one real profile at this point, so three genuinely different-looking "layouts" needs
invented content (same call the original design brief made: invented app names, monogram-tile
icons, no real products).

| | |
|---|---|
| **Title** | `500 24sp` Ink — *"More than one home screen"* |
| **Visual** | `ProfileSwitchDemo` — three fabricated `MockProfile` cards (own clock/date/favorites/dock, invented app names) in a small looping, fully automatic animation: **focus** on one profile (zoomed in) → **zoom out** to reveal its neighbors → **swipe** to the next profile (with a small "finger" touch-indicator riding along) → a brief **tap-pulse** marks the "selection" → **zoom back in**, hold, then the same sequence **plays in reverse** (zoom out → swipe back → pulse → zoom in) rather than jump-cutting back to the start, so the loop always animates continuously in both directions. |
| **Body** | `400 14sp/1.6` Muted — *"Profiles are separate home layouts, each with its own clock, app list, favorites, and dock if you want one. Swipe left from home to switch profiles."* plus *"Create multiple profiles for different occasions, like work, focus, personal etc."* — "swipe left", not "press and hold", matching the corrected gesture map. |
| **Layout** | `padding: 64dp 24dp 0` |
| **Actions** | dots 3/4 · **Next** → step 4. |
| **System back** | → step 2. |

*Superseded:* an earlier pass rendered the center card from the user's real step-2 picks with two
empty neighbor cards, honest but static — replaced per direct feedback with the fabricated,
animated mockup above, which actually demonstrates the swipe-to-switch gesture rather than just
implying it.

### Step 4 — Set as default (`4h`)

Bottom sheet over a **dimmed live Home** — now genuinely showing the favorites + dock from step 2.

| | |
|---|---|
| **Background** | real Home render (clock, FAVORITES + rows, dock) under `Scrim`. If favorites were skipped: clock + the dashed *"Nothing here yet — pick up to 8 apps · Add apps"* strip, dock still populated. |
| **Sheet** | rises from bottom, `Surface`, **28dp** top corners, `0 -8dp 24dp rgba(2,8,23,.1)` shadow, `34×4` grab handle. `padding: 20dp 24dp 34dp`. |
| **Title** | `500 18sp` Ink — *"Make Facet your home screen"* |
| **Explanation** | `400 13sp/1.5` Muted — *"Android will ask you to confirm. You can switch back to your old launcher any time from Settings."* |
| **Home-app row** | 1dp Hairline, 12dp corners, `12dp 14dp` — `[Facet icon] Facet Launcher` … `Home app` (Muted, trailing). |
| **Actions** | **Set as default** (primary, pill, Accent) · **Later** (text, Muted). |
| **Footnote** | `400 11.5sp` Faint — *"Permissions come later, one at a time, only when a feature needs them."* |
| dots | 4/4 |

**"Set as default":** `DefaultLauncherRepository.requestDefaultLauncherIntent()` (new — the
repository itself already existed, with `isDefaultLauncher()`; only the request-intent method is
new) returns, preferring:
1. `RoleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)` when
   `isRoleAvailable(ROLE_HOME) && !isRoleHeld(ROLE_HOME)` — in-place system dialog.
2. fallback `Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)` — the exact intent the Settings
   screen's own "Set as default launcher" row already used; that row was refactored to share this
   same method rather than duplicating the fallback logic.

Fired via `rememberLauncherForActivityResult(StartActivityForResult())`. **On any result**
(granted / denied / dismissed) → `onFinish()`. **"Later"** → `onFinish()` directly.

`onFinish()` = `LauncherViewModel.completeOnboarding()`.

> **Ordering:** the flag is written the moment either button is tapped, before the role dialog
> resolves — a role-grant activity restart then re-reads `onboardingCompleted = true` and lands on
> Home.

**Already-default variant:** if Facet already holds `ROLE_HOME` (reinstall), swap the row + button
for a Success-colored check + *"Facet is already your home screen"* and a single **Done**.

---

## 4. Coach marks (post-onboarding)

Two primitives, no coordinate-anchored spotlighting:

- **`GestureHintOverlay`** — translucent layer (90% `SurfaceContainer`) over Home with four labelled
  gesture hints + "Got it", stacked top to bottom at proportional (not fixed) offsets, each on the side
  its gesture starts from: clock hold (top-left, below the default clock), Switch facets (right edge),
  Widgets (left edge), All your apps (bottom-right). "Got it" is a `SurfaceButton` with a 1.5dp `Muted`
  border and a soft shadow — its fill is nearly the overlay's color, so the edge carries the contrast.
  Auto-dismiss on first gesture.
- **`FirstRunCallout`** — dismissible inline card (12dp corners, 1dp Hairline, `SettingsCard`
  styling) pinned to the top of a surface on its first open.

| id | Trigger | Surface | Teaches |
|---|---|---|---|
| `HOME_GESTURES` | onboarding just completed | Home overlay | swipe up *("All your apps")* / swipe right *("Widgets")* / swipe left *("Switch facets")* / press and hold the clock *("Press and hold the clock to resize or move it")* — the first three match `PRD.md` §5 |
| `PROFILES_INTRO` | first carousel open | inline callout | **operational only** (concept already covered by step 3) — *"Swipe to browse · tap a card to switch · Reorder up top"* |
| `HUB_INTRO` *(optional)* | first Hub open | inline callout | *"Your widgets live here · add up to 20 · swipe left for home"* — **deferred**, not built this pass (see §9) |

The dock needs no coach mark — set up in step 2, visible on Home. The drawer needs none —
swipe-up-to-open is standard; rail/search are visible affordances.

### Wiring

- **Gesture hint** in `HomeDrawerRoute`, rendered only while Home is genuinely at rest (not mid-
  drawer/hub/profile open). `HomeUiState.showGestureHint = settings.onboardingCompleted &&
  HOME_GESTURES_COACH_MARK_ID !in settings.coachMarksSeen` — a plain computed property on
  `HomeUiState`, no extra ViewModel state needed since `coachMarksSeen` already lives in
  `LauncherSettings`. Dismissed by a tap, a "Got it" tap, or the first frame of any drag (so a
  swipe attempt during the hint just clears it rather than also completing the real navigation
  underneath — a second swipe then behaves normally); dismissal calls
  `HomeViewModel.dismissGestureHint()` → `settingsRepository.markCoachMarkSeen(...)`.
- **Profiles callout** — `ProfileCarouselUiState.showIntroCallout = PROFILES_INTRO_COACH_MARK_ID
  !in globalSettings.coachMarksSeen` (same pattern); `onIntroDismissed()` persists. Ids are shared
  constants in `ui/components/CoachMarkIds.kt`.

Compose respects `ANIMATOR_DURATION_SCALE` automatically → static fallback for reduced motion.

---

## 5. Code inventory (as shipped)

**Data**
- `LauncherSettings`: `onboardingCompleted`, `defaultsSeeded`, `coachMarksSeen` + keys + setters
  (`markCoachMarkSeen` additive) — excluded from the backup bundle (see `BackupBundle.kt`'s own
  doc comment).
- `DefaultLauncherRepository.requestDefaultLauncherIntent(): Intent` — RoleManager +
  `ACTION_MANAGE_DEFAULT_APPS_SETTINGS` fallback. `isDefaultLauncher()` already existed and is
  reused as-is.

**Domain**
- `SeedDefaultDockUseCase` (dock + default-app + app repos), called unconditionally from
  `LauncherViewModel.init` — not gated on onboarding UI being shown.
- *No* `CompleteOnboarding` use case — single repository write, done from the ViewModel.

**UI — `ui/onboarding/`**
- `OnboardingScreen.kt` (stateless host, owns `step: OnboardingStep` and a separate
  `subScreen: OnboardingSubScreen?` — `DOCK_PICKER` / `FAVORITES_PICKER` — both `rememberSaveable`),
  `OnboardingViewModel.kt`, `OnboardingUiState.kt`.
- `OnboardingIntroPage.kt`, `OnboardingHomeSetupPage.kt`, `OnboardingProfilesPage.kt`,
  `SetDefaultLauncherSheet.kt` (a `ThemedModalBottomSheet` since the Home action menus were unified — content
  carries its own 24dp under the button; dismissing by scrim/swipe/Back counts as "Later"/"Done"),
  `OnboardingDots.kt`.
- The home-setup step's dock/favorites *editing* is **not** a bespoke search list —
  `OnboardingHomeSetupPage.kt` renders `DockAppPickerScreen`/`FavoritesPickerScreen` (unmodified,
  reused from `ui/dock`/`ui/profiles`) full-screen when `subScreen` is set, exactly like Settings
  does; only the live dock/favorites rows and their drag-to-reorder are local to this file.
  `OnboardingScreen` exposes `dockPickerViewModel`/`favoritesPickerViewModel` as nullable
  testing-seam params (production always `null`, falling back to the pickers' own `hiltViewModel()`)
  since `OnboardingScreenTest` builds every ViewModel by hand against a non-Hilt compose host.

**UI — coach marks**
- `ui/components/GestureHintOverlay.kt`, `ui/components/FirstRunCallout.kt`,
  `ui/components/CoachMarkIds.kt` (shared id constants).
- `HomeUiState.showGestureHint` (computed property) + `HomeViewModel.dismissGestureHint()`;
  `HomeDrawerRoute` renders the overlay only while Home is at rest.
- `ProfileCarouselUiState.showIntroCallout` (computed property) / `ProfileCarouselViewModel.onIntroDismissed()`;
  callout rendered in `ProfileCarouselScreen`, above the Reorder row.

**Wiring**
- `LauncherUiState.onboardingCompleted`; `LauncherViewModel` combine + `completeOnboarding()` +
  `seedDefaultDock()` call in `init`.
- `LauncherActivity` three-way `when` branch.

**No new nav routes** — onboarding stays a top-level `LauncherActivity` branch, outside
`FacetNavHost` entirely.

---

## 6. Reuse map (as shipped)

| Need | Reuse |
|---|---|
| Adding/removing dock apps | `DockAppPickerScreen` + `DockAppPickerViewModel`, reused wholesale (own search, own `DockAppRepository` writes) — an empty `SavedStateHandle` (no `profileId`) already means "the launcher-wide default dock", exactly onboarding's scope |
| Adding/removing favorites | `FavoritesPickerScreen` + `FavoritesPickerViewModel`, reused wholesale the same way against `DefaultFavoriteAppRepository` |
| Dock / favorites reordering | `ui/components/DragReorderState.kt` (shared headless drag mechanics) — `OnboardingHomeSetupPage` keeps its own row-rendering copy per section, same as `DockSettingsScreen`'s `DockAppsRow` and `HomeAppsListSettingsScreen`'s `DefaultFavoritesReorderList` each keep theirs (established precedent: these aren't shared render composables, only the gesture state is) |
| Home-apps content mode | `ListContentMode` (`FAVORITES`/`RECENTS`/`MOST_USED`) + `SettingsRepository.setListContentMode` — the same enum and repository call `HomeAppsListSettingsScreen` already used in Settings, via `ui/components/LabeledDropdownRow.kt` |
| Default-app resolution for the dock seed | `DefaultAppRepository.getDefaultAppPackages()` (already existed, used by the Appearance preview) |
| App icon rendering | `ui/components/AppIcon.kt` |
| Home-so-far preview (steps 2–4) | `ui/components/HomeSurfacePreview.kt` — the real shared "this is how Home looks" card, also used by Settings → Appearance/Home Apps List/Dock |
| Set-default explanation tone | `UsageAccessExplanationScreen`'s explicit-theming discipline (no stock M3 defaults) |
| Callout card styling | `ui/components/SettingsCard.kt`'s 12dp/Hairline pattern |
| Step / overlay transition easing | `HomeDrawerRoute`'s `CubicBezierEasing(.32,.72,0,1)` / 340 ms |
| Indicator dots | New `OnboardingDots.kt` — small enough not to warrant sharing with `ProfileCarouselScreen`'s own page indicator |
| Tokens | `ui/theme/` — `Surface`, `Ink`, `Muted`, `Faint`, `Hairline`, `Scrim`, `Accent`, `InkInverted` (text on the Accent-filled primary button), `SuccessColor` |

---

## 7. Edge cases

| Case | Behavior |
|---|---|
| Process killed mid-onboarding | Restarts at `INTRO` (or `rememberSaveable` step). Dock seed + any picked favorites already persisted. |
| Role grant restarts the activity | Flag already `true` → Home. |
| User dismisses the role dialog / picks "Later" | `onFinish()` runs → Home. Not-default is allowed; Settings → System shows status + fix. Open question: post-onboarding Home nudge? |
| `ROLE_HOME` unavailable | Fallback `Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)`. |
| Facet already default (reinstall) | "Already default" sheet variant. |
| No default apps resolvable (bare emulator) | Dock seed writes nothing; step 2 shows an empty dock row — "Manage dock apps" still opens the picker to add some. |
| No launchable apps at all | Lists empty; Skip/Next still work. |
| Large font scale / TalkBack | All steps scrollable; dots carry `contentDescription`; text are real nodes. |
| Home button during onboarding | `onHomePressed` pop is a no-op (onboarding is outside the NavHost) — stays on the step. |

---

## 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green

**Unit (`app/src/test`)**
- `SettingsRepositoryTest` — `onboardingCompleted` / `defaultsSeeded` round-trip + defaults;
  `coachMarksSeen` additive.
- `SeedDefaultDockUseCaseTest` — seeds the first N resolved default apps in order; no-ops when
  `defaultsSeeded`; tolerates an unresolvable package; caps at `MAX_APPS`; sets the flag.
- `OnboardingViewModelTest` — starts from the live favorites/dock; reflects the current
  `listContentMode`; `setListContentMode`/`reorderDockApps`/`reorderFavorites` delegate to the
  right repository. (No add/remove/search coverage here — that's owned by
  `DockAppPickerViewModel`'s and `FavoritesPickerViewModel`'s own existing tests, since onboarding
  now reuses those screens wholesale rather than re-implementing picking.)
- `DefaultLauncherRepositoryTest` — `requestDefaultLauncherIntent()` takes the role-request branch
  when available + unheld, the Settings-fallback branch otherwise (Robolectric `ShadowRoleManager`
  — `addAvailableRole`/`addHeldRole`); `isDefaultLauncher()` unaffected. The exact role-request
  action string is a hidden/system-API constant not on the public `RoleManager` surface, so the
  test asserts the *branch taken* (≠ the Settings fallback action) rather than pinning that string.
- `HomeUiStateTest` / `HomeViewModelTest` — `showGestureHint` true only when
  `onboardingCompleted && id !in coachMarksSeen`; `dismissGestureHint()` persists via
  `markCoachMarkSeen`.
- `ProfileCarouselUiStateTest` / `ProfileCarouselViewModelTest` — `showIntroCallout` reflects the
  set; `onIntroDismissed()` persists.

**Instrumented (`app/src/androidTest`, emulator only — `ANDROID_SERIAL=`)**
- `OnboardingScreenTest` — first-run shows intro; Next advances intro → home setup → profiles →
  set-default; Skip on home-setup jumps to profiles; tapping **Favorites** opens the full-screen
  favorites picker and **Done** returns to home setup; tapping **Manage dock apps** opens the
  full-screen dock picker; Later finishes onboarding; **completing onboarding requests no runtime
  permission** (Espresso-Intents, `times(0)` on `REQUEST_PERMISSIONS`/`ACTION_USAGE_ACCESS_SETTINGS`);
  system back from home-setup returns to intro. Builds `DockAppPickerViewModel`/
  `FavoritesPickerViewModel` by hand against the same in-memory repositories as `OnboardingViewModel`
  (same non-Hilt pattern `DockAppPickerScreenTest`/`FavoritesPickerScreenTest` already use), passed
  into `OnboardingScreen`'s testing-seam `dockPickerViewModel`/`favoritesPickerViewModel` params.
  (No separate `LauncherActivityTest` — the three-way branch is a thin `when` covered adequately by
  `OnboardingScreenTest` + the pre-existing Home/Drawer instrumented suite.)
- `HomeDrawerRouteTest` — gesture hint shows once onboarding has completed and dismisses on "Got
  it"; does not show before onboarding completes (the default for every other test in this file).
- `ProfileCarouselScreenTest` — intro callout shows on first open and dismisses on "Got it"; stays
  hidden once already dismissed. Every *pre-existing* test in this file now defaults to
  "already seen" (`introCalloutAlreadySeen = true`) so the new callout doesn't change their
  established layout/visibility assumptions.

**Previews** — light + dark `@Preview` for each of the four steps + both coach marks.

**A real cross-session collision, not a code bug:** partway through this build, a second session
was concurrently editing unrelated files (`SettingsViewModel`'s calendar-count feature, Room
migrations) in the same working tree, twice leaving the module transiently non-compiling. Both
times resolved once that session's edits settled — flagged here only because it's why the build
history looks like it stalled, not because of anything in this feature's own code.

---

## 9. Task breakdown — all complete

1. ✅ **Data** — `onboardingCompleted` / `defaultsSeeded` / `coachMarksSeen` fields/keys/setters +
   `SettingsRepositoryTest`; added to `LauncherUiState` + `LauncherViewModel`; documented as
   backup-excluded in `BackupBundle.kt`.
2. ✅ **`SeedDefaultDockUseCase`** + test; called unconditionally from `LauncherViewModel.init`.
3. ✅ **`DefaultLauncherRepository.requestDefaultLauncherIntent()`** + test; the Settings screen's
   own row refactored to share it instead of duplicating the fallback intent.
4. ✅ **`OnboardingViewModel` / `OnboardingUiState`** + `OnboardingViewModelTest`.
5. ✅ **Step composables** (`Intro`, `HomeSetup`, `Profiles`, `SetDefaultLauncherSheet`, dots) +
   previews. No shared `AppCheckboxRow` extraction (see §5).
6. ✅ **`OnboardingScreen`** host + `AnimatedContent` + system-back handling.
7. ✅ **`LauncherActivity`** three-way branch + `completeOnboarding()`.
8. ✅ **`GestureHintOverlay`** + `HomeUiState`/`HomeViewModel`/`HomeDrawerRoute` wiring + tests.
9. ✅ **`FirstRunCallout`** + `ProfileCarouselViewModel` wiring + tests. Hub callout **deferred** —
   optional in both design docs, Hub already has its own empty state doing similar work; add later
   with the identical `FirstRunCallout` component if wanted.
10. ✅ **Instrumented tests** + full-suite green (`./gradlew test`, `ANDROID_SERIAL=<emulator>
    ./gradlew connectedDebugAndroidTest`).
11. **Tick** the Phase 8 first-run box in `IMPLEMENTATION_PLAN.md` with the write-up (next).

---

## 10. Open questions — resolved during the build

1. **Set-default mechanism** — `RoleManager.ROLE_HOME` request + `ACTION_MANAGE_DEFAULT_APPS_SETTINGS`
   fallback (not `ACTION_HOME_SETTINGS` — that's what the Settings screen's own pre-existing row
   already used, so the fallback matches it exactly rather than introducing a second one).
2. **Screen 2 weight** — shipped combined (dock + favorites on one screen), matching what the
   design canvas (`Launcher.dc.html` turn 5, artboards `5c`–`5e`) had already mocked before this
   build started.
3. **Already-default case** — shipped as a dedicated sheet variant (Success check + "Facet is
   already your home screen" + single Done), not left to the Settings fallback.
4. **Intro concept lines** — shipped as swipe up / swipe right / swipe left, matching the corrected
   gesture map exactly (no long-press line — that gesture doesn't exist anymore).
5. **Post-onboarding nudge** — nothing added for v1, per the original proposal; still open if
   wanted later.
6. **Favorites step ordering** — shipped as selected-then-other (mirrors `FavoritesPickerScreen`'s
   own IN-DOCK/ALL-APPS split), not plain alphabetical — needed so a picked favorite has a visible
   way to be un-picked again (a gap in the original draft, caught during implementation).
7. **Profiles teaser** — resolved differently from both original options: one real live card (the
   user's own step-2 picks) + two honest empty-state placeholders, rather than 2–3 fabricated
   cards. See §3 step 3 and the plan's own Context section for the reasoning.
8. **Replay** — not built this pass; still open.
