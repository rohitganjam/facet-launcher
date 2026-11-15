package com.facetlauncher.app.data

import android.content.Context
import androidx.room.Room
import com.facetlauncher.app.data.local.FacetDatabase
import java.time.Clock

/** A real [LaunchHistoryRepository] on its own empty in-memory database, so Recents, Most used and search start empty. */
fun testLaunchHistoryRepository(context: Context, appRepository: AppRepository): LaunchHistoryRepository = LaunchHistoryRepository(
    Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build().launchHistoryDao(),
    appRepository,
    Clock.systemDefaultZone(),
)
