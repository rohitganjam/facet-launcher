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
| Media row | Under the clock, pushes the list down | Under the clock inside the clock pane |
| App list | Below the clock; Top or Bottom anchor; single column, two columns or grid | In the apps pane; same Top or Bottom anchor; always a single column when two columns is chosen; a grid has its columns narrowed so a cell stays at least 64dp (`landscapeGridColumns`); scrolls when taller than the pane |
| Dock | A row along the bottom, honouring the dock display mode | A column at the right edge, icons only, vertically centred, scrolls on overflow, inset from the edge and excluded from the system back gesture |
| Panes | n/a | Clock pane 40% of the width, a 24dp gap (where the divider handle will sit), apps pane the rest |
| Clock adjust mode | Handles and the alignment pill | Not available yet: the long-press menu has no "Adjust" row, and the alignment pill, zone handle and scale handles are not composed |

## 3. Portrait-only logic that landscape must not run

The scale re-clamp (`maxScaleThatFits`) is portrait geometry, and in landscape it would persist a wrong
`clockScale`, so it is skipped. The compact-spacing check reserves nothing above the list in landscape
(`appListTopTargetPx` is 0). If the window becomes landscape while adjust mode is on, adjust mode is
turned off.

## 4. Not built yet

The draggable divider (the zone handle turned 90°, reusing `clockZoneHeightDp` as the pane width), the
top / middle / bottom alignment pill and landscape scale handles, the landscape widget grid
(`FlowPackWidgetsUseCase` exists but is not wired in), and landscape facet-preview cards.
