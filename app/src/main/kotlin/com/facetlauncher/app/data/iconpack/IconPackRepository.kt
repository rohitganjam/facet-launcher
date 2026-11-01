package com.facetlauncher.app.data.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.flattenIcon
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import com.facetlauncher.app.data.model.IconPackInfo
import com.facetlauncher.app.data.model.IconPackSelection
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The launcher-theme intents icon packs answer to; each needs a matching `<queries>` entry in the manifest. */
internal val ICON_PACK_ACTIONS = listOf(
    "org.adw.launcher.THEMES",
    "org.adw.launcher.icons.ACTION_PICK_ICON",
    "com.novalauncher.THEME",
    "com.teslacoilsw.launcher.THEME",
    "com.gau.go.launcherex.theme",
    "com.fede.launcher.THEME_ICONPACK",
    "com.anddoes.launcher.THEME",
    "com.dlto.atom.launcher.THEME",
)

/** Packs kept parsed at once: the applied one plus the few the Icons screen previews. */
private const val MAX_LOADED_PACKS = 4

/** Finds installed icon packs and loads their icons. The applied pack lives in [SettingsRepository]. Open so tests can stand in for another app's resources. */
@Singleton
open class IconPackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val packageManager: PackageManager get() = context.packageManager
    private val loadMutex = Mutex()
    private val loaded = object : LinkedHashMap<String, LoadedPack>(MAX_LOADED_PACKS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LoadedPack>?) = size > MAX_LOADED_PACKS
    }

    /** A parsed pack, valid while the pack isn't updated and the same apps are installed. */
    private class LoadedPack(val packUpdated: Long, val forPackages: Set<String>, val icons: IconPackIcons?)

    /** The applied pack, null for the built-in icon styles. */
    val selection: Flow<IconPackSelection?> = settingsRepository.settings.map { it.iconPackSelection }.distinctUntilChanged()

    /** Every installed icon pack, by label. */
    open suspend fun getInstalledPacks(): List<IconPackInfo> = withContext(ioDispatcher) {
        val collator = Collator.getInstance()
        ICON_PACK_ACTIONS
            .flatMap { action -> runCatching { packageManager.queryIntentActivities(Intent(action), 0) }.getOrDefault(emptyList()) }
            .mapNotNull { it.activityInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .filterNot { it.packageName == FACET_LAUNCHER_PACKAGE_NAME }
            .map { info ->
                IconPackInfo(
                    packageName = info.packageName,
                    label = runCatching { info.loadLabel(packageManager).toString() }.getOrDefault(info.packageName),
                    icon = runCatching { info.loadIcon(packageManager)?.let(::flattenIcon)?.capped()?.asImageBitmap() }.getOrNull(),
                )
            }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    /**
     * The pack's icons for [installedPackages], or null when it isn't installed or has no readable appfilter. Reread
     * when the pack updates or the installed apps change, since only their entries are kept.
     */
    open suspend fun load(packageName: String, installedPackages: Set<String>): IconPackIcons? = withContext(ioDispatcher) {
        val updated = runCatching { packageManager.getPackageInfo(packageName, 0).lastUpdateTime }.getOrNull()
            ?: return@withContext null
        loadMutex.withLock {
            loaded[packageName]?.takeIf { it.packUpdated == updated && it.forPackages == installedPackages }?.let { return@withLock it.icons }
            val icons = runCatching { read(packageName, installedPackages) }.getOrNull()
            loaded[packageName] = LoadedPack(updated, installedPackages, icons)
            icons
        }
    }

    /** [load] for the applied pack. A pack that has been uninstalled (or restored from a backup without it) is unapplied. */
    suspend fun loadApplied(selection: IconPackSelection, installedPackages: Set<String>): IconPackIcons? {
        if (!isInstalled(selection.packageName)) {
            settingsRepository.setIconPack(null)
            return null
        }
        return load(selection.packageName, installedPackages)
    }

    /** Opens the pack's own app, where it has one. */
    fun open(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    protected open suspend fun isInstalled(packageName: String): Boolean = withContext(ioDispatcher) {
        runCatching { packageManager.getPackageInfo(packageName, 0) }.isSuccess
    }

    private fun read(packageName: String, installedPackages: Set<String>): IconPackIcons? {
        val resources = packageManager.getResourcesForApplication(packageName)
        val filter = readAppFilter(resources, packageName, installedPackages::contains) ?: return null
        return ResourceIconPack(resources, packageName, filter)
    }

    /**
     * Packs ship `appfilter.xml` as a raw asset, as a compiled XML resource, or both. The asset is preferred, since
     * its text can be scanned directly (see [AppFilter.parse]); a compiled resource needs the far slower pull parser.
     */
    private fun readAppFilter(resources: Resources, packageName: String, keepPackage: (String) -> Boolean): AppFilter? {
        runCatching { resources.assets.open("appfilter.xml") }.getOrNull()?.use { stream ->
            return AppFilter.parse(stream.readBytes().decodeToString(), keepPackage)
        }
        @Suppress("DiscouragedApi")
        val id = resources.getIdentifier("appfilter", "xml", packageName)
        return if (id != 0) resources.getXml(id).use { AppFilter.parse(it, keepPackage) } else null
    }

    private fun Bitmap.capped(): Bitmap =
        if (width <= PACK_ICON_SIZE && height <= PACK_ICON_SIZE) this else Bitmap.createScaledBitmap(this, PACK_ICON_SIZE, PACK_ICON_SIZE, true)
}
