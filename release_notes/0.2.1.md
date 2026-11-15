# Facet Launcher 0.2.1

## New Features
- You can now choose what each Home screen swipe (up, down, left, right) does. In Settings > Gestures, tap a swipe and pick an action: open the app drawer, open the Hub, switch facets, open the notification shade, or do nothing. Your current swipe settings carry over automatically.
- You can put the same action on two swipes after a confirmation. The app drawer always keeps at least one swipe so you can't lose access to it.
- Backups now save your swipe actions. Older backups still restore your previous swipe settings.

## Fixes
- The facet carousel now shows your new wallpaper right away when you set one from Daily photos or your own image. Before, it kept showing the old wallpaper.
- When you finish resizing a Hub widget, it no longer briefly jumps back to its old size before settling.

## Improvements
- Resizing a Hub widget feels smoother. The widget follows your finger exactly instead of jumping a whole cell at a time, an outline shows which cells it will fill, and when you let go it glides into place. This works in both portrait and landscape.
- The app drawer, Hub and facet carousel now slide in from the side you swiped from, and you close them by swiping back the other way.

## Internal
- Resize handle: read the drag delta before consuming it, so drags no longer stop short after the touch-slop jump. This fixes a regression from 22d8be52 that never shipped.
- Resize handle reports the finger's whole travel once past touch slop, so it no longer trails the finger by about 8dp. This fixes a regression from 2134a4f3 that never shipped. The follow-the-finger test is back to the exact distance.
- HubGestureTest: the resize-follows-finger check allowed for touch slop; this was replaced by the exact-distance check above.
- HubGestureTest: Now playing opens its settings, and only hosted widgets lack Configure (follow-up to 25ea4129).
- HubResizeGeometry: read Rect edges directly instead of a 4-way destructure (detekt).
- Gestures: detekt fixes and a closing settle in the two-swipes test.
- WallpaperRepository.homeWallpaper emits again when a new copy is stored or a new system wallpaper id is recorded. Added tests in WallpaperRepositoryTest and FacetCarouselViewModelTest.
