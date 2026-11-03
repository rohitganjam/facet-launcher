package com.facetlauncher.app

import android.app.Application
import android.content.Context
import android.icu.text.PluralRules
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import kotlin.concurrent.thread

@HiltAndroidApp
class FacetApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // ICU's plural-rules table loads lazily on first use and is slow; start it as early as possible
        // (before Hilt's onCreate) so the first pluralStringResource doesn't stall the main thread.
        thread(name = "plural-rules-warmup", isDaemon = true) {
            runCatching { PluralRules.forLocale(Locale.getDefault()) }
        }
    }
}
