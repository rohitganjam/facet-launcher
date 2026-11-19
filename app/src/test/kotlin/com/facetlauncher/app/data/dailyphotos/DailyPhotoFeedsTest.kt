package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhotoSource
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPhotoFeedsTest {

    @Test
    fun `the country is the phone's own with no list, and the default without one`() {
        assertEquals("de", feedCountry(Locale("de", "DE")))
        assertEquals("nl", feedCountry(Locale("nl", "NL")))
        assertEquals("us", feedCountry(Locale("en")))
    }

    @Test
    fun `the locale is the phone's language for its own country and english for the us otherwise`() {
        assertEquals("de-DE", localeTag("de", Locale("de", "DE")))
        assertEquals("hi-IN", localeTag("in", Locale("hi", "IN")))
        assertEquals("en-US", localeTag("us", Locale("en")))
        assertEquals("en-US", localeTag("us", Locale("nl", "NL")))
    }

    @Test
    fun `only https addresses on bing and microsoft's spotlight image host are allowed`() {
        assertTrue(isAllowedImageUrl("https://www.bing.com/th?id=OHR.A_UHD.jpg"))
        assertTrue(isAllowedImageUrl("https://res.public.onecdn.static.microsoft/creativeservice/a.jpg"))
        assertFalse(isAllowedImageUrl("http://www.bing.com/th?id=OHR.A_UHD.jpg"))
        assertFalse(isAllowedImageUrl("https://bing.com/th?id=a"))
        assertFalse(isAllowedImageUrl("https://www.bing.com.evil.example/a.jpg"))
        assertFalse(isAllowedImageUrl("https://other.onecdn.static.microsoft/a.jpg"))
        assertFalse(isAllowedImageUrl("https://img.peapix.com/a.jpg"))
        assertFalse(isAllowedImageUrl("not a url"))
    }

    @Test
    fun `each source is read with its own format`() {
        val bing = """{"images":[{"startdate":"20261119","urlbase":"/th?id=OHR.A","title":"t","copyright":"c"}]}"""
        assertEquals(1, parseFeed(DailyPhotoSource.BING, bing)!!.size)
        assertNull(parseFeed(DailyPhotoSource.SPOTLIGHT, bing))
        assertNull(parseFeed(DailyPhotoSource.BING, """{"batchrsp":{"items":[]}}"""))
    }

    @Test
    fun `the feed addresses carry the phone's country and language`() {
        assertEquals(
            "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=7&mkt=de-DE",
            HttpDailyPhotoApi.bingFeedUrl("de", Locale("de", "DE")),
        )
        assertEquals(
            "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=7&mkt=en-US",
            HttpDailyPhotoApi.bingFeedUrl("nl", Locale("en", "GB")),
        )
        assertEquals(
            "https://fd.api.iris.microsoft.com/v4/api/selection?placement=88000820&bcnt=4&fmt=json&country=NL&locale=nl-NL",
            HttpDailyPhotoApi.spotlightUrl("NL", "nl-NL"),
        )
    }

    @Test
    fun `the browser agent is tried only when the app's own gets nothing`() = kotlinx.coroutines.test.runTest {
        // Given an agent that is refused
        val agents = mutableListOf<String>()

        // When
        val refusedOnce = withBrowserAgentFallback("Own/1") { agent -> agents += agent; "feed".takeIf { agent == BROWSER_USER_AGENT } }
        val accepted = withBrowserAgentFallback("Own/1") { agent -> agents += agent; "feed" }
        val refusedBoth = withBrowserAgentFallback<String>("Own/1") { agent -> agents += agent; null }

        // Then
        assertEquals("feed", refusedOnce)
        assertEquals("feed", accepted)
        assertNull(refusedBoth)
        assertEquals(listOf("Own/1", BROWSER_USER_AGENT, "Own/1", "Own/1", BROWSER_USER_AGENT), agents)
    }
}
