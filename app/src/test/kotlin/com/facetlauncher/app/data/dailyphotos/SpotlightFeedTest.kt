package com.facetlauncher.app.data.dailyphotos

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpotlightFeedTest {

    private fun ad(title: String, image: String?) =
        """{"ad":{"title":"$title","copyright":"© $title","landscapeImage":{"asset":${image?.let { "\"$it\"" } ?: "null"}}}}"""

    /** Microsoft's response: each item's content is a JSON string inside the item. */
    private fun response(vararg ads: String) =
        ads.joinToString(prefix = """{"batchrsp":{"ver":"1.0","items":[""", postfix = "]}}") { "{\"item\":" + quote(it) + "}" }

    private fun quote(json: String) = "\"" + json.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private val host = "https://res.public.onecdn.static.microsoft/creativeservice"

    @Test
    fun `a response gives its photos with the landscape image as preview and original and the credit as given`() {
        // Given
        val json = response(ad("Frozen fractures", "$host/a_3840x2160.jpg"), ad("Puma power", "$host/b_3840x2160.jpg"))

        // When
        val photos = parseSpotlightFeed(json)!!

        // Then
        assertEquals(listOf("Frozen fractures", "Puma power"), photos.map { it.title })
        assertEquals("© Frozen fractures", photos[0].credit)
        assertEquals("$host/a_3840x2160.jpg", photos[0].imageUrl)
        assertEquals(photos[0].imageUrl, photos[0].thumbUrl)
        assertNull(photos[0].date)
    }

    @Test
    fun `an entry without an allowed image or that is not readable is left out and a repeat is dropped`() {
        // Given
        val json = response(
            ad("ok", "$host/a.jpg"),
            ad("no image", null),
            ad("elsewhere", "https://evil.example/a.jpg"),
            ad("repeat", "$host/a.jpg"),
            "not json",
        )

        // When
        val photos = parseSpotlightFeed(json)!!

        // Then
        assertEquals(listOf("ok"), photos.map { it.title })
    }

    @Test
    fun `something that is not a response is not parsed`() {
        assertNull(parseSpotlightFeed("not json"))
        assertNull(parseSpotlightFeed("[]"))
        assertNull(parseSpotlightFeed("""{"error":"busy"}"""))
    }

    @Test
    fun `several responses merge into one and a failed one is skipped`() {
        // Given
        val first = response(ad("a", "$host/a.jpg"), ad("b", "$host/b.jpg"))
        val second = response(ad("b", "$host/b.jpg"), ad("c", "$host/c.jpg"))

        // When
        val merged = mergeSpotlightResponses(listOf(first, "garbage", second))

        // Then
        assertEquals(listOf("a", "b", "c"), parseSpotlightFeed(merged!!)!!.map { it.title })
        assertNull(mergeSpotlightResponses(emptyList()))
        assertNull(mergeSpotlightResponses(listOf("garbage")))
    }

    @Test
    fun `a missing title is the first line of the hover text and a missing credit its copyright line`() {
        // Given an ad with no title or copyright, and a hover text of a caption, a credit and the usual hint
        val hover = "Ice cracks on Lake Khövsgöl, Mongolia\\r\\n© sng_j / Shutterstock\\r\\nRight-click to learn more"
        val json = response("""{"ad":{"iconHoverText":"$hover","landscapeImage":{"asset":"$host/a.jpg"}}}""")

        // When
        val photo = parseSpotlightFeed(json)!!.single()

        // Then
        assertEquals("Ice cracks on Lake Khövsgöl, Mongolia", photo.title)
        assertEquals("© sng_j / Shutterstock", photo.credit)
    }

    @Test
    fun `without a title or a usable hover text the description is the title`() {
        val hover = "Right-click to learn more"
        val json = response("""{"ad":{"description":" A long description. ","iconHoverText":"$hover","landscapeImage":{"asset":"$host/a.jpg"}}}""")

        val photo = parseSpotlightFeed(json)!!.single()

        assertEquals("A long description.", photo.title)
        assertEquals("", photo.credit)
    }

    @Test
    fun `the candidates go from the phone's language to english for its country to english for the us`() {
        assertEquals(listOf("NL" to "nl-NL", "NL" to "en-NL", "US" to "en-US"), spotlightCandidates("nl", Locale("nl", "NL")))
        assertEquals(listOf("US" to "en-US"), spotlightCandidates("us", Locale("en", "US")))
        assertEquals(listOf("US" to "en-US"), spotlightCandidates("us", Locale("en")))
    }

    @Test
    fun `the first candidate with photos wins and the other calls are made only for it`() = kotlinx.coroutines.test.runTest {
        // Given the phone's language is refused and English for its country answers
        val asked = mutableListOf<String>()
        val answer = response(ad("a", "$host/a.jpg"))
        val none = """{"batchrsp":{"ver":"1.0","errors":[{"code":2040}]}}"""

        // When
        val json = fetchSpotlight(spotlightCandidates("xx", Locale("zz", "XX")), calls = 3, enough = 8) { country, locale ->
            asked += "$country/$locale"
            if (locale == "en-XX") answer else none
        }

        // Then the refused locale was asked once, and English for the country three times
        assertEquals(listOf("XX/zz-XX", "XX/en-XX", "XX/en-XX", "XX/en-XX"), asked)
        assertEquals(listOf("a"), parseSpotlightFeed(json!!)!!.map { it.title })
    }

    @Test
    fun `when every candidate is refused or fails there is no feed`() = kotlinx.coroutines.test.runTest {
        // Given
        val asked = mutableListOf<String>()

        // When
        val json = fetchSpotlight(spotlightCandidates("xx", Locale("zz", "XX")), calls = 3, enough = 8) { country, locale ->
            asked += "$country/$locale"
            if (asked.size == 2) null else """{"batchrsp":{"errors":[{"code":2040}]}}"""
        }

        // Then each candidate was tried once
        assertNull(json)
        assertEquals(listOf("XX/zz-XX", "XX/en-XX", "US/en-US"), asked)
    }

    @Test
    fun `a failed later call does not lose the photos already had`() = kotlinx.coroutines.test.runTest {
        var calls = 0

        val json = fetchSpotlight(listOf("US" to "en-US"), calls = 3, enough = 8) { _, _ -> if (calls++ == 0) response(ad("a", "$host/a.jpg")) else null }

        assertEquals(listOf("a"), parseSpotlightFeed(json!!)!!.map { it.title })
    }

    @Test
    fun `asking stops once there are enough different photos and a repeat answer does not count twice`() = kotlinx.coroutines.test.runTest {
        // Given answers of four photos each, the third repeating the first
        val answers = listOf(
            response(*(1..4).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray()),
            response(*(5..8).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray()),
        )
        var asked = 0

        // When up to five calls are allowed and eight photos are enough
        val json = fetchSpotlight(listOf("US" to "en-US"), calls = 5, enough = 8) { _, _ -> answers[asked++] }

        // Then two calls were made and all eight photos are there
        assertEquals(2, asked)
        assertEquals(8, parseSpotlightFeed(json!!)!!.size)
    }

    @Test
    fun `asking goes on while answers repeat photos already had, up to the most calls`() = kotlinx.coroutines.test.runTest {
        var asked = 0
        val same = response(*(1..4).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray())

        val json = fetchSpotlight(listOf("US" to "en-US"), calls = 3, enough = 8) { _, _ -> asked++; same }

        assertEquals(3, asked)
        assertEquals(4, parseSpotlightFeed(json!!)!!.size)
    }

    @Test
    fun `the list is not cut at the number that stops the calls`() = kotlinx.coroutines.test.runTest {
        // Given three answers: four photos, four with two repeats, four new ones
        val answers = listOf(
            response(*(1..4).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray()),
            response(*(3..6).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray()),
            response(*(7..10).map { ad("p$it", "$host/p$it.jpg") }.toTypedArray()),
        )
        var asked = 0

        // When eight is enough
        val json = fetchSpotlight(listOf("US" to "en-US"), calls = 3, enough = 8) { _, _ -> answers[asked++] }

        // Then all three calls were made (six photos were not enough) and all ten photos are kept
        assertEquals(3, asked)
        assertEquals(10, parseSpotlightFeed(json!!)!!.size)
    }
}
