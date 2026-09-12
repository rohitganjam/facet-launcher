package com.facetlauncher.app.data.model

/** One system Settings deep link surfaced by the Drawer's "Search settings" section — [action] is an `android.provider.Settings.ACTION_*` string. */
data class SettingsSearchEntry(
    val id: String,
    val label: String,
    val action: String,
)
