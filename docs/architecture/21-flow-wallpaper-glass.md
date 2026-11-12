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
| `WallpaperBackdropHost` + `LocalWallpaperBackdrop` | `ui/components/WallpaperBackdrop.kt` | Around `FacetNavHost` in `LauncherActivity`. Draws the copy as the bottom layer, marks it as the Backdrop library's backdrop, provides it to the panels. |
| `Modifier.glassBackground(..., sampleWallpaper)` | `ui/components/GlassBackground.kt` | With a backdrop, a panel is drawn through `drawBackdrop` (blur, brightness and saturation per style, plus the tint and edge); without one it is the plain tint. The dock, apps list rows and grid tiles, calendar block, media row and native widgets opt in; icon backgrounds and the settings previews keep the tint. |
| `clearGlassLens` | `ui/components/ClearGlassLens.kt` | A copy of the library's rounded-rectangle lens shader (Apache-2.0, Kyant) with the depth pull as a strength. Used by Clear glass and Dark glass. |
| `WallpaperScreen` / `WallpaperViewModel` | `ui/settings/` | Settings → Change wallpaper (route `wallpaper`). |
| `WallpaperFit` | `ui/settings/` | The fit step's pan and zoom maths (`FitGeometry`, `WallpaperFit`), pure and tested. |

## The screen

*Change wallpaper* opens `WallpaperScreen`, which shows the copy (or a stand-in for the system wallpaper) and a status line, then two equal choices:

1. **Choose an image for Facet.** The system Photo Picker (no permission) → the **fit step**: the photo in a screen-shaped window, drag to move, pinch to zoom
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
| 31–32 | Blur, brightness and saturation; the lens does nothing, so Clear glass and Dark glass are sharp tinted panels. |
| 33+ | Everything, including the lens. |

## Prompts

- **First use.** `GlassBackgroundRow` (all five background pickers: dock, apps list, calendar, media row, native widgets) applies the chosen style and, for any style but Off, shows a sheet when `GlassBackgroundPromptViewModel.needsCopy` (no usable copy, not dismissed): *Choose an image* (opens the wallpaper screen) or a remembered *Not now* (`wallpaper_prompt_dismissed`). The state reaches the rows through `LocalGlassBackgroundPrompt`, provided by `FacetNavHost`; without it (previews, tests) there is no prompt.
- **After Android's picker.** *Set a system wallpaper* records the wallpaper id first. Coming back with a different id and no matching copy, `WallpaperViewModel` shows an offer card (*Choose an image* / *Not now*, the same remembered flag). Facet cannot read the new wallpaper, so the offer asks for the same image.

## Previews

`WallpaperRepository.currentHomeWallpaper()`, the one source of every preview's background (facet carousel cards, the Appearance, dock, list, drawer and notification previews), returns the saved copy first when it still matches the system wallpaper, then falls back to what Android lets it read.

## Real glass on icons

An icon's glass background samples the copy only inside live Home (`LocalIconGlassSamplesWallpaper`, provided in `HomeDrawerRoute`); the drawer, settings, previews and pickers keep the tint.

## Not built yet

- The *Use my current wallpaper* (screenshot) path.
- Icon glass on API 33+ uses the lens only when the icon shape is corner-based; other shapes get blur and fill only.
- A stale copy (system wallpaper changed since it was stored) is not drawn: `WallpaperBackdropViewModel` re-reads the system id on every resume and exposes no copy, so glass is the tint until the user picks again.

## Tests

`WallpaperCopyRepositoryTest`, `SetWallpaperFromPhotoUseCaseTest`, `WallpaperViewModelTest`, `WallpaperBackdropViewModelTest`, `GlassBackgroundPromptViewModelTest`, `WallpaperFitTest` (JVM);
`WallpaperScreenTest`, `GlassBackgroundRowTest` (emulator). `ImageDecoder` and the blur and lens effects are not covered on the JVM; they were checked on the API 36 emulator.
