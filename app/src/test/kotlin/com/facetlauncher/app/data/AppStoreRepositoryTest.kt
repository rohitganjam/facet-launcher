package com.facetlauncher.app.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AppStoreRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AppStoreRepository(context)

    private val playStore = ComponentName(PLAY_STORE_PACKAGE, "com.google.android.finsky.activities.MainActivity")
    private val otherStore = ComponentName("com.aurora.store", ".MainActivity")
    private val browser = ComponentName("com.example.browser", ".BrowserActivity")

    private fun installMarketHandler(component: ComponentName) = install(component, browsable = false) {
        addDataScheme("market")
        addDataAuthority("search", null)
    }

    private fun installBrowser() = install(browser, browsable = true) { addDataScheme("https") }

    private fun install(component: ComponentName, browsable: Boolean, data: IntentFilter.() -> Unit) {
        val packageManager = shadowOf(context.packageManager)
        packageManager.addActivityIfNotPresent(component)
        packageManager.addIntentFilterForActivity(
            component,
            IntentFilter(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                if (browsable) addCategory(Intent.CATEGORY_BROWSABLE)
                data()
            },
        )
    }

    @Test
    fun `the Play Store wins when it and another store are both installed`() = runTest {
        // Given the Play Store, another store and a browser
        installMarketHandler(otherStore)
        installMarketHandler(playStore)
        installBrowser()

        // Then the Play Store is offered
        assertEquals(AppStore.PLAY_STORE, repository.availableStore())
    }

    @Test
    fun `another store is offered when there is no Play Store`() = runTest {
        // Given only a non-Google store and a browser
        installMarketHandler(otherStore)
        installBrowser()

        // Then that store is offered
        assertEquals(AppStore.OTHER_STORE, repository.availableStore())
    }

    @Test
    fun `a disabled Play Store falls back to the next option`() = runTest {
        // Given a Play Store the user disabled, and a browser
        installMarketHandler(playStore)
        installBrowser()
        context.packageManager.setComponentEnabledSetting(playStore, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0)

        // Then the web is offered instead
        assertEquals(AppStore.WEB, repository.availableStore())
    }

    @Test
    fun `the web is offered when no store is installed`() = runTest {
        // Given only a browser
        installBrowser()

        // Then Google Play on the web is offered
        assertEquals(AppStore.WEB, repository.availableStore())
    }

    @Test
    fun `nothing is offered without a store or a browser`() = runTest {
        // Given neither a store nor a browser
        // Then the button has nowhere to go
        assertNull(repository.availableStore())
    }

    @Test
    fun `the Play Store search targets the Play Store with the trimmed query`() {
        // When building the Play Store search for a padded query
        val intent = repository.searchIntent(AppStore.PLAY_STORE, "  duo lingo ")

        // Then it opens the Play Store's app search for exactly what was typed
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(PLAY_STORE_PACKAGE, intent.`package`)
        assertEquals("market", intent.data?.scheme)
        assertEquals("search", intent.data?.authority)
        assertEquals("duo lingo", intent.data?.getQueryParameter("q"))
        assertEquals("apps", intent.data?.getQueryParameter("c"))
    }

    @Test
    fun `another store's search names no package so any market handler can answer`() {
        // When building the search for a store other than the Play Store
        val intent = repository.searchIntent(AppStore.OTHER_STORE, "maps")

        // Then it is a plain market search
        assertNull(intent.`package`)
        assertEquals("market://search?q=maps&c=apps", intent.dataString)
    }

    @Test
    fun `the web search encodes the query for Google Play`() {
        // When building the web search for a query with characters that need encoding
        val intent = repository.searchIntent(AppStore.WEB, "a&b c")

        // Then it opens Google Play's search page with the query intact
        assertEquals("https", intent.data?.scheme)
        assertEquals("play.google.com", intent.data?.host)
        assertEquals("/store/search", intent.data?.path)
        assertEquals("a&b c", intent.data?.getQueryParameter("q"))
        assertEquals(true, intent.hasCategory(Intent.CATEGORY_BROWSABLE))
    }
}
