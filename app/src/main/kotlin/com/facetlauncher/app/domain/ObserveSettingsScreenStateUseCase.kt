package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SettingsScreenState(
    val settings: LauncherSettings,
    /** The full folder library's size. */
    val folderCount: Int,
)

/** Combines [SettingsRepository] and [FolderRepository] into one observable state for the Settings screen. */
class ObserveSettingsScreenStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val folderRepository: FolderRepository,
) {
    operator fun invoke(): Flow<SettingsScreenState> =
        combine(settingsRepository.settings, folderRepository.observeFolders()) { settings, folders ->
            SettingsScreenState(settings = settings, folderCount = folders.size)
        }
}
