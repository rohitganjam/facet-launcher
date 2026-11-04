package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.upgradeLegacyNativeWidgetConfig

/** Backup version that first wrote widget settings as JSON; older ones wrote tab-separated lines. */
private const val FIRST_JSON_CONFIG_VERSION = 4

/** [this] in the current shape, so the import below this point only ever sees one: older backups get their widget settings rewritten. */
fun BackupBundle.upgradedToCurrent(): BackupBundle {
    if (backupVersion >= FIRST_JSON_CONFIG_VERSION) return this
    return copy(
        backupVersion = CURRENT_BACKUP_VERSION,
        facets = facets.map { it.withUpgradedConfigs() },
        widgetPlacements = widgetPlacements.map { it.copy(nativeWidgetConfig = upgradeLegacyNativeWidgetConfig(it.nativeWidgetConfig)) },
    )
}

private fun BackupFacet.withUpgradedConfigs(): BackupFacet = copy(
    clockWeatherLocation = upgradeLegacyNativeWidgetConfig(clockWeatherLocation),
    nativeStackPages = nativeStackPages.map { it.copy(nativeWidgetConfig = upgradeLegacyNativeWidgetConfig(it.nativeWidgetConfig)) },
)
