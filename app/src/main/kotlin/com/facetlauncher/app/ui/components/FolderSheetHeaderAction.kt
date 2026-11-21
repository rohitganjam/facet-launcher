package com.facetlauncher.app.ui.components

/** The sheet's header trailing action — either editing this folder in place, or confirming a pending add into it. */
sealed interface FolderSheetHeaderAction {
    /** The normal case: tapping a folder tile opens its contents with a rename pencil. */
    data class Rename(val onRename: (Long, String) -> Unit) : FolderSheetHeaderAction

    /** Reached from [AppContextMenu]'s "Add to folder" list — previews this folder's real contents before the pending app is actually added; tapping Add performs the add and dismisses. [onBack] returns to that folder list (a leading chevron in the header, since this preview sits on top of it rather than replacing it) rather than dismissing the whole flow. */
    data class AddHere(val onAdd: () -> Unit, val onBack: () -> Unit) : FolderSheetHeaderAction

    /** No trailing action at all — for a synthetic [Folder] that isn't backed by a real one (e.g. the "Recently installed" star item), where "Rename" would be meaningless. */
    data object None : FolderSheetHeaderAction
}
