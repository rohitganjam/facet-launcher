package com.facetlauncher.app.data.local

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
class FavoriteAppDaoTest {

    private fun createDatabase(): FacetDatabase =
        Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Test
    fun `insert then observe returns the inserted favorite scoped to its profile`() = runTest {
        // Given a profile and an empty favorites table
        val database = createDatabase()
        val profileId = database.profileDao().upsert(ProfileEntity(name = "Profile 1", position = 0))

        // When a favorite is inserted for that profile
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // Then observing that profile returns exactly that favorite
        val result = database.favoriteAppDao().observeForProfile(profileId).first()
        assertEquals(1, result.size)
        assertEquals("com.example.a", result.first().packageName)
    }

    @Test
    fun `favorites are scoped per profile, not shared`() = runTest {
        // Given two profiles, each with a favorite
        val database = createDatabase()
        val profileOneId = database.profileDao().upsert(ProfileEntity(name = "One", position = 0))
        val profileTwoId = database.profileDao().upsert(ProfileEntity(name = "Two", position = 1))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileTwoId, packageName = "com.example.b", activityName = ".Main", position = 0),
        )

        // Then observing one profile never returns the other's favorites
        val resultForProfileOne = database.favoriteAppDao().observeForProfile(profileOneId).first()
        assertEquals(listOf("com.example.a"), resultForProfileOne.map { it.packageName })
    }

    @Test
    fun `deleting a profile cascades to its favorites`() = runTest {
        // Given a profile with a favorite
        val database = createDatabase()
        val profileId = database.profileDao().upsert(ProfileEntity(name = "Profile 1", position = 0))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When the profile is deleted
        database.profileDao().delete(ProfileEntity(id = profileId, name = "Profile 1", position = 0))

        // Then its favorites are gone too (foreign key cascade)
        assertTrue(database.favoriteAppDao().observeForProfile(profileId).first().isEmpty())
    }

    @Test
    fun `deleteByComponent removes only the matching profile's entry`() = runTest {
        // Given the same app favorited under two different profiles
        val database = createDatabase()
        val profileOneId = database.profileDao().upsert(ProfileEntity(name = "One", position = 0))
        val profileTwoId = database.profileDao().upsert(ProfileEntity(name = "Two", position = 1))
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileOneId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )
        database.favoriteAppDao().upsert(
            FavoriteAppEntity(profileId = profileTwoId, packageName = "com.example.a", activityName = ".Main", position = 0),
        )

        // When deleting it by component for just profile one
        database.favoriteAppDao().deleteByComponent(profileOneId, "com.example.a", ".Main")

        // Then only profile one's entry is gone
        assertTrue(database.favoriteAppDao().observeForProfile(profileOneId).first().isEmpty())
        assertEquals(1, database.favoriteAppDao().observeForProfile(profileTwoId).first().size)
    }
}
