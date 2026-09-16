package com.facetlauncher.app.data.model

/** How the app picker screens' not-yet-selected app list is ordered — see [SortAppsForPickerUseCase]. */
enum class AppSortOption(val label: String) {
    ALPHABETICAL("Alphabetical"),
    LAST_USED("Last used"),
    INSTALL_DATE("Installed Date"),
    LAST_UPDATED("Last updated"),
}

enum class SortDirection {
    ASCENDING,
    DESCENDING,
}
