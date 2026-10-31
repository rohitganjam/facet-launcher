package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.ViewGroup

/**
 * The built widget view of each page in the clock stack. A view is made once and handed to whichever tile shows it, so
 * swiping to a page that was already built costs a re-attach, not a new inflate. [create] is `null` for a provider that is gone.
 */
internal class ClockWidgetHostCache(private val create: (Context, Int) -> AppWidgetHostView?) {
    private val views = HashMap<Int, AppWidgetHostView>()

    fun has(appWidgetId: Int): Boolean = appWidgetId in views

    /** The view for [appWidgetId], built on the first ask, and taken out of whatever last held it. */
    fun get(context: Context, appWidgetId: Int): AppWidgetHostView? {
        val view = views[appWidgetId] ?: create(context, appWidgetId)?.also { views[appWidgetId] = it } ?: return null
        (view.parent as? ViewGroup)?.removeView(view)
        return view
    }

    /** Drops the views of pages that left the stack. */
    fun retainOnly(appWidgetIds: Set<Int>) {
        views.keys.retainAll(appWidgetIds)
    }

    fun clear() = views.clear()
}
