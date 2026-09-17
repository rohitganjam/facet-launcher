package com.facetlauncher.app.data.model

/**
 * One entry in the App Drawer's letter-grouped content when folders are shown inline
 * ([DrawerFolderDisplayMode.INLINE]) — either an app or a folder, sorted together by
 * [displayName] via [com.facetlauncher.app.domain.GroupAppsByLetterUseCase]'s generic overload.
 * The Drawer wraps every app in [AppEntry] even outside INLINE mode, so its List/Grid rendering
 * has a single item type to branch on regardless of [DrawerFolderDisplayMode].
 */
sealed interface DrawerItem {
    val displayName: String

    data class AppEntry(val app: AppInfo) : DrawerItem {
        override val displayName: String get() = app.label
    }

    data class FolderEntry(val folder: Folder) : DrawerItem {
        override val displayName: String get() = folder.name
    }
}
