package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/** How the app picker screens' not-yet-selected app list is ordered — see [SortAppsForPickerUseCase]. */
enum class AppSortOption(@param:StringRes val labelRes: Int) {
    ALPHABETICAL(R.string.app_sort_alphabetical),
    LAST_USED(R.string.app_sort_last_used),
    INSTALL_DATE(R.string.app_sort_installed_date),
    LAST_UPDATED(R.string.app_sort_last_updated),
}

enum class SortDirection {
    ASCENDING,
    DESCENDING,
}
