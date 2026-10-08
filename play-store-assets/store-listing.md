# Play Console — store listing text

## Category

**Personalization** — the correct category for launcher/home-screen apps (where Nova
Launcher, Lawnchair, Niagara, etc. are all listed).

## Short description (max 80 characters)

> A minimal home screen with switchable setups for work, personal and focus.

(74 chars)

## Full description (max 4000 characters — this draft is ~3,100)

```
Facet Launcher is a fast, minimal home screen built around one idea: your phone is not used the same way all day. Create separate setups, called facets, and switch between them in a single tap.

FACETS
Create up to three independent facets, for example Work, Personal and Focus, and switch between them in a single tap. Swipe left on the home screen to preview every facet live and switch instantly.
- Each facet can have its own favorite apps, dock, clock style and calendar, or a widget in place of the clock
- New facets inherit your shared defaults, so you only change what differs
- Reorder, rename or remove facets at any time
- Every facet is also an app shortcut ("Switch to Work"), so automation apps can switch it for you

HOME SCREEN
- A clean home screen with a clock, date and your favorite apps
- Swap the clock for any widget of your choice, per facet
- Show Favorites, Recents or Most Used apps in the home list
- Choose left or right alignment, icons, text or both, and top or bottom placement
- A dock of up to five apps, shared across facets or set per facet
- Group apps into folders on the dock and in your favorites
- Notification badges as a dot or a count

CLOCK AND CALENDAR
- More than 30 clock styles, from clean typography to shape-based designs
- Choose font, weight, color, alignment and date format for each style
- Drag to move the clock and resize it directly on the home screen
- Optionally show upcoming calendar events below the clock, with per-calendar colors

APP DRAWER
- List view with a letter index, or a grid in several sizes
- Fast search across your apps, and optionally your contacts and system settings
- Call, message or email a contact straight from the search results
- Separate tabs for your Work Profile and Private Space apps
- Long-press any app for app info, uninstall and its own app shortcuts
- Adjustable drawer opacity and search bar position

WIDGET HUB
Widgets get their own dedicated screen, one swipe from home, so they never crowd your apps. Add up to 20 widgets, drag to arrange them and resize them freely.

APPEARANCE
- Light, dark or system theme
- Accent color taken from your wallpaper, or a fixed color of your choice
- Choice of launcher font and font weight
- Optional monochrome icon style
- Your wallpaper shows through everywhere

BACKUP AND RESTORE
Export your complete setup to a file and restore it on any device.

PRIVACY
Facet Launcher does not have internet access. There are no accounts, no analytics and no ads, and everything runs on your device. Optional features ask for permission only when you turn them on: Calendar for events on the clock, Contacts for contact search, Usage access for Recents and Most Used, and Notification access for badges. You can decline any of them and the rest of the launcher works normally.

REQUIREMENTS
Android 13 or later. Set Facet Launcher as your default home app from Settings to get the full experience.

Questions or feedback: <support email or site — fill in before publishing>
```

Verify before publishing: contact quick actions (call/message/email) and per-calendar colors
match the current build; fill in the support contact (the old GitHub link pointed at the
marketing-site repo and was dropped).

## Screenshots (8 phone screenshots, 1080x1920)

`screenshots/01`–`08`, in upload order: facets, home, clock styles, drawer, search, widgets,
folders, appearance. Built from emulator captures in `raw/` (status bar cropped off) by
`python3 build_screenshots.py` — edit the `SLIDES` list for copy changes, replace a `raw/*.png`
to refresh a screen. The old resize slide was dropped to stay within Play's 8-shot limit
(`raw/resize.png` is kept if you want it back).
