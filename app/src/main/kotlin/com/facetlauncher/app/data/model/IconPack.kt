package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.facetlauncher.app.R

/** An installed third-party icon pack (one made for Nova, ADW, Apex and the like). */
@Immutable
data class IconPackInfo(
    val packageName: String,
    val label: String,
    /** The pack app's own launcher icon, for a pack that covers none of the sample apps. */
    val icon: ImageBitmap?,
)

/** What an app the active icon pack has no icon for looks like. */
enum class IconPackFallback(@param:StringRes val displayNameRes: Int) {
    /** The app's own icon on the pack's backing tile and mask, where the pack ships them. */
    MATCH_PACK(R.string.icon_pack_fallback_match_pack),

    /** The app's own icon, in the Icon Shape setting. */
    KEEP_DEFAULT(R.string.icon_pack_fallback_keep_default),
}

/** The icon pack applied launcher-wide, as stored in settings. */
data class IconPackSelection(val packageName: String, val fallback: IconPackFallback)
