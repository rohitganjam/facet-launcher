# Landscape simplification plan

Tracked from [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) ("Landscape Home"). Tick items off here as they land.
A box is ticked only when its tests are green and, for a phase's last item, the phase is committed.

**Why.** Landscape works and is well tested, but it is plumbed clunkily: eight landscape twins are each stored,
resolved and written separately, and `HomeScreen` has grown into one 986-line function. This plan groups the twins,
resolves them once, and splits `HomeScreen` until its Detekt baseline can go.

## Starting point (measured 2026-10-20)

| | |
|---|---|
| `HomeScreen.kt` | 2,718 lines, 21 functions (limit 11 per file) |
| `HomeScreen` composable | 986 lines (limit 60), cyclomatic complexity 248 (limit 14), 88 parameters |
| Call sites of `HomeScreen(` | 66: 64 in `HomeScreenTest`, 1 in `HomeScreenLandscapeTest`, 1 in `HomeDrawerRoute` |
| Landscape twins | 8 props (clock alignment, divider, scale, widget width and height, row position, column alignment, vertical alignment) across 34 files |
| `isLandscapeHome` branches in `HomeDrawerRoute` | 13 |
| `HomeScreen.kt` entries in `app/detekt-baseline.xml` | 18 (LongMethod ×3, TooManyFunctions, CyclomaticComplexMethod, ComplexCondition, ComposableNestingDepth, ComposableParamOrder ×2, LambdaParameterInRestartableEffect, MatchingDeclarationName, ModifierWithoutDefault, UnstableCollections ×5, UnusedParameter) |

## Ground rules

- Every phase leaves the tree green (unit tests, the instrumented classes below, Detekt) and is committed on its own.
- No behaviour change in phases B to E. Phase A changes behaviour only where it fixes the two named gaps.
- The Detekt baseline is **not** touched until phase E, so other commits keep passing the pre-commit hook.
- The user's uncommitted resize-floor edits (`HubGridConstants.kt`, `ClockWidgetHostController(.Test).kt`, one hunk each in `HomeScreen.kt` and `HomeDrawerRoute.kt`) stay out of every commit here. Best case: they are committed first.
- Instrumented tests run on `emulator-5554` only, one class per invocation, after checking no Gradle run is going.
- Living docs in `docs/architecture/` and `TEST_REGISTRY.md` are updated in the same commit as the code they describe.

## Verification set (run at the end of each phase)

Unit: `HomeLayoutModeTest`, `HomeLandscapeLayoutTest`, `HomeLandscapeGeometryTest`, `HomeUiStateTest`,
`HomeViewModelTest`, `FacetRepositoryTest`, `SettingsRepositoryTest`, `AppearanceSettingsViewModelTest`.
Instrumented: `HomeScreenTest` (60), `HomeScreenLandscapeTest` (30), `ClockBlockTest` (45), `HomeDrawerRouteTest` (50),
`DockAppsAppearanceScreenTest` (14), `FacetDatabaseMigrationTest` (20). Then `./gradlew :app:detekt`.

## Phase A — data layer: group, resolve once, fix the gaps

Behaviour fixes: gap 1 and gap 2 below. Everything else is a refactor.

- [x] **A1. `LandscapeOverrides`.** One class of nullable fields (clock alignment, divider, scale, widget width and height, row position, column alignment, vertical alignment).
  - *Done.* Deviation: the facet keeps its flat columns and exposes the group as `FacetEntity.landscape` / `withLandscape(...)` (an extension property and function, like `selectedCalendarIds`) instead of a Room `@Embedded`. An `@Embedded` group can't keep the existing column order, which would change the schema identity hash and break the already-installed v32 databases. Schema JSON diff is empty. Settings hold `LauncherSettings.landscape`, read from the same six keys.
  - Facet: Room `@Embedded` keeping the existing column names, so the exported schema JSON is identical (`32.json` unchanged) and no migration is needed.
  - Global: `LauncherSettings.landscape: LandscapeOverrides`, read from the same DataStore keys.
  - Tests: schema JSON diff is empty; `FacetRepositoryTest`, `SettingsRepositoryTest` round-trips still pass; migration class green.
- [x] **A2. Resolve once.** `HomeUiState.clockPlacement(mode)` and `listAlignment(mode)` return the resolved values; one consistent precedence rule for clock and list (decide and document it; today they differ).
  - *Done.* One rule for clock and list (`resolveLandscape`): facet landscape, else facet portrait if it applies, else launcher landscape, else launcher portrait. `HomeUiState.clockPlacement(mode)`, `listAlignment(mode)`, `clockWidgetSizeDp(mode)`.
  - Tests: `HomeUiStateTest` covers facet override, no override, null landscape, and each precedence step.
