package com.facetlauncher.app.data.di

import android.content.Context
import com.facetlauncher.app.data.dailyphotos.DailyPhotoApi
import com.facetlauncher.app.data.dailyphotos.DailyPhotoCache
import com.facetlauncher.app.data.dailyphotos.PeapixApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DailyPhotoBindingModule {
    @Binds
    abstract fun bindDailyPhotoApi(api: PeapixApi): DailyPhotoApi
}

@Module
@InstallIn(SingletonComponent::class)
object DailyPhotoModule {
    @Provides
    @Singleton
    fun provideDailyPhotoCache(@ApplicationContext context: Context, clock: Clock): DailyPhotoCache = DailyPhotoCache(File(context.cacheDir, "daily_photos"), clock)
}
