# Reset Drawer State on Close or App Launch

This plan addresses the requirement to clear the search term, search results, and scroll position in the app drawer whenever it is closed or when an app is opened from it.

## User Review Required

> [!IMPORTANT]
> The drawer state reset will be triggered when the drawer is fully closed (animation finishes). This ensures that users don't see the UI "jump" (e.g., list scrolling to top) while the drawer is still sliding down.
>
> When an app is launched from the drawer, we will also explicitly trigger the drawer closure animation to ensure it returns to the home screen state and resets for the next time it's opened.

## Proposed Changes

### UI Layer

#### [MODIFY] [HomeDrawerRoute.kt](file:///Volumes/NVME/repo/lumen-launcher/app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt)

- Add a `LaunchedEffect` that monitors `isDrawerOpen`. When it transitions to `false` (drawer fully closed), reset the `drawerQuery`, notify the `DrawerViewModel`, and scroll both `listState` and `gridState` back to the top (index 0).
- Update the `onAppClick` callback passed to `AppDrawerScreen` to trigger the drawer closure animation. This will, in turn, trigger the reset logic once the animation completes.

## Verification Plan

### Automated Tests
- I will run existing tests for `HomeDrawerRoute` to ensure no regressions in drawer behavior.
- (Optional) I can add a test case to verify that `drawerQuery` is empty after closing the drawer.

### Manual Verification
- Open the drawer, scroll down, then close it. Re-open to verify it's at the top.
- Open the drawer, type a search term, then close it. Re-open to verify the search bar is empty.
- Open the drawer, type a search term, launch an app. Press Home to return and verify the drawer is closed and reset.
