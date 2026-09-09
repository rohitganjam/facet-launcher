package com.lumenlauncher.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves which installed app this device's own user already treats as their default handler
 * for a few common categories (browser, messaging, camera, mail, phone) — the same OS-level
 * resolution Android performs when, say, a `mailto:` link is tapped — via
 * [PackageManager.resolveActivity] against each category's representative [Intent], rather than
 * guessing at "well-known" package names (which drift stale across OEM skins and app updates).
 * Backs [com.lumenlauncher.app.domain.SelectPreviewAppsUseCase]'s bias for Settings →
 * Appearance's live preview card.
 */
@Singleton
class DefaultAppRepository @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * One package name per category, in [categoryIntents]' order, omitting any category with no
     * installed handler at all. Where the OS has no single unambiguous default (multiple apps
     * qualify and the user hasn't chosen one — [PackageManager.resolveActivity] with
     * [PackageManager.MATCH_DEFAULT_ONLY] returns null in that case), falls back to the first
     * candidate [PackageManager.queryIntentActivities] returns for that category — an
     * arbitrary-but-installed pick still reads as more "this device's own home screen" than
     * [com.lumenlauncher.app.domain.SelectPreviewAppsUseCase]'s own plain alphabetical fallback.
     */
    suspend fun getDefaultAppPackages(): List<String> = withContext(Dispatchers.Default) {
        categoryIntents().mapNotNull(::resolvePackage).distinct()
    }

    private fun resolvePackage(intent: Intent): String? {
        val packageManager = context.packageManager
        val default = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        if (default != null) return default
        return packageManager.queryIntentActivities(intent, 0).firstOrNull()?.activityInfo?.packageName
    }

    // Built fresh per call rather than as a top-level/companion `val` — an eagerly-constructed
    // `Intent`/`Uri.parse(...)` list would run at class-load time, which crashes any plain JVM
    // unit test that merely references this class (e.g. Mockito's `mock(DefaultAppRepository::class.java)`)
    // outside Robolectric's Android stub environment.
    private fun categoryIntents(): List<Intent> = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("http://")), // Browser
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")), // Messaging
        Intent(MediaStore.ACTION_IMAGE_CAPTURE), // Camera
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), // Mail
        Intent(Intent.ACTION_DIAL), // Phone
    )
}
