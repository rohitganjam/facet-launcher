package com.facetlauncher.app.data.model

import android.net.Uri

/**
 * The wire format shared by a facet's dynamic shortcut launch intent and any external deep link
 * targeting a facet — `facetlauncher://facet/{id}`, keyed on [com.facetlauncher.app.data.local.FacetEntity.id]
 * (stable), never its display name (renamable, not unique). See
 * [com.facetlauncher.app.data.FacetShortcutRepository]'s own doc for why the shortcut's *visible*
 * label and this *trigger* identifier are deliberately different fields.
 */
private const val FACET_DEEP_LINK_SCHEME = "facetlauncher"
private const val FACET_DEEP_LINK_HOST = "facet"

fun buildFacetDeepLinkUri(facetId: Long): Uri =
    Uri.Builder()
        .scheme(FACET_DEEP_LINK_SCHEME)
        .authority(FACET_DEEP_LINK_HOST)
        .appendPath(facetId.toString())
        .build()

/** `null` when [uri] isn't a `facetlauncher://facet/{id}` link, or its id segment isn't a valid `Long`. */
fun parseFacetIdFromDeepLink(uri: Uri): Long? {
    if (uri.scheme != FACET_DEEP_LINK_SCHEME || uri.authority != FACET_DEEP_LINK_HOST) return null
    return uri.lastPathSegment?.toLongOrNull()
}
