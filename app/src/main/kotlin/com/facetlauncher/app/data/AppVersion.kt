package com.facetlauncher.app.data

import com.facetlauncher.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/** The running build's version name, behind a class so tests can supply their own. */
@Singleton
open class AppVersion @Inject constructor() {
    open val name: String = BuildConfig.VERSION_NAME
}
