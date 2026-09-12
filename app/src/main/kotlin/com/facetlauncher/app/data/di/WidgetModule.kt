package com.facetlauncher.app.data.di

import android.appwidget.AppWidgetManager
import android.content.Context
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {

    @Provides
    @Singleton
    fun provideAppWidgetManager(@ApplicationContext context: Context): AppWidgetManager =
        AppWidgetManager.getInstance(context)

    @Provides
    @Singleton
    fun provideLauncherAppWidgetHost(@ApplicationContext context: Context): LauncherAppWidgetHost =
        LauncherAppWidgetHost(context)
}