- [x] **A3. One landscape writer per layer.** `updateLandscape { copy(...) }` on `FacetRepository` and `SettingsRepository`; the orientation is `HomeLayoutMode`, not a boolean. Remove the ~14 per-prop landscape writers and the `landscape: Boolean` parameters.
  - *Done.* `updateLandscape` on both repositories; 4 landscape writers removed and the `landscape: Boolean` parameters gone from 3 settings and 3 facet writers. The view-model commits take a `HomeLayoutMode`.
  - Tests: repository and view-model tests updated; `HomeViewModelTest` commit routing.
- [x] **A4. Route uses A2.** `HomeDrawerRoute` makes two calls instead of 13 `isLandscapeHome` branches, and passes `layoutMode` and `landscapeSpace` down once (no second computation in `HomeScreen`'s defaults).
- [x] **A5. One alignment card.** Portrait and Landscape cards in `DockAppsAppearanceScreen` share one composable keyed by orientation.
  - Tests: `DockAppsAppearanceScreenTest` unchanged and green.
- [x] **A6. Gap 1: seed landscape on override.** `ClockStyleGalleryViewModel.setOverridingClock` / `FacetRepository.updateOverridingClock` seed the facet's landscape overrides from the effective landscape values, so a facet starting to override doesn't change its landscape look.
  - Tests: new unit test: global landscape alignment set, facet starts overriding, landscape alignment unchanged.
- [x] **A7. Gap 2: backup.** Add the landscape overrides to `BackupSettings` and `BackupFacet` as optional, defaulted fields (no `CURRENT_BACKUP_VERSION` bump), with mapping both ways. Decide and document `allow_landscape` (currently device-local, not backed up).
  - *Done.* Only what portrait also backs up: the row position and two-column alignment twins, optional and additive, in `BackupSettings` and `BackupFacet`. `allow_landscape` stays device-local.
  - Tests: export/import round-trip test; an old backup without the fields still imports.
- [x] **A8. Docs and commit.** `02` (columns, keys, writers, counts), `09` (backup), `13`, `17`, test registry. One commit, or two (refactor, then the two fixes) if the diff is large.

## Phase B — mechanical file split of `HomeScreen.kt`

No logic change. Keep the baseline.

- [x] **B1. `ClockAdjustMode.kt`.** Move the enum (fixes MatchingDeclarationName).
- [x] **B2. `HomeAppList.kt`.** `HomeAppTwoColumnList`, `HomeAppListColumn`, `HomeAppGrid`, `HomeAppGridTile`, `HomeFolderGridTile`, their constants. At most 10 functions.
  - *Done.* Also `dashedBorder` (shared with `UsageAccessStrip`, now `internal`). The single-column layout now uses `HomeAppListColumn` too (new `rowWidthFraction` and `horizontalAlignment` parameters), so `HomeScreen` no longer carries its own copy of the row loop.
- [x] **B3. `AppRow.kt`, `FolderRow.kt` (with the folder glyph), `DockIcon.kt`.** Shared `CenteredWithBadge` goes to `ui/components`.
- [x] **B4. Fix what Detekt flags in the moved code.** Decompose `AppRow` (88 lines) and `FolderRow` (68); parameter order; modifier defaults on `SingleAppDockIcon` and `FolderDockIcon`.
  - *Done, further than planned.* The 18 or so parameters every tile repeated are now two `@Immutable` groups in `HomeItemSpec.kt`: `HomeItemActions` (callbacks) and `HomeItemStyle` (badges, folder candidates, label look), plus `HomeAppListContent` for a list and its style, and shared `AppItemMenu` / `FolderItemSheet` / `FolderItemMenu` / `appRowArrangement`. That fixed param order, length and the unstable collections in the moved code with no baseline entries. `AppRow` and `FolderRow` are split into a row plus a content composable. These groups are the start of phase C's design (see C1).
- [x] **B5. Baseline.** Remove only the `HomeScreen.kt` entries that no longer apply (the moved functions' LongMethod, ParamOrder, ModifierWithoutDefault, MatchingDeclarationName, part of TooManyFunctions); unstable-collection entries wait for phase C.
  - *Done.* Removed the seven `HomeScreen.kt` entries that no longer apply (ParamOrder x2, LongMethod for `AppRow` and `FolderRow`, MatchingDeclarationName, TooManyFunctions, ModifierWithoutDefault). 11 remain, all on the `HomeScreen` composable itself, for phases C and D. No entries were added for the new files.
  - Verify: verification set; fold screenshots of portrait and landscape identical before and after.

## Phase C — group `HomeScreen`'s state (changes its signature)

- [x] **C1. Design the groups.** (Started in phase B: `HomeItemActions`, `HomeItemStyle` and `HomeAppListContent` exist and are used below `HomeScreen`; `HomeScreen` still builds them from its flat parameters, so phase C passes them in directly.)
  - *Done.* Built as designed in `HomeScreenState.kt`, with two changes: the clock style fields sit directly in `HomeClockState` (no separate style class), and the items' label look is `HomeItemLook` (options, resolved to a `HomeItemStyle` where drawn, because resolving a themed colour needs composition). Every group is a `data class` so an unchanged group lets its composables skip.
  - **Design (89 flat parameters today, of which tests use 41).** `HomeScreen(state: HomeScreenState, actions: HomeScreenActions, modifier, layoutMode, landscapeSpace)`. All state classes are `@Immutable`, built once per change by `HomeUiState.toScreenState(mode)`.

    | Group | Fields |
    |---|---|
    | `HomeClockState` | style (`clockTemplateId`, font, colour, accent, date style, `use24HourTime`, `clockShowMeridiem`, `launcherFontOption`), `placement: ClockPlacement` (exists), `positionOwnerFacetId` / `Name`, `adjustMode`, `draggingHandle` |
    | `HomeClockWidgetState?` | `appWidgetId`, `widthDp`, `heightDp`, `minWidthDp`, `minHeightDp`, `isPro` (null: native clock; `hasCustomClockWidget` is derived) |
    | `HomeCalendarState` | `events`, `colors`, `display`, `nextAlarmMillis`, `batteryPercent`, `isCharging` |
    | `HomeListState` | `content: HomeAppListContent` (exists), `layout`, `alignment: ListAlignment` (exists), `rowPresentation`, `gridColumns`, `gridDisplayMode`, `showUsageAccessPrompt` |
    | `HomeDockState` | `items`, `displayMode` |
    | `HomeMediaState` | `player: MediaPlayerState?`, `showSkipButtons` |
    | `HomeScreenState` | the six above, plus the label look (`appLabelColorOption`, `homeAppsFontWeight`) |

    | Actions group | Callbacks |
    |---|---|
    | `HomeItemActions` (exists) | the twelve tile callbacks |
    | `HomeClockActions` | `onClockClick`, `onEditClockStyles`, `onNavigateToSettings`, `onNavigateToFacetSettings`, `onUseCustomWidgetClick`, `onSwitchToLauncherClockClick`, `onEventClick`, `onZoneHeightCommit`, `onScaleCommit`, `onAlignmentChange`, `onAdjustModeChange`, `onDraggingHandleChange` |
    | `HomeClockWidgetActions` | `createHostView`, `onSizeChange`, `onSizeCommit`, `onBoundsChange`, nested-scroll start / leftover scroll / stop / leftover fling |
    | `HomeMediaActions` | `onPrevious`, `onPlayPause`, `onNext`, `onOpen` |
    | `HomeScreenActions` | the four above, plus `onUsageAccessPromptClick` and `onAppListBoundsChange` |

  - **Tests.** `HomeScreenTest` / `HomeScreenLandscapeTest` call a test-only `TestHomeScreen(...)` helper with the 41 flat parameters they use (Detekt only checks `src/main`), which builds the groups and calls the real `HomeScreen`. The rewrite of the 66 call sites is then a rename.
- [x] **C2. `HomeScreen(state, actions, modifier, layoutMode, landscapeSpace)`.** Update the body to read from the groups; behaviour unchanged.
  - *Done.* The body reads the groups through short local aliases with the old parameter names (86 `val`s at the top of `HomeScreen`); phase D removes them as each part moves into its own composable.
- [x] **C3. `HomeUiState.toScreenState(mode)`** builds the groups; `HomeDrawerRoute` shrinks accordingly.
  - *Done differently.* The route builds `HomeScreenState` and `HomeScreenActions` inline: much of the data comes from route-local state (adjust mode, drag state, nested-scroll handoff, intents), so a `HomeUiState` method would not have had it. The route's call now reads as structured groups rather than 89 flat arguments.
- [x] **C4. Test fixtures.** A `homeState(...)` / `homeActions(...)` builder with the old defaults; rewrite the 66 call sites mechanically (script the rewrite, review the diff).
  - *Done.* Also a new `HomeScreenWiringTest` (11 tests): 49 of the 88 old parameters were never set by any `HomeScreen` test (they are tested one level down), so it sets each untested group to distinct values and checks they arrive (favorites vs dock, app vs folder callbacks, media buttons, clock menu rows, hosted-widget hooks, badge style, battery). A mutation check (swapping favorites and dock in `AppItemMenu`) fails it as intended. `FlatHomeScreen` (androidTest) keeps the 41 flat parameters tests use and builds the groups; the 45 direct `HomeScreen(` calls were renamed to it. `HomeScreenTest`'s own `TestHomeScreen` wrapper is unchanged.
- [x] **C5. Unstable collections.** The remaining `UnstableCollections` baseline entries go away because the lists now sit inside `@Immutable` groups. Check recomposition counts in the clock and list paths haven't regressed.
  - *Done.* Removed 7 more baseline entries (5 UnstableCollections, UnusedParameter, LambdaParameterInRestartableEffect). 4 remain, all on the `HomeScreen` body (LongMethod, CyclomaticComplexMethod, ComplexCondition, ComposableNestingDepth), for phases D and E.
  - Verify: verification set; fold install.

## Phase D — decompose the `HomeScreen` body

Targets: each function ≤ 60 lines, complexity ≤ 14, no nesting or condition violations, ≤ 10 functions per file.

- [ ] **D1. Pure state functions.** Effective clock scale, effective widget size, landscape pane width, max widget size, dock reserve become pure, unit-tested functions (some already live in `HomeLandscapeGeometry.kt`).
  - Tests: unit tests for each, including the boundary cases from the landscape bugs found so far (unmeasured pane, minimum floor, stale block bottom).
- [ ] **D2. `LandscapeHomeState`** holder for the measured values (pane width, dock row height, clock box, root size) so `HomeScreen` stops owning ~30 landscape locals.
- [ ] **D3. Layouts.** `HomePortraitLayout` and `HomeLandscapeContent` as their own composables.
- [ ] **D4. Adjust overlays.** Portrait adjust, landscape native-clock adjust, landscape widget adjust, and the two pills as separate composables (~300 lines out of `HomeScreen`).
- [ ] **D5. `HomeScreen` body is a thin dispatcher** (state holders, layout choice, overlays).
  - Verify: verification set; fold install; adjust-mode gestures checked on the fold in both orientations.

## Phase E — remove the baseline

- [ ] **E1.** Delete the remaining `HomeScreen.kt` entries from `app/detekt-baseline.xml`.
- [ ] **E2.** `./gradlew :app:detekt` passes with nothing re-baselined for `HomeScreen*` or the new files.
- [ ] **E3.** Update `docs/architecture/04` (package tree and counts), `07` (registry), `17`, `TEST_REGISTRY.md`; tick this file; update the `IMPLEMENTATION_PLAN.md` entry.

## Risks and watch-points

- **Phase C changes `HomeScreen`'s public API** and overlaps the user's uncommitted `HomeScreen.kt` / `HomeDrawerRoute.kt` hunks. Commit those first if possible; otherwise stage by hunk as before.
- **Recomposition.** Coarse groups can recompose more than 88 flat parameters did. Keep clock and list in separate groups, and compare before and after.
- **Room `@Embedded`** must produce identical column names and nullability. The schema JSON diff is the check.
- **Pre-commit hook** runs Detekt on staged Kotlin; a mid-phase commit with a half-removed baseline fails. Only edit the baseline in B5, C5 and E1, each in the commit that fixes the code.
- **Phase D is the longest.** Expect 20 to 30 small composables; stop between sub-steps if a test turns red rather than pushing on.

## Decisions log

| Date | Decision |
|---|---|
| 2026-10-20 | Detekt baseline restored for now; removal is phase E. |
| 2026-10-20 | Plan kept in this file; `IMPLEMENTATION_PLAN.md` links here. |
| (open) | Order: phase A first (default) or the `HomeScreen` split (B to D) first. |
| 2026-10-20 | Precedence (A2): one rule for every prop, facet landscape, then facet portrait if it applies, then launcher landscape, then launcher portrait. |
| 2026-10-20 | `allow_landscape` stays device-local and out of the backup (A7); the landscape clock placement isn't backed up because the portrait one isn't. |
| 2026-10-20 | Facet landscape twins stay flat columns behind an accessor, not `@Embedded` (A1), to keep the schema identity hash. |
