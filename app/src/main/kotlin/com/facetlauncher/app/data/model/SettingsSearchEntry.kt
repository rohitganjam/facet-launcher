package com.facetlauncher.app.data.model

/** One system Settings deep link surfaced by the Drawer's "Search settings" section — [action] is an `android.provider.Settings.ACTION_*` string. */
data class SettingsSearchEntry(
    val id: String,
    val label: String,
    val action: String,
    /** Other words that find it ("internet" for Wi-Fi). */
    val keywords: List<String> = emptyList(),
)
