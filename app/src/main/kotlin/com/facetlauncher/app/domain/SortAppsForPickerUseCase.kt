package com.facetlauncher.app.domain

import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.SortDirection
import java.text.Collator
import javax.inject.Inject

/**
 * Orders the app picker screens' (Favorites/Dock/Folder) not-yet-selected app list by
 * [AppSortOption] — never called on the already-selected section, which stays in its own frozen
 * placement order regardless of sort. [SortDirection.ASCENDING] is always the natural order of
 * the comparable value (A→Z, oldest first); [SortDirection.DESCENDING] reverses it, so the same
 * toggle works no matter which field is selected.
 */
class SortAppsForPickerUseCase @Inject constructor(private val usageStatsRepository: UsageStatsRepository) {
    suspend operator fun invoke(apps: List<AppInfo>, option: AppSortOption, direction: SortDirection): List<AppInfo> {
        if (option == AppSortOption.LAST_USED) return sortByLastUsed(apps, direction)

        val collator = Collator.getInstance()
        val ascending = when (option) {
            AppSortOption.ALPHABETICAL -> apps.sortedWith(Comparator { a, b -> collator.compare(a.label, b.label) })
            AppSortOption.INSTALL_DATE -> apps.sortedBy { it.firstInstallTime }
            AppSortOption.LAST_UPDATED -> apps.sortedBy { it.lastUpdateTime }
            AppSortOption.LAST_USED -> apps // unreachable — guarded above
        }
        return if (direction == SortDirection.ASCENDING) ascending else ascending.reversed()
    }

    private suspend fun sortByLastUsed(apps: List<AppInfo>, direction: SortDirection): List<AppInfo> {
        val lastUsed = usageStatsRepository.getLastUsedTimestamps()
        val (used, neverUsed) = apps.partition { lastUsed.containsKey(it.packageName) }
        val orderedUsed = used.sortedBy { lastUsed.getValue(it.packageName) }
        // "Never used" always sorts after "used," regardless of direction — it isn't a
        // meaningful ascending/descending position, just the lowest priority. Their own relative
        // order (already alphabetical, from the installed-app list) is left as-is.
        return (if (direction == SortDirection.ASCENDING) orderedUsed else orderedUsed.reversed()) + neverUsed
    }
}
