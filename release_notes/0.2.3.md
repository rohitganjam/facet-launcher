# Facet Launcher 0.2.3

## New Features
- New "Upload a screenshot" option on the Wallpaper screen that opens your gallery so you can use an image you already have.

## Fixes
- The "wallpaper changed" offer no longer stays on screen over a captured wallpaper. It now appears only when no saved copy matches your current wallpaper.
- The wallpaper capture shortcut now appears again after you change your wallpaper, instead of only once per launch, and it no longer reappears when Home becomes ready again.
- The capture shortcut is no longer shown while a capture is already in progress.

## Improvements
- Tapping "Capture my current wallpaper" now goes straight to Android's screen-sharing prompt, skipping the extra dialog.

## Internal
- Wallpaper capture offers are now keyed by view and system wallpaper id, dropped once shown, and held back while `ScreenCaptureRepository.capturing` is true (until frames are stored). Pill duration stays 5 seconds.
- Offer card visibility is now derived from whether a copy matches the current wallpaper, replacing the sticky flag cleared only by its own buttons.
- Removed strings for the old capture dialog and added the new row strings in all locales.
- Release 0.2.2 (48): in-app What's new, generated release notes and recorded build commit.
- Folded the unshipped 0.2.2 What's new notes into 0.2.3.
