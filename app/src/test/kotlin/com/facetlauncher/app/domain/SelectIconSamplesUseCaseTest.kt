package com.facetlauncher.app.domain

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.facetlauncher.app.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SelectIconSamplesUseCaseTest {

    private val icon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
    private fun app(packageName: String, withIcon: Boolean = true) = AppInfo(packageName, "$packageName.Main", packageName, if (withIcon) icon else null)

    private val select = SelectIconSamplesUseCase(SelectPreviewAppsUseCase())

    @Test
    fun `home apps come first`() {
        // Given two Home apps and a drawer that also holds defaults
        val installed = listOf(app("a"), app("b"), app("dialer"), app("home1"), app("home2"))

        // When picking samples
        val samples = select(homeApps = listOf(app("home1"), app("home2")), installedApps = installed, defaultPackages = listOf("dialer"))

        // Then Home leads, then the default app, then the drawer in order
        assertEquals(listOf("home1", "home2", "dialer", "a"), samples.map { it.packageName })
    }

    @Test
    fun `an empty home still fills every slot from defaults and the drawer`() {
        // Given no Home apps
        val installed = listOf(app("a"), app("b"), app("c"), app("camera"), app("messages"))

        // When picking samples
        val samples = select(homeApps = emptyList(), installedApps = installed, defaultPackages = listOf("messages", "camera"))

        // Then the defaults lead and the drawer makes up the rest
        assertEquals(listOf("messages", "camera", "a", "b"), samples.map { it.packageName })
    }

    @Test
    fun `apps are not repeated and iconless apps are skipped`() {
        // Given a Home app that is also a default, and an app without an icon
        val installed = listOf(app("blank", withIcon = false), app("dialer"), app("x"))

        // When picking samples
        val samples = select(homeApps = listOf(app("dialer")), installedApps = installed, defaultPackages = listOf("dialer"))

        // Then each app appears once and the iconless one is left out
        assertEquals(listOf("dialer", "x"), samples.map { it.packageName })
    }

    @Test
    fun `accept narrows the candidates to what a pack covers`() {
        // Given a pack that only covers some apps
        val installed = listOf(app("a"), app("b"), app("c"), app("d"), app("e"))
        val covered = setOf("b", "d", "e")

        // When picking samples for that pack
        val samples = select(homeApps = listOf(app("a")), installedApps = installed, defaultPackages = emptyList(), accept = { it.packageName in covered })

        // Then only covered apps are shown
        assertEquals(listOf("b", "d", "e"), samples.map { it.packageName })
    }
}
