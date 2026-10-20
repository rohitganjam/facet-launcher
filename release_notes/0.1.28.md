# Facet Launcher 0.1.28

## New Features
- Notification settings now include a live preview. It shows a sample notification badge in the style you picked (dot or count) and a sample media player with skip buttons that follow your setting. The preview works even before notification access is granted.

## Fixes
- With Center alignment on the Home screen, the notification badge no longer pushes the row off-center. The icon and name stay centered and the badge sits just beside them.

## Improvements
- The notification preview now uses your actual Home list and Dock layout, with sample badges in your chosen style.
- The notification preview now shows an "Allow access" prompt if notification access hasn't been granted.
- If badges and the media player are both turned off, the preview says so. If only one is on, the preview shows just that one.
- On Home rows that show only an icon, the notification badge now sits on the corner of the icon instead of next to it.
- The widget picker is now sorted by app name, then by widget name.
- Automations use less battery: they only watch device sources such as Wi-Fi while an enabled rule needs them. Location access is only used when a rule checks for a specific Wi-Fi network name.
- Settings screens and pop-up sheets no longer change with the in-app font size and weight options, so they stay readable and consistent. Home and the previews still follow those options, and your phone's own font and display size settings still apply.
- The Appearance settings are reorganized into separate Theme, Icons and Fonts sections. A new font preview shows where each text style is used.
- Some settings were renamed to be clearer:
  - "Launcher Appearance" is now "Appearance"
  - "System bar icons" is now "Status bar icons"
  - "Match theme" is now "Default"
  - The font option is now "Launcher Font Style"
- The folder contents sheet has a new look. It fills the screen like the app sheet, has a matching header (folder icon, name, app count, rename) and slides in and out.
- In the app sheet, "Add to folder" now has a folder icon with a "+" badge.

## Internal
- Update test registry.
- Register automation device-source callbacks only while an enabled rule reads them; nothing registered with no rules. Wi-Fi callback requests location info (SSID) only for named-network rules.
- Notification preview renders the real Home and Dock layout via `HomeSurfacePreview`.
- Added `UnscaledTypography` for the Settings screens and bottom sheets.
- Tests: `AppRowBadgeTest`, `NotificationPreviewTest`, `NotificationSettingsScreenTest`. Other tests and docs updated for the Appearance and sheet changes.
- Release 0.1.27 (28): version bump, in-app What's new and release notes.
