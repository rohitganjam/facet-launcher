package com.facetlauncher.app.data.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupWidgetPlacementTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `a widget in an older backup has no landscape order`() {
        // Given a widget entry written before landscape order existed
        val old = """{"providerPackageName":"com.example","providerClassName":".W","row":1,"col":2,"colSpan":2,"rowSpan":1,"profile":"PERSONAL"}"""

        // When it is read
        val placement = json.decodeFromString<BackupWidgetPlacement>(old)

        // Then it imports with no order, so landscape follows portrait's reading order
        assertNull(placement.landscapeOrder)
        assertEquals(2, placement.colSpan)
    }

    @Test
    fun `a landscape order survives a write and read`() {
        val original = BackupWidgetPlacement("com.example", ".W", row = 0, col = 0, colSpan = 1, rowSpan = 1, landscapeOrder = 5)

        val restored = json.decodeFromString<BackupWidgetPlacement>(json.encodeToString(original))

        assertEquals(5, restored.landscapeOrder)
    }
}
