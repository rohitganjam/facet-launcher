# Facet Launcher — Implementation Plan

Tracks work against [`Android launcher design planning/design_handoff_minimal_launcher/PRD.md`](<Android launcher design planning/design_handoff_minimal_launcher/PRD.md>) and [`README.md`](<Android launcher design planning/design_handoff_minimal_launcher/README.md>) — the current design handoff. (Historical entries below cite these by their old repo-root path; the content moved, not the meaning.) Engineering conventions (incl. the testing requirement every task below relies on) are in [`CLAUDE.md`](CLAUDE.md).

Tick a box only once its deliverable is built **and** its listed tests are green.

---

## ✅ Phase 1 complete — verified on-device

`assembleDebug`, `test` (9 JVM unit tests), and `connectedAndroidTest` (9 instrumented tests) are all green on `Medium_Phone_API_36.1`. APK installed, `LauncherActivity` launched via `adb`, and screenshots visually compared against `1a`/`1h` — see "Verification findings" below.

### How the build blocker got resolved
`:app:checkDebugAarMetadata` was failing because the pinned Compose BOM `2026.08.00` pulls in `androidx.compose.*:1.12.0`, which requires `compileSdk 37` — this machine only has `android-36` installed. **Fixed by pinning the Compose BOM back to `2026.01.01`** (latest version whose artifacts are still `compose-ui 1.11.4`, compatible with `compileSdk 36`) in both the `implementation` and `androidTestImplementation` platform declarations in `app/build.gradle.kts`. Installing `android-37` remains an option later if a newer Compose feature is needed, but isn't necessary for Phase 1+.

### A second blocker surfaced once `assembleDebug` was green
`connectedAndroidTest` failed everything with `NoSuchMethodException: android.hardware.input.InputManager.getInstance` — the transitively-resolved `espresso-core:3.5.0` (pulled in via Compose's `ui-test-junit4`) doesn't work against API 36's `InputManager`. **Fixed by adding an explicit `androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")`** to force resolution to a version that supports current API levels.

### Test-locator bugs found and fixed during verification
Two `AppDrawerScreenTest` cases asserted `onNodeWithText("A")` / `onNodeWithText("Z")`, which is ambiguous — the alphabet rail renders the same single-letter text as the drawer's section headers, so two matching nodes existed. Fixed by giving section headers a `testTag("header_$letter")` (in `AppDrawerScreen.kt`) and asserting on that instead. Separately, `LauncherRootTest`'s "swipe down while scrolled" case swiped on a single short list row, which wasn't enough scroll distance to reliably leave `firstVisibleItemScrollOffset == 0` — fixed by tagging the drawer's `LazyColumn` itself (`testTag("drawer_list")`) and swiping on that, so the gesture covers the full list height.

### Verification findings
- **Home surface matches `1a`**: clock (time/date), favorites list, dock all render and are laid out correctly.
- **App Drawer matches `1h` closely**: letter section headers, right-edge alphabet rail, and — because of the drawer's opacity overlay — text stays legible regardless of the wallpaper underneath.
- **Resolved:** Home-surface text (clock, favorites, dock labels) now carries a subtle drop shadow (`ui/theme/Color.kt`'s `HomeTextShadow` — `rgba(2,8,23,.5)`, `0,1` offset, `6px` blur) so it stays legible over arbitrary wallpapers without a scrim, matching how Pixel-style launchers solve this. Text color is also now code-configurable ahead of a real Settings option: `HomeClockTextColor`/`HomeClockTextColorMuted` govern the clock (time/date), and `HomeAppTextColor`/`HomeAppTextColorFaint` govern the favorites list + `FAVORITES` label — deliberately split into two independent knobs so clock and app-label color can be tuned separately. The App Drawer got the same treatment for consistency: `DrawerAppTextColor` (app rows), `DrawerHeaderTextColor` (letter headers), `DrawerRailTextColor` (alphabet rail) are now their own tokens in `Color.kt` rather than sharing `Ink`/`Faint` directly with Home — even though all six currently resolve to the same underlying values, and the Drawer needs no shadow (already legible via `DrawerOverlay`). Re-verified after each change: `assembleDebug`, `test` (9/9), `connectedAndroidTest` (9/9) all green. Exposing any of these as actual Launcher Settings is Phase 2 work.

### Environment notes worth not rediscovering
- **Gradle wrapper's bootstrap downloader is unreliable in this sandbox** (times out fetching the distribution zip even though `curl` to the same URL works, just slowly/throttled ~150–600 KB/s). Gradle **9.7.1** is already fully extracted and cached at `~/.gradle/wrapper/dists/gradle-9.7.1-bin/1w1c7tv4s851m17nbqdsro2tv/gradle-9.7.1/` — `./gradlew` uses it fine now; if that cache ever gets wiped, re-download via plain `curl` (not the wrapper) and re-extract into that same wrapper cache layout rather than waiting on `gradlew`'s own downloader.
- **AGP 9.2.1 has built-in Kotlin support** (https://developer.android.com/r/tools/built-in-kotlin) — do **not** apply `org.jetbrains.kotlin.android` as a separate plugin (it clashes: "Cannot add extension with name 'kotlin'"). Only `org.jetbrains.kotlin.plugin.compose` (for the Compose compiler) is applied explicitly.
- KSP (needed for Hilt) still registers sources the old way, which conflicts with AGP's built-in Kotlin unless `android.disallowKotlinSourceSets=false` is set in `gradle.properties` (already done).
- Local Android SDK (`~/Library/Android/sdk`) has only the `android-36` platform and no `cmdline-tools`/`sdkmanager` — fetching another platform needs Android Studio's own SDK Manager, not a CLI command.
- The emulator (`Medium_Phone_API_36.1`) doesn't stay booted between sessions — boot it with `~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 -no-snapshot-load &` and poll `adb wait-for-device` + `adb shell getprop sys.boot_completed` before running `connectedAndroidTest` or installing an APK.
- **This AVD's pre-installed system wallpaper picker (`com.android.wallpaper`, ThemePicker) crash-loops on its own `RecentWallpapersProvider`** (`SecurityException: Permission Denial ... requires com.google.android.apps.wallpaper.permission.RECENT_WALLPAPERS_PROVIDER`), reproduced consistently even after a full `-wipe-data` relaunch. Confirmed via `adb logcat` that this is entirely inside the system app's own code, not Facet's — Facet's `ACTION_SET_WALLPAPER` intent fires and resolves correctly (the chooser appears with the right candidates: Live Wallpaper Picker, Photos, Wallpaper & style ×2). If "Change wallpaper" ever looks broken again on this AVD, check for this crash-loop in logcat before assuming a Facet regression — it isn't fixable from the app side; try the "Photos" chooser entry, a different AVD image, or a physical device instead.

---

## 📌 Standing design convention (added mid-Phase 2 — carry forward)

**Every non-root screen gets an on-screen back button**, wired to the same nav-back action as the system back button/gesture (`ui/components/BackButton.kt`, a simple `‹` glyph — not a new icon-library dependency). This applies to `Settings` and the `Dock` picker today, and **must be added to every new screen Phases 3–7 introduce** (facet carousel, per-facet settings, clock style page, Hub, onboarding, pickers, rename dialog) — don't let it slip just because the system back gesture already technically works. **Exception:** Home and the App Drawer don't get one — they're gesture-driven (swipe open/close) by design, and a redundant back button doesn't fit that pattern.

## ✅ Phase 2 complete — verified on-device

`assembleDebug`, `test` (30 JVM unit tests), and `connectedAndroidTest` (31 instrumented tests) are all green on `Medium_Phone_API_36.1`. Full manual walkthrough on-device: long-press → sheet → Settings → 24h toggle (persisted across app restart) → Dock picker → add/remove apps → back button on both screens — all confirmed working against the real, persisted Room/DataStore state, not just test doubles.

### What got built (beyond the original 4 bullets — scope grew during the phase, see chat history for the full reasoning)
- Room (`FacetEntity`/`FavoriteAppEntity` schema-only; `DockAppEntity` actively used), DataStore-backed `SettingsRepository`, `DockAppRepository`.
- **Navigation-Compose adopted** (`ui/navigation/FacetNavHost.kt`) as the final-state navigation approach for the whole app, replacing Phase 1's hand-rolled `LauncherRoot` boolean state. Routes: `home`, `drawer`, `settings`, `dockPicker`.
- Full `3c` Settings screen — functional rows (24h time, notification dots, dock apps + Icons/Text mode, drawer opacity, show-drawer-icons, set-default-launcher status+deep-link) alongside visibly-disabled rows for not-yet-built sections (Facets, Clock styles, App Drawer Grid/labels/search, Appearance icons/accent, Backup & restore).
- Dock app picker (`4k`) with search, checkbox add/remove, 3–5 app floor/ceiling enforced on **both** the Settings drag-reorder row and the picker itself.
- Long-press sheet (`2a`) — 4 rows per the corrected screenshot (README's prose was missing "Launcher settings"; fixed in README.md too): Launcher settings + Change wallpaper functional, Switch/Edit facet disabled until Phase 3.
- **Alphabet rail reworked per direct design feedback mid-phase**: filtered letters only (not a fixed A–Z+#), rail visually compact (wraps its own content, tight spacing) and centered within a 90%-height touch zone per edge; a touch above/below the visible rail band clamps to the first/last letter rather than requiring pixel-precise placement (`letterAt(y, bandTopPx, bandBottomPx, letters)` in `AlphabetRail.kt`). F8's left-edge access reuses the exact same zone/mapping with no second visible rail; it's unconditional default behavior in list presentation, not a Settings toggle (there's nothing yet for it to be conditional on until Grid presentation exists).
- Back buttons on Settings and Dock picker (see standing convention above).

### Real bugs found and fixed during verification (not just test artifacts)
- **`NavHostController.popBackStack()` doesn't safely no-op at the start destination** — it pops that too, leaving `currentDestination == null` and the screen blank. A single swipe-to-close gesture can cross the close threshold more than once (many touch-move events per gesture), so this was reachable in normal use, not just a contrived test. Fixed with a guarded `popBackStackSafely()` extension (`FacetNavHost.kt`) used everywhere instead of the raw call.
- **Long-press-to-open-sheet didn't fire at all**, on-device, not just in tests. A hand-rolled `withTimeoutOrNull` racing `awaitPointerEvent()` doesn't reliably preempt a stationary hold (no further pointer events arrive to let cancellation propagate — confirmed via tracing: the timeout only "fired" once the *next* real event, i.e. eventual lift-off, arrived, ~1.5s later than intended). Replaced with Compose's own `detectTapGestures(onLongPress = ...)`, which is built to handle exactly this correctly. Trade-off: platform default long-press timeout/touch-slop instead of the literal 420ms/8px from README — imperceptible in practice, in exchange for it actually working.
- Two `AppDrawerScreenTest`-style locator bugs from wrong `testTag` placement (tag on a row wrapper instead of the actual `Switch`/`Checkbox` control, so `assertIsOn()`/`assertIsOff()` matched the wrong semantics node) — fixed by tagging the interactive control itself, not its container.
- A DataStore write's async round-trip (real disk I/O on its own dispatcher) isn't tracked by Compose test idling — assertions right after a toggle click could run before the write lands. Fixed with `composeRule.waitUntil { ... }` polling instead of assuming synchronous completion.
- A Compose-test-specific one (not a production bug, but a trap worth flagging): a `pointerInput` coroutine's internal timer is driven by **Compose Test's own controlled clock** (`composeRule.mainClock`), not real wall-clock time — `Thread.sleep()` between split `performTouchInput` calls doesn't advance it. Use `composeRule.mainClock.advanceTimeBy(ms)` instead when a test needs a pointerInput-internal delay/timeout to elapse.

### Deliberately deferred / not pursued
- A dedicated instrumented test for "movement cancels an in-progress long-press while a sibling drag-gesture detector is also present" proved flaky to drive reliably through the test's synthetic touch/clock APIs. The two behaviors that matter functionally (hold opens the sheet; releasing early cancels it) are covered; this specific interaction test was cut rather than chased further. Worth revisiting if a real usage report ever surfaces a double-open (drawer + sheet) in practice.

### Post-Phase-2 fix, found running as the real default launcher on-device
- **Removed the decorative "Gesture bar"** (`1a` layout item 7, README.md:78) from `HomeScreen.kt` — a static, non-interactive `104×4` pill under the dock that visually duplicated Android's own real gesture-navigation pill once Facet was actually set as the system default launcher (invisible during Phase 1/2 dev since Facet wasn't running as default yet). Confirmed non-functional (no `clickable`/`pointerInput` on it) before removing. `assembleDebug` + full `test`/`connectedDebugAndroidTest` re-verified green after removal.

---

## ✅ Phase 0 complete — Repo & tooling setup

- [x] Replace the mis-synced design docs with the correct `design_handoff_minimal_launcher/` folder (PRD, README, `.dc.html` files, screenshots).
  - **Deliverable:** `facet-launcher/design_handoff_minimal_launcher/` matches the source folder; no PackRight content remains.
- [x] Gradle scaffold: wrapper (Gradle 9.7.1 — bumped up from 9.3.1, see environment notes above), root/app `build.gradle.kts`, `settings.gradle.kts`, `local.properties`, `.gitignore`.
  - **Deliverable:** `./gradlew tasks` runs successfully.
- [x] `CLAUDE.md` — engineering conventions (architecture, Compose style, testing requirement).
- [x] `IMPLEMENTATION_PLAN.md` — this file.

---

## Phase 1 — Project scaffold + Home/Drawer skeleton

Screens covered: `1a` (home + drawer prototype), `1b` (clock, light stack).

- [x] **1.1 Manifest, Hilt & launcher registration**
  - Deliverable: `AndroidManifest.xml` with `HOME`/`DEFAULT`/`LAUNCHER` intent filter on `LauncherActivity`, `<queries>` for `MAIN`/`LAUNCHER` app visibility (API 30+); `FacetApplication` as `@HiltAndroidApp`, `LauncherActivity` as `@AndroidEntryPoint`.
  - Test: none (manifest/DI-wiring only); validated transitively by 1.3/1.3b's tests actually returning apps, and by the emulator install/launch in 1.9.

- [x] **1.2 Theme tokens**
  - Deliverable: `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` — light-scheme values transcribed from README.md's Design Tokens table (accent `#2563eb`, ink `#020817`, muted/faint/hairline, Inter type scale).
  - Test: none (pure constants); consumed and implicitly verified by 1.4–1.6's Compose previews/tests.

- [x] **1.3 `AppInfo` model + `AppRepository` + `GetInstalledAppsUseCase`**
  - Deliverable: `data/model/AppInfo.kt` (packageName, activityName, label, icon); `data/AppRepository.kt` (`@Inject`-constructed, `LauncherApps` provided via `data/di/AppModule.kt`) wrapping `LauncherApps.getActivityList`, sorted alphabetically (locale-aware `Collator`, foreshadowing F7's `AlphabeticIndex` need); `domain/GetInstalledAppsUseCase.kt` as the thin domain-layer entry point ViewModels call.
  - Test (`app/src/test/kotlin/.../data/AppRepositoryTest.kt`, Robolectric):
    - returns all launchable activities visible to `LauncherApps` for the current user
    - result is sorted alphabetically by label (case-insensitive)
  - Test (`app/src/test/kotlin/.../domain/GetInstalledAppsUseCaseTest.kt`): delegates to the repository and returns its result unchanged.

- [x] **1.3b `LauncherViewModel`**
  - Deliverable: `ui/launcher/LauncherViewModel.kt` (`@HiltViewModel`) exposing `LauncherUiState(apps, isLoading)` as a `StateFlow`, populated via `GetInstalledAppsUseCase` in `viewModelScope`. `LauncherActivity` obtains it with `by viewModels()` and collects it with `collectAsStateWithLifecycle()` — no direct repository/use-case calls in the Activity or any composable.
  - Test (`app/src/test/kotlin/.../ui/launcher/LauncherViewModelTest.kt`, `kotlinx-coroutines-test`): starts in a loading state with no apps; after the use case resolves, exposes the fetched apps and `isLoading = false`.

- [x] **1.4 `ClockBlock` composable**
  - Deliverable: `ui/home/ClockBlock.kt` — Light-stack style (`1b`): time `200 72px/1`/`-4px`, date `Wednesday, 26 August` format. No calendar events yet (stub/omitted — F1 calendar lands in Phase 4).
  - Test (`app/src/androidTest/kotlin/.../home/ClockBlockTest.kt`):
    - renders a time string matching `HH:mm`/`h:mm` given a fixed injected `Clock`
    - renders the date in `EEEE, d MMMM` format for a fixed date

- [x] **1.5 `HomeScreen` composable**
  - Deliverable: `ui/home/HomeScreen.kt` — stateless composable taking an `apps: List<AppInfo>` param (supplied by `LauncherViewModel` via `LauncherRoot`); renders clock block + short app list (first 5, standing in for Favorites until Phase 2's picker) + dock (next 4) + gesture bar, per `1a`/`1e` (Airy density).
  - Test (`HomeScreenTest.kt`):
    - given a fixed list of `AppInfo`, exactly 5 appear in the app-list section and 4 in the dock
    - each app row is clickable and reports the tapped `AppInfo` via callback (launching is wired in 1.7/1.9, not asserted here)

- [x] **1.6 `AppDrawerScreen` + `AlphabetRail`**
  - Deliverable: `ui/drawer/AppDrawerScreen.kt` (list layout, letter section headers, `4px 44px 40px 24px` content padding) + `ui/drawer/AlphabetRail.kt` (right-edge rail, drag-to-scrub). Grid layout, search, and left-edge rail (F6/F7/F8) are later-phase work — list-only for this pass.
  - Test (`AppDrawerScreenTest.kt`):
    - apps are grouped under correct letter headers
    - dragging the alphabet rail to a letter scrolls that header into view
  - Test (`AlphabetRailTest.kt`):
    - rail exposes one entry per distinct leading letter in the provided app list, in order

- [x] **1.7 `LauncherRoot`** — Home ↔ Drawer swipe wiring
  - Deliverable: `ui/launcher/LauncherRoot.kt` — swipe up (>55px) opens the drawer; swipe down while the drawer's list is scrolled to top closes it and clears state; transform/opacity timing per README.md (`.34s cubic-bezier(.32,.72,0,1)` / `.24s ease`).
  - Test (`LauncherRootTest.kt`, instrumented, `performTouchInput { swipeUp() }` / `swipeDown()`):
    - swipe up from Home opens the Drawer
    - swipe down at drawer scroll-top returns to Home
    - swipe down while drawer is scrolled (not at top) does *not* close it (scrolls instead)

- [x] **1.8 Launcher icon + strings**
  - Deliverable: adaptive icon (`ic_launcher_background`/`ic_launcher_foreground`), `strings.xml` (`app_name = "Facet Launcher"`).
  - Test: none (asset-only); verified visually in 1.9.

- [x] **1.9 Verification pass**
  - Deliverable: `./gradlew assembleDebug` succeeds; APK installed and launched on the `Medium_Phone_API_36.1` AVD via `adb`; `adb shell screencap` output visually compared against `design_handoff_minimal_launcher/screenshots/light-turn1-home-and-drawer.png` (`1a`/`1b`/`1h`). See "Verification findings" above for the one open item (Home-surface text legibility over arbitrary wallpapers).
  - Test: `./gradlew test` (9/9 passing) and `./gradlew connectedAndroidTest` (9/9 passing) both green on the AVD.

---

## Phase 2 — Settings + persistence

- [x] Room schema: `FacetEntity`, `FavoriteAppEntity`, `DockAppEntity` (F3, F4).
  - Test: DAO round-trip tests (`FacetDaoTest`, `FavoriteAppDaoTest`, `DockAppDaoTest`) — insert/query/delete, position ordering, FK cascade delete, upsert-by-component dedup, all with an in-memory Room database.
- [x] DataStore: launcher-wide toggles (24h time, dock display mode Icons/Text, drawer presentation + grid size, drawer opacity, notification dots on/off).
  - Test (`SettingsRepositoryTest`): each setter round-trips through the settings flow; defaults match the PRD/README (0.88f opacity, `FIVE_BY_SIX`, etc.); opacity is coerced into `0..1`.
- [x] Launcher Settings screen (`3c`) wired to the above — full layout, not-yet-built sections shown disabled rather than omitted (see chat history for the fidelity discussion).
  - Test (`SettingsScreenTest`, `SettingsViewModelTest`): toggling a row updates the underlying value; disabled rows have no click action at all; dock floor/ceiling enforced; back button navigates.
- [x] Dock Icons/Text display mode (new in README, added to PRD F3).
  - Test (`HomeScreenTest.dockTextModeRendersLabelsInsteadOfIcons`): dock renders labels instead of icons when Text mode is set.
- [x] *(pulled forward from Phase 3, since Settings needed a real entry point)* Long-press sheet (`2a`): Launcher settings + Change wallpaper functional; Switch facet / Edit facet visible but disabled until Phase 3.
  - Test (`HomeRouteTest`, `LongPressSheetTest`): holding still past the platform long-press timeout opens the sheet; releasing early cancels it; tapping each functional row invokes its callback; disabled rows don't.
- [x] *(not originally scoped for this phase, but a direct requirement of building Settings/Dock-picker)* Back button on every non-root screen — see the standing convention above.
  - Test: `SettingsScreenTest.backButtonInvokesOnBack`, `DockAppPickerScreenTest.backButtonInvokesOnDone`.
- [x] *(direct design feedback mid-phase, see chat history)* Alphabet rail rework: filtered letters only, compact/centered rail, 90%-height forgiving touch zone with clamped first/last-letter mapping, F8 left-edge access reusing the same zone.
  - Test: `AlphabetRailMappingTest` (band-clamped `letterAt`), `AlphabetRailTest`, `AppDrawerScreenTest` (filtered-letters-only, touch-above-band-clamps-to-first, left-edge mirrors right-edge, indicator shows/hides while dragging).

## ✅ Phase 3 complete — verified on-device

`assembleDebug`, `test` (43 JVM unit tests), and `connectedAndroidTest` (48 instrumented tests) are all green. Full manual walkthrough on-device: long-press Home → sheet now shows Switch/Edit facet as functional (not disabled) → Edit facet → per-facet settings → Favorite apps → checked Chrome + Calendar in the real installed-app list → back → both appear on Home's FAVORITES list, confirming the full round-trip through `FavoriteAppRepository`. Settings → View facets now navigates (was disabled). App Drawer's new search bar renders with real Material Icons (search glyph, letter headers, alphabet rail), its 3-dot overflow menu opens and "Launcher settings" navigates correctly. Settings → App Drawer → "Search bar position" toggled Top→Bottom moved the live search bar to the bottom of the drawer. The Home↔Drawer transition now follows the finger continuously (verified via a slow manual swipe) rather than the old instant threshold-cross.

### What got built
- [x] Facet carousel (`3a`, `3b`): `HorizontalPager`-based center/neighbor scaling with peeking neighbors (`contentPadding`), drag-to-page (native Pager clamping, no infinite scroll), tap-a-neighbor-to-center, Select-to-apply, long-press-to-reorder (index-swap-on-drag, mirroring `SettingsScreen.kt`'s Dock reorder pattern, with an added rotate+shadow lift effect — the first such visual in the codebase). Browsing vs. applying stays purely local (`pagerState.currentPage` never persists until Select fires), satisfying README's `facet`/`pickIdx` state split with no extra state needed.
  - Test: `FacetCarouselScreenTest` — Select applies the centered facet; back returns without applying; Add-facet page hidden at 3 facets; delete blocked/disabled at 1 remaining facet; deleting a facet removes its page.
  - Enabled the long-press sheet's "Switch facet" row (now shows the real active facet name/position/count) and "Edit facet" row.
- [x] Per-facet settings (`3d`) + max-3-facets / min-1-facet rules (F4), reached from the sheet's Edit facet or the carousel's gear. App list content: Favorites selectable, Recents/Most used shown disabled (Phase 4 dependency). Favorite apps row → Favorites picker (`4j`, modeled directly on `DockAppPickerScreen`'s search/checkbox pattern, capped at 8 instead of Dock's 3–5). Rename facet → shared `RenameDialog`. Delete → shared `ConfirmDialog`, required before deletion completes.
  - Test: `FacetSettingsScreenTest`, `FavoritesPickerScreenTest` — favorites count/navigation, cap enforcement at 8, rename round-trips through the repository.
- [x] **Uninstall-collapse rule for Favorites** (F2/F3) — `FavoriteAppRepository.observeFavoritesForFacet` mirrors `DockAppRepository`'s exact hydrate-and-filter pattern; an uninstalled app's entry is filtered out at read time, never a dead tile.
  - Test: `FavoriteAppRepositoryTest`.
- [x] **App Drawer search bar (F6 base search)** — rounded-rectangle bar, text-filters the list, renders at top or bottom per the new Settings row.
  - Test: `AppDrawerScreenTest` — typing filters by name (checked via letter headers, since the search field's own input text otherwise ambiguously matches the same query text); clearing restores the full list (`performScrollToNode` first — `LazyColumn` preserves its scroll anchor by item key across a size change, so clearing doesn't itself scroll back to the top).
- [x] **3-dot overflow menu in the search bar** — Material3 `DropdownMenu` (first use in the codebase) → "Launcher settings", alongside the existing long-press-sheet path.
  - Test: `AppDrawerScreenTest.overflowMenuNavigatesToSettings`.
- [x] **New Settings row: search bar position (Top/Bottom)** — new `LauncherSettings.searchBarPosition` + `SettingsRepository` key/setter, pill row following `DockDisplayModeRow`'s pattern.
  - Test: `SettingsRepositoryTest` round-trip; `AppDrawerScreenTest` (top/bottom-positioned bar checked against screen-half position).
- [x] **Home↔Drawer follow-finger transition rework** — Home and Drawer merged into one composable/route (`HomeDrawerRoute.kt`, replacing `HomeRoute.kt`+`DrawerRoute.kt`), driven by a hoisted `Animatable<Float>` "progress" (0=closed, 1=open). A drag anywhere on Home, or on the drawer's own list once scrolled to the top, tracks the finger 1:1 via `snapTo`; release past/before the halfway point commits open/closed via `animateTo` using README's own drawer-transform easing (`tween(340ms, CubicBezierEasing(.32,.72,0,1))`) rather than a generic physics spring. `FacetNavHost.kt` now has one `home` route instead of two; system back uses Compose's own `BackHandler` (Drawer is no longer a backstack entry) instead of `NavHost` pop.
  - Test: `HomeDrawerRouteTest` — drag past halfway commits open/closed; drag released before halfway springs back; swipe-down-at-top vs. while-scrolled still distinguishes close-vs-scroll; system back closes an open drawer.
- [x] **Icon convention: adopted Material Icons, retrofitted `BackButton.kt`** — added `androidx.compose.material:material-icons-core`; `BackButton.kt`'s `Text("‹")` glyph replaced with `Icon(Icons.AutoMirrored.Filled.ArrowBack)`; new search/overflow icons use `Icons.Default.Search`/`Icons.Default.MoreVert` from the start. Done as part of this phase rather than deferred, since the new search bar needed real icons anyway and retrofitting the one existing glyph alongside it was a small, contained change.

### Real bugs found and fixed during verification (not just test artifacts)
- **A single fast synthetic swipe could skip two `HorizontalPager` pages at once** (landing on the trailing Add-facet page instead of the intended neighbor) — high fling velocity from a full-distance gesture is enough for Pager's snapping to advance more than one page. Not purely a test artifact: the same physics apply to a fast real-world swipe. Test-side, worked around with a slower multi-step controlled drag; worth a closer look if real usage ever reports a similar overshoot.
- **`LazyColumn` preserves its scroll anchor by item key across a drastic size change** — filtering the drawer down to one match and back to the full list does *not* itself scroll back to the top; the list stays anchored near whatever item was first-visible while filtered. Reasonable default behavior (avoids a jarring jump), but worth knowing — nothing auto-scrolls the drawer to the top on search-clear.
- Compose UI test's `assertExists()`/`assertDoesNotExist()` check the semantics tree, not visibility — since the Drawer is now always composed (merely `Modifier.offset` off-screen when closed, for the follow-finger transition), tests asserting open/closed state switched to `assertIsDisplayed()`/`assertIsNotDisplayed()`.
- Mutating external Compose `mutableStateOf` test state directly (outside a real UI-triggered callback) proved unreliable for a `TextField`-backed clear operation; switched to driving the same `performTextInput`/`performTextReplacement` path a real user interaction would take, which is also the more faithful test.

### Deliberately deferred / not pursued
- ~~Live per-facet preview content (real mini favorites/dock rows) on each carousel page~~ — **resolved post-Phase-3, see below.**

## Post-Phase-3 polish — carousel redesign, live preview, theming convention

Direct follow-up work after Phase 3 shipped, driven by hands-on review of the built carousel/reorder flow. `assembleDebug`, `test`, and `connectedAndroidTest` all re-verified green after each change; manual on-device walkthrough confirmed every item below. `design_handoff_minimal_launcher/PRD.md` (F4) and `README.md` (`3a`/`3b`/Interactions/State Management/Assets sections) updated to match — see those files for the user-facing spec, not repeated in full here.

### Carousel redesigned to a recent-apps switcher
- **Tap-to-apply, no Select button.** Tapping any visible card — centred or a peeking neighbour — applies that facet immediately and closes; the header's **Select** text is gone. Browsing is swipe-only and never applies anything.
- **Reordering and rename moved out of the carousel.** Long-press-to-reorder-in-place no longer makes sense once any tap applies a card, so it's replaced by a header **Reorder** link that swaps the whole carousel (pager, dots, gear/trash row) for a plain vertical list: drag handle + name + overflow menu (**Facet settings** / **Delete**) per row, a pinned non-reorderable **Add facet** row at the bottom. Dragging a row by its handle live-reorders (`Modifier.animateItem()` on the other rows), committing on release. Rename now lives inside Per-facet settings, reached from the carousel's gear or the reorder list's overflow menu.
  - The reorder list's drag handle only has a gesture detector on the small handle icon itself, not the whole row — sidesteps any axis-of-scroll conflict entirely, unlike the carousel's own earlier drag-to-page-vs-drag-to-reorder arbitration problem.
- **Card sizing tightened**: uniform `CARD_SCALE = 0.855f` (previously two-tier 70%/60% centred/neighbour scaling) and reduced inter-card spacing, so more of each neighbour peeks in on both sides while browsing.
- **Add-facet card height bug fixed**: the gear/trash row below the pager was conditionally omitted when the Add page was centred (no facet to edit/delete), which let the pager claim that vertical space and made the Add card visibly taller than a real facet card. Fixed by always reserving that row's layout space (`Modifier.alpha(0f)` + `enabled = false` when there's no centred facet) instead of conditionally removing it.
- Test: `FacetCarouselScreenTest` rewritten for tap-to-apply (`tappingACardAppliesItImmediately`, `backWithoutTappingACardLeavesTheActiveFacetUnchanged`) plus new coverage for the reorder list (`reorderLinkOpensAReorderableListAndDoneReturnsToTheCarousel`, `addRowInTheReorderListIsHiddenOnceTheMaximumIsReached`, `deletingFromTheReorderListRemovesTheRow`, `facetSettingsFromTheReorderListNavigatesToEditFacet`).

### Live per-facet preview (resolves the Phase 3 deferral above)
Each carousel card now renders a genuine snapshot of that facet — the real `ClockBlock` composable (not a redrawn lookalike) plus that facet's actual favorite apps with icons, or `No favorites yet` if it has none — instead of a static "facet name + FAVORITES label" shell.
- New `domain/ObserveFacetPreviewsUseCase.kt`, spanning `FacetRepository` (which facets exist) and `FavoriteAppRepository` (each one's favorites) — fans out to *every* facet at once, unlike `ObserveHomeScreenStateUseCase` which only resolves the single active facet's favorites.
- New `FavoriteAppRepository.observeFavoritesForFacets(facetIds)`: fetches the installed-app list **once** and reuses it across all requested facets, rather than each facet independently re-querying `LauncherApps` (which is what naively calling the existing per-facet `observeFavoritesForFacet()` once per facet, fanned out via `combine()`, would do). This wasn't just an efficiency nicety — the redundant concurrent `LauncherApps` queries were real enough to make `FacetCarouselViewModel`'s combined `uiState` land outside Compose test's `waitForIdle()` window, failing 8 of 9 `FacetCarouselScreenTest` cases (see "Real bugs found" below).
- `FacetCarouselViewModel`/`FacetCarouselUiState` extended with `favoritesByFacetId` and `use24HourTime`; `FacetPreviewPage` rewritten accordingly.
- Test: `FavoriteAppRepositoryTest` — `observeFavoritesForFacets` returns each facet's own list keyed by id (not shared across facets), and emits immediately for an empty id list without touching `AppRepository` at all.

### Material3 components now explicitly themed — new standing convention
Found while reviewing the reorder list's overflow menu and the rename/delete dialogs: `AlertDialog`'s default `containerColor` resolves to Material3's own `surfaceContainerHigh` tone, and `OutlinedTextField`'s default border/label colors resolve to other unmapped `ColorScheme` slots — `FacetLightColorScheme` (`ui/theme/Theme.kt`) only maps a handful of slots (`primary`, `surface`, `onSurface`, `onSurfaceVariant`, `error`, `outline`), so anything reading an unmapped one silently renders with Material's stock tones instead of this app's palette. Fixed `ConfirmDialog`/`RenameDialog` (explicit `containerColor = Surface`, themed `OutlinedTextFieldDefaults.colors(...)`) and added a reusable `ui/components/ThemedDropdownMenu.kt` (14dp rounded corners + `Surface` background, matching the dialogs) now used by both the reorder list's overflow menu and the App Drawer's existing one.
- **New rule added to `CLAUDE.md`**: every Material3 component (`AlertDialog`, `DropdownMenu`, `TextField`, etc.) must be given this app's own color/shape tokens explicitly — never left at stock Material defaults, which silently drift from the rest of the UI.
- Also added `androidx.compose.material:material-icons-extended` (previously only `-core`) so the reorder list's drag handle could use the real `Icons.Default.DragHandle` glyph instead of a `Menu` (hamburger) stand-in.

### Dock floor removed, dock now shown in the carousel preview, spacing tuned
Further direct follow-up after the carousel/preview work above. `assembleDebug`, `test` (49 JVM unit tests), and `connectedAndroidTest` (54 instrumented tests) all re-verified green; manual on-device walkthrough (including a full `-wipe-data` emulator reset) confirmed each item. `PRD.md` (F3, F4) and `README.md` (Home layout items 5–6, `3a` carousel behavior, `3c` Settings table, `4k` Dock picker) updated to match.
- **Dock floor removed**: `DockAppRepository.MIN_APPS` `3 → 0` — the dock can now be emptied out completely via the Dock picker or Settings' drag-reorder row, with no blocked-removal state. `HomeScreen.kt`'s dock `Row` is now omitted entirely (not rendered empty) when `dockApps.isEmpty()`, rather than the previous always-3-to-5 assumption.
- **Dock now renders inside every facet carousel preview card**, not just Home — `FacetCarouselViewModel`/`FacetCarouselUiState` extended with `dockApps`/`dockDisplayMode` (sourced from `DockAppRepository`, shared across all facets since the dock isn't per-facet); `FacetPreviewPage` reuses the same `DockIcon` composable Home uses (promoted from `private` to `internal` in `HomeScreen.kt` for cross-package reuse within the module), same 16dp gap / omit-when-empty behavior as Home.
- **Spacing**: fixed 16dp gap added between the favorites list and whatever follows (the dock row, or the screen's own bottom padding when the dock is empty) on both Home and the carousel preview. Favorites-row vertical padding increased `8dp → 10dp` (was `padding(vertical = 8dp)`, giving a 16dp visual gap between adjacent rows; now `10dp` → 20dp).
- **Home-surface text shadow retuned** (`ui/theme/Color.kt`'s `HomeTextShadow`, shared by the clock, favorites rows, and dock text-mode labels): blur `6px → 2px → 1px` and offset `(0,1) → (0,2)` across two rounds of direct feedback, landing on a crisper, less-diffuse shadow than Phase 1's original value.
- Test: `FacetCarouselScreenTest.previewCardShowsBothFavoritesAndDockAppsTogether` (seeds a real favorite + a real dock app together, asserts both render on the same preview card); `HomeScreenTest.emptyDockIsOmittedEntirelyRatherThanShownAsAnEmptyRow`; `DockAppPickerScreenTest.uncheckingIsAllowedDownToAnEmptyDock` (replaces the old `uncheckingIsBlockedAtTheThreeAppFloor`); `SettingsViewModelTest.removing a dock app is allowed down to an empty dock` (replaces the old blocked-at-floor case, found failing during this pass's full-suite run — a leftover assertion from the pre-floor-removal behavior that the earlier instrumented-test update had missed).

### Real bugs found and fixed during verification (not just test artifacts)
- **`composeRule.waitForIdle()` doesn't wait for a `StateFlow` update that hasn't arrived yet** — it settles already-pending recomposition, not arbitrary async work. Adding a genuine `LauncherApps` query into `FacetCarouselViewModel`'s combined `uiState` (via the new preview use case) meant the screen sometimes hadn't rendered its first real frame by the time a test's very next assertion ran, even though the exact same `combine()`-then-`stateIn()` pattern was already used elsewhere without issue — the difference was real system-service latency, not a flaw in the pattern itself. Fixed at the test level: `FacetCarouselScreenTest.setContent()` now explicitly `waitUntil`s the screen actually renders before returning control to each test, rather than trusting a single `waitForIdle()` call caught it.
- A stray `adb uninstall` returning `DELETE_FAILED_INTERNAL_ERROR` during this work surfaced a corrupted emulator instance (likely from many raw `adb shell input touchscreen motionevent` sequences and force-stops during earlier gesture prototyping) — resolved with a full `-wipe-data -no-snapshot-load` relaunch. Worth remembering as a first troubleshooting step if `connectedAndroidTest` or `adb install`/`uninstall` start behaving strangely without a code-level explanation.

## Post-Phase-3 polish — Settings redesign (card layout, dropdowns, Calendar settings shell, per-facet overrides)

Direct follow-up requested ahead of Phase 4: "categorise the settings page better" via a card layout, several rows converted from pill toggles to real dropdowns, conditional row visibility in App Drawer, a shortened Drawer-opacity handle, a new Calendar settings screen, and a per-facet Clock card that can inherit from or override the global defaults. `assembleDebug`, `test` (60 JVM unit tests), and `connectedAndroidTest` (64 instrumented tests) all green; manual on-device walkthrough (screenshots) confirmed every item below, including the Presentation→Grid conditional-row swap and the facet Clock card's inherit/override switch actually gating the toggle.

### New reusable components
- **`ui/components/SettingsCard.kt`**: the app's first true "card" container — 14dp corners (matching the existing dialog/popup radius family) + a 1px `Hairline` border, since the page background is also `Surface` and a border (not a new background tone) is what makes a card read as a group. Also `CardDivider()`, a 1px `Hairline` row separator — the first real use of `Hairline`, which was imported-but-unused in `SettingsScreen.kt` even though README's own row-separator spec (`README.md:148`) called for it.
- **`ui/components/LabeledDropdownRow.kt`**: a generic title + current-value + chevron row that opens a `ThemedDropdownMenu` — the first "labeled dropdown selector" in the codebase (`ExposedDropdownMenuBox` had zero prior usages). Generic over `T` so one composable serves `DockDisplayMode`, `DrawerPresentation`, `DrawerGridSize`, and a facet's list-content-mode enum, with per-option `enabled` support for still-inert choices (Recents/Most used).
- **`ui/components/InheritOverrideCard.kt`**: the two-radio-row "Inherit default" / "Override for this facet" switch README specs at `3f` — reused for both the facet Clock card's 24-hour-time override and Calendar settings' show-all-day-events override, rather than duplicating the radio-row visual twice.

### Settings screen (`3c`) restructured into cards
Every section (`FACETS`, `CLOCK`, `NOTIFICATIONS` — split out of the old `SHARED ACROSS FACETS` grouping, `DOCK`, `APP DRAWER`, `APPEARANCE`, and an unheaded bottom card) is now a `SettingsCard` with `CardDivider()`s between rows, instead of a flat list. Dock's "Show apps as" pill row is now **"Display style"**, a real `LabeledDropdownRow<DockDisplayMode>`. App Drawer's **Presentation** and **Grid size** rows are now real (`LabeledDropdownRow`, wired to `SettingsRepository.setDrawerPresentation`/`setDrawerGridSize` — both already existed end-to-end in the data layer from Phase 2, just never called from the UI) — **Grid size only renders when Presentation is Grid**, and **Show icons**/**Show labels** (new, real, parallel to `showDrawerIcons`) are now mutually conditional on List vs. Grid instead of Show labels being a dead placeholder. Drawer opacity moved from Appearance into the App Drawer card. The old disabled "Calendar events" row is now a real **Calendar** row navigating to the new Calendar settings screen.

### Drawer-opacity slider: themed
Was a stock Material3 `Slider` with no `colors` at all (a real `CLAUDE.md` theming gap). Now explicit `SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Hairline)`. **Revised during the Material 3 shape pass (see `CLAUDE.md`'s shape section):** an earlier version of this also replaced the default thumb with a custom narrow `4dp`-wide/`8dp`-tall bar; reverted back to Material3's own default `Slider` thumb shape, per direct request, once "adhere to real M3 defaults" became the app's governing shape principle.

### New screen: Calendar settings (`4l`), UI shell only
New `ui/settings/CalendarSettingsScreen.kt` + `CalendarSettingsViewModel.kt`, route `calendarSettings?facetId={facetId}` (optional arg, `NO_ACTIVE_FACET_ID` sentinel for the global entry point — same pattern the codebase already uses for "no active facet"). Two real cards: **Show all-day events** (a genuine, persisted toggle — new `LauncherSettings.showAllDayEvents` field) and **Calendars to display** (renders the same permission-denied strip README specs at `4p`, since `READ_CALENDAR` isn't requested and no `CalendarContract` query exists yet — deliberately scoped out this pass, left for Phase 4 to wire into this same screen). Reached from the global Settings Clock card or a facet's Clock card; when facet-scoped, an `InheritOverrideCard` governs whether the toggle writes the global setting or that facet's own override.
- Test: `CalendarSettingsViewModelTest` (global vs. facet-scoped write target; switching to Override seeds the current effective value, switching back to Inherit clears it), `CalendarSettingsScreenTest` (toggle persists in both modes, inherit-mode toggle is read-only, permission strip renders).

### Per-facet settings (`3d`) restructured into cards, Clock card can override the global defaults
"App list content" is now a real `LabeledDropdownRow` (Favorites selectable; Recents/Most used render but stay disabled inside the dropdown — no new persisted field, since nothing else is actually selectable until Phase 4 builds them). "Apps to show" is now conditionally omitted (only relevant for non-Favorites modes) rather than always-shown-disabled. Favorite apps unchanged. A new **Clock** card mirrors the global Settings Clock card's three rows (Clock style, 24-hour time, Calendar) behind a single `InheritOverrideCard` switch — Clock style stays inert (no `ClockStyle` enum exists yet, Known Gap), but **24-hour time is now a real per-facet override** and **Calendar navigates to the same Calendar settings screen scoped to this facet** (its own independent inherit/override switch, not tied to the Clock card's).
- New `FacetEntity` columns `use24HourTimeOverride: Boolean?` / `showAllDayEventsOverride: Boolean?` (`null` = inherit). `FacetDatabase` bumped `version = 1 → 2`; `DatabaseModule.kt` now adds `.fallbackToDestructiveMigration(dropAllTables = true)` — no formal `Migration` class, since there's no released install base and no migration precedent anywhere in the codebase yet (a local install just loses its data on this one upgrade).
- The override is actually consumed, not just stored: `HomeUiState.effectiveUse24HourTime` (`activeFacet.use24HourTimeOverride ?: settings.use24HourTime`) now feeds `HomeDrawerRoute`'s `ClockBlock`, and `FacetCarouselUiState.effectiveUse24HourTime(facetId)` resolves each carousel preview card's own value independently (previously every preview card read the same global `use24HourTime`, which was itself a latent gap in the live-preview feature).
- Test: `FacetRepositoryTest` (override round-trip, null vs. set), `HomeUiStateTest`/`FacetCarouselUiStateTest` (effective-value resolution), `FacetSettingsScreenTest` (dropdown behavior, Apps-to-show visibility, Clock card's inherit/override gating the toggle and persisting through the real repository, Calendar row navigates with the facet's id).

### Real bugs found and fixed during this pass (not just test artifacts)
- **Material3's `DropdownMenuItem(enabled = false)` still exposes an `OnClick` semantics action**, just marked `Disabled` — it does *not* remove the click action the way a plain `Modifier.clickable(enabled = false)` row does. A test asserting `assertHasNoClickAction()` on a disabled dropdown option failed for exactly this reason; fixed by asserting `assertIsNotEnabled()` instead, which is what actually reflects Material's disabled-item semantics.
- Repeat of the now-familiar `StateFlow`-not-yet-populated trap (see the two entries above): an instrumented test clicked the facet Clock card's Override row before the Room-backed facet had loaded into `FacetSettingsViewModel.uiState` — `setOverridingClock` reads `uiState.value.facet`, which was still the initial `null`, so the click silently no-op'd. Fixed by waiting for the facet's name to render first, matching the existing `renamingUpdatesTheDisplayedName` test's pattern, rather than assuming the screen's first frame is already backed by real data.

## ✅ Phase 4 complete — Permission-gated features

- [x] F1 Calendar integration: `READ_CALENDAR` request flow, calendar picker (`4l`), clock event rows, denied-state strip. **Partially pulled forward** (see above) — the Settings-side shell (persisted show-all-day-events toggle, global + per-facet inherit/override, static permission-denied strip) already exists in `CalendarSettingsScreen.kt`; still owned by this phase: the actual `READ_CALENDAR` runtime request, a real `CalendarContract` query for the user's calendars (replacing the static strip), and rendering event rows on `ClockBlock`.
  - Test: denied permission renders the "Calendar access off" strip with a working "Turn on" action; granted + calendars selected renders event rows.
- [x] F2 Recents / Most Used: `PACKAGE_USAGE_STATS` explanation screen + Settings redirect, `UsageStatsManager`-backed list.
  - Test: ungranted state shows the explanation + redirect action, not a broken/empty list silently.
- [x] F6 Contacts search: `READ_CONTACTS` flow, contact rows + call/message/WhatsApp quick actions, apps→contacts→actions ordering.
  - Test: denied contacts access omits the contacts section entirely (never shown empty); WhatsApp action hidden when the app isn't installed.
- [x] *(added mid-planning, see chat history)* **F7 Alphabet rail bucketing rework** — switch from the current naive first-character grouping to `android.icu.text.AlphabeticIndex` (the PRD-decided, locale-correct component the system Contacts app also uses): correctly buckets Swedish Å/Ä/Ö, Spanish Ñ, and transliterates non-Latin scripts instead of dumping them in a generic bucket.
  - Test: locale-specific bucketing cases (e.g. Å/Ä/Ö sort after Z as distinct letters; a non-Latin app name lands in a transliterated bucket, not "#").
- [x] *(added mid-planning, see chat history; scope revised again mid-build — see the Milestone 6 write-up)* **F12 Long-Press Context Menu** (`4i`) — long-pressing an app icon (dock, favorites, or drawer) surfaces app info, uninstall, and the app's own quick actions (App Shortcuts). Rename was dropped from this milestone's scope — deferred to land alongside F11's icon-pack work instead.
  - Test: long-press on a favorites row, a Drawer List row, and a Drawer Grid tile each opens the menu; App info/Uninstall fire the correct `Intent`; quick actions render only when the app publishes shortcuts.
- [x] *(added by the user at Phase 4 kickoff — was previously a "Known gap: no owning phase")* **App Drawer Grid presentation** — `LazyVerticalGrid` rendering honoring `DrawerGridSize.columns`, a flat continuous grid with no letter headers (per direct feedback), the alphabet rail shared with List presentation (same rail/drag logic, jumping to each letter's first app instead of a header row).
  - Test: Grid renders tiles instead of rows for the same app set, no header tags; dragging the rail scrolls to the target letter's first tile.

### Milestone 1 complete — App Drawer Grid presentation + shared alphabet rail + F7 bucketing
`assembleDebug`, `test` (66 JVM unit tests), and `connectedAndroidTest` (66 instrumented tests) all green; manual on-device walkthrough confirmed Grid rendering and rail-drag scrolling.

- **Locale-correct bucketing moved to `domain/GroupAppsByLetterUseCase.kt`** — replaces the naive `label.firstOrNull()?.uppercaseChar()` grouping that lived inline in `AppDrawerScreen.kt`'s composable body (itself a `CLAUDE.md` layering violation, fixed as part of this move) with `android.icu.text.AlphabeticIndex.ImmutableIndex` (`buildImmutableIndex()` — the mutable form only supports iteration, the by-name lookup this needs lives on the immutable index it builds). Takes an explicit `locale: Locale = Locale.getDefault()` parameter rather than reading `Locale.getDefault()` internally, so tests can pin a locale deterministically instead of depending on whatever the test runner's default happens to be.
- **The alphabet rail needed zero changes** — `AlphabetRail.kt`/`letterAt(...)` were already presentation-agnostic (pure `Column` of `Text` + a pure band-clamping function, no `LazyListState` coupling at all). The only generalization was `AppDrawerScreen.kt`'s `onRailLetterChanged`, which now calls an injected `scrollToIndex: suspend (Int) -> Unit` bound to either `listState::scrollToItem` or `gridState::scrollToItem`.
- **Grid renders no letter headers at all — a continuous grid, not visually grouped [revised mid-milestone, per direct feedback]:** the first pass mirrored List's per-letter `item(header) + items(apps)` structure inside the grid too, but the user asked for Grid to just be a flat, uninterrupted grid of every app, with the rail jumping to each letter's *first app* instead of a header row. `GroupedApps` now exposes two separate index maps: `headerIndexForLetter` (List's original "1 header + N apps" flat index, unchanged) and a new `firstAppIndexForLetter` (no header offset — the index of that letter's first app within the flat, concatenated, still-alphabetical app sequence). `AppDrawerScreen.kt`'s `onRailLetterChanged` picks whichever map matches the active presentation. `DrawerGridContent` now just does `items(groupedApps.groups.values.flatten())` — no `item(header, span = maxLineSpan)` calls.
- **New `DrawerGridContent`/`DrawerGridTile`** — `LazyVerticalGrid(columns = GridCells.Fixed(gridSize.columns))`. Tiles: 44dp icon/13dp corners, centered label beneath (`FacetType.appNameGrid`, new — 9.5sp/1.2, added to `Type.kt`) truncated at 56dp width, per README's `1i` spec.
- **`HomeDrawerRoute.kt` now actually reads `drawerSettings.drawerPresentation`/`.drawerGridSize`** — previously computed by Settings (Post-Phase-3 Settings redesign) but never consumed by the Drawer itself, so choosing "Grid" in Settings changed nothing until this pass. Also added a second `LazyGridState` alongside the existing `LazyListState`, with the swipe-down-to-close nested-scroll "at top" check now branching on which presentation is active (a grid's "at top" check needs `LazyGridState.firstVisibleItemIndex`/`.firstVisibleItemScrollOffset`, not the list's).
- Test: `GroupAppsByLetterUseCaseTest` (Robolectric — plain Latin grouping, Swedish-locale Å bucketing distinct from A via an explicit pinned locale, both index maps' math, empty list); `AppDrawerScreenTest` additions (Grid renders tiles not rows for the same apps, no header tags at all; dragging the rail in Grid mode scrolls to the last letter's tile, mirroring the existing List-mode test).

### Milestone 2 complete — live app-list refresh
`assembleDebug`, `test` (66 JVM unit tests), and `connectedAndroidTest` (66 instrumented tests) all green.

- **`AppRepository.observeInstalledApps(): Flow<List<AppInfo>>`** — a `callbackFlow` registering a `LauncherApps.Callback` (`onPackageAdded`/`onPackageRemoved`/`onPackageChanged`/`onPackagesAvailable`/`onPackagesUnavailable`), re-running the existing fetch-and-sort logic on every callback and once immediately on subscription, `awaitClose { launcherApps.unregisterCallback(...) }`. The existing one-shot suspend `getInstalledApps()` is unchanged and still used by the Favorites/Dock pickers' seed steps and `FavoriteAppRepository`/`DockAppRepository`'s hydrate step — only the primary Home/Drawer source (`GetInstalledAppsUseCase.observe()` → `LauncherViewModel`) switched to the live flow.
- **Closes a real, previously undiscovered gap**: `LauncherViewModel` fetched the installed-app list exactly once at init, so an app installed or uninstalled while Facet was in the foreground (including via Phase 4's still-upcoming F12 Uninstall action) wouldn't be reflected anywhere until the process restarted.
- Test: `AppRepositoryTest.observeInstalledApps re-emits after a package is removed` — captures the registered `LauncherApps.Callback` via `ArgumentCaptor`, fires `onPackageRemoved` manually, asserts a second emission reflects it. Collects via a `Channel` + suspending `receive()` rather than `backgroundScope.launch { ... } + advanceUntilIdle()` — `getInstalledApps()`'s `withContext(Dispatchers.Default)` hop is a *real* dispatcher, which `advanceUntilIdle()` (virtual-time-only) doesn't wait for; genuine suspension does. `LauncherViewModelTest` updated to stub `observeInstalledApps()` instead of the old one-shot method.

### Milestone 3 complete — F2 Recents / Most Used
`assembleDebug`, `test` (89 JVM unit tests), and `connectedAndroidTest` (68 instrumented tests) all green; manual on-device walkthrough confirmed selection, persistence, the ungranted-access strip, the explanation screen, the real system Settings redirect, and (once granted) real ranked "Most used" data (`Facet Launcher`, `Settings`, `Gmail`, `Files`, `Phone` on the dev device).

- **`FacetEntity` gained real `listContentMode: ListContentMode` (default `FAVORITES`) and `appsToShowCount: Int` (default 5, coerced to README `3d`'s 4…8 range) fields** — a Room `Converters` class (new) stores the enum as its `name` string via `@TypeConverters(Converters::class)` on `FacetDatabase`, bumped to schema v3 (`.fallbackToDestructiveMigration`, same pre-release precedent as the v1→v2 bump). Unlike 24h-time/Calendar's inherit/override pair, this is a plain non-nullable field — each facet's list mode is independently configured, there's no "global default" to inherit from.
- **`UsageStatsRepository`** wraps `UsageStatsManager.queryUsageStats(INTERVAL_BEST, ...)` over a rolling 30-day window, aggregating a package's possibly-multiple sub-interval `UsageStats` entries into one (max `lastTimeUsed`, summed `totalTimeInForeground`) before ranking and hydrating against `AppRepository`'s installed-app list (uninstalled/never-used packages excluded). `getRecentApps(limit)` ranks by last-used time; `getMostUsedApps(limit)` by total foreground time.
- **`UsageAccessRepository.isGranted()`** — `PACKAGE_USAGE_STATS` is a special-access permission that never surfaces via `checkSelfPermission`; checked via `AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, ...)`, granted only through the system Settings redirect, so callers re-check on resume rather than relying on a grant-change callback (there isn't one).
- **`ObserveHomeScreenStateUseCase` restructured**: resolves the active `FacetEntity` first, then branches by `listContentMode` — `FAVORITES` → the existing `FavoriteAppRepository` flow unchanged; `RECENTS`/`MOST_USED` → `UsageStatsRepository`, gated on `UsageAccessRepository.isGranted()` (ungranted → empty list + `usageAccessGranted = false` on `HomeScreenState`, never a broken partial list). Also gained a `refresh()` method (backed by a replay-1 `MutableSharedFlow` trigger combined into the pipeline) since granting usage access happens out-of-band via system Settings with no callback — `HomeViewModel.refresh()` is wired to a `DisposableEffect`/`LifecycleEventObserver` in `HomeDrawerRoute.kt` that fires on `ON_RESUME`.
- **`HomeUiState.appListItems`** (renamed from `favorites`, which no longer fit once the same field could hold Favorites/Recents/Most-Used) plus new `activeListContentMode`/`showUsageAccessPrompt` derived properties. `HomeScreen.kt`'s section label is now dynamic ("FAVORITES"/"RECENTS"/"MOST USED"); a new `UsageAccessStrip` (README `4p`'s dashed-strip styling, with a real `onClick` this time — "Most used needs usage access from system settings." / "Open settings", exact copy from README's strip table) replaces the app-row list when `showUsageAccessPrompt` is true.
- **New `UsageAccessExplanationScreen.kt`** — the PRD-required "onboarding explanation screen" shown before the `ACTION_USAGE_ACCESS_SETTINGS` redirect (a special-access permission can't be requested via a normal runtime dialog). Auto-dismisses back to its caller the moment `UsageAccessExplanationViewModel.isGranted` flips true, re-checked on every resume. Reached from Home's strip; new `FacetDestinations.USAGE_ACCESS_EXPLANATION` route.
- **`FacetSettingsScreen.kt`'s "App list content" dropdown is now fully real** — all three options enabled (previously Recents/Most used were visible-but-disabled placeholders), backed by `FacetRepository.setListContentMode`/`setAppsToShowCount`. A new "Apps to show" row (4…8) appears only when mode ≠ Favorites.
- **`LabeledDropdownRow` reworked twice more during manual verification, per direct feedback:**
  - *Positioning*: the dropdown previously opened flush with the row's far-left title regardless of where the tappable value sat, because the `Popup`'s anchor was the whole row. Anchoring it to just the trailing value+chevron area fixed this for most cases, but Material3's own start/end-of-anchor fallback (it only right-aligns when left-aligning would overflow the window) turned out to depend on how wide the *currently selected label* happened to be — "Favorites" (short) and "Recents"/"Most used" (longer) resolved to different, inconsistent positions, and the fallback didn't respect the surrounding card's own padding either. Fixed deterministically instead: the menu's own rendered width is tracked (`Modifier.onSizeChanged`) and fed back in as an explicit `offset`, right-aligning it with the trailing anchor's edge regardless of either width.
  - *Click scope*: originally the whole row opened the dropdown; changed to only the label+chevron "options area," per direct feedback (title text is no longer part of the tap target).
  - *Per-option `testTag`s* (`"${rowTestTag}_option_${option}"`) were added to every `ThemedDropdownMenuItem` so tests can target a specific option deterministically instead of by text (which the row's own current-value label can also match once selected).
- **Grid size label reads "N cols × M rows"** (was "N × M") — `DrawerGridSize.displayLabel()` in `SettingsScreen.kt`, per direct feedback; the enum's own `columns`/`rows` field order (`FIVE_BY_SIX(5, 6)`) was already columns-first, so the label just names that order explicitly.
- Test: `UsageStatsRepositoryTest` (Robolectric, driving the real `UsageStatsManager` via `ShadowUsageStatsManager` rather than mocking `UsageStats` directly — mocking it didn't reliably intercept under Robolectric's own class instrumentation; ranking, sub-interval aggregation, limit capping, uninstalled/zero-foreground-time exclusion); `FacetRepositoryTest` (listContentMode/appsToShowCount round-trip, including the 4…8 coercion); `ObserveHomeScreenStateUseCaseTest` (Favorites/Recents/Most-used branching, ungranted state); `HomeUiStateTest` (`showUsageAccessPrompt` derivation); `HomeScreenTest` additions (dynamic section label, the strip replacing the list and firing its callback); `FacetSettingsScreenTest` additions (dropdown persists a Recents/Most-used selection, "Apps to show" appears only when relevant and persists its own selection) — both had to be rewritten to wait for the real Room-backed facet to load first (`waitUntil { onNodeWithText("Facet 1")... }`), same async-I/O-vs-idling gap as this file's other Room-backed tests: selecting an option while `uiState.facet` is still null silently no-ops.

### Real bugs found and fixed during this pass (not just test artifacts)
- **Two pre-existing `SettingsScreenTest`/`FacetSettingsScreenTest` cases were flaky around `ThemedDropdownMenu`'s `Popup`** — clicking a `LabeledDropdownRow` to open its dropdown, then immediately querying for a menu item, intermittently found nothing, even though the same flow works correctly when driven manually on-device (confirmed directly). Root cause: a `DropdownMenu`'s `Popup` is a separate window that doesn't always finish registering with Compose UI test's root registry by the time `performClick()` returns — and a single `waitForIdle()` immediately after the click didn't reliably catch it either, in one case needing an explicit `waitUntil` poll (the same class of gap as the `StateFlow`-vs-`waitForIdle()` lesson elsewhere in this doc, just triggered by cross-root Popup state instead of async I/O). Fixed at the test level in both files; not an app bug. Also disabled emulator animation scales (`window_animation_scale`/`transition_animation_scale`/`animator_duration_scale` → `0`) as a general instrumented-test reliability measure, though it alone didn't resolve this specific issue.
- **`runTest`'s `advanceUntilIdle()` only pumps the virtual test-dispatcher queue, not real dispatcher hops** — `AppRepository.getInstalledApps()`'s `withContext(Dispatchers.Default)` is a genuinely real dispatcher, so a `backgroundScope.launch { flow.collect { ... } } + advanceUntilIdle()` test pattern raced and never saw the first emission. Fixed by collecting into a `Channel` and using suspending `receive()` calls, which correctly await real dispatcher work regardless of virtual time. Not an app bug — a JVM-unit-test analogue of the Compose-test `waitForIdle()`-vs-real-async-work lesson already recorded above.
- **Two more full-suite-only instrumented failures** (`FavoritesPickerScreenTest.checkingAnAppAddsItToFavorites`, then on a later run `DockAppPickerScreenTest.uncheckingIsAllowedDownToAnEmptyDock`, both searching for the real device's alphabetically-first app, `com.google.android.calendar`) — initially assumed to be transient environment/emulator-load flakiness since both passed in isolation and on a later clean full-suite run. Turned out to be a real, reliably-reproducible bug once it started failing every run: **both `FavoritesPickerViewModel` and `DockAppPickerViewModel` populate `installedApps` from a real suspend fetch in `init { viewModelScope.launch { installedApps.value = getInstalledApps() } }`** — a genuine `Dispatchers.Default` hop through `AppRepository.getInstalledApps()`. Both test files' `setContent()` helpers only called `composeRule.waitForIdle()` before interacting, which doesn't reliably wait for that real dispatcher hop (same async-I/O-vs-idling gap as `AppRepositoryTest`'s `advanceUntilIdle()`-vs-real-dispatcher fix and the `ThemedDropdownMenu` `Popup`-timing fixes above), so the test could type into the search field and query for a row before the installed list ever populated, and the target row never rendered. Fixed in both `FavoritesPickerScreenTest` and `DockAppPickerScreenTest` (`checkingAnAppAddsItToFavorites`, `checkingIsBlockedAtTheEightAppCap`, `checkingAnAppAddsItToTheDock`, `uncheckingIsAllowedDownToAnEmptyDock`) by computing the target row's tag up front and polling for it to exist (`waitUntil(timeoutMillis = 3_000) { runCatching { onNodeWithTag(rowTag).assertExists() }.isSuccess }`) before searching/interacting, mirroring `FacetSettingsScreenTest`'s existing `waitUntil { onNodeWithText("Facet 1")... }` pattern. Verified each file individually (multiple repeat runs, all green) after the fix. Not an app bug — a test-only fix.
- **A third such flake surfaced during Milestone 3's own full-suite run** — `FacetCarouselScreenTest.deletingFromTheReorderListRemovesTheRow` (unrelated file, not touched this milestone) failed only in the 68-test full run, passed cleanly in isolation immediately after. Same transient-environment-load pattern as the two above, not a regression.
- **New instrumented tests exercising a `LabeledDropdownRow` selection followed by a Room-persistence assertion initially failed deterministically (not flakily) at the same spot every run** — traced to the tests missing the `waitUntil { onNodeWithText("Facet 1")... }` guard this file's *other* Room-backed `FacetSettingsScreenTest` cases already use: without it, `uiState.facet` can still be `null` when the click fires, and `FacetSettingsViewModel.setListContentMode`/`setAppsToShowCount` both silently no-op via a `?: return` guard against a null facet — the row's default-Favorites label looks correct on screen the whole time, masking the no-op. Fixed by adding the same wait; not an app bug.

## Post-Milestone-3 polish — Material 3 shape conformance (new governing principle, see `CLAUDE.md`)

*(User-directed, see chat history: every component's corner radius must come from Material 3's real shape scale/defaults via `MaterialTheme.shapes`, not a hand-picked dp value — added to `CLAUDE.md` as a standing rule. This deliberately supersedes the original `design_handoff_minimal_launcher/README.md` mockup's own hand-specified radii — e.g. its repeated 14dp for dialogs/cards/popups — for anything that's a real M3 component; icon/avatar tiles stay README-governed since M3 doesn't have a shape token for those.)*

`FacetLauncherTheme` doesn't override `MaterialTheme`'s `shapes` param, so `MaterialTheme.shapes` was already Compose Material3's own default `Shapes()` — confirmed against the library's own token source (`androidx.compose.material3.tokens.*Tokens.kt`, Material3 1.4.0) rather than guessed:

- **`ThemedDropdownMenu`** (Menu) — 14dp → `MaterialTheme.shapes.extraSmall` (4dp, M3's real Menu default).
- **`ConfirmDialog`/`RenameDialog`** (Dialog) — 14dp → `MaterialTheme.shapes.extraLarge` (28dp).
- **`SettingsCard`** (Card) — 16dp → `MaterialTheme.shapes.medium` (12dp).
- **`LongPressSheet`** (ModalBottomSheet-equivalent) — `topStart/topEnd 20dp` → `28dp` (M3's `SheetDefaults.ExpandedShape`/`extraLarge`, top-only — built as a literal `RoundedCornerShape`, since `MaterialTheme.shapes` has no per-corner slot).
- **Drawer search bar** (`DrawerSearchBar`, M3 SearchBar) — 14dp → `CircleShape` (M3's SearchBar container is fully rounded by default).
- **`UsageAccessExplanationScreen`'s "Open settings"** (Button) — 12dp → `CircleShape` (M3 buttons default to `CornerFull`).
- **`SettingsScreen`'s `PillOption`** (Search-bar-position Top/Bottom, a SegmentedButton) — 10dp → `CircleShape` (M3's `OutlinedSegmentedButtonTokens.Shape` is `CornerFull`).
- **`FacetCarouselScreen`'s `FacetReorderRow`/`AddFacetRow`** (Card-like rows) — 16dp → `MaterialTheme.shapes.medium`.
- **`FacetCarouselScreen`'s carousel page / `AddFacetPage`** (large hero surface, no single canonical M3 token — treated like Dialog/ModalBottomSheet's size class) — 20dp → `MaterialTheme.shapes.extraLarge`.
- **Drawer-opacity `Slider`'s custom thumb reverted to M3's own default thumb shape** (see the dedicated write-up above) as part of the same "trust real M3 defaults" pass.
- **Deliberately left alone** (icon/avatar tiles and other pure image/glyph masks, not M3 components): app-icon and dock-icon tiles (9/10/13/14/15dp), the alphabet-rail drag letter-indicator bubble, progress/selection dots, grab handles, checkbox radii.
- Verified on-device: dialog/card/menu/sheet/search-bar/segmented-pill radii all visibly changed as expected; full `test` (89 JVM unit tests) and `connectedAndroidTest` (68 instrumented tests) green — no test asserted on the old shape values, so this was a pure visual change with no functional regressions.

## Post-shape-conformance polish — velocity-based drawer gesture, sheet/screen animation, real default-launcher check, test hardening

Landed concurrently with the shape-conformance pass above, in a separate discussion (not driven through this session's own tool calls — confirmed by reading the resulting code and forcing a clean `compileDebugKotlin --rerun-tasks`, which built successfully with no new warnings beyond pre-existing deprecation notices). Two user-stated goals: (1) the Home↔Drawer gesture should consider flick *velocity*, not just how far the drag traveled; (2) sheets, screens, and dropdowns should animate in/out rather than cut instantly. Full unit suite reconfirmed green (`test`, 79 JVM unit tests) after these changes; a live-device instrumented re-run was attempted but the shared AVD was mid-use by a concurrently-running background session at the time (`DELETE_FAILED_INTERNAL_ERROR` on APK uninstall, the same transient-emulator-contention symptom already documented above under Phase 3) — deferred rather than forcing a `-wipe-data` reset that would have discarded that other session's in-progress run.

- **Home↔Drawer drag now commits open/closed on flick velocity, not just distance travelled.** `HomeDrawerRoute.kt` adds a `VelocityTracker` (Compose's own utility) alongside the existing drag handling — `addPosition()` on every move, `calculateVelocity()` on release. `settle(velocity)` now checks velocity *first*: a fling past `VELOCITY_THRESHOLD_PX = 1000f` (either direction) commits open/closed immediately regardless of how far the drag actually travelled; short of that threshold it falls back to the original distance-based rule (`COMMIT_TRAVEL_FRACTION`, tightened `0.25f → 0.20f` in the same pass — a bit less distance is now needed to commit). Wired into both drag paths: the direct drag-on-Home gesture (`detectVerticalDragGestures`' `onDragEnd`) and the drawer-list-scroll-driven close gesture (`NestedScrollConnection.onPreFling`, via `available.y`). Also refactored `dragBy`/`settle` to track a separate `dragTargetProgress` (the drag's intended target) apart from `progress.value` (the actually-animating value), so a fast drag's distance/velocity math isn't thrown off by animation lag; `containerHeightPx`'s pre-measurement default changed `1f → 2000f` to avoid a near-zero-divisor glitch on the very first frame.
- **Long-press sheet now animates in/out** instead of appearing/disappearing instantly: `HomeDrawerRoute.kt` wraps it in `AnimatedVisibility` with `slideInVertically`/`slideOutVertically` using README's own drawer-transform easing (`tween(340ms, CubicBezierEasing(.32,.72,0,1))`). The scrim (dimmed background + tap-outside-to-dismiss) moved out of `LongPressSheet.kt` into `HomeDrawerRoute.kt` itself as its own `AnimatedVisibility` (`fadeIn`/`fadeOut`, 240ms), so `LongPressSheet` no longer takes an `onDismiss` param — it's now a pure sheet-content composable, with the caller owning the scrim/dismiss-click layer around it. `LongPressSheet.kt`'s doc comment updated to match. **Gap**: no instrumented test yet covers tapping the scrim to dismiss (`HomeDrawerRouteTest.kt` has no scrim-tap case).
- **Whole-app navigation transitions**: `FacetNavHost.kt`'s `NavHost` now declares `enterTransition`/`exitTransition`/`popEnterTransition`/`popExitTransition` (slide + fade, same 340ms easing curve) instead of the default instant-cut between destinations — applies to every route (Settings, pickers, Facet settings, Calendar settings, the new Usage Access explanation screen, etc.), not just Home/Drawer.
- **Dropdowns weren't touched in this pass** — `ThemedDropdownMenu.kt`/`LabeledDropdownRow.kt` have no new animation code (checked directly, no diff there). Material3's `DropdownMenu` already ships its own default open/close transition (fade + expand) with no app-side wiring needed — this is what reads as "animated" for dropdowns; distinct from the sheet/nav-transition work above, which genuinely replaced an instant cut.
- **Real "set as default launcher" status**: new `data/DefaultLauncherRepository.kt` (`isDefaultLauncher()`, resolving `ACTION_MAIN`/`CATEGORY_HOME` via `PackageManager` and comparing against this app's own package) replaces whatever previously backed `SettingsUiState.isDefaultLauncher`. `SettingsViewModel` re-checks it once at `init` via the repository rather than a hardcoded/static value. New `domain/ObserveSettingsScreenStateUseCase.kt` also appeared alongside this, consistent with the existing `ObserveHomeScreenStateUseCase` pattern. **Gap**: no dedicated `DefaultLauncherRepositoryTest` or `SettingsViewModelTest` case exists yet for this — `SettingsScreenTest.functionalRowsHaveClickActions` only asserts the row *has* a click action, not that its status reflects a real check. Per `CLAUDE.md`'s testing bar, this isn't fully "done" until that lands.
- **`UsageStatsRepositoryTest` rewritten to drive the real `UsageStatsManager` via Robolectric's `ShadowUsageStatsManager`** instead of directly mocking `UsageStats`/`UsageStatsManager` with Mockito — the mock-based version this session originally wrote had compiled and passed at the time, but apparently didn't reliably intercept calls under Robolectric's own class instrumentation in practice; the shadow-based approach (same pattern `FacetDaoTest` already uses for Room) is more robust. Test count and coverage (ranking, sub-interval aggregation, limit capping, uninstalled/zero-foreground-time exclusion) unchanged.
- **Gap**: none of the velocity-based commit logic has test coverage yet — `HomeDrawerRouteTest.kt` still only covers the original distance-based drag-past-halfway/before-halfway cases (per Phase 3's write-up), nothing exercises a fast short flick committing via `VELOCITY_THRESHOLD_PX` instead of distance. Worth adding given `CLAUDE.md`'s testing bar, using `composeRule.mainClock` control the same way this codebase's other pointerInput-timing tests do (see Phase 2's "Real bugs found" notes on `mainClock.advanceTimeBy`).

### Real bug found: the new animations looked broken on-device — traced to this session's own earlier emulator settings
User reported the long-press sheet "just appears instead of animating in." Not a code bug — earlier this session, `animator_duration_scale`/`window_animation_scale`/`transition_animation_scale` were all set to `0` on this AVD as a test-reliability measure (see Milestone 3's "Real bugs found" section). Jetpack Compose reads `Settings.Global.ANIMATOR_DURATION_SCALE` via its `MotionDurationScale` mechanism and scales **all** Compose animation durations by it — not just instrumented tests, the real running app too — so with the scale at `0`, every `tween()`/`AnimatedVisibility` transition (the sheet, its scrim, all `NavHost` route transitions) completed instantly regardless of the animation code being correct. Confirmed no code-level animation-disabling exists anywhere (grepped for `snap()`, disable-animation flags, checked the app theme/manifest — nothing). Fixed by restoring all three settings to `1`; user confirmed on-device afterward that the sheet, scrim, and route transitions all animate correctly. **Lesson for future sessions on this emulator**: don't leave animator-duration-scale at `0` between work sessions — it silently fixes flaky Popup-timing tests but also silently breaks the real app's own animations for anyone testing on the same device afterward. Prefer disabling it only for the duration of an instrumented test run, or accept the flakiness workarounds already documented instead.

### Facet selection from the carousel now always returns to Home, regardless of entry path
Reported gap: tapping a facet card in the carousel applied the facet but returned to wherever the carousel was *entered from* — Home directly (correct), but Settings → Facets → tap a card landed back on Settings instead of Home. Root cause: `FacetCarouselScreen`'s card-tap handler (`onSelect`) reused the same `onBack` callback as the screen's actual back button, which only pops one level off whatever back stack got it there.
- **Fixed** by splitting the two concerns: `FacetCarouselScreen` now takes a distinct `onFacetApplied: () -> Unit` alongside `onBack` — `onBack` still just pops one level (back-without-applying, unchanged); `onFacetApplied` fires only when a card is actually tapped/applied. `FacetNavHost.kt` wires `onFacetApplied` to `navController.navigate(HOME) { popUpTo(HOME) { inclusive = false }; launchSingleTop = true }`, which clears everything above the existing Home entry off the back stack and reuses it (rather than recreating Home fresh) — works identically whether the carousel was reached directly from Home's long-press sheet or via Settings → Facets.
- Test: `FacetCarouselScreenTest.tappingACardAppliesItImmediately` extended to assert `onFacetApplied` fires and `onBack` does *not* on a card tap (previously asserted only the repository-level active-facet change, not which callback fired) — all 10 `FacetCarouselScreenTest` cases green. Manually verified on-device: Settings → Facets → tap a card now lands on Home, not back on Settings.

### Small fixes landed alongside Milestone 4/5 work
- **Removed the redundant "Done" button from the facet reorder screen** — reordering already autosaves on drag-release (`onCommit()` from `onDragEnd`), so the button did nothing the back button/system back didn't already do. `FacetCarouselScreenTest.reorderLinkOpensAReorderableListAndDoneReturnsToTheCarousel` renamed to `...AndBackReturnsToTheCarousel`, now exercises the back button instead.
- **New `domain/FlowCombine.kt`** — custom `combine6`/`combine7` overloads for when a 6th/7th flow is eventually needed. Kotlin's positional `combine()` only has overloads up to 5 flows; beyond that the only built-in option is the untyped vararg form (`Array<T>`, one shared type, unchecked casts). Per direct instruction, implemented instead as typed nested `combine(combine(f1,f2,f3,::Triple), combine(f4,f5,f6,::Triple)) { a,b -> ... }` — full type safety, no arity ceiling, one overload written per arity actually needed. Added proactively; the 5-flow `ObserveHomeScreenStateUseCase` combine below still fits Kotlin's native overload and doesn't use these yet.

### Milestone 4 complete — F1 Calendar integration
`assembleDebug`, `test`, and `connectedAndroidTest` all green (confirmed via a fresh full run, then a targeted re-run of the affected classes after the fixes below).

- **Permission**: added `READ_CALENDAR` to the main manifest; `CalendarSettingsScreen.kt`'s "Turn on" action wired to `rememberLauncherForActivityResult(RequestPermission())`, replacing the previously-inert styled text.
- **New `data/CalendarRepository.kt`** (`getCalendars()`, `getTodayEvents(calendarIds, includeAllDay)` via `CalendarContract.Calendars`/`Instances.query`) and **`data/CalendarPermissionRepository.kt`** (`isGranted()` via `ContextCompat.checkSelfPermission`).
- **`CalendarSettingsViewModel`/`Screen` rewritten**: the static permission-denied strip from the earlier Settings-redesign shell is now real — granted state renders an actual checkbox list of the device's calendars (`calendar_picker_row_$id`), persisted to a new `LauncherSettings.selectedCalendarIds: Set<String>?` DataStore field (`null` = "not yet initialized, implicitly all calendars" distinct from an explicit empty set). `DisposableEffect`/`LifecycleEventObserver` re-checks the grant on resume, since there's no direct callback for a permission changed outside the app.
- **`ClockBlock.kt`** gained `events: List<CalendarEvent>`/`onEventClick` — renders event rows below the date (rule + time + title, accent rule on the next upcoming event), sourced through `ObserveHomeScreenStateUseCase`'s now-5-flow `combine()`. Tapping a row fires `ACTION_VIEW` on the event's own `CalendarContract.Events` content URI.
- Test: `CalendarRepositoryTest` (5 tests — Robolectric has no real Calendar Provider registered by default, so this mocks `ContentResolver` directly with `MatrixCursor` rather than attempting genuine inserts); `CalendarSettingsViewModelTest`/`CalendarSettingsScreenTest` additions (denied strip, granted list rendering + persistence); `ObserveHomeScreenStateUseCaseTest`/`ClockBlockTest` additions (event ordering, click intent).

### Real bugs found — Milestone 4
- **`CalendarContract.Instances.query()` builds its own internal selection/URI before delegating to `ContentResolver.query()`** — an initial mocked-cursor test stub using `isNull()` matchers for selection/selectionArgs/sortOrder didn't match the framework's real (non-null) internal args. Fixed with `nullable()` matchers instead. Test-only, not an app bug.
- **`ClockBlockTest` asserted "10:00 AM"** — the app's actual time format (`"h:mm"`) has no AM/PM marker; the test's own expectation was wrong, not the code. Fixed to "10:00"/"11:00".
- **`ObserveHomeScreenStateUseCaseTest`'s new calendar tests NPE'd** on an unstubbed `favoriteAppRepository.observeFavoritesForFacet(1L)` (the default test facet uses `FAVORITES` mode) — an unstubbed Mockito `Flow`-returning mock returns Kotlin `null`, which NPEs when `combine()` tries to collect it. Fixed by adding the stub, matching every other test in the file.
- **`WRITE_CALENDAR` permission placement took two attempts to get right.** `CalendarSettingsScreenGrantedTest` seeds a real calendar into the device's Calendar Provider to test the granted-state UI end-to-end (inserting a calendar is a write operation, even though the app itself only ever reads). First attempt declared `WRITE_CALENDAR` in a new `app/src/androidTest/AndroidManifest.xml` — this compiled and merged correctly, but the `GrantPermissionRule` grant still failed with a `SecurityException` at insert time, because **self-instrumenting androidTest code runs inside the app-under-test's own process/UID**, so `GrantPermissionRule` checks the permission against the *app's* declared permissions, not the separate androidTest APK's manifest. Fixed by moving the declaration to `app/src/debug/AndroidManifest.xml` instead — scoped to the debug build variant (which `connectedAndroidTest` always runs against, and the only variant this project ever ships per `CLAUDE.md`'s Build & run section), so it's present in every real test/dev install but would never ship in a release build.

---

### Milestone 5 complete — F6 Contacts search
`assembleDebug`, `test`, and `connectedAndroidTest` all green (fresh full run, then a targeted re-run confirming the back-press fix below).

- **Permission**: added `READ_CONTACTS` to the main manifest. Settings' previously-disabled "Search contacts" row is now a real `ToggleRow` — turning it on triggers `rememberLauncherForActivityResult(RequestPermission())`; denial reverts the toggle. New `data/ContactPermissionRepository.kt`/`data/ContactRepository.kt` (`searchContacts(query, limit)` via `ContactsContract.CommonDataKinds.Phone`, deduped by `CONTACT_ID` keeping the first/primary phone number — no multi-number picker, per the approved scoping).
- **`DrawerViewModel` rewritten**: `combine(searchQuery, settingsRepository.settings).flatMapLatest { ... }` → `contactResults: StateFlow<List<ContactInfo>>`, gated on the toggle + grant + a non-blank query, capped at 5.
- **`AppDrawerScreen.kt` search-mode rework**, matching the `1j` mockup plus direct clarification: search now renders a flat `DrawerSearchResults` (no letter headers/rail — those stay browse-mode only) — an **APPS** section reusing whichever row/tile the active presentation (List/Grid) already uses, capped at 5, then a **CONTACTS** section, capped at 5, collapsed by default. Tapping a contact row toggles that row's own expanded state, revealing Call/Message/WhatsApp-if-installed action chips (`ACTION_DIAL`/`ACTION_SENDTO`/`ACTION_VIEW`) — each row's expansion is independent. The contacts section is omitted entirely (not a permission strip) when ungranted, matching the mockup's own denied-state annotation. The empty state (`4q`) was simplified during review to an icon + single line (`No matches found for "$query"`) rather than the original two-line title/subtitle copy.
- **New `BackHandler(enabled = query.isNotBlank()) { onQueryChanged("") }`** inside the Drawer — first back press while searching clears the query and stays open; only a second, empty-query back press falls through to `HomeDrawerRoute.kt`'s existing drawer-close handler.
- Test: `ContactRepositoryTest` (5 tests, same mocked-`ContentResolver`+`MatrixCursor` pattern as `CalendarRepositoryTest`); `AppDrawerScreenTest` additions — search results capped at 5 with no letter headers, empty state renders and its clear button works, contacts section omitted when there are no matches, tap-to-expand/collapse chips (WhatsApp correctly absent since the test device doesn't have it installed — exercises the real "not installed" branch, not a mocked one), back-press-while-searching clears the query without closing the drawer.

### Real bugs found — Milestone 5
- **The back-press test initially timed out waiting for search results to clear.** Root cause: the search field still held focus with the IME visible after `performTextInput()`, so the *first* system back press was consumed by keyboard dismissal — standard OS behavior, handled below the Activity's back-callback stack, before the app's own `BackHandler` ever sees it — not a code bug. Fixed at the test level (`Espresso.closeSoftKeyboard()` before `pressBack()`), matching what a real user experiences as needing a second back press once the keyboard is up.
- **Contacts chips are deliberately narrow in scope for now** (Call/Message/WhatsApp-if-installed, hardcoded). A follow-up question about whether Android exposes contact "connections" more generally (the same `ContactsContract.Data`/sync-adapter mechanism the stock Contacts app uses to surface arbitrary connected apps) led to scoping that as its own follow-up rather than expanding this milestone — tracked separately as **Phase 9**, below, including a bottom-sheet redesign of the tap-to-act interaction.

---

### Milestone 6 complete — F12 Long-Press Context Menu

**Scope revised mid-build, see chat history**: the originally-planned Rename row was pulled out entirely — it'll land alongside F11's icon-pack work instead of here — and replaced with a genuinely different feature: **quick actions**, i.e. the app's own published App Shortcuts (`LauncherApps`' shortcuts API — the same "Compose"/"New event"-style actions the stock Android launcher surfaces on long-press), not something enumerated by Facet. `assembleDebug` and `test` (99 JVM unit tests) green. `connectedAndroidTest` (83 instrumented tests): a full-suite run hit 2 failures — `DockAppPickerScreenTest.checkingAnAppAddsItToTheDock` and `CalendarSettingsScreenTest.permissionDeniedStripRendersAndIsClickableWhenUngranted` — neither in a file this milestone touched; both passed cleanly on an immediate isolated re-run. A third, different `CalendarSettingsScreenTest` case (`facetScopedModeShowsInheritOverrideSwitch`) then failed on that re-run too, also passing clean on its own right after — three different tests, two different classes, none repeating twice, matching this project's already-documented full-suite-only Popup/async-load-timing flake pattern (see Milestone 3's "Real bugs found"), not a regression from this milestone.

- **Shared `AppIcon` composable** (`ui/components/AppIcon.kt`, new) — every place an app icon renders (Home's favorites/dock, Drawer's List/Grid rows and search results, the new context menu's header, Settings' dock editor, the Favorites/Dock pickers, the facet carousel's preview cards) now goes through this one function instead of five-plus independently-duplicated `if (icon != null) Image(...) else Box(...)` blocks. Called out mid-build, ahead of F11's icon-rendering-mode work (system default vs. monochrome overlay, icon packs) landing in Phase 5 — that work now only needs to change one place instead of hunting down every duplicate.
- **`AppContextMenu`** (`ui/components/AppContextMenu.kt`, new) — reuses `ThemedDropdownMenu`/`ThemedDropdownMenuItem` (the same Surface-colored, M3-shaped popup Settings' dropdowns and the Drawer's overflow menu already use) rather than a bespoke `Popup`, with a header row (icon + label) as the first child. Rows: **App info** (`ACTION_APPLICATION_DETAILS_SETTINGS`), **Uninstall** (`ACTION_DELETE`, destructive-styled — the system handles its own confirmation), then a **Quick actions** section that only renders when the app publishes any shortcuts. Both system-settings intents fire directly via `LocalContext.current`, matching `ContactRow`'s existing chip-intent precedent from Milestone 5 rather than bubbling through a ViewModel — there's no business logic in "launch this already-fully-known intent."
- **New `data/AppShortcutRepository.kt`** wraps `LauncherApps.getShortcuts()`/`.hasShortcutHostPermission()`/`.startShortcut()`. Reading shortcuts requires Facet to be the active default launcher; when it isn't (e.g. during development, before the user sets it), `getShortcuts()` returns an empty list rather than throwing, so the menu just omits the section — never a broken/empty-but-visible one. Capped at 5, disabled shortcuts filtered out.
- **Long-press wiring**: `AppRow`/`DockIcon` (Home), `DrawerAppRow`/`DrawerGridTile` (Drawer, both List/Grid and their reuse inside search results) switched from `.clickable(onClick)` to `.combinedClickable(onClick, onLongClick)` — Compose's own long-press primitive (matches the platform default timeout, same lesson as `HomeDrawerRoute.kt`'s long-press sheet from earlier in this phase: no hand-rolled timers). Each row owns its own local `expanded` boolean and renders an `AppContextMenu` anchored to itself — kept local rather than hoisted, since a `Popup`'s anchor point has to be the specific row's own position, and per-row expand/collapse state is exactly the kind of pure UI state `CLAUDE.md` says composables can own directly (mirrors `ContactRow`'s existing local-`expanded` pattern).
- **Shortcuts fetch is a threaded suspend callback** (`onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>`), not precomputed UI state — fetched fresh only when a given row's menu actually opens (`LaunchedEffect(expanded, app)`), since eagerly querying shortcuts for every visible app on every app-list change would be a lot of wasted `LauncherApps` IPC calls for a rarely-opened menu. Implemented by `DrawerViewModel` (already the closest thing to a shared "drawer surface utilities" ViewModel after Milestone 5's contacts work) and wired identically into both `HomeScreen` and `AppDrawerScreen` from `HomeDrawerRoute.kt`.
- Test: `AppShortcutRepositoryTest` (5 tests — host-permission-denied returns empty, id/label mapping, disabled shortcuts filtered, capped at 5, a failed system query degrades to empty rather than throwing); `AppContextMenuTest` (new — header + core actions render when expanded, App info/Uninstall fire the correct `Intent` via Espresso-Intents, quick actions section omitted with no shortcuts and renders/fires `onLaunchShortcut` when present); `HomeScreenTest`/`AppDrawerScreenTest` additions (long-press on a favorite row / List row / Grid tile opens the menu).

### Real bugs found — Milestone 6
- **Every Robolectric test in this project had silently been running against a stale, cached API 23 SDK jar** — none of the existing Robolectric-based tests pin an SDK explicitly, and Robolectric's own default apparently isn't derived from this project's manifest (`minSdk 33`/`targetSdk 36`); it fell back to whatever was already cached on the machine. This had never surfaced before because nothing under test touched an API added after 23 — until `AppShortcutRepositoryTest` needed `LauncherApps.hasShortcutHostPermission()`/`getShortcuts()` (added API 25), which don't exist on that old jar's real `LauncherApps` class at all, so mocking them under `RobolectricTestRunner` failed with `NoSuchMethodError`. A first pass worked around this by dropping Robolectric for just that one test class (plain Mockito against the compileSdk stub classes, plus a `testOptions.unitTests.isReturnDefaultValues` Gradle flag to stop an unmocked `Process.myUserHandle()` call from throwing) — flagged directly as a workaround rather than the real fix, and reverted once the actual fix was in: a new `app/src/test/resources/robolectric.properties` pinning `sdk=33` (this app's real floor), which made Robolectric fetch the correct jar from Maven Central (network access confirmed available — this was a missing-config gap, not an environment restriction) and use it for every test in the project. Full unit suite re-verified green under the new jar (99/99, zero regressions) before treating this as done, per the user's explicit direction to test the real way rather than around the gap.
- **`LauncherApps.ShortcutQuery`'s fluent `setPackage(...).setQueryFlags(...)` chain briefly needed splitting into separate statements** while diagnosing the above — the AGP unit-test stub jar's stubbed setters don't return `this` the way the real device implementation does, so chaining NPE'd under the (temporary) non-Robolectric path. Reverted back to the natural chained form once Robolectric was fixed at the source, since real Android's fluent setters behave correctly and there was no longer a reason to avoid the idiomatic form.
- **Both gaps below fixed in a same-day follow-up** (see chat history):
  - **Favorites/Dock now collapse live on uninstall too, not just the Drawer's raw list.** `FavoriteAppRepository`/`DockAppRepository` switched from hydrating against `AppRepository`'s one-shot `getInstalledApps()` per subscription to the live `observeInstalledApps()` flow (the same one Milestone 2 built) — an uninstall via F12's menu now collapses Favorites/Dock immediately, no restart needed.
  - **Uninstalling now permanently deletes the Favorites/Dock row too, not just hides it at read time.** Direct follow-up question — the live collapse above only *filtered* an orphaned row out of what's displayed; the underlying `FavoriteAppEntity`/`DockAppEntity` row stayed in Room forever, silently reappearing at its old slot/position if the same app was ever reinstalled. Per explicit direction ("otherwise the user cannot modify the list properly"), fixed with real deletion instead: new `AppRepository.observeUninstalledPackages(): Flow<String>` — a *second*, independent `LauncherApps.Callback` registration emitting only on a genuine `onPackageRemoved`, deliberately not reused from `observeInstalledApps()`'s own callback, since that one also re-fires for reasons an app might only be *momentarily* missing (`onPackagesUnavailable` mid-update) that must never trigger a permanent delete. New `domain/CleanUpUninstalledAppsUseCase.kt` (this repo's first "runs forever reacting to events" use case, distinct from every other use case's one-shot/`Flow`-returning shape) collects it and calls new `DockAppRepository.removeByPackage()`/`FavoriteAppRepository.removeByPackage()` (the latter deletes across *every* facet in one query, not just the active one) — launched once, for the app process's whole lifetime, from `LauncherViewModel.init`. New `FavoriteAppDao.deleteByPackage`/`DockAppDao.deleteByPackage` queries match on package alone (not full component), since once a package is gone every one of its activities is too.
  - **Quick-action rows now show their own icon.** `ThemedDropdownMenuItem` gained an optional `leadingIcon` slot (threads straight to M3 `DropdownMenuItem`'s own); `AppShortcutRepository.getShortcuts()` resolves each shortcut's icon via `LauncherApps.getShortcutIconDrawable()` (same eager-bitmap-conversion pattern as `AppRepository.getInstalledApps()`), rendered through the shared `AppIcon`. App info/Uninstall also gained default `Icons.Default.Info`/`Icons.Default.Delete` leading icons per direct feedback, even though that wasn't part of the original gap.
  - Test: `FavoriteAppRepositoryTest`/`DockAppRepositoryTest` — one case each proving the live *filter* propagates without resubscribing (`MutableStateFlow` stub + `Channel`-collected emissions, mirroring `AppRepositoryTest`'s own live-flow test); new `CleanUpUninstalledAppsUseCaseTest` proving the use case actually calls both repositories' `removeByPackage` for the exact uninstalled package (`MutableSharedFlow(replay = 1)` stub, to sidestep a subscribe-before-emit race against the launched collector).

---

## ✅ Phase 5 complete — Theming & Appearance (F11 + dark mode)

*(added — see chat history: user-facing theming, distinct from the "theme every Material3 component explicitly" engineering convention already in `CLAUDE.md`, wasn't previously scheduled anywhere. A full dark-mode token set and Accent-handling spec already exist in the design handoff — `README.md`'s "Dark (`Launcher Dark.dc.html`)" table and `design_handoff_minimal_launcher/screenshots/dark-*.png` — just never implemented in code; only `FacetLightColorScheme` exists in `Theme.kt` today.)*

- [x] **Dark color scheme**: add `FacetDarkColorScheme` to `ui/theme/Theme.kt` using README's exact dark tokens (`Wallpaper #14171d`, `Surface #171a21`, `Ink #e7eaf0`, `Muted rgba(226,232,240,.5)`, `Hairline rgba(226,232,240,.07)`, `IconTile #39424f`, `Error #f2857f`, `Success #7fd493`). `FacetLauncherTheme` selects light vs. dark via `isSystemInDarkTheme()` — follows the system setting automatically; neither the PRD nor README specs an in-app override toggle, so none is planned unless that changes. Every color currently a bare `Color(...)` constant in `Color.kt` needs a light/dark pair resolved through the active scheme, not a hardcoded value — touches every screen. `@Preview`s get their required dark variant (`CLAUDE.md`'s "light + dark where the design spec defines both" — previously unenforceable with only one scheme).
  - Test: a unit test on the scheme-selection function (given dark mode on/off, resolved token values match the light/dark table); instrumented smoke test that a representative screen renders without crashing under `isSystemInDarkTheme() = true`.
- [x] **Accent color**: README's "Accent handling" spec — one variable driving ~100 usages, default source is Material You (`dynamicLightColorScheme`/`dynamicDarkColorScheme`, reading `system_accent1_*`; always available since min SDK 33 ≥ API 31, no fallback path needed). Un-disables Settings' existing **Accent from system** row; turning it off surfaces a fixed-color swatch picker, persisted (new `LauncherSettings.accentFromSystem: Boolean`, `customAccentColor: Int?` + matching `SettingsRepository` key/setter).
  - Test: `SettingsRepositoryTest` round-trip for the two new fields; a unit test that the resolved `primary` color is the custom value when `accentFromSystem = false`, and the dynamic-scheme value otherwise; `SettingsScreenTest` that toggling the row off reveals the swatch picker.

**Icon rendering mode (F11)** was originally scoped into this phase — moved to Phase 7, see below (relocated, not dropped).

## ✅ Phase 6 complete — Notification badges (F13)

- [x] `NotificationListenerService` integration, badges on favorites list and dock. **Scope revised mid-build, see chat history and the updated `PRD.md`/`README.md`:** badge style is a user choice (Dot / Count, capped `9+`), not the originally-decided dot-only indicator, and the setting lives on its own dedicated Notification Settings page rather than an inline Settings toggle.
  - Test: `NotificationBadgeRepositoryTest` (silent notifications excluded from counts; non-silent grouped/counted per package; a new snapshot replaces the previous one wholesale rather than accumulating; an empty list clears all counts). `NotificationSettingsScreenTest` (turning badges on without access granted routes to the explanation screen instead of persisting).

### What got built
- **Real data layer**: `data/model/NotificationInfo.kt`, `data/NotificationBadgeRepository.kt` (in-memory `StateFlow<Map<packageName, count>>`, recomputed wholesale on every listener callback — nothing persisted, nor should it be, since `NotificationListenerService.activeNotifications` is the correct source of truth), `data/NotificationAccessRepository.kt` (`NotificationManagerCompat.getEnabledListenerPackages().contains(packageName)` — same special-access-permission shape as `UsageAccessRepository`, no grant-change callback so callers re-check on resume), `data/FacetNotificationListenerService.kt` (`@AndroidEntryPoint`, refreshes the repository on `onListenerConnected`/`onNotificationPosted`/`onNotificationRemoved`).
- **Correct importance check, not the obvious-looking wrong one**: silence is determined via `NotificationListenerService.currentRanking`/`Ranking.getImportance()`, not `NotificationManager.getNotificationChannel()` — the latter only resolves channels belonging to the *calling* app, not other apps' channels, so it would have silently shown badges for every app regardless of their actual channel importance. Group-summary notifications (`FLAG_GROUP_SUMMARY`) are filtered out to avoid double-counting a conversation.
- **Manifest**: `<service>` declaration uses `android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"` on the `<service>` tag itself (system-enforced binding permission) — not a top-level `<uses-permission>`, which would be the wrong mechanism for this permission.
- **`NotificationAccessExplanationScreen`/`ViewModel`** — near-identical to `UsageAccessExplanationScreen`'s established pattern: redirects to `ACTION_NOTIFICATION_LISTENER_SETTINGS`, auto-dismisses via `LaunchedEffect(isGranted)` once granted, and persists `notificationDotsEnabled = true` the moment access is confirmed.
- **Dedicated Notification Settings page**: new `NotificationSettingsScreen`/`ViewModel`, reached via a `ClickableRow` (Settings' old inline toggle removed) whose subtitle reads `On · Dot` / `On · Count` / `Off`. Turning the switch on while access isn't granted routes to the explanation screen instead of writing the setting directly; a `Badge style` `LabeledDropdownRow` (Dot/Count) sits below it, disabled while the feature is off.
- **`AppIcon`/`NotificationBadge`** (`ui/components/AppIcon.kt`) — one `Badge()`-based composable serves both styles: no `content` lambda renders a plain M3 dot, `Badge(content = { Text(...) })` renders the numeric pill; `badgeLabel(count)` caps the label at `9+`.
- **Threaded end-to-end as real (not demo) data**: `ObserveHomeScreenStateUseCase` gained `notificationBadgeRepository`/`notificationAccessRepository`, combining a gated `badgeCounts` flow (empty unless the setting is on *and* access is granted) into `HomeScreenState`; `HomeUiState`/`HomeViewModel` and a mirrored `DrawerViewModel.badgeCounts` carry it to `HomeDrawerRoute.kt`, which passes real per-package counts into both `HomeScreen` (favorites list + dock) and `AppDrawerScreen` (List rows, Grid tiles, and their reuse inside search results). The earlier hardcoded `demoBadgeCount()` preview function used to validate the visual design (dot vs. numbered badge, confirmed by the user via Android Studio's own `@Preview` pane) was fully removed from both files once the real listener path replaced it.
- **Known gap, see updated `README.md`**: the `2px` wallpaper-coloured separator ring around the dock badge (`README.md` item 6) isn't implemented — `BadgedBox`/`Badge` render the badge with no ring today. Worth a follow-up if the plain badge reads poorly against a bright dock icon in practice.
- **Follow-up: Dock's Text display mode was missing badges entirely.** `DockIcon`'s `ICONS` branch anchored the badge to the icon corner via `AppIcon`, but the `TEXT` branch (added later, once Dock gained a Display style dropdown) just rendered a bare `Text` with no badge slot at all — a real regression path (switch Dock to Text mode and every notification indicator silently disappears), not the spec'd ring gap above. Fixed by wrapping the label in a `Row` with the same trailing-the-name badge AppRow already uses for the favorites list. Test: `HomeScreenTest.dockTextModeStillShowsANotificationBadgeTrailingTheLabel`.

### Real bugs found and fixed during this pass
- **The original hardcoded demo badged almost every app on the favorites list**, reported directly on-device ("i can see badges for alomst all my apps. it should show badges/dots only if there are any notifications in the notification shade") — expected, since `demoBadgeCount()` was a deliberate placeholder keyed off nothing real, built specifically so the user could validate the dot-vs-count visual before the real listener integration existed. Resolved by the real `NotificationListenerService` wiring above, which only ever reflects actual, non-silent, currently-active notifications.
- Threading the two new repositories into `ObserveHomeScreenStateUseCase`'s and `DrawerViewModel`'s constructors broke three test-source files that construct those classes directly rather than through Hilt (`ObserveHomeScreenStateUseCaseTest`, `HomeViewModelTest`, `HomeDrawerRouteTest`) — 7 compile errors, all "no value passed for parameter." Not a production bug; fixed by supplying mocked/real instances matching each file's own existing pattern (Mockito mocks in the unit test, real repository instances in the instrumented test).

### Full-suite verification pass (post-Phase-6)
`./gradlew test` (126 JVM unit tests, including a new `NotificationBadgeRepositoryTest` covering silent-filtering/per-package counting/wholesale-replace/empty-clears) is fully green. `connectedAndroidTest` (116 instrumented tests) is fully green.

**One real, previously-unexercised bug found and fixed**: `FavoritesPickerScreenTest.withNoFacetIdTheScreenEditsTheLauncherWideDefaultList` (added alongside the Default-favorites feature but — per that section's own write-up — never actually run end-to-end on-device before this pass) called `defaultFavoriteAppRepository.observeDefaultFavorites().first()` via a bare `runBlocking` directly on the instrumentation test's own thread. That flow chains into `AppRepository.observeInstalledApps()`'s live `LauncherApps.registerCallback`, which needs a Looper-bound thread — fine when collected via a ViewModel's own `viewModelScope` (Main), but the test's raw thread never calls `Looper.prepare()`, crashing with `RuntimeException: Can't create handler inside thread ... that has not called Looper.prepare()`. Fixed by asserting the persisted state through the UI (`assertIsOn()` on the checked row) instead, matching this file's own established pattern elsewhere — not by touching the repository from the test thread at all.

**One test-only flake fixed** (same async-I/O-vs-idling gap already documented elsewhere in this file): `NotificationSettingsScreenTest.turningBadgesOffDisablesTheStyleRowAndPersists` asserted the toggle's UI state immediately after `performClick()`, but the underlying write is a real DataStore round-trip the uiState `combine()` has to re-observe — fixed by polling (`waitUntil`) instead of asserting once, matching the fix already used for the identical `LabeledDropdownRow`/`Popup`-timing and `StateFlow`-population traps recorded earlier in this document.

**`CalendarSettingsScreenGrantedTest`/`PermissionsScreenGrantedTest` self-kill-race eliminated at the root, not worked around**: both classes previously used a real `GrantPermissionRule` + a `@After` calling `UiAutomation.revokeRuntimePermission()` on this app's own package to reset `READ_CALENDAR` before the next test class ran. Revoking a permission a live process holds always kills that process (mandatory Android platform behavior); logcat traces confirmed the resulting failure was a `SecurityException: Calling from not trusted UID!` inside `UiAutomationConnection.shutdown()` immediately followed by a `SIGKILL`, racing the instrumentation runner's own "test finished" status report back to Orchestrator — the self-kill could land first, misreporting a fully-passing test as "instrumentation process crashed." Confirmed via isolated single-class/single-method re-runs (before and after a full cold emulator relaunch) that no test assertion ever actually failed — only this post-assertion teardown race (`ActivityManager: Killing ... permissions revoked`, logged by the system itself, only ever appears *after* the test body's own assertions have already run). A deferred-revoke timing fix (background thread + delay) was tried first and reverted — it reduced but didn't eliminate the race, and introduced a worse correctness bug (the *next* class's ungranted-state tests could start before the delayed revoke landed). **Root fix**: `CalendarPermissionRepository` (`data/CalendarPermissionRepository.kt`) is now `open`/`isGranted()` is `open`, and a new shared `ui/settings/FakeCalendarPermissionRepository.kt` (`internal`, used by all four test classes in this package) returns a fixed `true`/`false` — mirroring `CalendarRepository`'s existing `FakeCalendarRepository` pattern. No real OS permission is ever granted or revoked for these tests anymore, so there's no process to kill and nothing to race. `GrantPermissionRule`/`@After`-revoke removed entirely from both `...GrantedTest` classes.

## ✅ Phase 7 mostly complete — plan updated to match actual code state (see chat history: this section had drifted well behind what was actually built)

- [x] `AppWidgetHost` integration: add/bind/configure flow, reposition (with displacement, not just reject-on-overlap), remove (`deleteAppWidgetId`), orphan detection. `data/widget/AppWidgetRepository.kt`/`LauncherAppWidgetHost.kt` wrap every `AppWidgetManager`/`AppWidgetHost` call; `domain/ResolveWidgetDropUseCase.kt` displaces overlapping widgets (right, then down) rather than rejecting the drop outright; `domain/ResolveWidgetResizeUseCase.kt` does the equivalent cascade-push-down for a growing resize.
  - Test: `AppWidgetRepositoryTest`, `ResolveWidgetDropUseCaseTest`, `ResolveWidgetResizeUseCaseTest`, `HubGestureTest` (real on-device drag/resize/move gestures).
  - ~~**Resize doesn't reject going below a provider's declared `minResizeWidth`/`minResizeHeight`**~~ — **closed, deliberate design decision (not a gap):** the Hub grid already snaps every resize to whole grid cells, and a provider's declared minimum is itself just a hint most providers set conservatively — enforcing it would mean rejecting cell-aligned sizes that render fine in practice, for a benefit (preventing a maybe-cramped widget layout) that's the user's own call to make, not the launcher's to block. Leaving it unenforced matches how most third-party launchers behave here.
  - **No dedicated test proves placement survives a real process death** — the underlying mechanism (Room-backed `WidgetPlacementEntity` + system-owned `AppWidgetHost` ids, both untouched by the app's own process dying) should survive by construction, but this is unverified, not confirmed.
- [x] Hub grid sizing: 20-widget cap (`HUB_MAX_WIDGETS`) and 50-row cap (`HUB_MAX_ROWS`) are enforced (`PlaceWidgetUseCase`), with a real at-capacity UI state (`HubUiState.isAtCapacity`, `HubAtCapacityStrip.kt`).
  - Test: `PlaceWidgetUseCaseTest`, `ObserveHubStateUseCaseTest`, `HubScreenTest`.
  - ~~**Column count not synced to the Drawer's grid-size setting**~~ — **closed, deliberate design decision (not a gap):** the Hub's fixed `HUB_COLUMNS = 5` (`domain/HubGridConstants.kt`) is intentionally independent of the App Drawer's grid-size setting. The two grids hold fundamentally different content at different densities — app icons (small, uniform, meant to be dense) versus widgets (large, variable colSpan/rowSpan, meant to breathe) — so coupling them would mean a user's icon-density preference silently reflowing every widget's layout, which reads as a bug, not a feature. This plan's original "sync to Drawer" line was carried over from before Hub's actual widget-grid design was worked out; superseded by this decision.
- [x] Orphaned-widget handling (provider uninstalled/updated) — `OrphanedWidgetTile.kt`'s Remove/Keep space rows are real, wired to `HubViewModel.onRemoveOrphan` → `DeleteWidgetUseCase`; a widget's `getAppWidgetInfo() == null` (provider gone) is what flips a tile into this state.
  - Test: `HubScreenTest` (orphan rendering + removal).
- [x] *(moved from Phase 5, see chat history — relocated, not part of this phase's original F5 scope)* **Icon rendering mode** (F11): System default vs. Monochrome overlay, applied globally (not per-app) — un-disables Settings' existing **Icons** row. Reads `Icon.getMonochrome()` (always available, API 33+) where an app provides one; for apps that don't, a real bitmap transform tints the icon with the current accent as a themed overlay. New `IconRenderMode` enum + `LauncherSettings.iconRenderMode` field, plumbed into `AppRepository`'s icon loading (or a new decorator layer over it) since `AppInfo.icon` is currently the raw `ActivityInfo` icon with no transform step.
  - Test (check if done): unit test that monochrome mode tints a fake bitmap deterministically with a given accent color; instrumented test that switching icon mode in Settings changes rendered icons across Home/Favorites/Dock/Drawer.
- [ ] ** **Return to Home Navigation** : Implementation done to return to home screen from any screen in launcher on receiving home button/gesture input. not yet tested.
  - Test: To be determined.

## ✅ Phase 8, F14 Backup & Restore + First-run onboarding complete — accessibility pass still open (see below)

- [x] F14 Backup & Restore: full-settings JSON export/import, guided widget re-add on import.
  - **Export** (`domain/ExportBackupUseCase.kt`) composes every settings/data `Repository` (`SettingsRepository`, `FacetRepository`, `FavoriteAppRepository`, `DockAppRepository`, `DefaultFavoriteAppRepository`, `WidgetPlacementRepository`) into one `BackupBundle` (`data/model/BackupBundle.kt`), written as JSON (kotlinx.serialization) to a user-picked destination via `BackupRepository.kt` (`ContentResolver`/SAF `CreateDocument`, never a guessed path).
  - **Import** (`domain/ImportBackupUseCase.kt`) is a destructive full-replace (confirmed with the user first, `ui/settings/backup/BackupRestoreScreen.kt`'s `ConfirmDialog`) — wipes and re-inserts every facet (with its own favorites), the dock, and default favorites; every setting field is re-applied through `SettingsRepository`'s existing per-field setters (no new bulk setter added). Rejects a backup from a newer app version (`backupVersion > CURRENT_BACKUP_VERSION`) instead of silently dropping fields it doesn't understand.
  - **Deliberately excluded from the snapshot** (see `BackupBundle.kt`'s own doc comment): `activeFacetId` (replaced with an index into the exported facet list, resolved to the new facet's real id on import), `selectedCalendarIds`/`calendarColors` (device/account-specific `ContentProvider` row ids, not portable), `calendarPermissionRequested`/`contactsPermissionRequested` (this install's own runtime-permission-history flags, not a user preference).
  - **Widget re-binding, per the PRD's own flagged constraint** ("`AppWidgetHost` ids aren't portable across installs or devices"): a backup records each widget's provider component + grid position/span (`BackupWidgetPlacement`), never its old `appWidgetId`. Import never writes these to `widget_placements` directly — they surface as a guided re-add list on the same screen (not a separate `NavHost` destination — carrying them across a nav-argument boundary had no clean answer, mirroring `HomeDrawerRoute`'s own reasoning for keeping its widget picker an in-place overlay). Each row goes through the real allocate/bind/configure flow (mirroring `HubWidgetPickerViewModel`), landing back at its originally-recorded position when that cell is still free (`PlaceWidgetUseCase` gained an optional `preferredRow`/`preferredCol`, tried before falling back to first-fit) — a provider no longer installed shows as unavailable rather than a dead retry button.
  - Test: `ExportBackupUseCaseTest`, `ImportBackupUseCaseTest`, `BackupRepositoryTest` (real file:// round-trip), `PlaceWidgetUseCaseTest` (new preferred-position cases), `BackupRestoreViewModelTest` (the re-add queue's allocate/bind/configure/place orchestration), `BackupRestoreScreenTest` (chrome + the destructive-import confirmation).
  - **Real bugs found and fixed while building this:**
    - `androidx.navigation:navigation-common:2.9.6`'s module metadata triggers AGP's automatic main/androidTest consistent-resolution to lock `kotlinx-serialization` at 1.7.3, but `androidx.room:room-testing:2.8.4` (added for `FacetDatabaseMigrationTest`'s `MigrationTestHelper`) needs 1.8.1 — its precompiled schema-bundle serializer classes call a `GeneratedSerializer` method 1.7.3 doesn't implement, crashing with `AbstractMethodError`. First worked around with a project-wide `resolutionStrategy.force(...)`; once this feature added `kotlinx-serialization-json:1.8.1` as a real `implementation` dependency, the main classpath itself requests 1.8.1, so the force was confirmed redundant (re-verified via `FacetDatabaseMigrationTest` passing without it) and removed — the version pin now lives in one place, the dependency declaration itself.
    - Mockito's static `eq()`/`any()` matchers return a raw `null` placeholder that Kotlin's compiler-inserted non-null parameter checks reject outright when stubbing/verifying a `suspend fun` with non-null reference parameters (`BackupRepository.writeBackup`, `FacetRepository.restoreFacet`) — worked around by having `ExportBackupUseCase` return the bundle it wrote (so the test asserts on a real returned value instead of an `ArgumentCaptor`) and by stubbing with concrete expected values instead of `any(...)`.
    - `HomeDrawerRouteTest.kt` had the identical `widgetPickerViewModel` default-Hilt-param gap already found and fixed in `KeyboardDismissalTest.kt` earlier this session (`HomeDrawerRoute`'s `widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel()` never overridden) — the whole test class was silently crashing on every run; fixed the same way (a manually-constructed `HubWidgetPickerViewModel` passed in explicitly). **Fixing it surfaced 5 real, previously-masked failures** (`swipingUpPastHalfwayCommitsTheDrawerOpen`, `releasingBeforeHalfwaySpringsBackClosed`, `systemBackClosesAnOpenDrawer`, `swipingDownAtDrawerTopReturnsToHome`, `lowVelocitySwipePastTwentyPercentStillClosesTheDrawer` — all assert Home's "FAVORITES" heading exists at some point, but this test's `homeViewModel` never seeds any favorite apps, and that heading only renders when non-empty) — flagged as a separate follow-up task, not fixed here (out of scope for this feature).
    - `HubHeader.kt` accepted a `columns: Int` parameter but never actually rendered it — `HubScreenTest.headerAlwaysShowsTheTitleAndCurrentCount` expected a "· N columns" suffix that had never been implemented. Decided (direct correction, see chat history) to drop the expectation rather than add the text: fixed the test to match the header's real, intended copy (`"$widgetCount of $HUB_MAX_WIDGETS widgets"`, no column count) instead of adding column text to the UI.
    - `HubScreenTest.removingAnOrphanedWidgetDeletesItsPlacement` times out waiting for the orphaned tile to disappear after tapping Remove — confirmed real (not flaky) across two isolated runs on a healthy emulator. Not investigated further; flagged as a separate follow-up task (out of scope for this feature).
- [x] First-run flow (`4f`–`4h`, extended to a 4-screen flow — see `ONBOARDING_FLOW.md` and the design
  canvas `Launcher.dc.html` turn 5): intro → home setup (dock + favorites) → facets teaser →
  set-as-default, permissions deferred to just-in-time.
  - **Data**: `LauncherSettings` gained `onboardingCompleted`/`defaultsSeeded`/`coachMarksSeen`
    (install-local, backup-excluded — see `BackupBundle.kt`'s own doc comment) via
    `SettingsRepository`'s usual key/getter/setter triple. `domain/SeedDefaultDockUseCase.kt` pre-fills
    the shared dock with this device's own resolved default browser/messaging/camera/mail/phone apps,
    called unconditionally from `LauncherViewModel.init` (not gated on the onboarding UI actually
    being shown), so a killed/skipped onboarding still leaves Home populated.
  - **Default-launcher request**: `DefaultLauncherRepository` (already existed, with only
    `isDefaultLauncher()`) gained `requestDefaultLauncherIntent()` — `RoleManager.ROLE_HOME`'s
    in-place system dialog when available and unheld, falling back to the exact
    `ACTION_MANAGE_DEFAULT_APPS_SETTINGS` intent the Settings screen's own row already used; that row
    now shares this method instead of duplicating the fallback.
  - **UI** (`ui/onboarding/`): `OnboardingScreen.kt` (stateless host, `rememberSaveable` step,
    `AnimatedContent` slide/fade with `HomeDrawerRoute`'s own 340ms `CubicBezierEasing(.32,.72,0,1)`,
    system-back per step) + `OnboardingViewModel`/`OnboardingUiState` (always the global/default
    repositories — no `facetId` concept at all) + four step composables. The intro step's Home
    diagram is a real mini "9:41" clock card with leader lines to Clock/Your apps/Dock labels,
    matching the design canvas artboard `5b` pixel-for-pixel (not a placeholder text list — an
    earlier pass under-built this and was corrected against the actual mock).
  - **Home-setup step redesigned after initial ship** (direct feedback): the original single search
    list toggled between Favorites/Dock via `OnboardingPickTarget` was replaced entirely. HOME APPS
    now comes first, DOCK second (40dp gap between, up from 28dp — also direct feedback), and both
    sections mirror their Settings counterparts instead of a bespoke onboarding-only picker: HOME
    APPS gets a `ListContentMode` dropdown (Favorites/Recently used/Most used, `LabeledDropdownRow`,
    same enum/repository call `HomeAppsListSettingsScreen` already used) and, in Favorites mode, a
    "Favorites" row; DOCK gets a "Manage dock apps" row (no more inline `+`). Both rows open the
    *actual* `FavoritesPickerScreen`/`DockAppPickerScreen` full-screen (reused unmodified — own
    search, own Hilt ViewModel) as a `subScreen` overlay inside `OnboardingScreen`, then return here;
    once apps are picked, both sections render a live drag-to-reorder list/row
    (`DragReorderState`, same headless mechanics `DockSettingsScreen`/`HomeAppsListSettingsScreen`
    use, each keeping its own row-rendering copy per established precedent). `OnboardingViewModel`
    correspondingly shrank to reorder + content-mode delegation only (`reorderDockApps`,
    `reorderFavorites`, `setListContentMode`) — no more `getInstalledApps`/search/pick-target state,
    since picking now happens entirely inside the reused picker screens. `OnboardingScreen` exposes
    `dockPickerViewModel`/`favoritesPickerViewModel` as nullable testing-seam params (always `null`
    in production) so `OnboardingScreenTest`'s non-Hilt compose host can supply hand-built instances
    the same way it already does for `OnboardingViewModel`.
  - **Facets teaser redesigned twice**: shipped first with three `HomeSurfacePreview` cards (center
    = the user's real step-2 picks, two empty neighbors) since a fresh install only has one real
    facet — honest but static. Replaced per direct feedback with a fabricated, animated mockup:
    `OnboardingFacetsPage.kt`'s `FacetSwitchDemo` cycles three invented `MockFacet` cards
    (own clock/date/favorites/dock, no real products — same call the original design brief made)
    through a fully automatic, looping demo of the actual gesture — focus → zoom out → swipe → tap-
    pulse "select" → zoom in → hold → **the same sequence in reverse** (zoom out → swipe back →
    pulse → zoom in) rather than jump-cutting back to the start, so the loop always animates
    continuously in both directions.
  - **Coach marks**: `ui/components/GestureHintOverlay.kt` (one-time, over Home right after
    onboarding — dismisses on tap, "Got it", or the first frame of any drag, so a swipe attempt just
    clears the hint rather than also completing the real navigation underneath) and
    `ui/components/FirstRunCallout.kt` (dismissible inline card, first carousel open). Both read/write
    `coachMarksSeen` directly as computed properties on `HomeUiState`/`FacetCarouselUiState` — no
    extra ViewModel state needed since the persisted set already does the job.
  - **Gesture map correction folded in**: mid-build, the user flagged that the onboarding docs/mocks
    taught facet-switching via swipe-left, while `PRD.md` §5 still described the older empty-space
    long-press sheet. Traced to a real, already-shipped decision (`HomeDrawerRoute`'s `facetAxis`)
    that had never been backported into the PRD — fixed `PRD.md` §5 (and F4/F9) to match reality
    before writing any onboarding code, so the intro screen's gesture list and the coach mark teach
    the gestures that actually work.
  - **Dock-is-no-longer-purely-shared correction** (post-review): the dock gained per-facet
    overrides in a change that landed concurrently with this build (see "Dock gains per-facet
    overrides" above) — onboarding still correctly seeds/edits the launcher-wide *default*
    `DockAppRepository` (unchanged, still what a fresh facet inherits), but its UI copy claiming
    the dock is "shared by every facet" was no longer accurate once a facet can override its own.
    Fixed the home-setup step's hint and the facets-teaser body copy to describe it as the default
    every facet starts from rather than an immutable shared resource.
  - Test: `SettingsRepositoryTest`, `SeedDefaultDockUseCaseTest`, `OnboardingViewModelTest` (starts
    from live favorites/dock, reflects `listContentMode`, `setListContentMode`/`reorderDockApps`/
    `reorderFavorites` delegate correctly — add/remove/search coverage now lives entirely in
    `DockAppPickerViewModel`'s/`FavoritesPickerViewModel`'s own existing tests, since onboarding
    reuses those screens wholesale rather than re-implementing picking), `DefaultLauncherRepositoryTest`
    (Robolectric `ShadowRoleManager`), `HomeUiStateTest`/`HomeViewModelTest`
    (`showGestureHint`/`dismissGestureHint`), `FacetCarouselUiStateTest`/`FacetCarouselViewModelTest`
    (`showIntroCallout`/`onIntroDismissed`) — all unit-level, all green. `OnboardingScreenTest`
    (instrumented): full step navigation, Skip, tapping Favorites/Manage dock apps opens the real
    full-screen pickers and Done/back returns, Later, system back, and **`completing onboarding
    requests no runtime permission`** (Espresso-Intents, `times(0)` on
    `REQUEST_PERMISSIONS`/`ACTION_USAGE_ACCESS_SETTINGS`) — satisfies this task's original test
    requirement; builds `DockAppPickerViewModel`/`FavoritesPickerViewModel` by hand against the same
    in-memory repositories as `OnboardingViewModel` (mirrors `DockAppPickerScreenTest`'s/
    `FavoritesPickerScreenTest`'s own non-Hilt pattern), passed via the new testing-seam params.
    `HomeDrawerRouteTest`/`FacetCarouselScreenTest` gained coach-mark show/dismiss cases; every
    *pre-existing* test in the latter now defaults to "already seen" so the new callout doesn't
    change their established assumptions.
  - **Real bugs found and fixed while building this:**
    - A `StateFlow` built via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), ...)`
      needs an active collector to start its upstream — `backgroundScope.launch { uiState.collect {} }`
      alone wasn't enough in instrumented/unit tests alike, because that launch runs on the test's own
      internal scheduler, a *different* one from the `StandardTestDispatcher` wired via
      `Dispatchers.setMain(...)`. `runCurrent()` (on the test's own scope) right after the launch
      closes the gap; `testDispatcher.scheduler.advanceUntilIdle()` alone silently never ran it,
      producing tests that "passed" while asserting against the never-updated default state.
    - `GestureHintOverlay`'s own internal `.testTag("gesture_hint_overlay")` was being shadowed by a
      second, redundant `.testTag(...)` applied to the `modifier` parameter at its call site in
      `HomeDrawerRoute` — two `testTag()` calls on the same node's modifier chain silently resolve to
      the *outer* one, not the inner one closest to the actual element, so the node was findable only
      by the (wrong, unused-by-any-test) outer tag. Removing the redundant call site tag fixed it.
    - A second Claude Code session was concurrently editing unrelated files (`SettingsViewModel`'s
      calendar-count feature, Room schema migrations, a `FacetCarouselScreen` subtitle string) in
      this same working tree throughout the build, twice leaving the module transiently
      non-compiling and once leaving one pre-existing, unrelated `FacetCarouselScreenTest` case
      failing on a text assertion its own author hadn't updated yet — not fixed here (not this
      feature's code), each resolved on its own once that session's edits settled.
    - `swipingUpStartingOnAnAppIconStillOpensTheDrawer` (`HomeDrawerRouteTest`, pre-existing, not
      touched by this feature) intermittently finds two `home_app_icon_com.example.A` nodes instead
      of one — reproduced twice, not investigated further, flagged as a separate follow-up.
    - Home-setup redesign: the dock tile row originally kept its old tap-to-remove alongside the new
      drag-to-reorder on the same tile — both gestures competing for the same `pointerInput` region
      is a real conflict (a stationary tap can register as a zero-distance drag). Resolved by
      dropping tap-to-remove entirely in favor of matching Settings' own `DockSettingsScreen`
      exactly: the tile row is drag-only, add/remove happens by unchecking in the full-screen picker.
    - While iterating on `OnboardingHomeSetupPage.kt`, a background instrumented test run
      (`connectedDebugAndroidTest`) was mistaken for idle emulator time and manually poked
      (`pm clear`/`am start`/screenshots) mid-suite, crashing the test instrumentation process for a
      handful of unrelated tests (`DockAppPickerScreenTest`, two `AppDrawerScreenTest` cases) with
      "Test instrumentation process crashed" — confirmed environmental, not a real regression, by
      re-running those classes alone afterward (all green). Moved manual on-device verification to
      the connected physical phone for the rest of this pass once the collision was caught, per the
      user's own direction.
- [ ] Accessibility pass: TalkBack labels on icon-only UI (dock, favorites, alphabet rail), layout survives large system font scale.
  - Test: semantic content-description assertions on dock/favorites/rail; a layout test at 200% font scale doesn't clip/overlap the alphabet rail (per README's noted open question — may motivate a condensed-rail fallback here).

## ✅ Phase 9 complete (plan was unticked but the work landed — see chat history)

*(added — see chat history: F6's contact chips ship in Phase 4/Milestone 5 as a fixed Call/Message/WhatsApp-if-installed list, deliberately scoped that way for now. This phase is the follow-up to make that list dynamic, sourced the way the stock Contacts/Dialer app does it, rather than hand-enumerating one app at a time.)*

- [x] **Discover third-party connections from the Contacts Provider.** `ContactRepository.dynamicConnections()` queries `ContactsContract.Data`, filters out the standard kinds already handled (`STANDARD_MIMETYPES`), and resolves each remaining row's owning package via `AccountManager.getAuthenticatorTypes()` — dropped entirely if the resolved activity requires a permission this app doesn't hold (a real bug found live via device logcat: Google Meet's row resolved but needed `CALL_PHONE`, which would have failed silently).
  - Test: `ContactRepositoryTest`.
- [x] **Icon/label resolution — deliberately *not* the `contacts.xml` path, by design.** The plan's original per-mimetype `contacts.xml`/`PackageManager.getResourcesForApplication(...)` approach was never built — see the next bullet, this *is* that decision, made from the start rather than after trying the fragile path first: each dynamic row's icon/label are the owning app's own launcher icon/label (resolved once via `AccountManager`), which is simpler and doesn't depend on OEM Contacts Provider quirks. Documented directly in `ContactRepository.kt`'s own doc comment.
- [x] **Tap-to-open bottom sheet** (`ContactConnectionsSheet.kt`) replaces the old inline chip row — starts at 30% of screen height, grows to fit content up to a 70% cap, scrolls internally past that. Every connection (Call, Message, Email, WhatsApp — tagged by `packageName == "com.whatsapp"` on an already-discovered dynamic row, not a separate hardcoded branch — plus whatever else discovery surfaces) sorts by a fixed `ContactConnectionType` order via `compareBy { it.type.ordinal }`. A number that collapses to more than one destination (e.g. two phone numbers) opens a disambiguation page inside the same sheet, with its own back-handling (`BackHandler` returns to the main list rather than falling through to the launcher).
  - Test: `ContactConnectionsSheetTest`, `AppDrawerScreenTest.tappingAContactRowOpensTheConnectionsSheetForThem`.
- [x] Fallback-behavior decision resolved (see the icon/label bullet above) — not deferred to a later revisit as originally planned; chosen and shipped from the start once the `contacts.xml` path was judged too OEM-fragile up front.

---

## Phase 10 — Advanced Clock Templates (F1 follow-up)

- [x] Implement advanced templates with fixed fonts and variable axes (Roboto Flex Wide, Narrow, Tech Distorted).
- [ ] Add "Pro" vs "Basic" version indicators (icons/labels) in the Clock Style Gallery for advanced templates.
- [ ] Evaluate meridiem display for advanced templates (currently implemented with fixed fonts).

---

## Post-Milestone-6 polish — type scale, live clock, calendar colors, Default favorites, reorder UX

Direct follow-up work spanning several separate requests. `assembleDebug`, `test` (122 JVM unit tests) green; `connectedAndroidTest` compiles clean throughout but wasn't re-run end-to-end on-device this pass (shared AVD was repeatedly unstable — see environment notes). Manual on-device walkthrough on the physical test phone confirmed the items below, including both real bugs.

### Type scale, insets, live clock
- **Full M3 typography migration**: `FacetTypography` (`ui/theme/Type.kt`) now built from Material3's own real `Typography()` defaults via `.copy(fontFamily = FontFamily.SansSerif)` per role, not hand-picked sizes — every `FacetType.X` reference across ~19 files replaced with `MaterialTheme.typography.<role>`. `FacetType` now holds only `clock` (the one deliberately custom style).
- **Edge-to-edge inset fixes**: every full-screen composable (App Drawer, Home, Settings, Permissions, Calendar settings, Facet settings/carousel, Dock/Favorites pickers) gained explicit `.windowInsetsPadding(WindowInsets.systemBars)` — fixes the app drawer's search bar rendering under the status bar.
- **Clock now ticks off `ACTION_TIME_TICK`** (+ `ACTION_TIME_CHANGED`/`ACTION_TIMEZONE_CHANGED`) via a context-registered `BroadcastReceiver`, replacing a 30s polling loop — real-time rollover, no polling.
- **Calendar events filtered by end time**: `CalendarEvent` gained `endTimeMillis`; `ClockBlock` shows all-day events plus any event whose end time hasn't passed yet, reusing the same tick-driven `now`.
- **Per-calendar accent bar colors**: new `domain/AssignCalendarColorsUseCase.kt` (pure, stable/cycling palette assignment) + `LauncherSettings.calendarColors: Map<calendarId, AccentSwatch>`, resolved per-theme via a new `AccentSwatch.resolvedColor()` extension.

### Dock/Favorites reorder + drag-jump bug fix
- **Drag-to-reorder** for both Dock (Settings) and Favorites (Settings' new Default favorites card, and each facet's own Apps card) — picker screens (`DockAppPickerScreen`/`FavoritesPickerScreen`) show selected apps first (frozen at page load, not live-reshuffling on check/uncheck) with removal only via unchecking; reordering lives exclusively in the Settings-side card, never the picker.
- **Real drag-jump bug found and fixed**: `remember`/`pointerInput` keyed on the raw app list reset mid-drag on every incidental `LauncherApps` re-emission, because `AppInfo.icon: ImageBitmap` has no structural equality — each such emission looked like a "new" list even when membership/order hadn't changed, resetting drag state and restarting the gesture-detection coroutine. Fixed by keying only on stable `(packageName, activityName)` identity and reading live values through `rememberUpdatedState` inside the gesture callback, applied everywhere this pattern appears (`FavoritesReorderList`, `DockSection`, `DefaultFavoritesReorderList`, defensively also `FacetReorderList`).

### AppContextMenu / long-press sheet visual redesign
- **`AppContextMenu`** (F12's long-press menu): corner radius changed from the shared `ThemedDropdownMenu` default (`extraSmall`, 4dp) to match `SettingsCard`'s own shape (`MaterialTheme.shapes.medium`, 12dp) via new optional `shape`/`contentPadding` params on `ThemedDropdownMenu`/`ThemedDropdownMenuItem` (defaults unchanged, so every other dropdown in the app is unaffected); row separators added between every row (were missing between App info/Uninstall and each quick action).
- **Long-press sheet** (`LongPressSheet.kt`) got the identical row-separator fix, plus a right-side chevron on every row (all four navigate away — to the carousel, Facet settings, Settings, or the system wallpaper picker).
- **Right-side chevron adopted as a standing convention** for any row that navigates to another screen: added to Settings' Facets/Calendar/Permissions/Select Dock Apps/Default favorites/Set-as-default-launcher rows and Facet Settings' Edit favorites/Calendar rows. Rows that open a dialog (Rename) intentionally excluded — a dialog isn't a navigation.

### New feature: Default favorites (global list, mirrors Dock)
Favorites were previously always per-facet with no baseline — a fresh facet started from an empty "Nothing here yet" state. New global **Default favorites** list, edited from its own Settings card exactly like Dock:
- New `DefaultFavoriteAppEntity`/`DefaultFavoriteAppDao`/`DefaultFavoriteAppRepository` (global, no facet scoping — structurally identical to `DockAppRepository`). `FacetDatabase` bumped to v6 (`fallbackToDestructiveMigration`, same pre-release precedent as every prior bump).
- `FacetEntity` gained `overridingFavorites: Boolean` (default `false` = inherit the default list). `FavoritesPickerScreen`/`ViewModel` generalized to take an optional `facetId` (mirroring `CalendarSettingsScreen`'s existing global/per-facet pattern) so the same picker edits either the default list or one facet's own.
- Facet Settings' **Apps** section redesigned around one combined Inherit/Override card — not two — governing list content mode, apps-to-show, and favorites together as a single unit, with one card below it (list-content dropdown, conditional apps-to-show, Edit favorites row + reorder list) matching Settings' own "APPS LIST" card shape exactly.
- `ObserveHomeScreenStateUseCase`/`ObserveFacetPreviewsUseCase`/`CleanUpUninstalledAppsUseCase` all updated to resolve/clean up the default list alongside per-facet ones.
- Test: `DefaultFavoriteAppRepositoryTest`, `FacetDaoTest` (new — see below), `FacetRepositoryTest` additions, `FavoritesPickerScreenTest`/`FacetSettingsScreenTest`/`SettingsScreenTest` additions.

### Two real bugs found and fixed — both were the actual cause of "can't switch app-list mode / can't select override"
- **Room `Converters` for `ListContentMode` were declared non-null in/out for a column that's `ListContentMode?` (nullable).** Room can't resolve a non-null-typed converter as a match for a nullable field, so every write to `listContentModeOverride` silently landed as SQL `NULL` regardless of what was passed in — switching to Override (or picking Recents/Most used) looked like it did nothing because the value never actually persisted. Fixed by declaring both converter methods nullable in/out. **This class of bug is invisible to a fake-in-memory-DAO test** (it just stores the Kotlin object directly, never touching a real converter) — added `data/local/FacetDaoTest.kt`, a genuine Room-backed (`Robolectric` + `Room.inMemoryDatabaseBuilder`) test suite that exercises the real converter and would have caught this.
- **A second, separate bug in this session's own `setOverridingApps` rewrite**: it made three sequential `FacetRepository` setter calls (mode, apps-to-show, favorites-flag), each built from the *same* facet snapshot captured at click time. Each setter does a full-row `INSERT OR REPLACE` from whatever entity it's handed, so each call after the first silently clobbered the previous call's field change — only the last call's own field actually stuck. Fixed with a single atomic `FacetRepository.setOverridingApps(...)` that sets all three fields in one `.copy()` + one `upsert()`, making the clobbering impossible by construction. Added `FacetRepositoryTest` coverage asserting all three fields land together, in both directions (Override and back to Inherit).

---

## Post-Phase-9 polish — permission banners, App list Position/Presentation, Launcher Font & App label color, calendar per-facet override fix

Direct follow-up work spanning several separate requests, landed in one long session. `assembleDebug`, `test`, and `connectedAndroidTest` all re-verified green after each change (the emulator needed several cold restarts across this session — see the environment note added to the testing-device memory; none of the instability turned out to be code-related except where called out below). Installed and manually confirmed on the physical test phone throughout.

### Dismiss-on-click permission banners
- **Home's usage-access banner and a new Drawer contacts-access banner** both now dismiss the moment the user taps their own "Turn on"/"Open settings" button, rather than staying up until the permission is actually confirmed granted (which could take a beat, or never happen if the user backs out of the system dialog). `HomeUiState`/`DrawerViewModel` each gained a `MutableStateFlow<Boolean>` dismissed-flag combined into their own `uiState`.
- **Settings → Permissions gained a Notification access row**, and the "Notification badges" toggle on the dedicated Notification Settings page now reflects the real OS grant (`notificationDotsEnabled && isAccessGranted`) instead of just the stored preference — found live on-device: the toggle read "on" by default while the real permission was never granted, so badges silently never showed with no visible reason why.
  - Test: `PermissionsScreenTest`, `PermissionsViewModelTest`, `NotificationSettingsScreenTest` (new `FakeNotificationAccessRepository`, mirroring the existing `FakeCalendarPermissionRepository` pattern).

### App list Position (Left/Right) and Presentation (Icon Only/Icon & Text/Text Only)
New per-facet-overridable Apps-list settings, added to `LauncherSettings`/`FacetEntity` alongside the existing content-mode override — `AppRowPosition`/`AppRowPresentation` enums, `HomeScreen.kt`'s `AppRow` reordering (badge/label/icon packed to the row's trailing edge for Right), and the FAVORITES/RECENTS/MOST USED section heading now follows the same alignment as the rows themselves.
  - Test: `HomeUiStateTest`, `HomeScreenTest`, `SettingsScreenTest`, `FacetSettingsScreenTest`.

### Launcher Font and App label color (Settings → Appearance)
- **Launcher Font**: the base font for every text role app-wide (`ui/theme/Type.kt`'s `facetTypography(fontFamily)`, now a function of the chosen `LauncherFontOption` instead of a fixed constant) — except the clock/calendar, which keep their own independent font picker. That picker gained a new `ClockFontOption.LAUNCHER_DEFAULT` ("Default launcher font") entry, now also the *default* for both `clockFontOption` and `calendarFontOption`, that resolves to whatever Launcher Font is currently set to.
- **App label color**: Home's app-list rows and dock labels get their own color, reusing `ClockColorOption` (the same enum the clock/calendar already used) rather than a new type.
- **`ClockColorOption` itself was redesigned mid-session, twice, on direct request**: `WALLPAPER_PRIMARY`/`WALLPAPER_SECONDARY` were replaced with `ACCENT_PRIMARY`/`ACCENT_SECONDARY` (tracks Settings → Theme → Accent color's own source — the "Basic colors" swatch, or the wallpaper's own Material You tones when "Wallpaper colors" is selected — rather than always being the raw wallpaper tones regardless of the user's accent choice); then `INK`/`WHITE`/`BLACK` were replaced with `THEME` (default — exactly today's `Ink`) and `THEME_INVERTED` (its deliberate opposite, added as `Color.kt`'s new `InkInverted` token) — a fixed White/Black pick only ever reads correctly in one of the app's two theme modes, so this always flips to whichever one currently contrasts instead of needing to be re-picked.
  - Test: `SettingsRepositoryTest`, `SettingsViewModelTest`, `ClockFontsTest` (new), `ConvertersTest` (new, see the real bug below).

### Calendar font/color made genuinely facet-overridable
`CalendarSettingsScreen`'s Font/Color card used to fake a per-facet override — facet-scoped edits stayed local and were silently discarded, even though the screen's own `overrideCalendar` flag already genuinely persists `showAllDayEvents` per-facet. Fixed: `FacetEntity` gained real `calendarFontOption`/`calendarColorOption` columns (Room bumped to **v10**), `FacetRepository.updateOverridingCalendar(...)` now seeds/persists both alongside the existing field, and the Font/Color card is now read-only under Inherit / live under Override, matching every other override card in the app.
  - Test: `CalendarSettingsViewModelTest`, `FacetRepositoryTest`, `CalendarSettingsScreenTest`.

### Real bugs found and fixed during this pass
- **A live crash on the user's own phone, caused by this session's own `ClockColorOption` rename.** Every existing install's Room database still held the old `INK`/`WHITE`/`BLACK` strings; `Converters.kt`'s `toClockColorOption` (and every other enum converter in that file) returned `null` for a string that no longer matched any constant, and Room's generated code for a NOT NULL column has no fallback of its own — it crashed outright on every launch (`IllegalStateException: Expected NON-NULL '...', but it was NULL`, confirmed via `adb logcat -b crash`). Root cause: every converter in `Converters.kt` was nullable-in/out, a pattern that only ever suited one now-removed nullable column (`listContentModeOverride`, from before facet settings switched to explicit override fields). Fixed by making every converter fall back to that type's own default instead of returning null — safe by construction for any *future* enum rename too, no migration needed. Added `data/local/ConvertersTest.kt` specifically asserting an unrecognized stored string (including the exact old `INK`/`WHITE`/`BLACK` values) falls back cleanly instead of crashing.
- **`AppRow`'s icon/label testTags were unreachable via the default merged-semantics query**, deterministically — not a flake, reproduced identically on a freshly-booted emulator every time. `combinedClickable` merges descendant semantics into itself (so TalkBack announces the row as one unit), and `testTag` is deliberately excluded from that merge (unlike text/content-description, which *do* get merged and matched fine) — so `onNodeWithTag(...)` needs `useUnmergedTree = true` for any tag living on a clickable row's descendant. Fixed in `HomeScreenTest.kt`'s five affected assertions; this is a general pattern worth remembering for any future test on a similarly-structured row.
- **`sectionLabelReflectsTheActiveListContentMode` asserted against `appListItems = emptyList()`**, even though `HomeScreen.kt`'s own heading only renders `if (appListItems.isNotEmpty())` — a pre-existing, unrelated test bug (predates this session) only now surfaced by getting a clean run at all. Fixed by giving the test a real one-item list.
- **`KeyboardDismissalTest` was silently crashing on every single run**, unconditionally, for as long as `HomeDrawerRoute`'s `widgetPickerViewModel: HubWidgetPickerViewModel = hiltViewModel()` default parameter has existed — this test builds every other ViewModel by hand and passes it in explicitly, but never that one, so the default's real `hiltViewModel()` call always executed against this test's plain (non-Hilt) `ComponentActivity` and always crashed before any assertion ran. Fixed by building a `HubWidgetPickerViewModel` by hand too, matching the other four. Once fixed, this surfaced a **separate, still-open, real issue**: back-press and swipe-down don't reliably clear the drawer search field's focus/keyboard in this test (2 of 3 tests in the class) — not chased further this pass, flagged here so it isn't lost.
- **`HubGestureTest.movingAWidgetAfterResizingItPreservesTheNewSpan` times out waiting for a placement update after a resize-then-move sequence** — reproduces consistently in isolation, unrelated to anything touched this session (Hub grid resize/move gesture code wasn't modified). Not investigated further; flagged as a separate, real, pre-existing issue in the Hub gesture state machine.

---

## Post-Phase-9 polish, continued — Settings reorganization, surface color tokens, themed dropdown

Direct follow-up requested after user feedback that Settings "still feels not well grouped," even after the earlier Appearance/Apps-screen splits. `test`/`connectedAndroidTest` re-verified green after every change (instrumented runs explicitly scoped to the emulator only via `ANDROID_SERIAL` — see the new CLAUDE.md rule this pass added, after a real incident where an unscoped run reached the user's phone and uninstalled the app mid-run).

### Settings list regrouped by user mental model, not build history
The flat top-level list (Facets, Clock, Notifications, Apps, App Drawer, Appearance, Permissions, an unheaded Backup/Default-launcher card) is now 5 named groups, chosen by what a user is actually looking for rather than the order sections happened to be built in:
- **FACETS** — unchanged, still the entry point to per-facet overrides.
- **APPEARANCE** — unchanged (Theme, Accent, Icons, Launcher Font, App label color). Deliberately does *not* absorb Clock & Calendar Style, even though that's also a "look" decision — grouping Clock & Calendar by *subject* (style + content together) read as more intuitive on direct request than grouping everything by *decision type* (style vs. content) would have.
- **CLOCK & CALENDAR** — unchanged (Clock & Calendar Style, Calendars to display).
- **HOME & APPS** (new) — Dock, Home Apps List, App Drawer, Notifications. Notifications moved here on the reasoning that its whole subject (dots/badges) is literally rendered on Dock/Home/Drawer icons; the row's own subtitle now also states this cross-cutting scope and the actual configured badge style explicitly (`"Enabled · Dots on Dock, Home & Drawer"`/`"Enabled · Counts on Dock, Home & Drawer"`/`"Disabled"` — not a generic "dots/badges" placeholder).
- **SYSTEM** (new) — Permissions, Backup & restore, Set as default launcher, now under one real header instead of Permissions standing alone and the other two sharing an unheaded card.

### Dock & Apps List split into three screens
The combined `AppsSettingsScreen`/`AppsSettingsViewModel` (DOCK + APPS LIST sections) is retired, replaced by three independent screens/ViewModels, each with only the repositories it actually needs (no more pulling in `DefaultFavoriteAppRepository` just to read `dockApps.size`, or vice versa):
- **`DockSettingsScreen`/`DockSettingsViewModel`** — Display style, Select Dock Apps, drag-to-reorder.
- **`HomeAppsListSettingsScreen`/`HomeAppsListSettingsViewModel`** — Position, Presentation, Default App list content, Apps to show, Default favorites + reorder. This is the screen Facet Settings' own "APPS LIST" override card maps to 1:1 now (previously ambiguous, since the combined screen bundled Dock in too, and Dock has never been facet-overridable).
- **`AppDrawerSettingsScreen`/`AppDrawerSettingsViewModel`** (new) — the 7 controls (Presentation, Grid/List size, Show icons/labels, Search contacts, Search bar position, Drawer opacity) that used to be the last big inline block left on the main Settings list, including moving the real `READ_CONTACTS` permission-request wiring (`rememberLauncherForActivityResult`) into this screen.
  - Test: `DockSettingsViewModelTest`/`DockSettingsScreenTest`, `HomeAppsListSettingsViewModelTest`/`HomeAppsListSettingsScreenTest`, `AppDrawerSettingsViewModelTest`/`AppDrawerSettingsScreenTest` (all new), `SettingsScreenTest`/`SettingsViewModelTest` updated (the latter now purely observational — no setters left once every mutable section moved out).

### Facet Settings section headers realigned
`FacetSettingsScreen`'s "APPS" → **"APPS LIST"** (matches the Home Apps List screen it overrides, explicitly *not* "HOME & APPS" — Dock and App Drawer are never facet-scoped, so the broader name would overpromise), "CLOCK" → **"CLOCK & CALENDAR"** (matches the renamed global group, content was already 1:1 so this is a label-only change).

### Settings page background now visually distinct from its cards
`ui/theme/Color.kt` gained `SurfaceContainer` (`#F8F9FA` light / `#14171D` dark) — every settings-family screen's own page background (both the sticky header and the scrolling content behind it), while `Surface` itself is untouched and still backs every `SettingsCard`. Previously both were the same tone, distinguished only by a card's drop shadow/hairline border. Direction is consistent in both themes: the card is always the *lighter* of the two. Values were tuned down from an initial, more contrasty draft after live comparison (an HTML artifact mocking up both themes side-by-side) — the user found the first pass too strong. `ThemedDropdownMenu`'s popup background also switched from `Surface` to `SurfaceContainer`, so a dropdown floating above a `SettingsCard` row reads as its own plane rather than blending into the card it's anchored to.
  - Screens updated: `SettingsScreen`, `AppearanceSettingsScreen`, `CalendarSettingsScreen`, `ClockStyleGalleryScreen`, `DockSettingsScreen`, `HomeAppsListSettingsScreen`, `AppDrawerSettingsScreen`, `NotificationSettingsScreen`, `PermissionsScreen`, `BackupRestoreScreen`, `FacetSettingsScreen`.

### New pre-existing test failures observed (not caused by this pass, not yet fixed)
Running the full instrumented suite surfaced 6 failures beyond the already-documented `HubGestureTest` one above, confirmed to already fail on the very first full run *before* any of this pass's changes existed (so not a regression from this work, but newly noticed by it):
- `HubScreenTest.removingAnOrphanedWidgetDeletesItsPlacement` — times out; the test only clicks a plain "Remove" button, doesn't touch anything this pass modified.
- `HomeDrawerRouteTest`: `lowVelocitySwipePastTwentyPercentStillClosesTheDrawer`, `swipingDownAtDrawerTopReturnsToHome`, `swipingUpPastHalfwayCommitsTheDrawerOpen`, `systemBackClosesAnOpenDrawer`, `releasingBeforeHalfwaySpringsBackClosed` — all fail on a missing "FAVORITES" text node; unrelated to Settings.

Not investigated further this pass; flagged here so they aren't lost or mistaken for a regression the next time this area is touched.

## Post-Phase-9 polish, continued — Font weight customization (Calendar + Home Apps) + Appearance live preview

Picking back up a feature explicitly deferred earlier this session: **"Along with launcher font, add font weight. We need this in calendar separately, and in appearance separately (for home - apps list+dock, and app drawer)"**, plus the earlier-deferred Appearance live preview. Two independent weight settings, not one — **Calendar weight** (facet-overridable, part of the `overrideClock` Clock+Calendar design bundle) and **Home Apps weight** (global-only, scopes to Home's app list + Dock + App Drawer text, not the whole app's typography). Plan approved in full before building — see chat history for the milestone breakdown (M1 data layer → M2 Calendar wiring → M3 Home Apps wiring → M4 live preview).

### ✅ M1 complete — data layer
- New `data/model/FontWeightOption.kt` — 6-stop enum (`THIN`…`SEMI_BOLD`, no heavier — governs body/label text, not headlines), plus `ui/theme/Type.kt`'s `FontWeightOption.resolve(): FontWeight`.
- `LauncherSettings`/`SettingsRepository` gained `calendarFontWeight`/`homeAppsFontWeight` (two independent DataStore keys + setters, same enum-name-string round-trip as every other style enum here).
- `FacetEntity` gained `calendarFontWeight` (Clock+Calendar design bundle, alongside `calendarFontOption`/`calendarColorOption`). Room **v11 → v12**, `MIGRATION_11_12` (`ALTER TABLE facets ADD COLUMN calendarFontWeight TEXT NOT NULL DEFAULT 'REGULAR'`) — verified with a real `FacetDatabaseMigrationTest` case (insert v11 row, migrate, assert survival + default), not just a compiling migration.
- `FacetRepository.updateOverridingClock(...)` extended with the new param; standalone `setCalendarFontWeight` added. `FacetSettingsViewModel`/`UiState` extended with the same inherit/override resolution pattern as `calendarFontOption`.
- Backup & Restore: `BackupSettings`/`BackupFacet` gained the new fields (defaulted — tolerant-reader discipline, an old backup missing them still deserializes), mapped through `BackupMapping.kt`/`ExportBackupUseCase`/`ImportBackupUseCase`.
- `ui/components/FontWeightSlider.kt` converted from its mockup `selectedIndex: Int` API to the real `selected: FontWeightOption, onSelectedChange: (FontWeightOption) -> Unit` API — same slider mechanics/live preview/all `@Preview`s, just re-typed.
- Tests: `TypeTest` (resolver, one assertion per stop), `SettingsRepositoryTest` round-trip test for both new setters (plus defaults assertion), `FacetRepositoryTest` (`updateOverridingClock` extended + new `setCalendarFontWeight` test), `FacetDatabaseMigrationTest.migration11To12AddsCalendarFontWeightColumnWithoutLosingExistingRows`. `./gradlew test` green.

### ✅ M2 complete — Calendar weight wiring
- `ClockStyleGalleryUiState`/`ViewModel` gained `calendarFontWeight` (facet-or-global resolution mirroring `calendarFontOption` exactly) + `setCalendarFontWeight`.
- `ClockStyleGalleryScreen.kt`'s CALENDAR card gained a `FontWeightSlider` row (Font → Color → Weight → live preview); the preview's `CalendarEventsBlock` call now threads the resolved weight in.
- Threaded into Home's real rendering too: `ClockBlock`/`HomeScreen`/`HomeUiState` all gained `calendarFontWeight`/`activeCalendarFontWeight` (mirrors `activeCalendarFontOption`'s own `overrideCalendar` gating exactly), wired through `HomeDrawerRoute`.
- Tests: `ClockStyleGalleryViewModelTest` (global + facet-scoped setter/resolution), `HomeUiStateTest` (`activeCalendarFontWeight` inherit/override cases).

### ✅ M3 complete — Home Apps weight wiring
- `AppearanceSettingsViewModel` gained `setHomeAppsFontWeight`; `AppearanceSettingsScreen.kt` gained a `FontWeightSlider` row right after Launcher Font.
- `HomeScreen.kt`'s `AppRow`/`DockIcon` gained `labelFontWeight` params; `HomeScreen`'s own signature gained `homeAppsFontWeight`, resolved once and passed to both.
- `AppDrawerScreen.kt`'s two real app-label `Text`s (`DrawerAppRow`, `DrawerGridTile`) gained the same resolved weight, threaded down from `AppDrawerScreen`'s own top-level signature — the first time Drawer's app-label text became settings-driven at all. `DrawerSearchResults` picks it up for free since it already reuses `DrawerAppRow`/`DrawerGridTile` directly.
- `HomeDrawerRoute.kt` threads `homeAppsFontWeight` into both `HomeScreen` and `AppDrawerScreen`; `FacetCarouselScreen.kt`'s own `DockIcon` reuse verified to pick up the real global value too.
- Tests: `AppearanceSettingsViewModelTest`/`AppearanceSettingsScreenTest` (setter + slider interaction).

### ✅ M4 complete — Appearance live preview
- New `AppearancePreviewCard` at the top of `AppearanceSettingsScreen.kt`'s list, above the existing controls — a `Wallpaper`-backed card (not `Surface`/`SurfaceContainer`, so `AppRow`/`DockIcon`'s text-shadow treatment renders exactly as it would on real Home, not washed out against a plain light card) reusing the real `AppRow` (promoted `private` → `internal`, matching `DockIcon`'s existing visibility) and `DockIcon` with two synthetic sample apps (`icon = null`, same placeholder convention this file's own `@Preview`s already use).
- Needs no preview-only state: driven directly by the screen's own live `settings` (`appRowPosition`/`appRowPresentation`/`appLabelColorOption`/`homeAppsFontWeight`/`dockDisplayMode`), which already updates immediately after every setter call. Theme/Accent and Launcher Font aren't threaded as explicit params — they render correctly for free, since `AppRow`'s text already reads `MaterialTheme.typography`/the app's real `colorScheme`.
- **Known scope boundary**: Icon render mode isn't visually demonstrated here — the sample apps have no real icon bitmap to tint, so their icon always falls back to the placeholder regardless of that setting.
- Tests: `AppearanceSettingsScreenTest.previewCardShowsSampleAppsAndDockIcon`, `.previewCardStillRendersAfterChangingAppLabelColor` (a control change round-trips through recomposition without the preview breaking).

---

## Post-Phase-9 polish, continued — Cold-start loading gate & installed-apps perf fix

Direct follow-up after noticing (and fixing) a flash of default-styled content on Home right after the launcher process starts.

- **`LauncherUiState`/`HomeUiState` both gate on a real `isLoading` flag** (the former already existed but was never read; the latter is new) — `LauncherActivity`/`HomeDrawerRoute` now render nothing at all (the real wallpaper shows through, since the window is already `windowShowWallpaper`/transparent-background) until each ViewModel's first real settings/apps emission lands, instead of a frame styled with hardcoded defaults. A 3-second timeout safety net in both ViewModels forces `isLoading = false` regardless, so a stuck upstream flow can never leave the actual home screen permanently blank.
- **Real root cause of a *second*, unrelated slowness this surfaced**: `AppRepository.getInstalledApps()` was decoding/flattening every installed app's icon sequentially in one coroutine — on a real phone with 100+ apps, multiple real seconds before the very first emission, previously invisible only because default content rendered over it in the meantime. Fixed by running each icon's decode as its own `async` on `Dispatchers.Default`'s thread pool instead of one after another.
- Tests: `LauncherViewModelTest`/`HomeViewModelTest` (`isLoading` true→false transition, and the timeout-fallback case with a never-emitting flow).

## Post-Phase-9 polish, continued — Facet switcher redesign & long-press rewiring

Reworks `FacetCarouselScreen` as prep for (and then completing) making it the Home long-press destination directly, replacing the old options sheet.

- **Per-card name + 3-dot menu**: facet name and a "Facet settings"/"Delete facet" dropdown now sit right above each page's own card (on the carousel's backdrop, not inside the card's `Surface`), tied to that specific page — swiping swaps in that page's own header along with its card. Replaces the old standalone gear/trash icon row.
- **"Launcher settings" row pinned to the bottom** of the carousel screen — this is where that option now lives, since the sheet it used to live in is gone.
- **Long-press on Home now opens the facet carousel directly** — `LongPressSheet.kt` and its test deleted outright. Its other rows already had new homes: Launcher settings → the carousel screen itself (above); Change wallpaper → a new row in Settings, right below Facets; Edit facet → reachable via the carousel's own per-card menu.
- **Wallpaper option added to Settings**: a "Change wallpaper" row (opens the system picker via `ACTION_SET_WALLPAPER`) next to the renamed "Launcher Appearance" row, whose subtitle is now a static description instead of the live theme/font values.
- **Card-scale/spacing fixes**: the carousel's `CARD_SCALE` `graphicsLayer` transform now anchors to top-center (not the default center), so the per-page name row isn't pushed down by half the scale's own lost height; a small fixed top gap and 16dp-below-the-card dot-indicator spacing were tuned to match.
- **Full preview threading**: the preview card only reflected Clock style before. `ObserveFacetPreviewsUseCase` now also fans out each facet's calendar events (gated by calendar permission + `overrideCalendar`'s `showAllDayEvents`, mirroring `ObserveHomeScreenStateUseCase` exactly); `FacetCarouselUiState` gained per-facet `calendarFontOption`/`calendarColorOption`/`calendarFontWeight`/`appRowPosition`/`appRowPresentation`/`listContentMode` helpers matching `HomeUiState`'s own gating; the preview card reuses the real `AppRow` (promoted `internal`) instead of a hand-drawn row, and its list header now shows FAVORITES/RECENTS/MOST USED matching the facet's actual mode.
- **Real bug found and fixed while threading calendar data through**: `observeCalendarEvents` in both `ObserveHomeScreenStateUseCase` and `ObserveFacetPreviewsUseCase` always queried with the *global* `selectedCalendarIds`, even when a facet overrides calendar settings with its own selection — only `showAllDayEvents` actually respected the override. Both now resolve `selectedCalendarIds` the same way.
- **Real bug found and fixed via direct user report**: reusing `AppRow`/`DockIcon` in the preview card meant their own click/long-press consumed the touch instead of letting it reach the card's own "apply this facet" tap — tapping directly on a favorite row or dock icon silently did nothing, and long-pressing one opened Home's real Uninstall/App Info menu inside what's meant to be a read-only preview. Fixed: their `onClick` now fires the same facet-apply action as the rest of the card; both gained an `enableLongPressMenu` flag (default `true`, matching Home's real behavior) that the preview sets `false`.
- Tests: `FacetCarouselScreenTest` (per-card menu tap/delete/settings-navigation, tap-on-favorite/dock-still-applies, long-press-on-favorite/dock-does-not-open-context-menu, Launcher-settings-row-navigates), `SettingsScreenTest`/`AppearanceSettingsScreenTest` updates, `ObserveFacetPreviewsUseCaseTest` (new — calendar-events fan-out, per-facet `selectedCalendarIds` override), `ObserveHomeScreenStateUseCaseTest` (`selectedCalendarIds` override case).

## Post-Phase-9 polish, continued — App-wide ripple strengthened

Material3's default ripple was too faint to notice, especially over Home's wallpaper. `FacetLauncherTheme` now overrides `LocalRippleConfiguration` app-wide with roughly double the default alpha levels. The tint itself is passed as `Ink` explicitly rather than left `Color.Unspecified` — this app's screens don't wrap content in a Material3 `Surface` (Home/Drawer rows are plain `Column`/`Box` + `.background()`), so `Color.Unspecified` would have deferred to `LocalContentColor`, which never actually gets set to the theme-aware `Ink` here and stays stuck at Compose's own fixed default — confirmed via direct on-device report that the ripple looked identically dark in both themes before this fix.

---

## Known gap: PRD requirements with no owning phase

Found while planning Phase 2 — each of these has a real PRD requirement but isn't scheduled anywhere in Phases 2–8 above. Not resolved now (would be its own re-planning pass); flagging so it isn't assumed "already covered" later:

- ~~**App Drawer Grid presentation**~~ (F6) — **resolved and built:** now owned and shipped by Phase 4 (Milestone 1, see above) — `LazyVerticalGrid` rendering, alphabet rail shared unchanged with List. F8's left-edge rail was already done (Phase 2).
- ~~**Multiple clock styles**~~ (F1: "Rule", "Date-forward", the style picker page `3e`/`3f`) — **resolved and built:** `ClockTemplateId` now has 10+ templates (Light stack, Rule & meridiem, Date forward, Weight contrast, Italic accent, Spelled out, two Vertical stack variants, plus Phase 10's advanced Roboto Flex Wide/Narrow), all reachable from the real `ClockStyleGalleryScreen.kt` picker, globally and per-facet.
- ~~**F11 icon customization**~~ (System default vs. Monochrome overlay, accent color) — **resolved:** now owned by Phase 5 (see above), alongside dark mode, plus Icon rendering mode from Phase 7. **Update (see chat history):** Settings' Appearance card is no longer empty — it now has Launcher theme, Accent color, Icons, **Launcher Font** (new — the base font for every text role app-wide except clock/calendar, which keep their own independent font/color pickers; a `LAUNCHER_DEFAULT`/"Default launcher font" choice on those follows whatever this is set to), and **App label color** (new — Home app-list row + dock label text color, reusing the same `THEME`/`THEME Inverted`/`Accent primary`/`Accent secondary` palette the clock/calendar already offer).
- ~~**F7 Alphabet rail bucketing**~~ — **resolved:** now owned by Phase 4 (see above). Not yet built — Phase 4 hasn't started.
- ~~**F12 Long-Press Context Menu**~~ — **resolved and built:** shipped by Phase 4 (Milestone 6, see above) — App info/Uninstall/Quick actions; rename deferred to Phase 5's icon-pack work instead of shipping here.

A second, later audit against the full PRD (see chat history) found two more gaps not yet assigned to any phase — recorded here so they aren't lost, decision on where they land still pending:

- ~~**F9 real wallpaper rendering**~~ — **resolved and already built, just not via `WallpaperManager`:** an earlier audit here wrongly flagged this as unbuilt after grepping only for `WallpaperManager` references. `Theme.FacetLauncher` (`app/src/main/res/values/themes.xml`) sets `android:windowShowWallpaper = true` with a transparent `android:windowBackground` — the standard mechanism real Android launchers use, letting the OS composite the live system wallpaper behind the window at the system level, no manual bitmap-drawing code needed. Home/Drawer's own Compose surfaces paint no opaque background over it, so the real wallpaper already shows through today. The "launch system wallpaper picker" intent (Phase 2's Change Wallpaper row) covers the other half of F9. Nothing left to build here.
  - **Follow-up (preview cards):** `windowShowWallpaper` compositing can't reach a preview surface, so the facet-carousel cards and the Appearance preview card were still flat `Surface`/`Wallpaper`-token slabs. Now backed by the real system wallpaper via `data/WallpaperRepository.kt` (`WallpaperManager.peekDrawable()` → `builtInDrawable` → `getWallpaperColors` gradient → `Wallpaper` token, all `runCatching`-guarded; injected `WallpaperManager` from `AppModule`), a `data/model/HomeWallpaper.kt` sealed type, and a shared `ui/components/WallpaperBackground.kt`. Loaded once per screen-open by `FacetCarouselViewModel`/`AppearanceSettingsViewModel`. No scrim (matches Home's text-shadow approach). Carousel cards center-crop (locked to screen aspect); the Appearance card bottom-anchors its crop (short band → show the dock area). Tests: `WallpaperRepositoryTest` (JVM/Robolectric, 6 cases) + a rendering assertion in each screen's instrumented test. `test` + full `connectedAndroidTest` (287) green.
- ~~**Home swipe-down → notification shade**~~ — **resolved and built** (by a separate concurrent session, not tracked under any phase above): `data/NotificationShadeRepository.kt` + `SWIPE_DOWN_SHADE_DISTANCE` wiring in `HomeDrawerRoute.kt`/`HomeViewModel.kt`. Not verified end-to-end by this session; worth a manual on-device check and a dedicated write-up next time this area is touched.

Whichever phase picks one of these up should also un-disable the corresponding Settings row(s) built in Phase 2, where applicable.

---

## Post-Phase-10 polish — 16 shape-based clock templates + clock hit-box/gesture fix

Direct follow-up to Phase 10: the 16 templates originally speced in the design mockup's "Eleven new clock templates" / "Eight more" rounds, plus a real, long-standing gesture bug found while componentizing the widget for them.

- **16 new `ClockTemplateId` entries** (`ClockTemplateId.kt`) — `ACCENT_FIELD`, `HOUR_TILE`, `CHIP`, `DUOTONE_OVERLAP`, `CORNER_FRAME`, `STUB`, `HALO`, `DIGIT_CELLS`, `NEGATIVE_PANEL`, `HOLLOW_HOUR`, `HIGHLIGHTER`, `COLUMN_RULE`, `COLON_MARK`, `PILL_PAIR`, `SHELF`, `HALF_IMMERSED` — the first templates in `ClockTemplates.kt` to use a shape (`clip`/`background`/`border`) rather than pure typography; every fill is either the resolved `Accent` or the caller's own `textColor`, with `Surface` as the knockout tone for digits sitting on a filled field (matching `Theme.kt`'s own `onPrimary = Surface` convention). `HollowHourTemplate` uses `TextStyle`'s `drawStyle = Stroke(...)` for an outline-only glyph; `HalfImmersedTemplate` uses a real `BlendMode.Multiply` inside an offscreen `graphicsLayer` so the blend composites against the glyph's own rasterized pixels rather than the wallpaper.
- **Shared "clock part" composables** (`ClockGlyphText`/`HourText`/`MinuteText`/`SeparatorText`/`TemplateDateText`, next to the existing `MeridiemText`) — hour/minute/separator/date each theme and position independently now; `WeightContrastTemplate`/`AccentContrastTemplate` migrated onto them as the zero-visual-diff pattern-setters.
- **Real bug found and fixed**: the clock's tap-to-open/long-press-to-drag hit box (`home_clock_block`) was full-width regardless of `clockAlignment`, because `ClockBlock`'s own Column and every template's own root called `.fillMaxWidth()`. Any long-press landing in the empty space beside a Left/Right-aligned clock was silently swallowed by this box before `HomeDrawerRoute`'s own outer long-press (open the facet carousel) ever saw it. Fixed by dropping `fillMaxWidth()` from every template root (20 existing + 16 new) and giving `ClockBlock` a new `clockContentModifier` param, applied to a wrap-content box around just `ClockDisplay` (positioned via `Modifier.align(clockAlignment.resolve())` inside `ClockBlock`'s still-`fillMaxWidth()` outer Column, so `CalendarEventsBlock`'s own independent `calendarAlignment` is untouched) — only the clock's actual rendered content is tappable now, everything else falls through. `ClockStyleGalleryScreen.kt`'s per-card preview needed the same explicit per-child `Modifier.align(...)`, since the shared `horizontalAlignment` it used to lean on no longer has any effect once every template lost its own `fillMaxWidth()`.
- Tests: one smoke test per new template (`ClockBlockTest`), a `HomeScreenTest` case proving a long-press beside the clock no longer reveals the drag handle, a `HomeDrawerRouteTest` case proving that same long-press still reaches the facet carousel, and a `ClockStyleGalleryScreenTest` case locking in the gallery's own per-card alignment fix.

---

## ✅ Clock widget resize — complete, verified on-device

Adds a scale factor alongside the existing zone-height *position* feature — corner-drag resize of the clock+date only (not `CalendarEventsBlock`), global + per-facet override via the existing `overrideClock` bundle. Long-press on the clock opens a bottom sheet ("Change widget position" / "Resize clock widget" / "Edit styles") instead of directly revealing the move handle.

- [x] Single uniform `clockScale` field (default `0.8f`) on `LauncherSettings`/`FacetEntity` + `SettingsRepository`/`FacetRepository` persistence, folded into `resetClockPosition`.
- [x] DB: `MIGRATION_14_15` adds `clockAccentColorOption` + `clockDateStyle` + `clockScale` in one step (nothing shipped between 14 and 15; `VERSION = 15`, `15.json` is the export). `DatabaseModule` back on `fallbackToDestructiveMigrationOnDowngrade` (destructive on *upgrade* was silently wiping real data).
- [x] `HomeUiState`/`FacetCarouselViewModel` override resolution + `FacetCarouselScreen` preview card wiring.
- [x] **`uniformScale` is draw-only** — reports the *natural* footprint to layout and scales the glyph via a graphics layer anchored to the alignment-facing bottom corner (`TransformOrigin` 0/0.5/1 · 1). Nothing positioned relative to the clock reflows when the scale changes; the clock grows up/out into empty space, its bottom stays pinned.
- [x] `ClockCornerHandle.kt` (top-left + top-right). Handle geometry is a pure function of the clock's natural box (from primitive `IntSize`/`Offset` state, not the reused `LayoutCoordinates` object) × the live scale — no frame lag, no jump on release. First-move grab-offset capture so it doesn't pop.
- [x] `liveScale: Float?` (absolute, held until the committed value round-trips) — no scale collapse/flicker on release.
- [x] Long-press → `ClockAdjustSheet.kt` ("Adjust size & position" / "Edit … styles"); `dragModeEnabled` → `ClockAdjustMode` enum (`NONE`/`MENU`/`ADJUST`). **One combined `ADJUST` mode** shows the move handle *and* the two resize handles together — either can be used, and dragging the clock upward with the move handle **shrinks it to fit in real time** (`effectiveClockScale` clamps to `maxScaleThatFits` live *only while in `ADJUST`*), persisting the shrunk scale on release. Exits only on tap-away from all handles.
- [x] Fit math (`maxScaleThatFits`) runs **only** while dragging a handle and once per style/position change (`LaunchedEffect`-keyed one-shot that re-clamps + persists only if the saved scale actually clips). `HOME_CLOCK_MIN_SCALE`/`MAX_SCALE` = `0.5`..`2.0`; the max also reserves room for the top handle to clear the status-bar / notification-shade strip.
- [x] Appearance: fade in once (M3 emphasized-decelerate, 250ms, draw-phase alpha) after the measured box holds steady for 120ms — waits out the variable-font remeasure so the clock doesn't visibly "grow from the bottom".
- [x] Unit tests green (`SettingsRepositoryTest`/`FacetRepositoryTest`); instrumented `FacetDatabaseMigrationTest.migration14To15...` covers all three columns.
- [x] `clockAccentColorOption` + `clockDateStyle` (Full/Condensed) global + per-facet.
- [x] 16 shape-based clock templates in `ClockTemplates.kt` + shared `HourText`/`MinuteText`/`SeparatorText`/`TemplateDateText` parts; clock hit-box narrowed to the rendered content only.

- [x] `HomeScreen` no longer takes a Room entity — `clockPositionOwningFacet: FacetEntity?` → `clockPositionOwnerFacetId: Long?` (all the composable needed). `HomeUiState`/`HomeViewModel` keep the entity internally.

---

## "Switch Facets" vs "Manage Facets" — split the two carousel entry points

The one `FacetCarouselScreen` is now entered in one of two modes (`FacetCarouselMode`, param on the screen; default `SWITCH`):

- **`SWITCH`** — Home long-press (`FacetDestinations.FACET_CAROUSEL`, unchanged route). Header renamed `"Facets"` → `"Switch Facets"`. The swipeable carousel and its in-place `"Reorder"` link are unchanged.
- **`MANAGE`** — Settings → Facets (new `FacetDestinations.FACET_MANAGE` route; `SettingsScreen`'s `onViewFacets` now points here instead of `FACET_CAROUSEL`). Opens straight into the existing reorderable list (`isReordering` starts `true`), header `"Manage Facets"`, no carousel and no separate `"Reorder"` link. The back button always exits the screen (to Settings) rather than toggling out of reorder mode, and the `isReordering` `BackHandler` is disabled in this mode for the same reason. `onFacetApplied`/`onNavigateToSettings` are unused here (no card-apply, no in-carousel Launcher-settings row) — wired to no-ops in `FacetNavHost`.

- A `"Drag to re-order facets"` hint (`bodySmall`/`Muted`, testTag `facet_reorder_hint`) is pinned as the first `LazyColumn` item above the facet rows in `FacetReorderList` — shows in both the `MANAGE` list and the `SWITCH` carousel's in-place reorder view.

- Tests: `FacetCarouselScreenTest` — `switchModeShowsTheSwitchFacetsHeaderOverTheCarousel`, `manageModeOpensDirectlyToTheReorderListTitledManageFacets` (also asserts the hint text), `manageModeBackButtonLeavesTheScreenRatherThanExitingReorder`; `setContent` gained a `mode` param. Full `FacetCarouselScreenTest` (19 cases) green on `Medium_Phone_API_36.1`. New light+dark `@Preview` for the manage mode.

### Switch Facets — Launcher-settings row visibility + bottom-cluster layout

Follow-up from an on-device dark-mode screenshot review of the `SWITCH` carousel:

- **`LauncherSettingsRow` was invisible in dark mode** — a bare `Surface` fill on `CarouselBackdrop` is a ~7-luminance-unit delta on OLED. Now carries the `SettingsCard` lift (`shadow(4.dp)` + `Surface` + a `border(1.dp, Ink.copy(alpha = 0.14f))` — a real value, not the 7%-alpha `Hairline`), a leading `Icons.Default.Tune` glyph (`Muted`; `Tune` not a gear, so it doesn't read as a second per-facet "settings" control), and an `Ink` (was `Muted`) chevron.
- **Dead vertical band between the per-facet gear/trash row and the dots** — root cause was the pager taking `weight(1f)` (all spare height) while each page drew at a top-anchored `graphicsLayer` scale, so the bottom ~14.5% of the over-tall pager rendered empty. Removed the `graphicsLayer` page scale entirely and made the preview card a **true scale model of the device screen**:
  - `CAROUSEL_CARD_SCALE = 0.55f` — the card is that fraction of the screen's width *and* height, so it keeps the current phone's own aspect ratio (`BoxWithConstraints` in `FacetPreviewPage` now carries `Modifier.aspectRatio(screenWidthDp / screenHeightDp)`, passed in as `screenAspectRatio`).
  - The horizontal inset (neighbour-card peek) is derived: `screenWidthDp * (1 − scale) / 2`. `CAROUSEL_PAGE_INSET` constant deleted.
  - The pager is content-sized — `height = screenHeightDp * scale + CAROUSEL_PAGE_CHROME_HEIGHT` (name row + gear/trash row) — not `weight(1f)`, so dots + Launcher-settings pack directly beneath it, with a trailing `Spacer(Modifier.weight(1f))` collecting surplus at the bottom. `CAROUSEL_HEIGHT_FRACTION` never shipped; `CARD_SCALE` deleted; `FacetPreviewPage`'s `contentScale` divides by `maxHeight` directly.
- Verified on `emulator-5554` in dark mode: row stands out, gap gone, the card is now visibly screen-shaped, content hugs the top. `FacetCarouselScreenTest` 19/19 green (`launcherSettingsRowInvokesTheCallback` extended to assert the row's title/subtitle render).

### Follow-ups — header gutter + Settings → Facets transition

- **Header now uses the app-standard 24dp horizontal gutter** (was a bespoke 20dp). The
  back-chevron + title row is extracted to `CarouselHeaderTitleRow` (shared by the loaded
  screen and the brief empty state); the Launcher-settings row and the reorder list moved
  20dp → 24dp to stay aligned with it.
- **Settings → Facets no longer flashes the wallpaper/Home** on the way in. Two causes:
  (1) `FacetNavHost`'s route transitions were slide **+ crossfade** — a fading layer over the
  wallpaper-showing (transparent) window briefly reveals the wallpaper; removed `fadeIn`/
  `fadeOut`, leaving a pure slide (two opaque screens sliding past each other always cover the
  full width). (2) `FacetCarouselContent`'s empty state (`facets` still loading) painted
  nothing; it now paints the backdrop + `CarouselHeaderTitleRow` so the screen is opaque and
  reads as "arrived" during the slide. `FacetCarouselScreenTest` 19/19 still green (the empty
  state deliberately omits the `facet_carousel_screen` tag the test helper waits on).
- **Page background now `SurfaceContainer`** (was the dimmer, carousel-only `CarouselBackdrop`
  token) — matches every settings-style screen (`SettingsScreen`, `FacetSettingsScreen`, …),
  so navigating Settings ↔ Switch/Manage Facets no longer steps to a darker surface. The
  `CarouselBackdrop` / `CarouselBackdropDragging` tokens are now unused.
- **Preview cards (and the Add-facet card) now carry the `SettingsCard` lift** —
  `shadow(4.dp)` + `border(1.dp, Hairline)` on the `extraLarge` shape — since the
  `Surface`-on-`SurfaceContainer` tone step alone is too small to read as raised.

---

## Hub — "Can't show content" / widgets placed 1×1 (found via on-device Samsung logs)

Two bugs, both in how the Hub sizes hosted `AppWidgetHostView`s. Confirmed against `dumpsys
appwidget` (Facet's widgets had `appWidgetSizes=[]` while every other launcher's were populated)
and logcat (Samsung's Glance clock widget throwing `NoSuchElementException` mid-recomposition).

- **`OPTION_APPWIDGET_SIZES` was never set** → modern RemoteViews/Glance widgets (Samsung clock,
  battery, weather, …) do `sizes.first()` on an empty list and fail to render, so the host shows
  "Can't show content". `AppWidgetHostView.updateAppWidgetSize` leaves it `[]` on One UI. New
  `AppWidgetRepository.updateWidgetSize(appWidgetId, w, h)` calls `appWidgetManager.updateAppWidgetOptions`
  with an explicit `[SizeF(w, h)]` (+ min/max); `HubWidgetTile` now calls it (via a new
  `onSizeChanged` lambda threaded through `HubScreen`/`HubGrid`) on every size change, guarded on
  `w > 0 && h > 0`, as the *last* write so it wins the merged options bundle.
- **Widgets were placed 1×1** because the span came from `minWidth`/`minHeight` only — a modern
  widget declares those near 0 and its real intent in `targetCellWidth`/`targetCellHeight`
  (API 31+). New `AppWidgetRepository.defaultSpanFor(info)` prefers the target cells, clamped to
  `HUB_COLUMNS`, and falls back to the dp math. Used by both the picker display
  (`getWidgetProviderOptions`) and placement (`HubWidgetPickerViewModel.finishPlacing`, which
  dropped its own copy of the dp math and its now-unused `@ApplicationContext`).
- Tests: `AppWidgetRepositoryTest` (`updateWidgetSize` sets a non-empty `OPTION_APPWIDGET_SIZES`
  / ignores a 0-size tile; `defaultSpanFor` prefers/clamps target cells, falls back to minWidth);
  `HubWidgetPickerViewModelTest` updated for the new constructor + `defaultSpan` stub. Full unit
  suite + all `ui.hub` and `ui.launcher` instrumented tests green on `emulator-5554`.
- **Note:** the span fix only affects *newly added* widgets — a widget already saved at 1×1 keeps
  its stored span until removed + re-added (or resized).

---

## Facets: Manage Facets is its own screen; Switch Facets is a translucent overlay

Follow-up to the `FacetCarouselMode` split — the user wanted `MANAGE` to be a *real* settings
screen (not a mode of the carousel sharing its ViewModel), and the `SWITCH` carousel restyled.

- **`ManageFacetsScreen` + `ManageFacetsViewModel` are new, standalone.** The VM depends only
  on `FacetRepository` + `SettingsRepository` (no `ObserveFacetPreviewsUseCase` — the reorder
  list never needed the expensive per-facet preview flow the carousel runs). The screen uses
  `StickyHeaderLayout` + a `DockSettingsHeader`-shaped header on opaque `SurfaceContainer`, like
  every other settings screen. `FacetReorderList`/`FacetReorderRow`/`AddFacetRow` moved
  here from `FacetCarouselScreen.kt`; all `facet_reorder_*` test tags preserved.
- **`FacetCarouselScreen` is SWITCH-only now** — `FacetCarouselMode` enum, `mode`/`onBack`
  params, `isReordering` state and the in-place reorder branch are all gone;
  `FacetCarouselViewModel.reorderFacets`/`renameFacet` deleted (no callers left).
- **Carousel restyle:** background is `SurfaceContainer.copy(alpha = 0.6f)` — a translucent
  overlay; Home shows through. The header is replaced by a single right-aligned `SecondaryButton`
  ("Reorder" — themed `OutlinedButton`, `CircleShape`, `Ink @14%` border, `Surface` fill,
  disabled with one facet) that navigates to `FACET_MANAGE`. No back button — system back /
  tapping a card is the way out. The "Launcher settings" row is unchanged.
- **`FacetNavHost`:** `FACET_MANAGE` → `ManageFacetsScreen`. `FACET_CAROUSEL` gets its own
  `fadeIn + scaleIn(0.92)` / `fadeOut + scaleOut(0.92)` transitions (200ms), and the NavHost-level
  slide transitions return `EnterTransition.None`/`ExitTransition.None` when the counterpart route
  is `FACET_CAROUSEL` — so Home holds still and the carousel reads as an overlay on top of it.
- Tests: new `ManageFacetsViewModelTest` (4) + `ManageFacetsScreenTest` (6, the moved reorder
  tests); `FacetCarouselScreenTest` dropped the 7 MANAGE/in-place-reorder tests, gained
  `reorderButtonNavigatesToManageFacets` / `reorderButtonIsDisabledWithOnlyOneFacet`. Full
  unit suite + all 40 `ui.facets` instrumented tests green on `emulator-5554`; verified the
  carousel/overlay + Manage Facets screen on-device.
- **Follow-up:** the Hub's translucent-overlay base colour switched `DrawerOverlay` → `SurfaceContainer`,
  and then the App Drawer's did too — so the Hub, App Drawer, the Switch Facets carousel, and
  every settings page all share one base tone (`SurfaceContainer`); each keeps its own alpha.
  `DrawerOverlay` is now unused.

---

## Secondary actions use a shared tonal-accent square button (`TonalButton`)

The carousel's *Reorder*, the Hub header's *Add*, and the Hub empty-state's *Add widget* were
three different one-off treatments (a bordered `OutlinedButton`; plain clickable text; an
`Accent`-filled `Box`). Unified into one component, `ui/components/TonalButton.kt`:

- **M3 filled-tonal role** — accent-tinted container + `Accent` text/icon. This app maps no
  `secondaryContainer` slot, so the fill is `Accent.copy(alpha = 0.28f).compositeOver(Surface)`
  — composited to an **opaque** colour so the button looks the same on the translucent Switch
  Facets scrim, straight on the wallpaper (Hub), or on an opaque card. A `1dp` `Accent @ 32%`
  hairline keeps the edge defined on any background. (First cut used a bare `alpha` fill and was
  invisible on the translucent carousel — a translucent layer over a translucent scrim.)
- **M3 Expressive square shape** — `MaterialTheme.shapes.medium` (12dp), a **deliberate
  departure** from `CLAUDE.md`'s "buttons = Full/`CircleShape`" rule, per direct request. M3
  Expressive supports both round and square button shapes; noted in the component KDoc.
- `FacetCarouselScreen`'s inline `SecondaryButton` deleted; `HubHeader` / `HubEmptyState`
  dropped their `clickable`/`background`/`Accent`/`Surface` imports.
- Tests: existing `hub_add_button` / `hub_empty_add_widget` / `facet_carousel_reorder` test
  tags preserved, so `ui.hub` + `ui.facets` instrumented suites cover it unchanged.

---

## Switch Facets moves from a Home long-press to a left swipe

Per direct UX report: long-pressing "empty" Home space to reach the Switch Facets carousel was
too hard to land — the clock/calendar own the top, the favorites `AppRow`s take a full-width hit
slab through the middle, and the dock owns the bottom, leaving only thin gutters that the user
can't see. The gesture was invisible *and* its target was fragmented.

- **`HomeDrawerRoute`** — the outer `detectTapGestures(onLongPress = …)` on the Home surface is
  gone (its import too). The existing horizontal-axis branch of the Home `detectDragGestures`
  now splits by direction: a net-**rightward** drag drives the Home↔Hub follow-finger axis as
  before; a net-**leftward** drag from a closed Hub accrues in a new `homeHorizontalDragDistance`
  and, on release, calls `onNavigateToFacetCarousel()` once it has cleared
  `COMMIT_TRAVEL_FRACTION` (20%) of the container width — the same fraction the Hub itself
  commits on. The Hub axis is never fed a leftward delta from a closed state, so its `progress`
  stays exactly `0f` and the release check is unambiguous. The carousel is still a NavHost
  destination reached by a fling-to-navigate trigger (not a follow-finger panel), but its
  transition now **slides in from the right** like the Hub (see the transition note below).
- **Symmetry:** Home horizontal swipe is now fully assigned — left = Switch Facets (`3a`),
  right = Hub (`F5`). README's "facet switching does not use swipe on the home screen" line and
  the gestures table were revised.
- Tests (`HomeDrawerRouteTest`): the 3 long-press cases
  (`holdingStillForTheFullDurationNavigatesToTheFacetCarousel`,
  `releasingBeforeTheDurationDoesNotNavigate`, `longPressBesideTheClockStillReachesTheFacetCarousel`)
  removed; replaced with `swipingLeftPastThresholdOpensTheFacetCarousel`,
  `releasingALeftDragBeforeThresholdDoesNotOpenTheFacetCarousel`,
  `swipingLeftOpensTheFacetCarouselNotTheHub`, and
  `aDiagonalSwipeMostlyLeftOpensTheFacetCarouselNotTheDrawer` (axis-lock guard, mirroring the
  existing mostly-right case). `HomeScreenTest`'s long-press-beside-clock / context-menu cases
  are unaffected — those exercise `HomeScreen`'s own clock/`AppRow` detectors, not this route.

### …and a rightward swipe on the carousel dismisses it back to Home

The mirror gesture: left swipe on Home opens the carousel, right swipe on the carousel closes it
(applies nothing — same as system back). Handles the two "the pager isn't in the way" cases the
user called out:

- **`FacetCarouselScreen`** gains an `onDismiss` param (wired to `navController.popBackStackSafely()`
  in `FacetNavHost`). Two mechanisms feed it, kept from overlapping because the pager consumes its
  own drags:
  - **Empty space** (the scrim around/below the pager — Reorder row, dots, settings row, trailing
    spacer, insets): a `detectHorizontalDragGestures` on the outer `Column` accumulates signed x
    and calls `onDismiss()` on release once it passes `DISMISS_SWIPE_FRACTION` (15%) of screen
    width. A drag that starts on the pager gets consumed there, so the parent detector cancels
    and never double-fires.
  - **On the first facet's card**: a `NestedScrollConnection` on the same `Column` intercepts
    rightward pre-scroll while `pagerState.currentPage == 0` (nowhere to browse to), takes the
    delta before the pager makes it a dead overscroll, and dismisses in `onPreFling` on a
    decisive drag distance or a rightward fling past `DISMISS_FLING_VELOCITY`. Swiping right from
    any *other* page still browses to the previous facet, untouched.
- Tests (`FacetCarouselScreenTest`): `swipingRightOnTheFirstFacetCardReturnsHome`,
  `swipingRightInEmptySpaceReturnsHome`,
  `swipingRightOnANonFirstFacetCardBrowsesInsteadOfDismissing`. `onDismiss` added to the test
  helper's defaults.

### …then a same-session animation tweak (slide instead of fade), superseded below

Briefly: `FACET_CAROUSEL`'s `NavHost` transition changed from `fadeIn + scaleIn(0.92)` to
`slideInHorizontally { it }` / `slideOutHorizontally { it }`, so the carousel slid in from the
right and back out, matching the left-swipe-in / right-swipe-out gesture direction, on the same
340ms `cubic-bezier(.32,.72,0,1)` `animationSpec` the drawer/Hub use. Still a fling-to-navigate
`NavHost` destination at that point, not a follow-finger panel. Superseded minutes later, same
session, by the section below — kept here only so the "Real bug found" trail stays intact.

### …then made an actual follow-finger panel, like the Hub — superseding both sections above

Per direct follow-up request: "make it a follow-finger panel like the hub." The carousel is no
longer a `NavHost` destination at all — it's merged into `HomeDrawerRoute` exactly like the Hub,
permanently composed and just offset off-screen when closed, driven by a third `SwipeAxisState`
(`facetAxis`, sized by `containerWidthPx` like `hubAxis`) instead of a threshold-then-navigate
callback.

- **`HomeDrawerRoute`** — new params `onNavigateToFacetSettings: (Long) -> Unit` and
  `onNavigateToManageFacets: () -> Unit` replace `onNavigateToFacetCarousel`; new
  `facetViewModel: FacetCarouselViewModel = hiltViewModel()` param, mirroring `hubViewModel`.
  `facetAxis` gets folded into every place `hubAxis`/`drawerAxis` already were: the
  home-pressed-event close-all effect, the focus-clear effect, the clock-adjust-cancel effect,
  and `BackHandler`.
  - **Two-axis ownership on Home's own horizontal drag**: previously the branch only ever fed
    `hubAxis` (rightward) and separately accumulated a distance for a release-time carousel
    navigation. Now it feeds `hubAxis` *or* `facetAxis` live, frame by frame. Whichever axis a
    gesture's first horizontal delta engages (`dragActive` flips true) keeps owning every
    subsequent frame regardless of a direction reversal — `dragBy()` already tolerates negative
    deltas fine (walks progress back down), so there's no risk of a wavering swipe handing control
    to the other panel mid-drag. `onDragEnd`/`onDragCancel` settle whichever of the two is
    `dragActive` (exactly one, never both, per gesture).
  - **New panel `Box`**, drawn last (frontmost — it used to sit above everything as a modal
    `NavHost` destination, and staying frontmost here preserves that read), offset
    `(1f - facetAxis.progress.value) * containerWidthPx` — 0 at rest-open, full width at
    rest-closed, the mirror of the Hub's own `(hubAxis.progress.value - 1f) * containerWidthPx`.
    Its own `detectHorizontalDragGestures` handles empty-space swipes back to Home exactly like
    the Hub's own Box does.
  - `FacetCarouselScreen`'s three "leaves the panel" callbacks (`onFacetApplied`,
    `onEditFacet`, `onReorderFacets`) now resolve locally instead of navigating away from a
    destination: applying closes `facetAxis` (`coroutineScope.launch { facetAxis.close() }`);
    edit-facet/reorder call the new nav params directly and *don't* close the panel first — like
    the Hub's own add-widget-picker overlay, navigating to a child screen and coming back (via its
    own `onBack`) lands you right back in the still-open carousel, not back at Home.
- **`FacetCarouselScreen`** no longer owns opening/closing at all (no more `onDismiss`,
  `DISMISS_SWIPE_FRACTION`/`DISMISS_FLING_VELOCITY` constants, or its own outer
  `detectHorizontalDragGestures` for empty space — that's the host `Box`'s job now). It keeps only
  the one thing it's uniquely positioned to detect: a rightward drag landing *on the pager* while
  settled on the first page, which the pager would otherwise eat as a dead overscroll. Two new
  params, `onDismissDrag: (deltaPx: Float) -> Unit` and `onDismissDragEnd: () -> Unit`, forward
  that `NestedScrollConnection`'s raw deltas up to the host, which feeds them into the very same
  `facetAxis.dragBy(-deltaPx)` / `.settle()` the empty-space Box uses — so that specific gesture
  is genuinely follow-finger too, not a separate threshold check. The `gestureMovedPager` latch
  (don't treat flinging *through* page 0 from another page as a dismiss) is unchanged.
- **Real bug found**: the preview cards are a *genuine live copy* of Home's own content (clock,
  FAVORITES/RECENTS/MOST USED list, dock) — unlike the Hub, whose content never happened to
  textually collide with Home's. Being permanently composed (not a `NavHost` destination torn
  down when not navigated to) put that content in the semantics tree even while fully closed and
  off-screen, so `onNodeWithText("FAVORITES")` in `HomeDrawerRouteTest` started matching two nodes
  (Home's real list and the carousel card's copy) and multiple pre-existing tests broke. Fixed
  with `Modifier.clearAndSetSemantics {}` on the panel `Box`, applied only while
  `!isFacetOpen` — Compose's own `assertIsNotDisplayed()` already treats "node doesn't exist" as
  passing, so this needed no test-side workaround, just the production fix.
- **`FacetNavHost`**: `FACET_CAROUSEL` destination, its `FacetDestinations` constant, and the
  `fadeIn`/`fadeOut`/`fadeSpec` machinery that existed only for it are all deleted. The NavHost-
  level transitions lose their `FACET_CAROUSEL`-conditional `EnterTransition.None`/
  `ExitTransition.None` branches (nothing needs them any more) and go back to the plain slide
  every other destination uses.
- Tests: `HomeDrawerRouteTest` swaps its lambda-based assertions
  (`onNavigateToFacetCarousel`/`onDismiss` firing) for panel-visibility ones
  (`onNodeWithTag("facet_carousel_screen").assertIsDisplayed()/.assertIsNotDisplayed()`),
  matching how the Hub's own tests already worked; gains `facetViewModel` construction in its
  `setContent` (sharing the same `facetRepository`/`settingsRepository`/app-backed repos as
  `homeViewModel`/`launcherViewModel`, same reasoning as that block's own doc), plus new cases
  `swipingRightInEmptySpaceOnTheOpenCarouselReturnsToHome`,
  `swipingRightOnTheFirstFacetCardWhileOpenReturnsToHome`,
  `systemBackClosesAnOpenFacetCarousel`. `KeyboardDismissalTest` (unrelated to carousel
  behavior) just gains a throwaway `facetViewModel` so it still constructs.
  `FacetCarouselScreenTest`'s three dismiss-drag tests are renamed and reworked to assert the
  `onDismissDrag`/`onDismissDragEnd` forwarding contract directly instead of a boolean
  "dismissed" flag; its empty-space case is deleted outright (that gesture no longer lives in this
  screen — `HomeDrawerRouteTest`'s new empty-space case covers it at the host level instead).
  Full suite: 354 JVM unit tests green; 43/43 targeted instrumented tests
  (`FacetCarouselScreenTest` + `HomeDrawerRouteTest` + `KeyboardDismissalTest`) green on
  `emulator-5554` (one run crashed mid-suite from a concurrent `adb install` racing the same
  device from unrelated work elsewhere — not a regression, confirmed clean on immediate retry).

---

## Dock gains per-facet overrides (mirrors per-facet favorites) + a live preview on the facet settings screen

The dock was the last Home surface with no per-facet override — favorites, app-list
position/presentation/content-mode, clock/calendar design, and calendar selection all already
resolve per active facet, but every facet shared one dock (`DockAppRepository`/`dock_apps`)
and one `dockDisplayMode`. Now a facet can override both, behind a single **DOCK** Inherit/
Override card on its settings screen, exactly the shape of the existing **APPS LIST** card.

- **Schema v15 → v16** (`Migrations.MIGRATION_15_16`, migration test added):
  `facets.overrideDock` (`INTEGER NOT NULL DEFAULT 0`) + `facets.dockDisplayMode`
  (`TEXT NOT NULL DEFAULT 'ICONS'`), plus a new `facet_dock_apps` table — the per-facet
  counterpart to the global `dock_apps`, structurally identical to `favorite_apps` (facetId
  FK, cascade delete, unique `(facetId, packageName, activityName)` index).
- **`FacetDockAppRepository`** (new) is to `DockAppRepository` exactly what `FavoriteAppRepository`
  is to `DefaultFavoriteAppRepository`: `observeDockAppsForFacet` / `observeDockAppsForFacets`
  (live-hydrated, uninstall-collapsing), `add`/`remove`/`replace`/`reorder`, plus raw/restore for
  backup. `DockAppRepository` is unchanged and stays the launcher-wide default dock. Tests:
  `FacetDockAppRepositoryTest` + `FacetDockAppDaoTest` mirror the favorites ones.
- **`ObserveHomeScreenStateUseCase`** resolves the dock per active facet via `flatMapLatest`
  (mirroring `observeAppListItems`); `HomeUiState.activeDockDisplayMode` resolves the style,
  consumed by `HomeDrawerRoute`. **`ObserveFacetPreviewsUseCase`** gained `FacetPreviewData.dockApps`
  so every carousel preview card renders its own facet's effective dock (previously all cards
  shared one) — `FacetCarouselViewModel` dropped its `DockAppRepository` dependency and its
  shared `dockApps`/`dockDisplayMode`, replaced by a per-facet `dockDisplayMode(facetId)`
  resolver. Tests added to both use-case test classes.
- **`FacetSettingsScreen`** — new **DOCK** section (`DockSection`): Inherit/Override card, a
  `LabeledDropdownRow<DockDisplayMode>` "Display style", a "Select dock apps" row →
  `DockAppPickerScreen` scoped to this facet, and a drag-reorder list. The old
  `FavoritesReorderList` was generalized to `AppReorderList(apps, onReorder, testTagPrefix)` and
  is now shared by favorites + dock. `FacetRepository.updateOverridingDock`/`setDockDisplayMode`
  added; `setOverridingDock` seeds the display mode + copies the default dock in when the
  facet's own is empty (mirrors `setOverridingApps`).
- **`DockAppPickerViewModel`** is now facet-aware exactly like `FavoritesPickerViewModel` —
  `SavedStateHandle`'s `facetId` (`NO_ACTIVE_FACET_ID` sentinel = global) routes reads/writes
  to `FacetDockAppRepository` or `DockAppRepository`. Route `dockPicker?facetId={facetId}`
  + `FacetDestinations.dockPicker(facetId)`. The global picker's header is now "Default dock"
  (matching "Default favorites"); the facet one stays "Dock".
- **Backup**: `BackupFacet` gained `overrideDock` / `dockDisplayMode` / `dockApps` (all
  defaulted — a v1 backup still deserializes, inheriting the launcher-wide dock);
  `CURRENT_BACKUP_VERSION` 1 → 2. Export/import round-trip each facet's dock. Tests updated.
- **Live preview card** (requested mid-build): `FacetPreviewCard` at the top of the facet
  settings screen — same idea as Settings → Appearance's own preview, scoped to one facet. Real
  production `AppRow` + `DockIcon` over the device's actual `WallpaperBackground`, driven by the
  facet's *effective* values so toggling any override updates it live. Shows the real apps that
  will appear on Home: `previewAppListItems` (favorites/recents/most-used, resolved via
  `ObserveFacetPreviewsUseCase` — now injected into `FacetSettingsViewModel` alongside
  `WallpaperRepository`) and `effectiveDockApps`.
- Instrumented tests: `FacetSettingsScreenTest` (dock card defaults to inherit, override
  enables its rows + persists, "Select dock apps" navigates, preview card renders near the top);
  `DockAppPickerScreenTest` (`facetScopedPickerTitlesItselfDockAndWritesToThatFacetsOwnDock`,
  plus the global picker's title assertion updated to "Default dock").
- `CleanUpUninstalledAppsUseCase` also purges `facet_dock_apps` on a genuine uninstall.

---

## Facet settings restructured — Apps-list & Dock reuse the launcher's own settings screens

Follow-up to the above: the per-facet settings screen had grown into one long inline scroll
(preview + APPS LIST card + DOCK card + CLOCK card). Restructured to match how `ClockStyleGallery`
is already reused for both the global and per-facet cases:

- **`FacetSettingsScreen`** is now a short nav list — Rename, then per area an
  `InheritOverrideCard` (the override switch *stays here*) + a single row into that area's own
  screen. No preview, no inline controls.
- **`HomeAppsListSettingsScreen` / `HomeAppsListSettingsViewModel` and `DockSettingsScreen` /
  `DockSettingsViewModel` are now facet-aware** via the `SavedStateHandle` `facetId` sentinel
  (`NO_ACTIVE_FACET_ID` = the launcher-wide default), exactly like `ClockStyleGalleryViewModel`.
  A real id reads/writes that facet's own row + `FavoriteAppRepository` /
  `FacetDockAppRepository`; no id writes `SettingsRepository` + the default lists. Routes gained
  `?facetId={facetId}` + `FacetDestinations.homeAppsListSettings(id)` / `dockSettings(id)`
  builders; `FacetSettingsScreen`'s rows navigate with the facet id, the global Settings
  screen without. `HomeAppsListSettingsScreen(onEditDefaultFavorites)` → `onEditFavorites`.
- **`HomeSurfacePreview`** (`ui/components/`) — the "this is how Home looks" card extracted from
  the two copies that existed (`AppearancePreviewCard`, the facet `FacetPreviewCard`) into
  one shared component (real `AppRow`/`DockIcon` over `WallpaperBackground`; empty `appList` or
  `dockApps` omits that surface). Now on **four** screens: Appearance, Home Apps List (app rows;
  `previewApps` = the favorites in Favorites mode, `SelectPreviewAppsUseCase` sample otherwise),
  Dock (dock icons; the real `dockApps`), and each of those in its facet-scoped form.
- **`FacetSettingsViewModel` slimmed** — dropped the preview flow, `WallpaperRepository`,
  `ObserveFacetPreviewsUseCase`, and the per-control setters (those live on the reused VMs
  now); keeps `uiState` (for the override subtitles + seed values), `renameFacet`, and the
  three `setOverriding*` seed-and-toggle actions. `FacetSettingsComponents.kt` holds the shared
  header/row/section-header helpers.
- Tests: `FacetSettingsScreenTest` rewritten for the nav list; `HomeAppsListSettingsScreenTest` /
  `DockSettingsScreenTest` / their VM tests cover both the global and a facet-scoped instance
  (setter routing, preview renders); `AppearanceSettingsScreenTest` unchanged.

---

## Dock Folders (F-Folders) — Phase 1: global Dock scope

Grouping several apps into one Dock slot. Full design discussion (including the competitor
reference that shaped the final shape) lives in the feature branch's own plan doc; this entry
records what actually landed. Built on `feature/folders`.

**Scope for this phase**: Dock only (global, not per-facet) — Favorites folders and per-facet
folder overrides are explicitly deferred, mirroring how every other Dock feature in this app
shipped global-first (see the per-facet Dock override entry above). Folder creation/membership is
driven entirely through the existing long-press context menu — no drag-to-merge gesture (ruled
out early: this app uses that gesture nowhere else, and it's the riskiest mechanic to get right)
— plus a "+ Create folder" entry directly in the Dock's own picker screen.

### Data layer
- **Schema v17 → v18** (`Migrations.MIGRATION_17_18`): new `dock_folders` (id, name, position —
  occupies one slot in the *same* ordering space as `dock_apps.position`, merged in Kotlin, not
  enforced across tables by Room) and `dock_folder_apps` (folderId FK cascade, packageName,
  activityName, position — membership, unique per folder+component). No facetId on either table
  yet (global scope only, matching `dock_apps` itself having none).
- **`DockFolderDao`** — `observeAllWithApps()` (a `@Relation` query returning `DockFolderWithApps`),
  folder/membership CRUD, `deleteEmptyFolders()` (a folder is never shown/left empty), and a
  dedicated `updateFolderPosition()` — deliberately a plain `UPDATE`, not `upsertFolder`'s
  `REPLACE` conflict strategy, since `REPLACE` on an existing row is a delete-then-insert at the
  SQLite level and would cascade-delete the folder's own membership rows via its FK.
- **`data/model/DockItem.kt`** — `sealed interface DockItem { SingleApp(app) | Folder(id, name, apps) }`,
  the shared vocabulary threaded through every surface below instead of raw `AppInfo`.
- **`DockAppRepository`** gained `observeDockItems()` (merges `dock_apps` + `dock_folders`-with-apps
  + installed apps, sorted by position; a folder emptied by an uninstall is dropped from the
  emitted list entirely rather than shown empty) plus `createFolder`/`addAppToFolder`/
  `removeAppFromFolder`/`renameFolder`/`ungroupFolder`/`reorderDockItems`. `removeByPackage`
  (uninstall cleanup) now also purges folder membership and deletes emptied folders — no separate
  change needed in `CleanUpUninstalledAppsUseCase`, since it already calls this method.
- **`ObserveQuickAddStateUseCase`**'s dock-capacity check now counts `DockItem`s, not raw apps — a
  folder costs exactly one Dock slot regardless of how many apps it holds.
- Backup/restore, per-facet Dock override, and the Dock's Settings picker/reorder screens are
  **not yet wired to folders** in this phase (see "Deferred" below) — folders don't yet round-trip
  through export/import, and a facet overriding its own dock always sees plain apps.

### UI
- **`AppContextMenu`** gained a third page (alongside its existing single page) via
  `AnimatedContent` — mirrors `ContactConnectionsSheet`'s own main-list/disambiguation-page slide,
  so folder-adding stays inside the *same* `ModalBottomSheet` instance rather than closing one
  sheet and opening another. A new **"Add to folder"** row (`folderCandidates` non-null) opens it:
  "+ Create new folder" first (opens `RenameDialog`, named at creation), then every existing
  folder. A separate **"Remove from folder"** row (`removeFromFolderId` non-null) replaces it when
  the menu is opened from inside a folder's own contents sheet — the two never both apply to the
  same app.
- **`DockFolderSheet`** (new, `ui/home/`) — tapping a folder tile opens this. Deliberately the same
  hand-rolled bottom-sheet idiom as `ContactConnectionsSheet` (scrim + slide-up, dynamic height,
  `CardDivider` rows), not a `Dialog`/`Popup` look — matches this app's own established shape for
  "tap something small, reveal what's inside," as opposed to `AppContextMenu`'s real
  `ModalBottomSheet` (reserved for long-press menus). Wrapped in a full-screen `Dialog` so it can
  be hosted locally from `DockIcon` without threading extra state through `HomeScreen`'s already
  large prop surface. Header has an inline rename affordance; each app row's long-press reuses
  `AppContextMenu` a third time with "Remove from folder".
- **`DockFolderContextMenu`** (new, `ui/components/`) — long-pressing the folder *tile itself* (not
  an app inside it) opens this instead of `AppContextMenu`: Rename / Ungroup only, since a folder
  has no app-info/uninstall/shortcuts of its own. Ungroup returns every member to the Dock as its
  own standalone tile — no destructive "delete folder and its apps" action exists, matching
  platform convention.
- **`HomeScreen`'s `DockIcon`** now takes a `DockItem` instead of raw `AppInfo`, branching into
  `SingleAppDockIcon` (unchanged rendering) or the new `FolderDockIcon` — a `medium`/12dp-shaped
  tile (CLAUDE.md's M3 shape table, tile scale) showing a 2×2 mini-grid of the folder's first 4
  app icons (`FolderTileGlyph`). This rippled into every `DockIcon` consumer: the facet carousel's
  read-only preview cards (`FacetCarouselScreen`) and `ObserveFacetPreviewsUseCase`'s
  `FacetPreviewData.dockApps` (now `List<DockItem>`; a facet's own per-facet dock override still
  maps to plain `SingleApp` entries, since per-facet folders aren't built yet).
- **`AppDrawerScreen`** — `DrawerAppRow`/`DrawerGridTile` (browse mode) and `DrawerSearchResults`
  (which already renders through those same two composables, so no extra plumbing was needed) all
  gained the same `folderCandidates`/`onCreateFolder`/`onAddToFolder` params, so "Add to folder" is
  reachable from every long-press context: Dock, Drawer, and Search alike. Favorites' own `AppRow`
  deliberately does **not** get the row yet — there's no Favorites-folder surface to route into
  until Phase 2.
- **`DrawerViewModel`** gained `dockFolders` (a `StateFlow<List<DockItem.Folder>>`, filtered from
  `DockAppRepository.observeDockItems()`) plus `createDockFolder`/`addToDockFolder`/
  `removeFromDockFolder`/`renameDockFolder`/`ungroupDockFolder` — single-repository logic, so per
  `CLAUDE.md`'s layering rule these call `DockAppRepository` directly rather than going through a
  new domain use case.

### Tests
New `DockFolderDaoTest` (JVM/Robolectric, real in-memory Room — hydration/ordering, cascade delete,
`updateFolderPosition` preserving membership where `upsertFolder`'s `REPLACE` would have wiped it,
`deleteEmptyFolders`/`deleteAllFolders`, unique-index dedup), mirroring `FacetDockAppDaoTest`.
`DockAppRepositoryTest` additions (a hand-written `FakeDockFolderDao` alongside the existing
`FakeDockAppDao`; merge/sort ordering, uninstall-collapse for both standalone apps and folder
members, every CRUD method, reorder across both tables), `ObserveQuickAddStateUseCaseTest`
(item-counted capacity), `AppContextMenuTest` additions (folder page navigation, create/add/remove
flows, back-without-dismiss), new `DockFolderContextMenuTest` and `DockFolderSheetTest`, and a
`HomeScreenTest` case for the folder tile opening its sheet. Full `./gradlew test` (JVM) and
`ANDROID_SERIAL=<emulator-serial> ./gradlew connectedDebugAndroidTest` (the full existing suite,
run once before these additions to confirm zero regressions, then again with them included) both
green on `Medium_Phone_API_36.1`.

### Deferred (not built in this phase)
- **Favorites folders** — `FavoriteFolderEntity`/`FavoriteFolderAppEntity` + a `FavoriteItem`
  sealed type, built as a near-identical twin of everything above (the way `DockAppPickerScreen`/
  `FavoritesPickerScreen` already are today). At that point the "Add to folder" page gains
  "Dock"/"Favorites" sections when opened from Drawer/Search, and `AppRow`'s long-press gains the
  row too.
- **Per-facet folder overrides** — `FacetDockFolderRepository`, mirroring exactly how per-facet
  Dock overrides shipped as a distinct pass after global Dock (see above).
- **Backup & Restore** — dock folders don't yet round-trip through export/import
  (`BackupMapping.kt`/`ExportBackupUseCase`/`ImportBackupUseCase` untouched); `DockAppRepository`
  already exposes `getRawDockFolders()`/`restoreDockFolder()` as the hooks for this, just not
  wired into the backup use cases yet.
- **`DockAppPickerScreen`** still only toggles installed apps — the competitor-informed "+ Create
  folder" entry (and rendering an existing folder as its own row instead of a checkbox) discussed
  in this feature's design pass was **not** built this phase. Folder creation/membership is
  reachable only via the long-press "Add to folder" flow for now.
- **`DockSettingsScreen`'s "Select Dock Apps" checklist** still only toggles installed apps — an
  existing folder can't be added/removed from there (only via the long-press "Add to folder" flow).
  [Fixed since] the reorder row now renders folders (`PlacedItem`-based `DockAppsRow`), and so does
  `HomeSurfacePreview` (both its Dock and Favorites rendering, shared by `DockSettingsScreen`,
  `HomeAppsListSettingsScreen`/`FavoritesReorderList`, and `AppearanceSettingsScreen`) — a folder
  placed in the Dock or in Favorites used to render as its member apps flattened out (Dock preview)
  or silently vanish from the count/preview/reorder list entirely (Favorites, which only queried
  the app-only `observeFavoritesForFacet`/`observeDefaultFavorites` methods, never the
  folder-aware `observeFavoriteItems`/`observeDefaultItems`) — both were reported as real bugs and
  fixed by switching those screens onto the `PlacedItem`-based repository methods end to end.

### On-device verification pass (found and fixed two real gaps)

Manually verified on the `Medium_Phone_API_36.1` emulator, set as the actual default launcher —
create folder → tile renders → tap opens contents → long-press an app inside → remove from
folder → long-press the tile → rename/ungroup, exercised in both Dock display styles (Icons and
Text — both render and open the sheet correctly; an earlier report of "only works in Icons mode"
traced back to leftover corrupted state from an earlier fumbled manual-testing pass, not a real
bug in either display mode). Two real, user-facing bugs were caught and fixed in this pass:

- **`ObserveSettingsScreenStateUseCase`/`SettingsUiState`/`SettingsViewModel`** still read
  `DockAppRepository.observeDockApps()` (the old flat method) — a folder's member apps vanished
  entirely from the main Settings screen's "Dock" row subtitle (`SettingsScreenState.dockApps` →
  renamed `dockItems`, now `List<DockItem>` via `observeDockItems()`). The subtitle itself
  (`dockSummary()` in `SettingsScreen.kt`) now reads "4 Dock Apps, 1 Folder" instead of silently
  under-counting — previously it read "4 of 5" with the folder's app simply missing, which looks
  exactly like data loss to a user, not a cosmetic gap.
- **Settings had no folder management surface independent of long-press** — added a **"Folders"**
  row under HOME & APPS (competitor-informed, matches the Slate-launcher reference from this
  feature's design pass): `FoldersSettingsScreen`/`FoldersSettingsViewModel` (new), listing every
  Dock folder (name + app count), tapping a row opens the same `DockFolderSheet` used from the
  live Dock, and Ungroup sits directly on the row as its own icon button (this app's "few
  always-visible actions, no '...' overflow menu" convention). Route `FOLDERS_SETTINGS` in
  `FacetNavHost.kt`. `DockAppPickerScreen`'s own "+ Create folder" entry (mentioned as
  competitor-informed but not built, above) is still not built — this new screen is a *view/manage*
  surface for folders that already exist, not a second creation path.
- Tests: new `FoldersSettingsScreenTest` (empty state, row rendering, opens `DockFolderSheet`,
  Ungroup fires without opening the sheet, launching an app from the sheet dismisses + fires
  `onAppClick`), a new `SettingsScreenTest` case for the Folders row, and the existing
  `dockRowIsClickableAndReflectsDockAppCount` test's assertion continues to pass unchanged (the
  no-folder case still reads "N Dock Apps", so the format is additive, not a breaking change).
  Full `./gradlew test` and `ANDROID_SERIAL=<emulator-serial> ./gradlew connectedDebugAndroidTest`
  both green after these fixes.

---

## Folders — Phase 2: generalized to Favorites + per-facet overrides, backup/restore wired

Supersedes Phase 1's "Deferred" list above — everything it named as not-yet-built (Favorites
folders, per-facet folder overrides, backup/restore round-trip) landed in this pass, alongside a
model rename that replaced the Dock-only `DockItem` with a shared vocabulary used everywhere a
folder can live. **Phase 1's own `DockFolderDao`/`DockFolderSheet`/`DockFolderContextMenu`/
`DockItem` never actually landed as committed code** — this phase's design (below) is what's
actually in the tree; treat Phase 1's UI/data-layer bullets above as superseded, not as history.

### Data layer
- **Schema v17 → v18** (`Migrations.MIGRATION_17_18`): `folders` (id, name) + `folder_apps` (id,
  folderId FK cascade, packageName, activityName, position; unique index on
  folderId+packageName+activityName) are the folder library itself — global, not per-list. Four
  placement tables point a folder at one slot in a specific list, each just (folderId[, facetId],
  position) with an FK cascade back to `folders`: `dock_folder_placements` (unique on folderId),
  `default_favorite_folder_placements` (unique on folderId), `facet_dock_folder_placements`
  (facetId FK→facets, unique on facetId+folderId), `favorite_folder_placements` (facetId
  FK→facets, unique on facetId+folderId).
- **`data/model/PlacedItem.kt`** — `data class Folder(id, name, apps: List<AppInfo>)` (hydrated,
  live) and `sealed interface PlacedItem { SingleApp(app) | FolderItem(folder) }`, the one shared
  model used by Dock, Favorites, the default Favorites list, and every per-facet override alike.
- **`FolderRepository`** (new) owns folder identity/membership centrally: `observeFolders()`
  (hydrates against installed apps, drops uninstalled members, keeps 0-app folders — never
  auto-deleted for being empty), `createFolder`/`renameFolder`/`deleteFolder` (cascades through
  membership + all four placement tables via FK), `addAppToFolder`/`removeAppFromFolder`/
  `reorderFolderApps`, `removeByPackage` (uninstall cleanup), plus backup hooks
  `getRawFolders()`/`restoreFolder()`/`deleteAllFolders()`.
- **`DockAppRepository`/`FacetDockAppRepository`/`FavoriteAppRepository`/
  `DefaultFavoriteAppRepository`** each gained an item-merging observer (`observeDockItems()`,
  `observeDockItems(facetId)`/`observeDockItemsForFacets(...)`, `observeFavoriteItems(facetId)`/
  `observeFavoriteItemsForFacets(...)`, `observeDefaultItems()`) — combines its existing app DAO +
  the matching folder-placement DAO + `FolderRepository.observeFolders()` + installed apps into
  one `List<PlacedItem>`, sorted by a shared `position` space. Each also gained
  `placeFolder`/`placeFolderInDock`, `removeFolderPlacement`/`removeFolderFromDock`, an
  item-aware reorder (`reorderDockItems`/`reorderFavoriteItems`/`reorderItems`), and
  `replaceItems(facetId, items: List<PlacedItem>)` (renamed from the old app-only
  `replaceDockApps`/`replaceFavorites`) so switching a facet to Override now seeds folder
  placements too, not just apps. `removeByPackage` now also calls
  `folderRepository.removeByPackage`.
- **Per-facet folder overrides are fully wired** — `FacetSettingsViewModel`'s Dock/Favorites
  override toggles read/seed via the new `observeDockItems`/`observeFavoriteItems`/`replaceItems`,
  and `ObserveHomeScreenStateUseCase`/`ObserveFacetPreviewsUseCase` both switch between the
  per-facet observer (override case) and the global one (inherit case) — a facet's own folder
  placements flow into the live Home screen and the facet-carousel preview cards.
- New `AddFolderToDockUseCase`/`RemoveFolderFromDockUseCase`/`AddFolderToFavoritesUseCase`/
  `RemoveFolderFromFavoritesUseCase` resolve the active facet's override exactly like
  `AddAppToDockUseCase`/`AddAppToFavoritesUseCase` already did for apps.

### Backup & Restore — now fully round-trips (was explicitly deferred in Phase 1)
`CURRENT_BACKUP_VERSION` bumped 2→3. `BackupBundle.kt` gained `folders: List<BackupFolder>`
(global, index-referenced like `activeFacetIndex`), `dockFolderPlacements`/
`defaultFavoriteFolderPlacements` (`List<BackupFolderPlacement>`), and `BackupFacet` gained its
own `dockFolderPlacements`/`favoriteFolderPlacements` — all defaulted for a tolerant read of
pre-v3 backups. `BackupFolderPlacement(folderIndex, position)` references the folder library by
index. `ExportBackupUseCase` exports the folder library once with a `folderId→index` map, then
each of the four placement lists through that map; `ImportBackupUseCase` restores folders first
(before facets/dock/favorites — `deleteAllFolders()` cascades every placement table), builds
`newFolderIdByIndex`, then restores each placement list. A placement whose folder no longer
exists on export is dropped rather than exported dangling.

### UI
- **`DockAppPickerScreen`/`FavoritesPickerScreen`** each gained an Apps/Folders tab
  (`AnimatedContent` slide) with a `showFoldersTab: Boolean = true` param — forced `false` from
  `OnboardingScreen` (no folder can exist that early in the flow).
- **`DockSettingsScreen`'s reorder row** now takes `dockItems: List<PlacedItem>` — folders render
  and reorder in Settings (Phase 1 had flagged this as a real gap; closed here).
- **`SettingsScreen`** gained a "Folders" row (subtitle "No folders yet"/"N folder(s)") into
  `FoldersSettingsScreen`; the Dock row's own subtitle now reads `dockSummary(uiState.dockItems)`.
- **`FolderTileContextMenu`** (new, `ui/components/`) replaces the never-shipped Phase-1
  `DockFolderContextMenu` design — long-pressing a folder *tile* opens Rename only at this point
  (the quick Add/Remove Favorites/Dock rows described below landed in Phase 3, not here).
- **`FoldersSettingsScreen`/`FoldersSettingsViewModel`** (new) list the whole folder library from
  `FolderRepository`, navigate to a new **`FolderDetailScreen`** (own drag-reorder +
  "Add to folder" → **`FolderAppPickerScreen`**), with delete gated by `ConfirmDialog`. Wired into
  `FacetNavHost` via new `FOLDERS_SETTINGS`/`FOLDER_DETAIL`/`FOLDER_APP_PICKER` routes.
- **`FolderContentsSheet`** (new, `ui/components/`) — tapping a folder tile anywhere (Home, Dock,
  Favorites) opens this: a hand-rolled `Dialog`-hosted sheet (mirrors `ContactConnectionsSheet`'s
  idiom, not a real `ModalBottomSheet`) showing the folder's live contents, with inline rename and
  an "Add here" header action reused by `AppContextMenu`'s own folder-preview page.
- **`HomeScreen`'s `DockIcon`/`AppRow`** now branch on `PlacedItem` instead of raw `AppInfo`,
  rendering `FolderDockIcon`/`FolderRow` (a `medium`/12dp 2×2 mini-grid tile, `FolderTileGlyph`)
  for a `PlacedItem.FolderItem` — this now applies to the Favorites list too, not just the Dock.

### Tests
Repository tests for every new observer/CRUD method across all four repositories, `FolderDao`
hydration/cascade/uniqueness tests, `ExportBackupUseCase`/`ImportBackupUseCase` round-trip tests
for folders + all four placement kinds, and UI test coverage for the picker tabs, Settings'
Folders row/screen, and `FolderDetailScreen`/`FolderAppPickerScreen`.

---

## Folders — Phase 3: membership-aware Add/Remove for apps and folders (Dock + Favorites)

Phase 2 gave folders parity with apps for *placement* (both can occupy a Dock/Favorites slot,
both round-trip through backup), but the long-press menu only ever offered **Add** — there was no
way to remove a placed app from Home/Drawer's long-press menu, and folder tiles had no
Favorites/Dock row at all (Rename only). Root cause: `ObserveQuickAddStateUseCase`/`QuickAddState`
was capacity-only — it hid the Add row once a list was full, but never checked whether the
specific item was *already* a member, so nothing could know "Remove" applied.

### Domain
- **`ObserveQuickAddStateUseCase`** reworked around a new sealed `QuickPlacementAction`
  (`Add(isFacetOverride)` / `Remove(isFacetOverride)`). `QuickAddState` is now
  `data class QuickAddState(val favoritesAction: QuickPlacementAction?, val dockAction: QuickPlacementAction?)`
  — `null` still hides the row (not a member *and* the list is full), but a member now always gets
  `Remove` regardless of capacity. Dropped the old `operator fun invoke(): Flow<QuickAddState>`
  (it was capacity-only and shared globally, the wrong shape once membership matters per-item) for
  two one-shot suspend functions, `forApp(app: AppInfo)` and `forFolder(folder: Folder)`, each
  resolving the active facet's override via `facetRepository.getById(activeFacetId)` (matching
  `AddAppToDockUseCase`'s own style) then checking `items.any { ... }` against the resolved
  `List<PlacedItem>` for that specific app's component or that specific folder's id.
- New **`RemoveAppFromDockUseCase`**/**`RemoveAppFromFavoritesUseCase`** mirror
  `AddAppToDockUseCase`/`AddAppToFavoritesUseCase`'s override resolution exactly, calling
  `removeDockApp`/`removeFavorite` instead of the add methods.

### UI
- **`AppContextMenu`** — the old `addToFavoritesOverride: Boolean?`/`onAddToFavorites`/
  `addToDockOverride: Boolean?`/`onAddToDock` params became
  `onRequestQuickAddState: suspend (AppInfo) -> QuickAddState`, `onFavoritesAction`, `onDockAction`
  (all keyed on the new `QuickPlacementAction`). Fetched in the same `LaunchedEffect(expanded, app)`
  block as shortcuts (fetched fresh only when the menu opens). `QuickPlacementAction` carries a
  nullable `facetName` (the active facet's real name when overriding, `null` for the launcher-wide
  default) rather than a plain `isFacetOverride: Boolean` — the row's own label never changes
  ("Add to Favorites"/"Remove from Dock", via `quickPlacementLabel()`), and a small trailing
  `QuickPlacementBadge` pill names the target list ("Global" or the facet's name, via
  `quickPlacementBadgeText()`). Decided explicitly over folding that distinction into the sentence
  itself (e.g. "Add to facet favorites") — see chat history; a `Global`/name badge reads
  unambiguously where the old "Favorites" vs "facet favorites" pair didn't. `AppContextMenuItem`
  gained an optional `trailingContent` slot and its label `Text` a `weight(1f)` to push it to the
  row's trailing edge; the badge itself uses `MaterialTheme.shapes.small` (8dp, CLAUDE.md's own
  Chips token) rather than a fully-rounded pill, and stays `Muted`-only (no accent tint) to match
  this menu's existing single-tone restraint. Existing test tags
  (`app_context_menu_add_to_favorites`/`_add_to_dock`) kept for both Add and Remove rows rather
  than renamed.
- **`FolderTileContextMenu`** gained the same two rows (Favorites/Dock, same `quickPlacementLabel`/
  `QuickPlacementBadge` treatment, same `onRequestQuickAddState`/`onFavoritesAction`/`onDockAction`
  shape but keyed on `Folder`) — closing the gap Phase 2 left open (folder tiles had Rename only).
- **`DrawerViewModel`** — dropped the old shared `quickAddState: StateFlow<QuickAddState>` for
  `quickAddStateForApp`/`quickAddStateForFolder` (fetch-on-open, like `getShortcuts`) plus
  `onFavoritesAction`/`onDockAction`/`onFolderFavoritesAction`/`onFolderDockAction`, each
  dispatching to the matching Add/Remove use case based on the tapped `QuickPlacementAction`.
- **`HomeScreen`** threaded the new params (app + folder variants) through every intermediate
  composable — `AppRow`, `DockIcon`/`SingleAppDockIcon`/`FolderDockIcon`, `FolderRow` — down into
  the `AppContextMenu`/`FolderTileContextMenu` call sites. **`AppDrawerScreen`** got the app-only
  swap (Drawer has no folder tiles). **`HomeDrawerRoute`** rewired both screens' call sites
  accordingly.

### Tests
Rewrote `ObserveQuickAddStateUseCaseTest` for `forApp`/`forFolder` + membership (mocking
`facetRepository.getById`, not `observeFacets()`); new `RemoveAppFromDockUseCaseTest`/
`RemoveAppFromFavoritesUseCaseTest`/`AddFolderToDockUseCaseTest`/`RemoveFolderFromDockUseCaseTest`/
`AddFolderToFavoritesUseCaseTest`/`RemoveFolderFromFavoritesUseCaseTest` (mirroring
`AddAppToDockUseCaseTest`'s fixture style). Rewrote `AppContextMenuTest`'s Favorites/Dock cases for
the new `onRequestQuickAddState` shape and added Remove-row cases; new `FolderTileContextMenuTest`
(previously missing entirely) covers Rename plus both new rows' Add/Remove/facet-override wording.
`DrawerViewModelTest`/`HomeDrawerRouteTest`/`KeyboardDismissalTest` updated for the new
`DrawerViewModel` constructor shape. Full `./gradlew test` green. On-device on
`Medium_Phone_API_36.1`: `FolderTileContextMenuTest` (12/12) passed cleanly; `AppContextMenuTest`
initially showed one pre-existing, unrelated failure —
`tappingAnExistingFolderPreviewsItRatherThanAddingImmediately` asserted on `onNodeWithText("Games")`
while a folder-candidate row and `FolderContentsSheet`'s own header both showed that text at once
(the underlying `ModalBottomSheet` isn't dismissed while the preview `Dialog` opens on top of it —
unrelated to this phase's own rows) — fixed by scoping the assertion to the sheet itself
(`onNode(hasTestTag("folder_contents_sheet") and hasAnyDescendant(hasText("Games")))`).

## Android Work Profile support

Scoped through discussion before building (see chat history): Facets and the OS-level Android
Work Profile are different kinds of things — Facets are a soft, purely local, user-created
construct; a Work Profile is a hard, MDM-owned Android user that can appear/vanish outside the
launcher's control. So this stays entirely in `data/`, visible equally to every Facet — no Facet
schema changes, no new per-Facet "show work apps" toggle (a work app is just another pickable app,
the same way the existing Favorites/Dock pickers already let you hand-pick which apps appear).

### Data
- New `AppProfile` enum (`PERSONAL`/`WORK`) — Android exposes at most one extra profile for a
  launcher's purposes, so a 2-valued enum is a deliberate scope choice. Only `AppRepository`/
  `AppWidgetRepository`/`WorkProfileRepository` ever hold a real `android.os.UserHandle`; every
  Facet-adjacent `data:model.AppInfo`/`WidgetProviderOption` gains a `profile: AppProfile` field
  instead.
- **`AppRepository`** iterates every `UserManager.userProfiles` handle instead of the hardcoded
  `Process.myUserHandle()`, tagging each `AppInfo`. Live updates now also react to a new
  `BroadcastReceiver` for `ACTION_MANAGED_PROFILE_ADDED/REMOVED/AVAILABLE/UNAVAILABLE` (a profile
  appearing/disappearing isn't a per-package `LauncherApps.Callback` event). `observeUninstalledPackages()`
  emits `(packageName, profile)` instead of a bare package name, and a new `observeProfileRemoved()`
  drives a **bulk** cleanup path — removing a whole profile tears down every app in it atomically,
  with no guarantee each one also fires its own per-package uninstall event.
- New `WorkProfileRepository` — **read-only** `hasWorkProfile()`/`isWorkProfilePaused()`. Deliberately
  no write path: Android already gives the user a system Settings toggle (and usually a Quick
  Settings tile) for pausing/resuming a Work Profile, so this app reflects that state rather than
  reimplementing the control surface.
- Every table keyed by `(packageName, activityName)` — `favorite_apps`, `facet_dock_apps`,
  `dock_apps`, `default_favorite_apps`, `folder_apps` — gains a `profile` column and a widened
  unique index (Room migration `18→19`), or a personal and Work Profile copy of the same app
  (identical package+activity, different Android user) collide as one row. `widget_placements`
  gets the plain column only (keyed on the real system `appWidgetId`, no index to widen).
  Uninstall-collapse (`CleanUpUninstalledAppsUseCase`) is now profile-scoped end to end, plus the
  separate bulk sweep on `observeProfileRemoved()`. `BackupAppEntry`/`BackupWidgetPlacement` carry
  the profile through export/import too (defaulted, so a pre-existing backup still imports cleanly).
- `LauncherActivity.launchApp()` launches a Work Profile app via `LauncherApps.startMainActivity`
  instead of a plain launch `Intent`, which doesn't resolve correctly across profiles.
- **Hub widgets**: `AppWidgetRepository` enumerates providers from every profile
  (`AppWidgetManager.getInstalledProvidersForProfile`), and binds a Work Profile widget via the
  4-arg `bindAppWidgetIdIfAllowed`/`EXTRA_APPWIDGET_PROVIDER_PROFILE` overloads instead of the
  personal-only ones. A placed widget's actual profile is read back from the system
  (`profileForWidget`, via `AppWidgetProviderInfo`) after binding rather than carried through the
  bind/configure round trip as extra state. Deliberately **no** bulk Hub-placement cleanup on
  Work Profile removal — the existing orphan-detection path (`ObserveHubStateUseCase`, a widget
  whose `getAppWidgetInfo` returns null) already covers this for free once the profile's ids stop
  resolving.

### UI
- **App Drawer** gains a Personal/Work pill switcher (`DrawerProfileTabRow`, mirroring
  `DockPickerTabRow`'s own pill/indicator construction), shown only when a Work Profile exists.
  Browsing is scoped to the selected tab; **searching spans both regardless of tab** — the tab is
  a browsing filter, not a search filter. `hasWorkProfile` is threaded from `WorkProfileRepository`
  through `LauncherUiState`, not inferred from "any app tagged WORK", so the tab still shows (with
  an empty/paused state) while the profile is paused.
- **`AppIcon`** gains an `isWorkApp` corner badge — a themed `Accent` dot via M3's own `Badge`
  composable (bottom-start, matching every other launcher's own Work Profile badge convention),
  not Android's stock `getUserBadgedIcon` briefcase asset, which would render with the OS's own
  tones instead of this app's palette (CLAUDE.md's theming rule). `FacetScopeBadge`'s pill
  construction generalized into a shared `ScopeBadge` primitive, reused by a new `WorkScopeBadge`
  pill shown next to a Work Profile app's name in `AppContextMenu`'s header and in the Hub widget
  picker's tiles.
- **Settings** gets a read-only "Work Profile" row (Active/Paused, from `WorkProfileRepository`)
  shown only when a Work Profile exists, tapping through to the system Settings app — no
  pause/resume control from inside this app.
- **Hub widget picker**: every profile's widgets show in one list, badged — no separate tab here
  either, matching the Favorites/Dock pickers' own "just show everything, badged" treatment.

### Tests
`AppRepositoryTest`/`AppWidgetRepositoryTest` cover multi-profile enumeration, `resolveUserHandle`/
`profileForWidget`, and the Work Profile bind/bind-intent paths (`EXTRA_APPWIDGET_PROVIDER_PROFILE`).
`CleanUpUninstalledAppsUseCaseTest` covers both the per-package profile-scoped path and the new
bulk profile-removed sweep. `LauncherViewModelTest`/`SettingsViewModelTest` cover `hasWorkProfile`/
`isWorkProfilePaused` flowing into their respective UI states. New Room migration test
(`migration18To19...`) on `FacetDatabaseMigrationTest`, run on-device (`Medium_Phone_API_36.1`) —
confirms existing rows default to `PERSONAL` and the widened index actually allows a personal/work
pair to coexist. `AppDrawerScreenTest` (tab visibility, tab filtering, search-spans-both-tabs) also
caught a real crash during development: the Drawer's search-results `LazyColumn` keyed items by
`packageName + activityName` alone, so a personal/Work Profile pair collided as a duplicate
`LazyList` key — fixed by keying with `profile` included everywhere apps are listed. New
`AppContextMenuTest` cases cover the Work badge's presence/absence. Full `./gradlew test` green;
`FacetDatabaseMigrationTest`, `AppDrawerScreenTest`, `HomeDrawerRouteTest`, `KeyboardDismissalTest`,
`AppContextMenuTest`, and every Hub/Settings instrumented test green on `Medium_Phone_API_36.1`.

### Follow-up: profile-aware shortcuts, Secure Folder, and known gaps

- **App shortcuts are now profile-aware.** `AppShortcut` carries its own `profile: AppProfile`;
  `AppShortcutRepository.getShortcuts()`/`launchShortcut()` resolve the shortcut's own profile to
  a `UserHandle` via `AppRepository.resolveUserHandle()` rather than always querying the personal
  profile, and no-op gracefully if that profile has since vanished (a Work Profile removal racing
  a long-press).
- **Quiet-mode (pause/resume) toggle — deliberately not built, by explicit user decision.**
  `UserManager.requestQuietModeEnabled()` (the actual pause/resume call) turns out to be callable
  not just by a profile owner but also by whichever app is the current default launcher — so this
  app technically *could* add its own pause/resume control. The user chose not to: Android already
  surfaces this via system Settings and (on most OEMs) a Quick Settings tile, and a second control
  surface for the same OS state would just risk drifting out of sync with it. This is a known,
  deliberate gap, not an oversight — revisit only if a user-facing need for an in-app toggle shows up.
- **Samsung Secure Folder** — its own launcher-visible app (`com.samsung.knox.securefolder`), not
  reachable through `UserManager.getUserProfiles()`/Work-Profile APIs at all (Samsung deliberately
  doesn't expose it to third-party launchers even post-Android-15). New `SecureFolderRepository`
  checks `PackageManager.getLaunchIntentForPackage()` for it; when present, the App Drawer's 3-dot
  overflow menu (`AppDrawerScreen`/`DrawerViewModel.secureFolderIntent`) gets an "Open Secure
  Folder" row that starts its own launch intent — same treatment as opening any other app, since
  that's all it structurally is from this launcher's point of view.
## Private Space support (Android 15+)

A hidden secondary profile (`UserManager.USER_TYPE_PROFILE_PRIVATE`) the user locks/unlocks with
biometrics/PIN — distinct from Work Profile (a hard MDM-owned profile) and Secure Folder (a plain
launchable app, not a profile at all). Scoped deliberately narrow for v1: browse + launch + search
only, no Favorites/Dock/Hub integration, no Room migration needed as a result.

Building this surfaced a live correctness bug that had to be fixed first: `AppRepository.profileFor()`
had no branch for a Private Space handle, so it silently fell through to `PERSONAL` — meaning a
configured Private Space's apps already leaked into the main Drawer (unconditionally in search,
and in browse mode on any device without a Work Profile, per `AppDrawerScreen`'s own
`isSearching || !hasWorkProfile` filter-bypass condition).

### Data
- `AppProfile` gains a third value, `PRIVATE`. `AppRepository.profileFor()` now positively checks
  `USER_TYPE_PROFILE_PRIVATE` alongside the existing `USER_TYPE_PROFILE_MANAGED` check.
  `LauncherActivity.launchApp()`'s Work-Profile-only branch widened to `profile != PERSONAL`
  (Private Space apps launch via the identical `LauncherApps.startMainActivity` cross-profile path).
  `GetInstalledAppsUseCase` (both `invoke()` and `observe()`) now excludes `PRIVATE` apps — the
  single choke point keeping them out of the main Drawer and every Favorites/Dock/Folder picker;
  `AppWidgetRepository.installedProvidersWithProfile()` excludes them the same way for Hub widgets.
- New `PrivateSpaceRepository`, modeled on `WorkProfileRepository` but with a 3-state
  `PrivateSpaceState` (`NotConfigured`/`Locked`/`Unlocked`) instead of a boolean pair — verified
  against Android's own launcher-integration docs and a real reference implementation that, unlike
  first assumed, a locked Private Space's handle *stays* enumerable via `userManager.userProfiles`
  (same as a paused Work Profile), so `UserManager.isQuietModeEnabled(handle)` is what actually
  distinguishes Locked from Unlocked — no broadcast-history inference needed. Listens for the
  generic, profile-type-agnostic `ACTION_PROFILE_ADDED/REMOVED/AVAILABLE/UNAVAILABLE` broadcasts
  (Android 15+, distinct from Work Profile's `ACTION_MANAGED_PROFILE_*` ones). Also exposes
  `requestUnlock()` — unlike Work Profile's deliberately read-only design, a locked Private Space
  isn't meaningfully browsable at all, so triggering the OS's own unlock prompt (via
  `UserManager.requestQuietModeEnabled(false, handle)`) is required for the feature to work, not an
  optional control surface.
- New manifest permission `android.permission.ACCESS_HIDDEN_PROFILES` — per Android's own docs,
  also requires holding `RoleManager.ROLE_HOME`, which this app already holds once set as the
  default launcher (no separate role-request code needed).

### UI
- New `PrivateSpaceScreen`/`PrivateSpaceViewModel` — a separate, deliberately minimal pair rather
  than a mode flag on `AppDrawerScreen`/`DrawerViewModel` (which carry ~30 params of behavior
  irrelevant here: folders, favorites/dock quick-add, contacts/settings search, shortcuts). Its own
  small search bar, scoped only to Private Space's own apps — never merged with the main Drawer's
  search. Wrapped in a new `PrivateSpaceTheme` — a small, fixed-dark "incognito" palette
  (`ui/theme/PrivateSpaceTheme.kt`) that nests its own `MaterialTheme` inside the app's own
  `FacetLauncherTheme` rather than threading a third axis through the shared token system in
  `Color.kt`, which has no existing precedent for a screen-local override.
- App Drawer's 3-dot overflow menu gains a "Private Space" row (`DrawerViewModel.privateSpaceState`,
  a live `StateFlow` unlike `secureFolderIntent`'s one-shot `val` — lock state changes mid-session),
  shown for `Locked`/`Unlocked`, hidden for `NotConfigured`. Tapping it while `Unlocked` opens the
  screen; while `Locked`, triggers the OS unlock flow instead — the user re-taps once unlocked
  rather than auto-navigating on unlock, avoiding fragile timing against the availability broadcast.
- Two-level back gesture in `HomeDrawerRoute.kt`: a new `showPrivateSpaceDrawer` boolean, checked in
  the existing `BackHandler`'s `when` chain at the same priority tier as `showWidgetPicker` (both
  are "innermost sub-panel of an already-open parent surface") — one back press closes Private
  Space back to the regular Drawer, a second then closes the Drawer to Home, unchanged from before.

### Tests
`AppRepositoryTest`/`AppWidgetRepositoryTest` cover the `PRIVATE` classification and its exclusion
from Hub widgets; `GetInstalledAppsUseCaseTest` covers its exclusion from the Drawer/pickers;
`ConvertersTest` covers the Room enum round-trip. New `PrivateSpaceRepositoryTest` covers the
3-state derivation and `requestUnlock()`. `DrawerViewModelTest` covers `privateSpaceState`
exposure. `AppDrawerScreenTest` covers the overflow row's visibility/click delegation;
`HomeDrawerRouteTest` covers the two-level back gesture (`Espresso.pressBack()`, same pattern as
the existing Drawer/Hub/Facet-carousel back tests) and that tapping the row while `Locked` doesn't
open the screen. Full `./gradlew test` green; `AppDrawerScreenTest`, `HomeDrawerRouteTest`, and
`KeyboardDismissalTest` (whose `DrawerViewModel` construction needed the new `PrivateSpaceRepository`
param) green on `Medium_Phone_API_36.1`.

### Out of scope for v1
Favorites/Dock picking of Private Space apps, Hub widgets from it, Room persistence of any
Private-Space-specific state, the long-press context menu/shortcuts/"add to folder" from its
screen, and respecting the user's Drawer grid/list presentation setting inside it (a fixed list for
now). No per-icon "Private" badge either — unlike Work Profile apps, which visually mix with
Personal apps in the same list, Private Space apps only ever appear in their own separately-themed
screen.

## Per-profile identity fix (crash + clone/dual-app correctness)

Motivated by a real user-reported crash on a Techno Spark 20 Pro (Android 14, HiOS): below API 35
`AppRepository.profileFor()` had no way to ask `LauncherApps.getLauncherUserInfo` (API 35+ only),
so it guessed "any non-primary handle = WORK." HiOS's "App Clone"/dual-apps feature creates exactly
such a non-primary handle (Android's native Clone Profile mechanism), so it got misclassified as a
Work Profile — which crashed via an unguarded `UserManager.isQuietModeEnabled()` call on a handle
that wasn't actually managed, and even guarded, hid clone apps behind a Work tab instead of the
main list. Investigating the fix (see chat history) surfaced a deeper, pre-existing latent bug:
`AppProfile` was being used as the *identity* key for launch routing and Room dedup, not just a
display label — two distinct real profiles landing on the same enum value (a genuine Work Profile
*and* a clone profile, both guessable as WORK pre-35) could silently collide: favoriting one could
overwrite the other, and tapping one's tile could launch the other's copy instead. This mattered
beyond clone apps — a genuine MDM/Work Profile user hits the identical collision risk if a clone
profile also exists on their device, which this app can't tell apart from a real Work Profile
before API 35.

### What got built
- `AppProfile` gained a 4th value, `OTHER` — any profile that can't be positively identified (always
  true below API 35; an unrecognized `userType` on API 35+, e.g. a clone profile). `PERSONAL` is now
  reserved strictly for the true primary user.
- Identity moved off the enum entirely: `AppInfo` gained `val userHandle: UserHandle` (the real
  handle it was enumerated from, never reconstructed) as its true identity; `AppProfile` is now
  purely a *display* category (tab/badge), still computed via `profileFor()`, but never used for
  dedup/launch/storage identity again.
- Room migration `MIGRATION_19_20` (`FacetDatabase.VERSION` 19→20): adds `userId: Int` to the six
  profile-keyed tables (`favorite_apps`, `dock_apps`, `facet_dock_apps`, `default_favorite_apps`,
  `folder_apps`, `widget_placements`), widens their unique indices from `(..., profile)` to
  `(..., userId)`, backfills `userId = 0` for existing `'PERSONAL'` rows deterministically (the
  primary user always hashes to `0`) via raw SQL. Non-PERSONAL rows are left at a `-1` sentinel —
  new `RepairOrphanedProfileRowsUseCase` (wired into `LauncherViewModel.init` as a one-time suspend
  pass, not a live collector) backfills each to the single currently-live matching handle if exactly
  one candidate exists, else leaves it orphaned (won't hydrate until re-added — graceful, not a
  crash). **`UserHandle.getIdentifier()` turned out to be hidden API, not in the public SDK**
  (confirmed against compileSdk 36's `android.jar` stub) — `UserHandle.hashCode()` is used instead
  everywhere a stable `Int` is needed; AOSP's own implementation returns the internal per-user id
  verbatim, the standard workaround other third-party launchers use for this.
- `LauncherActivity.launchApp()`, `AppShortcutRepository`, `AppWidgetRepository`
  (`bindAppWidgetIdIfAllowed`/`createBindIntent`), and every Favorites/Dock/Folder repository's
  hydrate/add/remove path now use `AppInfo.userHandle`/`WidgetProviderOption.userHandle` directly —
  no more re-deriving a handle from the display enum via `resolveUserHandle()`.
- `CleanUpUninstalledAppsUseCase`'s profile-removal sweep changed from a bulk
  `removeByProfile(AppProfile.WORK)` (would wipe a surviving colliding profile's rows too) to a
  precise `removeByUserId(removedHandle.hashCode())`, reading the exact removed handle off
  `ACTION_MANAGED_PROFILE_REMOVED`'s `Intent.EXTRA_USER` extra (`AppRepository.observeProfileRemoved()`
  now emits `UserHandle`, not `Unit`).
- `WorkProfileRepository.hasWorkProfile()`/`isWorkProfilePaused()` (booleans) replaced by
  `observeWorkProfiles(): Flow<List<WorkProfileInfo>>` — 0 or 1 entries in practice (stock Android
  provisions at most one real Work Profile), but list-shaped so it stays correct under the rare
  case it ever isn't. `isQuietModeEnabled` calls in both `WorkProfileRepository` and
  `PrivateSpaceRepository` are now wrapped in `runCatching` (previously unguarded — the actual
  crash's proximate cause).
- App Drawer's Work/Personal tab switcher became fully dynamic (`DrawerTab` a sealed type, not a
  fixed 2-value enum): one tab per live Work Profile handle, plus an always-present Personal tab
  that merges `PERSONAL` and `OTHER` apps — decided explicitly by the user: a clone app should "just
  show up in the list," not get hidden behind its own tab, since below API 35 there's no way to know
  what it actually is. A new person-icon badge (`OtherProfileBadge`/`OtherScopeBadge`, mirroring the
  existing Work badge pattern in `AppIcon.kt`/`FacetScopeBadge.kt`) marks an `OTHER` app inline so
  it's still visually distinguishable. Settings' single "Work Profile" row became one row per
  `WorkProfileInfo` entry the same way.
- Fixed App Info's profile targeting as a related fix while already touching
  `AppContextMenu.kt`: it now uses `LauncherApps.startAppDetailsActivity` (via a new
  `AppRepository.openAppDetails`, threaded as an `onAppInfo` callback the same way
  `onLaunchShortcut` already was) instead of an untargeted `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`
  intent. Uninstall stays untargeted — there's no public per-profile uninstall API for a
  third-party launcher, a genuine platform limitation, documented inline rather than worked around.

### Tests
New: `AppRepositoryTest` (`OTHER` fallback below API 35, `getInstalledApps` carrying real
`UserHandle`s, per-profile `getActivityList` failure isolation, `observeProfileRemoved`'s
`EXTRA_USER` handling, and — the actual stress case this whole redesign exists to survive — a
real Work Profile + a real Private Space + an unclassifiable third profile all present on the same
device at once, asserting all three classify distinctly and never collide); `removeByUserId`
collision-regression tests in all five profile-keyed repositories; new
`RepairOrphanedProfileRowsUseCaseTest` (single/zero/multiple-candidate resolution, already-resolved
rows untouched); `PrivateSpaceRepositoryTest`'s `isQuietModeEnabled` throwing-doesn't-crash case
(resolves to `Unlocked`, per the `getOrDefault(false)` fallback). ~22 pre-existing pure-JVM test
files that construct `AppInfo` via its default constructor needed `@RunWith(RobolectricTestRunner::class)`
added — `AppInfo.userHandle`'s default (`Process.myUserHandle()`) calls a real Android API at
construction time now, a real, broad behavioral cost of this design worth knowing about. Full
`./gradlew test` green (555 tests, 0 failures).

### Real bug found and fixed running on-device (`Medium_Phone_API_36.1`)
`connectedDebugAndroidTest` (417 tests) surfaced a genuine bug the JVM/Robolectric suite couldn't:
`switchingToTheWorkTabShowsOnlyWorkProfileApps` reliably failed — after tapping the Work tab, both
the personal *and* the Work Profile copy of the same app stayed visible (`CollectionInfo(rowCount=3)`
confirmed via `printToLog` that the LazyColumn genuinely held 2 app rows, not a stale-semantics
artifact). Root cause: the test built its "Work Profile" `UserHandle` via
`mock(UserHandle::class.java)`. Mockito bypasses the real constructor, so the mock's private
`mHandle` field is left at Java's `int` default, `0` — which is also the *real* primary user's own
id. `AppDrawerScreen`'s Work-tab filter (`apps.filter { it.userHandle == tab.handle }`) is correct
production code, comparing real `UserHandle`s via `==`; on a real device/emulator this calls AOSP's
actual `UserHandle.equals()`, which reads `mHandle` directly — so the personal app's genuinely-real
handle spuriously compared equal to the mock's zeroed-out one. Robolectric's shadowed `UserHandle`
apparently doesn't hit this path, which is why the JVM-side tests never caught it. Fixed by
replacing the mock with a real, distinct `UserHandle` built via its public `Parcel` constructor
(`UserHandle(Parcel)`, writing a non-zero int and reading it back) — a `fakeUserHandle(id)` helper
added to `AppDrawerScreenTest.kt`, the only file using `mock(UserHandle::class.java)`. Confirmed
fixed (`AppDrawerScreenTest` standalone: 35/35) and confirmed the fix doesn't regress anything else
(full suite re-run: 417/417 excluding one unrelated, pre-existing flake — see below). This is worth
remembering for any *future* test needing a "different but real" `UserHandle` on this project:
`mock(UserHandle::class.java)` is unsafe wherever code compares handles via `==`/`.equals()`, even
though it's fine for tests that only ever pass the mock through opaquely (never compared).

The full-suite re-run's one remaining failure — `DockAppPickerScreenTest.foldersTabCheckboxReflectsWhetherTheFolderIsPlacedInThisDock`,
a `ComposeTimeoutException` on `waitUntil` — is unrelated: that file/screen was untouched this pass,
and it passes cleanly (7/7) run standalone, so it's a pre-existing timing flake under full-suite
load rather than a regression.

### Deliberately out of scope
Turning Settings/Drawer into a UI that distinguishes *which* `OTHER` profile an app belongs to
when more than one exists simultaneously (e.g. two separate clone profiles) — they still merge into
one Personal tab, still each launch/favorite/dock correctly via their own real handle, just without
a way to tell them apart from each other visually. Accepted: below API 35 there's no API to name
what an `OTHER` profile even is, so a per-profile UI split would have nothing meaningful to label
itself with.

### Related gap closed in passing
`AndroidManifest.xml` was missing the `android.permission.ACCESS_HIDDEN_PROFILES` permission that
the earlier "Private Space support" write-up above already documents as required —
`LauncherApps.getLauncherUserInfo` needs it to positively identify a Private Space handle
(`USER_TYPE_PROFILE_PRIVATE`) rather than it coming back `null` and silently falling through to
`profileFor()`'s `else` branch (`OTHER`, post this pass's fix — previously `PERSONAL`). The
permission was apparently never actually added when that feature was originally built, only
documented; added now since this pass's work touches the exact same classification path.

## About Facet Launcher settings screen
New "About Facet Launcher" row under Settings → SYSTEM (after "Set as default launcher"), opening
a dedicated screen (`ui/settings/AboutScreen.kt`) with three rows: the live app version (read from
`BuildConfig.VERSION_NAME`/`APPLICATION_ID`, never hardcoded, so it can't drift from what
`scripts/release.sh` bumps), "Check for updates" (opens the Play Store listing via
`https://play.google.com/store/apps/details?id=...`), and "Join the Discord" (opens the same
`https://discord.gg/BRjwWZ23E` invite the marketing site already uses). No ViewModel — everything
here is either a compile-time constant or a plain `Intent` launch, same category as this screen's
"Change wallpaper" row, so it stays a stateless composable per `AboutScreen`/`AboutContent`.
`app/build.gradle.kts` needed `buildFeatures.buildConfig = true` added (previously off) to expose
`BuildConfig` at all.

### Tests
`AboutScreenTest` (version row shows the real `BuildConfig.VERSION_NAME`; Play Store and Discord
rows are interactive — launching an external app isn't itself verifiable from a Compose test, same
category as `SettingsScreenTest`'s `changeWallpaperRowIsInteractive`; back button) +
`SettingsScreenTest.aboutRowIsClickable`. One real bug found here: the version row is a plain,
non-`clickable` `Row`, so unlike this screen's other rows it doesn't pick up `mergeDescendants`
from a `clickable()` modifier — its tagged node had no text in the merged semantics tree until
`Modifier.semantics(mergeDescendants = true) {}` was added explicitly.

## App picker sort control (Favorites/Dock/Folder) + picker consolidation

Added a sort control (Alphabetical / Last used / Installed Date / Last updated, with an asc/desc
toggle) to the Favorites, Dock, and Folder "add apps" pickers, positioned directly under the search
field and applying only to the not-yet-selected section — the already-placed section keeps its own
frozen order untouched. Discovered `FavoritesPickerScreen.kt`/`DockAppPickerScreen.kt` were ~95%
byte-for-byte duplicates and `FolderAppPickerScreen.kt` the same picker again minus the Folders tab,
so consolidated the shared inner UI into one `ui/components/AppPickerScreen.kt` (+
`ui/components/AppSortControl.kt` for the new sort row) first, rather than pasting the same sort
logic into three files; each of the three screens is now a thin wrapper supplying its own
title/labels/test-tag prefix and repository calls. `AppInfo` gained `firstInstallTime`/
`lastUpdateTime` (`AppRepository` populates them — cross-profile-safe install time via
`LauncherActivityInfo.getFirstInstallTime()`, best-effort update time via `PackageManager` with a
fallback to install time for a Work Profile app `PackageManager` can't resolve). New
`domain/SortAppsForPickerUseCase.kt` does the actual sorting (spans `AppInfo`'s own fields plus
`UsageStatsRepository` for "Last used", so it's a use case, not repository logic); `UsageStatsRepository`
gained `getLastUsedTimestamps()` (epoch-wide, unlike the existing Recents block's 7-day lookback).
"Last used" is gated behind the existing Usage Access permission (`UsageAccessRepository`) and its
existing `UsageAccessExplanationScreen` flow — picking it while ungranted routes there instead of
applying, and leaves the dropdown's current selection untouched if the user doesn't grant it. UI
went through a few direct-feedback rounds: the sort dropdown and direction arrow ended up grouped
at the row's trailing edge (not spread across it); the direction icon is a vendored Material
Symbols "list_arrow" (`res/drawable/list_arrow_24.xml`, not in the classic icon set this app
otherwise draws from) that flips 180°; the closed dropdown button matches the page's own `Surface`
background rather than `SurfaceContainer` (the open popup keeps `ThemedDropdownMenu`'s normal
default styling — `ThemedDropdownMenu` gained an optional `containerColor` override for this, an
otherwise-unused general capability today); "Last installed" was renamed "Installed Date" (an app
only installs once, so "last" didn't make sense there — "Last updated" keeps "last" since repeated
updates are a real concept).

### Tests
New `SortAppsForPickerUseCaseTest` (every option × direction, never-used-apps-sort-last, ungranted
fallback), `FavoritesPickerViewModelTest`/`DockAppPickerViewModelTest` (neither existed before —
this is their first unit coverage), extended `FolderAppPickerViewModelTest`, `AppRepositoryTest`,
`UsageStatsRepositoryTest`. Instrumented: each of `FavoritesPickerScreenTest`/
`DockAppPickerScreenTest`/`FolderAppPickerScreenTest` gained a direction-toggle test (verified via
the ViewModel's own `otherResults` state, not measured pixel positions — reversing a long real
installed-app list can push rows off the LazyColumn's composed window, and a single `waitForIdle()`
isn't guaranteed to catch the ViewModel's own `StateFlow` recombination either) and a dropdown-select
test that opens the real popup, taps "Installed Date", and confirms `otherResults` is genuinely
ordered by `firstInstallTime`, not just relabeled; `DockAppPickerScreenTest` additionally covers the
Usage-Access-gating flow end to end (picks "Last used" while ungranted, asserts it routes to
`onNavigateToUsageAccessExplanation` instead of applying). `docs/architecture/` (`07-registries.md`,
`04-package-structure.md`, `03-reactive-data-flow.md`) and `TEST_REGISTRY.md` updated to match.

## Folders in the App Drawer (`DrawerFolderDisplayMode`)

New Settings → App Drawer → "Folders in drawer" row (`AppDrawerSettingsScreen`, between "Search
bar position" and the opacity slider), four options backed by a new `DrawerFolderDisplayMode` enum/
DataStore key: `DO_NOT_SHOW` (default — the original, unchanged behavior; folders stay reachable
only via Dock/Favorites or `AppContextMenu`'s "Add to folder" row), `INLINE` (folders sort into the
drawer's existing alphabetical letter groups, indistinguishable in position from an app), and
`SHOW_FIRST`/`SHOW_LAST` (every folder instead renders in its own pinned section before/after the
lettered content, with its own "Folders" header in List — Grid gets no header, same as the lettered
content it already renders header-less).

`GroupAppsByLetterUseCase`/`GroupedApps` were generalized to `GroupedItems<T>` (a `typealias
GroupedApps = GroupedItems<AppInfo>` keeps every existing call site and test untouched) plus a new
generic `invoke(items, locale, nameOf)` overload, so the same `AlphabeticIndex` bucketing now also
groups the new `data/model/DrawerItem` sealed type (`AppEntry`/`FolderEntry`) for `INLINE` mode.
`AppDrawerScreen` wraps every app as `DrawerItem.AppEntry` regardless of mode — even outside
`INLINE` — so `DrawerListContent`/`DrawerGridContent` render one item type throughout rather than
branching per mode; new `DrawerFolderRow`/`DrawerFolderTile` mirror `DrawerAppRow`/`DrawerGridTile`'s
exact layout/styling with `FolderTileGlyph`/`FolderContentsSheet`/`FolderTileContextMenu` standing in
for `AppIcon`/`AppContextMenu` (rename + Add to Favorites/Dock), the same relationship
`ui/home/HomeScreen.kt`'s `FolderRow`/`FolderDockIcon` already has to `AppRow`/`DockIcon` — reusing
that screen's own components directly (`FolderTileGlyph`/`FolderRow` are `internal`) rather than
duplicating them. Real bug caught while building the `INLINE` merge: `GroupAppsByLetterUseCase`
only buckets by leading letter, it never re-sorts *within* a bucket (relying on its input already
being alphabetical) — so naively concatenating `apps + folders` before grouping put every folder
after every app inside a shared letter's bucket instead of truly interleaving them; fixed by sorting
the merged list by name before grouping.

`AlphabetRail` gained a `RailFolderPosition` (`NONE`/`TOP`/`BOTTOM`) and `RailFolderGlyph` — a
folder icon sized off `MaterialTheme.typography.labelSmall`'s own font size (via `LocalDensity`, the
same way `sp` already scales with the system font-scale setting for the letters), so it grows/shrinks
in lockstep with them at any accessibility text size rather than sitting fixed-size among them. Its
own custom `Layout` treats the glyph as one more measured child, so the existing fit-to-height
spacing math (shrinking `RAIL_LETTER_SPACING` down to `0` when a long letter list or a large font
scale would otherwise overflow — see that file's own doc) covers it for free. Drag/touch hit-testing
(`letterAt`) was generalized to `railSelectionAt`, returning a `RailSelection` (`Letter`/`Folders`)
instead of a bare `String?` — `letterAt` itself is kept as a thin wrapper delegating to
`railSelectionAt(..., folderPosition = NONE)` so its own existing tests needed no changes. The folder
glyph gets its own equal slot in the same proportional band-clamping split (not a pixel-accurate
measurement of its real rendered height — a reasonable approximation given it's sized to match a
letter). `INLINE`/`DO_NOT_SHOW` never show the glyph (`RailFolderPosition.NONE`) since a folder is
reachable by its own letter (or not shown at all) either way; only `SHOW_FIRST`/`SHOW_LAST` render
it, at whichever end the pinned section sits, and dragging onto it scrolls to that section's start
index instead of a letter's.

Search results never include folders — scoped out of this pass; browse-mode only.

Also added `drawerFolderDisplayMode` to `BackupBundle`'s `BackupSettings` (defaulted for
tolerant-reader discipline, no `CURRENT_BACKUP_VERSION` bump needed) and wired it through
`ExportBackupUseCase`/`ImportBackupUseCase`, so this setting round-trips through backup/restore like
every sibling App Drawer setting already does.

### Tests
New `GroupAppsByLetterUseCaseTest` case for the generic overload; `AlphabetRailMappingTest` gained
four `railSelectionAt` cases (`NONE` parity with `letterAt`, `TOP`/`BOTTOM` folder-slot resolution,
folder-only with no letters); `SettingsRepositoryTest` (default + round-trip) and
`AppDrawerSettingsViewModelTest` (setter call) extended for the new key. Instrumented:
`AppDrawerScreenTest` gained four cases covering all three non-default modes plus the do-not-show
default (`DO_NOT_SHOW` renders nothing, `SHOW_FIRST`/`SHOW_LAST` show a pinned "Folders" header —
`SHOW_LAST`'s needed `performScrollToNode` first, since the LazyColumn doesn't compose an
off-screen 27th section by default — and `INLINE` renders the folder under its shared letter header
with no separate "Folders" section); `AppDrawerSettingsScreenTest` gained a dropdown-switch case for
the new row. Ran the full existing unit suite plus the full instrumented suite on
`Medium_Phone_API_36.1` to confirm no regressions from generalizing `GroupedApps`/`AlphabetRail`.
`docs/architecture/` (`02-persistence-room.md`'s DataStore key diagram/writer table/section table
and key counts, `12-flow-drawer-search-and-app-actions.md`'s data-in diagram + new §1a) and
`TEST_REGISTRY.md` updated to match.

## Language translation (i18n) — Pass 1: string-resource migration

Every UI string in the app today is hardcoded inline in Compose/Kotlin — `res/values/strings.xml`
is essentially empty and there are zero `stringResource()` call sites. Before any actual
translation can happen, every user-facing literal needs to move into `strings.xml` (default
`values/`, no other locales yet) behind `stringResource(R.string.x)` in composables /
`context.getString()` elsewhere. Scoped by grepping every literal string in `ui/` — counts below
are from that scan and are approximate (the grep also catches some non-UI text like format
patterns/comments, filtered out where obviously not real copy).

### 0. Infrastructure
- [x] ~~Enable AGP's built-in `HardcodedText` lint rule~~ — **tried and reverted.** That check
  (`HardcodedValuesDetector`) only inspects `android:text="..."` in XML layouts; it does not
  analyze Kotlin/Compose `Text(...)` calls at all, so on this Compose-only app it silently found
  zero hits and enforced nothing (confirmed: generating a baseline produced only unrelated
  pre-existing findings — `MissingPermission`, `InlinedApi`, `ModifierParameter`, etc. — never one
  `HardcodedText` entry). Reverted rather than leave a config that looks like enforcement but isn't.
- [x] Built a real check instead, in a new `:detekt-rules` Gradle module (plain JVM,
  `kotlin("jvm")` — not an Android module, since a custom static-analysis rule has to be compiled
  before the module it checks runs, so it can't live inside `:app` itself; documented as an
  exception to "single `:app` module" in `CLAUDE.md`). Went through two implementations:
  - **First cut was a custom Android Lint check** (UAST/PSI-based, `lint-api 32.4.0` paired to
    AGP 9.4.0), and it worked — verified 289 real hits, zero false positives, a real regression
    test (new hardcoded string fails, existing ones don't). **Replaced with Detekt anyway per
    explicit user preference** ("i still think detekt is the better option") after discussing the
    tradeoff (Lint's custom-detector API is more version-fragile; Detekt's is more ergonomic for
    pure-syntax checks but means introducing a whole new tool). Not a case of the Lint version
    being broken — it was reverted while working, so if Detekt ever becomes unworkable here, that
    approach is a known-good fallback to reach for again.
  - **Landed on Detekt 2.0.0-alpha.6** (group `dev.detekt`, plugin id `dev.detekt` — not the
    1.23.x stable line's `io.gitlab.arturbosch.detekt`/`io.gitlab.arturbosch.detekt`). 1.23.8
    (latest stable at the time) crashed outright on this machine's JDK 25
    (`IllegalArgumentException: 25.0.3` deep in a bundled JetBrains platform utility trying to
    parse the JDK version string) — a real, reproducible incompatibility, not a config mistake
    (confirmed via full stacktrace). The user pointed at 2.0.0-alpha.6 specifically as a version
    that supports Java 25; confirmed empirically after fixing it up for that version's
    substantially-reshaped API (package renamed `io.gitlab.arturbosch.detekt.api` →
    `dev.detekt.api`; `Rule` construction, `RuleSetProvider.instance()`, and reporting
    (`CodeSmell`/`Issue`/`Debt` → plain `Finding(entity, message)`) all changed shape — decompiled
    the actual 2.0.0-alpha.6 jars with `javap` to get the real API surface rather than guessing).
  - `ComposeHardcodedTextRule` (`detekt-rules/src/main/kotlin/com/facetlauncher/detekt/`): same
    detection logic as the Lint version — (1) a named argument to a known text-carrying parameter
    (`text`, `title`, `label`, `contentDescription`, `message`, `hint`, `placeholder`,
    `description`) bound to a string-literal template, or (2) `Text(...)`/`BasicText(...)`'s first
    *positional* argument being a string literal — registered as rule `ComposeHardcodedText` in a
    `facet` rule set via `FacetRuleSetProvider`
    (`META-INF/services/dev.detekt.api.RuleSetProvider`), wired into `:app` via
    `detektPlugins(project(":detekt-rules"))`. `app/detekt.yml` explicitly marks
    `facet.ComposeHardcodedText.active: true` — **custom Detekt rules are inactive by default**
    even when correctly discovered (confirmed via `debug = true` logging: the provider was
    registered but produced 0 findings until this config existed — don't assume "no findings"
    means "nothing to find" the way it does for Lint). `disableDefaultRuleSets = true` in the
    `detekt {}` block keeps detekt's own bundled rule sets (style/complexity/etc., ~1900
    unrelated findings) out of scope entirely, rather than baselining that noise.
  - **Two real false positives found and fixed during verification, both from the same root
    cause**: Kotlin can't syntactically distinguish a `@Composable` call from a data class
    constructor call (both are just `Capitalized(...)`) without full-classpath type resolution,
    which this rule deliberately avoids. `AnimatedContent(label = "app_context_menu_page")` — a
    non-visual animation debug tag, not UI text — was excluded via an
    `ANIMATION_LABEL_CALLS` allowlist scoped to just the `label` param. `AppInfo(...)`/
    `CalendarEvent(...)` — real domain model constructors used in `@Preview` fixture data, whose
    `label`/`title` fields collided with the same param-name heuristic — were excluded via a
    `NON_COMPOSABLE_CONSTRUCTORS` set. Both are documented as a known, open-ended category in the
    rule's own code comments (not closable without type resolution) — add more names to that set
    if another domain model trips it during sections 1–7.
  - **Environment quirk worth not rediscovering**: the Gradle daemon appears to cache the
    `:detekt-rules` plugin jar's *behavior* across invocations even after it's rebuilt — a plain
    `./gradlew :app:detekt` after editing the rule can silently keep running the old version.
    `./gradlew --stop` before re-testing a rule change is required, not optional.
  - **Verified working end to end**: 292 real hits after both false-positive fixes (all inside
    `ui/`, zero outside it — spot-checked). Baselined via `./gradlew :app:detektBaseline`
    (`app/detekt-baseline.xml`, 281 deduped entries — same shrink-as-you-migrate discipline as any
    baseline). Then the actual regression test: added a scratch composable with a brand-new
    `Text("...")` literal, confirmed `./gradlew detekt` failed on exactly that line and nothing
    else, removed it, confirmed green again — repeated this full check twice, once under a
    temporarily-downgraded JDK 21 daemon (see below) and again after restoring JDK 25, so the
    passing state is the one actually verified, not just assumed to still hold.
  - **JDK daemon**: temporarily changed `gradle/gradle-daemon-jvm.properties` to JDK 21 to get
    unblocked while diagnosing the 1.23.8 crash (via `./gradlew updateDaemonJvm --jvm-version=21`
    — needed adding the `org.gradle.toolchains.foojay-resolver-convention` settings plugin, since
    that task can't resolve toolchain download URLs without it; kept the plugin afterward since
    it's harmless and also lets Gradle auto-provision `:detekt-rules`' `jvmToolchain(17)` if ever
    needed). **Reverted back to JDK 25** (`updateDaemonJvm --jvm-version=25`) once 2.0.0-alpha.6
    was confirmed working there — full re-verification (`assembleDebug`, `test`, the detekt
    regression test above) all green on the restored JDK 25 daemon, so the project's daemon JVM
    ends this section unchanged from where it started.
  - **Also added mrmans0n/compose-rules** (`io.nlopez.compose.rules:detekt:0.6.6`, pinned to the
    exact version its own POM declares against `dev.detekt-core:2.0.0-alpha.6` — the same version
    this project already pins, avoiding another API-mismatch hunt) per user request — a
    third-party Compose best-practice ruleset (Modifier placement/ordering, state hoisting,
    `remember` misuse, parameter naming, unstable-collection params, etc.) that mechanizes several
    conventions `CLAUDE.md` already documents by hand. Unrelated to the string migration (no
    overlap with `ComposeHardcodedText`), but wired in alongside it since it was already mid-setup.
    All 44 of its rules explicitly enabled in `app/detekt.yml` under a `Compose:` block (same
    "custom rule sets need explicit `active: true`" requirement as our own `facet` set). Produced
    249 new findings, all genuine (spot-checked `ParameterNaming` — `onQueryChanged` should be
    `onQueryChange`, present tense — and `UnstableCollections` — a raw `List<Folder>?` composable
    param that should be an `ImmutableList`; neither is a false positive, both are real pre-existing
    debt this codebase hasn't addressed yet). Baselined together with the string-migration backlog
    (`app/detekt-baseline.xml` now 461 entries: 281 `ComposeHardcodedText` + 180 across the 11
    `Compose.*` rules that fired) rather than fixed now — fixing 249 pre-existing style findings is
    its own separate pass, out of scope here. Re-ran the full new-string-fails/existing-strings-pass
    regression test after this addition to confirm the two rule sets don't interfere with each other.
  - **Deliverable:** `detekt-rules/` module; `app/detekt.yml`; `detektPlugins(project(":detekt-rules"))`
    and `detekt {}` config in `app/build.gradle.kts`; `app/detekt-baseline.xml` committed;
    `./gradlew detekt` green on JDK 25. Regenerate the baseline (delete it, `./gradlew --stop`,
    rerun `./gradlew :app:detektBaseline`, commit the smaller file) as each section below lands.
- [x] Decide plural/format-arg conventions up front — Android `plurals` resources for counts
  ("3 apps"), `%1$s`/`%1$d` positional placeholders for interpolated values — documented as a
  comment at the top of `res/values/strings.xml` so every task below follows the same pattern.
- [x] Confirm target locales with the user — **Spanish, French, German, Portuguese**
  (`values-es/`, `values-fr/`, `values-de/`, `values-pt/`), decided 2026-09-18. Actual translation
  is pass 2, out of scope here.

### 1. Centralized model/enum labels (`data/model/`) — do before the screens below — ✅ complete
High-leverage: these are option labels rendered by dropdowns/pickers across many screens, so
converting them once fixes every consuming screen.
- [x] `AppSortOption.kt`, `ClockDateStyle.kt`, `ClockFontOption.kt`, `ClockColorOption.kt`,
  `FontWeightOption.kt`, `IconRenderMode.kt`, `LauncherFontOption.kt`, `ClockTemplateId.kt`,
  `ui/theme/AccentSwatch.kt` — every enum's `label`/`displayName: String` field became a
  `@param:StringRes val labelRes/displayNameRes: Int`, with call sites resolving it via
  `stringResource(...)` (added `res/values/strings.xml` entries per enum; `ClockFontOption`/
  `LauncherFontOption` share `font_name_*` entries for the fonts both offer, so a translator only
  translates "Roboto Flex" etc. once). `SettingsSearchEntry.kt` itself needed no change (its
  `label: String` field is fine as-is) — its actual source, `SystemSettingsRepository`'s private
  `SETTINGS_CATALOG` (data-layer, not `data/model/`, but the only place these labels originate),
  got the equivalent treatment: `CatalogEntry.label: String` → `@StringRes labelRes: Int`,
  resolved via `context.getString(...)` directly in `search()` — since this is a real
  `Repository` with `Context` already injected, unlike the pure-Kotlin model enums, which have no
  `Context` and so resolve their label at render time via `stringResource()` in Compose instead.
  - **`LabeledDropdownRow.kt`'s `label` parameter changed from `(T) -> String` to
    `@Composable (T) -> String`** — a necessary, deliberate signature change (not a leftover from
    section 2) so call sites can call `stringResource()` inside the lambda they pass in. Every
    call site updated: `ClockStyleGalleryScreen.kt` (7 dropdown/label sites — Clock and Calendar
    style's font/color/date-style pickers), `AppearanceSettingsScreen.kt` (3 — Icons/Launcher
    Font/App label color pickers, plus one direct `AccentSwatchCircle` content-description),
    `CalendarSettingsScreen.kt` (one `AccentSwatch` content-description inside a
    `Modifier.semantics {}` block — that lambda isn't `@Composable`, so the string had to be
    resolved to a local `val` *before* entering the modifier chain, not inline inside `semantics {}`),
    `FontWeightSlider.kt`, `AppSortControl.kt` (its own hand-rolled dropdown, not
    `LabeledDropdownRow`), `SettingsScreen.kt` (the Clock & Calendar Style row's subtitle).
  - **Real accidental content loss, caught and fixed**: an early `Edit` on `AccentSwatch.kt`
    silently dropped an unrelated 4-line comment (the AMBER color's contrast-ratio rationale) that
    happened to sit between the edited regions — not something `old_string`/`new_string` should
    have touched. Restored it before continuing; a reminder to re-read a file's actual diff after
    an edit that touches a large block, not just trust the edit summary.
  - **Detekt's `ComposeHardcodedText` baseline count (461) is unaffected by this whole section —
    confirmed, not a bug.** Traced why: enum-entry constructor arguments (`ALPHABETICAL("Alphabetical")`)
    aren't `KtCallExpression` nodes in Kotlin's PSI, so the rule's `visitCallExpression` override
    never saw them; `SystemSettingsRepository`'s `CatalogEntry("wifi", "Wi-Fi", ...)` calls use
    *positional* (not named) arguments for a type that isn't `Text`/`BasicText`, which is exactly
    the rule's own documented "doesn't catch positional args outside Text/BasicText's first one"
    gap from section 0. Both are real, now-confirmed-with-concrete-examples instances of that gap,
    not new ones — these strings were only found via this section's deliberate scan, never via
    the automated gate. Worth remembering for sections 2–7: the detekt count going quiet doesn't
    mean a screen has no hardcoded strings left if those strings are one property-hop away from
    the actual `Text(...)`/`Icon(...)` call site.
  - **Unplanned but necessary infra fix, found via `./gradlew test` regressing**: Robolectric
    JVM unit tests had never had `testOptions.unitTests.isIncludeAndroidResources = true` set
    (`app/build.gradle.kts`) — `SystemSettingsRepositoryTest` became the first test in the whole
    suite to call `context.getString()` on an app-defined resource, and without that flag it threw
    `Resources$NotFoundException` even though the resource genuinely exists and the code compiles
    fine. Enabling it was necessary, but it also makes Robolectric load the app's *real*
    `AndroidManifest.xml` instead of a synthetic default one — which surfaced two more real gaps
    that were only ever hidden by the previous incomplete config, not introduced by this change:
    - `AppRepositoryTest`/`PrivateSpaceRepositoryTest` both already had a documented workaround for
      Robolectric's `ContextCompat.registerReceiver` permission quirk
      (`shadowOf(it).grantPermissions("org.robolectric.default.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")`)
      — that hardcoded `org.robolectric.default` prefix was Robolectric's *synthetic* package name;
      with the real manifest loaded, `ContextCompat` synthesizes the permission from this app's
      *real* package instead, so the grant silently stopped matching. Fixed by building the
      permission name from `context.packageName` instead of hardcoding the placeholder.
      (Briefly suspected this needed a manifest-level `<permission>` declaration instead — added
      one, confirmed via the merged-manifest output that it was real but didn't fix anything, and
      reverted it once the actual mechanism was found: this is purely a Robolectric shadow-side
      grant, unrelated to what the manifest declares.)
    - `DefaultLauncherRepositoryTest`'s `` `isDefaultLauncher falls back to the HOME-intent check
      when the role isn't available` `` asserted `false` on the assumption that "nothing resolves
      the HOME intent in Robolectric's fake PackageManager" — true only because the old config
      never loaded the real manifest; Facet's own `LauncherActivity` genuinely declares a HOME
      intent-filter, so once the real manifest loads, `resolveActivity` legitimately resolves to
      *this app itself* (correctly matching real single-launcher-installed device behavior) and
      the old assertion no longer held. Fixed by explicitly registering a *different* package as
      the HOME resolver, so the test now actually exercises "some other launcher is currently
      default" rather than accidentally relying on nothing being registered at all.
  - **Verification**: `./gradlew assembleDebug test` and `./gradlew :app:detekt` all green (585
    unit tests, including the 2 fixed Robolectric ones and the rewritten `DefaultLauncherRepositoryTest`
    case).

### 2. Shared components (`ui/components/`) — ✅ complete
- [x] `AppContextMenu.kt`, `FolderContentsSheet.kt`, `FolderTileContextMenu.kt`,
  `AppPickerScreen.kt`, `InheritOverrideCard.kt`, `GestureHintOverlay.kt`, `FacetScopeBadge.kt`,
  `RenameDialog.kt`, `HomeSurfacePreview.kt`, `ConfirmDialog.kt`, `BackButton.kt`,
  `AppSortControl.kt` — 37 new `strings.xml` entries + 1 `<plurals>`. `FontWeightSlider.kt`/
  `LabeledDropdownRow.kt` needed no further changes: section 1 already converted their own literals
  when it changed `LabeledDropdownRow`'s `label` param to `@Composable`. `ScreenHeader.kt` had none
  to begin with — its `title`/`subtitle` are caller-supplied params; only its own `@Preview` fixture
  data is literal, excluded per section 1's established precedent (`AppInfo`/`CalendarEvent` preview
  fixtures aren't real user-facing text).
  - **Heavy reuse across files**, grouped as shared entries at the top of the new `strings.xml`
    section rather than duplicated per file (same font_name_* precedent as section 1):
    `content_description_back` (`BackButton`, `AppContextMenu`'s folder-page chevron,
    `FolderContentsSheet`'s `AddHere` chevron — a real, previously-unnoticed duplicate: `BackButton.kt`
    had its own separate hardcoded `"Back"` this whole time, caught by re-scanning after the first
    pass showed a leftover hit), `action_cancel`/`action_save`/`action_reset`/`action_rename`/
    `action_done`, `folder_add_to_folder`/`folder_remove_from_folder`/`folder_create_new_folder`/
    `folder_new_folder_title`/`folder_name_this_folder`/`folder_rename_title`/
    `folder_rename_explanation`/`folder_no_apps_yet`, and a `quick_placement_add_to`/
    `quick_placement_remove_from` %1$s-templated pair (+ `quick_placement_favorites`/
    `quick_placement_dock` for the %1$s itself) backing `AppContextMenu.kt`'s
    `quickPlacementLabel()` — changed from a plain `fun` to `@Composable fun` so it can call
    `stringResource()` internally (all 4 call sites already ran inside composable context, so this
    was a safe, non-breaking signature change).
  - **New shared `<plurals name="folder_app_count">`** ("%d app"/"%d apps") replacing an *identical*
    `"${folder.apps.size} apps"` literal found independently duplicated in three files —
    `AppContextMenu.kt`, `AppPickerScreen.kt` (both in this section's scope) and
    `ui/settings/FoldersSettingsScreen.kt` (technically section 3's file, but fixed here too rather
    than leave one of three identical copies stale — see the i18n conventions comment at the top of
    `strings.xml` re: plurals for counts). Read via `pluralStringResource(id, count, count)`.
  - Two hardcoded directional glyphs in `GestureHintOverlay.kt` (`"←"`/`"→"`/`"↑"`) were moved to
    resources too, for mechanical consistency with everything else `Text(...)` renders there — not
    because they need translation, just because they're still literal `text =` arguments the same
    rule flags.
  - `app/detekt-baseline.xml` regenerated (`./gradlew --stop && ./gradlew :app:detektBaseline`):
    461 → 425 entries (`ComposeHardcodedText` 281 → 244). `./gradlew :app:detekt` green;
    `./gradlew test` full suite green.

### Real bug found and fixed via the plurals migration
The old `"${folder.apps.size} apps"` concatenation always said "apps" regardless of count — a
1-app folder read "1 apps". `pluralStringResource(R.plurals.folder_app_count, ...)` fixed this for
real (correctly renders "1 app" singular), which broke two *existing* instrumented tests that had
been asserting on the ungrammatical string: `AppContextMenuTest.tappingAddToFolderSlidesToTheFolderPageListingExistingFolders`
(`composeRule.onNodeWithText("1 apps")`) and `FoldersSettingsScreenTest.folderRowRendersNameAndAppCount`
(same). Both updated to assert `"1 app"` instead — caught only because
`ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` was run on-device for this
section (per this file's device-safety rule: `adb devices -l` confirmed only the emulator was
attached before running). Ran the full `ui.components` package (28 + 12 tests) plus
`AppearanceSettingsScreenTest` (14 tests) and `FoldersSettingsScreenTest` (6 tests) on
`Medium_Phone_API_36.1` — all green after the two fixes.

### 3. Settings screens (~218 strings across ~20 files — the largest surface) — ✅ complete
- [x] `SettingsScreen.kt` — nav hub, done first. Compound subtitles (clock style · time format,
  calendar count · all-day visibility, App Drawer's 3-part layout/contact-search/settings-search,
  notifications enabled/disabled) rebuilt as format-string templates
  (`dot_join_2`/`dot_join_3`/`comma_join_2`, reusable across this whole phase) instead of raw `+`
  concatenation. `dockSummary`/`folderCountSummary` switched to real `<plurals>` — both already had
  an explicit `if (count == 1) singular else plural` branch in the original code, so this was a
  correctness-neutral resourcing, not a new grammar decision (contrast `appsListSummary`'s
  "N Favorites"/"N Recents"/"N Most used", which never had such a branch — left as flat format
  strings, unchanged behavior, no new pluralization invented).
- [x] `NotificationBadgeStyle` centralized to `@StringRes displayNameRes` (`Dot`/`Count`) — fixed a
  real duplicate: `NotificationSettingsScreen.kt` had its own separate hardcoded `"Dot"/"Count"`
  mapping for the exact same enum. `NotificationSettingsScreen.kt` migrated alongside it (header,
  toggle row, dropdown title).
- [x] `AppDrawerSettingsScreen.kt` — `DrawerPresentation`/`DrawerListItemSize`/`SearchBarPosition`/
  `DrawerFolderDisplayMode` all centralized to `displayNameRes` (same pattern); `DrawerGridSize`
  stays a `@Composable` format-template function (`"%1$d cols × %2$d rows"`) since its label is
  computed from the enum's own numeric fields, not a fixed per-value string. Found and fixed a
  third-party duplicate while at it: `OnboardingHomeSetupPage.kt` (phase 4's own file) had its own
  separate `DrawerPresentation` "List"/"Grid" mapping — fixed in place rather than left stale, same
  reasoning as phase 2's `folder_app_count` cross-phase fix.
- [x] `HomeAppsListSettingsScreen.kt` — `AppRowPosition`/`AppRowPresentation`/
  `AppListVerticalAlignment`/`ListContentMode`/`DockDisplayMode` all centralized to `displayNameRes`.
  `ListContentMode`/`DockDisplayMode` each replaced an *independent* duplicate hand-rolled mapping in
  `ui/facets/FacetSettingsComponents.kt` (phase 5's file) — fixed there too (`internal fun
  ListContentMode.displayLabel()`/`DockDisplayMode.displayLabel()` now delegate to
  `stringResource(displayNameRes)`), which required marking `FacetSettingsScreen.kt`'s private
  `appsSubtitle()` helper `@Composable` too (its only caller was already in composable context).
- [x] `AppearanceSettingsScreen.kt` — the raw-string rows added earlier this session (font-size
  feature: Icon Style/Launcher Font/Font Color/Font Size/Font Weight) now resource-backed; `ThemeMode`
  centralized to `displayNameRes` (`Light`/`Dark`/`System`, its only consumer).
- [x] `backup/BackupRestoreScreen.kt` — header reuses `settings_backup_restore_title` (identical text
  to the Settings row that navigates here, no separate resource). "Restored N facet(s)"/"N widget(s)
  need re-adding" converted to real `<plurals>` — a fresh, correct plural form (`"...needs..."` singular
  verb vs `"...need..."` plural), not present in the original crude `"(s)"` notation, but a natural
  byproduct of writing genuine plurals rather than a deliberate grammar-fixing pass.
- [x] `PermissionsScreen.kt` + `PermissionsViewModel.kt` — the ViewModel builds `PermissionRowState`
  outside Composable context, so it needed `@ApplicationContext Context` injected (Hilt) and
  `context.getString(...)`, same as section 1's `SystemSettingsRepository` precedent. Broke both
  `PermissionsViewModelTest` (needed a `Context` constructor arg — fixed by stubbing
  `context.getString(anyInt())` to return `""`, since a bare Mockito mock returns `null` there, which
  crashes the non-null `PermissionRowState.title`/`subtitle` assignment and gets silently swallowed by
  `combine{}`, leaving `uiState` stuck at its empty default — a real "why is this 0 not 4" debugging
  dead-end worth remembering) and `PermissionsScreenTest` (needed the already-available real
  `LocalContext.current` passed as the new first constructor arg, at both its call sites).
- Verified: full `./gradlew test` green; on-device (`emulator-5554`) —
  `SettingsScreenTest` (19), `NotificationSettingsScreenTest` (6), `AppDrawerSettingsScreenTest` (6),
  `HomeAppsListSettingsScreenTest` (8), `FacetSettingsScreenTest` (12), `OnboardingScreenTest` (19),
  `AppearanceSettingsScreenTest` (14), `BackupRestoreScreenTest` (4), `PermissionsScreenTest` (4) —
  all green. `app/detekt-baseline.xml` regenerated across this whole batch: 425 → 365 entries
  (`ComposeHardcodedText` 244 → 183).
- [x] `DockSettingsScreen.kt` — used the just-centralized `DockDisplayMode.displayNameRes` directly
  (its own separate "Icons"/"Text" mapping was the third independent copy of that enum's labels
  found this phase, after `FacetSettingsComponents.kt`); header/"Drag to reorder" reuse
  `settings_dock_title`/`drag_to_reorder_content_description` (renamed from
  `home_apps_list_drag_to_reorder` once a second consumer showed up); "N of MAX" dock-count
  subtitle became a `%1$d of %2$d` format string.
- [x] `FoldersSettingsScreen.kt` — header/empty-state/create-dialog reuse `settings_folders_title`/
  `app_picker_no_folders_yet`/`folder_new_folder_title`/`folder_name_this_folder` (all already
  centralized this phase or in phase 2); "Delete folder" shared between the `ConfirmDialog` title
  and the row's own delete-icon `contentDescription` (identical text, one resource).
- [x] `CalendarSettingsScreen.kt` — "Calendars to display" reuses `settings_calendars_title`;
  "Turn on" reuses `permission_turn_on`.
- [x] `FolderDetailScreen.kt` — almost entirely reuse: `folder_add_to_folder`, `action_rename`,
  `folder_rename_title`/`folder_rename_explanation`, `drag_to_reorder_content_description`,
  `folder_remove_from_folder` — only its own empty-state sentence needed a new resource.
- [x] `AboutScreen.kt` — Version/Check for updates/Join the Discord rows + header, all new
  (no cross-file overlap).
- [x] `UsageAccessExplanationScreen.kt`/`NotificationAccessExplanationScreen.kt` — near-identical
  shape (headline + body + "Open settings" CTA); headers reuse
  `permission_usage_access_title`/`permission_notification_access_title`, the CTA text shares one
  `open_settings` resource between both screens. Two-part `"..." + "..."` string concatenation for
  each body paragraph collapsed into one resource string apiece.
- [x] `FolderAppPickerScreen.kt` — its one string (`"IN FOLDER"` section label) resourced; no
  ViewModel in this package needed changes beyond `PermissionsViewModel` (already done above).
- [x] Bonus: `AppDrawerSettingsScreen.kt`'s opacity-percentage display (`"${...}%"`) converted to a
  `percent_format` (`"%1$d%%"`) resource — caught in a final full-package hardcoded-string sweep,
  the only real hit remaining after excluding `@Preview` fixture data.
- Final verification: `./gradlew compileDebugKotlin/compileDebugAndroidTestKotlin/testDebugUnitTest`
  all green; on-device (`emulator-5554`), the entire `ui.settings` instrumented package —
  15 test suites, 99 tests, 0 failures (`AboutScreenTest`, `AppDrawerSettingsScreenTest`,
  `AppearanceSettingsScreenTest`, `CalendarSettingsScreenTest`/`...GrantedTest`,
  `DockSettingsScreenTest`, `FolderAppPickerScreenTest`, `FolderDetailScreenTest`,
  `FoldersSettingsScreenTest`, `HomeAppsListSettingsScreenTest`, `NotificationSettingsScreenTest`,
  `PermissionsScreenTest`/`...GrantedTest`, `SettingsScreenTest`, `backup/BackupRestoreScreenTest`).
  `app/detekt-baseline.xml` regenerated one final time for the phase: 365 → 333 entries
  (`ComposeHardcodedText` 183 → 151). Phase totals since the start of section 2:
  `ComposeHardcodedText` 281 → 151 (baseline 461 → 333 overall).

### 4. Onboarding (~65 strings — self-contained, isolable as one PR) — ✅ complete
- [x] `OnboardingScreen.kt` — one string ("Skip"). Its `DrawerPresentation` duplicate had already
  been fixed in phase 3 once `DrawerPresentation.displayNameRes` existed.
- [x] `OnboardingIntroPage.kt` — app title reuses `app_name`; the three gesture lines reuse
  `gesture_hint_arrow_up`/`right`/`left` (phase 2) for their glyphs; the diagram's "Dock" leader
  label reuses `settings_dock_title` (phase 3) — everything else new.
- [x] `OnboardingHomeSetupPage.kt` — "Show apps as"/"Apps to show"/"Drag to reorder" reuse phase-3
  resources; `ListContentMode`'s onboarding labels ("Recently used" for `RECENTS`) deliberately kept
  *separate* from `ListContentMode.displayNameRes` ("Recents") — friendlier first-run copy is a real,
  intentional wording difference from Settings, not a duplicate to collapse.
- [x] `OnboardingFacetsPage.kt` — "Back" reuses `action_back`; the three-part concatenated body
  string (`"\n..." + "..." + "\n\n..."`) collapsed into one resource, newlines preserved via `\n`
  escapes. `MOCK_FACETS`' invented app names/dates ("Bloom", "Wednesday, 26 August", etc.) left as
  plain `String` fields, not resourced — genuinely fictional illustrative content, not real
  detekt-flagged `Text(...)` call sites (positional args on a private data class, not a named
  text-carrying param), same category as section 1's documented detector gap.
- [x] `SetDefaultLauncherSheet.kt` — app-row label reuses `app_name`, "Done" reuses `action_done`
  (identical text to `AppPickerScreen`'s own "Done").
- [x] Bonus: `OnboardingDots.kt` (not in the original file list) — its `"Step %d of %d"`
  `contentDescription` caught in the final package sweep, converted to a `%1$d`/`%2$d` format string
  (moved out of the non-composable `semantics {}` lambda, same pattern as
  `CalendarSettingsScreen.kt`'s swatch content description in section 1).
- Verified: `./gradlew compileDebugKotlin`/`compileDebugAndroidTestKotlin`/`testDebugUnitTest` all
  green; on-device (`emulator-5554`) — `OnboardingScreenTest` (19) + `SetDefaultLauncherSheetTest`
  (6), 25/25, 0 failures. `app/detekt-baseline.xml`: 333 → 288 entries (`ComposeHardcodedText`
  151 → 106).

### 5. Drawer, Facets, Home, Hub — ✅ complete
- [x] `drawer/AppDrawerScreen.kt` (~29), `drawer/ContactConnectionsSheet.kt` (~13),
  `drawer/DrawerViewModel.kt` (0 — no UI-facing string literals, nothing to migrate),
  `drawer/AlphabetRail.kt` (~7), `drawer/PrivateSpaceScreen.kt` (~4)
- [x] `facets/FacetCarouselScreen.kt` (~19), `facets/ManageFacetsScreen.kt` (~15),
  `facets/FacetSettingsScreen.kt` (~13), `facets/FacetSettingsComponents.kt` (already 0, fixed in
  phase 3), `facets/FavoritesPickerScreen.kt` (already 0, fixed in phase 3)
- [x] `home/HomeScreen.kt` (~20), `home/clock/ClockStyleGalleryScreen.kt` (~25),
  `home/ClockAdjustSheet.kt` (~9), `home/ClockBlock.kt` (0 — only `@Preview` fixture data via the
  already-excluded `CalendarEvent` constructor, section 1's documented detector gap)
- [x] `hub/picker/HubWidgetPickerScreen.kt` (~10), `hub/OrphanedWidgetTile.kt` (~8),
  `hub/HubHeader.kt`, `HubGrid.kt`, `HubEmptyState.kt`, `HubAtCapacityStrip.kt` (small) — plus the
  rest of `ui/hub/` swept for completeness (`HubScreen.kt`, `HubWidgetTile.kt`,
  `WidgetGrabGesture.kt`, `WidgetResizeHandle.kt`, `HubUiState.kt`, `HubViewModel.kt`): all clean,
  the only string-shaped literals in them are `Log.d(TAG, "...")` debug calls, which the rule
  correctly ignores (lowercase `d` callee, not a Composable).
  - **Heavy reuse across files**: new shared `home_list_header_favorites`/`_recents`/`_most_used`
    (used by both `HomeScreen.kt` and `FacetCarouselScreen.kt`'s preview, which previously had its
    own separate, driftable copy of the same three labels); `dot_join_2`/`dot_join_3`/
    `comma_join_2` (phase 3) threaded through `FacetSettingsScreen.kt`'s inherit/override
    subtitles; `ClockAdjustSheet.kt`'s "Launcher settings"/"Facet settings" rows reuse
    `facet_carousel_launcher_settings`/`_subtitle`/`facet_carousel_facet_settings` outright rather
    than duplicating near-identical copy (this also **fixed a real casing inconsistency** —
    `ClockAdjustSheet`'s subtitle read "Appearance, default clock..." (lowercase `default`) against
    `FacetCarouselScreen`'s already-shipped "Appearance, Default clock..." (capitalized); both now
    render the one canonical string). `HubEmptyState.kt`'s "+" reuses `facet_carousel_add_glyph`;
    its "Add widget" button reuses `HubWidgetPickerScreen`'s own `hub_widget_picker_title`.
  - **New `<plurals name="hub_widget_picker_option_count">`** ("%d widget"/"%d widgets") replacing
    an existing `if (size == 1) "1 widget" else "$size widgets"` branch in
    `HubWidgetPickerScreen.kt` — already correctly singular/plural in the original code, just
    converted to a real resource per this migration's plurals policy.
  - **New pattern, first occurrence this migration**: `HubWidgetPickerScreen.kt`'s
    `AddFailureReason`-to-message mapping runs inside a `LaunchedEffect` — a suspend lambda, where
    `stringResource()` (a `@Composable` function) can't be called directly. Resolved both failure
    strings (`hub_widget_picker_full`/`_setup_cancelled`) to local `val`s in the composable body
    *before* the `LaunchedEffect` block, then referenced those captured strings from inside the
    coroutine closure — worth reusing this shape if another async event-handling callback needs a
    string resource.
  - `ClockStyleGalleryScreen.kt`'s own private `ClockAlignment.clockStyleGalleryDisplayLabel()`
    extension function (a `when` returning `"Left"`/`"Center"`/`"Right"`) was deleted entirely —
    `ClockAlignment` (`data/model/LauncherSettings.kt`) got a real `@param:StringRes
    displayNameRes` field instead, reusing `AppRowPosition`'s already-existing
    `app_row_position_left`/`_center`/`_right` strings (identical wording, same left/center/right
    concept) rather than adding three duplicate new ones — same centralization pattern as section 1.
  - `dashedBorder`, format args and `%1$d×%2$d`-style dimension strings
    (`hub_widget_dimensions`/`hub_widget_label_with_dimensions`) were used for `HubWidgetPickerScreen.kt`'s
    "4×2"-style grid-size labels rather than leaving them as raw string-template literals.
  - `CalendarEvent`'s "Team standup"/"Design review" sample preview data
    (`ClockStyleGalleryScreen.kt`'s calendar preview, `ClockBlock.kt`'s own `@Preview`) is real,
    on-screen content in the shipped app (not just an IDE `@Preview`) but deliberately excluded —
    confirmed by reading `ComposeHardcodedTextRule.kt`'s own `NON_COMPOSABLE_CONSTRUCTORS` doc
    comment, which names this exact fixture data as the reason `CalendarEvent` is on that allowlist.
- Verified: `./gradlew compileDebugKotlin`/`compileDebugAndroidTestKotlin`/`testDebugUnitTest` all
  green; on-device (`emulator-5554`, confirmed via `adb devices -l` before each run, package-scoped
  runs since a comma-separated multi-class `-Pandroid.testInstrumentationRunnerArguments.class`
  filter turned out to silently run only the *first* listed class — a real gap caught only because
  a re-check of the result XML showed 1 test instead of the expected ~200, not because the build
  reported anything wrong) — `ui.drawer` (42), `ui.facets` (49), `ui.home` incl. `ui.home.clock`
  (99), `ui.hub` incl. `ui.hub.picker` (24): 214/214 green. `app/detekt-baseline.xml`: 461 → 197
  entries (`ComposeHardcodedText` 106 → 15, all now outside this phase's own file list —
  `AppPickerScreen.kt`/`ScreenHeader.kt` from phase 2, `PermissionsScreen.kt` from phase 3,
  `ClockAccessoryRow.kt`/`ClockTemplates.kt`/`ClockZoneHandle.kt` in `ui/home/clock/` but never
  actually listed in this section's own file scope, and `FacetCarouselScreen.kt`'s one remaining
  hit is a legitimate invisible `alpha(0f)` spacer, not real text — left as known, pre-existing gaps
  rather than pulled into this phase's scope unannounced).

### Real bugs found and fixed this phase
1. **`FacetSettingsScreen.kt`**: `uiState.globalClockTemplateId.name.replace("_", " ")` rendered
   the enum's raw Kotlin name (e.g. "LIGHT STACK", wrong casing, untranslatable) instead of
   `ClockTemplateId.displayNameRes` (already centralized in section 1, just never wired up here).
   Fixed to `stringResource(uiState.globalClockTemplateId.displayNameRes)`.
2. **`FacetSettingsScreen.kt`**, found late via the post-migration `detektBaseline` diff (not the
   initial per-file catalog): `title = "Apps list settings"` at its Apps-list-settings row was
   missed on the first pass — the file's diff looked complete but the baseline regeneration still
   showed one `ComposeHardcodedText` hit here. New `facet_settings_apps_list_settings_title`
   resource; re-verified `FacetSettingsScreenTest` (12/12) green on-device after the fix. Kept as a
   reminder that "the file compiles and its own tests pass" isn't sufficient proof of full
   migration — the baseline diff is the actual completeness check.
3. **`HomeScreen.kt`**: its `ListContentMode` section header (FAVORITES/RECENTS/MOST USED) had its
   own separate string literals instead of the shared `home_list_header_*` resources — collapsed
   into the same three shared strings `FacetCarouselScreen.kt`'s preview now also uses (see reuse
   note above).

### 6. Special case — not a simple resource swap — ✅ complete
- [x] `home/clock/TimeInWords.kt` — user decided against English-only scoping: real per-locale
  grammar for Spanish, French, German, and Portuguese, not a vocabulary swap of the English
  construction. English fixed a real, undocumented bug along the way — `"Nine Five"` for `9:05`
  instead of the doc comment's own claimed `"Nine Oh Five"` (the `< 10` branch never actually
  prepended `"Oh"`). Per-language number-word tables (0–59) plus each language's own hour/minute
  connector and grammar: Spanish `"Once y Veintidós"` / `"Doce En Punto"` (hour 1 → feminine
  `"Una"`, minutes stay masculine `"Uno"` — `hora` is feminine, `minuto` isn't); French `"Onze
  Heures Vingt-Deux"` / `"Douze Heures"` (both hour *and* a units-digit-1 minute → `"Une"` — both
  `heure`/`minute` are feminine; hyphenated tens-compounds except `"et Une"` for ×1); German `"Elf
  Uhr Zweiundzwanzig"` / `"Zwölf Uhr"` (units-before-tens compounds, `"einundzwanzig"`; hour 1
  contracts to `"Ein Uhr"`, minute 1 stays uncontracted `"Eins"`); Portuguese `"Onze e Vinte e
  Dois"` / `"Doze Em Ponto"` (same feminine-hour-only pattern as Spanish). `timeInWords()` now
  takes a `locale: Locale` param (`ClockTemplates.kt`'s `SpelledOutTemplate` threads its own
  already-available `locale` through, previously unused for this call). New
  `TimeInWordsTest.kt` (26 cases) — a testing gap identified and filled per direct request: hour
  wrap-around, minute edge cases (0/1–9/10–19/tens), every language's own gender-agreement rule,
  and the locale-fallback path, one test per behavior.

### 7. Non-Compose user-visible text — ✅ complete
- [x] Checked `data/`/`domain/` for Toast, notification, or widget-host text shown outside Compose:
  no hits. `FacetNotificationListenerService` only *reads* notifications (for badge counts), never
  posts its own; the app hosts other apps' `AppWidgetHostView`s (`AppWidgetRepository.kt`,
  `LauncherAppWidgetHost.kt`) rather than rendering its own `RemoteViews` text. A broader sweep of
  every `"[A-Z][a-z]+ [a-z]` string literal across `data/`/`domain/` turned up only KDoc comments
  referencing real UI copy by name for documentation (e.g. `"Search contacts" toggle`), not actual
  hardcoded user-visible literals.

### 8. Wrap-up — ✅ complete
- [x] `./gradlew lint` confirms zero `HardcodedText` findings (expected and unchanged from section
  0's own finding — that check only inspects XML `android:text`, useless on this Compose-only app)
  and, more usefully, **zero `UnusedResources` findings** — no orphaned `strings.xml` entries from
  the whole migration's reuse-first discipline.
  - Lint's `PluralsCandidate` check (unrelated to `HardcodedText`, found incidentally) flagged 8
    `%d`-plus-word strings as possible missing plurals. Fixed the 4 that are real, reachable
    grammar bugs — `format_count_apps` ("1 apps"), `settings_calendars_selected` ("1 calendars
    selected"), `settings_favorites_count` ("1 Favorites"), `settings_recents_count` ("1
    Recents") — converted to `<plurals>` + `pluralStringResource()` at their 8 call sites across
    `FacetSettingsScreen.kt`/`SettingsScreen.kt`. Left the other 4 alone: `settings_most_used_count`
    and `hub_widget_picker_remaining` ("%d left") don't actually change wording between singular
    and plural in English (false positives); `drawer_grid_size_label` (`DrawerGridSize` columns/
    rows are fixed enum constants, never 1) and `hub_header_subtitle` (`HUB_MAX_WIDGETS = 20`,
    never 1) can't structurally reach the singular case with current code — matches this
    migration's standing conservative-pluralization policy (section 2's real-bug note) rather than
    introducing unreachable grammar branches.
- [x] `docs/architecture/README.md`'s table has no row for "add a `strings.xml` entry" — string
  resources aren't part of that doc's architecture-boundary/component-registry scope (it tracks
  Hilt modules, repositories, DB schema, nav destinations, etc., not UI copy), so no update needed.

## Language translation (i18n) — Pass 2: Spanish, French, German, Portuguese — ✅ complete

Real translation into `values-es/strings.xml`, `values-fr/strings.xml`, `values-de/strings.xml`,
and `values-pt/strings.xml` — every one of Pass 1's 437 `<string>` entries and 12 `<plurals>`
translated, not machine-generated, for all four locales. `values-pt` uses Brazilian Portuguese
vocabulary (`aplicativo`/`app`, `salvar`, `tela`) rather than European Portuguese, matching where
the larger Android-using population sits; German keeps `App`/`Apps`/`App-Drawer` as established
loanwords the same way Spanish/French kept `app`.

- [x] Verified programmatically before any on-device check: a resource-name diff between
  `values/strings.xml` and each new file (zero missing, zero extra in both directions) and a
  per-resource format-specifier diff (`%1$s`/`%1$d`/etc., positions and types) — a mismatch here
  would crash at runtime rather than just look wrong, so this ran before `./gradlew
  processDebugResources` rather than relying on that catching it. All four locales matched exactly
  on the first pass. `./gradlew processDebugResources` (real AAPT compilation, not just XML
  well-formedness) green for all four.
- [x] Verified for real on an emulator — not just "it compiles." Android's per-app `LocaleManager`
  override (`adb shell cmd locale set-app-locales com.facetlauncher.app --user 0 --locales <lang>`)
  rather than changing the emulator's system-wide locale, since the latter would've disrupted any
  other test run sharing this device. Screenshotted the onboarding flow in all four languages —
  confirmed real, correct rendering, not just "some text changed."
  - **Caught and fixed a real bug this way that the whole Pass 1 migration missed**:
    `OnboardingUiState.kt`'s `dockCountLabel`/`favoriteCountLabel` were plain Kotlin computed
    properties doing raw string interpolation (`"${dockApps.size} of
    ${DockAppRepository.MAX_APPS}"}`) — outside any Composable, so `ComposeHardcodedText` could
    never see it (it only scans Compose UI call arguments). Confirmed on-device: the onboarding
    "Favorites"/"Manage dock apps" rows showed "0 of 6"/"5 of 5" in literal English even with the
    default locale active, while every other string on the same screen was correctly translated.
    Fixed by deleting both computed properties and resolving `stringResource()` at the two call
    sites in `OnboardingHomeSetupPage.kt` instead (both already inside `LazyColumn`'s `item {}`,
    a real Composable context) — reusing `DockSettingsScreen.kt`'s existing identical `"%1$d of
    %2$d"` string, renamed from the dock-specific-sounding `dock_apps_count_of_max` to the
    honestly-generic `format_count_of_max` since it's now shared across two unrelated screens (all
    3 call sites + all 4 locale files updated together). This is exactly the kind of gap manual
    on-device verification catches that a static rule structurally cannot — the German and
    Portuguese passes re-checked this exact spot ("0 von 6"/"5 von 5" and "0 de 6"/"5 de 5") and
    confirmed the fix holds across all four locales.
  - Caught the same locale-override tooling causing 3 *false* on-device test failures
    (`OnboardingScreenTest`/`SetDefaultLauncherSheetTest` expecting exact English text) — the
    per-app locale override persists across `adb install -r` reinstalls of the same package, so
    manually testing a non-English locale on the emulator and then immediately running the
    instrumented suite on that same still-installed package leaked the override into the test run.
    Fixed by resetting the override (`--locales ""`) before rerunning. Applied this discipline for
    every locale across both the Spanish/French and German/Portuguese passes: reset the override
    immediately after each on-device screenshot check, before the next instrumented run.
- [x] Full unit suite green (615/615) after all four locales; `OnboardingScreenTest` (19),
  `SetDefaultLauncherSheetTest` (6), and `DockSettingsScreenTest` (7) all re-verified clean on the
  emulator with the locale override reset each time. `app/detekt-baseline.xml` unchanged (213
  entries) — this pass touched only resource files and a computed-property removal, no new
  hardcoded-string surface.

Play Store listing translation remains open — every in-app string across all four locales
(Spanish, French, German, Portuguese) is now translated and verified.

## App-wide font size control (`FontScaleOption`) — ✅ complete

A global, user-adjustable text-size scale for every text role in the app except the clock
(`FacetType.clock` — already exempt, since it has its own widget-size adjustment via
`clockScale`/`ClockBlock`). Modeled on `LauncherFontOption`: one global setting, not facet-overridable
and not split per-surface — readability preference is closer in kind to `launcherFontOption`/
`homeAppsFontWeight` than to the clock/calendar's per-facet decorative styling, so it stays a single
shared knob (see chat history for the calendar-vs-launcher styling discussion this fell out of).

Along the way, two related gaps got fixed in the same pass (direct request):
1. **Calendar event text** (`CalendarEventsBlock.kt`) was reading a hardcoded `fontSize = 16.sp`,
   independent of the app's type scale — switched to `MaterialTheme.typography.bodyLarge.fontSize`
   (over `titleMedium`, same 16sp M3 default, but `bodyLarge`'s wider 0.5sp letter-spacing read
   better — direct request) so it inherits the new scale for free, no new param needed.
2. **`homeAppsFontWeight` didn't reach Settings.** It was only ever applied via explicit
   `.copy(fontWeight = ...)` at Home's own app-list/Dock/Drawer labels — Settings' own rows
   (`LabeledDropdownRow`, `SettingsScreen`'s list rows, all `bodyLarge`/`bodyMedium`) never read it.
   Fixed by also feeding it into `facetTypography()` as the global `bodyLarge`/`bodyMedium`/
   `bodySmall` weight — deliberately **not** `title*`/`label*`/`headline*`/`display*`: a scan of
   every `titleMedium`/`titleLarge`/`titleSmall` consumer (`ConfirmDialog`, `RenameDialog`,
   `AppContextMenu`, `FolderTileContextMenu`, onboarding sheets, `FacetCarouselScreen`) found they
   all rely on Material's own Medium weight for dialog/menu titles; blanket-overriding those by
   default would have flattened every dialog title app-wide the moment this shipped, not just where
   a user actually touches the new slider. Restricting to body roles matches `FontWeightOption`'s
   own documented scope ("regular text... never headlines") and is a no-op at the default `REGULAR`
   value (M3's own `bodyLarge`/`bodyMedium`/`bodySmall` are already `FontWeight.Normal`), so nothing
   visually changes until a user picks something else. Home's own labels keep their existing
   explicit `.copy(fontWeight = ...)` unchanged — redundant with the new global value there, but
   harmless, and still needed since `titleMedium` itself isn't covered by the global override.

### 1. `FontScaleOption` enum — `data/model/FontScaleOption.kt`
Five named stops: `SMALL(0.85f)`, `DEFAULT(1.0f)`, `LARGE(1.15f)`, `EXTRA_LARGE(1.3f)`, `HUGE(1.45f)`,
each with `@param:StringRes val displayNameRes: Int` and `val scale: Float` — same shape as
`FontWeightOption`, a stepped `Slider` walking `entries` by index.

### 2. `Type.kt` — scale every Material role except the clock
`facetTypography(fontFamily, fontWeight: FontWeight = FontWeight.Normal, fontScale: Float = 1f)`.
A private `TextStyle.scaledBy(fontFamily, fontScale, fontWeight?)` extension replaces the old
per-role `.copy(fontFamily = ...)` repetition — `fontScale` multiplies every role's real
`fontSize`/`lineHeight`; `fontWeight` (non-null) only passed for `bodyLarge`/`bodyMedium`/
`bodySmall`, every other role keeps its own default weight (see rationale above). `FacetType.clock`
stays its own untouched `object` — exempt by construction.

### 3. Hardcoded-size audit — no action needed
Full-codebase scan (`.sp`/`fontSize`/`TextUnit(`/`.em`/`dimensionResource`) confirmed every hardcoded
font size lives in `ClockTemplates.kt`/`ClockAccessoryRow.kt` (the clock template gallery) plus
`Type.kt`'s own `FacetType.clock` — all correctly exempt. The only hits outside those files are
`AppDrawerScreen.kt:538`/`FacetCarouselScreen.kt:734` (`FacetType.clock.copy(fontSize = ...)`, an
empty-state glyph and a "+" overflow tile) and `AlphabetRail.kt:162`'s folder-rail glyph, which
already derives its size from `MaterialTheme.typography.labelSmall.fontSize.toDp()` — picks up the
new scale for free. Confirmed step 2 alone is sufficient; no other call site needed touching.

### 4. Persistence — `SettingsRepository`/`LauncherSettings`
`LauncherSettings.fontScaleOption: FontScaleOption = FontScaleOption.DEFAULT` (global only, same
tier as `launcherFontOption`); `Keys.FONT_SCALE_OPTION = stringPreferencesKey("font_scale_option")`,
decode/fallback in the `settings` flow, `suspend fun setFontScaleOption(option: FontScaleOption)`.

### 5. Theme wiring
`FacetLauncherTheme` gains `homeAppsFontWeight: FontWeightOption` and
`fontScaleOption: FontScaleOption` params, both passed into `facetTypography(...)`.
`LauncherUiState`/`LauncherViewModel`'s `combine(...)` block carry both through from `settings`
(mirroring `launcherFontOption`); `LauncherActivity.kt` passes both to `FacetLauncherTheme`.

### 6. UI — `FontSizeSlider` + `FontWeightSlider` gains `showPreview`/`label`
New `ui/components/FontSizeSlider.kt`, same shape as `FontWeightSlider.kt` (stepped `Slider` over
`FontScaleOption.entries`, `@Preview`s incl. all-stops/dark). `FontWeightSlider` gained
`label: String = "Weight"` and `showPreview: Boolean = true` params — both sliders in
`AppearanceSettingsScreen` now pass `showPreview = false` (the screen's own `HomeSurfacePreview` card
already shows live text above them, so each slider's own sample-text row was redundant, direct
request); the calendar style gallery's own `FontWeightSlider` also passes `showPreview = false` (its
`CalendarEventsBlock` preview renders right below), keeping its default `"Weight"` label unchanged.
Appearance's Font block final order/labels, all direct request: **Launcher Font → Font Color → Font
Size → Font Weight** — "Icons" renamed to **"Icon Style"**, "App label color" moved and renamed to
**"Font Color"** (right after Launcher Font, ahead of the two sliders), the two sliders relabeled
from "Text Size"/"Text Weight" to **"Font Size"**/**"Font Weight"**. `testTag`s left unchanged
throughout (`appearance_icons_row`, `appearance_app_label_color_row`, etc.) since they're not
user-visible.

### 7. Backup/restore
`BackupBundle.kt`: `val fontScaleOption: String = FontScaleOption.DEFAULT.name` on `BackupSettings` —
defaulted, tolerant-reader discipline, no `CURRENT_BACKUP_VERSION` bump (same as
`calendarFontWeight`/`homeAppsFontWeight`). Wired through `ExportBackupUseCase`/`ImportBackupUseCase`
the same way as `homeAppsFontWeight`.

### Tests
Unit: `TypeTest` gained three `facetTypography` cases (scales every role's `fontSize`/`lineHeight`
at 1.3x; leaves `fontSize` untouched at the default 1x; applies `fontWeight` only to
`bodyLarge`/`bodyMedium`/`bodySmall`, never `titleMedium`/`labelLarge`/`headlineSmall`).
`SettingsRepositoryTest` gained a default-value assertion plus a round-trip case (folded into the
existing bulk round-trip test alongside `launcherFontOption`). `AppearanceSettingsViewModelTest`
gained a setter-call case. Instrumented: `AppearanceSettingsScreenTest` gained
`fontSizeSliderChangesTheSetting`, mirroring the existing `homeAppsFontWeightSliderChangesTheSetting`
(drags `appearance_font_size_slider_control` to its last stop, asserts the repository settles on
`FontScaleOption.HUGE`) — not yet run on-device: no emulator was booted in this session (`adb
devices -l` was empty); run `./gradlew connectedDebugAndroidTest` on `Medium_Phone_API_36.1` before
considering this fully verified. Full unit suite (`./gradlew test`) passes.
`docs/architecture/02-persistence-room.md` (key diagram/writer table/section table, key/writer/field
counts 49→50/51→52/49→50, backup key count 34→35) and
`13-flow-facets-theme-notifications-onboarding.md` (theme resolution diagram + the
weight-scope-restriction rationale) updated; `TEST_REGISTRY.md` regenerated.

---

## ✅ Home Clock Widget Substitution (PRD F15) — mostly complete, plan updated to match actual code state (see chat history: this section had drifted well behind what was actually built)

Lets the user replace a facet's Home clock with a single hosted third-party `AppWidget`, reusing
F5's Hub widget-hosting infrastructure rather than a parallel system. Position reuses the clock's
existing move-handle system unchanged; **scale becomes real dp width/height pushed to the widget's
provider** (`updateAppWidgetOptions`/`OPTION_APPWIDGET_SIZES`, same mechanism Hub's own resize
already uses — see `AppWidgetRepository.kt:174`/`HubWidgetTile.kt`), not the clock's own continuous
`clockScale` graphics-layer multiplier, since a hosted widget's `RemoteViews` need to actually
reflow at the new size rather than being visually stretched. **Facet-level only, revised from the
original design** — each facet owns its own independent widget choice (own `appWidgetId`), not a
single shared instance with a per-facet position override; unset defaults to that facet's native
clock, and picking a widget from that facet's sheet *is* the override, with no separate
Inherit/Override switch (unlike every other Clock-card setting). Tradeoff (loses clock theming —
accent color, launcher font, calendar overlay — once a third-party widget occupies the slot) is
explicitly accepted, not something to design around.

- [x] **Data model**: nullable `clockWidgetAppWidgetId: Int?` + `clockWidgetWidthDp`/`clockWidgetHeightDp: Int?` on `FacetEntity` (facet-scoped, not `LauncherSettings`) + `FacetRepository.setClockWidgetAppWidgetId`/`setClockWidgetSize` persistence; `FacetDatabase.VERSION` (now 24) migrations add all three columns (`Migrations.kt`).
- [x] **UI entry point**: **"Use custom widget"** row in `ClockAdjustSheet.kt`, alongside "Adjust size & position" / "Edit … styles" — sheet content is state-dependent (hosted widget active hides "Edit … styles", adds "Switch to launcher clock widget" via `SwitchFacetToNativeClockUseCase`).
- [x] **Rendering**: `ClockBlock.kt` has a hosted-widget branch — when `clockWidgetAppWidgetId` is set, it renders an `AppWidgetHostView` (via `HomeViewModel.createClockWidgetHostView`) in place of `ClockDisplay`, inside the clock's existing position/handle chrome.
- [x] **Resize mechanism**: `ClockWidgetHostController`/`ClockWidgetFacetController` drive live real dp width/height on drag, pushing the committed size to `AppWidgetRepository.updateWidgetSize` on release, clamped to the provider's declared minimums plus the screen-fit clamp.
- [x] **Picker**: `ClockWidgetPickerScreen`/`ClockWidgetPickerViewModel` — a thin, facet-scoped variant of the Hub's widget picker (reuses `HubAddWidgetEvent`/`HubWidgetPickerUiState`/`WidgetProviderGroup` directly, `remaining` always `null` since a facet has exactly one clock slot), writing the bound id to the active facet's `clockWidgetAppWidgetId`.
- [x] **Lifecycle reuse**: `ClockWidgetPickerViewModel` runs the full allocate → bind (bind-permission detour) → configure (provider config-activity detour) → finish round trip, mirroring `HubWidgetPickerViewModel`'s own orchestration shape.
- [x] **Uninstall/orphan handling**: `createClockWidgetHostView` returns `null` when the provider is gone (see `ClockBlock.kt`'s own doc), and `ClockBlock` falls back to rendering nothing hosted rather than crashing; ids aren't left dangling.
- [x] **Facet deletion**: `DeleteFacetUseCase` releases `clockWidgetAppWidgetId` via `AppWidgetRepository.deleteAppWidgetId` before dropping the `FacetEntity` row — same "don't leak ids" rule F5 already applies to Hub widget removal.
- [ ] **Backup/restore**: `BackupBundle`/`BackupMapping` still don't carry `clockWidgetAppWidgetId`/`clockWidgetWidthDp`/`clockWidgetHeightDp` — genuinely not done yet; needs the same per-facet field treatment as other widget ids, with F14's existing "re-binding isn't fully automatic" caveat applying here too.
- [ ] **Docs**: `02-persistence-room.md` covers the new columns/migration, but `03 §4` (Home startup sequence), `10` (widget host lifecycle), and `13` (facet Clock-card settings) still don't mention this feature — update per CLAUDE.md's table.
- [x] **Tests**: unit (`ClockWidgetHostControllerTest`, `ClockWidgetFacetControllerTest`, `SwitchFacetToNativeClockUseCaseTest`, `DeleteFacetUseCaseTest`, `FacetRepositoryTest`, `HomeViewModelTest`, `HomeUiStateTest`); instrumented (`ClockBlockTest`); migration coverage in `FacetDatabaseMigrationTest`.

---

## 📝 Planned, not started — Calendar/Appearance styling consolidation + facet-symmetric Appearance overrides

Design-only so far (see chat history for the full back-and-forth this fell out of). Calendar's own
styling controls (`calendarFontWeight`/`calendarFontOption`/`calendarColorOption`/`calendarAlignment`,
today a "Calendars" card inside `ClockStyleGalleryScreen`) largely duplicate knobs that already exist
elsewhere for the same purpose, at both the global and per-facet layer. Consolidating them removes
settings sprawl; doing it well surfaces a second, larger gap — `AppearanceSettingsScreen`
(`homeAppsFontWeight`/`launcherFontOption`/`fontScaleOption`/`appLabelColorOption`) is the only
facet-customizable-looking settings category with **no** actual per-facet override mechanism, unlike
Apps/Dock/Clock+Calendar/Calendar-selection which all have one. Split into two parts so the
lower-risk consolidation can ship and settle before the bigger override feature starts.

**Decisions locked in from discussion, not to be re-litigated without new information:**
- **Font size** needs no work — `CalendarEventsBlock` already reads `MaterialTheme.typography.bodyLarge`,
  already scaled app-wide by `fontScaleOption` (`Type.kt`).
- **Font weight** (`calendarFontWeight` → `homeAppsFontWeight`) and **font family**
  (`calendarFontOption: ClockFontOption` → `launcherFontOption: LauncherFontOption`) are clean merges —
  same underlying 5-font set, `ClockFontOption`'s only extra member (`LAUNCHER_DEFAULT`) is a
  passthrough sentinel that becomes moot once calendar has no independent font choice.
- **Color** (`calendarColorOption` → `appLabelColorOption`, not `clockColorOption`/`clockAccentColorOption`):
  all four already share the `ClockColorOption` type, but `clockAccentColorOption` is only read by
  templates where `usesAccentColor` is true (bad fit — calendar color would silently no-op on other
  templates), and `clockColorOption` risks tying calendar's body-like text to what's often a
  deliberately bold/loud clock-statement color. `appLabelColorOption` (default `THEME`) matches the
  weight/family decision's direction and calendar's actual role as smaller, denser, list-like text.
- **Alignment** (`calendarAlignment` → `clockAlignment`): both fields are already per-facet-overridable
  today, and `ClockStyleGalleryScreen`/`ClockStyleGalleryViewModel` are already dual-mode (global route
  `CLOCK_STYLE_GALLERY` vs facet-scoped `FACET_CLOCK_STYLE_GALLERY/{facetId}`, branching on a `facetId`
  read off `SavedStateHandle`) — this merge rides on existing machinery, no new facet work needed.
- **Preview**: `ClockStyleGalleryScreen`'s embedded `CalendarEventsBlock` is the *only* live
  calendar-styling preview in the app (`AppearanceSettingsScreen`'s `HomeSurfacePreview` has no clock/
  calendar rendering at all) — losing the Calendars card must not lose that preview. Keep the
  `CalendarEventsBlock` call, drop only the control rows above it; it already reads live settings
  state, so it keeps working as a read-only "here's what your calendar looks like" strip.
- **Nav**: "Clock style" moves from a top-level `SettingsScreen` row into a row inside
  `AppearanceSettingsScreen` (same `CLOCK_STYLE_GALLERY` route, different entry point). "Calendars"
  (which-calendars-to-show, `CalendarSettingsScreen`) is unaffected and stays where it is — its
  "Clock & Calendar" section in `SettingsScreen` loses its other row and should be renamed or folded
  into "Home & Apps" rather than left as a one-item section.
- **Full preview**: give Appearance a live mockup (wallpaper + clock + apps + dock), matching what
  `FacetCarouselScreen`'s private `FacetPreviewPage` already renders for facet previews. Don't build a
  third independent implementation alongside `FacetPreviewPage` and `HomeSurfacePreview` (which
  already duplicate the same wallpaper/apps/dock scaffold) — extract a shared "mini home preview"
  composable both consumers call, parameterized cleanly rather than `FacetPreviewPage`'s current ~30
  flat params. `ClockBlock` is already proven safe in a compact/scaled, non-interactive context (no
  gesture code baked in — drag-to-resize lives externally in `HomeScreen.kt`; `FacetPreviewPage`
  already renders it scaled via `clockScale` + a measured `LocalDensity`), so folding it into the
  shared composable is low-risk.
- **Facet-level Clock folds into the same Appearance override, not its own category** (revised —
  supersedes the original Part B sketch, direct request): mirroring Part A's global nav move, the
  facet-scoped "Clock style" row moves inside the facet Appearance screen too, and
  `FacetSettingsScreen`'s separate Clock+Calendar `InheritOverrideCard` goes away — one Appearance
  `InheritOverrideCard` now gates clock styling too. "Calendars" (which-calendars-to-show, backed by
  `overrideCalendar`) stays its own independent section — data selection, not style, unaffected by
  this. The Appearance section also moves higher in `FacetSettingsScreen`'s row order (right after
  "Rename facet", ahead of Apps list/Dock) since it now governs the facet's overall look, not just one
  slice of it.
- **Per-field override granularity, facet-only — reuses the existing `LAUNCHER_DEFAULT` sentinel
  pattern, not a new mechanism** (corrected — supersedes an earlier sketch of this section that
  invented nullable columns and a new paired selector widget; direct correction: "this is already
  supported today — calendar and clock font options already support launcher default as a choice.
  this same concept should be extended"). `ClockFontOption` already ships exactly this: a
  `LAUNCHER_DEFAULT` member (`ClockFontOption.kt:20`) that "doesn't pick a font of its own; it resolves
  to whatever `LauncherFontOption` ... is currently set to" — used as the *default value* of both
  `LauncherSettings.clockFontOption` and `FacetEntity.clockFontOption`, with no separate override
  boolean or nullable column involved (`FacetEntity.kt:32`/`40`). Extending "this same concept" means:
  add an equivalent sentinel member to each enum backing a field that should gain per-field facet
  granularity — `FontWeightOption` and `FontScaleOption` need one (note `FontScaleOption` already has a
  member literally named `DEFAULT(1.0f)` — the concrete "normal size" stop — so the new sentinel must
  be named `LAUNCHER_DEFAULT` like `ClockFontOption`'s, not `DEFAULT`, to avoid colliding with it), and
  any color enum gaining new facet reach needs the same. `FacetEntity` columns stay **non-null**,
  exactly like `clockFontOption` today — no nullable-column migration, no new override-tracking field.
  Dropdown-backed fields surface the sentinel as one more `LabeledDropdownRow` entry, same as
  `clockFontOption` today. **Sliders are the one exception, direct request**: a slider's own step range
  has no clean ordinal spot for "inherit" (is it below Small? its own category?), so
  `FontSizeSlider`/`FontWeightSlider` get a small paired dropdown instead — "Launcher default" /
  "Override" — sitting above the slider, facet-mode only; picking "Override" enables the slider
  (seeded at the current resolved value, not an arbitrary stop) and writes a concrete value; picking
  "Launcher default" disables the slider and writes the `LAUNCHER_DEFAULT` sentinel. The underlying
  data model is unchanged by this — still the same non-null enum + sentinel value as every other
  field, just a different UI presentation for these two specifically.
- **The one real difference from the existing pattern**: for clock fields, `LAUNCHER_DEFAULT` is
  meaningful even in the *global* `AppearanceSettingsScreen`/`ClockStyleGalleryScreen`, because
  `clockFontOption` is a genuinely separate field from `launcherFontOption` at every scope — "should
  the clock specifically follow the launcher font, or have its own?" is a sensible question globally,
  not just per-facet. For the *new* Appearance-proper fields, the global screen **is** where the
  launcher default itself gets defined, so offering "use launcher default" there is circular and must
  not appear — the sentinel option on these specific fields is filtered out of the option list when the
  screen is in global mode, and only shown in facet mode. Existing clock fields (`clockFontOption`,
  `clockColorOption`, `clockAccentColorOption`, etc.) keep their current global-and-facet availability
  unchanged.
- **Correction: `launcherFontOption` belongs in the new facet-overridable set after all** — an earlier
  pass here wrongly said it "needs no sentinel... nothing above it to defer to" and dropped it from the
  new-fields list. That's true only of the *global* copy (correct, nothing above global to defer to);
  the *facet* copy of `launcherFontOption` needs exactly the same treatment as
  `homeAppsFontWeight`/`fontScaleOption`/`appLabelColorOption` — a facet should be able to run its own
  font independent of the launcher default. Fix: `LauncherFontOption` gains its own
  `LAUNCHER_DEFAULT` sentinel (mirroring `ClockFontOption`'s), and `launcherFontOption` rejoins the four
  new non-null `FacetEntity` columns from Part B's task list below.
- **New: a `FACET_DEFAULT` sentinel for clock's own style fields, chaining through the facet's
  Appearance instead of jumping straight to global** (direct request). Today `ClockFontOption`'s
  `LAUNCHER_DEFAULT` always resolves to the *global* `launcherFontOption`, even for a facet that has
  its own overridden font — so customizing a facet's font doesn't flow through to that facet's clock
  without a second, manual edit to `clockFontOption`. Reason given: "I can define basic facet changes —
  like font style — and want it to reflect in my clock too, if it consumes facet default, instead of
  manually changing it to match." Fix: `ClockFontOption` gains a third member, `FACET_DEFAULT`,
  resolving to *this facet's own* resolved `launcherFontOption` (i.e. `facet.launcherFontOption ==
  LAUNCHER_DEFAULT ? global.launcherFontOption : facet.launcherFontOption` — the same resolution
  Part B's Appearance screen already does, just consumed by the clock too). `FACET_DEFAULT` and
  `LAUNCHER_DEFAULT` behave identically until the facet actually overrides its own font, at which point
  `FACET_DEFAULT` follows it and `LAUNCHER_DEFAULT` deliberately doesn't (kept as an explicit "pin this
  facet's clock to the global font regardless" escape hatch). Since `FACET_DEFAULT` strictly dominates
  `LAUNCHER_DEFAULT` at facet scope, **`FacetEntity.clockFontOption`'s default value becomes
  `FACET_DEFAULT`**, not `LAUNCHER_DEFAULT` (existing facets migrate to it — see task below).
  `FACET_DEFAULT` is meaningless at global scope (no facet to chain through) — filtered out of the
  option list there, same technique as the other facet-only sentinel entries; `LAUNCHER_DEFAULT` stays
  the global default, unchanged.
  **Resolved: color does *not* get the same `FACET_DEFAULT` chain-through treatment as font** (direct
  discussion, settled). Font needed it because every font pick is fully independent — two facets can
  pick genuinely different fonts with nothing shared between them, so "chain through the facet's own
  choice" was the only way to avoid a second manual edit. The `FACET_DEFAULT`-for-color question was
  originally reasoned from "the accent swatch is a single global primitive" — that premise is corrected
  below (accent color *does* become facet-overridable), but the conclusion still holds for a different
  reason: font's ask was specifically "auto-follow so I don't have to manually re-sync," and a facet can
  already directly pick `ACCENT_PRIMARY` for its own `clockColorOption` with no chaining mechanism
  needed for that to work. So: `ClockColorOption` gains a `LAUNCHER_DEFAULT` member only (parallel
  structure to `ClockFontOption`, no `FACET_DEFAULT`), used for `clockColorOption`/
  `clockAccentColorOption` at both scopes — a facet can explicitly defer to the *global* clock color, or
  pick one of the four regular options directly and facet-independently, same as it already can today.
- **Correction: accent color (`accentFromSystem`/`customAccentSwatch`/`wallpaperAccentRole`) *does*
  belong in Part B's facet-overridable set** — an earlier pass silently left it out along with
  `themeMode`/`iconRenderMode` (all three share one doc-comment tier in `LauncherSettings.kt:167-168`,
  "global only... not facet-overridable") without a deliberate call being made either way. Checked and
  confirmed: `FacetEntity` has no per-facet wallpaper column, and none is planned in this phase — but
  that doesn't block this. `accentFromSystem`/`customAccentSwatch`/`wallpaperAccentRole` govern *which*
  tonal role of the one shared wallpaper gets used, or whether to bypass it for a manually-picked
  "Basic colors" swatch instead — a facet can pick its own answer to that independent of the global one
  without needing its own wallpaper.
- **Overridden again: `themeMode`/`iconRenderMode` join the facet-overridable set too** (direct
  instruction — "move ALL settings to facet", superseding the "leave these two global-only" call made
  moments earlier when the user had no preference between them). Every remaining field in
  `LauncherSettings.kt:167-168`'s "global only" tier is now in scope for Part B — nothing about
  Appearance stays global-exclusive. Both are plain flat dropdowns (`ThemeMode`: `LIGHT`/`DARK`/
  `SYSTEM`; `IconRenderMode`: `SYSTEM_DEFAULT`/`MONOCHROME_BLACK_WHITE`/`MONOCHROME_ACCENT`), so they
  get the same treatment as `launcherFontOption`/clock fields — a `LAUNCHER_DEFAULT` member folded
  directly into each enum's own option list, no paired widget (neither name collides with an existing
  member, so no naming conflict to work around).
- **Accent color's facet UI matches the slider treatment, not the plain-dropdown one** (direct
  correction): a swatch picker is a rich custom widget, same category as a slider, not a flat option
  list — folding `LAUNCHER_DEFAULT` into it directly would be as awkward as folding it into a slider's
  steps. So: `accentFromSystem: Boolean` is promoted to a tri-state enum for facet purposes,
  `AccentSourceOption { LAUNCHER_DEFAULT, WALLPAPER, BASIC }` (`customAccentSwatch`/
  `wallpaperAccentRole` stay as satellite fields, read only when `BASIC`/`WALLPAPER` is selected, same
  relationship they already have today). The facet Appearance screen's Accent color row gets the same
  paired "Launcher default" / "Override" dropdown as the sliders — picking "Override" reveals the
  existing Wallpaper/Basic picker UI (unchanged) underneath, scoped to the facet; picking
  "Launcher default" hides it and writes the sentinel. Same data-model principle as everywhere else in
  this plan: still a non-null enum + sentinel underneath, the paired dropdown is just this specific
  field's UI presentation of it, chosen because the value-selection control itself is a picker, not a
  list.
- **Outer `InheritOverrideCard` gate — likely droppable for Appearance specifically**: Apps/Dock/
  Calendar stay all-or-nothing (no per-field granularity there), so they keep their outer inherit/
  override gate. Appearance, once every field carries its own `LAUNCHER_DEFAULT`-capable sentinel,
  gets that "fully inherits" state for free — a facet where every field is left at the sentinel value
  *is* inheriting, no separate boolean needed. Recommendation carried into the tasks below: no
  `overrideAppearance` gate, no outer card — the facet Appearance screen is just always reachable, each
  field independently resolving `LAUNCHER_DEFAULT` or its own value. Flagged as a recommendation, not
  fully settled — revisit if a summary "3 fields overridden" affordance turns out to need a real flag.

### Part A — Global level (do first)

- [x] Remove `calendarFontWeight`/`calendarFontOption`/`calendarColorOption`/`calendarAlignment` from
  `LauncherSettings`, their `SettingsRepository` keys/getters/setters, and the Calendars card's
  control rows in `ClockStyleGalleryScreen.kt`; wire `CalendarEventsBlock` to read
  `homeAppsFontWeight`/`launcherFontOption`/`appLabelColorOption`/`clockAlignment` instead. Keep the
  `CalendarEventsBlock` preview call itself.
- [x] Remove the matching `FacetEntity` override columns (same four) — `FacetDatabase.VERSION` bump +
  `Migration` step per CLAUDE.md's table (`MIGRATION_22_23`, v22→v23, create-copy-drop-rename since
  SQLite `DROP COLUMN` needs 3.35+; test added to `FacetDatabaseMigrationTest`). **This intentionally
  removes a facet's ability to have its own calendar style independent of the global default until
  Part B restores general Appearance overrides** — documented in `02-persistence-room.md`'s §0 and
  "Clock design" section intro so it doesn't read as an accidental regression later.
- [x] Give `AppearanceSettingsScreen`'s preview a clock. **Scoped down from the original literal
  plan** (extracting `FacetPreviewPage`'s whole wallpaper+clock+apps+dock rendering, including its
  card-aspect-ratio + density-scaling machinery, out of `FacetCarouselScreen` for reuse here) — that
  full extraction was judged too high-risk to the already-shipped, tested carousel for the value it
  added, given `AppearanceSettingsScreen`'s preview is a different shape entirely (a short band, not
  a full-screen-aspect card). Landed instead: `HomeSurfacePreview` (already shared by Appearance/
  Dock/Home-Apps-List) gained an optional `clockContent: (@Composable () -> Unit)? = null` slot,
  rendered above the app rows when non-null; `AppearanceSettingsScreen` passes a real `ClockBlock`
  fed from its own `settings: LauncherSettings` (global mode only — a facet's own clock look/preview
  stays on its separate "Clock style" screen, so facet mode passes `null`). `FacetCarouselScreen`/
  `FacetPreviewPage` themselves are untouched. New testTag `appearance_preview_clock`.
- [x] Move the "Clock style" row from `SettingsScreen.kt` into `AppearanceSettingsScreen.kt`; remove it
  from `SettingsScreen`; fold the now-single-item "Clock & Calendar" section into "Home & Apps"
  (`ClickableRow`/`NavigationChevron` promoted from `private` to package-visible so both screens can
  share them).
- [x] `BackupBundle`: drop the four removed calendar style fields (tolerant-reader discipline, no
  `CURRENT_BACKUP_VERSION` bump, following the `fontScaleOption` precedent) — wired through
  `BackupMapping`/`Export`/`ImportBackupUseCase`.
- [x] Docs: `README.md`, `01`, `02` (key/field/migration-step/writer-count tables, backup field
  count), `05` (F11), `06` (test count), `07` (writer counts), `13` (`updateOverridingClock` value
  count, calendar's new theming data source) all updated; `TEST_REGISTRY.md` regenerated
  (133 classes / 1070 cases).
- [x] Tests: removed/updated tests referencing the deleted fields, setters, and testTags across
  `SettingsRepositoryTest`, `FacetRepositoryTest`, `HomeUiStateTest`, `ClockStyleGalleryViewModelTest`,
  `ClockStyleGalleryScreenTest`, `ClockBlockTest`, `BackupRepositoryTest`, `ImportBackupUseCaseTest`,
  `SettingsScreenTest`; new coverage in `AppearanceSettingsScreenTest`
  (`clockStyleGalleryRowIsClickable`) and `ClockStyleGalleryViewModelTest` (Appearance fields resolve
  from global settings regardless of facet scope); `FacetDatabaseMigrationTest` gained
  `migration22To23DropsCalendarStyleColumnsWithoutLosingExistingRowsOrOtherColumns`. Full unit suite
  (620 cases) passes; `detekt` clean; androidTest sources compile (not run — no emulator booted this
  session, per CLAUDE.md's instrumented-test policy).

### Part B — Facet level (after Part A ships and settles)

- [ ] Add a `LAUNCHER_DEFAULT`-equivalent sentinel member to each enum that needs new per-field facet
  reach: `FontWeightOption` (new member, distinct from any existing entry), `FontScaleOption` (must be
  named `LAUNCHER_DEFAULT`, not `DEFAULT` — that name is taken by the existing `1.0f` stop),
  `LauncherFontOption` (was wrongly scoped out of an earlier pass — see correction above; needed so the
  *facet* copy can defer to the global font, same as `ClockFontOption` already does), `ThemeMode` and
  `IconRenderMode` (both plain flat dropdowns, `themeMode`/`iconRenderMode` now in scope per "move ALL
  settings to facet" — direct instruction), and `ClockColorOption` (backs `appLabelColorOption`,
  `clockColorOption`, and `clockAccentColorOption` — one enum change covers all three; see the
  color-vs-font discussion above for why this one stays `LAUNCHER_DEFAULT`-only, no `FACET_DEFAULT`).
  Each resolves the same way `ClockFontOption.LAUNCHER_DEFAULT` already does (`ClockFonts.kt:84`):
  defers to the corresponding global `LauncherSettings` value rather than carrying a value of its own.
- [ ] New tri-state enum `AccentSourceOption { LAUNCHER_DEFAULT, WALLPAPER, BASIC }` replacing
  `accentFromSystem: Boolean` for facet purposes (see accent color design decision above) —
  `customAccentSwatch`/`wallpaperAccentRole` stay as satellite fields, read only when `BASIC`/
  `WALLPAPER` is selected.
- [ ] New **non-null** `FacetEntity` columns: `homeAppsFontWeight: FontWeightOption =
  FontWeightOption.LAUNCHER_DEFAULT`, `launcherFontOption: LauncherFontOption =
  LauncherFontOption.LAUNCHER_DEFAULT`, `fontScaleOption: FontScaleOption =
  FontScaleOption.LAUNCHER_DEFAULT`, `appLabelColorOption: ClockColorOption =
  ClockColorOption.LAUNCHER_DEFAULT`, `themeMode: ThemeMode = ThemeMode.LAUNCHER_DEFAULT`,
  `iconRenderMode: IconRenderMode = IconRenderMode.LAUNCHER_DEFAULT`, `accentSource: AccentSourceOption
  = AccentSourceOption.LAUNCHER_DEFAULT`, `customAccentSwatch: String?`, `wallpaperAccentRole:
  WallpaperAccentRole = WallpaperAccentRole.PRIMARY` — `FacetDatabase.VERSION` bump + `Migration` step,
  each non-satellite column `NOT NULL DEFAULT` its sentinel so every existing facet row transparently
  inherits on migration, no true/false branching needed (unlike a boolean-gate design). Existing
  `clockColorOption`/`clockAccentColorOption` columns keep their current default (`ACCENT_PRIMARY`/
  `THEME` etc.), just gain `LAUNCHER_DEFAULT` as a newly-selectable option, same non-migration treatment
  as `clockFontOption`.
- [ ] `AppearanceSettingsScreen`/`AppearanceSettingsViewModel` go dual-mode, mirroring
  `ClockStyleGalleryScreen`/`ClockStyleGalleryViewModel`: new `FACET_APPEARANCE_SETTINGS/{facetId}`
  route alongside the existing global one; ViewModel branches on `facetId` from `SavedStateHandle`. Read
  path resolves `if (facetValue == X.LAUNCHER_DEFAULT) globalValue else facetValue` per field
  (same shape as `ClockFonts.kt`'s existing resolution); write path sets only that facet's column,
  never `SettingsRepository`.
- [ ] `LabeledDropdownRow`-backed fields handle this the same way `entries` already does for
  `ClockFontOption` — no new widget, just the sentinel added as one more option. Applies to
  `homeAppsFontWeight`/`launcherFontOption`/`fontScaleOption`/`appLabelColorOption`/`themeMode`/
  `iconRenderMode`: filter the `LAUNCHER_DEFAULT` entry out of the option list when the screen is in
  **global** mode (offering "use launcher default" there is circular — global mode *is* defining that
  value), but keep it in facet mode. Existing clock fields (`clockFontOption`, etc.) are unaffected —
  keep showing `LAUNCHER_DEFAULT` in both modes exactly as today.
- [ ] `FontSizeSlider`/`FontWeightSlider` **and** the Accent color row gain a facet-mode-only paired
  "Launcher default" / "Override" dropdown above the value control (see design decisions above) — the
  control itself (slider, or the existing Wallpaper/Basic swatch picker) stays disabled while "Launcher
  default" is selected and writes the sentinel; selecting "Override" seeds it at the current resolved
  value and enables it for editing. Not shown at all in the global `AppearanceSettingsScreen` (same
  reasoning as the dropdown fields above — nothing to inherit from there).
- [ ] Add `ClockFontOption.FACET_DEFAULT` (see design decision above) — resolves to this facet's own
  resolved `launcherFontOption`, chaining through the same `facetValue`-or-`globalValue` resolution
  Appearance itself uses, rather than jumping straight to the global font. Migration: existing
  `FacetEntity.clockFontOption`/`LauncherSettings.clockFontOption` rows keep their current values
  unchanged (nothing forces an existing explicit choice to `FACET_DEFAULT`); only the **default value**
  for newly-created facets' `clockFontOption` column changes, from `LAUNCHER_DEFAULT` to
  `FACET_DEFAULT`. Filter `FACET_DEFAULT` out of the option list in global mode (no facet to chain
  through there) — same technique as the other facet-only entries. `LAUNCHER_DEFAULT` remains available
  and unchanged at both scopes, as the explicit "pin to the global font regardless of this facet's own"
  choice. **Settled: no `ClockColorOption` equivalent** — see the color-vs-font discussion above.
- [ ] Move the facet-scoped "Clock style" row inside the facet Appearance screen (mirroring Part A's
  global nav move); remove `FacetSettingsScreen.kt`'s separate Clock+Calendar section and its
  `InheritOverrideCard`. Per the recommendation above, don't add a new outer Appearance
  `InheritOverrideCard` either — the facet Appearance row becomes a plain nav row (like Calendars),
  always reachable, each field resolving independently. "Calendars" keeps its own independent section,
  `overrideCalendar` untouched. Revisit only if product wants a single "N fields overridden" affordance
  that a per-field-only model can't cheaply express — that's the one case still worth a real flag.
- [ ] Extend the shared mini-home-preview composable (built in Part A) to resolve facet-scoped values
  (`facetValue`-or-`globalValue` per field, same resolution as above) when editing a facet, instead of
  always reading global `LauncherSettings`.
- [ ] `BackupBundle`/`CURRENT_BACKUP_VERSION` bump for all the new per-facet fields (`homeAppsFontWeight`,
  `launcherFontOption`, `fontScaleOption`, `appLabelColorOption`, `themeMode`, `iconRenderMode`,
  `accentSource`, `customAccentSwatch`, `wallpaperAccentRole`); `Export`/`ImportBackupUseCase` updates,
  tolerant-reader fallback to `LAUNCHER_DEFAULT` on unparseable values (mirroring
  `ClockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT)` in `BackupMapping.kt`);
  `clockFontOption`'s existing backup handling also needs its fallback re-checked once `FACET_DEFAULT`
  exists, so an unparseable facet-scoped value falls back to `FACET_DEFAULT` there, not
  `LAUNCHER_DEFAULT`.
- [ ] Docs: `01`, `02` (new columns, migration step, backup version bump), `07 §3`, `13` (facet
  Appearance override — sentinel-resolution model, not a boolean gate, now covering every Appearance
  field with no global-only exceptions; supersedes its existing Clock-override description; also
  document `ClockFontOption.FACET_DEFAULT` and its resolution chain, and `AccentSourceOption`).
- [ ] Tests: unit (persistence round-trip on the new columns; resolution helper's `LAUNCHER_DEFAULT`-vs-
  explicit-value branching per field, mirroring any existing `ClockFonts.kt` resolution test; option-list
  filtering — sentinel present in facet mode, absent in global mode, across every new field; the
  Accent color row's paired dropdown enabling/disabling the swatch picker; `FACET_DEFAULT`'s
  chain-through resolution — clock font follows a facet's overridden `launcherFontOption` when set,
  falls back to global when the facet doesn't override, and diverges correctly from `LAUNCHER_DEFAULT`
  in the overridden case); instrumented (facet-scoped Appearance screen edits don't leak into the
  global default and vice versa; a facet overriding only one field still tracks every other field live
  if the launcher default changes; the shared preview reflects the correct facet's resolved values).

### FacetSettingsScreen restructure — switches move to their destination screens (direct request)

Separate from Part A/B above: `FacetSettingsScreen` used to host a per-section `InheritOverrideCard`
(Apps list / Dock / Clock style) directly on itself, each rendered outside the actual controls it
gated. Restructured to look like `SettingsScreen`: a plain nav-row list (Rename row, then one
"HOME & APPS" card with Apps list/Dock/Clock style/Calendars rows), with the Inherit/Override switch
moved onto each destination screen instead — the same place `CalendarSettingsScreen` already put it,
which served as the reference pattern for this change.

- [x] `HomeAppsListSettingsScreen`/`HomeAppsListSettingsViewModel`: added `InheritOverrideCard`
  (`testTagPrefix = "home_apps_list"`) and `setOverriding(overriding: Boolean)`; `controlsEnabled =
  !isFacetScoped || isOverriding` threaded through all 5 dropdowns, the favorites row, and the reorder
  list.
- [x] `DockSettingsScreen`/`DockSettingsViewModel`: same pattern, `testTagPrefix = "dock_settings"`,
  `setOverriding` calling `updateOverridingDock` (+ `facetDockAppRepository.replaceItems` seed on
  first override).
- [x] `ClockStyleGalleryScreen`/`ClockStyleGalleryViewModel`: same pattern, `testTagPrefix =
  "clock_style"`, `setOverridingClock`; `ClockPositionResetRow` gained an `enabled` param so the reset
  row dims/disables along with everything else while inheriting.
- [x] `FacetSettingsScreen`/`FacetSettingsViewModel`: removed the 3 `InheritOverrideCard` blocks and
  `setOverridingClock`/`setOverridingApps`/`setOverridingDock` (moved to the destination ViewModels
  above); merged Apps list/Dock/Clock style/Calendars into one `SettingsCard` under a "HOME & APPS"
  `SectionHeader`.
- [x] Tests: removed `FacetSettingsScreenTest`'s 3 now-dead override-persistence tests (their testTags
  no longer exist on this screen — coverage moved to the destination screens' own tests); updated its
  `"APPS LIST"` text assertion to `"HOME & APPS"`. Added `facetScopedModeShowsInheritOverrideSwitch` to
  `HomeAppsListSettingsScreenTest`, `DockSettingsScreenTest`, and `ClockStyleGalleryScreenTest`
  (mirroring `CalendarSettingsScreenTest`'s existing reference test). Fixed several pre-existing
  facet-scoped tests that assumed controls were always live regardless of override state (a
  behavioral change introduced by this restructure, since these screens previously wrote to a facet's
  row unconditionally): `HomeAppsListSettingsScreenTest`'s
  `facetScopedScreenWritesThePositionToThatFacetsRowOnceOverriding` (renamed),
  `aFolderFavoritedOnAFacet_isCountedAndShownInPreviewAndReorderList`;
  `DockSettingsScreenTest`'s `facetScopedScreenWritesTheDisplayStyleToThatFacetsRowOnceOverriding`
  (renamed), `aFolderInAFacetsOwnDock_rendersAsAFolderTileInThePreview`;
  `ClockStyleGalleryScreenTest`'s `facetScopedClockAlignmentPersistsToTheFacetDirectly`,
  `facetScopedResetClockWidgetPositionClearsTheFacetsOwnHeightAndAlignment` — each now flips the
  relevant `*_override_row` on (and waits for the facet's `override*` flag to persist) before
  interacting with the now-gated control. `TEST_REGISTRY.md` regenerated (133 classes / 1070 cases).
  Full unit suite passes, `detekt` clean, `androidTest` sources compile.
- [x] Docs: `13-flow-facets-theme-notifications-onboarding.md` — relabeled the "FacetSettingsScreen —
  one Inherit/Override switch per card" diagram subgraph to "Destination screens — each owns its own
  Inherit/Override switch", and added a bullet describing the new nav-list shape of
  `FacetSettingsScreen` and where `setOverriding*` now lives.

### Move dock/app-list display style + position to Appearance (direct request)

Separate from Part A/B: dock's Icons/Text display style and the Home apps list's row position,
icon/text presentation, and vertical (top/bottom) anchor — all "look"/placement, not content —
moved out of `DockSettingsScreen`/`HomeAppsListSettingsScreen` into `AppearanceSettingsScreen`,
which gained a facet-scoped mode for exactly these four fields. Per direct request, the facet
override model here is the **per-field `LAUNCHER_DEFAULT` sentinel** Part B already sketches for
the rest of Appearance (`clockFontOption`'s existing precedent), not a new whole-block
Inherit/Override switch: picking `LAUNCHER_DEFAULT` from a dropdown is itself what reverts a facet
to inheriting — there's no separate toggle to flip first, unlike Clock/Calendar/Apps/Dock's own
content screens.

- [x] `DockDisplayMode`/`AppRowPosition`/`AppRowPresentation`/`AppListVerticalAlignment` each gained
  a `LAUNCHER_DEFAULT` member (first entry, mirroring `ClockFontOption`'s ordering) — facet-only,
  filtered out of the dropdown's option list at global scope (`entries - X.LAUNCHER_DEFAULT`);
  `FacetEntity`'s four matching columns default to it for new facets. New string resources
  (`*_launcher_default`, "Default launcher position"/"Default launcher style").
- [x] `FacetEntity.resolveSentinel(facetValue, sentinel, globalValue)` — the per-field counterpart to
  the existing `resolveOverride` (boolean-gated) helper: `facetValue == sentinel ? globalValue :
  facetValue`, independent of any override flag. Used everywhere these four fields are read:
  `HomeUiState.activeAppRowPosition/activeAppRowPresentation/activeAppListVerticalAlignment/
  activeDockDisplayMode`, `FacetCarouselViewModel.appRowPosition/appRowPresentation/dockDisplayMode`,
  `DockSettingsUiState.dockDisplayMode`, `HomeAppsListUiState.appRowPosition/appRowPresentation/
  appListVerticalAlignment` (both still read the resolved value for their own preview cards, even
  though they no longer expose editable controls for it), `FacetSettingsUiState.dockDisplayMode/
  appRowPosition/appRowPresentation`, and the new `AppearanceSettingsUiState`.
- [x] `FacetRepository.updateOverridingApps`/`updateOverridingDock` trimmed — no longer take
  position/presentation/verticalAlignment/displayMode params, since those are no longer seeded
  alongside the whole-block override flag; the flags now gate only their screens' remaining content
  (list content mode/count/favorites; the dock's own app list). The individual per-field setters
  (`setDockDisplayMode`/`setAppRowPosition`/`setAppRowPresentation`/`setAppListVerticalAlignment`)
  already existed and are unchanged — `AppearanceSettingsViewModel` calls them now instead of
  `DockSettingsViewModel`/`HomeAppsListSettingsViewModel`.
- [x] `MIGRATION_23_24` (`FacetDatabase.VERSION` 23→24) — no column/type change (the columns are
  already `TEXT NOT NULL`; `LAUNCHER_DEFAULT` is just a new valid string), but a real data migration
  is still needed: a facet that wasn't overriding apps/dock had stale, previously-unread values
  sitting in these four columns — left alone, they'd silently start "overriding" once resolution
  stopped gating on `overrideApps`/`overrideDock`. `UPDATE facets SET ... = 'LAUNCHER_DEFAULT' WHERE
  overrideApps = 0` (and the same for `dockDisplayMode`/`overrideDock`) resets exactly those rows;
  a facet that *was* overriding keeps its real chosen value untouched.
  `migration23To24ResetsLookFieldsToLauncherDefaultOnlyForFacetsNotOverridingThatSection` in
  `FacetDatabaseMigrationTest` covers both cases with two seeded rows.
- [x] `AppearanceSettingsViewModel`/`AppearanceSettingsScreen` go dual-mode — `facetId?` via
  `SavedStateHandle` (`FacetNavHost`'s `APPEARANCE_SETTINGS` route widened to
  `"appearanceSettings?facetId={facetId}"`, matching `DOCK_SETTINGS`'s existing query-param-style
  shape, with a new `FacetDestinations.appearanceSettings(facetId)` helper). New
  `AppearanceSettingsUiState` (`facet: FacetEntity?` + `settings: LauncherSettings` +
  `isFacetScoped`/resolved `dockDisplayMode`/`appRowPosition`/`appRowPresentation`/
  `appListVerticalAlignment` properties) alongside the screen's existing global-only `settings`
  StateFlow. Facet mode shows only the four moved-in fields (each dropdown offering
  `LAUNCHER_DEFAULT`); every other field (Clock style row, theme, accent, icons, launcher font, app
  label color, font size/weight) is global-only and simply absent in facet mode, same reasoning
  Calendar/Clock already use for their own global-only fields.
- [x] `FacetSettingsScreen`/`FacetSettingsViewModel` gained a new "Appearance" row
  (`facet_appearance_row`) in the "HOME & APPS" card, between Dock and Clock style, subtitled with
  the effective dock display style + app row presentation (`DockDisplayMode.displayLabel()`/new
  `AppRowPresentation.displayLabel()` in `FacetSettingsComponents.kt`); navigates to the facet-scoped
  Appearance screen. `FacetSettingsViewModel`'s own `dockDisplayMode`/`appRowPosition`/
  `appRowPresentation` properties switched from `isOverridingDock`/`isOverridingApps`-gated
  resolution to `resolveSentinel`.
- [x] `DockSettingsScreen`/`HomeAppsListSettingsScreen`: removed the now-moved dropdown rows
  (`dock_display_style_row`; `default_app_row_position_row`/`default_app_row_presentation_row`/
  `app_list_vertical_alignment_row`) and their ViewModel setters/params — both screens still read
  the resolved value for their own preview card, just don't expose a control for it any more.
  `HomeScreen.kt`'s 4 exhaustive `when`/`if` branches over `DockDisplayMode`/`AppRowPosition`
  (`AppRow`/`FolderRow`/`DockIcon`/`FolderDockIcon`) updated for the new `LAUNCHER_DEFAULT` member —
  the two `DockDisplayMode` ones collapsed from `when` to a plain `if (displayMode == TEXT)` (only
  two real renderings), the two `AppRowPosition` ones added an explicit `LAUNCHER_DEFAULT -> ` branch
  alongside `LEFT` (defensive only — a resolved value passed down here is never actually the
  sentinel).
- [x] `BackupMapping.kt`'s `BackupFacet.toFacetEntity()` tolerant-reader fallback for
  `appRowPosition`/`appRowPresentation`/`dockDisplayMode` changed from a concrete value (`LEFT`/
  `ICON_AND_TEXT`/`ICONS`) to `LAUNCHER_DEFAULT`, matching `clockFontOption`'s existing facet-scope
  fallback precedent — an unparseable facet-scoped value now falls back to "inherit" rather than a
  guessed concrete look. Global `LauncherSettings`-level fallbacks (`ExportBackupUseCase`/
  `ImportBackupUseCase`) are untouched (`LAUNCHER_DEFAULT` is meaningless at global scope).
  `appListVerticalAlignment` was already missing from `BackupFacet` entirely before this change (a
  pre-existing gap, out of scope here) — restoring a facet backup already reset it to the entity
  default, which is now `LAUNCHER_DEFAULT` instead of `BOTTOM` (a behavior-neutral-to-slightly-better
  change, not a regression).
- [x] Tests: `FacetRepositoryTest` updated for the trimmed `updateOverridingApps` signature;
  `DockSettingsViewModelTest`/`HomeAppsListSettingsViewModelTest` updated (setter tests removed
  where the setter moved, added resolution tests proving the field no longer depends on
  `overrideDock`/`overrideApps`); `AppearanceSettingsViewModelTest` gained `SavedStateHandle`/
  `FacetRepository` + facet-mode tests (global/facet-scoped writes, sentinel resolution) — its
  `createViewModel` helper gained a `settings: LauncherSettings` param after a first draft's
  pre-call `when(settingsRepository.settings)` stub got silently overwritten by the helper's own
  internal default stub (Mockito's last-stub-wins). `AppearanceSettingsScreenTest` gained a
  `facetId`-scoped `setContent` variant and coverage for all four moved rows (global writes,
  `LAUNCHER_DEFAULT` filtered at global scope, facet mode hiding Clock-style/global-only fields,
  facet-scoped writes, the preview's new clock). `DockSettingsScreenTest`/
  `HomeAppsListSettingsScreenTest` lost their now-dead display-style/position tests (coverage moved
  to `AppearanceSettingsScreenTest`) and had their `facetScopedModeShowsInheritOverrideSwitch` tests
  re-gated on a control that's still actually on those screens (`add_dock_app_row`/
  `default_list_content_row`) instead of the removed one. `FacetSettingsScreenTest` gained
  `appearanceRowNavigatesWithTheFacetsIdAndReflectsTheEffectiveLookFields`.
  `TEST_REGISTRY.md` regenerated (133 classes / 1079 cases: 625 unit / 454 instrumented). Full unit
  suite (625 cases) passes; `detekt` clean (two new baseline entries —
  `TooManyFunctions:AppearanceSettingsViewModel` and `LongMethod:DockSettingsScreen.kt:
  DockSettingsContent` — matching the existing convention for other ViewModels/screen-content
  functions in this file, e.g. `ClockStyleGalleryViewModel`/`CalendarSettingsContent`); androidTest
  compiles and the touched instrumented classes (`DockSettingsScreenTest`,
  `HomeAppsListSettingsScreenTest`, `AppearanceSettingsScreenTest`, `FacetSettingsScreenTest`,
  `FacetDatabaseMigrationTest`) verified passing on the emulator.
- [x] Docs: `02-persistence-room.md` (ER diagram gate-column annotations updated to "content only
  now"; new paragraph on the sentinel resolution shape as a third pattern distinct from
  boolean-gated and always-global fields; migration-step table row for 23→24), `07-registries.md`
  (`AppearanceSettingsViewModel`'s nav-arg column: `—` → `facetId?`), `13-flow-...md` (new `LOOK`
  diagram subgraph for `AppearanceSettingsScreen`'s sentinel writes, distinct from the `OVERRIDE`
  subgraph's boolean-gated ones; `FacetSettingsScreen` bullet mentions the new Appearance row),
  `README.md`/`06-testing.md` (test counts).

### Appearance screen redesign — separate cards, renamed rows, calendar preview relocated (direct request)

Follow-up to the section above, same session: `AppearanceSettingsScreen` split into three visually
separate cards, its look-field rows renamed, and its calendar preview moved in from
`ClockStyleGalleryScreen` with a preview scaled like the facet carousel's own cards.

- [x] Card separation: "DOCK & HOME" (the four `LAUNCHER_DEFAULT`-sentinel look fields, always
  shown), then in global mode only "CLOCK" (just the Clock style nav row, on its own now — no
  longer bundled with theme/accent/etc) and "GENERAL" (theme, accent, icons, launcher font, app
  label color, font size/weight sliders). New `AppearanceSectionHeader` (mirrors
  `FacetSettingsComponents.kt`'s `SectionHeader` — a third near-identical private copy, matching
  this codebase's existing precedent of one per file rather than a shared cross-package import)
  and three new string resources (`appearance_section_dock_home`/`_clock`/`_general`).
- [x] Row renames (in place, same string keys — confirmed unreferenced anywhere else in Kotlin
  source before changing): `dock_display_style` "Display style" → "Show Dock apps as",
  `home_apps_list_position` "Position" → "Home Apps Alignment", `home_apps_list_presentation`
  "Presentation" → "Show Home apps as", `home_apps_list_list_position` "List position" → "Home
  Apps list position".
- [x] Calendar preview moved from `ClockStyleGalleryScreen` (which dropped its own
  `CalendarEventsBlock` preview block, section label, divider, and the two now-dead
  `homeAppsFontWeight`/`appLabelColorOption` params/UiState fields/tests that only fed it) to
  `AppearanceSettingsScreen`'s own preview card — `ClockBlock` already renders
  `CalendarEventsBlock` unconditionally beneath the clock, so this needed no new calendar-reading
  code, just fixed sample events (`PreviewCalendarEvents`, same "Team standup"/"Design review"
  sample `ClockStyleGalleryScreen` used to show, now living in `AppearanceSettingsScreen.kt`) fed
  through a fixed `PREVIEW_CLOCK` reference instant (matching that screen's own `fixedClock`).
- [x] New `AppearancePreviewCard` (private, `AppearanceSettingsScreen.kt`) replaces the plain
  `HomeSurfacePreview` call for this screen only (`HomeSurfacePreview` itself, and its Dock/
  Home-Apps-List callers, are untouched — the `clockContent` slot added earlier this session was
  reverted since nothing uses it any more) — scaled to `APPEARANCE_PREVIEW_CARD_SCALE = 0.55f` of
  the real screen (matching `FacetCarouselScreen`'s own `CAROUSEL_CARD_SCALE`) and density-scaled
  to match, via the same `BoxWithConstraints` + scaled-`LocalDensity` technique
  `FacetPreviewPage` uses for its carousel cards — deliberately reimplemented rather than shared,
  so a change to one can't regress the other (that card stays read-only/non-interactive here, with
  no click-to-apply or header/footer icon row, unlike the carousel's). **Caught and fixed during
  verification**: a first draft locked the card to `fillMaxWidth()` at the real screen's aspect
  ratio directly (no scale-down), which made it nearly full-screen-tall on a real device and
  pushed every card below it off the initial viewport — instrumented tests (and a manual
  screenshot check on the emulator) caught this before it shipped.
- [x] Tests: `ClockStyleGalleryScreenTest` lost `calendarPreviewSectionIsSeparatedFromClockSectionByADivider`/
  `calendarPreviewMovesLiveWhenClockAlignmentChanges` (coverage moved) and its
  `facetScopedModeShowsInheritOverrideSwitch` gained the same "wait for facet-scoped state to
  resolve asynchronously" guard other screens' equivalent tests already needed (a latent gap that
  started flaking once this file's item count dropped, unrelated to the redesign itself, caught in
  the same round of on-emulator verification). `ClockStyleGalleryViewModelTest` lost its two
  now-dead `homeAppsFontWeight`/`appLabelColorOption` resolution tests. `AppearanceSettingsScreenTest`
  gained `globalModeShowsThreeSeparateSectionsInOrder`, `facetScopedModeShowsOnlyTheDockAndHomeSection`,
  `dockAndHomeRowsShowTheirRenamedTitles`, `previewCardShowsTheCalendarPreviewInGlobalMode`; several
  existing tests needed scrolling into view that hadn't before (the "General" card is now much
  further down, past two more cards) — `previewCardStillRendersAfterChangingAppLabelColor`
  specifically needed to scroll back *up* to the preview card after editing a "General" row, since
  the preview is now far enough away to be disposed from the `LazyColumn`'s composed range by the
  time that edit lands. `TEST_REGISTRY.md` regenerated (133 classes / 1079 cases: 623 unit / 456
  instrumented — the unit/instrumented split shifted from the removed/added tests above, total
  unchanged). Full unit suite, `detekt`, and both compile targets pass; `AppearanceSettingsScreenTest`
  (25 cases), `ClockStyleGalleryScreenTest` (11 cases), and `FacetSettingsScreenTest` all verified
  passing on the emulator, plus a manual install + screenshot check of both the collapsed and
  scrolled states.
- [x] Docs: `13-flow-facets-theme-notifications-onboarding.md` — new bullet describing the
  three-card layout, renamed row titles, and the calendar preview's move, plus the matching
  `CAROUSEL_CARD_SCALE`/`APPEARANCE_PREVIEW_CARD_SCALE` scaling note; `06-testing.md` (test counts).

### Appearance preview: live favorites/dock/calendar instead of sampled/mock data (direct request)

Follow-up to the section above, same session: the redesigned preview card was still showing
*sampled* installed apps (`SelectPreviewAppsUseCase`) and fixed sample calendar events, not this
scope's actual configured content. "in launcher settings - consume defaults / in facet settings -
consume the resolved output between facet and defaults" (direct request) — the same
`overrideApps`/`overrideDock`/`overrideCalendar` facet-or-global resolution pattern
`HomeAppsListSettingsViewModel`/`DockSettingsViewModel`/`ObserveHomeScreenStateUseCase` already use.

- [x] `AppearanceSettingsViewModel` rewritten: `GetInstalledAppsUseCase`/`SelectPreviewAppsUseCase`/
  `DefaultAppRepository` dependency removed entirely; `uiState` now `combine()`s `settings`,
  `facetRepository.observeFacets()`, and two nested favorites/dock `Pair` combines
  (`FavoriteAppRepository`/`DefaultFavoriteAppRepository`, `FacetDockAppRepository`/`DockAppRepository`).
  New `effectiveFavorites`/`effectiveDockItems` getters on `AppearanceSettingsUiState` resolve
  facet-or-global exactly like those other two ViewModels' own equivalents (`if (facet?.overrideApps
  == true) facetFavorites else globalFavorites`, same for dock). `calendarEvents` computed via a new
  private `observeCalendarEvents(facet, settings)` suspend fun, called from inside the `combine`
  transform lambda (itself `suspend`) — mirrors `ObserveHomeScreenStateUseCase`'s own
  permission-gated, facet-or-global `showAllDayEvents`/`selectedCalendarIds` resolution, empty (never
  a sample fallback) when `CalendarPermissionRepository.isGranted()` is false.
- [x] `CalendarRepository.getTodayEvents` marked `open` (mirrors the existing `getCalendars`
  precedent) so an instrumented test can override it with a fixed event list.
- [x] `AppearancePreviewCard` (`AppearanceSettingsScreen.kt`) takes `favorites: List<PlacedItem>`/
  `dockItems: List<PlacedItem>`/`calendarEvents: List<CalendarEvent>` instead of the removed
  `previewApps: List<AppInfo>` — favorites render via the same `PlacedItem.SingleApp`/`FolderItem`
  branch `HomeSurfacePreview` uses (so a favorited folder renders as a real `FolderRow`, not
  flattened), dock via `DockIcon` (which already branches on `PlacedItem` internally). The clock now
  uses its own real system-default `Clock` instead of a fixed reference instant, since real calendar
  events need to render sensibly against real "now". Fixed `PREVIEW_CLOCK`/`PreviewCalendarEvents`
  sample data deleted.
- [x] **Real bug found via a from-scratch instrumented-test rewrite, not observed live**: an
  instrumented test seeded favorites/dock rows with a synthetic `AppInfo(packageName =
  "com.example.appearance.N", ...)` — `DefaultFavoriteAppRepository.observeDefaultItems()`/
  `DockAppRepository.observeDockItems()` hydrate every stored row against
  `AppRepository.observeInstalledApps()`'s live installed-app list (the same uninstall-collapse
  pattern `DockAppRepository` already documents), so a package that was never actually installed is
  silently `mapNotNull`-filtered out of what the preview ever renders — the test could never pass no
  matter how long it polled. Fixed by seeding with the device's own real installed apps
  (`appRepository.getInstalledApps()[n]`), matching the precedent already established in
  `FacetCarouselScreenTest`'s `seed`/`seedApps` split (`setContent` here gained the same split, one
  lambda for `FacetRepository`-only setup, one for the real app-repository-dependent seeding).
- [x] **Second bug, same rewrite**: a calendar-preview test's fixture used
  `endTimeMillis = 1` (1ms after the Unix epoch) — `CalendarEventsBlock` itself filters out any
  non-all-day event whose `endTimeMillis` has already passed relative to real "now", so the seeded
  event was silently dropped every time; fixed by timing the fixture relative to
  `System.currentTimeMillis()` instead of a fixed small constant.
- [x] **Third bug, same rewrite**: a facet-overriding test fetched a `FacetEntity` once, then called
  `facetRepository.setOverrideApps(facet, true)` followed by `updateOverridingDock(facet, true)`
  reusing that same now-stale snapshot — since `FacetDao.update` replaces the whole row, the second
  call's `.copy(overrideDock = true)` clobbered the first call's `overrideApps = true` back to
  `false`. Fixed by re-fetching the facet between the two writes.
- [x] **Fourth bug, unit tests**: `AppearanceSettingsViewModelTest`'s own `createViewModel()` helper
  unconditionally re-stubbed `defaultFavoriteAppRepository.observeDefaultItems()`/
  `dockAppRepository.observeDockItems()`/etc. with an empty-list default *after* a caller had already
  stubbed the same mock with real data — Mockito's last-stub-wins silently discarded the caller's
  stub (the same trap this test class's `calendarGranted` param was already built to avoid, just not
  yet applied to the favorites/dock repositories). Fixed by giving `createViewModel()` plain
  `facetFavorites`/`globalFavorites`/`facetDockItems`/`globalDockItems` value params, stubbed only
  once inside the helper — mirrors `DockSettingsViewModelTest.createViewModel`'s own `dockItems:
  List<PlacedItem>` param, rather than letting a test pre-stub a mock the helper also touches.
- [x] Tests: `AppearanceSettingsViewModelTest` gained 5 cases covering global/facet-not-overriding/
  facet-overriding favorites+dock resolution and calendar-granted/ungranted. `AppearanceSettingsScreenTest`
  gained `previewCardShowsRealCalendarEventsWhenPermissionIsGranted`,
  `previewCardShowsNoCalendarEventsWithoutPermissionRatherThanASampleFallback`,
  `facetScopedPreviewShowsTheDefaultFavoritesAndDockWhileNotOverriding`,
  `facetScopedPreviewShowsThatFacetsOwnFavoritesAndDockWhileOverriding`; existing
  `previewCardShowsTheRealFavoritesAndDockApps`/`previewCardStillRendersAfterChangingAppLabelColor`/
  `previewCardRendersTheWallpaperBehindItsContent` rewritten to seed real installed apps instead of
  sample `AppInfo`s and to poll (`waitUntil`) for the seeded content to actually render — the
  favorites/dock/calendar repositories are real Room + `LauncherApps` queries that settle
  asynchronously after the first composition, same lesson as `FacetCarouselScreenTest`'s own
  `previewCardShowsBothFavoritesAndDockAppsTogether`. `TEST_REGISTRY.md` regenerated (133 classes /
  1087 cases). Full unit suite, `detekt`, and both compile targets pass; `AppearanceSettingsScreenTest`
  (28 cases) and `ClockStyleGalleryScreenTest` verified passing on the emulator, plus a manual
  install + screenshot check confirming the preview shows a real configured favorite ("Calendar")
  and the real dock apps (Chrome/Messages/Camera), not sample data.
- [x] Docs: `13-flow-facets-theme-notifications-onboarding.md` — updated to describe the preview's
  real data sourcing (`FavoriteAppRepository`/`DefaultFavoriteAppRepository`/`DockAppRepository`/
  `FacetDockAppRepository`/`CalendarRepository`/`CalendarPermissionRepository`) instead of sampled
  installed apps.
- [x] **Real bug found via direct user report, not caught by the tests above**: the preview card's
  own `PREVIEW_HOME_APP_COUNT = 2`/`PREVIEW_DOCK_APP_COUNT = 3` were arbitrary "glanceable strip"
  caps left over from the old sample-data era — with live data wired in, they silently truncated the
  preview to 2 favorites/3 dock apps regardless of how many were actually configured (4 favorites, 5
  dock apps configured → only 2/3 shown), which read as a propagation bug but was purely a display
  cap. Fixed by pointing both constants at the app's real, single-source-of-truth limits instead of a
  duplicated magic number — `AppListLimits.MAX_FAVORITES` (6) and `DockAppRepository.MAX_APPS` (5) —
  making `.take()` a no-op safety net rather than a lossy truncation, and re-verified manually on the
  emulator across both override states (facet not overriding shows the global default's full list;
  facet overriding shows that facet's own full list).
- [x] **Spacing fix, same direct report**: `AppearanceSectionHeader` (new this session, see above) was
  missing the `top = 18.dp` padding `SettingsScreen`'s own `SectionHeader` uses (`padding(top =
  18.dp, bottom = 6.dp)`) — since `SettingsCard` itself carries no bottom margin, "CLOCK"/"GENERAL"
  sat flush against the card above with no breathing room, unlike every section header on the main
  Settings screen. Fixed to match exactly.

---

## ✅ Manage Facets row tap + Facet Settings "Apply facet" header button — complete (direct request)

Two small, related UX tweaks to how a facet is applied from Settings, alongside the existing
overflow-menu "Apply facet" entry in `ManageFacetsScreen` (unchanged).

- [x] **`ManageFacetsScreen`**: `FacetReorderRow`'s row itself is now clickable (`onEditFacetClick`)
  — previously only the drag handle and the "..." overflow menu were interactive, so there was no
  direct way to reach a facet's own settings besides the menu. Mirrors `FoldersSettingsScreen`'s
  row-is-clickable convention. The drag handle's own `pointerInput` only intercepts actual
  movement, so a plain tap on the handle itself still falls through to open settings too — a
  harmless second way in, not a conflict with dragging.
- [x] **`FacetSettingsScreen`**: new **"Apply facet"** button in the header (`FacetSettingsHeader`),
  same visual pattern as `FoldersSettingsScreen`'s "+ Create folder" — a `Row` with an icon
  (`Icons.Default.Check`, matching `FacetCarouselScreen`'s existing active-facet checkmark) + label,
  right-aligned via the title's `Modifier.weight(1f)`. Disabled (not hidden, so the header doesn't
  reflow) when this facet is already active — mirrors `ManageFacetsScreen`'s own
  `enabled = !isActive` on its overflow menu's equivalent entry. `FacetSettingsViewModel` gained
  `activeFacetId`/`isActive` on its `UiState` and an `applyFacet()` writer
  (`settingsRepository.setActiveFacetId(facetId)`, same one-liner as
  `ManageFacetsViewModel.applyFacet`). `FacetNavHost` pops back to `HOME` on apply, same as
  `ManageFacetsScreen`'s `onFacetApply`.
- [x] Tests: `ManageFacetsScreenTest.tappingTheRowNavigatesToEditFacet`;
  `FacetSettingsScreenTest.applyFacetButtonActivatesThisFacetAndInvokesOnFacetApply` /
  `applyFacetButtonIsDisabledWhenThisFacetIsAlreadyActive`. Full unit suite and
  `compileDebugAndroidTestKotlin` pass.

---

## ✅ Facet Deep Links, Dynamic Shortcuts & External Trigger Ingestion — complete

Lets an outside caller (another app, an automation tool like Samsung Modes & Routines/Tasker, or a
future in-app scheduler) switch the active facet without opening the launcher's own UI first. Two
independent entry points feed one shared ingestion path, and the existing reactive settings
pipeline (`SettingsRepository.setActiveFacetId` → `LauncherSettings.activeFacetId` StateFlow →
`ObserveHomeScreenStateUseCase`/`HomeViewModel` → Compose) already carries a facet switch through to
Home with no new plumbing needed on that side — see `13-flow-facets-theme-notifications-onboarding.md`'s
existing Facets flow diagram, which this work only adds a new entry arrow into (`S1`).

**Decisions:**
- **Identifier = the facet's Room `id` (Long), not its display name.** Names can be duplicated or
  renamed after a shortcut/automation is already wired up; the id is stable for the facet's
  lifetime and is what `FacetRepository`/`SettingsRepository` already key on everywhere else.
- **Deep link shape**: `facetlauncher://facet/{id}` (custom scheme — no App Links verification
  needed, this isn't an `https` link). `LauncherActivity` gets a second `<intent-filter>` (`ACTION_VIEW`,
  `CATEGORY_DEFAULT`, `data android:scheme="facetlauncher" android:host="facet"`) alongside its
  existing `HOME` one — a manifest intent-filter is what lets an arbitrary external `Intent` (Tasker,
  another app's explicit `ACTION_VIEW`, etc.) reach the launcher without going through our own
  shortcuts at all.
- **One ingestion path serves both triggers.** A dynamic shortcut's own `Intent` also targets
  `LauncherActivity` with the same `facetlauncher://facet/{id}` `Uri` as its `data` — so a shortcut
  invoked by an automation picker and a deep link sent by some other app hit the exact same code
  path in `LauncherActivity`/`LauncherViewModel`, not two parallel handlers.
- **No-op on an unknown/deleted facet id** (explicit requirement) — the ingestion use case looks the
  id up via `FacetRepository.getById` first; a miss (stale shortcut for a facet deleted since, a
  hand-typed bad id, a race with a delete) does nothing rather than crashing or silently creating a
  facet.
- **Shortcuts stay in lockstep with `FacetRepository` state**, not hand-triggered — a live collector
  (mirroring `CleanUpUninstalledAppsUseCase`'s "runs for the app's whole lifetime" shape) re-publishes
  the full dynamic shortcut set on every `observeFacets()` emission, so add/rename/reorder/delete all
  propagate without a manual "resync" step anywhere. `FacetRepository.MAX_FACETS` (3) is well under
  `ShortcutManagerCompat`'s per-activity cap, so no capacity/eviction logic is needed.
- **`ShortcutManagerCompat`/`ShortcutInfoCompat`** (already available transitively via the existing
  `androidx.core:core-ktx` dependency — no new dependency) over the raw platform `ShortcutManager`,
  for its API-level shims. `setDynamicShortcuts(...)` (a full atomic replace) each sync, rather than
  hand-rolled add/update/remove diffing — simpler and can't drift.
- **Scheduled timers (point 4 of the request) are explicitly out of scope for this phase** — "to be
  built later." Nothing here should block it: a future scheduler is just another caller of the same
  `ActivateFacetByIdUseCase` this phase builds (e.g. a `WorkManager` job invoking it directly in-process,
  no need to round-trip through an `Intent` at all).
- **Display name and trigger key are deliberately two different fields, never conflated.** What an
  automation picker (Samsung Modes & Routines, etc.) *shows* the user is `ShortcutInfoCompat`'s short/
  long label — set to `"Switch to <facet's live name>"` (direct request: reads as an action, not just
  a bare name, since that's how it's listed alongside other apps' shortcuts in a picker). What
  actually *fires* the switch is the facet's numeric Room `id`, carried in two places that are never
  the display name: the shortcut's own `ShortcutInfoCompat` id string (`"facet_<id>"` — internal
  bookkeeping, never rendered by the OS picker) and the `Uri` on its launch `Intent`
  (`facetlauncher://facet/<id>`), which is what `ActivateFacetByIdUseCase` actually parses and looks
  up. Renaming a facet updates the visible label on the next `observeFacets()` emission (same
  shortcut id, same trigger `Uri` — a rename is not a delete+recreate), so an already-configured
  automation rule keeps working and just shows `"Switch to <new name>"`.

- [x] **Domain**: `ActivateFacetByIdUseCase` (`domain/ActivateFacetByIdUseCase.kt`) — spans
  `FacetRepository` (validate the id exists) and `SettingsRepository` (`setActiveFacetId`). No-op
  when `facetRepository.getById(id) == null`.
- [x] **Data**: new `FacetShortcutRepository` (`data/FacetShortcutRepository.kt`) wrapping
  `ShortcutManagerCompat` — `syncShortcuts(facets: List<FacetEntity>)` builds one `ShortcutInfoCompat`
  per facet: id `"facet_<id>"` (trigger key, opaque to the user), `setShortLabel(...)` **and**
  `setLongLabel(...)` both set to `"Switch to <facet.name>"` (the only user-visible piece — direct
  request, so the shortcut reads as an action in an automation picker rather than a bare name),
  intent = explicit `ACTION_VIEW` to `LauncherActivity` with `data = buildFacetDeepLinkUri(facet.id)`,
  `rank` = facet position — then `ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)`. The
  `facetlauncher://facet/{id}` `Uri` build/parse pair lives in `data/model/FacetDeepLink.kt` (shared
  by the repository and `LauncherViewModel`'s ingestion side below).
- [x] **Domain**: `SyncFacetShortcutsUseCase` (`domain/SyncFacetShortcutsUseCase.kt`) — spans
  `FacetRepository` and `FacetShortcutRepository`; `suspend operator fun invoke()` collects
  `facetRepository.observeFacets()` forever, calling `syncShortcuts(...)` on every emission (never
  returns — same "runs for the app's whole lifetime" shape as `CleanUpUninstalledAppsUseCase`).
- [x] **Wiring**: `LauncherViewModel.init` gained `viewModelScope.launch { syncFacetShortcuts() }`
  alongside its existing `cleanUpUninstalledApps`/`repairOrphanedProfileRows` launches.
- [x] **Manifest**: second `<intent-filter>` on `LauncherActivity` (`ACTION_VIEW`, `CATEGORY_DEFAULT`,
  `data android:scheme="facetlauncher" android:host="facet"`), alongside its existing `HOME` one.
- [x] **Ingestion**: `LauncherActivity.onCreate` (cold start) and `onNewIntent` (warm start, alongside
  its existing `HOME`-category handling) both call a new `handleFacetDeepLink(intent)` that checks
  `intent.data` and, if present, calls `viewModel.activateFacetFromDeepLink(uri)` — parses the id via
  `parseFacetIdFromDeepLink` and delegates to `ActivateFacetByIdUseCase`. Malformed/missing/wrong-scheme
  data parses to a no-op, same as an unknown id.
- [x] **Docs**: new `14-flow-deep-links-and-shortcuts.md` + a row in `docs/architecture/README.md`'s
  doc table; `13`'s existing Facets flow diagram gained the new external-trigger entry arrow into
  `S1`; `07`'s repository/use-case registries and `04`'s package-structure counts updated;
  `TEST_REGISTRY.md` regenerated (137 classes / 1104 cases).
- [x] **Tests — unit only, no new instrumented test** (deliberate: this codebase's `androidTest` suite
  never launches a real `Activity` via `ActivityScenario` anywhere — every instrumented test drives a
  composable/`ViewModel` directly through `createComposeRule()` — so an `ActivityScenario<LauncherActivity>`
  test for the manifest intent-filter/`onNewIntent` glue would be a new, unprecedented pattern for two
  lines of trivial glue code that's otherwise fully covered below; not worth the precedent). Unit:
  `ActivateFacetByIdUseCaseTest` (known id activates it; unknown/deleted id is a no-op, via a real
  `FacetRepository`+`SettingsRepository` pair, mirroring `EnsureActiveFacetUseCaseTest`'s own harness);
  `SyncFacetShortcutsUseCaseTest` (shortcut set reflects `FacetRepository` state and re-syncs on
  add/rename/delete, via a mocked `FacetShortcutRepository`); `FacetShortcutRepositoryTest`
  (Robolectric — asserts `shortLabel`/`longLabel` equal `"Switch to <facet.name>"` while the shortcut
  id and the intent's `Uri` both encode `facet.id`, never the name; renaming a facet changes the label on the
  next sync without changing the shortcut id or trigger `Uri`; a sync fully replaces the previous
  set); `FacetDeepLinkTest` (Robolectric — build/parse round-trip, rejects wrong scheme/host/non-numeric
  id); `LauncherViewModelTest` gained `runs syncFacetShortcuts once on init`,
  `activateFacetFromDeepLink delegates the parsed id to ActivateFacetByIdUseCase`, and
  `activateFacetFromDeepLink is a no-op for a uri that isn't a facet deep link`. Full unit suite,
  `detekt`, and `compileDebugKotlin` all pass.

---

## ✅ Home App List: Two-Column & Grid Layouts + Cap Raise to 12 — complete (direct request)

Home's app list (Favorites/Recents/Most Used) is a single vertical column today, capped at 6 items
(`AppListLimits.MAX_FAVORITES`/`MAX_APPS_TO_SHOW`). Adding two more layout options — **two columns**
and **grid** — and raising the cap to 12 for all three list-content modes, now that the app-list
overflow-scroll fix (above, this session) makes a longer list scroll correctly instead of fighting
the Drawer's swipe-open gesture. Four new per-facet "look" settings, each following the existing
sentinel-override pattern exactly (`AppRowPosition`/`AppRowPresentation`/`DockDisplayMode`/
`AppListVerticalAlignment` are the direct templates — see `FacetEntity.resolveSentinel`).

Decided: 2-column split is **interleaved** by original order (index 0,2,4… left column top to
bottom; 1,3,5… right column top to bottom), not first-half/second-half. Each layout mode gets its
own alignment/presentation controls in Settings → Appearance, swapped in based on which layout is
selected (mirrors how "Apps to show" is already hidden for Favorites mode). Grid column count is
user-choosable (4/5/6). Grid gets its own icon/text display setting shaped like `DockDisplayMode`
(icons-only or text-only, no combined option) — separate from the Dock's own setting.

New enums (`data/model/LauncherSettings.kt`): `AppListLayout` (SINGLE_COLUMN/TWO_COLUMN/GRID),
`AppListColumnAlignment` (BOTH_LEFT/BOTH_RIGHT/MIRRORED — two-column only), `AppListGridColumns`
(FOUR/FIVE/SIX, `Int`-backed like `DrawerGridSize`), `AppListGridDisplayMode` (ICONS/TEXT — grid
only), each with a `LAUNCHER_DEFAULT` sentinel.

- [x] **Data model**: 4 new fields on `LauncherSettings` (real defaults) and `FacetEntity`
  (`LAUNCHER_DEFAULT` defaults); `SettingsRepository` keys/read-mapping/setters; `FacetRepository`
  direct-`copy()` setters (no flag flip, same as the sibling sentinel fields); resolved `active*`/
  resolved-`val` properties via the existing `resolveSentinel` in `HomeUiState`,
  `FacetSettingsViewModel`, `FacetCarouselViewModel`, `AppearanceSettingsViewModel` (+ its 4 new
  setter functions), `HomeAppsListSettingsViewModel`.
- [x] **Migration**: `FacetDatabase.VERSION` 24→25, `MIGRATION_24_25` adding the 4 columns
  (`ALTER TABLE facets ADD COLUMN ... DEFAULT 'LAUNCHER_DEFAULT'`, no data-reset step needed —
  brand-new columns).
- [x] **Backup**: `BackupSettings`/`BackupFacet` gain the 4 fields (global real-default,
  per-facet `LAUNCHER_DEFAULT`-default tolerant-reader); `BackupMapping.kt` export/import for the
  per-facet side; `ExportBackupUseCase`/`ImportBackupUseCase` for the global side. No
  `CURRENT_BACKUP_VERSION` bump (purely additive, same precedent as `fontScaleOption`).
- [x] **Cap raise**: `AppListLimits.MAX_FAVORITES`/`MAX_APPS_TO_SHOW` 6→12. `AppearancePreviewCard`
  needs its own decoupled small preview-row cap (independent of `MAX_FAVORITES`) so its fixed-size
  scaled card doesn't visually overflow once the real cap grows.
- [x] **`HomeScreen.kt` rendering**: outer scroll `Column` (alignment/`heightIn`/
  `onGloballyPositioned`/`home_app_list_scroll_region` — what the app-list nested-scroll fix
  depends on) stays unconditional; only its `verticalScroll` presence (grid owns its own scroll,
  the other two don't) and inner content switch on `appListLayout`. New `HomeAppTwoColumnList`
  (interleaved split, reuses `AppRow`/`FolderRow` with `AppRowPosition.LEFT`/`RIGHT` per column —
  no new mirroring logic needed) and `HomeAppGrid`/`HomeAppGridTile`/`HomeFolderGridTile`
  (`LazyVerticalGrid(columns = GridCells.Fixed(n))`, tile look modeled on the App Drawer's
  `DrawerGridTile` but branching `AppListGridDisplayMode` like `DockIcon` branches
  `DockDisplayMode`, not `DrawerGridTile`'s own `showLabel: Boolean`). Verified: `LazyVerticalGrid`
  dispatches through the same nested-scroll primitive as `verticalScroll`, so
  `appListNestedScrollConnection` needs zero changes; bounds-reporting
  (`onAppListBoundsChange`) needs zero new code since it's on the outer, mode-agnostic `Column`.
  `useCompactAppSpacing` doesn't apply to Grid (its tiles don't take `verticalPadding`/`iconSize`).
- [x] **Settings UI** (`AppearanceSettingsScreen.kt`'s "DOCK & HOME" card): new always-visible
  "App list layout" `LabeledDropdownRow`, then conditional rows per mode — Single column keeps the
  existing position/presentation rows; Two columns shows a new "Column alignment" row (position
  row hidden) but keeps presentation; Grid shows new "Grid columns"/"Grid display" rows (both
  position and presentation hidden). `AppListVerticalAlignment` row stays visible always.
  `AppearancePreviewCard` (its own doc: "deliberately reimplemented, not shared") gets its own
  copy of the 3-way branch. `HomeSurfacePreview.kt` (shared by Home Apps List/Dock settings) gets
  the same branch + 4 new params; its `maxAppRows` param renamed to `maxAppItems` (rows stop being
  the right unit once grid/2-column exist).
- [x] **Tests**: `FacetEntityTest` gains direct `resolveSentinel` coverage (currently only tests
  `resolveOverride` — a real pre-existing gap); `FacetSettingsViewModelTest`/
  `SettingsRepositoryTest`/`FacetRepositoryTest`/backup tests get cases for the 4 new fields,
  mirroring `appRowPosition`'s existing ones; new `FacetDatabaseMigrationTest` case for
  `MIGRATION_24_25`; `HomeScreenTest` gains 3 cases (interleaved split lands the right items in
  the right column; each `AppListColumnAlignment` renders the expected per-column icon/label
  order; grid mode renders the chosen column count and respects `AppListGridDisplayMode`).
- [x] **Docs**: `02-persistence-room.md` (ER diagram, migration table, key/writer tables, the
  "third resolution shape" field list), `03` (Home state graph — 4 new `HomeUiState` properties),
  `13-flow-facets-theme-notifications-onboarding.md` (`LOOK` subgraph + per-field Settings-row-
  title bullets), `04` only if new composables land in a new file; `TEST_REGISTRY.md` regenerated.

Full design/plan detail: see chat history (this session) — a research-backed plan was reviewed and
approved before starting.

**Real bugs found on-device (post-merge, 2026-09-28):**
- `AppearancePreviewCard` and the Facet Carousel's own preview card capped favorites at a flat
  `PREVIEW_HOME_APP_COUNT = 6` regardless of layout — once the cap raise shipped, Grid/Two-column
  previews showed mostly empty card space despite 12 real favorites configured. Fixed with a
  layout-aware `previewItemCap` (row-budget × items-per-row), duplicated in both files per their
  own "deliberately reimplemented, not shared" precedent.
- **`FacetCarouselScreen`'s `FacetPreviewPage` never read a facet's own `appListLayout` at all** —
  it always rendered the old single-column `AppRow` list no matter what layout that facet actually
  used on real Home, and had no cap (would overflow with 12 items). This was a real gap in the
  original two-column/grid feature: `FacetCarouselViewModel`'s `appListLayout(facetId)`/etc.
  accessors already existed but were never wired into the carousel's own preview. Fixed by adding
  the same 3-way branch (`FacetPreviewAppList`/`FacetPreviewRow`/`FacetPreviewGridTile`).
- Both grid branches used `Arrangement.SpaceEvenly`, which stretched an incomplete last row's
  items across the full card width instead of packing them left — mismatched
  `HomeScreen`'s real `LazyVerticalGrid(GridCells.Fixed(n))` behavior. Fixed with fixed
  equal-weight slots per column (empty ones left blank).
- New coverage: `AppearanceSettingsScreenTest` (`previewCardRendersGridLayoutWithTheChosenColumnCount`,
  `previewCardGridDisplayModeSwitchesBetweenIconsAndText`) and `FacetCarouselScreenTest`
  (`previewCardRendersTheFacetsOwnGridLayoutInsteadOfAlwaysSingleColumn`,
  `previewCardGridDisplayModeShowsTextInsteadOfIconsWhenChosen`) — all four assert the left-packed
  incomplete-row bounds directly, so a regression back to `SpaceEvenly` or a dropped layout branch
  would fail them. `TEST_REGISTRY.md` regenerated.

## ✅ App Drawer & Private Space: "Recently Installed" Category — complete (direct request)

A single virtual item — not a real `Folder`/`FolderEntity` in Room, just a computed view over
`AppInfo.firstInstallTime` — positioned in every app-browsing list (main App Drawer's
Personal/Work tabs, and Private Space's own separate list) by a 3-way `RecentlyInstalledPosition`
setting: `SHOW_FIRST` (default, above everything including a `SHOW_FIRST`-pinned folder section),
`SHOW_LAST` (below everything including a `SHOW_LAST` folder section), or `DO_NOT_SHOW`. Tapping it
opens a bottom sheet of every app installed in the last 72 hours in that scope.

Decided: scoped to whichever app list it's shown in — the main Drawer's copy is scoped to the
active tab (`tabScopedApps`, unlike pinned folders, which are deliberately *not* tab-filtered);
Private Space gets its own fully independent copy over its own already-isolated app list. Zero
qualifying apps → hidden entirely, not an empty state. Fixed 72h window on `firstInstallTime` (not
`lastUpdateTime`), not user-configurable. **List-only** (no grid-tile counterpart — direct feedback
after the initial build).

- [x] **`RecentlyInstalledAppsUseCase`** (`domain/`, pure `@Inject constructor()`, no Room):
  filters `AppInfo.firstInstallTime` within the last 72h, sorted newest-first; `nowMillis` a
  defaulted param for direct unit-testability.
- [x] **Main Drawer**: computed inline via `remember(tabScopedApps, recentlyInstalledPosition)` in
  `AppDrawerScreen.kt`, mirroring how `pinnedFolders`/`groupedItems` are already computed there —
  no `DrawerViewModel` `StateFlow` involved. New `DrawerRecentlyInstalledRow` (List-only, no grid
  counterpart) — no long-press menu (tap-only). `DrawerListContent` renders it as either the first
  or the very last `item`, gated on `recentlyInstalledPosition`. `letterIndexOffset` and
  `onRailSelectionChanged`'s `RailSelection.Folders`/`RailSelection.RecentlyInstalled` branches
  share a `recentlyInstalledOffset` (leading-only, 0 in Grid or when `SHOW_LAST`) and a local
  `afterLetteredContentIndex()` helper so the alphabet rail's letter-jump, "Folders" tap, and
  "Recently installed" tap all land on the right index regardless of where either category sits.
- [x] **Category glyph**: `RecentlyInstalledGlyph` — the same 12dp-rounded-tile-with-inset-vignette
  container shape as `FolderTileGlyph` with `RecencyIcon` (see below) standing in for a folder's
  own icon — not a star (direct feedback: a star read as generic/unrelated to "recently
  installed"). Background/tint went through two rounds of direct feedback: `FolderGlyphBackground`
  + literal white first (since that background is dark in both themes), then swapped to `Ink`
  background + `InkInverted` tint (direct feedback) — `Ink`/`InkInverted` are exact opposites of
  each other in both themes, so this stays themed and high-contrast without a literal color,
  deliberately diverging from `FolderTileGlyph`'s own fixed-dark tile.
- [x] **Section header**: icon-only `DrawerRecentlyInstalledHeader`/`PrivateSpaceRecentlyInstalledHeader`
  (`RecencyIcon`, no text label) render directly above the row for both `SHOW_FIRST`/`SHOW_LAST`
  placement, mirroring a letter group's own header minus the label (direct feedback, added after
  the rest of the feature shipped) — bumped `recentlyInstalledOffset` from 1 to 2 (header + row).
- [x] **Recency icon**: `RecencyIcon` (`FolderContentsSheet.kt`, `internal` for cross-file reuse) —
  a custom `ImageVector` built from a supplied Material Symbols "history" SVG path via
  `PathParser().parsePathString(...)`, shifted into Compose's `[0, viewportHeight]` space with a
  `group(translationY = 960f)` wrapper (the source's `viewBox="0 -960 960 960"` has no direct
  Compose equivalent) — used verbatim rather than substituted with `androidx.compose.material.icons`'
  own (older, visually different) `History` icon, per direct feedback.
- [x] **Sheet**: reused `FolderContentsSheet` via a synthetic `Folder(id = -1L, name = "Recently
  installed", apps = recentlyInstalledApps)` — `Folder`/`FolderTileGlyph`/`FolderContentsSheet`
  are already plain-data/Room-decoupled. `FolderSheetHeaderAction` gained a third case, `None`, so
  the sheet renders with no "Rename" button on this non-editable virtual folder. Each contained
  app also gets a small `RecencyBadge` (same `RecencyIcon`, literal white on `IconTile`) at its
  icon's bottom-end corner — a new `showRecencyBadge: Boolean` param on
  `FolderContentsSheet`/`FolderContentsList`/`FolderContentsAppRow`, `false` by default so real
  folders' contents are unaffected.
- [x] **Alphabet rail entry**: `AlphabetRail`/`railSelectionAt` gained a
  `recentlyInstalledPosition: RailFolderPosition` param (reusing the folder glyph's own
  `NONE`/`TOP`/`BOTTOM` type) and a third `RailSelection` case, `RecentlyInstalled` — its glyph
  sits outermost at whichever end it's on (ahead of a `TOP` folder glyph, or after a `BOTTOM` one).
  The big letter-jump indicator (the pill shown while dragging) got a fixed `96.dp × 64.dp`
  footprint instead of padding-wraps-content, so a wide letter no longer resizes the pill versus a
  narrow one; its `Text` now reads `MaterialTheme.typography.displayMedium` (the app's own
  font-family/weight setting, like every other piece of text) instead of `FacetType.clock` — that
  style is the one deliberate exception to the app's type scale, fixed to
  `FontFamily.SansSerif`/`ExtraLight` regardless of the user's font choice, and read visibly
  thinner than the Folders/Recently-installed glyphs shown in the same indicator slot.
- [x] **Private Space's own copy**: `PrivateSpaceViewModel` gained `SettingsRepository` +
  `RecentlyInstalledAppsUseCase` dependencies; new `recentlyInstalledApps: StateFlow<List<AppInfo>>`
  via `combine(apps, query, settings)`, hidden while searching — mirrors the main Drawer's own
  search-hides-browse-content precedent. `PrivateSpaceScreen` gained a
  `recentlyInstalledPosition` param and renders the row as either its leading or trailing `item`
  (no alphabet rail there to place it on) styled through `PrivateSpaceTheme`'s own color tokens,
  not this app's usual `Accent`/`Ink`. Fully independent state from the main Drawer's copy — same
  use case class, same global setting, separate computation.
- [x] **Settings**: `recently_installed_position` DataStore key (`RecentlyInstalledPosition.name`,
  default `SHOW_FIRST`) — `SettingsRepository` keys/read-mapping/setter,
  `LauncherSettings.recentlyInstalledPosition`,
  `AppDrawerSettingsViewModel.setRecentlyInstalledPosition`, a `LabeledDropdownRow` in
  `AppDrawerSettingsScreen.kt` mirroring `DrawerFolderDisplayMode`'s own dropdown —
  `SHOW_FIRST`/`SHOW_LAST` get their own "recents"-worded strings (`recently_installed_position_
  show_first/last`, en+de+es+fr+pt) rather than reusing folders' "Show folders first/last" text
  verbatim, but `DO_NOT_SHOW` gets its own dedicated string too (not shared with
  `DrawerFolderDisplayMode`'s, despite identical English text) — threaded from `HomeDrawerRoute.kt`
  into both `AppDrawerScreen` and `PrivateSpaceScreen`.
- [x] **Tests**: `RecentlyInstalledAppsUseCaseTest` (window inclusion/exclusion at the 72h
  boundary, sorting, empty input, install-vs-update distinction); `AppDrawerScreenTest` gains
  cases (shows/hides on setting+qualifying-apps, sits above a `SHOW_FIRST` folder section, sits
  below the lettered content when `SHOW_LAST`, tapping opens the sheet with the right apps, and —
  the regression guard for this item's tab-scoping, deliberately unlike pinned folders — apps are
  scoped to the active tab); `AppDrawerSettingsScreenTest` gains a dropdown round-trip case;
  `AlphabetRailMappingTest` gains cases for `recentlyInstalledPosition` at `TOP`/`BOTTOM`,
  independently and combined with a folder glyph at either end; `PrivateSpaceViewModelTest` (4
  cases: 72h filter over Private Space's own apps, respects the setting, hidden while searching,
  independent of the main Drawer). `TEST_REGISTRY.md` regenerated.
- [x] **Docs**: `12-flow-drawer-search-and-app-actions.md` (§1b rewritten for the 3-way position,
  the recency icon/badge, the rail's `TOP`/`BOTTOM` entry, Private Space's independent copy);
  `11-flow-profiles-and-spaces.md` (Private Space app-list row cross-references §1b);
  `02-persistence-room.md` (ER diagram, writer table, key/field registry — now a `String` enum
  key, not `Boolean`); `07-registries.md` (use-case row); `04-package-structure.md` (file counts).

Full design/plan detail: see chat history (this session) — a research-backed plan was reviewed and
approved (twice revised on direct feedback before starting: tab-scoping vs. folders, and adding
Private Space's own copy), then further revised after the initial build shipped: Grid support
dropped, the category glyph changed from a star to the supplied "history" icon (then made literal
white), the big rail letter-indicator's font/pill fixed, and the on/off toggle expanded into a
3-way `SHOW_FIRST`/`SHOW_LAST`/`DO_NOT_SHOW` position setting with its own dedicated strings
(sharing neither `DrawerFolderDisplayMode`'s "Show folders first/last" text nor
`SearchBarPosition`'s generic "Top"/"Bottom" — direct feedback on each).

## ✅ Home press reliability, one action sheet, system bar icons, gesture-hint refresh & clock Alignment pill — built; on-device evaluation open (direct request)

- [x] **Home press stopped working after a while** — root cause: `HomeDrawerRoute`'s Home-press collector ran the
  Drawer/Hub/carousel closes inline (~1 s); a drag landing on the same `Animatable` threw a `CancellationException` that
  escaped `collect` and unsubscribed it for good. Each close now runs in its own `launch`, and an already-closed axis
  returns immediately. Confirmed from phone logcat (`MutationInterruptedException` → collector gone → `subscribers=1`).
- [x] **Every sheet closes on Home** — new `DismissOnHomePress`; added to `FolderContentsSheet` and the contact
  connections sheet, clock sheet/clock widget picker reset in the route's own collector.
- [x] **`ThemedModalBottomSheet`** — one themed M3 sheet (+ Home-swipe block + Home-press dismiss) for the app, folder
  and clock menus and the set-default prompt; drops the clock menu's hand-rolled overlay and the set-default prompt's
  fixed padding that ignored the nav bar inset. `skipPartiallyExpanded` for menus short enough to show whole.
- [x] **Debug logging removed** — `SCROLLPROBE` `Log.e` calls (fired every scroll/layout pass, in release too), the
  assertion-less `debugScrollProbe` test, and `WidgetGrabGesture`'s per-pointer-event `Log.d`s.
- [x] **System bar icons setting** — `SystemBarIconStyle` (Match theme / Light / Dark), Appearance → General, global,
  in backups (defaulted, no version bump). Rule in `ui/theme/SystemBars.kt`: wallpaper screens use the setting, opaque
  screens follow the theme, Private Space always light; sheets set their own nav icons. Contrast scrims off on API 29+.
- [x] **Gesture hint refresh** — four hints (new clock-hold hint) at proportional offsets on the side each gesture starts
  from; overlay 90% opaque; "Got it" gets a 1.5dp `Muted` border + shadow (its fill was 1.05–1.3:1 against the overlay).
- [x] **Clock adjust-mode Alignment pill** — `ClockAdjustToolbar`, left/center/right, below the height handle (the
  clock block is pinned only 24dp above it), dimmed to 25% and disabled during any handle/resize drag and held back
  ~200ms after, swallows its own taps even then (Home's root exits adjust mode on any unconsumed tap), no Done button. Writes through
  `HomeViewModel.onClockAlignmentCommit` with the same facet-or-global ownership as height/scale.
- [x] **Set-default prompt skipped when already default** — no more "Facet is already your home screen" sheet:
  `HomeUiState.isDefaultLauncher` is tri-state, the prompt shows only when confirmed `false`, the gesture hint waits
  for the check, and `HomeViewModel` marks the prompt seen for an already-default device. Dropped `AlreadyDefaultContent`
  and its two strings (all five languages).
- [ ] **Evaluate on a real device:** is the 8dp gap (`CLOCK_ADJUST_TOOLBAR_GAP`, measured from the handle's 48dp touch
  strip) enough against accidental touches — the earlier recommendation was 16dp; does the pill covering the top of the
  app list bother; update the clock sheet row subtitle ("Resize or reposition the clock") to mention alignment.
- [x] **Overflowing Home list opened the drawer instead of scrolling (real bug; its regression test had never passed)** —
  `HomeScreen` reported the app list's bounds to `HomeDrawerRoute` from a `SideEffect`, which doesn't subscribe to the
  state it reads: `appListSize`/`appListOriginInRoot` are written by `onGloballyPositioned` after layout and read nowhere
  in composition, so the route kept a stale rectangle (in the test, the empty list's zero height) and its swipe detector
  claimed drags that started on the list. Now a `snapshotFlow` in a `LaunchedEffect`, with no extra recompositions.
  `HomeDrawerRouteTest.swipingWithinAnOverflowingAppListScrollsItInsteadOfOpeningTheDrawer` is the regression test
  (also failed at its own introducing commit, so no bisect target).
- [x] **Tests**: `HomeDrawerRouteTest` gains the regression test (fails on the original code at the final assertion) and
  Home-press cases for the clock sheet; `FolderContentsSheetTest`/`AppDrawerScreenTest` Home-press cases;
  `SystemBarsTest` (rule truth table) + `SystemBarsAppearanceTest` (real window flags); `SettingsRepositoryTest`,
  `BackupRepositoryTest`, `ImportBackupUseCaseTest` for the new key; `AppearanceSettingsScreenTest` row;
  `ClockAdjustToolbarTest`, `HomeScreenTest` (toolbar visibility/placement/fade/taps), `HomeViewModelTest`
  (`onClockAlignmentCommit` facet vs global); gesture-hint test asserts all four hints. `TEST_REGISTRY.md` regenerated.
- [x] **Docs**: `12 §4` (adjust mode, Alignment pill, Home press, sheets), `13 §2a` + `§4` (system bar icons, hint,
  set-default sheet), `02` (key, writer, section), `04` (tree), `09` (backup field), `CAPABILITIES.md`,
  `ONBOARDING_FLOW.md`, `ONBOARDING_DESIGN_BRIEF.md`, design `README.md`/`PRD.md` revision notes, `CLAUDE.md`
  (sheet + Home-press conventions, test-name pitfall), `CLOCK_RESIZE_SPEC.md` (marked historical).

## ✅ Icon shape — built (direct request)

- [x] **`IconShape`** (Squircle n=4 default / Rounded n=2.6 / Circle / Square) — global, free, independent of `IconRenderMode`.
  `icon_shape` DataStore key + `setIconShape`, `LocalIconShape`, `LauncherUiState.iconShape`, backed up (additive field,
  no version bump). Rounded (n=2.6) matches One UI's icon mask, which measures n≈2.5–2.6 on a Galaxy S25 Ultra, and is visibly rounder than the textbook squircle.
- [x] **M3 icon-radius tiers retired** — `AppIcon` has no `cornerRadius` param (nor its size-derived 4/8/12dp tiers);
  CLAUDE.md carries an exception to the M3 shape rule for app icons. `AppIconGlyph` clips the icon *and* the no-icon
  placeholder to the chosen shape; `ui/components/SuperellipseShape.kt` holds `SuperellipseShape(n)` (`SquircleShape` n=4, `RoundedShape` n=2.6) and `toComposeShape`.
- [x] **Appearance → General → "Icon Shape"** — visual picker under Icon Style (each option drawn in its own shape,
  selected one outlined in Accent); hidden on the facet-scoped screen like the other global rows.
- [x] **Tests**: `SuperellipseShapeTest`, `SettingsRepositoryTest` (default + round-trip), `AppearanceSettingsViewModelTest`,
  `AppearanceSettingsScreenTest` (picker persists; hidden when facet-scoped). `TEST_REGISTRY.md` regenerated.
- [x] **Docs**: `02` (ER, writer, key table, counts), `09` (backup field), `13 §2` (theme locals), `README.md` counts.

## 🚧 In progress — Facet automation rules (all eight phases built; backup/restore of rules deferred)

Built-in rules that switch the active facet automatically. Entry point: **Settings → Facets → "Facet automation"**, a row under "Manage facets". The screen lists rules and carries a card explaining that the per-facet "Switch to <name>" shortcuts work in Samsung Modes & Routines / Tasker for anything more advanced. Builds on `ActivateFacetByIdUseCase` and the shortcut/deep-link work above.

**Decisions:**
- **One trigger per rule, no AND/OR** in v1. Rule = target facet + trigger + end behavior + enabled.
- **End behavior is per rule:** *Return to baseline* (default) / *Switch to facet X* / *Stay*.
- **Level-based evaluation, not edge-triggered.** Overlapping rules must resolve to "back to the other rule", which an edge model can't express.
- **Evaluated lazily** on screen-on, unlock, Home press and startup — no alarms, no `SCHEDULE_EXACT_ALARM`. A 9:00 rule applying at 9:03 on unlock is indistinguishable from exact timing, and it never switches under a user mid-use. Device state (Bluetooth, charging) is also read at startup, since a killed process misses broadcasts.
- **Persisted state (DataStore):** `baselineFacetId`, `suppressedRuleIds`, last evaluated truth per rule.
- **Manual wins.** Carousel, Facet settings "Activate", **shortcuts and deep links** all count as manual: set baseline = chosen facet and suppress every currently-true rule until it ends. Only the evaluator switches as `Automation` (baseline and suppressed untouched). The source is an explicit parameter at the single choke point, never inferred by watching the active facet.
- **Evaluator:** drop suppressed rules that are now false → candidates = enabled, entitled, true, non-suppressed → most recently activated wins, else baseline. A rule's end behavior runs on true→false and is **skipped if the rule was suppressed**. A rule that becomes true after a manual switch applies normally.
- **No toast** on automatic switches.
- **Triggers** (each device trigger has a *while connected / while not connected* polarity):
  - Free: schedule (weekdays + start/end).
  - Pro: Bluetooth device, Wi-Fi (any network, or a specific SSID), headphones, and **Battery** (one trigger: a charging state plus a below/above level, working charging or not — see the Battery bullet in the Design block).
  - Out: calendar, Battery Saver (possible later).
- **Permissions are settled at the moment the user makes the choice that needs them** (see "Permission gate" in the Design block): `BLUETOOTH_CONNECT` (runtime) when Bluetooth is chosen; location (runtime, system location toggle on) when **"Named network"** is chosen for Wi-Fi, since that is how a network name is read; `ACCESS_NETWORK_STATE` (normal, no prompt) covers "Any network". Schedule, Headphones and Battery need none. A permission revoked later in system settings makes the saved rule unavailable rather than failing silently (PRD revocation principle). Verify the merged manifest still has no `INTERNET`.
- **Pro gating is one seam** until billing lands: `CanUseTriggerUseCase(type)` and the rule limit, both treated as entitled. **Free users get 2 rules** (`AutomationLimits.FREE_MAX_RULES`, built and tested): every saved rule counts, enabled or not, and editing never counts as adding. Pro has no cap yet. Existing Pro-trigger rules, and rules beyond the free 2, pause (not deleted) if entitlement is revoked — *proposed:* the first two by list order stay active. The 3→10 facet cap and billing are a separate plan.
- **Rules cascade-delete with their target facet** (FK). A deleted baseline facet falls back through `EnsureActiveFacetUseCase`.

**Design — visual reference: [`facet-automation.html`](<Android launcher design planning/design_handoff_minimal_launcher/facet-automation.html>)** (nine screens, light + dark toggle: Settings entry, automation list, schedule editor, trigger picker, Wi-Fi named network, free-user state, permission denied, unavailable rule, battery editor). Open it in a browser and build phase 6 against it. Built from `SettingsCard`, `StickyHeaderLayout`, the `4p` dashed strip and `ThemedModalBottomSheet`.
- **Entry:** a second row, "Facet automation", under "Manage facets" in Settings' FACETS card. Subtitle is the live count ("2 rules active") or "No rules".
- **Automation screen** (`StickyHeaderLayout` + back button, `SurfaceContainer` page, 24dp gutter):
  - **Status line** at the top, only when at least one rule exists. It replaces the toast we decided against and answers "why does Home look like this?". Rule-driven: "Showing Work because of the rule Weekdays 9:00–18:00". Manual: "Showing Travel, the facet you chose. <rule> resumes when it ends".
  - **RULES card:** one row per rule — trigger icon, derived title, subtitle ("Work, then previous facet"), enable switch — then an accent "Add rule" row, shown only when allowed.
  - **Rule titles are derived from the trigger** ("Weekdays · 9:00–18:00", "Car · connected"); rules have no name field.
  - **USE OTHER APPS card:** plain text — every facet has a "Switch to" shortcut for Tasker, Samsung Modes and Routines and similar apps; choosing a facet yourself, or with a shortcut, pauses any rule that is already active until it ends. This is the only place the user learns the manual-wins rule, so it stays visible.
  - **Empty state:** dashed strip, "Add a rule to switch facets automatically", action "Add rule".
- **Rule editor** is a `ThemedModalBottomSheet` (so it also closes on Home press): Switch to (facet dropdown) / When (trigger) / trigger parameters / When it ends (radio: Return to previous facet · Switch to a facet [dropdown] · Stay on <target>) / Cancel + Save. "Delete rule" shows only when editing.
  - **Schedule:** day chips (announce full day names) + From/Until time rows, to the minute. **From starts at the first second of that minute and Until runs through the last second of its minute**, so From = Until is valid and means exactly one minute. Until earlier than From means an overnight rule that belongs to the day it starts on, with a helper line "Ends next day". **A schedule with no days can't be saved:** Save stays tappable (no disabled buttons) but shows the inline message "Pick at least one day" and writes nothing.
  - **Device triggers:** a "While connected / While not connected" segmented control. Bluetooth adds a device picker. Headphones has no other parameters.
  - **Wi-Fi (decided):** a segmented "Any network / Named network" choice, then — only for Named network, and only once the location permission is granted — a **separate "Network" field** for picking the actual network. Choosing Named network runs the permission request; denied leaves the choice on Any network.
  - **Network field (decided): pick from a list only — the user never types a name or address.** The list shows the current network first (when connected), then networks in range from the latest scan. **Consequence:** Android hides a user's saved networks from apps, so only a network that is in range when the rule is set up can be chosen (an office network can't be picked from home). Matching is by network name.
  - **Bluetooth (decided):** the device is picked from the paired-devices list, never typed. A device with no name is stored with its address as the name, so a picked device always has both. Not picking one blocks Save.
  - **Battery (decided): one trigger = a charging state plus a level, and the level works in both states.** The editor shows a **Charging / Not charging** choice, then a **Below / Above** choice with a threshold slider. The level is always required (there is no "any level" option), which gives four combinations: not charging below, not charging above, charging below, charging above (for example "not charging, below 20%" or "charging, above 80%"). **Consequence: a plain "when charging" rule with no level can't be expressed;** the closest are "charging, below 100%" (charging and not yet full) and "charging, above 5%". Model: `Battery(whileCharging, level: BatteryLevelCondition)`, where the condition is `BatteryLevelCondition(direction, thresholdPercent)`. Levels are **strict**: below 20% means 19% and under, below 5% means 1–4%, below 100% means "not full"; above 80% means 81% and over. The slider has 5% stops, 5–100% for Below (20 stops) and **5–95% for Above (19 stops), because nothing is above 100%**. The picker shows a single "Battery" row. The semantics are built and tested (`Battery.isMetBy(charging, levelPercent)`, `BatteryLevelCondition.isMetBy`); the source that supplies level and charging state is phase 5. Whether hysteresis is needed at all is a phase 5 call, since evaluation is sampled when Home is about to be seen.
- **Trigger picker** (built as a dropdown row in the editor, not a second sheet): Schedule (free) then Bluetooth, Wi-Fi, Headphones, Battery, each with a Pro pill when the user is on the free plan. Everyone is entitled until billing exists, so the pills only appear once `EntitlementRepository` can say otherwise.
- **Pro states** (once billing lands): tapping a locked trigger opens the upgrade sheet from the billing plan. A paused rule is dimmed with a lock and the subtitle "Paused. Needs Pro", and can still be deleted. A free user sees a quiet dashed strip ("Free includes 2 schedule rules. Pro adds more rules and triggers for Bluetooth, Wi-Fi, charging, headphones and battery." + "See Pro"), not a banner. **At the 2-rule limit** the "Add rule" row shows a Pro pill and opens the upgrade sheet instead of the editor.
- **Permission gate (decided): the permission is requested when the trigger type changes, and a rule without its permission cannot be configured.**
  - **Bluetooth:** choosing the type runs the system request immediately, after the Pro check — a locked type opens the upgrade sheet and never asks for a permission. Granted shows the Bluetooth configuration; denied returns the type to the default, Schedule, and the Bluetooth configuration is never shown.
  - **Wi-Fi:** the type itself needs nothing, so it opens with "Any network". Choosing **Named network** runs the location request: granted reveals the separate Network field; denied leaves the choice on Any network.
  - No permission strips and no half-configured rule exist in the editor.
  - **Denial the system won't re-prompt (don't ask again)** takes the same path, plus a one-line note under the row that reverted (confirmed): "Bluetooth rules need Bluetooth access. Allow it in system settings." with "Open settings" (for Wi-Fi: "Named networks need location access. Allow it in system settings.").
  - **Revoked after the rule was saved:** the rule stays but is unavailable (not evaluated) and the list row reads "Needs Bluetooth access". Tapping it requests the permission first; granted opens the editor, denied does nothing. The editor is never opened in an unpermitted state. A Bluetooth rule whose device is gone reads "Device not found".
  - Save no longer needs a missing-permission exception. Invalid data (for example no schedule days) is a separate matter and is validated on its own.
- **Accessibility:** rows ≥48dp, switches labelled with the rule summary, radio group with proper roles, chips with full day names, strips and status line readable as plain text.
- **Theming:** all colors from `ui/theme/` tokens (`Surface`, `Ink`, `Muted`, `Hairline`, `Accent`, `ErrorColor`); light + dark `@Preview` per composable; shapes from `MaterialTheme.shapes` (cards `.medium`, chips/segments per the shape table).
- **Copy rules:** sentence case, no terminal punctuation on labels, no "please"/"successfully"; all strings in `values/` plus the four translations (also update the string-resource `ComposeHardcodedText` Detekt rule's expectations if it flags anything).
- **Design handoff:** done with phase 6 — a "Facet automation" section and the new `4p` rows are in the design `README.md`.

**Phases (evaluator first — it is the riskiest logic):**

- [x] **1. Evaluator + tests, no UI.** Built as `domain/EvaluateFacetAutomationUseCase` plus `data/model/AutomationRule`/`RuleEndBehavior`/`AutomationState` (`afterManualSwitch` is the manual-switch rule). 22 JVM tests green (`EvaluateFacetAutomationUseCaseTest` timeline scenarios, `AutomationStateTest`). Takes `conditionsMet` (rule ids whose trigger holds and are usable) and `existingFacetIds`. `AutomationRule` has no trigger yet — phase 3 adds it. `domain/EvaluateFacetAutomationUseCase` as a pure function over (rules, trigger truth, baseline, suppressed, last truth, now) → desired facet + new state. Scenario-table unit tests: baseline return, overlap (Work + Car, Car ends → Work), manual switch suppresses then clears, suppressed rule skips end behavior, switch-to-X and Stay endings, new rule after manual switch, deleted/disabled-while-active, first run.
- [x] **2. Switch source.** `FacetSwitchSource` (MANUAL default / AUTOMATION) on `ActivateFacetByIdUseCase`, which is now the single choke point for user switches: carousel (`selectFacet`), Manage facets and Facet settings "Apply", plus the shortcut/deep link via `LauncherViewModel`. A manual switch writes `AutomationState.afterManualSwitch` first, then the active facet (so an evaluation between the two sees the override). **Pulled forward from phase 3:** `AutomationStateRepository` over its own `facet_automation` DataStore (`@AutomationDataStore`), kept out of `LauncherSettings` and backups. Deliberately left calling `SettingsRepository.setActiveFacetId` directly: the two delete-the-active-facet fallbacks, `EnsureActiveFacetUseCase` (startup) and `ImportBackupUseCase` — repairs, not user choices; the evaluator tolerates a stale baseline. Tests: `ActivateFacetByIdUseCaseTest` (manual / automation / no-op), `AutomationStateRepositoryTest`, and state-recording tests in `ManageFacetsViewModelTest`, `FacetSettingsViewModelTest`, new `FacetCarouselViewModelTest`. Instrumented tests compile with the new constructors (`ui/AutomationTestSupport.kt`) but were not run on an emulator. Docs `01`, `02 §5.7`, `04`, `07`, `13`, `14` updated.
- [x] **3. Data.** `AutomationTrigger` (sealed: Schedule, Bluetooth, Wifi, Headphones, Battery with a required `BatteryLevelCondition`; the entity has a `batteryDirection` column next to `batteryThreshold`, both required for a battery rule, and the 25→26 migration was amended in place because it had never shipped) and `trigger` on `AutomationRule`; `AutomationRuleEntity` (flat columns, trigger/end behavior as plain strings so no new `Converters`; `targetFacetId` CASCADE, `endFacetId` SET NULL), `AutomationRuleDao`, `AutomationRuleRepository` + `AutomationRuleMapping` (skips rows it can't interpret; a deleted switch-to facet degrades to return-to-baseline), `FacetDatabase.VERSION` 25 → 26 with `MIGRATION_25_26` (DDL from the exported `26.json`). Tests: `AutomationRuleDaoTest` (ordering, both FK actions), `AutomationRuleRepositoryTest` (round-trip of every trigger and ending, save/update/order, skipped rows), `FacetDatabaseMigrationTest.migration25To26…` (ran green on the emulator with the other 13). `FacetDatabase` carries `@Suppress("TooManyFunctions")` (one accessor per DAO). Docs `01`, `02`, `04`, `07`, `15` updated. **Decided: automation backup/restore is deferred until all phases are built.** Until then rules are not in `BackupBundle`: import calls `deleteAllFacets()` first, so a restore deletes every rule (cascade); documented in `02 §6`. Doing it later needs `BackupBundle` fields (facet refs by index) and a version call.
- [x] **3b. Schedule semantics + validation (decided and built).** `AutomationTrigger.Schedule.isActiveAt(LocalDateTime)`: minute-granular, **start minute active from its first second, end minute through its last**, so `start == end` is exactly one minute (e.g. 9:00–9:00 is active 9:00:00–9:00:59); an end before the start is overnight and belongs to the day it starts on (Friday 22:00–06:00 includes Saturday until 06:00:59, but not Saturday evening); an empty day set is never active. `AutomationRule.validationErrors()` (`data/model/AutomationRuleValidation.kt`) rejects **no days** (`NO_SCHEDULE_DAYS`) and minutes outside 0..1439 (`INVALID_SCHEDULE_MINUTE`); `start == end` and overnight are valid. `AutomationRuleRepository.save()` now returns `SaveRuleResult` (`Saved(id)` / `Invalid(errors)`) and writes nothing when invalid; the phase 6 editor uses the same check for its inline messages. Tests: `AutomationTriggerTest` (8, including the second-level boundaries), `AutomationRuleValidationTest` (8), and rejection cases in `AutomationRuleRepositoryTest`. **Also rejected, from later decisions:** a named Wi-Fi network that is blank (`BLANK_WIFI_NETWORK`; null still means any network), a Bluetooth rule whose device address or name is blank (`NO_BLUETOOTH_DEVICE`), and a battery level condition whose threshold isn't one of the slider stops for its direction — 5% to 100% for Below, 5% to 95% for Above, in 5% steps (`INVALID_BATTERY_THRESHOLD`). **A rule whose target or "switch to" facet no longer exists is a no-op, not an exception:** `save()` returns `SaveRuleResult.FacetMissing` and writes nothing (checked against `FacetDao`); at run time a switch to a deleted facet is already a no-op (`ActivateFacetByIdUseCase` guard, covered for the automation source too), and a stored "switch to" ending whose facet was deleted becomes return-to-baseline. **No format or length checks on addresses and names:** they are only ever picked from lists. **Deliberately allowed:** a "switch to" ending that points at the rule's own target facet, and duplicate rules — the free cap below bounds clutter and the editor offers nothing to prevent them.
- [x] **4. Schedule trigger + runner (built).** `WakeEventsRepository` (screen on, unlock, clock/timezone change), `RefreshAutomationStateUseCase` (samples every rule against the injected `Clock` and persists the evaluator's state, without switching), `ApplyFacetAutomationUseCase` (one pass: refresh, then an `AUTOMATION` switch if the rules want a different facet and the user hasn't switched by hand meanwhile), `RunFacetAutomationUseCase` (one sequential, conflated collector started from `LauncherViewModel.init`, triggered by wake events, Home presses, rule changes and active-facet changes). A schedule is true via `Schedule.isActiveAt`; device triggers are never met until phase 5. A switch only updates the active facet in the background — it never brings Home to the front or launches anything, so it can't interrupt another app. **Gap found and fixed:** evaluation is lazy, so a rule can be true but unobserved (a schedule started while Home stayed visible); a manual switch then wouldn't have suppressed it and the next evaluation would have overridden the user's choice. `ActivateFacetByIdUseCase` now re-samples the rules (`RefreshAutomationStateUseCase`) before recording a manual switch, which also covers the device triggers once their sources exist. Tests: `ApplyFacetAutomationUseCaseTest` (14 real-chain scenarios — Room, DataStore, evaluator, use cases, controllable clock — including the second-level window boundaries, an overnight window across midnight, manual-switch holding, the unobserved-rule gap, and a no-op before any facet exists), `RunFacetAutomationUseCaseTest` (7), `WakeEventsRepositoryTest` (5), plus new cases in `ActivateFacetByIdUseCaseTest`. All five hand-built-ViewModel instrumented classes ran on the emulator (86 of 88 on the first pass, 3 timing flakes in `FacetSettingsScreenTest` and `HomeDrawerRouteTest` that passed on re-run). Notes: the Robolectric test grants `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` itself (the shipped manifest declares it, so API 31–32 is fine); runner tests use `runCurrent()` because `advanceUntilIdle()` ignores `backgroundScope` work. Docs `03`, `04`, `06`, `07`, `15` updated.
- [x] **5. Device triggers (built).** `DeviceState` + `AutomationTrigger.isMetBy` (one pure truth function; unknown Bluetooth, no battery reading and an unreadable named network are "not met", negated or not), `HeadphonesRepository`, `WifiRepository` (synchronous initial read, then network callbacks; `nearbyNetworkNames` for the picker), `BluetoothRepository` (ACL broadcasts registered dynamically + one-time A2DP/headset/GATT query + 1.5 s timeout; `pairedDevices` for the picker), `DeviceStateRepository` (one hot shared state; `onPermissionsChanged()` re-subscribes Wi-Fi/Bluetooth), `AutomationPermissionRepository` (`isUsable` drops rules whose permission is missing), wired into `RefreshAutomationStateUseCase` and `RunFacetAutomationUseCase`. Manifest: `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` (no `INTERNET`). **Permissions screen:** Bluetooth and Location rows with explanations in all five languages, `bluetooth_permission_requested` / `location_permission_requested` flags, grants wake the sources. No hysteresis for battery (strict thresholds, level moves 1% at a time). Headphones counts Bluetooth audio too (Android can't tell a car stereo from headphones). Tests: `DeviceStateTest` (17), `HeadphonesRepositoryTest`, `WifiRepositoryTest`, `BluetoothRepositoryTest`, `DeviceStateRepositoryTest`, `AutomationPermissionRepositoryTest`, 20 real-chain scenarios in `ApplyFacetAutomationUseCaseTest` (device triggers, permissions, the unobserved-rule gap), `PermissionsViewModelTest`. Docs `01`–`04`, `07`, `15` updated.
- [x] **6. UI (built).** `ui/settings/automation/`: `FacetAutomationScreen` (status line, rules card with enable switches, shortcuts card, empty state, unavailable rows), the Settings entry row (`SettingsUiState.automationRuleCount`), and the rule editor sheet (`ThemedModalBottomSheet`, so Home press closes it) with the schedule editor (day chips, From/Until, "Ends the next day"), Bluetooth, Wi-Fi (Any/Named + Network picker), headphones and battery editors, End-behavior radios, inline validation, Delete with confirmation. The permission gate lives in `RuleEditorViewModel.changeTrigger`: Bluetooth type and Named network ask first; a refusal keeps the previous trigger and shows a note with "Open settings"; tapping an unavailable row asks for its permission. `ObserveFacetAutomationUseCase` feeds the list; `pairedDevices()` and `nearbyNetworkNames()` became `suspend` on `Dispatchers.IO`. Strings in 5 locales, light and dark previews, 44dp day chips, radio groups with `selectableGroup`, design handoff `README.md` updated. Tests: `ObserveFacetAutomationUseCaseTest`, `AutomationRuleFormattingTest`, `FacetAutomationViewModelTest`, `RuleEditorStateTest`, `RuleEditorViewModelTest` (JVM), `FacetAutomationScreenTest`, `RuleEditorSheetTest`, `SettingsScreenTest` (emulator). **Deviations:** the trigger picker is a dropdown row in the editor rather than a second sheet, and a Bluetooth rule whose device was unpaired is not yet flagged "Device not found" (both noted in the handoff README).
- [x] **7. Pro seam (built).** `EntitlementRepository.isPro` (the single seam, constant `true` until billing), `CanUseTriggerUseCase` (`invoke(trigger, isPro)`; `entitledRuleIds`: free keeps the first two *schedule* rules in list order), `SaveAutomationRuleUseCase` (trigger gate, then the free rule limit for a new rule, then validation; `SaveRuleResult.ProRequired(ProReason)`), and the entitlement filter in `RefreshAutomationStateUseCase` (paused rules never run and are never deleted; `RunFacetAutomationUseCase` re-runs when entitlement changes). UI: Pro pills on device triggers in the *When* dropdown (a locked pick opens the upgrade sheet and never asks for a permission), "Add rule" with a Pro pill at the limit, `NEEDS_PRO` rows ("Paused. Needs Pro", lock, switch off, tap opens the upgrade sheet), the free-plan strip with See Pro, and `UpgradeSheet` (explains and dismisses; the purchase action is the billing plan's). Tests: `CanUseTriggerUseCaseTest`, `SaveAutomationRuleUseCaseTest`, additions to `ObserveFacetAutomationUseCaseTest`, `ApplyFacetAutomationUseCaseTest`, `RunFacetAutomationUseCaseTest`, `RuleEditorViewModelTest`; `FacetAutomationScreenTest` and `RuleEditorSheetTest` on the emulator. **Also in this step: M3 shape audit** (strips 12dp, mockup sheet 28dp, day chips are M3 filter chips) and a docs drift sweep (counts and registries in `01`–`07`, the privacy policy, `CAPABILITIES.md`).
- [x] **8. Docs + site (done).** Architecture docs `01`–`07`, `12 §4` and `15` match the code (counts, registries, the Pro seam, the UI), `CAPABILITIES.md` has a Facet automation section, `TEST_REGISTRY.md` is regenerated, and the privacy policy lists the Bluetooth, location and network-state permissions (edited in `../facet-launcher-site/build.py`, regenerated, and mirrored in `docs/privacy-policy.html`; the site repo changes are uncommitted and nothing is published). **Not done:** a Facet automation entry on the site's features page (marketing copy), and automation in backup/restore (deferred by decision).

**Tests:** evaluator scenario tests (phase 1), `AutomationRuleRepositoryTest` + migration test, `ActivateFacetByIdUseCaseTest` for source handling, ViewModel tests, Compose tests for the rule editor and permission prompts.

## 🚧 In progress — Billing: one-time "Facet Pro" purchase (phases 1-2 of 5 built)

A single one-time Google Play purchase, no subscription. It replaces the constant-`true` `EntitlementRepository` and adds the facet cap (3 free, 10 with Pro). Everything that gates automation already reads `EntitlementRepository.isPro`, so billing only has to make that value real. Play Store only.

**Decisions:**
- **One product** (`pro`, non-consumable). Pro covers *both* more facets (up to 10) and automation (device triggers, more than 2 rules). The **price is never in the code**: it is a Play Console setting and the app shows whatever Play returns for the product.
- **Entitlement is three-valued internally:** granted, not granted, unknown. **Only an explicit "no purchase" answer from Play turns Pro off.** Play being unavailable, offline or slow keeps the last known value (cached in DataStore), so Pro works offline and at cold start. With no cache and no answer yet, the user is Free (nothing proves otherwise). A *pending* purchase is not Pro until it completes.
- **A lapse is only a refund or revocation** (or a different Google account on the phone). It cannot be expiry. When it happens:
  - **Facets:** the first 3 by list order stay selectable; the rest are **disabled** (dimmed, lock, tap opens the upgrade sheet) until the count drops to 3 or below. **A disabled facet can still be deleted** (the existing rule that the last remaining facet cannot be deleted still applies), which is how the user gets back under the limit. Nothing is deleted for them. Adding is blocked.
  - **Automation:** unchanged and already built: Pro-trigger rules and rules beyond the first two schedule rules **pause**.
- **Facet cap:** `FacetLimits.FREE_MAX_FACETS = 3`, `PRO_MAX_FACETS = 10`. Today `FacetRepository.MAX_FACETS = 3` is a constant checked in `ManageFacetsViewModel`, `FacetCarouselViewModel` and the repository; it becomes entitlement-aware. Nothing above 3 has shipped, so no grandfathering.
- **No `INTERNET` permission.** Play Billing talks to the Play Store app over IPC. Verify against the merged manifest when the library is added; the privacy policy gets a line that purchases are handled by Google Play.

**Design:**
- **`BillingRepository`** wraps `BillingClient` (kept thin and `open` so tests can substitute it): connect, query owned purchases, product details (price), launch the purchase flow (needs an `Activity`, so the ViewModel emits a `UiEvent` and the screen launches it), acknowledge within 3 days, and a `PurchasesUpdatedListener` that is always registered so a purchase or promo code redeemed outside the app is picked up. It re-queries at startup and on every resume.
- **`EntitlementRepository`** (becomes real): combines the cached value in DataStore with `BillingRepository`. `isPro: StateFlow<Boolean>` keeps its shape.
- **`FacetLimits` + `SelectableFacetsUseCase`** (pure): `maxFor(isPro)`, `canAdd(count, isPro)`, and `selectableFacetIds(facets, isPro)` (Pro: all; Free: first 3 by position).
- **Single choke point for selection:** `ActivateFacetByIdUseCase` refuses a facet that is not selectable, which covers the carousel, shortcuts and deep links. `EnsureActiveFacetUseCase` falls back to the first selectable facet if the active one is disabled. Automation receives only selectable ids as `existingFacetIds`, so a rule targeting a disabled facet is a no-op through the existing "facet missing" fallback. `SyncFacetShortcutsUseCase` publishes shortcuts only for selectable facets.
- **UI:**
  - Carousel and Manage facets show disabled facets dimmed with a lock; tapping opens the upgrade sheet. Add facet at the limit carries a Pro pill and opens the same sheet.
  - **One shared `ProUpgradeSheet`** in `ui/components` (the automation `UpgradeSheet` moves there) with a reason line (more facets, automation trigger, automation rule limit), the Play price, **Buy**, **Restore purchases**, and states for loading, unavailable, pending and error.
  - **Settings → "Facet Pro" row:** shows Pro or Free, with Buy and Restore, so restoring is discoverable without hitting a limit.
- **Backup/import:** imports all facets as they are; the lapse rule above handles a free user who imports more than 3.
- **Debug only:** an entitlement override in the `debug` source set so every Pro state can be tested without Play. Release builds cannot reach it.

**Phases (limits first: everything in 1 is testable with a fake entitlement, no Play needed):**

- [x] **1. Facet limits and gates (built).** `FacetLimits` (3 free, 10 Pro, `selectableIds` by list order), `SelectableFacetsUseCase`, `AddFacetUseCase` (`AddFacetResult`), and `ProReason` (now `FACET_LIMIT`, `FACET_LOCKED` too). Enforcement: `ActivateFacetByIdUseCase` is a no-op for a disabled facet (covers shortcuts and deep links); `EnsureActiveFacetUseCase.keepUsable()` (collector started from `LauncherViewModel`) moves the active facet to the first selectable one when the selectable set changes; `SyncFacetShortcutsUseCase` publishes only selectable facets; `RefreshAutomationStateUseCase` passes only selectable ids to the evaluator, so a rule aimed at a disabled facet is a no-op. UI: disabled facets are dimmed with a lock in Manage facets and the carousel (tap opens the upgrade sheet; Manage keeps *Delete* enabled and disables *Facet settings* and *Apply*), the add row and add page show at the free limit with a Pro pill, `ProPill` and `ProUpgradeSheet` moved to `ui/components` with facet reasons (strings in 5 locales, `facet_locked_content_description`). `FacetRepository.MAX_FACETS` is gone. Tests: `FacetLimitsTest`, `SelectableFacetsUseCaseTest`, `AddFacetUseCaseTest`, additions to the activate, ensure-active, sync, automation-apply and Manage facets ViewModel tests; `ManageFacetsScreenTest` and `FacetCarouselScreenTest` on the emulator. Everything is testable today with `FakeEntitlementRepository`; nothing is visible to users until billing makes the entitlement real.
- [x] **2. Billing and entitlement (built).** Play Billing `billing-ktx:9.1.0` (the latest stable release). `BillingRepository` (`open`, `queryPro()`, `priceText()`, `launchPurchase(activity)`, `updates` flow, acknowledges purchases, pending purchases enabled) with the pure mapping in `ProPurchaseMapping.kt`; product id `facet_pro` (`BillingProducts.PRO`, must match Play Console). `EntitlementRepository` is now the "everyone is Pro" base class, and `PlayEntitlementRepository` (bound by `EntitlementModule`) caches the last answer in a new `facet_entitlement` DataStore: only an explicit owned or no-purchase answer changes it, a pending purchase or an unreachable Play keeps it, and an empty cache with no answer is Free. It persists before publishing, `awaitLoaded()` stops a cold start reconciling facets against a placeholder (`SelectableFacetsUseCase` waits on it), and `LauncherActivity.onResume` refreshes. **Finding:** the billing library merges `android.permission.INTERNET` (through `transport-backend-cct`, its telemetry) and pulls in `play-services-location`; the manifest now removes `INTERNET`, and the `verifyNoInternetPermission` Gradle task (part of `check` and the release build) fails if it returns. Verified on the emulator: the installed app requests the billing permission and `ACCESS_NETWORK_STATE`, not `INTERNET`. **Still to verify on a real Play account (phase 5):** that purchases and restore work with `INTERNET` removed. Tests: `ProPurchaseMappingTest`, `PlayEntitlementRepositoryTest` (cache semantics, refund, pending, in-app completion).
- [ ] **3. Purchase UI.** Buy and Restore in `ProUpgradeSheet`, the Settings "Facet Pro" row, price display, and pending/error/unavailable states; the debug override.
- [ ] **4. Docs, policy, release.** New `16-flow-billing.md` plus updates to `01`, `02` (DataStore keys), `03`, `07`, `13`, `15`; `CAPABILITIES.md`; the privacy policy line; the design handoff (upgrade sheet, disabled facets, Settings row, with M3 shapes). Add mockups for the new states first if you want them.
- [ ] **5. Play Console and testing (yours, with a checklist from me).** Create the `pro` product and price, add licence testers, upload to an internal testing track, run purchase, restore, refund and offline cases on a real device, and complete the Data safety form.

**Open:** the price (Play Console), the copy for the upgrade sheet, and whether to add mockups before phase 4.

**Where this stands:** phases 1 and 2 are built and committed (`2a2a362`, `7cd7b9c`). **Phase 3 (purchase UI and the debug entitlement override) is next, after the toolchain upgrade.** Heads-up: now that the entitlement comes from Play, a debug or sideloaded build is treated as **Free** (nothing is cached and Play has no purchase to report), so the Pro states and locked facets are visible on the emulator, and Pro cannot be switched on until the debug override in phase 3 exists. Product id is `facet_pro` and the library is `billing-ktx:9.1.0`. The billing library merges `INTERNET` in; the manifest removes it and `verifyNoInternetPermission` guards it (phase 5 must confirm purchases and restore still work on a real Play account).

## 🚧 In progress — Dependency upgrade to the latest stable versions

Direction from the owner: stay on the latest **stable** release of every library. Done in two batches so any breakage is attributable.

- [x] **Batch A: libraries (done, verified on the emulator).** Compose BOM 2026.01.01 → 2026.09.00, Navigation 2.9.6 → 2.10.2, Lifecycle 2.10.0 → 2.11.0, Activity Compose 1.10.1 → 1.13.0, Core KTX 1.18.0 → 1.19.1, Room 2.8.4 → 2.8.5, Hilt navigation 1.3.0 → 1.4.0, kotlinx-serialization 1.8.1 → 1.11.0, Robolectric 4.16.1 → 4.17, Mockito 5.23.0 → 5.24.0, AndroidX test orchestrator 1.5.1 → 1.6.1, compose-rules detekt 0.6.6 → 0.6.7, benchmark macro 1.2.0-beta01 → 1.5.0, UI Automator 2.2.0 → 2.4.0, and `compileSdk` 36 → 37 (the Android 37 platform was already installed; `targetSdk` stays 36, that is a behaviour decision, not a library). The long serialization-pin comment in `app/build.gradle.kts` was shortened. The newer Compose lint flagged two real `NonObservableLocale` errors in the day chips (`ScheduleFields.kt`), now fixed by reading `LocalConfiguration`. Checks: everything compiles including `:benchmark`, 999 unit tests pass, detekt and `lintDebug` clean, `verifyNoInternetPermission` passes, and the rule editor, automation screen, Manage facets, carousel and Settings suites pass on the emulator.
- [ ] **Batch B: toolchain (next, before billing phase 3).** Gradle 9.7.1 → 9.8.0 (`gradle/wrapper/gradle-wrapper.properties`), Android Gradle Plugin 9.4.0 → 9.4.1 (also the `com.android.test` plugin), Kotlin 2.3.10 → 2.4.20 (also the Compose compiler and serialization plugins, which track Kotlin), KSP 2.3.10 → 2.3.12 (check it supports Kotlin 2.4). Riskiest part: after it, run unit tests, detekt, lint, the Room schema export (`app/schemas`, must not change), a Hilt/KSP build, and the instrumented suites. **Not upgradable to a stable release:** detekt (`dev.detekt` 2.0.0-alpha.6 is the only 2.x line; the 1.23 line crashes on this machine's JDK 25). Already current: Hilt 2.60.1, DataStore 1.2.1, Play Billing 9.1.0, JUnit 4.13.2, coroutines-test 1.11.0, AndroidX test core/rules/ext-junit/espresso. Re-run the version check (`dl.google.com/dl/android/maven2` and Maven Central metadata) at the start of the session, since versions move.

## ✅ Facet clock row moved into Appearance; two stale tests fixed (direct request)

- [x] **Facet-scoped Appearance** now shows the clock preview and a "Clock" section with the Clock & calendar style row
  (opens that facet's own clock style screen), both resolved through the facet's `overrideClock`
  (`AppearanceSettingsUiState.clockSettings`). The row is gone from Facet settings; `FacetNavHost` routes the Appearance
  row to the facet gallery when a `facetId` is present.
- [x] **Tests**: `AppearanceSettingsScreenTest` (facet scope keeps clock row/preview, hides global-only rows, reflects an
  override, row navigates; scope settle now waits on the view model instead of the clock row), `FacetSettingsScreenTest`
  (clock row removed), `FavoritesPickerScreenTest` (cap test searches for the overflow app before waiting for its row —
  the cap is 12, so it sits below the lazy list's fold).
- [x] **Docs**: `13` (nav list + Appearance cards), `CAPABILITIES.md`.
