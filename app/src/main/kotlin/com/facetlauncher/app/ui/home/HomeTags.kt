package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Put in front of every test tag Home's components set. Empty on Home itself; a scaled preview of
 * Home sets its own, so several previews (and the real Home beneath them) never share a tag.
 */
internal val LocalHomeTagPrefix = staticCompositionLocalOf { "" }

/** [name] with the current [LocalHomeTagPrefix] in front. */
@Composable
internal fun homeTag(name: String): String = LocalHomeTagPrefix.current + name
