package com.facetlauncher.app.data.di

import android.content.Context
import androidx.room.Room
import com.facetlauncher.app.data.local.DefaultFavoriteAppDao
import com.facetlauncher.app.data.local.DockAppDao
import com.facetlauncher.app.data.local.FavoriteAppDao
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.Migrations
import com.facetlauncher.app.data.local.ProfileDao
import com.facetlauncher.app.data.local.ProfileDockAppDao
import com.facetlauncher.app.data.local.WidgetPlacementDao
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
    fun provideFacetDatabase(@ApplicationContext context: Context): FacetDatabase =
        Room.databaseBuilder(context, FacetDatabase::class.java, "facet.db")
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
    fun provideProfileDao(database: FacetDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideFavoriteAppDao(database: FacetDatabase): FavoriteAppDao = database.favoriteAppDao()

    @Provides
    fun provideDockAppDao(database: FacetDatabase): DockAppDao = database.dockAppDao()

    @Provides
    fun provideProfileDockAppDao(database: FacetDatabase): ProfileDockAppDao = database.profileDockAppDao()

    @Provides
    fun provideDefaultFavoriteAppDao(database: FacetDatabase): DefaultFavoriteAppDao = database.defaultFavoriteAppDao()

    @Provides
    fun provideWidgetPlacementDao(database: FacetDatabase): WidgetPlacementDao = database.widgetPlacementDao()
}
