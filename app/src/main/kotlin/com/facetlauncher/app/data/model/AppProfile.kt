package com.facetlauncher.app.data.model

/**
 * Which Android user an [AppInfo]/[WidgetProviderOption] belongs to. Android exposes at most one
 * extra (managed Work) profile alongside the primary user for a launcher's purposes, so a
 * 2-valued enum is a deliberate scope choice, not an oversight — [AppRepository][com.facetlauncher.app.data.AppRepository]
 * and [WorkProfileRepository][com.facetlauncher.app.data.WorkProfileRepository] are the only
 * places that ever hold a real `android.os.UserHandle`; everything above `data/` only ever sees
 * this enum, never the framework type.
 */
enum class AppProfile {
    PERSONAL,
    WORK,
}
