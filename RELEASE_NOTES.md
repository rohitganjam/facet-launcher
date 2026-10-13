# Facet Launcher 0.1.24

## New Features
- (none)

## Fixes
- The media player row on Home no longer stays stuck on screen for apps (like Spotify or YouTube) that keep a paused session alive after you've dismissed their notification — it now disappears when the notification is gone.
- When several apps could show a media player row, the picker now sensibly prefers whichever is actively playing, then falls back to the most recently played.
- Fixed Home staying sharp and not fading behind the Widgets Hub and Switch Facets panels on older Android versions (Android 11) — it now fades and shrinks consistently on all versions, matching the Drawer behavior.

## Improvements
- Added a bit more spacing between the calendar and the media player row on Home.
- The Widgets Hub and Switch Facets screen headers now look identical in style (title, subtitle, and text shadow).

## Internal
- Increased HOME_MEDIA_ROW_TOP_GAP from 4dp to 8dp.
- Removed the onWallpaper flag from ScreenHeader so Hub and Switch Facets headers are identical by construction.
- Replaced the Android 12+-only RenderEffect blur approach for Home fade with homeLayerFade, matching the drawer's cross-version fade/scale approach.
- Added NotificationInfo.isMedia and exposed media-session packages via NotificationBadgeRepository; added ActiveSessionPicker for session selection logic.
- Release 0.1.23 (code 24).
