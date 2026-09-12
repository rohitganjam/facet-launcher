package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.facetlauncher.app.data.model.SettingsSearchEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class CatalogEntry(val id: String, val label: String, val action: String, val keywords: List<String>)

/** Curated system Settings deep links for the Drawer's "Search settings" section — every [CatalogEntry.action] is a stable AOSP `Settings.ACTION_*` constant, not an OEM-specific one. */
private val SETTINGS_CATALOG = listOf(
    CatalogEntry("wifi", "Wi-Fi", Settings.ACTION_WIFI_SETTINGS, listOf("wifi", "wi-fi", "wireless", "internet", "network")),
    CatalogEntry("bluetooth", "Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS, listOf("bluetooth", "pair", "pairing")),
    CatalogEntry("airplane_mode", "Airplane mode", Settings.ACTION_AIRPLANE_MODE_SETTINGS, listOf("airplane", "flight mode")),
    CatalogEntry("display", "Display", Settings.ACTION_DISPLAY_SETTINGS, listOf("display", "brightness", "screen", "font size", "dark theme")),
    CatalogEntry("sound", "Sound & vibration", Settings.ACTION_SOUND_SETTINGS, listOf("sound", "volume", "vibration", "ringtone", "media")),
    CatalogEntry("battery_saver", "Battery saver", Settings.ACTION_BATTERY_SAVER_SETTINGS, listOf("battery", "power saver", "power")),
    CatalogEntry("storage", "Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS, listOf("storage", "space")),
    CatalogEntry("apps", "Apps", Settings.ACTION_APPLICATION_SETTINGS, listOf("apps", "applications", "app info", "manage apps")),
    CatalogEntry("location", "Location", Settings.ACTION_LOCATION_SOURCE_SETTINGS, listOf("location", "gps")),
    CatalogEntry("security", "Security", Settings.ACTION_SECURITY_SETTINGS, listOf("security", "lock screen", "fingerprint", "password", "pin")),
    CatalogEntry("accessibility", "Accessibility", Settings.ACTION_ACCESSIBILITY_SETTINGS, listOf("accessibility", "talkback", "magnification")),
    CatalogEntry("date_time", "Date & time", Settings.ACTION_DATE_SETTINGS, listOf("date", "time", "timezone", "clock")),
    CatalogEntry("language", "Languages & input", Settings.ACTION_LOCALE_SETTINGS, listOf("language", "languages", "keyboard", "input")),
    CatalogEntry("accounts", "Accounts", Settings.ACTION_SYNC_SETTINGS, listOf("accounts", "sync", "account")),
    CatalogEntry("nfc", "NFC", Settings.ACTION_NFC_SETTINGS, listOf("nfc", "tap and pay")),
)

/**
 * Wraps stock Android's own Settings deep-link intents (no dangerous permission needed) for the
 * Drawer's "Search settings" toggle — typing e.g. "wifi" surfaces a result that jumps straight
 * into that system Settings sub-screen, the same idea as [ContactRepository] wrapping the
 * Contacts Provider for the drawer's contacts section.
 */
@Singleton
class SystemSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Catalog entries whose label/keywords contain [query], filtered to those this device can actually resolve — some OEMs drop or rename standard Settings screens (same [android.content.pm.PackageManager.resolveActivity] guard [ContactRepository] uses for its own dynamic connections). */
    suspend fun search(query: String): List<SettingsSearchEntry> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val packageManager = context.packageManager
        SETTINGS_CATALOG
            .filter { entry -> entry.keywords.any { it.contains(query, ignoreCase = true) } }
            .filter { entry -> packageManager.resolveActivity(Intent(entry.action), 0) != null }
            .map { entry -> SettingsSearchEntry(id = entry.id, label = entry.label, action = entry.action) }
    }
}
