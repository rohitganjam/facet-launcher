# 08 — Flow: Placements (Favorites, Dock, Folders) — add, remove, reorder, clean up

"Placement" is the app's word for *an app or folder pinned somewhere*: the Home app list
(favorites), the Home dock, or inside a folder. This flow covers every write to the
placement tables (`app_placements` and `folder_placements`, plus `folder_apps` for folder membership) and the three background processes that keep them consistent with the OS.

## 1. The routing rule — global vs per-facet

Every add/remove goes through a `domain/` use case that decides which list to write (`DOCK` or
`FAVORITES`) and whose it is (the active facet, or the launcher-wide default when the facet isn't
overriding), based on the **active facet's override flag**. The composable and ViewModel never know
which rows were hit.

```mermaid
flowchart TB
    UI["AppContextMenu / FolderTileContextMenu\n(Drawer long-press, Home long-press)"] -- "QuickPlacementAction.Add / Remove" --> DVM[DrawerViewModel]
    DVM --> UC{"AddToActiveListUseCase /\nRemoveFromActiveListUseCase\n(list, item: app or folder)"}
    UC --> RT["ResolveActivePlacementTargetUseCase(list)"]
    RT --> S["settingsRepository.settings.first().activeFacetId"]
    S --> F["facetRepository.getById(activeFacetId)"]
    F --> D{"facet.placementTarget(list)"}
    D -- "Dock and facet.overrideDock, or Favorites and facet.overridingFavorites" --> T1["PlacementTarget(list, facet.id)"]
    D -- "otherwise" --> T2["PlacementTarget(list) — launcher-wide"]
    T1 & T2 --> POS["position = placementRepository.observeItems(target).first().size\n(append to end)"]
    POS --> DAO["appPlacementDao.upsert(AppPlacementEntity(list, pkg, activity, position, profile, userId))"]
    DAO --> ROOM[(Room invalidates table)]
    ROOM --> HOME["Home / Drawer re-emit via combine\n(see 03 §2)"]
```

- Capacity is enforced *before* the use case, in `ObserveQuickAddStateUseCase`
  (`HomeViewModel`): an `Add` action is only offered when `items.size < max`
  (`AppListLimits.MAX_DOCK_APPS` for dock, `AppListLimits.MAX_FAVORITES` for favorites); a
  `Remove` when the item is already a member. The context menu renders exactly what that state says.
- `userId` on the new row is `app.userHandle.hashCode()` — the same value the unique index and the
  hydration join use, so a Work-profile copy of an app is a distinct placement.

## 2. Reorder (drag)

Reordering rewrites `position = index` for every item in the list (an `upsert` per item, which replaces the existing row on its unique key `(list, ownerKey, component)` or `(list, ownerKey, folderId)`), not a per-row position update:

| Surface (all use `ui/components/DragReorderState`) | ViewModel call | Repository | Mechanism |
|---|---|---|---|
| Dock settings (`DockSettingsScreen`) | `DockSettingsViewModel` → | `PlacementRepository.reorderItems(PlacementTarget(DOCK, facetId), items)` | `upsert` of every app and folder placement with `position = index` (`AppPlacementDao.upsert` / `FolderPlacementDao.upsert`); `replaceItems(facetId, items)` instead calls `deleteAllForOwner(list, facetId)` first, used when an override is switched on |
| Home app list settings (`HomeAppsListSettingsScreen`) | `HomeAppsListSettingsViewModel` → | `PlacementRepository.reorderItems(PlacementTarget(FAVORITES, facetId), items)` | same |
| Folder contents (`FolderDetailScreen`) | `FolderDetailViewModel` → | `FolderRepository.reorderFolderApps(folderId, apps)` | same |
| Onboarding (`OnboardingHomeSetupPage`) | `OnboardingViewModel.reorderDockApps / reorderFavorites` | `PlacementRepository.reorderItems` on the launcher-wide targets only (no facet exists yet) | same |
| Manage facets (`ManageFacetsScreen`) | `ManageFacetsViewModel.reorderFacets` | `FacetRepository.reorderFacets` | `update` per facet with new `position` |

Home itself has no drag-reorder of dock or app list — Home's drag gestures are the clock
handles (zone height / scale); reordering is always done from a settings screen.

Because `position` is rewritten for the whole list, gaps and duplicates can't accumulate; the
cost is N upserts per drag end (Room runs each as its own statement).

