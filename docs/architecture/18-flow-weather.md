# 18 — Flow: weather on the clock

In progress; the build plan and the decisions are in `IMPLEMENTATION_PLAN.md` ("Weather on the clock").
This page is filled in as each part lands, and every statement here describes what is built, not what is planned.
Built so far: the network permission, the MET client and parser, the refresh gate and repository, the Room store,
and the settings and facet fields. Not built yet: location, place names, the accessory, the sheet and the settings screens.

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

## 6. Limits

When MET answers HTTP 429 (over the limit) or 403 (a blocked `User-Agent`), the call fails once: no retry loop.
The state becomes "Weather is busy, try again later", the cached forecast keeps showing, and a `Retry-After`
header, when sent, sets the earliest next attempt.
