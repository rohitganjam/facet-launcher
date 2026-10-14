package com.facetlauncher.app.data

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Opens another app's notification by sending the intent that app attached to it. */
@Singleton
class NotificationOpener @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Returns whether the intent was sent. False for a notification with no intent, or one the app has since cancelled. */
    fun open(contentIntent: PendingIntent?): Boolean {
        if (contentIntent == null) return false
        return runCatching { contentIntent.send(context, 0, null, null, null, null, startOptions()) }.isSuccess
    }

    // From Android 14 the sender has to opt in to the activity it starts coming to the front.
    private fun startOptions(): Bundle? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }
}
