package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Samsung Secure Folder's own launchable front door — a completely ordinary installed app, not an OS profile like Work Profile/Private Space. */
private const val SECURE_FOLDER_PACKAGE_NAME = "com.samsung.knox.securefolder"

/**
 * Samsung's Secure Folder is a real, normal app (`com.samsung.knox.securefolder`) installed in
 * the personal profile — its contents run inside a Work-Profile-shaped Android user under the
 * hood (Knox), but that user is deliberately not exposed to third-party launchers, so there's
 * nothing to enumerate the way [AppRepository]/[WorkProfileRepository] do for a real Work
 * Profile. This only offers a direct way *in*: Samsung's own "Add Secure Folder to Apps screen"
 * setting can hide its icon from the regular app list, so a user who's done that still needs a
 * way to reach it without digging through system Settings.
 */
@Singleton
class SecureFolderRepository @Inject constructor(@ApplicationContext private val context: Context) {

    /** `null` on any non-Samsung device, or a Samsung device without Secure Folder set up. */
    fun launchIntent(): Intent? = context.packageManager.getLaunchIntentForPackage(SECURE_FOLDER_PACKAGE_NAME)
}
