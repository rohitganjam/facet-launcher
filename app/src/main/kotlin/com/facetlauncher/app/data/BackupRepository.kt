package com.facetlauncher.app.data

import android.content.Context
import android.net.Uri
import com.facetlauncher.app.data.model.BackupBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * F14 Backup & Restore — the only place `ContentResolver`'s document I/O is touched, per
 * `CLAUDE.md`'s layering rule. [uri] comes from the caller's own
 * `ActivityResultContracts.CreateDocument`/`OpenDocument` launcher (a user-picked destination via
 * the system's own document picker — this never guesses a file path), and every read/write is
 * dispatched on [Dispatchers.IO], matching every other I/O-bound `Repository` in this codebase.
 */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun writeBackup(uri: Uri, bundle: BackupBundle) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.encodeToString(BackupBundle.serializer(), bundle).toByteArray(Charsets.UTF_8))
        } ?: error("Could not open an output stream for $uri")
    }

    /** `null` means the file at [uri] isn't valid JSON, or isn't shaped like a [BackupBundle] at all — a clean "not a backup file" signal rather than an exception the caller has to know to catch. */
    suspend fun readBackup(uri: Uri): BackupBundle? = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@withContext null
        runCatching { json.decodeFromString(BackupBundle.serializer(), text) }.getOrNull()
    }
}
