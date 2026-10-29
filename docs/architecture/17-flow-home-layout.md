# 17 — Flow: Home layout (portrait and landscape)

How `HomeScreen` chooses between its two layouts and what each one contains (build history in
`IMPLEMENTATION_PLAN.md`, "Landscape Home"; the structure refactor in `LANDSCAPE_SIMPLIFICATION_PLAN.md`).

## 1. Choosing a layout

`HomeScreen(state, actions, modifier, layoutMode, landscapeSpace)` takes two groups, `HomeScreenState` (list, dock, clock, hosted widget, calendar, media: `HomeScreenState.kt`) and `HomeScreenActions` (the item, clock, widget and media callbacks), instead of the 89 flat parameters it used to have; `HomeDrawerRoute` builds them from the view models. Its `layoutMode: HomeLayoutMode` defaults to `rememberHomeLayoutMode()`. That reads
the window size from `LocalConfiguration` and calls `homeLayoutModeFor(width, height)`: **portrait when
the window is taller than it is wide, whatever its size, or narrower than 600dp; landscape otherwise**.
The rule follows the window, not a rotation flag, so a phone held upright and a tablet held upright both
get portrait, while a phone on its side, an unfolded foldable (the Pixel Fold's inner display is
841 × 700dp) and a tablet on its side get the landscape structure. A window under 600dp wide stays
portrait even when it is wider than tall (a narrow split-screen window). The manifest sets no `screenOrientation`, so a rotation recreates the
activity, and state lives in the ViewModels. Callers and tests can force a mode with the parameter.

### 1.0 Allowing landscape at all

Settings → Appearance → Dock & Apps appearance has a launcher-wide **Allow landscape mode** switch
(`allow_landscape`, default on, global screen only). Off, everything stays portrait:
`rememberHomeLayoutMode()` returns portrait whatever the window (it reads `LocalAllowLandscape`, which
`LauncherActivity` provides from the setting, so Home, the Hub and the route all follow), `LauncherActivity`
requests a portrait orientation (`homeRequestedOrientation`, applied once settings have loaded; Android
ignores it on large screens, where the layout is still forced portrait), and the Landscape alignment card is
hidden. The landscape values already saved are kept and come back when the switch is turned on.

### 1.1 Compact and roomy landscape

`HomeScreen` also takes `landscapeSpace: HomeLandscapeSpace`, defaulting to `rememberHomeLandscapeSpace()`,
which calls `homeLandscapeSpaceFor(height)`: **compact below 480dp tall** (a phone on its side, Material's
compact height class), **roomy** at 480dp or more (a tablet on its side, an unfolded foldable).

| | Compact | Roomy |
|---|---|---|
| Dock | A column at the right edge, icons only | A full-width row along the bottom, in the user's dock style (`home_dock_row`). The divider and the alignment pill stop above it (`HomeLandscapeLayout.onDockRowHeightChange`) |
| Grid columns | The requested count, narrowed so a cell stays at least 64dp | The requested count scaled by the apps pane width over 360dp, never fewer than requested and at most 8 (`landscapeGridColumns`), so tiles stay about phone-sized and there are more of them |

Everything else (the panes, the settings twins, the widget grid) is shared.

## 2. What each layout contains

```
portrait                         landscape
┌──────────────────────┐         ┌───────────┬──────────────────┬────┐
│ clock (offset from   │         │ clock     │ apps pane        │dock│
│ the zone handle)     │         │ pane      │ (list or grid,   │col │
│ media row            │         │ (top /    │  scrolls)        │    │
│ app list             │         │ middle /  │                  │    │
│ dock row             │         │ bottom)   │                  │    │
└──────────────────────┘         └───────────┴──────────────────┴────┘
```

