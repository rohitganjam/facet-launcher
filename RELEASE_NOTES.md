# Facet Launcher 0.1.36

## New Features
- New "Hide status bar" option hides the status bar on the Home screen, Hub and app drawer. It's off by default and applies only to this device. When the bar is hidden, swipe down on Home to show it for 3 seconds, and swipe down again to open notifications. Hiding the bar doesn't shift or resize the screen, including on phones with a camera cutout.
- New "Force icon shape" switch: when you use an icon pack, it can crop the pack's icons to the shape you picked.

## Fixes
- Pressing Home while in another app no longer opens the search in the app drawer by mistake.

## Improvements
- Icon Shape and Icon size are now on one card above the icon pack settings, and they always apply.
- The "Keep default" icon option is now called "Use default icons", and the row for apps missing from an icon pack now describes the option you selected.
- In Appearance settings, the "General" section is now "Look & Feel", and its first row is now "General settings" instead of "Themes and colors".

## Internal
- Force icon shape setting is carried to AppIcon via IconOutline.
- Added launcherSystemBarInsets() for Home, Hub and Drawer, which drops the status bar from the top inset so a revealed bar overlays content. Sheet windows follow the hidden state, and the hide is re-applied on resume and window focus.
- On API 30–34 the activity opts out of decor-fits-system-windows, and the v29 theme sets shortEdges cutout mode.
- onNewIntent now tracks whether the activity was stopped since it last resumed, because lifecycle state can't tell a bare Home press from a return from an app.
- Added a baseline profile, an unminified profile build type and compilation benchmarks.
- Enabled resource shrinking with a catch-all keep.xml, which clears Play Console's R8 recommendation without removing any resources.
- Play Store listing: added icon packs to the full description.
- Updated the giveaway-pick script.
- Refreshed graphify output (twice).
- Release 0.1.35: version bump, What's new, release notes.
