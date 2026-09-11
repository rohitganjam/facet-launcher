package com.lumenlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfileDockAppDaoTest {

    private fun createDatabase(): LumenDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LumenDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the inserted dock app scoped to its profile, ordered by position`() = runTest {
        // Given a profile and an empty per-profile dock table
        val database = createDatabase()
        val profileId = database.profileDao().upsert(ProfileEntity(name = "Profile 1", position = 0))

        // When two dock apps are inserted out of position order
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileId, packageName = "com.example.b", activityName = ".Main", position = 1),
        )
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // Then observing that profile returns them ordered by position
        val result = database.profileDockAppDao().observeForProfile(profileId).first()
        assertEquals(listOf("com.example.a", "com.example.b"), result.map { it.packageName })
    }

    @Test
    fun `dock apps are scoped per profile, not shared`() = runTest {
        // Given two profiles, each with a dock app
        val database = createDatabase()
        val profileOneId = database.profileDao().upsert(ProfileEntity(name = "One", position = 0))
        val profileTwoId = database.profileDao().upsert(ProfileEntity(name = "Two", position = 1))
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileTwoId, packageName = "com.example.b", activityName = ".Main", position = 0),
        )

        // Then observing one profile never returns the other's dock apps
        val resultForProfileOne = database.profileDockAppDao().observeForProfile(profileOneId).first()
        assertEquals(listOf("com.example.a"), resultForProfileOne.map { it.packageName })
    }

    @Test
    fun `deleting a profile cascades to its dock apps`() = runTest {
        // Given a profile with a dock app
        val database = createDatabase()
        val profileId = database.profileDao().upsert(ProfileEntity(name = "Profile 1", position = 0))
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the profile is deleted
        database.profileDao().delete(ProfileEntity(id = profileId, name = "Profile 1", position = 0))

        // Then its dock apps are gone too (foreign key cascade)
        assertTrue(database.profileDockAppDao().observeForProfile(profileId).first().isEmpty())
    }

    @Test
    fun `deleteByPackage removes every profile's entry for that package`() = runTest {
        // Given the same app in two profiles' docks
        val database = createDatabase()
        val profileOneId = database.profileDao().upsert(ProfileEntity(name = "One", position = 0))
        val profileTwoId = database.profileDao().upsert(ProfileEntity(name = "Two", position = 1))
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.profileDockAppDao().upsert(
            ProfileDockAppEntity(profileId = profileTwoId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the package is cleaned up after an uninstall
        database.profileDockAppDao().deleteByPackage("com.example.a")

        // Then it's gone from every profile's dock
        assertTrue(database.profileDockAppDao().observeForProfile(profileOneId).first().isEmpty())
        assertTrue(database.profileDockAppDao().observeForProfile(profileTwoId).first().isEmpty())
    }
}
