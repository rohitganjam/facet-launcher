package com.lumenlauncher.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wraps the system default-launcher check — whether Lumen is the currently resolved `HOME` activity. */
@Singleton
class DefaultLauncherRepository @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun isDefaultLauncher(): Boolean = withContext(Dispatchers.Default) {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        resolved?.activityInfo?.packageName == context.packageName
    }
}
