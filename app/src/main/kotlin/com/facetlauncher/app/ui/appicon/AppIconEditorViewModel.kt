package com.facetlauncher.app.ui.appicon

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PackIconMatch
import com.facetlauncher.app.domain.AppIconChoices
import com.facetlauncher.app.domain.ChangeAppIconUseCase
import com.facetlauncher.app.domain.ObserveAppIconChoicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/** Browse grid thumbnails, in pixels: a 44dp cell on a 3x screen. */
private const val THUMBNAIL_PX = 144

/** Thumbnails kept while browsing, enough for several screens of a grid. */
private const val THUMBNAIL_CACHE_SIZE = 240

/** The app menu's Change icon and Browse pages, for every app menu under Home. */
@HiltViewModel
class AppIconEditorViewModel @Inject constructor(
    private val observeAppIconChoices: ObserveAppIconChoicesUseCase,
    private val changeAppIcon: ChangeAppIconUseCase,
    private val iconPackRepository: IconPackRepository,
) : ViewModel() {
    private val thumbnails = LruCache<String, ImageBitmap>(THUMBNAIL_CACHE_SIZE)

    fun choices(app: AppInfo): Flow<AppIconChoices> = observeAppIconChoices(app)

    fun catalog(packPackage: String): Flow<PackCatalogState> = flow {
        emit(PackCatalogState(isLoading = true))
        emit(PackCatalogState(iconPackRepository.catalog(packPackage), isLoading = false))
    }

    suspend fun thumbnail(packPackage: String, drawableName: String): ImageBitmap? {
        val key = "$packPackage/$drawableName"
        thumbnails.get(key)?.let { return it }
        return iconPackRepository.drawIcon(packPackage, drawableName, THUMBNAIL_PX)?.asImageBitmap()?.also { thumbnails.put(key, it) }
    }

    fun usePackIcon(app: AppInfo, match: PackIconMatch) {
        viewModelScope.launch { changeAppIcon.usePackIcon(app, match) }
    }

    fun useOwnIcon(app: AppInfo) {
        viewModelScope.launch { changeAppIcon.useOwnIcon(app) }
    }

    fun reset(app: AppInfo) {
        viewModelScope.launch { changeAppIcon.reset(app) }
    }

    fun editor(onFindPacks: () -> Unit): AppIconEditor = AppIconEditor(
        observeChoices = ::choices,
        observeCatalog = ::catalog,
        loadThumbnail = ::thumbnail,
        onUsePackIcon = ::usePackIcon,
        onUseOwnIcon = ::useOwnIcon,
        onReset = ::reset,
        onFindPacks = onFindPacks,
    )
}
