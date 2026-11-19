package com.facetlauncher.app.data.dailyphotos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BingFeedTest {

    private val sample = """
        {"images":[
          {"startdate":"20261119","enddate":"20261120","urlbase":"/th?id=OHR.KnowledgeSculpture_EN-US974","title":"Where ideas take shape",
           "copyright":"The \"Body of Knowledge\" sculpture by Jaume Plensa, Frankfurt (© Todamo/Shutterstock)","hsh":"x"},
          {"startdate":"20261118","urlbase":"/th?id=OHR.Owls_EN-US1","title":"Nest","copyright":"Barn owls"}
        ],"tooltips":{}}
    """.trimIndent()

    @Test
    fun `a response gives its photos with a preview and the UHD original on bing's host and the date`() {
        // When
        val photos = parseBingFeed(sample)!!

        // Then
        assertEquals(2, photos.size)
        assertEquals("https://www.bing.com/th?id=OHR.KnowledgeSculpture_EN-US974_640x480.jpg", photos[0].thumbUrl)
        assertEquals("https://www.bing.com/th?id=OHR.KnowledgeSculpture_EN-US974_UHD.jpg", photos[0].imageUrl)
        assertEquals("2026-11-19", photos[0].date)
    }

    @Test
    fun `a copyright with a credit in brackets gives the caption as the title and the credit as given`() {
        val photos = parseBingFeed(sample)!!

        assertEquals("The \"Body of Knowledge\" sculpture by Jaume Plensa, Frankfurt", photos[0].title)
        assertEquals("© Todamo/Shutterstock", photos[0].credit)
    }

    @Test
    fun `a copyright without a credit in brackets is the credit and bing's own title is the title`() {
        val photos = parseBingFeed(sample)!!

        assertEquals("Nest", photos[1].title)
        assertEquals("Barn owls", photos[1].credit)
    }

    @Test
    fun `an entry without an address that can be built is left out and a date that is not eight digits is none`() {
        val json = """
            {"images":[
              {"urlbase":"/th?id=OHR.Ok","startdate":"2026-11-19"},
              {"title":"no base"},
              {"urlbase":"https://evil.example/th?id=x"}
            ]}
        """.trimIndent()

        val photos = parseBingFeed(json)!!

        assertEquals(1, photos.size)
        assertNull(photos[0].date)
    }

    @Test
    fun `something that is not a response is not parsed`() {
        assertNull(parseBingFeed("not json"))
        assertNull(parseBingFeed("[]"))
        assertNull(parseBingFeed("""{"error":"busy"}"""))
    }
}
