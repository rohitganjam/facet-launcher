package com.lumenlauncher.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** F6's `READ_CONTACTS` grant check — a normal runtime permission, requested from Settings' "Search contacts" toggle. */
@Singleton
class ContactPermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
}
