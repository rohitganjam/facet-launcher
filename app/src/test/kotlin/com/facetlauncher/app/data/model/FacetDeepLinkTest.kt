package com.facetlauncher.app.data.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FacetDeepLinkTest {

    @Test
    fun `parseFacetIdFromDeepLink round-trips a uri built by buildFacetDeepLinkUri`() {
        assertEquals(42L, parseFacetIdFromDeepLink(buildFacetDeepLinkUri(42L)))
    }

    @Test
    fun `parseFacetIdFromDeepLink rejects a uri with a different scheme`() {
        assertNull(parseFacetIdFromDeepLink(Uri.parse("https://facet/42")))
    }

    @Test
    fun `parseFacetIdFromDeepLink rejects a uri with a different host`() {
        assertNull(parseFacetIdFromDeepLink(Uri.parse("facetlauncher://not-facet/42")))
    }

    @Test
    fun `parseFacetIdFromDeepLink rejects a non-numeric id segment`() {
        assertNull(parseFacetIdFromDeepLink(Uri.parse("facetlauncher://facet/not-a-number")))
    }
}
