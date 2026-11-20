# Facet Launcher 0.2.5

## New Features
- Now Playing widget now adapts to its size, with six layouts: a small tile, compact, strip with a progress line, cover card, card, and full player. A 1×1 is always a tile and a 2×2 is always a cover card.

## Fixes
- Fixed the Hub in landscape sliding sideways, which shifted every widget left when you pulled the Hub in.
- Fixed resizing a widget on the landscape Hub: the drag now stops at the widest size that can be saved, so it no longer snaps back when you let go.
- Fixed the landscape Home screen's app list to keep the same right-side margin as the clock's left margin when there is no dock beside it.

## Improvements
- Daily photos now come directly from Microsoft (Bing and Windows Spotlight) and follow your phone's language and region, with fallbacks if a source isn't available.
- Photos you open in Daily photos stay saved for as long as the source still lists them.
- The source and profile switches in Daily photos now have a visible track, making them easier to read.
- The Daily photos subtitle now says "recent photos" instead of "a week".
- Landscape dock icons are now spread from top to bottom, with an end icon lined up with the apps list.

## Internal
- Added `nowPlayingLayoutFor` to select the Now Playing layout from the widget's box size.
- Landscape Hub grid: moved side padding out of the scroll area to prevent spurious horizontal scrolling.
- Hub landscape resize: clamp the drag at the fifth column of the portrait position to match the save validation.
- Replaced the Peapix source with the Bing HPImageArchive API and the Windows Spotlight service, selected by locale with a fallback chain.
- Daily photos: Spotlight requests stop at 8 photos (max 3 calls).
- Release 0.2.4 version bump.
