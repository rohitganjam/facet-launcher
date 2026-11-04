package com.facetlauncher.app

import android.app.Application
import android.content.Context
import android.icu.text.PluralRules
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
