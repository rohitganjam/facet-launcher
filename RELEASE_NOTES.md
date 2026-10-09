# Facet Launcher 0.1.18

## New Features
- **Facet automation.** Set up rules that switch your facet for you. Open Settings → Facet automation to add rules that run on a schedule (days and times, overnight allowed), or when you connect to a Wi-Fi network or a Bluetooth device, plug in headphones, or reach a battery level while charging or not. Picking a facet yourself, or switching with a "Switch to" shortcut from an app like Tasker or Samsung Modes and Routines, overrides the active rule until it ends. A rule that needs a permission you haven't granted shows as unavailable.
- **Facet Pro.** A one-time purchase through Google Play, with no subscription, that you can restore on any phone signed in to the same Google account. The free plan includes 3 facets and 2 schedule rules. Pro adds up to 10 facets, automation rules for Wi-Fi, Bluetooth, headphones and battery, more rules, and your own widget in place of the clock. Find it in Settings, or tap anything marked Pro.
- **Icon shape.** Choose Squircle, Rounded, Circle or Square for app icons in Appearance.

## Improvements
- Going back now slides the previous screen in on every screen, instead of the system's default shrink animation.
- Home text (clock, calendar, app labels) has a softer offset shadow so it stays readable on light wallpapers.
- The clock style option, with a live preview, now lives in Appearance instead of Facet settings.
- Text buttons such as "Open settings" and "Set as default" now use the same rounded-square shape as the rest of the app.
- The Permissions screen lists Bluetooth and Location, which the new automation rules can use.

## Notes on Pro
- If Pro is ever refunded or removed, nothing is deleted: your first 3 facets stay usable, the rest are paused until you are back to 3 or Pro returns, and rules that need Pro pause. A custom clock widget you already set up stays in place.
- Facet Launcher still has no internet permission. Purchases are handled by the Google Play app.

## Internal
- Upgraded Gradle (9.8.0), the Android Gradle Plugin (9.4.1), Kotlin (2.4.10) and the main libraries to their latest stable releases.
- Added Play Billing, with a build check that fails if the billing library ever brings the INTERNET permission back, and a debug-only Pro override for testing.
