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
device*; purely local, on-device processing (reading calendar, contacts, usage stats, media
session info, Bluetooth connection state or the Wi-Fi network name to render UI or evaluate a
rule, never stored beyond that or sent anywhere) does not meet that bar. This is a stronger,
more literally true claim than most apps can make, since it's not just policy — it's
structurally impossible given the manifest.

If Console's flow forces you to itemize data types anyway (some versions of the form ask you
to walk through categories even when the answer is "not collected" for all of them), the
per-permission text below explains *why* each one is accessed, for use in whichever field asks.

## Notification access (powers app-icon badges and the home media player)

> Facet Launcher is a home-screen launcher app. It uses Notification Listener access for two
> standard launcher features. First, app icon badges — a small dot or count on an app's icon
> showing unread notifications (present in Pixel Launcher, Samsung One UI Home, and most
> third-party launchers). Android does not expose a public, non-restricted API for a launcher to
> read per-app unread counts, so listener access is required; the app reads only the source
> package and a count. Second, an optional media player row on the home screen. Android only
> lets an app read active media sessions (title, artist, album art, playback state, and the
> transport controls) through a granted notification listener, and the app also checks whether
> the player's own media notification is still showing, so the row disappears when you dismiss
> it. The app never reads the text of other apps' notifications. Everything is processed
> locally to draw the screen and is never stored beyond that or transmitted off-device.

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

## Bluetooth — `BLUETOOTH_CONNECT` (runtime permission, requested only when needed)

> Used only by the optional facet automation rules. When the user creates a rule triggered by
> a Bluetooth device, the app lists their paired devices to choose from and notices when the
> chosen device connects or disconnects, so it can switch facets. Android asks for the
> permission at that moment; refusing keeps the previous trigger. The chosen device is saved
> on the device inside that rule and is never transmitted.

## Location — `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (runtime permission, requested only when needed)

> Used only to read the name (SSID) of a Wi-Fi network, which Android makes available to apps
> only if they hold a location permission. It is requested only when the user creates a facet
> automation rule for a *named* Wi-Fi network, to list networks in range and to notice when the
> device connects to the chosen one. The app never reads, stores or transmits the device's
> location. A rule for "any Wi-Fi network" does not need it.

## Network and Wi-Fi state — `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` (normal permissions)

> Lets Wi-Fi automation rules tell whether the device is currently connected to Wi-Fi. These
> permissions do not give the app internet access, and the app declares no `INTERNET`
> permission.

## Status bar — `EXPAND_STATUS_BAR` (normal permission)

> Lets a swipe-down gesture on the home screen open the notification shade, as on any home
> screen. No data is read.

## Hidden profiles — `ACCESS_HIDDEN_PROFILES` (normal permission)

> Lets the launcher list apps in Android's Private Space, in its own tab, once the user has
> unlocked it. Standard for launchers that support Private Space.

## In-app purchase (Google Play Billing)

> The app offers an optional one-time purchase. Payment, receipts and refunds are handled
> entirely by Google Play: the app asks the Play Store app on the phone whether the Google
> account owns the purchase and stores only that yes/no entitlement on the device. The app has
> no server and declares no `INTERNET` permission. Declare in-app purchases in Play Console
> as usual; Google processes the purchase data, not the app.

## Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny)

> Powers an optional home-screen calendar strip showing upcoming events (from today until noon
> tomorrow), enabled by the user in Settings. Calendar data is read directly from the device's calendar provider and
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
