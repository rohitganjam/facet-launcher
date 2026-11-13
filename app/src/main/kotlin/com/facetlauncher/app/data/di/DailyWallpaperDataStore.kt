package com.facetlauncher.app.data.di

import javax.inject.Qualifier

/** The `facet_daily_wallpaper` DataStore: the daily wallpaper's settings and progress, kept out of `facet_settings` and backups. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DailyWallpaperDataStore
