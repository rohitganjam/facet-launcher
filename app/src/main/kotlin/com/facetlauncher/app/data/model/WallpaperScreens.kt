package com.facetlauncher.app.data.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** What, if anything, the wallpaper screen suggests about the user's other screens. */
enum class OtherScreenPromptKind {
    /** Nothing to suggest. */
    None,

    /** A screen has no copy of its own: Facet shows another one there, which may not line up. */
    MissingCopy,

    /** The phone looks like a foldable, but Facet has only seen one of its screens. */
    FoldableUnseen,
}

/** A suggestion about the other screens, and the prompt keys to remember if the user says no. */
@Immutable
data class OtherScreenPrompt(
    val kind: OtherScreenPromptKind = OtherScreenPromptKind.None,
    val dismissKeys: ImmutableList<String> = persistentListOf(),
    /** `true` when the view showing now is the one lacking the copy, so it can be captured right here. */
    val thisScreen: Boolean = false,
    /** `true` when every view lacking a copy belongs to a screen whose other orientation has one: only the turned view is missing. */
    val otherOrientation: Boolean = false,
)

/** The view showing now when Home should offer to capture its wallpaper, and whether the screen's other orientation already has a capture. */
data class WallpaperCopyNeed(val view: ScreenSize, val otherOrientation: Boolean)

/** Every screen known in its natural orientation, the largest first, and the prompt about its views. */
@Immutable
data class WallpaperScreens(val sizes: ImmutableList<ScreenSize> = persistentListOf(), val prompt: OtherScreenPrompt = OtherScreenPrompt())
