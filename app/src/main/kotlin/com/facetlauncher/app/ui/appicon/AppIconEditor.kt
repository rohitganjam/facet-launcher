package com.facetlauncher.app.ui.appicon

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import com.facetlauncher.app.data.iconpack.PackCatalog
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PackIconMatch
import com.facetlauncher.app.domain.AppIconChoices
import kotlinx.coroutines.flow.Flow

/** A pack's icons as the Browse page shows them; [catalog] is null when the pack lists none. */
@Immutable
data class PackCatalogState(val catalog: PackCatalog? = null, val isLoading: Boolean = true)

/**
 * What the app menu's Change icon pages need, bundled so it reaches every app menu (Home, Dock, drawer, folders, search)
 * through [LocalAppIconEditor] instead of every sheet's parameters. Without one the menu shows no pencil.
 */
@Immutable
class AppIconEditor(
    val observeChoices: (AppInfo) -> Flow<AppIconChoices>,
    val observeCatalog: (packPackage: String) -> Flow<PackCatalogState>,
    /** A grid-sized thumbnail of a pack's drawable, or null when it can't be drawn. */
    val loadThumbnail: suspend (packPackage: String, drawableName: String) -> ImageBitmap?,
    val onUsePackIcon: (AppInfo, PackIconMatch) -> Unit,
    val onUseOwnIcon: (AppInfo) -> Unit,
    val onReset: (AppInfo) -> Unit,
    val onFindPacks: () -> Unit,
)

val LocalAppIconEditor = staticCompositionLocalOf<AppIconEditor?> { null }