`HomeScreen` is a thin dispatcher: `rememberHomeFrame` (`HomeFrame.kt`) resolves one `HomeFrame` per recomposition (state, actions, measurements, `ClockGeometry`, media layout, reveal alphas, item style), `HomeScreenEffects` clears in-flight drag values and re-clamps the saved scale, and `HomePortraitContent` / `HomeLandscapeContent` (`HomeContent.kt`) arrange the shared slots in `HomeSlots.kt` (`HomeClockSlot`, `HomeMediaSlot`, `HomeAppListRegion`, `HomeDockItems`). The adjust overlays are `ClockAdjustOverlays` and `ClockAdjustPills` (`ClockAdjustControls.kt`, `ClockResizeOverlays.kt`), and the clock maths is pure (`HomeClockGeometry.kt`, tested by `HomeClockGeometryTest`). The app list, its tiles and the dock live in their own files (`HomeAppList.kt`, `AppRow.kt`, `FolderRow.kt`, `DockIcon.kt`); they take two shared groups, `HomeItemActions` (what a tap or long-press does) and `HomeItemStyle` (badges, folder candidates, label look), built once in `HomeScreen`. Portrait is unchanged from before the
split. Landscape is `HomeLandscapeLayout`, a stateless composable that takes the clock, apps and dock as
slots.

| Part | Portrait | Landscape |
|---|---|---|
| Clock | Positioned by absolute offset from the zone handle; `clockAlignment` is left, centre or right | Always left-aligned with a 24dp margin. `clockAlignment` maps LEFT → top, CENTER → middle, RIGHT → bottom of the clock pane |
| Media row | Under the clock, pushes the list down | Pinned to the top of the apps pane, above the list, whatever the list's Top or Bottom anchor; the list gets the height the row leaves |
| App list | Below the clock; Top or Bottom anchor; single column, two columns or grid | In the apps pane; same Top or Bottom anchor; single column, two columns or grid as chosen; a grid's column count follows the compact or roomy rule above (`landscapeGridColumns`); scrolls when taller than the pane |
| Dock | A row along the bottom, honouring the dock display mode | A column at the right edge, icons only, vertically centred, scrolls on overflow, inset from the edge and excluded from the system back gesture |
| Panes | n/a | Clock pane (40% of the window until a width is saved), a 24dp gap holding the divider handle, apps pane the rest |
| Clock adjust mode | A horizontal height handle, an alignment pill (left / center / right) and top corner scale handles | A vertical divider handle, an alignment pill (top / middle / bottom) and scale handles; see section 4 |

## 2a. Portrait's height handle limit

The handle's upper limit (`clockHandleFloorPx`) is not the clock's full-size footprint but the clock at
its smallest scale plus the headroom its resize handles need: dragging the handle up shrinks the clock
live (`effectiveClockScale`), and the handle stops where it reaches the smallest scale. Anything below the
clock box keeps its height, and a hosted clock widget, which can't shrink that way, keeps its whole block.
The live drag value is clamped too, so the handle follows the finger as soon as a drag reverses.

## 3. Portrait-only logic that landscape must not run

The scale re-clamp (`ClockScaleReclampEffect`, using `ClockFit.maxScale`) is portrait geometry, and in
landscape it would persist a wrong `clockScale`, so it is skipped. It also waits until the height handle has room to move (`isClockGeometryMeasured`: ceiling above floor, read from the latest frame): until the content height reaches the geometry the handle sits at its floor and the fit reads as the minimum scale. If the geometry never settles it saves nothing. The compact-spacing check reserves
nothing above the list in landscape (`HomeMediaLayout.listTopTargetPx` is 0).

## 4. Landscape adjust mode

Long-pressing the clock opens the same menu, now with "Adjust clock size & position". The controls:

