# 17 — Flow: Home layout (portrait and landscape)

How `HomeScreen` chooses between its two layouts and what each one contains. Landscape is being built
in stages (see `IMPLEMENTATION_PLAN.md`, "Landscape Home"); this document describes what exists now and
marks what does not yet.

## 1. Choosing a layout

`HomeScreen` takes `layoutMode: HomeLayoutMode`, defaulting to `rememberHomeLayoutMode()`. That reads
the window size from `LocalConfiguration` and calls `homeLayoutModeFor(width, height)`: **landscape when
the window is wider than it is tall**, portrait otherwise. The rule follows the window's shape, not a
rotation flag, so an unfolded foldable (the Pixel Fold's inner display is 841 × 700dp) gets landscape and
a tablet held upright does not. The manifest sets no `screenOrientation`, so a rotation recreates the
activity, and state lives in the ViewModels. Callers and tests can force a mode with the parameter.

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
`appListRegion`, `dockIcon`), and each layout arranges them. Portrait is unchanged from before the
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

The geometry is pure functions in `HomeLandscapeGeometry.kt`. A hosted clock widget has no landscape
adjust yet: the menu hides the Adjust row for it, and adjust mode ends if a widget gets bound.

## 5. Not built yet

The landscape widget grid (`FlowPackWidgetsUseCase` exists but is not wired in), landscape
facet-preview cards, and gesture thresholds for a short window.
