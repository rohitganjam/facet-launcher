# 21 — Wallpaper copy and real glass

Android does not let a launcher read the system wallpaper (`WallpaperManager.peekDrawable()` throws `READ_EXTERNAL_STORAGE denied` on API 30 and 36, with Facet
as the default home or not). So the glass panels ([13](13-flow-facets-theme-notifications-onboarding.md), *Glass panels*) are plain tints by default. If the user
gives Facet an image, Facet keeps a **copy** of it, draws it behind the app, and the panels sample it for a real blur (API 31+) and, for the clear styles, a
lens (API 33+).

## Parts

| Piece | Where | Role |
|---|---|---|
| `WallpaperCopyRepository` | `data/` | Owns `wallpaper_copy.png` (screen size, `filesDir`, not in backups). Reads it once, saves through a temp file, removes it. Decodes a picked photo with `ImageDecoder`, sets the system wallpaper with `WallpaperManager.setBitmap`, and reports the screen size (portrait) and the system wallpaper id. |
| `SetWallpaperFromPhotoUseCase` | `domain/` | Crops the framed part to the screen size, sets it as the system wallpaper (Home, or Home and lock), stores it as the copy, records the id. Stores nothing unless the system accepted the wallpaper. |
| `RemoveWallpaperCopyUseCase` | `domain/` | Deletes the copy and the recorded id. The system wallpaper stays. |
| `WallpaperBackdropViewModel` | `ui/launcher/` | The stored copy as a `StateFlow<Bitmap?>`, loaded as the app starts. |
| `WallpaperBackdropHost` + `LocalWallpaperBackdrop` | `ui/components/WallpaperBackdrop.kt` | Around `FacetNavHost` in `LauncherActivity`. Records the copy into the Backdrop library's layer (through `rememberLayerBackdrop`'s `onDraw`, on an empty box) without drawing it, so the system wallpaper is always what shows, and provides it to the panels. A screenshot copy never stands in for the wallpaper. |
| `Modifier.glassBackground(..., sampleWallpaper)` | `ui/components/GlassBackground.kt` | With a backdrop, a panel is drawn through `drawBackdrop` (blur, brightness and saturation per style, plus the tint and edge); without one it is the plain tint. The dock, apps list rows and grid tiles, calendar block, media row and native widgets opt in; icon backgrounds and the settings previews keep the tint. |
| `clearGlassLens` | `ui/components/ClearGlassLens.kt` | A copy of the library's rounded-rectangle lens shader (Apache-2.0, Kyant) with the depth pull as a strength. Used by every glass style, though under the heavy blur of the frost styles little is visible. |
| `WallpaperScreen` / `WallpaperViewModel` | `ui/settings/` | Settings → Change wallpaper (route `wallpaper`). |
| `WallpaperFit` | `ui/settings/` | The fit step's pan and zoom maths (`FitGeometry`, `WallpaperFit`), pure and tested. |

## The screen

*Change wallpaper* opens `WallpaperScreen`, which shows the copy (or a stand-in for the system wallpaper) and a status line, then two equal choices:

1. **Choose an image for Facet.** The gallery app, through `GetContent("image/*")` (no permission; a chooser when several apps answer) → the **fit step**: the photo in a screen-shaped window, drag to move, pinch to zoom
   (1× to 5× of the zoom that just covers the screen), double-tap to reset, a faint outline of the clock and dock, and a choice of *Home screen* or *Home and lock
   screen* → *Set wallpaper*. The result is cropped to the screen size, set as the system wallpaper, stored as the copy and its system id recorded.
2. **Set a system wallpaper.** `ACTION_SET_WALLPAPER`, as before. Glass stays a tint.

With a copy, the screen also offers *Remove the copy*. When the screen resumes it re-reads the system wallpaper id; if it differs from the recorded one the copy is
**stale** (the wallpaper was changed elsewhere), and the screen says so.

## What decides whether glass is real

`WallpaperBackdropHost` provides a backdrop only when all of these hold: a copy exists, the device is on API 31 or later (below that `RenderEffect` blur does not
exist and the unblurred copy would show inside the panel), and the window is portrait (the copy is a portrait screen). Otherwise panels draw their tint and the
system wallpaper shows through as before.

| API | Behaviour |
|---|---|
| 30 | Plain tint panels. |
| 31–32 | Blur, brightness and saturation; the lens does nothing, so Clear glass and Dark glass are sharp tinted panels and the other styles lose only the edge bend. |
| 33+ | Everything, including the lens. |

## Prompts

- **First use.** `GlassBackgroundRow` (all five background pickers: dock, apps list, calendar, media row, native widgets) applies the chosen style and, for any style but Off, shows a sheet when `GlassBackgroundPromptViewModel.needsCopy` (no usable copy, not dismissed): *Choose an image* (opens the wallpaper screen) or a remembered *Not now* (`wallpaper_prompt_dismissed`). The state reaches the rows through `LocalGlassBackgroundPrompt`, provided by `FacetNavHost`; without it (previews, tests) there is no prompt.
- **After Android's picker.** *Set a system wallpaper* records the wallpaper id first. Coming back with a different id and no matching copy, `WallpaperViewModel` shows an offer card (*Choose an image* / *Not now*, the same remembered flag). Facet cannot read the new wallpaper, so the offer asks for the same image.

