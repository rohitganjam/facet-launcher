package com.facetlauncher.app.data.iconpack

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconPackSelection
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class IconPackRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Application>()

    private fun settingsRepository() = SettingsRepository(
        PreferenceDataStoreFactory.create(produceFile = { File(context.cacheDir, "icon-pack-repo-${System.nanoTime()}.preferences_pb") }),
    )

    /** Registers [packageName] as an app answering [action], the way an icon pack declares itself. */
    private fun installPack(packageName: String, label: String, action: String) {
        val app = ApplicationInfo().apply {
            this.packageName = packageName
            nonLocalizedLabel = label
        }
        shadowOf(context.packageManager).installPackage(PackageInfo().apply { this.packageName = packageName; applicationInfo = app })
        val resolve = ResolveInfo().apply { activityInfo = ActivityInfo().apply { this.packageName = packageName; name = "$packageName.Main"; applicationInfo = app } }
        shadowOf(context.packageManager).addResolveInfoForIntent(Intent(action), resolve)
    }

    @Test
    fun `finds packs from any launcher's theme action, once each, sorted by label`() = runTest {
        // Given packs declared for Nova and ADW, one of them for both
        installPack("com.zeta.pack", "Zeta", "com.novalauncher.THEME")
        installPack("com.alpha.pack", "Alpha", "org.adw.launcher.THEMES")
        installPack("com.zeta.pack", "Zeta", "org.adw.launcher.THEMES")

        // When listing installed packs
        val packs = IconPackRepository(context, settingsRepository()).getInstalledPacks()

        // Then each appears once, by label
        assertEquals(listOf("Alpha", "Zeta"), packs.map { it.label })
    }

    @Test
    fun `the launcher itself is never offered as a pack`() = runTest {
        // Given this app answering a theme action
        installPack(FACET_LAUNCHER_PACKAGE_NAME, "Facet", "com.novalauncher.THEME")

        // Then it isn't listed
        assertEquals(emptyList<String>(), IconPackRepository(context, settingsRepository()).getInstalledPacks().map { it.packageName })
    }

    @Test
    fun `an applied pack that isn't installed is cleared`() = runTest {
        // Given a stored pack missing from the device
        val settings = settingsRepository().apply { setIconPack("com.missing.pack") }

        // When loading it
        val icons = IconPackRepository(context, settings).loadApplied(IconPackSelection("com.missing.pack", IconPackFallback.MATCH_PACK), emptySet())

        // Then nothing loads and the setting goes back to the built-in styles
        assertNull(icons)
        assertNull(settings.settings.first().iconPackPackage)
    }

    @Test
    fun `selection follows the stored pack and fallback`() = runTest {
        // Given a pack and a fallback stored
        val settings = settingsRepository().apply {
            setIconPack("com.alpha.pack")
            setIconPackFallback(IconPackFallback.KEEP_DEFAULT)
        }

        // Then the selection carries both, and clearing the pack clears it
        assertEquals(IconPackSelection("com.alpha.pack", IconPackFallback.KEEP_DEFAULT), IconPackRepository(context, settings).selection.first())
        settings.setIconPack(null)
        assertNull(IconPackRepository(context, settings).selection.first())
    }
}
