package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.facetlauncher.app.LauncherActivity
import com.facetlauncher.app.R
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.buildFacetDeepLinkUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val SHORTCUT_ID_PREFIX = "facet_"
private const val SHORTCUT_LABEL_PREFIX = "Switch to "

/**
 * Publishes one dynamic [ShortcutManagerCompat] shortcut per facet, so system automation pickers
 * (Samsung Modes & Routines, Tasker, etc.) can discover and trigger a facet switch without opening
 * Facet's own UI. Kept in lockstep with [FacetRepository]'s live state by
 * [com.facetlauncher.app.domain.SyncFacetShortcutsUseCase] — never hand-triggered from a single
 * add/rename/delete callsite. [com.facetlauncher.app.data.model.FacetLimits.PRO_MAX_FACETS] (10) is well under
 * [ShortcutManagerCompat]'s per-activity cap, so no eviction/capacity logic is needed here.
 *
 * The shortcut's *displayed* name and its *trigger* key are deliberately different fields: the
 * short/long label is `"Switch to <facet's live name>"` (what the user and the OS picker see —
 * reads as an action, not just a name, since that's how it shows up in an automation picker), while
 * the shortcut id (`"facet_<id>"`) and the launch intent's `Uri` ([buildFacetDeepLinkUri]) both
 * carry the facet's numeric, stable [FacetEntity.id] — what
 * [com.facetlauncher.app.domain.ActivateFacetByIdUseCase] actually resolves against. Renaming a
 * facet updates the label on the next [syncShortcuts] call without touching the shortcut id or its
 * trigger `Uri`, so an already-configured automation rule keeps working and just shows the new
 * name.
 */
@Singleton
class FacetShortcutRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Full atomic replace, not a hand-rolled add/update/remove diff — simpler and can't drift from [facets]. */
    fun syncShortcuts(facets: List<FacetEntity>) {
        ShortcutManagerCompat.setDynamicShortcuts(context, facets.map(::shortcutFor))
    }

    private fun shortcutFor(facet: FacetEntity): ShortcutInfoCompat {
        val intent = Intent(Intent.ACTION_VIEW, buildFacetDeepLinkUri(facet.id), context, LauncherActivity::class.java)
        val label = "$SHORTCUT_LABEL_PREFIX${facet.name}"
        return ShortcutInfoCompat.Builder(context, "$SHORTCUT_ID_PREFIX${facet.id}")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setRank(facet.position)
            .setIntent(intent)
            .build()
    }
}
