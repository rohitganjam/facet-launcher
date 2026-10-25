# Facet Launcher 0.1.30

## New Features
- **Weather on your clock:** The current weather icon and temperature can now appear next to the alarm and battery under the clock. Tap it to see a 24-hour forecast, current details and the outlook for the coming days. Forecasts come from MET Norway.
- **Clock accessories settings:** A new settings screen lets you turn the alarm, battery and weather items on or off and pick temperature units. You can set these for the whole launcher or separately for each facet. It opens with a live preview of your clock that updates as you change settings.
- **Weather location:** Weather can use your current location, or you can search for a city. City search works offline from a built-in list of more than 34,000 cities.
- **Saved cities:** Save up to 10 cities and switch between them. Pick the active one with a single tap, and remove any with the trash icon. If you leave city mode without choosing a city, the app asks whether to turn weather off, use your current location, or keep choosing.
- **Coloured weather icons:** Weather icons can now be shown in colour as well as the default single colour.
- **App drawer preview:** App drawer settings now show a live preview of your drawer. It's sized like your phone's screen, and you can drag it to scroll through sample apps.
- Your weather settings and saved cities are now included in backups.

## Fixes
- Folders in the app drawer grid are now centred in their space, like app icons.
- The Appearance settings preview and the facet carousel now show the alarm, battery and weather row under the clock. Before, it was missing.

## Improvements
- The "Airy" grid density in the app drawer is now called "Spacious" and matches the item size used in list view. Existing settings and older backups still work.
- The app drawer grid always shows at least two rows, so you can still see the density you picked when there isn't much room.
- The alarm, battery and weather icons under the clock are now all the same size, and the weather item's tap area matches the others.
- Icons under the clock now have the same drop shadow as the text next to them.
- Weather appears faster after you unlock or open the launcher. It no longer waits for a fresh location fix.
- The Location option on the weather screens now opens the app's Permissions settings, and the Location permission there now says it's used for weather.
- Small date and accessory text in clock styles now has a minimum size, so it's easier to read.
- By default, the clock area now takes up about 40% of the screen, so the divider and app list start a little lower. Positions you've already set stay the same.
- The style gallery shows a sample of the weather item when weather is on.

## Internal
- Declare INTERNET permission (weather only); replace `verifyNoInternetPermission` with `verifyInternetPermission`; update Play disclosures
- MET Locationforecast client, parser, refresh policy (Expires, 10-minute interval, rate-limit block) and WeatherRepository, with tests
- LocationRepository, CityList/CityRepository, PlaceGeocoder, ResolvePlaceUseCase, SearchPlacesUseCase, RefreshWeatherUseCase, ObserveWeatherUseCase, WeatherUnits, day/rain summaries; place name moved into fetch state
- Bundle GeoNames cities15000 as `assets/cities.tsv` (CC BY 4.0) via `scripts/gen-cities.py`, with test against the real file
- Room `weather_hours`/`weather_meta` tables behind RoomWeatherStore; schema v33 with `MIGRATION_32_33` and test
- Clock accessory switches as launcher defaults and per-facet values following `overrideClock`; Override seeds the three switches
- Saved cities stored as a single DataStore entry
- Drawer grid tiles: drop the wrapping Box, sheets live inside the Column
- Drawer settings preview card sizing iterations (350dp → 380dp)
- Docs, architecture docs, CLAUDE.md and plan updated
- Regenerate graphify-out (×4)
- Release 0.1.29
