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
    /** `true` when the screen showing now is the one lacking the copy, so it can be captured right here. */
    val thisScreen: Boolean = false,
)

/** Every screen known, the largest first, and the prompt about them. */
@Immutable
data class WallpaperScreens(val sizes: ImmutableList<ScreenSize> = persistentListOf(), val prompt: OtherScreenPrompt = OtherScreenPrompt())
