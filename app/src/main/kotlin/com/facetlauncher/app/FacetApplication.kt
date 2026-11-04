package com.facetlauncher.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.icu.text.PluralRules
import android.os.Bundle
import com.facetlauncher.app.data.di.ApplicationScope
import com.facetlauncher.app.domain.EnsureCalendarColorsUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import java.util.Locale
import kotlin.concurrent.thread

@HiltAndroidApp
class FacetApplication : Application() {
    @Inject lateinit var ensureCalendarColors: EnsureCalendarColorsUseCase

    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // A calendar added or removed on the phone gets its colour at once, not only when a screen that lists calendars opens.
        ensureCalendarColors.keepAssigned().catch { }.launchIn(applicationScope)
        // Calendar access can be granted or revoked from system Settings while the process lives, with no callback.
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) = ensureCalendarColors.recheckAccess()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // ICU's plural-rules table loads lazily on first use and is slow; start it as early as possible
        // (before Hilt's onCreate) so the first pluralStringResource doesn't stall the main thread.
        thread(name = "plural-rules-warmup", isDaemon = true) {
            runCatching { PluralRules.forLocale(Locale.getDefault()) }
        }
    }
}
