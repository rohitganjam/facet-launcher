# Play Console — sensitive permission disclosures

Reference text for whatever Play Console actually prompts for when you upload the AAB
(a dedicated Permissions Declaration form, the Data Safety section's freeform justification
fields, or both — confirmed by direct doc lookup that `BIND_NOTIFICATION_LISTENER_SERVICE`
and `PACKAGE_USAGE_STATS` are *not* on Google's currently-published list of permissions
requiring the formal Declaration Form + demo video, unlike SMS/Location/Contacts-adjacent
permissions — so this is prepared in case Console asks anyway, per-permission or in Data Safety).

## Data Safety section — top-level answer

**Recommended: "No data collected."**

Facet Launcher has no `INTERNET` permission at all — it cannot transmit anything off-device,
under any circumstance. Play's own definition of "collection" is data *transmitted off the
device*; purely local, on-device processing (reading calendar/contacts/usage stats to render
UI, never stored beyond that or sent anywhere) does not meet that bar. This is a stronger,
more literally true claim than most apps can make, since it's not just policy — it's
structurally impossible given the manifest.

If Console's flow forces you to itemize data types anyway (some versions of the form ask you
to walk through categories even when the answer is "not collected" for all of them), the
per-permission text below explains *why* each one is accessed, for use in whichever field asks.

## Notification access (powers app-icon badges)

> Facet Launcher is a home-screen launcher app. App icon badges — a small count overlaid on
> an app's icon showing unread notifications — are a standard launcher feature (present in
> Pixel Launcher, Samsung One UI Home, and most third-party launchers). Android does not
> expose a public, non-restricted API for a third-party launcher to read per-app unread
> counts, so Notification Listener access is required to compute and display these badges.
> The app reads only notification metadata (source package and count) needed to render the
> badge; notification content is never read, stored, or transmitted off-device.

## Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

> The app drawer includes an optional "Most used" sort mode that ranks installed apps by real
> usage frequency, surfacing frequently-used apps first — a standard organizational feature
> also found in other Android launchers' app drawers and recents-style views. Usage data is
> queried locally via `UsageStatsManager`, aggregated only into a ranking, and never
> transmitted off-device or persisted beyond what's needed to compute the current ranking.

## Uninstall shortcut — `REQUEST_DELETE_PACKAGES`

> Used solely to let the user uninstall an app directly from its long-press context menu on
> the home screen or app drawer — a standard launcher convenience also present in stock
> Android launchers. It triggers the system's own uninstall confirmation dialog; there is no
> silent or background uninstall capability.

## Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny)

> Powers an optional home-screen calendar strip showing upcoming events, enabled by the user
> in Settings. Calendar data is read directly from the device's calendar provider and
> rendered locally; it is never copied, stored separately, or transmitted.

## Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny)

> Powers an optional contact search feature in the app drawer, enabled by the user in
> Settings. Contact data is queried on-device at search time and never stored or transmitted.

---

**Next step to actually resolve the ambiguity**: start the app listing in Play Console and
upload `app-release.aab` — Console scans the manifest and surfaces whichever forms it
actually requires for this specific permission combination. That's more authoritative than
any generic policy doc, since requirements are occasionally permission-combination-specific
and change over time.
