# Facet Launcher 0.2.6

## New Features
- Three new clock styles: Echo, Day Line (runs midnight to midnight) and Liquid Hour. They support accent colors and have names in six languages.
- You can long-press anywhere inside the clock's resized area to open the clock's settings. Without a background panel, this used to open the Home settings instead.

## Fixes
- Bluetooth no longer causes problems if the app's Bluetooth permission has been revoked. The app now handles that case.
- With a background panel on a centered clock, calendar events now stay aligned to the left.

## Improvements
- You can now resize the clock to any width and height, in both portrait and landscape. The text scales to fit the tighter side, and there is no longer a maximum size.
- With a background panel, the panel now matches the area you sized the clock to and follows live as you drag. Before, it followed the drawn text.
- The clock style gallery opens with your current style centered.
- Clock accessories are now in the clock's settings card, not in the gallery. You can reach them from Home's widget sheet and from the Widget stack's Configure option.
- The widget sheet options now appear in this order: Adjust, Widget stack, Replace, Remove.

## Internal
- Release 0.2.5 version bump; 0.2.6 version and What's New notes are pending in the working tree.
- Clock: free-shaped resize box with uniform text scale. Long-press hit area is now the sized box. Docs, test registry and IMPLEMENTATION_PLAN updated.
- Bluetooth paired-device and GATT queries catch SecurityException (with tests). IntentCompat replaces deprecated extras reads.
- Instrumented tests now run on HiltTestApplication with a debug-only HiltTestActivity and ProvideViewModels. This works around hilt-navigation-compose 1.4.0 building a Hilt factory on every hiltViewModel(), which throws on a plain ComponentActivity. Also fixed the WallpaperBackdropHost stand-in panel and MediaPlayerRow ink sampling.
- Compose lint: screens resolve their own ViewModel, modifier params follow the compose-rules order, and unused parameters are removed.
- Split the longest composables (AppPickerScreen, ContactConnectionsSheet, FolderContentsSheet, OnboardingScreen, HomeDrawerRoute). Route state, effects, screen model and overlays moved to their own files. LongMethod limit set to 80.
- Detekt baseline trimmed and unneeded suppressions dropped. FacetDestinations, FolderSheetHeaderAction and OnboardingStep moved to their own files. DragReorderState.onDrag and WallpaperRepository.toBitmapOrNull simplified. Widget grab loop now ends via a flag instead of two breaks.
