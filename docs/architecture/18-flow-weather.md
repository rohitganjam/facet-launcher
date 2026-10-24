# 18 — Flow: weather on the clock

In progress; the build plan and the decisions are in `IMPLEMENTATION_PLAN.md` ("Weather on the clock").
This page is filled in as each part lands, and every statement here describes what is built, not what is planned.
Built so far: the network permission, the MET client and parser, the refresh gate and repository, the Room store,
the settings and facet fields, location, place names and the city list code, and the domain that turns stored hours into
what the UI shows. Not built yet: the bundled city list file, the accessory, the sheet and the settings screens.

## 1. Network and permissions

- `INTERNET` is declared in `AndroidManifest.xml` and used only by the weather request to MET Norway.
  Nothing touches the network, location or the geocoder while weather is off.
- The `verifyInternetPermission` Gradle task (part of `check` and the release build) fails if the merged
  manifest of either variant loses the permission, for example through a stray `tools:node="remove"`.
- `ACCESS_COARSE_LOCATION` was already declared for Wi-Fi automation (docs 15); weather requests it on its own
  and never needs fine or background location.

## 2. The request (`data/weather/MetWeatherApi`)

One `GET` to `https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=…&lon=…[&altitude=…]` with the
coordinates rounded to two decimals (`WeatherPlace.rounded`; MET accepts four). The `User-Agent` is
`FacetLauncher/<versionName> github.com/rohitganjam/facet-launcher`, since MET requires an app name and a way to reach the
developer and blocks requests without one. When a `Last-Modified` came back last time it goes out as `If-Modified-Since`.
The answer is turned into a `WeatherApiResult`:

| HTTP | Result |
|---|---|
| 200 | `Success(forecast, expiresAt, lastModified)`; an unreadable body is `Failed(INVALID_RESPONSE)` |
| 304 | `NotModified(expiresAt)` |
| 429 | `Failed(RATE_LIMITED, retryAt)` — `retryAt` from `Retry-After` (seconds or an HTTP date) when sent |
| 403 | `Failed(BLOCKED)` (MET refuses the `User-Agent`) |
| other, or a thrown `IOException` | `Failed(SERVER)` or `Failed(OFFLINE)` |

`parseMetForecast` reads the `compact` JSON: hourly points for the first couple of days, then every six hours. Every
`next_*` block is optional (the six-hourly points have no `next_1_hours`, the last points have none), so `precipitationMm`
and `symbolCode` can be `null`; a point with no temperature or an unreadable time is skipped.

## 3. The refresh gate (`WeatherRefreshPolicy`, `WeatherRepository`)

Every refresh goes through `WeatherRepository.refresh(place)`, one at a time, so the Home resume, the sheet's button and a
changed location cannot hammer MET. `WeatherRefreshPolicy.decide` allows a request only when none of these hold:

1. a rate limit is being waited out (`blockedUntil`), which also holds back a new place;
2. the place is the same and `Expires` has not passed (`UpToDate`);
3. the place is the same and the last attempt, successful or not, was under 10 minutes ago (`TooSoon`).

A different place skips 2 and 3, and its stored hours are cleared before the request so another place's weather never
shows. Nothing is retried. After a 429 the call fails with `Busy(until)`: `until` is `Retry-After`, or 30 minutes when
MET sends none, and the cached forecast keeps showing; the UI is to say "try again later". Offline and server errors
fail once and wait for the next trigger.

The fetch state (`WeatherFetchState`) is persisted, so a killed process does not reset the gate.

## 4. Stored hours and the Room store

`WeatherRepository` merges each answer into the stored rows (`mergeStoredHours`): the new points replace everything from
their first time on, earlier hours stay as last seen, and rows older than two days go. Those earlier hours are what lets
today's low be known after it has passed. `RoomWeatherStore` keeps them in `weather_hours` and the fetch state in the
single `weather_meta` row (`docs/02`), replacing the hours in one transaction.

## 5. Settings and facet fields

