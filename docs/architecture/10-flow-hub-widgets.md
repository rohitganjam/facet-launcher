# 10 — Flow: Hub Widgets (host lifecycle, add, move, resize, delete, orphans)

The Hub is Facet's widget surface: a 5-column grid (`HUB_COLUMNS = 5`, `HUB_MAX_ROWS = 50`,
`HUB_MAX_WIDGETS = 20` in `domain/HubGridConstants.kt`) hosting real `AppWidgetHostView`s.
Two stores cooperate: the **system** `AppWidgetService` owns bindings (which `appWidgetId` maps to
which provider), Facet's Room `widget_placements` owns *where* each id sits.

## 1. Who owns what

```mermaid
flowchart LR
    subgraph SYS["Android AppWidgetService"]
        H["host id 1024\nappWidgetId → provider binding\n(survives Facet data clear)"]
    end
    subgraph APP["Facet"]
        LH["LauncherAppWidgetHost : AppWidgetHost\nproviderChanges: SharedFlow&lt;Int&gt;"]
        AWR["AppWidgetRepository (@Singleton)\nallocate / bind / configure / createHostView\nupdateWidgetSize / delete / start&stopListening"]
        WPR["WidgetPlacementRepository\nwidget_placements (appWidgetId PK, row, col, colSpan, rowSpan, profile, userId)"]
        UC["ObserveHubStateUseCase\ncombine(placements, merge(refreshTrigger, providerChanges))\n→ HubWidgetState(isOrphaned = getAppWidgetInfo(id) == null)"]
        VM["HubViewModel"]
        SCR["HubScreen / HubGrid / HubWidgetTile"]
    end
    H <--> LH
    LH --> AWR
    AWR --> UC
    WPR --> UC
    UC --> VM --> SCR
    SCR -- "createHostView(context, id)" --> VM --> AWR
```

- **Host listening** is tied to visibility, not process: `HomeDrawerRoute` calls
  `hubViewModel.onHubVisible()` → `host.startListening()` when the Hub opens and
  `onHubHidden()` → `stopListening()` in `onDispose`. Widgets don't receive updates while the Hub
  is closed.
- **Orphans**: a placement whose `appWidgetId` no longer has provider info (provider uninstalled,
  Work Profile removed, or bindings lost) is rendered as `OrphanedWidgetTile` with
  *Remove* (`DeleteWidgetUseCase`) / *Keep space* (`onKeepOrphanSpace` — no-op, the row stays).
- Providers are enumerated per profile
  (`userManager.userProfiles.flatMap { getInstalledProvidersForProfile(it) }`), so Work Profile
  widgets appear in the picker tagged with their `AppProfile`; `widget_placements.profile/userId`
  record which profile a placed widget came from.

## 2. Add a widget (`HubWidgetPickerViewModel`)

```mermaid
sequenceDiagram
    autonumber
    participant P as HubWidgetPickerScreen
    participant VM as HubWidgetPickerViewModel
    participant AWR as AppWidgetRepository
    participant SYS as AppWidgetManager / Host
    participant UC as PlaceWidgetUseCase
    participant WPR as WidgetPlacementRepository
    P->>VM: onProviderSelected(option)
    VM->>WPR: observeAll().first()  (existing.size ≥ HUB_MAX_WIDGETS → AddFailed(HUB_FULL))
    VM->>AWR: allocateAppWidgetId()
    AWR->>SYS: host.allocateAppWidgetId()
    VM->>AWR: bindAppWidgetIdIfAllowed(id, provider, userHandle)
    alt bind allowed (Facet is default launcher / previously granted)
        AWR-->>VM: true
    else needs user consent
        AWR-->>VM: false
        VM-->>P: HubAddWidgetEvent.LaunchBindPermission(createBindIntent(...))
        P->>VM: onBindResult(granted)   (false → failAndRelease)
    end
    VM->>VM: proceedAfterBind(id)
    VM->>AWR: createConfigureIntentSender(id, info)
    alt provider has a configure activity
        VM-->>P: HubAddWidgetEvent.LaunchConfigure(intentSender)
        P->>VM: onConfigureResult(ok)   (false → failAndRelease)
    end
    VM->>VM: finishPlacing(id, providerPackage, providerClass)
    VM->>AWR: defaultSpanFor(info) → (cols, rows) from targetCellWidth/Height or minWidth/Height, coerced to grid
    VM->>UC: PlaceWidgetUseCase(existing, colSpan, rowSpan) → Placed(row, col) | HubFull
    VM->>WPR: upsert(WidgetPlacementEntity(id, provider, row, col, colSpan, rowSpan, profile, userId))
    VM-->>P: HubAddWidgetEvent.WidgetAdded
    Note over VM,AWR: any failure path → failAndRelease: deleteAppWidgetId(id) + AddFailed(reason)
```

