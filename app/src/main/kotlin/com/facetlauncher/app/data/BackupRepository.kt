package com.facetlauncher.app.data

import android.content.Context
import android.net.Uri
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.BackupBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun writeBackup(uri: Uri, bundle: BackupBundle) = withContext(ioDispatcher) {
        val bytes = json.encodeToString(BackupBundle.serializer(), bundle).toByteArray(Charsets.UTF_8)
        // Clear the file before writing: some document providers ignore the "wt" mode, and the leftover tail of a
        // longer file this one overwrites would corrupt the JSON.
        context.contentResolver.openFileDescriptor(uri, "wt")?.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).use { stream ->
                stream.channel.truncate(0)
                stream.write(bytes)
            }
        } ?: error("Could not open $uri for writing")
    }

    /** `null` means the file at [uri] isn't valid JSON, or isn't shaped like a [BackupBundle] at all — a clean "not a backup file" signal rather than an exception the caller has to know to catch. */
    suspend fun readBackup(uri: Uri): BackupBundle? = withContext(ioDispatcher) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@withContext null
        decode(text) ?: firstJsonObject(text)?.let(::decode)
    }

    private fun decode(text: String): BackupBundle? =
        runCatching { json.decodeFromString(BackupBundle.serializer(), text) }.getOrNull()

    /** Recovers backups exported before the truncation fix, which ended with the leftover tail of an older, longer file. */
    private fun firstJsonObject(text: String): String? {
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        var end = -1
        for (i in start until text.length) {
            val c = text[i]
            when {
                escaped -> escaped = false
                inString -> if (c == '\\') escaped = true else if (c == '"') inString = false
                c == '"' -> inString = true
                c == '{' -> depth++
                c == '}' -> if (--depth == 0) {
                    end = i
                    break
                }
            }
        }
        return if (end < 0) null else text.substring(start, end + 1)
    }
}
