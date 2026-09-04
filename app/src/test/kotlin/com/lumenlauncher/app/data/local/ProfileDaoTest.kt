package com.lumenlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lumenlauncher.app.data.model.ListContentMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Real Room-backed (not a fake in-memory [ProfileDao]) — these actually exercise [Converters],
 * which a fake DAO never touches. That gap is exactly how a real bug shipped undetected: the
 * [ListContentMode]-to-[String] converters were declared non-null in/out for a *nullable* column,
 * which Room silently can't bind — every write landed as SQL NULL regardless of what was passed
 * in, so "Override for this profile" on the App list content card never actually stuck.
 * `ProfileRepositoryTest`'s fake in-memory DAO round-tripped fine (it just stores the Kotlin
 * object directly) and so never caught it; this suite would have.
 */
@RunWith(RobolectricTestRunner::class)
class ProfileDaoTest {

    private fun createDao(): ProfileDao {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LumenDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return database.profileDao()
    }

    @Test
    fun `a non-null listContentMode round-trips through Room, not just an in-memory copy`() = runTest {
        // Given a stored profile with no override
        val dao = createDao()
        val id = dao.upsert(ProfileEntity(name = "Profile 1", position = 0))

        // When it's upserted again with a real (non-default) mode
        dao.upsert(ProfileEntity(id = id, name = "Profile 1", position = 0, listContentMode = ListContentMode.MOST_USED))

        // Then a fresh read (forcing the value through Converters.toListContentMode) reflects it
        val result = dao.observeAll().first().single()
        assertEquals(ListContentMode.MOST_USED, result.listContentMode)
    }

    @Test
    fun `toggling overrideApps flag round-trips`() = runTest {
        // Given a profile inheriting global apps
        val dao = createDao()
        val id = dao.upsert(ProfileEntity(name = "Profile 1", position = 0, overrideApps = false))

        // When it's switched to override
        dao.upsert(ProfileEntity(id = id, name = "Profile 1", position = 0, overrideApps = true))

        // Then it reflects the new flag
        val result = dao.observeAll().first().single()
        assertEquals(true, result.overrideApps)
    }

    @Test
    fun `every ListContentMode value round-trips`() = runTest {
        val dao = createDao()
        val id = dao.upsert(ProfileEntity(name = "Profile 1", position = 0))

        for (mode in ListContentMode.entries) {
            dao.upsert(ProfileEntity(id = id, name = "Profile 1", position = 0, listContentMode = mode))
            assertEquals(mode, dao.observeAll().first().single().listContentMode)
        }
    }
}
