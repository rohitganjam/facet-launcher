package com.facetlauncher.app.data.dailyphotos

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PeapixFeedTest {

    private val sample = """
        [
          {"title":"North Window, Arches National Park","copyright":"© lightphoto/Getty Image",
           "fullUrl":"https://img.peapix.com/a_1920.jpg","thumbUrl":"https://img.peapix.com/a_640.jpg","imageUrl":"https://img.peapix.com/a.jpg",
           "pageUrl":"https://peapix.com/bing/1","date":"2026-11-12"},
          {"title":"Braided glacial rivers","copyright":"© Someone",
           "fullUrl":"https://img.peapix.com/b_1920.jpg","thumbUrl":"https://img.peapix.com/b_640.jpg","imageUrl":"https://img.peapix.com/b.jpg"}
        ]
    """.trimIndent()

    @Test
    fun `a feed gives its photos with the credit exactly as given and the date when there is one`() {
        // When the feed is parsed
        val photos = parsePeapixFeed(sample)!!

        // Then both photos are there, the credit untouched, and only the first has a date
        assertEquals(2, photos.size)
        assertEquals("© lightphoto/Getty Image", photos[0].credit)
        assertEquals("https://img.peapix.com/a.jpg", photos[0].imageUrl)
        assertEquals("https://img.peapix.com/a_640.jpg", photos[0].thumbUrl)
        assertEquals("2026-11-12", photos[0].date)
        assertNull(photos[1].date)
    }

    @Test
    fun `something that is not a feed is not parsed`() {
        assertNull(parsePeapixFeed("not json"))
        assertNull(parsePeapixFeed("{\"error\":\"busy\"}"))
    }

    @Test
    fun `an entry without both image addresses, or from another host, is left out`() {
        // Given one good entry, one with no original, one from another host, and one over plain http
        val json = """
            [
              {"title":"ok","thumbUrl":"https://img.peapix.com/t.jpg","imageUrl":"https://img.peapix.com/o.jpg"},
              {"title":"no original","thumbUrl":"https://img.peapix.com/t.jpg"},
              {"title":"elsewhere","thumbUrl":"https://evil.example/t.jpg","imageUrl":"https://evil.example/o.jpg"},
              {"title":"plain","thumbUrl":"http://img.peapix.com/t.jpg","imageUrl":"http://img.peapix.com/o.jpg"}
            ]
        """.trimIndent()

        // Then only the good one is kept, and a missing credit reads as empty
        val photos = parsePeapixFeed(json)!!
        assertEquals(listOf("ok"), photos.map { it.title })
        assertEquals("", photos[0].credit)
    }

    @Test
    fun `only https addresses on peapix are allowed`() {
        assertTrue(isAllowedImageUrl("https://img.peapix.com/a.jpg"))
        assertTrue(isAllowedImageUrl("https://peapix.com/a.jpg"))
        assertFalse(isAllowedImageUrl("http://img.peapix.com/a.jpg"))
        assertFalse(isAllowedImageUrl("https://notpeapix.com/a.jpg"))
        assertFalse(isAllowedImageUrl("https://peapix.com.evil.example/a.jpg"))
        assertFalse(isAllowedImageUrl("file:///sdcard/a.jpg"))
        assertFalse(isAllowedImageUrl("not a url"))
    }

    @Test
    fun `the country is the phone's when peapix has it and the default otherwise`() {
        assertEquals("de", peapixCountry(Locale("de", "DE")))
        assertEquals("gb", peapixCountry(Locale("en", "GB")))
        assertEquals("us", peapixCountry(Locale("en", "US")))
        assertEquals("us", peapixCountry(Locale("nl", "NL")))
        assertEquals("us", peapixCountry(Locale("en")))
    }

    @Test
    fun `the feed address asks for a week and adds the country only for bing`() {
        assertEquals("https://peapix.com/bing/feed?n=7&country=de", PeapixApi.feedUrl(com.facetlauncher.app.data.model.DailyPhotoSource.BING, "de"))
        assertEquals("https://peapix.com/spotlight/feed?n=7", PeapixApi.feedUrl(com.facetlauncher.app.data.model.DailyPhotoSource.SPOTLIGHT, "de"))
    }
}
