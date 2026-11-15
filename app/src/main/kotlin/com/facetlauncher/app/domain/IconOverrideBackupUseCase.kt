package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.BackupIconOverride
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import java.util.Base64
import javax.inject.Inject

/**
 * Custom icons in and out of a backup. On restore a row whose profile isn't on this device keeps an unresolved `userId`
 * and matches by profile, and a row whose app isn't installed yet is kept, since apps reinstall for hours after a restore.
 */
class IconOverrideBackupUseCase @Inject constructor(
    private val iconOverrideRepository: IconOverrideRepository,
    private val appRepository: AppRepository,
) {
    suspend fun export(): List<BackupIconOverride> = iconOverrideRepository.getAll().map { override ->
        BackupIconOverride(
            packageName = override.packageName,
            activityName = override.activityName,
            profile = override.profile.name,
            source = override.source.name,
            packPackage = override.packPackage,
            drawableName = override.drawableName,
            calendarPrefix = override.calendarPrefix,
            snapshotPng = iconOverrideRepository.snapshotBytes(override)?.let { Base64.getEncoder().encodeToString(it) },
        )
    }

    /** Replaces every custom icon with [entries]. */
    suspend fun restore(entries: List<BackupIconOverride>) {
        val restored = entries.mapNotNull { entry ->
            val source = IconOverrideSource.entries.firstOrNull { it.name == entry.source } ?: return@mapNotNull null
            val profile = AppProfile.entries.firstOrNull { it.name == entry.profile } ?: AppProfile.PERSONAL
            val userId = appRepository.resolveUserHandle(profile)?.hashCode() ?: IconOverride.UNRESOLVED_USER_ID
            val png = entry.snapshotPng?.let { runCatching { Base64.getDecoder().decode(it) }.getOrNull() }
            IconOverride(
                packageName = entry.packageName,
                activityName = entry.activityName,
                userId = userId,
                profile = profile,
                source = source,
                packPackage = entry.packPackage,
                drawableName = entry.drawableName,
                calendarPrefix = entry.calendarPrefix,
            ) to png
        }
        iconOverrideRepository.restore(restored)
    }
}
