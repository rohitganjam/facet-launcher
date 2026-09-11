# Lumen Launcher — Engineering Conventions

Native Android launcher app. Kotlin-only (no Java files), Jetpack Compose (no XML layouts), single `:app` module. MVVM throughout: composables → `ViewModel`s → `Repository`s → `domain/` use cases, strictly layered. Hilt for DI, Retrofit for networking, Room for persistence, Coroutines + `Flow` for async state (no `LiveData`).
Requirements: [`Android launcher design planning/design_handoff_minimal_launcher/PRD.md`](<Android launcher design planning/design_handoff_minimal_launcher/PRD.md>).
Design spec: [`Android launcher design planning/design_handoff_minimal_launcher/README.md`](<Android launcher design planning/design_handoff_minimal_launcher/README.md>).
Build/task tracking: [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) — keep it ticked off as work lands.

## Project structure

```
app/src/main/kotlin/com/lumenlauncher/app/
  data/            # Repositories, DAOs/services, models — wraps Android framework APIs (LauncherApps, WallpaperManager, etc.), Room, Retrofit
    model/
  domain/          # use cases — business rules that compose one or more Repositories; nothing here touches Android framework APIs directly
  ui/
    theme/         # Color.kt, Type.kt, Theme.kt — tokens transcribed from README.md's Design Tokens table
    launcher/      # top-level screen composition / gesture routing (Home <-> Drawer <-> Hub)
    home/
    drawer/
    hub/
    profiles/
    settings/
app/src/test/kotlin/...        # JVM unit tests (Robolectric where Android classes are touched)
app/src/androidTest/kotlin/...  # instrumented Compose UI tests (run on the Medium_Phone_API_36.1 AVD or a device)
```

One package per screen/surface area, mirrored between `main`, `test`, and `androidTest`.

Naming: screen composables/wrappers end in `Screen` (`HomeScreen`), their `ViewModel`s in `ViewModel` (`HomeViewModel`), UI state classes in `UiState` (`HomeUiState`), one-off UI events in `UiEvent` (`HomeUiEvent`), data-source classes in `Repository` (`AppRepository`), and `domain/` classes in `UseCase` (`GetInstalledAppsUseCase`).

## Kotlin style

- Prefer immutable data (`val`, `data class`) for anything representing UI state or domain models.
- No `!!`. Handle nullability explicitly; if a null truly can't occur, that's what a non-null type is for.
- Strict layering, no shortcuts: composables never call `data/` or `domain/` directly — a `ViewModel` is the only thing a composable talks to. Business logic (querying `LauncherApps`, computing list groupings, permission checks) lives in `data/` `Repository` classes, never inline in a composable or `ViewModel`; logic that spans more than one repository becomes a `domain/` use case. Composables read state and emit events; they don't fetch or compute domain data themselves.
- Only a `Repository` touches a Room DAO or Retrofit service directly — never reach into either from a `ViewModel` or a composable.
- Inject dependencies (Repositories into ViewModels/use cases, DAOs/services into Repositories) with Hilt via constructor injection — no manual singletons or service locators.
- Coroutines: suspend functions for anything doing I/O or calling a blocking system API, dispatched on `Dispatchers.IO` — never block the main thread. Expose state via `StateFlow`/`Flow` only (never `LiveData`), collected in Compose with `collectAsStateWithLifecycle()`.
- Favor small, named functions over long ones with inline comments explaining sections — the section boundary itself should be the function boundary.
- Keep comments short. A one-line comment for genuinely non-obvious rationale; skip it otherwise. Some existing code carries long multi-paragraph comments — don't extend that style or add to it when editing nearby; trim it down where you touch it.

## Compose conventions