- **Divider handle.** `ClockZoneHandle(dragOrientation = Horizontal)`: a full-height rail in the gap
  between the panes, dragged left and right. The pane width is saved in landscape's own
  `landscapeClockZoneHeightDp` (falling back to portrait's zone height until set) and is clamped to what fits
  (`landscapeClockPaneWidthPx`: at least the margins plus the smallest clock, at most half the window), so a
  value carried over from portrait is always valid, though it can look big or small.
- **Alignment pill.** `ClockAdjustToolbar(vertical = true)`: top / middle / bottom, writing the same
  `clockAlignment` as left / center / right (see the table above). It sits in the clock pane at the
  bottom edge, or at the top when the clock is bottom-aligned.
- **Scale handles.** On the clock's corners away from the anchor (bottom corners for a top-aligned clock,
  top corners otherwise). The clock scales from its left edge around top, middle or bottom
  (`ClockBlock(clockScaleOriginY)`); `landscapeScaleAtTouch` turns a touch into a scale and
  `landscapeMaxClockScale` limits it to the pane. Dragging one saves landscape's own scale
  (`landscapeClockScale`, falling back to portrait's until set).
- **Narrow pane.** As with portrait's height handle, pulling the divider left shrinks the clock in real
  time. It stops at the smallest clock scale plus the margins (the live drag value is clamped too, so the
  handle never loses the finger), and on release the shrunk scale is saved so it doesn't snap back.
  Landscape also always clamps the *rendered* scale to its pane, adjust mode or not, without saving it:
  a saved scale or width carried over from portrait can look smaller here until it is dragged.

`ClockBlock` measures its clock box at the clock's natural width even when the column is narrower (a
narrow pane), because the resize maths relies on that size; clamping it let the divider go past the clock.

The clock's scale is draw-only, so in portrait it keeps its natural height and the calendar sits below that.
Landscape clamps the scale to the pane, which would leave the calendar far below a clock drawn smaller than
its box, and off the bottom in a short window. So landscape passes `scaledFootprint = true`: the clock box
reports the *drawn* height (`scaledClockHeight`) and shifts up by the empty space above it
(`scaledClockTop`), so the calendar follows the clock. The box inside still measures the natural size, which
the resize maths and `landscapeVisualClockRect` rely on.

The clock pane (`ClockPane` in `HomeLandscapeLayout`) is the whole left pane and scrolls vertically, only
when the clock and calendar are taller than it. Its bounds go to `HomeDrawerRoute`, which leaves vertical
drags there to the pane (see [12](12-flow-drawer-search-and-app-actions.md)), so swiping up opens the Drawer
from the apps side only. The pane swallows what its scroll cannot use, so it never reaches the app list's
leftover-scroll handoff.

A hosted clock widget adjusts too, by real size rather than scale: the corner handles (bottom corners
for a top-aligned widget, top corners otherwise) resize it between one landscape Hub cell (64dp square, not the portrait cell) and the pane
(`landscapeMaxWidgetSize`, `landscapeWidgetSizeAtTouch`), and the divider stops at that one-cell
width, shrinking the widget live and saving that size on release. The rendered size is always clamped to
the pane without being saved. Landscape saves its own size (`facets.landscapeClockWidgetWidthDp`/`HeightDp`) and
uses the portrait one until it has one, so a resize in one orientation never changes the other.
The route resolves everything that differs between orientations once, from the layout mode: `HomeUiState.clockPlacement(mode)` (alignment, divider position, scale; resolved by `resolveFacetStyle`), `listAlignment(mode)` (row position, vertical anchor, two-column alignment) and `clockWidgetSizeDp(mode)`. Landscape's values are one `LandscapeOverrides` group per scope, the launcher's `landscape_*` keys and a facet's `landscape*` columns (see [02](02-persistence-room.md)), written through `updateLandscape`; a `null` field means "same as portrait". One rule resolves every prop (`resolveLandscape`): the facet's landscape value, else the facet's portrait value if it applies (the clock override is on, or the list field isn't `LAUNCHER_DEFAULT`), else the launcher's landscape value, else the launcher's portrait value. The list alignment is edited under "Landscape alignment" in Dock & Apps appearance (with a "Same as portrait" choice), the clock values by adjust mode in landscape. A facet that starts overriding the clock is seeded with the launcher's landscape alignment, so its landscape look doesn't change. The facet carousel and Appearance previews stay portrait-shaped, and the style gallery edits portrait values only; its reset clears both. Portrait clamps the rendered size in read mode too, so a larger size can't spill out of the screen.

The geometry is pure functions: the pane, handle and touch maths in `HomeLandscapeGeometry.kt`, and the per-frame
resolution (handle bounds, fit limits, effective scale and widget size, pane width) in `HomeClockGeometry.kt`
(`computeClockGeometry`).

## 5. Not built yet

The landscape widget grid is built (see [10](10-flow-hub-widgets.md) §5). Not built yet: landscape
facet-preview cards, and gesture thresholds for a short window.
