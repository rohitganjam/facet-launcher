package com.facetlauncher.app.data

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Wraps the system default-launcher check and request — whether Facet is the currently resolved `HOME` activity, and how to ask to become it. */
@Singleton
class DefaultLauncherRepository @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun isDefaultLauncher(): Boolean = withContext(Dispatchers.Default) {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        resolved?.activityInfo?.packageName == context.packageName
    }

    /**
     * An in-place system confirmation dialog via [RoleManager.createRequestRoleIntent] when the
     * `ROLE_HOME` role API is available and not already held, falling back to the same
     * `ACTION_MANAGE_DEFAULT_APPS_SETTINGS` screen this app's own Settings row has always used
     * otherwise (older API levels, or an OEM that doesn't expose the role).
     */
    fun requestDefaultLauncherIntent(): Intent {
        val roleManager = context.getSystemService(RoleManager::class.java)
        return if (roleManager != null &&
            roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
        } else {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        }
    }
}
