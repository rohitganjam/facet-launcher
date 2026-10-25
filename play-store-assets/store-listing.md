# Play Console — store listing text

## Category

**Personalization** — the correct category for launcher/home-screen apps (where Nova
Launcher, Lawnchair, Niagara, etc. are all listed).

## Short description (max 80 characters)

> Your phone, on switch: a minimal home screen that changes with your day.

(72 chars)

## Full description (max 4000 characters — this draft is 3,972)

```
Your phone isn't used the same way all day, so why should your home screen be?

Facet Launcher is a fast, minimal home screen built around facets: separate setups for work, home and focus that you switch in a single tap, or let your phone switch for you. One phone, a home screen that always fits the moment.

FACETS: YOUR PHONE, ON SWITCH
Create up to ten independent facets, like Work, Personal and Focus. Swipe left on the home screen to preview every facet live, then tap to switch instantly.
- Give each facet its own favorite apps, dock, clock style, calendar and app list layout, or put a widget where the clock goes
- New facets inherit your shared defaults, so you only change what's different
- Reorder, rename or remove facets whenever you like
- Every facet is also an app shortcut ("Switch to Work"), so automation apps can switch it too

FACET AUTOMATION: IT SWITCHES FOR YOU
Walk into the office and your Work facet is waiting. Plug in headphones and Focus kicks in. Each rule picks a facet and one trigger:
- A schedule, by day and time window
- A Wi-Fi network (any, or a named one), a Bluetooth device, headphones, or the battery level
- Choose what happens when the rule ends: go back, switch to another facet, or stay
Pick a facet yourself and your choice always wins.

A HOME SCREEN THAT GETS OUT OF THE WAY
- Just a clock, the date and the apps you actually use
- Swap the clock for any widget you like, facet by facet
- Show Favorites, Recents or Most Used apps
- Lay your list out as a single column, two columns or a grid, aligned left or right, with icons, text or both, at the top or bottom
- A dock of up to five apps, shared or set per facet
- Group apps into folders on the dock and in your favorites
- Notification badges as a dot or a count
- An optional media player row with album art, song details, playback controls and a progress bar, right on your home screen

CLOCKS WITH CHARACTER
- More than 30 clock styles, from clean typography to bold shapes
- Set the font, weight, color, alignment and date format for each style
- Drag to move your clock and resize it right on the home screen
- See what's next: upcoming calendar events sit under the clock, from now until noon tomorrow. Show one to three, with an optional color bar for each calendar

AN APP DRAWER THAT KEEPS UP
- A letter-indexed list, or a grid in several sizes
- Fast search across your apps, and optionally your contacts and system settings
- Call, message or email a contact straight from the results
- Separate tabs for your Work Profile and Private Space apps
- Long-press any app for app info, uninstall and its own shortcuts
- Adjust the drawer's opacity and where the search bar sits

WIDGET HUB
Give your widgets a home of their own, one swipe from your home screen, so they never crowd your apps. Add up to 20, drag them into place and resize them freely.

MAKE IT YOURS
- Light, dark or system theme
- An accent color pulled from your wallpaper, or one you choose
- Your pick of launcher font and weight
- Icons as squircles, rounded squares, circles or squares, or a monochrome look
- Your wallpaper shows through everywhere

BACKUP AND RESTORE
Export your facets and settings to a file and restore them on any device.

PRIVATE BY DESIGN
Facet Launcher uses the internet only for the optional weather feature, which is off until you turn it on and then asks for an approximate location. No accounts, no analytics, no ads: everything else stays on your device. Optional features ask for permission only when you turn them on: Calendar for events on your home screen, Contacts for contact search, Usage access for Recents and Most Used, Notification access for badges and the media player, Bluetooth for Bluetooth rules, and Location for the weather at your position and so Android lets the app read a Wi-Fi network name for named-network rules. Say no to any of them and the rest of the launcher works normally.

REQUIREMENTS
Android 11 or later. Set Facet Launcher as your default home app in Settings to get the full experience.

Questions or feedback: <support email or site — fill in before publishing>
```

Verify before publishing: contact quick actions (call/message/email), per-calendar colors and the
facet automation triggers match the current build; fill in the support contact (the old GitHub
link pointed at the marketing-site repo and was dropped). The description lists every feature as
available, so declare in-app purchases in Play Console and keep it consistent with the app.

## Screenshots (8 phone screenshots, 1080x1920)

`screenshots/01`–`08`, in upload order: facets, home, clock styles, drawer, search, widgets,
folders, appearance. Built from emulator captures in `raw/` (status bar cropped off) by
`python3 build_screenshots.py` — edit the `SLIDES` list for copy changes, replace a `raw/*.png`
to refresh a screen. `08_appearance` predates the Appearance hub redesign, and `02_home` and
`01_facets` predate the media row and the new Hub/Switch Facets headers: refresh them from the
current build. The old resize slide was dropped to stay within Play's 8-shot limit
(`raw/resize.png` is kept if you want it back).
