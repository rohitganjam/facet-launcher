package com.facetlauncher.app.data.model

/**
 * Which Android user an [AppInfo]/[WidgetProviderOption] belongs to, for *display* purposes only
 * (which tab it shows under, which badge it gets) — the primary user, a managed Work Profile,
 * (Android 15+) a Private Space, or [OTHER] for any other non-primary profile we can't positively
 * identify (an OEM dual-app/clone profile, or *any* non-primary profile at all below API 35,
 * where there's no API to ask). This is deliberately NOT used as an identity key anywhere
 * (favoriting, docking, launch routing, widget binding, uninstall cleanup) — two distinct real
 * profiles can land on the same value here (e.g. a genuine Work Profile and a clone profile both
 * classify as [OTHER] pre-35), so [AppInfo.userHandle] is the real identity; this enum is purely
 * "how should this look in the UI."
 */
enum class AppProfile {
    PERSONAL,
    WORK,
    PRIVATE,
    OTHER,
}
