package com.facetlauncher.app.data.places

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CityListTest {

    private val rows = """
        Bengaluru	Karnataka	IN	12.9716	77.5946	8443675	Asia/Kolkata
        Pune	Maharashtra	IN	18.5204	73.8567	3124458	Asia/Kolkata
        Mumbai	Maharashtra	IN	19.0760	72.8777	12442373	Asia/Kolkata
        Navi Mumbai	Maharashtra	IN	19.0330	73.0297	1119477	Asia/Kolkata
        Puducherry	Puducherry	IN	11.9416	79.8083	657209	Asia/Kolkata
        London	England	GB	51.5074	-0.1278	8982000	Europe/London
        London	Ontario	CA	42.9849	-81.2453	383822	America/Toronto
        Zürich	Zurich	CH	47.3769	8.5417	402762	Europe/Zurich
        New York City	New York	US	40.7128	-74.0060	8336817	America/New_York
        Los Angeles	California	US	34.0522	-118.2437	3898747	America/Los_Angeles
    """.trimIndent().lines().asSequence()

    private val list = CityList.parse(rows)

    @Test
    fun `every well formed row is read`() {
        assertEquals(10, list.size)
    }

    @Test
    fun `a row that does not parse is skipped`() {
        val broken = sequenceOf("Nowhere\t\tXX\tnot-a-number\t1.0\t5\tUTC", "too\tshort", "", "Pune\tMaharashtra\tIN\t18.52\t73.85\t3124458\tAsia/Kolkata")

        assertEquals(1, CityList.parse(broken).size)
    }

    @Test
    fun `search ranks a prefix before a word start before a substring`() {
        // Given "mumbai" matches Mumbai (prefix), Navi Mumbai (second word), and nothing as a bare substring here
        val names = list.search("mumbai").map { it.name }

        assertEquals(listOf("Mumbai", "Navi Mumbai"), names)
    }

    @Test
    fun `search finds a substring last`() {
        assertEquals(listOf("Pune", "Puducherry"), list.search("pu").map { it.name })
        assertEquals(listOf("Puducherry"), list.search("cherry").map { it.name })
    }

    @Test
    fun `within one rank the larger city comes first`() {
        // Given two Londons
        val london = list.search("london")

        assertEquals(listOf("GB", "CA"), london.map { it.countryCode })
    }

    @Test
    fun `search ignores case and accents`() {
        assertEquals("Zürich", list.search("zurich").single().name)
        assertEquals("Zürich", list.search("ZÜRICH").single().name)
    }

    @Test
    fun `a blank query and a query with no match find nothing`() {
        assertEquals(emptyList<City>(), list.search("  "))
        assertEquals(emptyList<City>(), list.search("atlantis"))
    }

    @Test
    fun `search honours its limit`() {
        assertEquals(1, list.search("o", limit = 1).size)
    }

    @Test
    fun `nearest picks the closest city`() {
        // Given a point in the south of Bengaluru
        val city = list.nearest(12.90, 77.60)!!

        assertEquals("Bengaluru", city.name)
        assertEquals("Asia/Kolkata", city.timezone)
    }

    @Test
    fun `nearest can be limited to a country`() {
        // Given a point at London, UK, asked for only Canadian cities
        val city = list.nearest(51.5, -0.12, countryCode = "ca")

        assertEquals("London", city!!.name)
        assertEquals("CA", city.countryCode)
    }

    @Test
    fun `nearest answers none when nothing is within the distance`() {
        assertNull(list.nearest(0.0, 0.0, maxKm = 300.0))
        assertTrue(list.nearest(0.0, 0.0) != null)
    }

    @Test
    fun `nearest on an empty list answers none`() {
        assertNull(CityList(emptyList()).nearest(12.9, 77.6))
    }

    @Test
    fun `the distance between two known cities is about right`() {
        // Bengaluru to Pune is roughly 735 km as the crow flies
        assertEquals(735.0, distanceKm(12.9716, 77.5946, 18.5204, 73.8567), 15.0)
        assertEquals(0.0, distanceKm(12.97, 77.59, 12.97, 77.59), 0.0001)
    }
}
