package com.facetlauncher.app.data

import android.content.Context
import com.facetlauncher.app.data.model.ReleaseNotes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val NOTES_DIR = "whats_new"
private const val NOTES_EXTENSION = ".md"

/**
 * The in-app "What's new" content: one `assets/whats_new/<version>.md` per release, written (and signed
 * off) by `scripts/release.sh` before the build, separate from the `release_notes/` change log.
 */
@Singleton
open class ReleaseNotesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Every release that has notes, newest first. A file that can't be read or has no bullets is skipped. */
    open suspend fun releases(): List<ReleaseNotes> = withContext(Dispatchers.IO) {
        val assets = context.assets
        assets.list(NOTES_DIR).orEmpty()
            .filter { it.endsWith(NOTES_EXTENSION) }
            .mapNotNull { fileName ->
                val markdown = runCatching { assets.open("$NOTES_DIR/$fileName").bufferedReader().use { it.readText() } }.getOrNull()
                markdown?.let { parseReleaseNotes(fileName.removeSuffix(NOTES_EXTENSION), it) }
            }
            .sortedWith(NewestVersionFirst)
    }
}
