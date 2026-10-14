package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObserveQuickAddStateUseCaseTest {

    private val useCase = ObserveQuickAddStateUseCase()

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private val app = appInfo('z')

    /** The same app as [app] but in another user's profile (a Work Profile copy). */
    private fun workCopyOf(app: AppInfo): AppInfo {
        val parcel = android.os.Parcel.obtain()
        parcel.writeInt(10)
        parcel.setDataPosition(0)
        val handle = android.os.UserHandle(parcel)
        parcel.recycle()
        return app.copy(profile = com.facetlauncher.app.data.model.AppProfile.WORK, userHandle = handle)
    }

    @Test
    fun `forApp shows Add against the launcher-wide default when neither list names a facet`() {
        val result = useCase.forApp(app, dockItems = emptyList(), favoriteItems = emptyList(), dockFacetName = null, favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Add(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Add(facetName = null), result.dockAction)
    }

    @Test
    fun `forApp shows Add to facet favorites when favoritesFacetName is passed`() {
        val result = useCase.forApp(app, dockItems = emptyList(), favoriteItems = emptyList(), dockFacetName = null, favoritesFacetName = "P1")

        assertEquals(QuickPlacementAction.Add(facetName = "P1"), result.favoritesAction)
    }

    @Test
    fun `forApp hides Favorites row once the list is already at the cap and app isn't a member`() {
        val fullFavorites = (1..AppListLimits.MAX_FAVORITES).map { PlacedItem.SingleApp(appInfo('a' + it)) }

        val result = useCase.forApp(app, dockItems = emptyList(), favoriteItems = fullFavorites, dockFacetName = null, favoritesFacetName = null)

        assertEquals(null, result.favoritesAction)
    }

    @Test
    fun `forApp shows Remove from Favorites when the app is already a member, even at the cap`() {
        val fullFavoritesWithApp = listOf(PlacedItem.SingleApp(app)) +
            (1 until AppListLimits.MAX_FAVORITES).map { PlacedItem.SingleApp(appInfo('a' + it)) }

        val result = useCase.forApp(app, dockItems = emptyList(), favoriteItems = fullFavoritesWithApp, dockFacetName = null, favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.favoritesAction)
    }

    @Test
    fun `forApp shows Add to facet dock when dockFacetName is passed`() {
        val result = useCase.forApp(app, dockItems = emptyList(), favoriteItems = emptyList(), dockFacetName = "P1", favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Add(facetName = "P1"), result.dockAction)
    }

    @Test
    fun `forApp hides Dock row once the dock is already at the cap and app isn't a member`() {
        val fullDock = (1..5).map { PlacedItem.SingleApp(appInfo('a' + it)) }

        val result = useCase.forApp(app, dockItems = fullDock, favoriteItems = emptyList(), dockFacetName = null, favoritesFacetName = null)

        assertEquals(null, result.dockAction)
    }

    @Test
    fun `forApp shows Remove from Dock when the app is already a member, even at the cap`() {
        val fullDockWithApp = listOf(PlacedItem.SingleApp(app)) + (1..4).map { PlacedItem.SingleApp(appInfo('a' + it)) }

        val result = useCase.forApp(app, dockItems = fullDockWithApp, favoriteItems = emptyList(), dockFacetName = null, favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.dockAction)
    }

    @Test
    fun `forFolder shows Add when the folder isn't yet placed`() {
        val folder = Folder(id = 9L, name = "Games", apps = emptyList())

        val result = useCase.forFolder(folder, dockItems = emptyList(), favoriteItems = emptyList(), dockFacetName = null, favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Add(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Add(facetName = null), result.dockAction)
    }

    @Test
    fun `forFolder shows Remove when the folder is already placed in Favorites and Dock`() {
        val folder = Folder(id = 9L, name = "Games", apps = emptyList())
        val placedFolder = listOf(PlacedItem.FolderItem(folder))

        val result = useCase.forFolder(folder, dockItems = placedFolder, favoriteItems = placedFolder, dockFacetName = null, favoritesFacetName = null)

        assertEquals(QuickPlacementAction.Remove(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Remove(facetName = null), result.dockAction)
    }

    @Test
    fun `forApp treats a Work Profile copy as not yet a member when only the personal copy is placed`() {
        // Given the personal copy is already in both lists
        val work = workCopyOf(app)
        val placed = listOf(PlacedItem.SingleApp(app))

        // When opening the long-press menu of the Work Profile copy
        val result = useCase.forApp(work, dockItems = placed, favoriteItems = placed, dockFacetName = null, favoritesFacetName = null)

        // Then it offers Add, not Remove, for both lists
        assertEquals(QuickPlacementAction.Add(facetName = null), result.favoritesAction)
        assertEquals(QuickPlacementAction.Add(facetName = null), result.dockAction)
    }
}
