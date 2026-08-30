package com.lumenlauncher.app.data.di

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.LauncherApps
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLauncherApps(@ApplicationContext context: Context): LauncherApps =
        context.getSystemService(LauncherApps::class.java)
            ?: error("LauncherApps service unavailable on this device")

    @Provides
    @Singleton
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager =
        context.getSystemService(UsageStatsManager::class.java)
            ?: error("UsageStatsManager service unavailable on this device")

    @Provides
    @Singleton
    fun provideAppOpsManager(@ApplicationContext context: Context): AppOpsManager =
        context.getSystemService(AppOpsManager::class.java)
            ?: error("AppOpsManager service unavailable on this device")

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context) = context.contentResolver
}
