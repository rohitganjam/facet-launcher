package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.domain.GroupAppsByLetterUseCase
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import javax.inject.Inject

/** The pure use cases [AppDrawerScreen] applies to its `apps` parameter, injected into `DrawerViewModel` and passed down as a parameter. */
class DrawerListUseCases @Inject constructor(
    val rank: RankBySearchRelevanceUseCase,
    val group: GroupAppsByLetterUseCase,
    val recentlyInstalled: RecentlyInstalledAppsUseCase,
)
