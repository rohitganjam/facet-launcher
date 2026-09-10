repo: rohitganjam/lumen-launcher
branch: master
path: app/src/main

## Last sync
date: 2026-09-07T15:48:07Z

### Updated in this project
- Added turn 6: eleven new clock template concepts introducing shape and colour, after auditing all 20 shipped templates in ClockTemplates.kt.
- Added turn 5 to both launcher designs: the shipped Settings directory and its nine sub-screens.
- Redrew Appearance with both accent sources — wallpaper Material You tones and the ten fixed light/dark swatches.
- Adopted the implementation's recalibrated tokens (page vs card surface split, 55% secondary text, 12px card radius).
- Documented Clock & Calendar Style, Notifications, Permissions and Backup & restore as separate destinations.

## Screen map
| Project screen | Repo files |
| --- | --- |
| 5a Settings | ui/settings/SettingsScreen.kt, ui/navigation/LumenNavHost.kt |
| 5b/5c Appearance | ui/settings/AppearanceSettingsScreen.kt, ui/theme/AccentSwatch.kt, ui/theme/Color.kt |
| 5d Clock & Calendar Style | ui/home/clock/ClockStyleGalleryScreen.kt, data/model/ClockTemplateId.kt, data/model/ClockFontOption.kt, data/model/ClockColorOption.kt |
| 5e Home Apps List | ui/settings/HomeAppsListSettingsScreen.kt |
| 5f App Drawer | ui/settings/AppDrawerSettingsScreen.kt |
| 5g Dock | ui/settings/DockSettingsScreen.kt |
| 5h Notifications | ui/settings/NotificationSettingsScreen.kt |
| 5i Permissions | ui/settings/PermissionsScreen.kt |
| 5j Backup & restore | ui/settings/backup/BackupRestoreScreen.kt |
| 6a–6m new clock templates | ui/home/clock/ClockTemplates.kt, data/model/ClockTemplateId.kt, ui/theme/Color.kt |
| Shared card/row/dropdown chrome | ui/components/SettingsCard.kt, ui/components/LabeledDropdownRow.kt |
| Tokens & typography | ui/theme/Color.kt, ui/theme/Theme.kt, ui/theme/Type.kt, data/model/LauncherSettings.kt |

## Sync history
- 2026-09-07T15:28:15Z — first import: settings tree, appearance, clock gallery, backup screens read from app/src/main.
