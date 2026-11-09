# Play Console — store listing text

## Category

**Personalization** — the correct category for launcher/home-screen apps (where Nova
Launcher, Lawnchair, Niagara, etc. are all listed).

## Short description (max 80 characters)

> Your phone, on switch: a minimal home screen that changes with your day.

(72 chars)

## Full description (max 4000 characters — this draft is 3,994)

```
Your phone isn't used the same way all day, so why should your home screen be?

Facet Launcher is a fast, minimal home screen built around facets: separate setups for work, home and focus that you switch in one tap, or let your phone switch for you.

FACETS: YOUR PHONE, ON SWITCH
Create up to ten facets, like Work, Personal and Focus. Swipe left on the home screen to preview them live, then tap to switch.
- Give each facet its own favorites, dock, clock style, calendar, app list layout and landscape setting
- Every facet is also an app shortcut ("Switch to Work"), so automation apps like Tasker can switch it too

FACET AUTOMATION: IT SWITCHES FOR YOU
Walk into the office and your Work facet is waiting. Plug in headphones and Focus kicks in. Each rule picks a facet and one trigger:
- A schedule, a Wi-Fi network, a Bluetooth device, headphones or the battery level
- Choose what happens when the rule ends: go back, switch to another facet, or stay
Pick a facet yourself and your choice always wins.

A HOME SCREEN THAT GETS OUT OF THE WAY
- Just a clock, the date and the apps you actually use
- Swap the clock for a Facet widget (Weather, Agenda or Now Playing) or any Android widget, and resize it in place
- Show up to 20 Favorites, Recents or Most Used apps, as one column, two columns or a grid, with icons, text or both
- Press and hold to drag apps, folders and dock icons: drop on an empty spot to move, on another to swap
- A dock of up to five apps for each facet, with folders on the dock and in favorites
- Choose what swiping left, right, up and down does
- Notification badges as a dot or a count
- An optional media player row with album art, playback controls and a progress bar
- Built for landscape, foldables and tablets, with the clock, apps and dock side by side

CLOCKS WITH CHARACTER
- More than 30 clock styles, from clean typography to bold shapes, each with its own font, color and date format
- Drag to move and resize your clock right on the home screen
- Weather beside your alarm and battery, with a 24-hour forecast and the days ahead one tap away. Use your location or save up to ten cities
- Upcoming calendar events under the clock, with an optional color bar per calendar
- Widget stacks: swipe between the clock, Facet widgets and other widgets in the same spot

AN APP DRAWER THAT KEEPS UP
- A letter-indexed list, or a grid in several sizes
- Fast search across apps, and optionally contacts and system settings, with call, message or email from the results
- Separate tabs for your Work Profile and Private Space apps
- Long-press an app for info, uninstall, shortcuts and its ongoing notifications

WIDGET HUB
Give widgets a home of their own, one swipe from your home screen, so they never crowd your apps. Add up to 20, drag them into place and resize freely.

MAKE IT YOURS
- Light, dark or system theme, with an accent from your wallpaper or your pick
- Your pick of launcher font and weight
- Icon shapes (squircle, rounded square, circle, square) and styles: full color or a monochrome look tinted to your accent
- Icon packs: apply any pack you've installed (Nova, ADW, Apex and more) to every app

BACKUP AND RESTORE
Export your facets, settings and home layout to a file and restore them on any device.

FACET PRO
Facet Launcher is free. An optional Pro upgrade adds more automation rules, Wi-Fi, Bluetooth, headphone and battery triggers, and widget stacks of up to five pages.

PRIVATE BY DESIGN
The internet is used only for the optional weather feature, which is off until you turn it on. No accounts, no analytics, no ads: everything else stays on your device. Optional features ask for permission only when you turn them on, and the launcher works normally if you say no.

REQUIREMENTS
Android 11 or later. Available in English, German, Spanish, French and Portuguese. Set Facet Launcher as your default home app in Settings to get the full experience.

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
