package com.lumenlauncher.app.data.di

import android.content.Context
import androidx.room.Room
import com.lumenlauncher.app.data.local.DefaultFavoriteAppDao
import com.lumenlauncher.app.data.local.DockAppDao
import com.lumenlauncher.app.data.local.FavoriteAppDao
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.Migrations
import com.lumenlauncher.app.data.local.ProfileDao
import com.lumenlauncher.app.data.local.WidgetPlacementDao
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
            .addMigrations(*Migrations.ALL)
            // Real installs now hold real user data (profiles, dock/favorite layout, widget
            // placements) — a destructive fallback on *upgrade* would silently wipe all of it the
            // next time the schema version bumps and nobody remembered to add a Migration. Letting
            // Room throw IllegalStateException instead (its default when a required migration is
            // missing) is deliberate: a loud crash caught in dev/QA beats a silent data loss in the
            // field. See Migrations.kt for the process every future version bump must follow.
            // Downgrades (an old build replacing a newer one, e.g. reinstalling a debug build) have
            // no general safe resolution and are dev-only in practice, so those alone still fall
            // back destructively.
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun provideProfileDao(database: LumenDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideFavoriteAppDao(database: LumenDatabase): FavoriteAppDao = database.favoriteAppDao()

    @Provides
    fun provideDockAppDao(database: LumenDatabase): DockAppDao = database.dockAppDao()

    @Provides
    fun provideDefaultFavoriteAppDao(database: LumenDatabase): DefaultFavoriteAppDao = database.defaultFavoriteAppDao()

    @Provides
    fun provideWidgetPlacementDao(database: LumenDatabase): WidgetPlacementDao = database.widgetPlacementDao()
}
