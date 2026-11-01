# Facet Launcher 0.1.34

## New Features
- Clock stack: each facet's clock area can now hold several pages, such as the clock and widgets. Swipe sideways to move between them. Page dots appear during and just after a swipe, including in landscape and while adjusting.
- A new Widget stack screen lists the facet's pages. You can reorder, configure, remove and add pages there. Free users get one page and Pro users get up to five.
- Each facet remembers the page it last showed.
- A page whose app was uninstalled now shows a placeholder with a Remove button.

## Fixes
- Responsive widgets on Home now get their real size, so they no longer stay stuck on their smallest layout.
- Swiping between clock pages no longer flickers, resizes, jitters or re-loads a widget as the next page arrives.
- Horizontal drags on a widget that doesn't scroll now pass through to Home, so you can still swipe to other screens.
- A neighboring widget page no longer briefly shows a clock while you swipe to it.
- In landscape, taller or differently aligned pages are no longer clipped or shifted when they land.
- A swiped widget page now slides instead of squeezing.

## Improvements
- The clock's alignment, size and divider are now saved separately for each facet. They no longer change when you edit the clock override or launcher-wide settings. A facet you haven't adjusted shows the default layout, and your first adjustment saves it to that facet.
- "Reset clock position" now clears only the clock's placement.
- The Alignment row is removed from the Clock style gallery.
- The clock menu now shows options that match the visible page. These include clock and calendar settings, "Replace with custom widget" and "Replace with clock".
- Replacing a page no longer requires Pro.
- Hosted widgets now use tighter padding.
- "Edit defaults" on the inherit card now links to the launcher-wide screen.
- New and restored facets start with the native clock as their first page.
- New strings are translated into all five languages.

## Internal
- Added the `clock_stack_items` table and `ClockStackRepository`. Migration 34→35 creates a first page for every facet by moving existing hosted widgets into a one-item stack.
- `Facet` now carries `clockStack`. The five single-widget fields are removed from `Facet` and the landscape group, and the columns are retired. Home, the resize commit, facet deletion and the widget picker read and write the stack and release every widget id involved.
- Added `ReplaceClockStackItemUseCase`, which swaps a page in place and releases the old widget id. The picker now takes an Add or Replace target. The Pro upsell reason is now `WIDGET_STACK`.
- Migration 35→36 adds the placement-ownership flag, derived from `overrideClock`.
- Pager rework: neighbouring pages stay composed and parked. `HomeWidgetSizing` is answered per widget id. Page room comes from `ClockGeometry.widgetRoomPx`. Pages shift relative to `ClockPagerState.anchorIndex`. A hosted view is only told its size when it changes, and the clock slot is placed from the height measured in the same pass.
- `ClockWidgetTile` now reads the host view from the tile instead of a local that was null after the first recomposition.
- Tests added for the pager anchor, per-widget neighbour sizing, widget room, Home wiring, `ClockWidgetPickerViewModel` add and replace flows, and the stack screen Add flow.
- Fixed the `clockScaleIsClampedToItsBounds` test to drag one move per frame.
- Fixed a preview fixture, and no facet now resolves to the launcher clock.
- Docs: refreshed docs 10 and 12, the test registry, counts and registries, Home state inputs, adjust sheet rows and placement ownership. Added plan docs for the widget stack.
- Regenerated graphify-out.
- Release notes scripts now show why `claude -p` failed, using stdout when stderr is empty.
- Release 0.1.33: version bump, What's new, build commit recorded.
