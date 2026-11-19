# Facet Launcher 0.2.4

## New Features
- (none)

## Fixes
- Facet now appears in the Play Store and can be installed on devices that lack Wi-Fi, Bluetooth, or location hardware, which were previously hidden from it.
- The wallpaper copy cards no longer suggest a copy that is still being saved after you set a wallpaper.

## Improvements
- Setting a wallpaper is noticeably faster: the confirm step dropped from about 2.7 s to about 1.9 s, and the remaining views and screens finish saving in the background.
- Opening, scrolling, searching in, and closing the app drawer, hub, and carousel is much smoother, with far fewer stuttering frames.
- Swiping the drawer, hub, and carousel open and closed is smoother.

## Internal
- Wallpaper copies are now quality-95 JPEGs instead of lossless PNGs (~30 ms vs ~0.5 s each on S25 Ultra); old PNG copies are still read until that view is saved again.
- Copies for views other than the one showing are stored in parallel in the application scope, only while the system wallpaper is still the one just set; `WallpaperCopyRepository.isFinishingCopies` drives the other-orientation card and Home's capture pill.
- `WallpaperBackdropHost` bakes a small frosted bitmap per theme off the main thread, rebuilt whenever the copy changes; `FrostSurface` draws it as a plain image under the same wash, falling back to the theme tint until ready or with no copy. Glass icons still sample the copy and the frost together. Replaces live per-frame blur (77% → 5% janky frames, 77 ms → 7 ms median on debug S25 Ultra).
- `panelPlacement` now slides panels via a `graphicsLayer` translation (taken from the layer's draw size so the first frame and rotation stay correct) instead of layout placement.
- Manifest: declared `required=false` for Wi-Fi, Bluetooth, and location hardware features implied by `ACCESS_WIFI_STATE`, Bluetooth, and location permissions.
- Release 0.2.3 version bump.
