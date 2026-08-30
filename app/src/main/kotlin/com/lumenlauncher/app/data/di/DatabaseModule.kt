package com.lumenlauncher.app.data.di

import android.content.Context
import androidx.room.Room
import com.lumenlauncher.app.data.local.DefaultFavoriteAppDao
import com.lumenlauncher.app.data.local.DockAppDao
import com.lumenlauncher.app.data.local.FavoriteAppDao
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLumenDatabase(@ApplicationContext context: Context): LumenDatabase =
        Room.databaseBuilder(context, LumenDatabase::class.java, "lumen.db")
            // No released install base yet and no migration precedent in this codebase —
            // destructive fallback is the right, minimal move for a pre-release schema bump.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProfileDao(database: LumenDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideFavoriteAppDao(database: LumenDatabase): FavoriteAppDao = database.favoriteAppDao()

    @Provides
    fun provideDockAppDao(database: LumenDatabase): DockAppDao = database.dockAppDao()

    @Provides
    fun provideDefaultFavoriteAppDao(database: LumenDatabase): DefaultFavoriteAppDao = database.defaultFavoriteAppDao()
}
