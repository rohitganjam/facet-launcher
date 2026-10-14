package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget

/**
 * Pre-fills the shared dock with this device's own default browser/messaging/camera/mail/phone
 * apps (whichever resolve and are installed), so a fresh install's Home isn't empty even if the
 * user force-quits partway through onboarding. Runs once — guarded by
 * [com.facetlauncher.app.data.model.LauncherSettings.defaultsSeeded] — and unconditionally from
 * [com.facetlauncher.app.ui.launcher.LauncherViewModel.init], not gated on the onboarding UI
 * itself being shown or completed.
 */
class SeedDefaultDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val defaultAppRepository: DefaultAppRepository,
    private val placementRepository: PlacementRepository,
    private val getInstalledApps: GetInstalledAppsUseCase,
) {
    suspend operator fun invoke() {
        if (settingsRepository.settings.first().defaultsSeeded) return
        val installed = getInstalledApps()
        defaultAppRepository.getDefaultAppPackages()
            .mapNotNull { packageName -> installed.firstOrNull { it.packageName == packageName } }
            .take(AppListLimits.MAX_DOCK_APPS)
            .forEachIndexed { position, app -> placementRepository.addApp(PlacementTarget.GlobalDock, app, position) }
        settingsRepository.setDefaultsSeeded(true)
    }
}
