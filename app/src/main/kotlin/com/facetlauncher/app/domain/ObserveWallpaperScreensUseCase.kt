package com.facetlauncher.app.domain

import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.OtherScreenPrompt
import com.facetlauncher.app.data.model.OtherScreenPromptKind
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.data.model.WallpaperScreens
import com.facetlauncher.app.data.model.promptKey
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The screens Facet knows and what to suggest about them, once there is a copy to complete. A screen without a copy of its own is offered
 * one (unless the user said no to that screen), which is how a capture on one screen of a foldable leads to one on the other: only the
 * screens that lack data are mentioned. When only one screen is known but the phone has a hinge sensor, the user is told to unfold it.
 */
class ObserveWallpaperScreensUseCase @Inject constructor(private val wallpaperCopyRepository: WallpaperCopyRepository) {

    operator fun invoke(): Flow<WallpaperScreens> = combine(wallpaperCopyRepository.screens, wallpaperCopyRepository.dismissedPrompts, wallpaperCopyRepository.currentScreen) { screens, dismissed, current ->
        WallpaperScreens(screens.map { it.size }.toImmutableList(), prompt(screens, dismissed, current))
    }

    /**
     * The screen showing now when it has no copy that matches the wallpaper while another screen has one (or had one): Home offers to
     * update it. `null` when it is fine, there is no copy anywhere, or the user said no to that screen.
     */
    fun screenNeedingCopy(): Flow<ScreenSize?> = combine(
        wallpaperCopyRepository.screens,
        wallpaperCopyRepository.currentScreen,
        wallpaperCopyRepository.dismissedPrompts,
    ) { screens, current, dismissed ->
        current.takeIf { screen ->
            screens.any { it.hasCopy || it.outdated } && screens.none { it.size == screen && it.hasCopy } && screen.promptKey !in dismissed
        }
    }.distinctUntilChanged()

    private fun prompt(screens: List<ScreenCopy>, dismissed: Set<String>, current: ScreenSize): OtherScreenPrompt {
        if (screens.none { it.hasCopy }) return OtherScreenPrompt()
        val missing = screens.filter { !it.hasCopy && it.size.promptKey !in dismissed }
        return when {
            missing.isNotEmpty() -> OtherScreenPrompt(OtherScreenPromptKind.MissingCopy, missing.map { it.size.promptKey }.toImmutableList(), thisScreen = missing.any { it.size == current })
            screens.size == 1 && WallpaperCopyRepository.FOLDABLE_PROMPT !in dismissed && wallpaperCopyRepository.maybeFoldable() ->
                OtherScreenPrompt(OtherScreenPromptKind.FoldableUnseen, persistentListOf(WallpaperCopyRepository.FOLDABLE_PROMPT))
            else -> OtherScreenPrompt()
        }
    }
}
