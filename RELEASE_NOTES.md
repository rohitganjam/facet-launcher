# Facet Launcher 0.1.35

## New Features
- Icon pack support: apply icon packs you've installed (the kind made for Nova, ADW, Apex and similar launchers) to every app from Settings → Appearance → Icons, alongside the built-in styles. Apps the pack doesn't cover can be styled to match the pack or keep their own icon. The screen also shows how many of your apps the pack covers, lists the ones it misses, and links to where you can find more packs.
- Calendar icons from icon packs now show today's date and update by themselves when the day, time or time zone changes.
- New Icon size slider on the Icons screen (80–120%) that resizes app icons everywhere: Home, the dock, the app drawer, folders and Private Space.
- New "Shrink list to fit" option for each facet (off by default): if Home's list doesn't fit on screen, it switches to compact rows instead of scrolling.
- New "Icons and text" style for the dock and Home's grid, which shows the app name under each icon.
- Pressing the Home button while you're already on Home now opens the app drawer with search ready to type.
- Onboarding's home setup page now lets you choose where the search bar goes and turn contacts and settings search on or off.

## Fixes
- On a clock stack, calendar events now show only on the built-in clock page, not on pages that hold a widget.

## Improvements
- App icons are now the same size on Home, the dock, the drawer, folders and Private Space.
- Icon pack icons are sharper.
- Folders now match the layout of the place you opened them from (Home's grid or the drawer's grid, with the same columns and labels), or use standard list rows otherwise.
- The Icons screen opens faster: each pack shows its own app icon, and a pack is only loaded when you tap it to apply it.
- Installed icon packs are now labeled "External", next to "Built-in".
- List rows have more room in the Regular and Spacious sizes.
- Onboarding now asks you to pick facets before home setup, and home setup is the last step.
- The launcher only asks for contacts permission when you turn on contacts search.
- In Settings, the search bar position option now sits above the search toggles, and the contacts search description reads "Show contacts in search results".
- Icon outlines are more consistent: all of them reach the same brightness, and plain icons now have the same rim thickness as themed ones.

## Internal
- IconPackRepository: finds packs by launcher-theme intents, parses appfilter.xml (asset text scan, then XML resource fallback) and caches parsed packs. AppRepository swaps in pack icons and rescans when the pack changes. New `icon_pack_package` / `icon_pack_fallback` settings, included in backups. A pack that is no longer installed gets unapplied on the next scan.
- Icon pack `<calendar>` parsing tolerates the doubled brace that some real packs have in a component. Dated apps redraw on date/time/zone broadcasts and when Home resumes, without a full re-list.
- Applied pack coverage now reuses Home's parse instead of parsing every installed pack.
- Pack icon cap raised from 192px to 256px.
- Settings migration 36→37 for "Shrink list to fit".
- `DrawerListItemSize` padding: Regular 4→12dp, Spacious 8→16dp (rows 68/76dp).
- Home press reselect: treat STARTED as already in front (onNewIntent runs after onPause, so RESUMED never matched).
- ClockBlock skips CalendarEventsBlock when a widget id is set.
- Onboarding search options landed inside commits 5ba8534 and a944203 (swept up from the shared index). d1abf00 is an empty commit that records this.
- Tests, translations, living docs, CAPABILITIES, PRD copies, IMPLEMENTATION_PLAN, TEST_REGISTRY and ONBOARDING_FLOW.md updated.
- Regenerated graphify-out (×2).
- Added combined 0.1.27–0.1.34 release notes and a giveaway-pick script.
- Refreshed Play Store screenshots. Updated the Play Store listing and capabilities for weather, landscape, widget stacks and Pro.
- Release 0.1.34: version bump, What's new, release notes.
