# 17 — Flow: Home layout (portrait and landscape)

How `HomeScreen` chooses between its two layouts and what each one contains. Landscape is being built
in stages (see `IMPLEMENTATION_PLAN.md`, "Landscape Home"); this document describes what exists now and
marks what does not yet.

## 1. Choosing a layout

`HomeScreen` takes `layoutMode: HomeLayoutMode`, defaulting to `rememberHomeLayoutMode()`. That reads
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

`HomeScreen` defines the content once, as local composable lambdas (`clockBlock`, `mediaRow`,
`appListRegion`, `dockIcon`), and each layout arranges them. The app list, its tiles and the dock live in their own files (`HomeAppList.kt`, `AppRow.kt`, `FolderRow.kt`, `DockIcon.kt`); they take two shared groups, `HomeItemActions` (what a tap or long-press does) and `HomeItemStyle` (badges, folder candidates, label look), built once in `HomeScreen`. Portrait is unchanged from before the
split. Landscape is `HomeLandscapeLayout`, a stateless composable that takes the clock, apps and dock as
slots.

| Part | Portrait | Landscape |
|---|---|---|
| Clock | Positioned by absolute offset from the zone handle; `clockAlignment` is left, centre or right | Always left-aligned with a 24dp margin. `clockAlignment` maps LEFT → top, CENTER → middle, RIGHT → bottom of the clock pane |
| Media row | Under the clock, pushes the list down | Pinned to the top of the apps pane, above the list, whatever the list's Top or Bottom anchor; the list gets the height the row leaves |
| App list | Below the clock; Top or Bottom anchor; single column, two columns or grid | In the apps pane; same Top or Bottom anchor; single column, two columns or grid as chosen; a grid has its columns narrowed so a cell stays at least 64dp (`landscapeGridColumns`); scrolls when taller than the pane |
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

The scale re-clamp (`maxScaleThatFits`) is portrait geometry, and in landscape it would persist a wrong
`clockScale`, so it is skipped. The compact-spacing check reserves nothing above the list in landscape
(`appListTopTargetPx` is 0).

## 4. Landscape adjust mode

Long-pressing the clock opens the same menu, now with "Adjust clock size & position". The controls:

- **Divider handle.** `ClockZoneHandle(dragOrientation = Horizontal)`: a full-height rail in the gap
  between the panes, dragged left and right. The pane width is saved in `clockZoneHeightDp`, the field
  portrait uses for its zone height, so one value serves both orientations and is clamped to what fits
  (`landscapeClockPaneWidthPx`: at least the margins plus the smallest clock, at most half the window). A
  value from the other orientation is therefore always valid, though it can look big or small after a
  rotation.
- **Alignment pill.** `ClockAdjustToolbar(vertical = true)`: top / middle / bottom, writing the same
  `clockAlignment` as left / center / right (see the table above). It sits in the clock pane at the
  bottom edge, or at the top when the clock is bottom-aligned.
- **Scale handles.** On the clock's corners away from the anchor (bottom corners for a top-aligned clock,
  top corners otherwise). The clock scales from its left edge around top, middle or bottom
  (`ClockBlock(clockScaleOriginY)`); `landscapeScaleAtTouch` turns a touch into a scale and
  `landscapeMaxClockScale` limits it to the pane. Dragging one saves `clockScale`, which both
  orientations share.
- **Narrow pane.** As with portrait's height handle, pulling the divider left shrinks the clock in real
  time. It stops at the smallest clock scale plus the margins (the live drag value is clamped too, so the
  handle never loses the finger), and on release the shrunk scale is saved so it doesn't snap back.
  Landscape also always clamps the *rendered* scale to its pane, adjust mode or not, without saving it:
  a saved scale or width carried over from portrait can look smaller here until it is dragged.

`ClockBlock` measures its clock box at the clock's natural width even when the column is narrower (a
narrow pane), because the resize maths relies on that size; clamping it let the divider go past the clock.

A hosted clock widget adjusts too, by real size rather than scale: the corner handles (bottom corners
for a top-aligned widget, top corners otherwise) resize it between one landscape Hub cell (64dp square, not the portrait cell) and the pane
(`landscapeMaxWidgetSize`, `landscapeWidgetSizeAtTouch`), and the divider stops at that one-cell
width, shrinking the widget live and saving that size on release. The rendered size is always clamped to
the pane without being saved. Landscape saves its own size (`facets.landscapeClockWidgetWidthDp`/`HeightDp`) and
uses the portrait one until it has one, so a resize in one orientation never changes the other.
The route resolves everything that differs between orientations once, from the layout mode: `HomeUiState.clockPlacement(mode)` (alignment, divider position, scale), `listAlignment(mode)` (row position, vertical anchor, two-column alignment) and `clockWidgetSizeDp(mode)`. Landscape's values are one `LandscapeOverrides` group per scope, the launcher's `landscape_*` keys and a facet's `landscape*` columns (see [02](02-persistence-room.md)), written through `updateLandscape`; a `null` field means "same as portrait". One rule resolves every prop (`resolveLandscape`): the facet's landscape value, else the facet's portrait value if it applies (the clock override is on, or the list field isn't `LAUNCHER_DEFAULT`), else the launcher's landscape value, else the launcher's portrait value. The list alignment is edited under "Landscape alignment" in Dock & Apps appearance (with a "Same as portrait" choice), the clock values by adjust mode in landscape. A facet that starts overriding the clock is seeded with the launcher's landscape alignment, so its landscape look doesn't change. The facet carousel and Appearance previews stay portrait-shaped, and the style gallery edits portrait values only; its reset clears both. Portrait clamps the rendered size in read mode too, so a larger size can't spill out of the screen.

The geometry is pure functions in `HomeLandscapeGeometry.kt`.

## 5. Not built yet

The landscape widget grid is built (see [10](10-flow-hub-widgets.md) §5). Not built yet: landscape
facet-preview cards, and gesture thresholds for a short window.
