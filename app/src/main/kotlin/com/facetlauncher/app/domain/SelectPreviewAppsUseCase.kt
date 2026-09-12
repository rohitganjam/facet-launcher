package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject

/**
 * Picks which of the device's real installed apps back Settings → Appearance's live preview card
 * ([com.facetlauncher.app.ui.settings.AppearancePreviewCard]). Rather than an arbitrary
 * alphabetical slice of [GetInstalledAppsUseCase]'s output, this leads with three tiers, most
 * to least preferred:
 *  1. [preferredPackages] — this device's own actual default-app picks for a few common
 *     categories (browser/messaging/camera/mail/phone), resolved by
 *     [com.facetlauncher.app.data.DefaultAppRepository] via [android.content.pm.PackageManager]
 *     rather than a guess at package names;
 *  2. [FALLBACK_CATEGORY_PACKAGES] — a hardcoded guess at common package names per category,
 *     used only where (1) came up empty for that category (no default chosen, or
 *     [com.facetlauncher.app.data.DefaultAppRepository]'s own OS query genuinely found nothing);
 *  3. [ALWAYS_CONSIDERED_PACKAGES] — Play Store and Gmail specifically, near-ubiquitous apps
 *     worth surfacing even though they aren't a category "default."
 * All three tiers are deduplicated against each other and filtered down to whatever's actually
 * present in [installedApps] — an entry that isn't installed is simply skipped. Whatever count
 * isn't filled by those tiers is backfilled from [installedApps]' own remaining apps in their
 * original order — the plain fallback this preview used exclusively before any of this existed.
 */
class SelectPreviewAppsUseCase @Inject constructor() {
    operator fun invoke(installedApps: List<AppInfo>, preferredPackages: List<String>, count: Int): List<AppInfo> {
        val byPackage = installedApps.associateBy { it.packageName }
        val osPreferred = preferredPackages.mapNotNull(byPackage::get)
        val fallbackPreferred = FALLBACK_CATEGORY_PACKAGES.mapNotNull { candidates -> candidates.firstNotNullOfOrNull(byPackage::get) }
        val alwaysConsidered = ALWAYS_CONSIDERED_PACKAGES.mapNotNull(byPackage::get)
        val preferred = (osPreferred + fallbackPreferred + alwaysConsidered).distinctBy { it.packageName }
        val preferredPackageNames = preferred.mapTo(mutableSetOf()) { it.packageName }
        val remaining = installedApps.filterNot { it.packageName in preferredPackageNames }
        return (preferred + remaining).take(count)
    }

    private companion object {
        // One guess per category, same order as DefaultAppRepository's own intent resolution —
        // only ever fills a category the OS-resolved list didn't already cover.
        val FALLBACK_CATEGORY_PACKAGES = listOf(
            listOf("com.android.chrome", "com.sec.android.app.sbrowser", "org.mozilla.firefox"), // Browser
            listOf("com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms"), // Messaging
            listOf("com.google.android.GoogleCamera", "com.sec.android.app.camera", "com.android.camera2"), // Camera
            listOf("com.google.android.gm"), // Mail
            listOf("com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer"), // Phone
        )
        val ALWAYS_CONSIDERED_PACKAGES = listOf("com.android.vending", "com.google.android.gm") // Play Store, Gmail
    }
}