- **Stateless composables + state hoisting.** A screen composable (e.g. `HomeScreen`) takes an immutable `UiState` and event lambdas as parameters; its `ViewModel` (e.g. `HomeViewModel`) owns the actual state, exposes it as `StateFlow<HomeUiState>`, and is the only thing that talks to `data/`/`domain/`. One-off effects (navigation, snackbars) are modeled as a `UiEvent` emitted separately from state, not folded into it. This is what makes composables unit-testable without booting Android.
- Every public composable that renders meaningful UI gets a `@Preview` (light + dark where the design spec defines both) — cheap to add, catches layout breakage immediately.
- Use the design tokens from `ui/theme/` (`MaterialTheme.colorScheme`, `MaterialTheme.typography`) — never hardcode a color or text size that's already named in README.md's Design Tokens table.
- **Theme every Material3 component explicitly — never let one fall back to stock Material defaults.** `LumenLightColorScheme` (`ui/theme/Theme.kt`) only maps a handful of `ColorScheme` slots (`primary`, `surface`, `onSurface`, `onSurfaceVariant`, `error`, `outline`, ...); any component that reads an *unmapped* slot (e.g. `AlertDialog`'s default `containerColor`, which resolves to `surfaceContainerHigh`; `OutlinedTextField`'s default border/label colors) silently renders with Material's own baseline tones instead of this app's palette, producing a popup or field that's subtly off from every other surface in the app. When adding or touching a component like `AlertDialog`, `DropdownMenu`, `TextField`/`OutlinedTextField`, `Dialog`, etc., always pass its color/shape parameters explicitly (`containerColor`, `shape`, `colors = ...Defaults.colors(...)`) using this app's own tokens (`Surface`, `Ink`, `Muted`, `Accent`, `ErrorColor`, `Hairline`, 14dp `RoundedCornerShape` for popups/dialogs) rather than trusting the default resolves correctly — see `ui/components/ConfirmDialog.kt`, `RenameDialog.kt`, and `ThemedDropdownMenu.kt` for the pattern.
- Modifier parameter: always present, always first optional parameter, always applied to the composable's own outermost node (standard Compose convention — don't break it here).
- Use `LazyColumn`/`LazyVerticalGrid` for any list that can grow past a screenful (app drawer, contact list) — never a plain `Column` with `.verticalScroll()`.
- Keep composable parameters stable (immutable data classes/collections) and side-effect-free to avoid unnecessary recomposition.
- Gestures (swipe up/down, long-press, drag) are implemented with `pointerInput` / `detectDragGestures` etc. at the surface they're specified on in README.md — check the exact thresholds and easing named there (e.g. 420ms long-press, 55px swipe threshold, `cubic-bezier(.32,.72,0,1)`) rather than approximating.

## Shape (Material 3) — governing principle for every component

