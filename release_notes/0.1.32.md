# Facet Launcher 0.1.32

## New Features
- After an update, a small "What's new" pill appears once at the top of the Home screen to point you to the release notes. It shows only if you haven't read that version's notes yet, and it doesn't appear on a fresh install.

## Fixes
- In landscape, the facet settings and delete buttons were cut off. The "Add facet" card was also squeezed down to a thin strip. Both now display properly.
- In the contact connections sheet, dragging back up now scrolls the list instead of closing the app drawer.
- In landscape, the clock menu and the set-default sheets no longer cut off their last row. They now scroll.
- In landscape, the large gap between the clock and the calendar on Home is gone. Rows are no longer cut off in short windows.

## Improvements
- On short screens, such as landscape, facet settings and delete facet are now in a three-dot menu next to the facet name. The "Add facet" card switches to a compact layout when space is tight. Portrait is unchanged.
- On the landscape Home screen, the clock side now scrolls when its content doesn't fit. Swiping up from the apps side still opens the app drawer.

## Internal
- Debug settings: add a row to show the What's new pill again.
- HomeDrawerRouteTest: supply WeatherViewModel and the What's new pill ViewModel so the test runs again.
- KeyboardDismissalTest: supply the weather and What's new pill view models.
- AppContextMenu: split state, pages, overlays and transition into helpers; clears LongMethod and LambdaParameterInRestartableEffect (baseline 61 → 59).
- Detekt baseline regenerated over the combined fixes (77 → 61).
- Detekt: clear ComplexCondition, LoopWithTooManyJumpStatements and two CyclomaticComplexMethod entries (PlaceWidgetUseCase.isFree, HubViewModel.hasSameFrame, cursor loop cleanup, placementActionsFor, queryCustomDataRows/connectionFor); baseline 83 → 77.
- Split flat-list settings screens, the clock style gallery and the home setup page into private composables; suppress LongMethod on Migrations.migrate and FacetNavHost.
- AppDrawerScreen takes parameter groups (item actions, appearance, search bar, search sources): about 65 parameters down to 17; baseline 85 → 83.
- AppDrawerScreen: move list derivations, rail jump math and tab helpers into DrawerBrowseModel, DrawerRailTarget and DrawerTab, with unit tests; baseline 88 → 85.
- Composables take ImmutableList/ImmutableMap/ImmutableSet; UnstableCollections baseline 37 → 2.
- Drop the Compose stability config; List parameters are compared by instance again.
- Detekt: configure rules to fit the codebase, keep UnstableCollections on; baseline 160 → 128.
- Regenerate graphify-out.
