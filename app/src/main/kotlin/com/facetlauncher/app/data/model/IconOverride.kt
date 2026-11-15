package com.facetlauncher.app.data.model

/** Where an app's custom icon comes from. */
enum class IconOverrideSource {
    /** A drawable from an installed icon pack. */
    PACK,

    /** The app's own icon, even while an icon pack is applied. */
    OWN,
}

/**
 * One app's custom icon, launcher-wide. [userId] is the profile's `UserHandle.hashCode()`, as in placements, or
 * [UNRESOLVED_USER_ID] for a restored row whose profile wasn't found; that row then matches by [profile].
 */
data class IconOverride(
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val profile: AppProfile,
    val source: IconOverrideSource,
    val packPackage: String? = null,
    val drawableName: String? = null,
    /** A pack's day-of-month prefix for a calendar app (`calendar_` + `14`); [drawableName] is its plain fallback. */
    val calendarPrefix: String? = null,
    /** File name of the PNG drawn when the pack is gone, under the overrides folder. */
    val snapshotFile: String? = null,
    val updatedAt: Long = 0L,
) {
    companion object {
        const val UNRESOLVED_USER_ID = -1
    }
}

/**
 * Which override each app takes, by [AppInfo.listKey]: its exact activity first, then a restored row for its profile,
 * then, when the app has a single launcher activity, the one row kept for its package (an update renamed the activity).
 */
fun List<IconOverride>.matchApps(apps: List<AppInfo>): Map<String, IconOverride> {
    if (isEmpty()) return emptyMap()
    val exact = associateBy { "${it.packageName}/${it.activityName}/${it.userId}" }
    val unresolved = filter { it.userId == IconOverride.UNRESOLVED_USER_ID }.associateBy { "${it.packageName}/${it.activityName}/${it.profile}" }
    val byPackage = groupBy { "${it.packageName}/${it.userId}" }
    val activitiesPerPackage = apps.groupingBy { "${it.packageName}/${it.userHandle.hashCode()}" }.eachCount()
    val launchers = apps.mapTo(HashSet()) { "${it.packageName}/${it.activityName}/${it.userHandle.hashCode()}" }
    return buildMap {
        apps.forEach { app ->
            val userId = app.userHandle.hashCode()
            val packageKey = "${app.packageName}/$userId"
            val match = exact["${app.packageName}/${app.activityName}/$userId"]
                ?: unresolved["${app.packageName}/${app.activityName}/${app.profile}"]
                ?: byPackage[packageKey]?.singleOrNull()?.takeIf { stale ->
                    activitiesPerPackage[packageKey] == 1 && "${stale.packageName}/${stale.activityName}/${stale.userId}" !in launchers
                }
            if (match != null) put(app.listKey, match)
        }
    }
}
