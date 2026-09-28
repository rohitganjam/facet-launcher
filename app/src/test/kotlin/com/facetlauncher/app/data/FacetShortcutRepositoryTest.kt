package com.facetlauncher.app.data

import android.content.Context
import android.content.pm.ShortcutManager
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.parseFacetIdFromDeepLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FacetShortcutRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val shortcutManager = context.getSystemService(ShortcutManager::class.java)
    private val repository = FacetShortcutRepository(context)

    @Test
    fun `short and long labels read 'Switch to' the facet's name, while the shortcut id and trigger uri encode its id`() {
        // Given a facet
        val facet = FacetEntity(id = 7, name = "Work", position = 0)

        // When its shortcut is published
        repository.syncShortcuts(listOf(facet))

        // Then the label the user/OS picker sees names the action and the facet, while what
        // actually triggers it (the shortcut's own id, and its launch intent's Uri) both carry the
        // numeric id instead
        val shortcut = shortcutManager.dynamicShortcuts.single()
        assertEquals("Switch to Work", shortcut.shortLabel)
        assertEquals("Switch to Work", shortcut.longLabel)
        assertEquals("facet_7", shortcut.id)
        assertFalse(shortcut.id.contains("Work"))
        assertEquals(7L, parseFacetIdFromDeepLink(shortcut.intent!!.data!!))
    }

    @Test
    fun `renaming a facet updates the label on the next sync without changing the shortcut id or trigger uri`() {
        // Given a published shortcut
        val facet = FacetEntity(id = 3, name = "Focus", position = 0)
        repository.syncShortcuts(listOf(facet))
        val originalUri = shortcutManager.dynamicShortcuts.single().intent!!.data

        // When that facet is renamed and re-synced (not deleted and recreated)
        repository.syncShortcuts(listOf(facet.copy(name = "Deep Focus")))

        // Then the visible label changes, but the trigger key (id + Uri) doesn't — an
        // already-configured automation rule keeps pointing at the same shortcut
        val updated = shortcutManager.dynamicShortcuts.single()
        assertEquals("Switch to Deep Focus", updated.shortLabel)
        assertEquals("facet_3", updated.id)
        assertEquals(originalUri, updated.intent!!.data)
    }

    @Test
    fun `sync fully replaces the previous shortcut set rather than merging into it`() {
        // Given two published facet shortcuts
        val a = FacetEntity(id = 1, name = "A", position = 0)
        val b = FacetEntity(id = 2, name = "B", position = 1)
        repository.syncShortcuts(listOf(a, b))

        // When syncing again with only one of them (the other was deleted)
        repository.syncShortcuts(listOf(a))

        // Then only that one remains — no stale shortcut left over from the previous sync
        assertEquals(listOf("facet_1"), shortcutManager.dynamicShortcuts.map { it.id })
    }
}
