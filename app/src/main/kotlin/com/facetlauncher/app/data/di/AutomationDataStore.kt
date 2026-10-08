package com.facetlauncher.app.data.di

import javax.inject.Qualifier

/** The `facet_automation` DataStore — separate from `facet_settings` so it stays out of backups and `LauncherSettings`. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AutomationDataStore
