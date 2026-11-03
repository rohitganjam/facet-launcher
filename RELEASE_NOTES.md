# Facet Launcher 0.1.38

## New Features
- (none)

## Fixes
- (none)

## Improvements
- Settings now has an About section with your app version, What's new, Check for updates, a Discord link, and Open source licenses.
- The new licenses screen lists the libraries, fonts, and data sources the app uses, and each one opens its full license text.
- A new Gestures page in Settings lets you choose what happens when you open Home (search).
- Added Dutch, Latin American Spanish, and European Portuguese translations.

## Internal
- Moved the About screen into a Settings card and removed the About route; added LicenseRepository serving bundled licence texts from assets/licenses (Apache 2.0, CC BY 4.0, per-font OFL).
- Gestures page and Permissions accessibility row added in 3284a66; double-tap to lock (accessibility service) then disabled in e8c4a9e pending Play AccessibilityService declaration approval.
- Commented out the accessibility service in the manifest, the Gestures switch, the Permissions row, and the Home double-tap action; ignored their tests.
- Set isAccessibilityTool in the service config for when the service returns.
- versionCode is 38 / 0.1.37 as uploaded; the next release script bump gives 0.1.38 / 39.
- Added nl, es-419 and pt-PT string overlays.
- Refreshed graphify output.
- Release 0.1.36 version bump, What's new, and release notes.
