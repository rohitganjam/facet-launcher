# Facet Launcher 0.1.40

## New Features
- (none)

## Fixes
- Widgets that need a setup step (for example, picking an account or options) now show their setup screen when you add them. Previously this step was skipped for every widget. Setup also now works for widgets from a Work Profile app and for widgets whose setup screen isn't publicly accessible.

## Improvements
- (none)

## Internal
- Replace the reflective call to hidden `getIntentSenderForConfigureActivity` (blocked for apps, always returned null) with a trampoline activity using the public `AppWidgetHost.startAppWidgetConfigureActivityForResult`.
- Fix duplicate `Intent` imports; add tests for the widget configure trampoline.
- Refresh the graphify output.
- Release 0.1.39 (versionCode 40): version bump, in-app What's new, and release notes.
