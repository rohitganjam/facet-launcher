package com.facetlauncher.app.data

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * [DefaultAppRepository.getDefaultAppPackages]'s primary path — [android.content.pm.PackageManager.resolveActivity]
 * finding a single registered candidate — is what's covered here, the same way
 * [ContactRepositoryTest] shadows [android.content.pm.PackageManager] for its own resolution
 * checks. The "resolveActivity is ambiguous, fall back to queryIntentActivities' first result"
 * branch isn't exercised here: Robolectric's `ShadowApplicationPackageManager.resolveActivity`
 * (verified by disassembling `shadows-framework-4.16.1.jar`) delegates straight to
 * `queryIntentActivities` and returns a non-null result whenever *any* candidate is registered —
 * it never reproduces real Android's `MATCH_DEFAULT_ONLY`-is-ambiguous-so-return-null behavior.
 * That branch is real, correct behavior on-device/on-emulator (only real `PackageManager`
 * enforces the distinction) and is exercised end-to-end there by
 * `AppearanceSettingsScreenTest`'s instrumented run instead.
 */
@RunWith(RobolectricTestRunner::class)
class DefaultAppRepositoryTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repository = DefaultAppRepository(context)

    private fun resolveInfoFor(packageName: String) = ResolveInfo().apply {
        activityInfo = ActivityInfo().apply {
            this.packageName = packageName
            name = ".Main"
        }
    }

    @Test
    fun `a registered default browser is resolved`() = runTest {
        // Given a default browser registered for the Browser category's own intent
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        shadowOf(context.packageManager).addResolveInfoForIntent(browserIntent, resolveInfoFor("com.example.browser"))

        // Then it's the only package resolved
        assertEquals(listOf("com.example.browser"), repository.getDefaultAppPackages())
    }

    @Test
    fun `no registered handler for any category yields an empty list`() = runTest {
        // Given no ResolveInfo registered anywhere
        assertEquals(emptyList<String>(), repository.getDefaultAppPackages())
    }

    @Test
    fun `multiple resolved categories come back in category order`() = runTest {
        // Given handlers registered for Camera and Phone (out of declaration order), plus Browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val dialIntent = Intent(Intent.ACTION_DIAL)
        shadowOf(context.packageManager).apply {
            addResolveInfoForIntent(dialIntent, resolveInfoFor("com.example.dialer"))
            addResolveInfoForIntent(cameraIntent, resolveInfoFor("com.example.camera"))
            addResolveInfoForIntent(browserIntent, resolveInfoFor("com.example.browser"))
        }

        // Then the result follows the category order (Browser, Messaging, Camera, Mail, Phone),
        // not registration order
        assertEquals(
            listOf("com.example.browser", "com.example.camera", "com.example.dialer"),
            repository.getDefaultAppPackages(),
        )
    }

    @Test
    fun `the same package resolved for two categories is only returned once`() = runTest {
        // Given one app registered as the handler for both Browser and Mail
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        shadowOf(context.packageManager).apply {
            addResolveInfoForIntent(browserIntent, resolveInfoFor("com.example.superapp"))
            addResolveInfoForIntent(mailIntent, resolveInfoFor("com.example.superapp"))
        }

        assertEquals(listOf("com.example.superapp"), repository.getDefaultAppPackages())
    }
}
