package com.facetlauncher.app.data.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * An invisible trampoline that opens a widget's configure activity and hands its result back to the caller.
 * The only public way to start one is [android.appwidget.AppWidgetHost.startAppWidgetConfigureActivityForResult], which
 * needs an Activity and a request code; it is also the only way that works for a Work Profile provider and for a configure
 * activity that isn't exported. Launch it with [AppWidgetRepository.createConfigureIntent] and a plain StartActivityForResult.
 */
@AndroidEntryPoint
class WidgetConfigureActivity : ComponentActivity() {

    @Inject lateinit var appWidgetRepository: AppWidgetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recreated while the configure activity is open: its result still comes back here.
        if (savedInstanceState != null) return
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID ||
            !appWidgetRepository.startConfigureActivity(this, appWidgetId, REQUEST_CONFIGURE)
        ) {
            finishWith(Activity.RESULT_CANCELED, null)
        }
    }

    @Deprecated("Required by AppWidgetHost.startAppWidgetConfigureActivityForResult, which reports through onActivityResult.")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONFIGURE) finishWith(resultCode, data)
    }

    private fun finishWith(resultCode: Int, data: Intent?) {
        setResult(resultCode, data)
        finish()
    }

    private companion object {
        const val REQUEST_CONFIGURE = 1
    }
}
