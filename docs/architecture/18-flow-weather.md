# 18 — Flow: weather on the clock

In progress; the build plan and the decisions are in `IMPLEMENTATION_PLAN.md` ("Weather on the clock").
This page is filled in as each part lands, and every statement here describes what is built, not what is planned.

## 1. Network and permissions

- `INTERNET` is declared in `AndroidManifest.xml` and used only by the weather request to MET Norway.
  Nothing touches the network, location or the geocoder while weather is off.
- The `verifyInternetPermission` Gradle task (part of `check` and the release build) fails if the merged
  manifest of either variant loses the permission, for example through a stray `tools:node="remove"`.
- `ACCESS_COARSE_LOCATION` was already declared for Wi-Fi automation (docs 15); weather requests it on its own
  and never needs fine or background location.

## 2. Limits

When MET answers HTTP 429 (over the limit) or 403 (a blocked `User-Agent`), the call fails once: no retry loop.
The state becomes "Weather is busy, try again later", the cached forecast keeps showing, and a `Retry-After`
header, when sent, sets the earliest next attempt.
