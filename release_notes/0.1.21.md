# Facet Launcher 0.1.21

## New Features
- (none)

## Fixes
- (none)

## Improvements
- The app now works on Android 11, not just Android 12 and up
- On Android 11, Appearance shows a simpler set of accent color presets, since the newer Material You color picker isn't available on that version

## Internal
- Lower minSdk from 31 to 30 and gate Android 12+ APIs behind version checks: accent color source tabs, BLUETOOTH_CONNECT vs install-time BLUETOOTH permission, Wi-Fi SSID retrieval fallback via WifiManager, widget targetCellWidth/Height, onboarding backdrop blur
- Add tests for each Android 11 compatibility gate and update architecture docs
- Bump version to 0.1.20 (code 21) and add release notes
