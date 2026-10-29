package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.facetlauncher.app.data.icon.StyledPicture
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconStyle
import com.facetlauncher.app.ui.theme.LocalStyledIconCache
import kotlinx.collections.immutable.ImmutableList

/**
 * Makes the picture the style draws (glyph or outline) for every app in the background, so the App Drawer shows each styled
 * the moment it scrolls into view rather than building it then. A style that draws neither empties the cache.
 */
@Composable
fun WarmStyledIcons(apps: ImmutableList<AppInfo>, renderMode: IconRenderMode) {
    val cache = LocalStyledIconCache.current
    val picture = when (renderMode.style) {
        IconStyle.GLYPH -> StyledPicture.GLYPH
        IconStyle.OUTLINE -> StyledPicture.OUTLINE
        else -> null
    }
    LaunchedEffect(apps, picture) {
        if (picture != null) cache.warm(apps, picture) else cache.clear()
    }
}
