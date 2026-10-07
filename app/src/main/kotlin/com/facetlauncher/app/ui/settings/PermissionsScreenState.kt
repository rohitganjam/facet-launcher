package com.facetlauncher.app.ui.settings

/** One row on the Permissions screen. */
enum class PermissionKind {
    CALENDAR,
    CONTACTS,
    USAGE_ACCESS,
    NOTIFICATION_ACCESS,
    BLUETOOTH,
    LOCATION,
}

data class PermissionRowState(
    val kind: PermissionKind,
    val title: String,
    val subtitle: String,
    val isGranted: Boolean,
    /**
     * Only meaningful for the runtime-permission rows ([PermissionKind.CALENDAR], [PermissionKind.CONTACTS],
     * [PermissionKind.BLUETOOTH], [PermissionKind.LOCATION]) — whether the app
     * has already asked the system for this permission at least once. `checkSelfPermission`
     * alone can't tell "never asked" apart from "the user said no for good"; combined with
     * [android.app.Activity]'s own `shouldShowRequestPermissionRationale` at the call site, this
     * is what lets the row decide between firing a real request and sending the user to this
     * app's own system App Info screen instead.
     */
    val hasRequestedBefore: Boolean = false,
)

data class PermissionsUiState(
    val permissions: List<PermissionRowState> = emptyList(),
)
