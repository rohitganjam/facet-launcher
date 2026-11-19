package com.facetlauncher.app.domain

import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.OtherScreenPrompt
import com.facetlauncher.app.data.model.OtherScreenPromptKind
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.data.model.WallpaperCopyNeed
import com.facetlauncher.app.data.model.WallpaperScreens
import com.facetlauncher.app.data.model.promptKey
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The views Facet knows (each screen in each orientation) and what to suggest about them, once there is a copy to complete. A view without a
 * copy of its own is offered one (unless the user said no to that view), which is how a capture on one screen of a foldable, or in one
 * orientation, leads to one on the other: only the views that lack data are mentioned. When only one screen is known but the phone has a hinge sensor, the user is told to unfold it.
 */
class ObserveWallpaperScreensUseCase @Inject constructor(private val wallpaperCopyRepository: WallpaperCopyRepository) {

    operator fun invoke(): Flow<WallpaperScreens> = combine(
        wallpaperCopyRepository.screens,
        wallpaperCopyRepository.dismissedPrompts,
        wallpaperCopyRepository.currentScreen,
        wallpaperCopyRepository.isFinishingCopies,
    ) { screens, dismissed, current, finishing ->
        // While the other views' copies of a wallpaper just set are being stored, none is missing yet.
        WallpaperScreens(screens.map { it.screen }.distinct().toImmutableList(), if (finishing) OtherScreenPrompt() else prompt(screens, dismissed, current))
    }

    /**
     * The view showing now when it has no copy that matches the wallpaper while another view has one (or had one): Home offers to
     * capture it. `null` when it is fine, there is no copy anywhere, or the user said no to that view.
     */
    fun copyNeed(): Flow<WallpaperCopyNeed?> = combine(
        wallpaperCopyRepository.screens,
        wallpaperCopyRepository.currentScreen,
        wallpaperCopyRepository.dismissedPrompts,
        wallpaperCopyRepository.isFinishingCopies,
    ) { screens, current, dismissed, finishing ->
        val view = screens.firstOrNull { it.size == current }
        when {
            finishing || view == null || view.hasCopy || view.size.promptKey in dismissed || screens.none { it.hasCopy || it.outdated } -> null
            else -> WallpaperCopyNeed(
                current,
                otherOrientation = screens.any { it.screen == view.screen && it.size != view.size && it.hasCopy },
                wallpaperId = wallpaperCopyRepository.currentSystemWallpaperId(),
            )
        }
    }.distinctUntilChanged()

    private fun prompt(screens: List<ScreenCopy>, dismissed: Set<String>, current: ScreenSize): OtherScreenPrompt {
        if (screens.none { it.hasCopy }) return OtherScreenPrompt()
        val missing = screens.filter { !it.hasCopy && it.size.promptKey !in dismissed }
        return when {
            missing.isNotEmpty() -> OtherScreenPrompt(
                OtherScreenPromptKind.MissingCopy,
                missing.map { it.size.promptKey }.toImmutableList(),
                thisScreen = missing.any { it.size == current },
                otherOrientation = missing.all { view -> screens.any { it.screen == view.screen && it.size != view.size && (it.hasCopy || it.outdated) } },
            )
            screens.map { it.screen }.distinct().size == 1 && WallpaperCopyRepository.FOLDABLE_PROMPT !in dismissed && wallpaperCopyRepository.maybeFoldable() ->
                OtherScreenPrompt(OtherScreenPromptKind.FoldableUnseen, persistentListOf(WallpaperCopyRepository.FOLDABLE_PROMPT))
            else -> OtherScreenPrompt()
        }
    }
}
