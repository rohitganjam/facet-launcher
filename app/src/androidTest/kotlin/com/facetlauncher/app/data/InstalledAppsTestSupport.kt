package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile

/**
 * Installed apps in the personal profile, one per package. A Work Profile on the test device repeats an app's package, so the
 * work copies are left out (their picker rows carry a `_work` tag suffix) and a test's `onNodeWithTag("..._row_<package>")`
 * matches the personal row only.
 */
suspend fun AppRepository.getTestableApps(): List<AppInfo> =
    getInstalledApps().filter { it.profile == AppProfile.PERSONAL }.distinctBy { it.packageName }