Every component's corner radius must come from Material 3's real shape scale and defaults ([m3.material.io/styles/shape](https://m3.material.io/styles/shape/shape-scale-tokens)), applied through `MaterialTheme.shapes` — never a bespoke dp value picked by eye. This app's `LumenLauncherTheme` doesn't override `shapes`, so `MaterialTheme.shapes` is already Compose Material3's own default `Shapes()` instance, matching M3's scale exactly:

| Token | Value | `MaterialTheme.shapes.*` | Used by (M3 default)                                   |
|---|---|---|--------------------------------------------------------|
| None | 0dp | — (`RectangleShape`) | —                                                      |
| Extra small | 4dp | `.extraSmall` | Text fields, snackbars                                 |
| Small | 8dp | `.small` | Chips, Menus/Dropdowns                                 |
| Medium | 12dp | `.medium` | Cards, small FABs                                      |
| Large | 16dp | `.large` | FABs, extended FABs, navigation drawers                |
| Extra large | 28dp | `.extraLarge` | Dialogs, large FABs, modal bottom sheets (top corners) |
| Full | fully rounded | `CircleShape` | Buttons, search bars, segmented buttons, switches      |

When adding or touching a component, look up its category on the [Compose Material3 component list](https://developer.android.com/develop/ui/compose/components) or the [`androidx.compose.material3` API reference](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary) and use that component's real default shape (verified against the library's own token source when in doubt — `ShapeTokens.kt`/`MenuTokens.kt`/`DialogTokens.kt`/etc. under `androidx.compose.material3.tokens`) rather than guessing a dp value. A "top corners only" variant (e.g. a bottom sheet) is built as a literal `RoundedCornerShape(topStart = ..., topEnd = ..., bottomEnd = 0.dp, bottomStart = 0.dp)` using the matching token's dp value, since `MaterialTheme.shapes` doesn't expose per-corner variants.

This is a deliberate departure from this project's earlier convention of following `design_handoff_minimal_launcher/README.md`'s own hand-specified radius values (14dp dialogs/cards/popups, 20dp sheet, etc.) — decided explicitly by the user over keeping the original mockup's numbers.

App/dock icon tiles (every place `ui/components/AppIcon.kt`'s `AppIcon` renders one) were originally exempted from this rule and left on README's own Design Tokens table, but were later folded in too (decided explicitly by the user — see chat history): `AppIcon`'s `cornerRadius` parameter defaults to a size-derived M3 tier — `extraSmall`/4dp for icons ≤24dp, `small`/8dp for 25–36dp, `medium`/12dp for 37dp+ — rather than a hand-picked ratio, and every icon size in the app should be one of the five named values in `AppIconSize` (`ui/components/AppIcon.kt`) rather than a bespoke `Dp`. The one remaining exemption is genuinely non-icon glyphs with no M3 or `AppIcon` counterpart of their own — drag handles, progress dots, and similar small decorative marks — which stay whatever shape reads best for their own specific use.

## Testing — required for every capability

**No feature is done until it has tests that would fail if the behavior broke.** When a plan task is marked complete in `IMPLEMENTATION_PLAN.md`, the tests for it must already be green.

Two tiers:

1. **Unit tests** (`app/src/test/kotlin`, JVM, fast, no emulator) — for everything in `data/` and any pure logic (grouping apps by letter, filtering, permission-state derivation). Use JUnit4 + Robolectric when a test needs real Android framework classes (`Context`, `LauncherApps`) without an emulator. Use `kotlinx-coroutines-test`'s `runTest` for suspend functions.
2. **Compose UI tests** (`app/src/androidTest/kotlin`, instrumented, runs on the `Medium_Phone_API_36.1` AVD or a connected device) — for composables: assert on rendered content (`onNodeWithText`, `onNodeWithContentDescription`) and interactions (`performClick`, `performTouchInput { swipeUp() }`). Every screen-level composable gets at least one test exercising its primary interaction (e.g. `AppDrawerScreenTest` asserts swiping down at scroll-top closes the drawer; scrolled-down swipe-down does not).

Conventions:
- Test class name mirrors the class under test: `AppRepository` → `AppRepositoryTest`.
- Test method names describe behavior, not implementation: `` `swipe down at top closes drawer`() `` not `testSwipeDown()`.
- Given/When/Then structure (as comments or blank-line sections) inside each test body.
- A task's "deliverable" in the implementation plan is not complete until its paired test(s) exist and pass — don't check off a box on green-code-with-no-tests.

Run tests:

```bash
./gradlew test                  # unit tests (fast, no device needed)
./gradlew connectedAndroidTest   # instrumented Compose tests — needs a booted emulator/device
```

**Instrumented tests run on the emulator only — never on a physical phone connected for manual testing.** `connectedAndroidTest`/`connectedDebugAndroidTest` with no device scoping runs on *every* device `adb devices` currently lists, phone included, which install/uninstalls the app mid-suite and can leave a manually-tested phone with the app removed entirely. Before running either task, check `adb devices -l`; if more than one device is listed, prefix the command with `ANDROID_SERIAL=<emulator-serial>` (e.g. `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest`) so it only targets the emulator. Do this every time, even for a quick targeted `-Pandroid.testInstrumentationRunnerArguments.class=...` run — the phone doesn't have to be freshly connected in this session for the risk to apply.

**Never disable device/emulator animation scales (`animator_duration_scale`/`window_animation_scale`/`transition_animation_scale`) as a test-reliability workaround.** Jetpack Compose reads these system-wide settings and scales *every* Compose animation by them — not just in instrumented tests, in the real running app too, so a `0` setting left over from a test session silently breaks the actual app's animations for the next person testing on that device (this happened once — see `IMPLEMENTATION_PLAN.md`'s Post-shape-conformance polish section). Animation lag is expected and correct; write tests that tolerate it instead: `composeRule.waitForIdle()` and any assertion after `performClick()` already correctly wait out Compose's own real-time animation clock in instrumented tests, so most interactions need no special handling. Where a test still needs to wait for something async on top of an animation (a `StateFlow` update, real I/O), poll with `composeRule.waitUntil(timeoutMillis = ...) { ... }` rather than a fixed `Thread.sleep()` or assuming a single `waitForIdle()` caught it — see the many examples of this pattern already in `IMPLEMENTATION_PLAN.md`'s "Real bugs found" sections.

## Build & run

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.lumenlauncher.app/.LauncherActivity
```

Setting Lumen as the actual system default launcher is a device-wide change — leave that to the user to do manually (Settings → Apps → Default apps) rather than doing it from an automated pass.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
