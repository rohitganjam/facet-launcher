package com.facetlauncher.app.data.model

import androidx.compose.runtime.Immutable

/** One release's in-app "What's new" entry: user-facing bullets only, newest release first in the list the screen shows. */
@Immutable
data class ReleaseNotes(
    val version: String,
    val newFeatures: List<String>,
    val improvements: List<String>,
)
