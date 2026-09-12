# Keyboard Dismissal on Home Gesture

Ensure the on-screen keyboard is dismissed when returning to the home screen via the home gesture or back button, specifically when the app drawer is open.

## User Review Required

> [!NOTE]
> This change introduces explicit focus clearing when the drawer is closed or the home gesture is triggered. This is standard launcher behavior.

## Proposed Changes

### UI Components

#### [MODIFY] [HomeDrawerRoute.kt](file:///Volumes/NVME/repo/facet-launcher/app/src/main/kotlin/com/facetlauncher/app/ui/launcher/HomeDrawerRoute.kt)
- Add `LocalFocusManager` to clear focus when the home gesture is triggered.
- Clear focus in the `homePressedEvent` collector.
- Clear focus in the `BackHandler` when closing the drawer or hub.
- Clear focus when an app is launched from the drawer.
- Add a `LaunchedEffect` to clear focus whenever a drawer drag begins, ensuring the keyboard hides as the user swiped the drawer away.

#### [MODIFY] [AppDrawerScreen.kt](file:///Volumes/NVME/repo/facet-launcher/app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt)
- Add `LocalFocusManager` to clear focus when the search query is cleared via the back button.

## Verification Plan

### Automated Tests
- No new automated tests are added, as keyboard visibility is hard to test in unit tests without complex espresso setups. Existing tests should pass.

### Manual Verification
1. Open the app drawer.
2. Tap the search bar to open the keyboard.
3. Perform the home gesture (swipe up from bottom or press home button).
4. **Verify**: The keyboard is dismissed as the drawer closes.
5. Open the app drawer and keyboard again.
6. Press the back button.
7. **Verify**: The keyboard is dismissed and search is cleared (first back press) or drawer closes (second back press).
8. Open the app drawer and keyboard again.
9. Swipe the drawer down manually.
10. **Verify**: The keyboard is dismissed as soon as the swipe begins.