The launcher-wide options are DataStore keys (`docs/02 §5`): the accessory switches `show_next_alarm`,
`show_battery_percentage`, `show_weather`, the four units, the location mode and the chosen city. The units, mode and city
are never per facet. The three switches have a per-facet value on `Facet` (`clockShowAlarm`, `clockShowBattery`,
`clockShowWeather`) that applies while the facet overrides the clock (`overrideClock`), resolved in
`resolveFacetStyle` into `ResolvedClockStyle.showAlarm/showBattery/showWeather`. Home already uses the alarm and battery
switches: `HomeScreenStateBuilder` drops the alarm or the battery from the accessory row when its switch is off.
Backup carries all of these except the current location and the cache (`docs/09 §4`).

## 6. Where it is for (`LocationRepository`, `RefreshWeatherUseCase`)

`RefreshWeatherUseCase` is the one entry point for a refresh; Home on resume and the sheet's button are to call it only while
weather is shown, so with weather off no location is read, no geocoder is asked and no request is made.

- **Current location** (the default): `LocationRepository` takes one approximate fix (the network provider's current
  location with a 10-second timeout, else the last known), only with `ACCESS_COARSE_LOCATION` or fine, and never
  in the background. Without the permission the result is `NeedsLocationPermission`; with the permission but no fix and no
  earlier place it is `NoLocation`. A stored place is kept while the device is within 5 km of it, so walking around does
  not refetch; a new one is rounded to two decimals, with the fix's altitude when it has one (an altitude of exactly 0
  counts as unknown).
- **Chosen city:** the city's coordinates, rounded, with no permission and no location read; no city is `NoCity`.

## 7. Names and the city list (`data/places`)

`ResolvePlaceUseCase` names a position: the device's geocoder (`AndroidPlaceGeocoder`, `Geocoder` in the user's locale; the
listener API from Android 13, the blocking one before) answers first, with the city (`locality`, then `subAdminArea`, then
`adminArea`); otherwise the nearest city of the bundled list is used, and the UI shows it as "Near X". The name is stored with
its source (`PlaceNameSource`) in the fetch state and dropped when the place changes; a name that came from the list is
asked for again on the next refresh in case the geocoder works by then. The nearest listed city (in the geocoder's country
when it gave one, within 300 km) also supplies the IANA timezone.

`CityRepository` reads `assets/cities.tsv` once (one tab-separated row per city: `name, admin, countryCode, latitude,
longitude, population, timezone`). A missing or unreadable asset is an empty list, so weather works without it, with no
names from the list and no offline picker. `CityList` searches by name (starts with, then a word starts with, then
contains, ignoring case and accents, the larger city first) and finds the nearest city. `SearchPlacesUseCase` is the
picker's search: geocoder results first (each with the nearest listed city's zone, else the device's), then list matches that
are not within 10 km of a geocoder result.

## 8. What the UI reads (`ObserveWeatherUseCase`)

`WeatherState` is derived from the stored hours, the fetch state and the settings, and re-derived every five minutes:
the current hour, up to 24 hourly points (six-hourly points are left out), local days from today on (`dailySummaries`,
in the device's zone, or the chosen city's, so a day's boundaries and today's low and high include the hours stored earlier in
the day), the rain summary (`summarizeRain`: wet hours of at least 0.2 mm, spells joined across up to two dry hours, the
wettest spell or the current one when it holds at least half as much, intensity from the wettest hour: light under 2.5 mm,
moderate under 7.6 mm, else heavy), the place name, the zone, and when it was fetched. A forecast fetched more than three hours
ago is `isOutdated`: the accessory hides and the sheet says how old it is. `WeatherUnits` resolves each unit option against
the locale (`AUTO`) and converts from the feed's units.

## 9. Limits

When MET answers HTTP 429 (over the limit) or 403 (a blocked `User-Agent`), the call fails once: no retry loop.
The state becomes "Weather is busy, try again later", the cached forecast keeps showing, and a `Retry-After`
header, when sent, sets the earliest next attempt.
