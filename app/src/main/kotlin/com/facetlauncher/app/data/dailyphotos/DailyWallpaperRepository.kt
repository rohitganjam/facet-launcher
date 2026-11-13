package com.facetlauncher.app.data.dailyphotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.facetlauncher.app.data.di.DailyWallpaperDataStore
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.DailyWallpaperState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The daily wallpaper's settings and progress ([DailyWallpaperState], its own DataStore, not in backups) and the one prepared photo
 * waiting for screen-off, a JPEG in the cache folder. `open` so tests can substitute it.
 */
@Singleton
open class DailyWallpaperRepository @Inject constructor(
    @DailyWallpaperDataStore private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val SOURCE = stringPreferencesKey("source")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val INCLUDE_LOCK = booleanPreferencesKey("include_lock")
        val PAUSED = booleanPreferencesKey("paused")
        val CHECKED_DAY = stringPreferencesKey("checked_day")
        val ATTEMPTS_DAY = stringPreferencesKey("attempts_day")
        val ATTEMPTS = intPreferencesKey("attempts")
        val PENDING = stringPreferencesKey("pending")
        val APPLYING = booleanPreferencesKey("applying")
        val CURRENT = stringPreferencesKey("current")
        val CURRENT_SET_AT = longPreferencesKey("current_set_at")
        val SYSTEM_ID = intPreferencesKey("system_id")
        val LOCK_ID = intPreferencesKey("lock_id")
        val HISTORY = stringPreferencesKey("history")
        val PROBLEM = stringPreferencesKey("problem")
    }

    private val pendingFile get() = File(File(context.cacheDir, "daily_photos"), PENDING_FILE)

    open val state: Flow<DailyWallpaperState> = dataStore.data.map { it.toState() }

    open suspend fun get(): DailyWallpaperState = state.first()

    /** An atomic read-modify-write of the whole state. */
    open suspend fun update(transform: (DailyWallpaperState) -> DailyWallpaperState) {
        dataStore.edit { preferences -> preferences.write(transform(preferences.toState())) }
    }

    /** Stores [bitmap] as the photo waiting for screen-off; `false` when it could not be written. */
    open suspend fun savePending(bitmap: Bitmap): Boolean = withContext(ioDispatcher) {
        runCatching {
            val file = pendingFile
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, "$PENDING_FILE.tmp")
            temp.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)) }
            check(temp.renameTo(file) || (file.delete() && temp.renameTo(file)))
        }.isSuccess
    }

    open suspend fun readPending(): Bitmap? = withContext(ioDispatcher) {
        runCatching { pendingFile.takeIf { it.isFile }?.let { BitmapFactory.decodeFile(it.path) } }.getOrNull()
    }

    open suspend fun deletePending() {
        withContext(ioDispatcher) { pendingFile.delete() }
    }

    private fun Preferences.toState(): DailyWallpaperState = DailyWallpaperState(
        enabled = this[Keys.ENABLED] ?: false,
        source = this[Keys.SOURCE]?.let { runCatching { DailyPhotoSource.valueOf(it) }.getOrNull() } ?: DailyPhotoSource.BING,
        wifiOnly = this[Keys.WIFI_ONLY] ?: true,
        includeLock = this[Keys.INCLUDE_LOCK] ?: false,
        paused = this[Keys.PAUSED] ?: false,
        checkedDay = this[Keys.CHECKED_DAY]?.toDayOrNull(),
        attemptsDay = this[Keys.ATTEMPTS_DAY]?.toDayOrNull(),
        attempts = this[Keys.ATTEMPTS] ?: 0,
        pending = this[Keys.PENDING]?.toPhotoOrNull(),
        applying = this[Keys.APPLYING] ?: false,
        current = this[Keys.CURRENT]?.toPhotoOrNull(),
        currentSetAtMillis = this[Keys.CURRENT_SET_AT],
        systemId = this[Keys.SYSTEM_ID] ?: DailyWallpaperState.NO_ID,
        lockId = this[Keys.LOCK_ID] ?: DailyWallpaperState.NO_ID,
        history = this[Keys.HISTORY].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
        problem = this[Keys.PROBLEM]?.let { runCatching { DailyWallpaperProblem.valueOf(it) }.getOrNull() },
    )

    private fun MutablePreferences.write(state: DailyWallpaperState) {
        this[Keys.ENABLED] = state.enabled
        this[Keys.SOURCE] = state.source.name
        this[Keys.WIFI_ONLY] = state.wifiOnly
        this[Keys.INCLUDE_LOCK] = state.includeLock
        this[Keys.PAUSED] = state.paused
        setOrRemove(Keys.CHECKED_DAY, state.checkedDay?.toString())
        setOrRemove(Keys.ATTEMPTS_DAY, state.attemptsDay?.toString())
        this[Keys.ATTEMPTS] = state.attempts
        setOrRemove(Keys.PENDING, state.pending?.encode())
        this[Keys.APPLYING] = state.applying
        setOrRemove(Keys.CURRENT, state.current?.encode())
        state.currentSetAtMillis?.let { this[Keys.CURRENT_SET_AT] = it } ?: remove(Keys.CURRENT_SET_AT)
        this[Keys.SYSTEM_ID] = state.systemId
        this[Keys.LOCK_ID] = state.lockId
        this[Keys.HISTORY] = state.history.take(DailyWallpaperState.HISTORY_SIZE).joinToString(SEPARATOR)
        setOrRemove(Keys.PROBLEM, state.problem?.name)
    }

    private fun MutablePreferences.setOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value != null) this[key] = value else remove(key)
    }

    private companion object {
        const val PENDING_FILE = "pending.jpg"
        const val JPEG_QUALITY = 95
        const val SEPARATOR = "\n"
        const val FIELD_SEPARATOR = '\u001F'

        fun String.toDayOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

        fun DailyWallpaperPhoto.encode(): String = listOf(imageUrl, title, credit).joinToString(FIELD_SEPARATOR.toString()) { it.replace(FIELD_SEPARATOR, ' ') }

        fun String.toPhotoOrNull(): DailyWallpaperPhoto? {
            val parts = split(FIELD_SEPARATOR)
            return if (parts.size == 3 && parts[0].isNotEmpty()) DailyWallpaperPhoto(parts[0], parts[1], parts[2]) else null
        }
    }
}
