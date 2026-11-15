package com.facetlauncher.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.local.IconOverrideDao
import com.facetlauncher.app.data.local.toEntity
import com.facetlauncher.app.data.local.toModel
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Bitmaps of drawn custom icons kept in memory, so a rescan doesn't redraw them. */
private const val DRAWN_CACHE_SIZE = 64

/**
 * Per-app custom icons, launcher-wide. A pack icon is drawn live from its pack while the pack is installed, so pack
 * updates and dated calendar icons apply, and from a PNG snapshot taken when it was picked once the pack is gone.
 * Snapshots live in the app's own files, under [FOLDER].
 */
@Singleton
open class IconOverrideRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: IconOverrideDao,
    private val iconPackRepository: IconPackRepository,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val folder: File get() = File(context.filesDir, FOLDER)
    private val drawn = LruCache<String, Bitmap>(DRAWN_CACHE_SIZE)

    open val overrides: Flow<List<IconOverride>> get() = dao.observeAll().map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()

    open suspend fun getAll(): List<IconOverride> = dao.getAll().map { it.toModel() }

    /** [app]'s icon becomes pack [packPackage]'s [drawableName] (dated by [calendarPrefix] for a calendar app), with [snapshot] kept for when the pack is gone. */
    suspend fun setPackIcon(app: AppInfo, packPackage: String, drawableName: String?, calendarPrefix: String?, snapshot: Bitmap?) {
        val file = snapshot?.let { writeSnapshot(it) }
        replace(app) { userId ->
            IconOverride(
                packageName = app.packageName,
                activityName = app.activityName,
                userId = userId,
                profile = app.profile,
                source = IconOverrideSource.PACK,
                packPackage = packPackage,
                drawableName = drawableName,
                calendarPrefix = calendarPrefix,
                snapshotFile = file,
                updatedAt = clock.millis(),
            )
        }
    }

    /** [app] shows its own icon, whatever icon pack is applied. */
    suspend fun setOwnIcon(app: AppInfo) {
        replace(app) { userId ->
            IconOverride(app.packageName, app.activityName, userId, app.profile, IconOverrideSource.OWN, updatedAt = clock.millis())
        }
    }

    /** Drops [override], and its snapshot. */
    suspend fun reset(override: IconOverride) {
        dao.delete(override.packageName, override.activityName, override.userId)
        deleteSnapshot(override.snapshotFile)
    }

    suspend fun resetAll() {
        val all = getAll()
        dao.deleteAll()
        all.forEach { deleteSnapshot(it.snapshotFile) }
    }

    /** An uninstalled app's custom icons, every activity of it. */
    suspend fun removeByPackage(packageName: String, userId: Int) {
        val gone = getAll().filter { it.packageName == packageName && it.userId == userId }
        dao.deleteByPackage(packageName, userId)
        gone.forEach { deleteSnapshot(it.snapshotFile) }
    }

    /** A removed profile's custom icons. */
    suspend fun removeByUserId(userId: Int) {
        val gone = getAll().filter { it.userId == userId }
        dao.deleteByUserId(userId)
        gone.forEach { deleteSnapshot(it.snapshotFile) }
    }

    /**
     * [override]'s pack icon for [dayOfMonth]: from the pack while it is installed, else its snapshot, else null (the app
     * then shows its default icon). Not for [IconOverrideSource.OWN], which [AppRepository] draws.
     */
    open suspend fun drawPackIcon(override: IconOverride, dayOfMonth: Int): Bitmap? {
        val pack = override.packPackage ?: return null
        val packUpdated = iconPackRepository.packUpdatedAt(pack)
        val dated = override.calendarPrefix?.let { "$it$dayOfMonth" }
        val key = "${override.packageName}/${override.activityName}/${override.userId}@${override.updatedAt}:${dated ?: override.drawableName}:$packUpdated"
        drawn.get(key)?.let { return it }
        val bitmap = if (packUpdated != null) {
            dated?.let { iconPackRepository.drawIcon(pack, it) } ?: override.drawableName?.let { iconPackRepository.drawIcon(pack, it) }
        } else {
            null
        } ?: readSnapshot(override.snapshotFile)
        bitmap?.let { drawn.put(key, it) }
        return bitmap
    }

    /** The PNG bytes of [override]'s snapshot, for a backup. */
    suspend fun snapshotBytes(override: IconOverride): ByteArray? = withContext(ioDispatcher) {
        override.snapshotFile?.let { name -> runCatching { File(folder, name).readBytes() }.getOrNull() }
    }

    /** Replaces every custom icon with [restored], each with its snapshot's PNG bytes, as a backup restore does. */
    suspend fun restore(restored: List<Pair<IconOverride, ByteArray?>>) {
        resetAll()
        val rows = restored.map { (override, png) ->
            val file = png?.let { bytes -> withContext(ioDispatcher) { runCatching { saveFile(bytes) }.getOrNull() } }
            override.copy(snapshotFile = file).toEntity()
        }
        dao.replaceAll(rows)
    }

    private suspend fun replace(app: AppInfo, build: (Int) -> IconOverride) {
        val userId = app.userHandle.hashCode()
        val previous = dao.get(app.packageName, app.activityName, userId)
        dao.upsert(build(userId).toEntity())
        previous?.snapshotFile?.let { deleteSnapshot(it) }
    }

    private suspend fun writeSnapshot(bitmap: Bitmap): String? = withContext(ioDispatcher) {
        runCatching {
            val bytes = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }
            saveFile(bytes)
        }.getOrNull()
    }

    private fun saveFile(bytes: ByteArray): String {
        folder.mkdirs()
        val name = "${UUID.randomUUID()}.png"
        File(folder, name).writeBytes(bytes)
        return name
    }

    private suspend fun readSnapshot(name: String?): Bitmap? = withContext(ioDispatcher) {
        name?.let { runCatching { BitmapFactory.decodeFile(File(folder, it).path) }.getOrNull() }
    }

    private suspend fun deleteSnapshot(name: String?) {
        if (name == null) return
        withContext(ioDispatcher) { runCatching { File(folder, name).delete() } }
    }

    companion object {
        const val FOLDER = "icon_overrides"
    }
}