- **Use my current wallpaper.** A link on the wallpaper screen (shown with no copy or a stale one) opens a dialog: *Take screenshot* opens `WallpaperCaptureScreen` (route `wallpaperCapture`) and *I have an image already* opens the gallery. The capture screen has no background of its own, so the window's wallpaper shows through, and it hides the system bars; a card with three numbered steps (key actions in bold, including tapping Power + Volume down and not holding them, which opens the shutdown menu) stays until *Take screenshot* is tapped (no timer), which clears it so the screenshot is clean; a tap on the empty screen brings it back. The card also has *Choose screenshot* and *Return*. Choose pops back to the wallpaper screen (*Return* and Back do too, without the picker) and sets `wallpaper_pick_screenshot` on its saved state, which opens the picker. The fit step has no Home/Lock choice, and `UseScreenshotAsWallpaperCopyUseCase` stores the framed part and records the current system id. The system wallpaper is not set.

## Previews

`WallpaperRepository.currentHomeWallpaper()`, the one source of every preview's background (facet carousel cards, the Appearance, dock, list, drawer and notification previews), returns the saved copy first when it still matches the system wallpaper, then falls back to what Android lets it read.

## Screens that dim the wallpaper

Glass samples the raw wallpaper copy. The Hub draws the theme surface at 60% over the wallpaper, so it provides that colour as `LocalBackdropTint` and a glass panel on it (the native widgets) draws the tint over what it blurs before its own fill, which matches what the Hub shows. The Drawer has its own overlay and no glass panels of this kind. A screen with a different dim needs the same local.

## Real glass on icons

An icon's glass background samples the copy only inside live Home (`LocalIconGlassSamplesWallpaper`, provided in `HomeDrawerRoute`); the drawer, settings, previews and pickers keep the tint.

## Daily photos

*Set a wallpaper with Facet* is one flow: its row on the wallpaper screen opens `DailyPhotosScreen`, a grid of a week's photos under a *Bing* and a *Spotlight* tab (Peapix feeds, seven photos each however many are asked for), and at the foot of the grid *Choose your own image* opens the picker (`GetContent`), as the row used to. Picking a photo, from either, replaces the screen with the fit step (`WallpaperScreen` drops the list when a photo is being framed, so Back from the fit step lands on the wallpaper screen). Tapping a daily photo downloads its original (usually above 1080p; the 640 px thumbnails are only for the grid), `WallpaperViewModel.onPickedBitmap(photo, credit)` hands it to the same fit step and set-and-store path as a chosen image, and the feed's copyright text is shown exactly as given as a small footer on the photo while it is framed (`WallpaperUiState.credit`; never part of the saved crop). The other two ways, the screenshot and Android's own picker, are separate rows on the wallpaper screen as before.

- **Caching:** `DailyPhotoCache` keeps each feed's JSON (keyed by source and, for Bing, the device country) and its thumbnails under the app's cache folder for the rest of the calendar day it was fetched on, so reopening the screen costs nothing. The first open of a new day fetches again; if that fails, the last cache is shown with a note. An original is never cached.
- **Network:** this is the app's second network use (after weather). Only the opened tab is fetched, never in the background; addresses must be https on `peapix.com` hosts (`isAllowedImageUrl`). The site's privacy policy and the Play data safety answers cover it.
- **Progress:** setting a wallpaper (or storing a copy, or reading a large photo, or downloading a daily photo) takes a few seconds, so `BlockingProgressOverlay` covers the screen with a spinner and a message (*Setting your wallpaper…*, *Saving…*, *Opening the photo…*, *Downloading the photo…*) and takes every touch and the Back button until it is done.
- **Fit step gestures:** the pan and zoom handler keeps the latest frame in a holder updated by every event (`LatestFit`) and is not keyed on the frame, because touch events can arrive faster than recompositions and a stale frame lost all but the last of them.

## Not built yet

- Icon glass on API 33+ uses the lens only when the icon shape is corner-based; other shapes get blur and fill only.
- A stale copy (system wallpaper changed since it was stored) is not drawn: `WallpaperBackdropViewModel` re-reads the system id on every resume and exposes no copy, so glass is the tint until the user picks again.

## Tests

`WallpaperCopyRepositoryTest`, `SetWallpaperFromPhotoUseCaseTest`, `WallpaperViewModelTest`, `WallpaperBackdropViewModelTest`, `GlassBackgroundPromptViewModelTest`, `UseScreenshotAsWallpaperCopyUseCaseTest`, `WallpaperFitTest` (JVM);
`DailyPhotosViewModelTest`, `DailyPhotoRepositoryTest`, `PeapixFeedTest` (JVM); `DailyPhotosScreenTest`, `WallpaperScreenTest`, `WallpaperCaptureScreenTest`, `GlassBackgroundRowTest`, `WallpaperBackdropHostTest` (emulator). `ImageDecoder` and the blur and lens effects are not covered on the JVM; they were checked on the API 36 emulator.