`PlaceWidgetUseCase` is pure: it scans rows top-down, columns left-right for the first
rectangle that doesn't overlap an existing placement; with `preferredRow/Col` (backup restore) it
tries that slot first.

## 3. Move, resize, delete (`HubViewModel`)

```mermaid
flowchart TB
    G["HubGrid gesture\n(WidgetGrabGesture / WidgetResizeHandle)"]
    G -- "onWidgetDropped(id, row, col, colSpan, rowSpan)" --> RD["ResolveWidgetDropUseCase\nmove target; push each overlapped widget\nright-then-down; null if anything can't fit"]
    G -- "onWidgetResized(id, row, col, colSpan, rowSpan)" --> RR["ResolveWidgetResizeUseCase\nresize target; cascade-push overlapped widgets\nstraight down; null if past HUB_MAX_ROWS"]
    G -- "onWidgetDroppedOnTrash(id)" --> DEL["DeleteWidgetUseCase\nwidgetPlacementRepository.deleteById(id)\nappWidgetRepository.deleteAppWidgetId(id)"]
    RD --> M["mergeResolved(current, resolved)"]
    RR --> M
    M --> C["CompactWidgetsUseCase\nevery widget floats up until it rests on another"]
    C --> COMMIT["commitPlacements → upsert × n"]
    DEL --> C2["CompactWidgetsUseCase(remaining) → commitEntities"]
    COMMIT --> ROOM[(widget_placements)]
    C2 --> ROOM
    ROOM --> UC["ObserveHubStateUseCase re-emits"]
    TILE["HubWidgetTile onSizeChanged"] -- "updateWidgetSize(id, wDp, hDp)" --> OPT["AppWidgetManager.updateAppWidgetOptions\nOPTION_APPWIDGET_MIN/MAX_WIDTH/HEIGHT"]
```

- A drop or resize that can't be resolved (`null` from the use case) is silently rejected — the
  tile snaps back; nothing is written.
- Every successful mutation ends with `CompactWidgetsUseCase` so the grid never keeps a gap
  above a widget.
- `updateWidgetSize` tells the provider its real rendered size so responsive widgets
  (`targetCellWidth` aware) lay out correctly.

## 4. Persistence & recovery matrix

| Event | System binding | `widget_placements` row | What the user sees |
|---|---|---|---|
| Add completes | created | created | tile |
| Add fails at bind/configure/placement | released (`deleteAppWidgetId`) | none | `AddFailed(reason)` snackbar |
| Delete (trash / orphan Remove) | released | deleted | gone |
| Provider app uninstalled | binding invalid | row kept | `OrphanedWidgetTile` (Remove / Keep space) |
| Work Profile removed | bindings gone | rows kept (`userId` still set) | orphan tiles |
| Facet data cleared / reinstalled | **kept** (host id 1024 is stable) | gone | nothing — the bindings leak until the system GCs them |
| Backup restore | new ids allocated per widget | re-created via the picker state machine | interactive re-bind list (see 09 §3) |
