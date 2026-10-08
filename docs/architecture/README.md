# Facet Launcher — Living Blueprint

An architecture reference derived from the code as it stands on `feature/work-profile-support`
(app `0.1.5`, Room schema v23, 244 Kotlin files under `app/src/main`). Every claim below was
checked against source, not against planning docs; where the code diverges from
`CLAUDE.md`'s stated conventions, [05-findings.md](05-findings.md) says so.

| Doc | What it covers |
|---|---|
| [01-architecture-and-layers.md](01-architecture-and-layers.md) | Layer boundaries, unidirectional data flow, Hilt components/scopes/modules, entry points |
| [02-persistence-room.md](02-persistence-room.md) | **Everything persisted**: storage map, Room ER diagram (13 tables), DAO method matrix, DAO→Repository→Flow, converters, migrations v10→v23, the complete DataStore key registry (47 keys, read/write paths, all 49 writers), backup file format |
| [03-reactive-data-flow.md](03-reactive-data-flow.md) | The four core reactive flows: live installed-app list, placement hydration, Home state graph, startup |
| [04-package-structure.md](04-package-structure.md) | Package map with the "where does this file go" rules and the test-tree mirror |
| [05-findings.md](05-findings.md) | Architect's review: 12 findings ranked by severity with concrete fixes, plus what's working well |
| [06-testing.md](06-testing.md) | How tests are done: tiers, harnesses, fixtures, conventions, run commands, coverage gaps |
| [TEST_REGISTRY.md](TEST_REGISTRY.md) | **Generated** — every test class and case (137 classes / 1104 cases), regenerate with `scripts/gen-test-registry.py` |
| [07-registries.md](07-registries.md) | Complete inventories: 41 repositories (deps, source wrapped, API, consumers), 43 use cases, 30 ViewModels |
| [08-flow-placements.md](08-flow-placements.md) | Flow: favorites/dock/folder add, remove, reorder; override routing; uninstall cleanup, profile sweep, orphan repair, dock seeding |
| [09-flow-backup-restore.md](09-flow-backup-restore.md) | Flow: export, destructive import, widget re-bind state machine, versioning |
| [10-flow-hub-widgets.md](10-flow-hub-widgets.md) | Flow: widget host lifecycle, add (bind/configure/place), move/resize/compact, delete, orphans |
| [11-flow-profiles-and-spaces.md](11-flow-profiles-and-spaces.md) | Flow: profile classification, Work Profile, Private Space, Secure Folder, tear-down |
| [12-flow-drawer-search-and-app-actions.md](12-flow-drawer-search-and-app-actions.md) | Flow: drawer data-in, search pipeline (apps/contacts/settings), per-app actions, Home gesture routing |
| [13-flow-facets-theme-notifications-onboarding.md](13-flow-facets-theme-notifications-onboarding.md) | Flows: facet switch/preview/override, theme resolution, notification badges, onboarding |
| [14-flow-deep-links-and-shortcuts.md](14-flow-deep-links-and-shortcuts.md) | Flow: facet deep links (`facetlauncher://facet/{id}`), dynamic-shortcut sync, external-trigger ingestion |
| [15-flow-facet-automation.md](15-flow-facet-automation.md) | Framework: rules that switch the active facet — concepts, baseline/overlay state model, the pure evaluator, manual-wins enforcement, planned triggers and runner, invariants. Mixes built and planned (labelled) |
| [16-flow-billing.md](16-flow-billing.md) | Flow: the one-time `facet_pro` Google Play purchase, the cached entitlement and its offline rules, Buy/Restore, what a lapse does, the debug override, and why there is still no `INTERNET` |

## Stack at a glance

| Concern | Choice (verified in `app/build.gradle.kts`) |
|---|---|
| Language / UI | Kotlin only, Jetpack Compose (BOM `2026.01.01`, Material3), no XML layouts |
| Module layout | `:app` (`minSdk 31`, `targetSdk/compileSdk 36`), plus `:detekt-rules` (custom Detekt rules) and `:benchmark` (Macrobenchmark tests against `:app`'s `benchmark` build type) |
| DI | Hilt `2.60.1` via KSP — `@HiltAndroidApp` / `@AndroidEntryPoint` / `@HiltViewModel` |
| Navigation | `navigation-compose 2.9.6` + `hilt-navigation-compose` — one `NavHost` (`FacetNavHost`) |
| Persistence | Room `2.8.4` (`facet.db`, v20, schema export on) + `datastore-preferences 1.2.1` (`facet_settings`) |
| Async | Coroutines + `Flow`/`StateFlow` only; `collectAsStateWithLifecycle()` in Compose; no `LiveData` |
| Serialization | `kotlinx-serialization-json` (backup bundle only) |
| Networking | **None** — no Retrofit/OkHttp dependency, no `INTERNET` permission |
| Tests | JUnit4 + Robolectric `4.16.1` + `kotlinx-coroutines-test` (JVM); Compose UI tests + `room-testing` + Orchestrator (instrumented) |

## Keeping this blueprint alive

The change→doc routing table lives in `CLAUDE.md` ("Living architecture docs"). Short version:

- Add a Room entity / column / DAO method → `02` (ER diagram, DAO matrix, migration table).
- Add a DataStore key → `02 §5` (diagram, writer table, section table) — the `§5.6` checklist.
- Add a Hilt module or change a scope → `01`; add a repository / use case / ViewModel → `07` + `01` counts.
- Add or change a user-visible flow → the matching `08`–`13` doc, or a new `14-flow-….md` + a row above.
- Add/rename/remove a test → `python3 scripts/gen-test-registry.py`; change how tests are done → `06`.
- Resolve a finding in `05` → delete it rather than marking it done.