## 3. Folders

```mermaid
sequenceDiagram
    participant D as DrawerViewModel / FoldersSettingsViewModel
    participant FR as FolderRepository
    participant FD as FolderDao
    D->>FR: createFolder(name)
    FR->>FD: insertFolder(FolderEntity(name)) → folderId
    D->>FR: addAppToFolder(folderId, app)
    FR->>FD: upsertFolderApp(FolderAppEntity(folderId, pkg, activity, position, profile, userId))
    D->>D: AddToActiveListUseCase(list, PlacedItem.FolderItem(folder))
    Note over D: routes exactly like an app, but writes a folder_placements row (list, folderId, position)
    D->>FR: deleteFolder(folderId)
    FR->>FD: deleteFolder(id)
    Note over FD: FK CASCADE removes folder_apps + every folder_placements row for it
```

A folder is one row in `folders`; membership is `folder_apps`; *where it appears* is a
`folder_placements` row (its `list` and owning facet, or none for the global lists). Deleting the folder cascades
everywhere in SQLite. `FolderRepository.observeFolders()` hydrates members against the live app
list, so a folder whose apps are all uninstalled shows as empty, not missing.

## 4. Keeping placements honest — three background processes

```mermaid
flowchart LR
    subgraph LVM["LauncherViewModel.init (process lifetime)"]
        A["CleanUpUninstalledAppsUseCase()\ncollects forever"]
        B["RepairOrphanedProfileRowsUseCase()\nruns once"]
        C["SeedDefaultDockUseCase()\nruns once per install"]
    end
    OS1["LauncherApps.Callback.onPackageRemoved\n→ AppRepository.observeUninstalledPackages(): (pkg, userId)"] --> A
    OS2["ACTION_MANAGED_PROFILE_REMOVED\n→ AppRepository.observeProfileRemoved(): UserHandle"] --> A
    A -- "removeByPackage(pkg, userId) on all 5 repos" --> T1[(app_placements, folder_apps)]
    A -- "removeByUserId(handle.hashCode()) on all 5 repos" --> T1
    B -- "getOrphanedRows() WHERE userId = -1\n→ backfillUserId(entity, handlesFor(profile).singleOrNull().hashCode())" --> T1
    C -- "defaults_seeded == false →\nDefaultAppRepository.getDefaultAppPackages()\n∩ installed → placementRepository.addApp × ≤ MAX_DOCK_APPS\n→ setDefaultsSeeded(true)" --> T2[(app_placements)]
```

| Process | Trigger | Effect | Why it exists |
|---|---|---|---|
| **Uninstall cleanup** | Per-package `onPackageRemoved` (not `onPackagesUnavailable` — a paused Work Profile must keep its rows) | Deletes the matching rows from `app_placements` (every list, global and per-facet: `deleteByPackage(list, global, pkg, userId)` for `DOCK` and `FAVORITES`, `global = true` and `false`) and from `folder_apps` for that `(pkg, userId)` | Hydration already *hides* missing apps; this makes the removal permanent so the slot frees up and backups don't carry ghosts |
| **Profile removal sweep** | `ACTION_MANAGED_PROFILE_REMOVED` with `EXTRA_USER` | `removeByUserId` for that exact handle only | Unenrolling a Work Profile doesn't reliably fire per-package events; scoped to the handle so a clone profile in the same `AppProfile` category survives |
| **Orphan repair** | Once, at startup | Rows written before schema v20 have `userId = -1`; resolved from their `profile` if exactly one live handle matches that category | Without it, pre-v20 rows never join against the live list and vanish from Home |
| **Default dock seed** | Once, gated by `defaults_seeded` | Fills an empty dock with the OS-resolved browser/SMS/camera/mail/phone apps that are installed | A fresh install (and onboarding) shows a usable dock immediately, independent of the onboarding UI |

## 5. Invariants this flow guarantees

1. A component is placed at most once per list, owner and profile (the unique index is `(list, ownerKey, packageName, activityName, userId)`).
2. `position` is dense `0..n-1` within a list (one `(list, owner)` pair) after any reorder.
3. A placement row never outlives a *confirmed* uninstall or profile removal; it does survive a
   *temporary* absence (paused profile, app being updated) and shows again when the app returns.
4. Every write is routed through a use case that consults the active facet; nothing in `ui/`
   chooses a table.
