# Facet Launcher 0.1.22

## New Features
- Added an optional media player row on the home screen, showing the current album art, song info, playback controls, and a progress bar (choice of flat or wavy style). Turn it on from the Notifications settings page.

## Fixes
- (none)

## Improvements
- Reorganized Appearance settings into a clearer hub: Clock & calendar, Dock & home, Notifications and media, and General, each with short, icon-labeled sections.
- Moved notification and calendar display settings into Appearance, and renamed "Home & apps" to "Apps and folders".
- Settings rows now clearly show when notification or calendar access still needs to be granted.
- Refined clock designs: aligned the hour and minute in the Accent Field clock, separated the date and extra info onto their own line in the Ruler clock, and improved text contrast for date and accessory text.
- Added German, Spanish, French, and Portuguese translations for the new and updated settings.

## Internal
- Added MediaSessionRepositoryTest, driving a real MediaSession to cover metadata, playback state, transport actions, and album art decoding (bitmap, file URI, http URI, undecodable file), with notification access granted/revoked via shell for test isolation.
- Upgraded material3 to 1.5.0-beta01 (overriding the BOM's 1.4.0) for LinearWavyProgressIndicator; updated TimePicker color parameter usage for the renamed API.
- Split and expanded Appearance settings tests to cover the new hub plus sub-screens over a shared test host.
- Updated architecture docs, CAPABILITIES, design README, test registry, and implementation plan.
