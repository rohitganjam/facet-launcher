package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.AppStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val PLAY_STORE_PACKAGE = "com.android.vending"

/** Finds a store for the Drawer's no-results app search and builds the intent that searches it. */
@Singleton
class AppStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /** The Play Store if it's installed and enabled, else any other `market://` store, else a browser for Google Play on the web; `null` if none. */
    suspend fun availableStore(): AppStore? = withContext(ioDispatcher) {
        AppStore.entries.firstOrNull { resolves(searchIntent(it, "")) }
    }

    /** Searches [store] for [query] as an app; the Play Store is targeted by package so another store never shows a chooser over it. */
    fun searchIntent(store: AppStore, query: String): Intent {
        val uri = when (store) {
            AppStore.PLAY_STORE, AppStore.OTHER_STORE -> Uri.Builder().scheme("market").authority("search")
            AppStore.WEB -> Uri.Builder().scheme("https").authority("play.google.com").path("/store/search")
        }.appendQueryParameter("q", query.trim()).appendQueryParameter("c", "apps").build()
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (store == AppStore.PLAY_STORE) setPackage(PLAY_STORE_PACKAGE)
            if (store == AppStore.WEB) addCategory(Intent.CATEGORY_BROWSABLE)
        }
    }

    private fun resolves(intent: Intent): Boolean =
        context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
}
