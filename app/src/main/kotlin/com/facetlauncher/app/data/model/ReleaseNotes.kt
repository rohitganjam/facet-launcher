package com.facetlauncher.app.data.model

import androidx.compose.runtime.Immutable

/** The `coachMarksSeen` id of one version's What's new announcement: present once the user has seen it, or never needed to (a fresh install). */
fun whatsNewSeenId(version: String): String = "WHATS_NEW_$version"

/** One release's in-app "What's new" entry: user-facing bullets only, newest release first in the list the screen shows. */
@Immutable
data class ReleaseNotes(
    val version: String,
    val newFeatures: List<String>,
    val improvements: List<String>,
)
