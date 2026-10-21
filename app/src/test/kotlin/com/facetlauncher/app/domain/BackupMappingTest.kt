package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupMappingTest {

    private val facet = Facet(name = "Work", position = 0)

    @Test
    fun `a facet's landscape row position and column alignment survive a backup round trip`() {
        // Given a facet with landscape values for the two list alignments portrait also backs up
        val original = facet.copy(landscapeAppRowPosition = AppRowPosition.CENTER, landscapeAppListColumnAlignment = AppListColumnAlignment.MIRRORED)

        // When it goes out and comes back
        val restored = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        // Then the landscape values are kept
        assertEquals(AppRowPosition.CENTER, restored.landscapeAppRowPosition)
        assertEquals(AppListColumnAlignment.MIRRORED, restored.landscapeAppListColumnAlignment)
    }

    @Test
    fun `a facet without landscape values restores with none`() {
        val restored = facet.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        assertNull(restored.landscapeAppRowPosition)
        assertNull(restored.landscapeAppListColumnAlignment)
    }

    @Test
    fun `an unrecognized landscape name in a backup is just not set`() {
        val backup = facet.toBackupFacet(favorites = emptyList(), dockApps = emptyList())
            .copy(landscapeAppRowPosition = "NOT_A_REAL_VALUE", landscapeAppListColumnAlignment = "ALSO_NOT_REAL")

        val restored = backup.toFacet()

        assertNull(restored.landscapeAppRowPosition)
        assertNull(restored.landscapeAppListColumnAlignment)
    }
}
