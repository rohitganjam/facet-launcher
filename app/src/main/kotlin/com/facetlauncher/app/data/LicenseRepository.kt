package com.facetlauncher.app.data

import android.content.Context
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.LicenseInfo
import com.facetlauncher.app.data.model.LicenseSection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LICENSES_DIR = "licenses"

/** Reads the licence texts bundled under `assets/licenses/`, unmodified as published. */
@Singleton
open class LicenseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    /** The texts of [info], in order. A file that can't be read is skipped. */
    open suspend fun sections(info: LicenseInfo): List<LicenseSection> = withContext(ioDispatcher) {
        info.files.mapNotNull { file ->
            runCatching { context.assets.open("$LICENSES_DIR/${file.asset}").bufferedReader().use { it.readText() } }
                .getOrNull()
                ?.let { LicenseSection(file.heading, it.trim()) }
        }
    }
}
