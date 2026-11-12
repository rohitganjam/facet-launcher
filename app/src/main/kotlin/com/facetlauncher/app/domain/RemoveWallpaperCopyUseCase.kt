package com.facetlauncher.app.domain

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import javax.inject.Inject

/** Deletes the glass copy and the id recorded with it, so every panel goes back to its tint. The system wallpaper is left as it is. */
class RemoveWallpaperCopyUseCase @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        wallpaperCopyRepository.remove()
        settingsRepository.setWallpaperCopySystemId(-1)
    }
}
