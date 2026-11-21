package com.facetlauncher.app.ui.navigation

import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.ProReason

object FacetDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DOCK_PICKER = "dockPicker?facetId={facetId}"
    const val FACET_MANAGE = "facetManage"
    const val CLOCK_STACK = "clockStack?facetId={facetId}"
    const val FACET_AUTOMATION = "facetAutomation"
    const val FACET_PRO = "facetPro/{reason}"
    const val FACET_SETTINGS = "facetSettings/{facetId}"
    const val FAVORITES_PICKER = "favoritesPicker?facetId={facetId}"
    const val PERMISSIONS = "permissions"
    const val GESTURES = "gestures"
    const val ACCESSIBILITY_ACCESS_EXPLANATION = "accessibilityAccessExplanation"
    const val NOTIFICATION_SETTINGS = "notificationSettings"
    const val FOLDERS_SETTINGS = "foldersSettings"
    const val FOLDER_DETAIL = "folderDetail/{folderId}"
    const val FOLDER_APP_PICKER = "folderAppPicker/{folderId}"
    const val NOTIFICATION_ACCESS_EXPLANATION = "notificationAccessExplanation"
    const val CLOCK_ACCESSORIES = "clockAccessories?facetId={facetId}"
    const val UNITS_AND_LOCATION = "unitsAndLocation"
    const val WEATHER_LOCATION = "weatherLocation"
    const val FACET_CLOCK_STYLE_GALLERY = "facetClockStyleGallery/{facetId}"
    const val BACKUP_RESTORE = "backupRestore"
    const val APPEARANCE_SETTINGS = "appearanceSettings"
    const val THEMES_AND_COLORS = "themesAndColors"
    const val FONTS = "fonts"
    const val ICONS = "icons"
    const val CUSTOM_ICONS = "customIcons"
    const val WALLPAPER = "wallpaper"
    const val WALLPAPER_CAPTURE = "wallpaperCapture"
    const val DOCK_SETTINGS = "dockSettings?facetId={facetId}"
    const val HOME_APPS_LIST_SETTINGS = "homeAppsListSettings?facetId={facetId}"
    const val APP_DRAWER_SETTINGS = "appDrawerSettings"
    const val WHATS_NEW = "whats_new"
    const val LICENSES = "licenses"
    const val LICENSE_TEXT = "licenseText/{licenseId}"

    fun facetClockStyleGallery(facetId: Long) = "facetClockStyleGallery/$facetId"

    fun facetSettings(facetId: Long) = "facetSettings/$facetId"

    fun facetPro(reason: ProReason) = "facetPro/${reason.name}"

    fun folderDetail(folderId: Long) = "folderDetail/$folderId"
    fun licenseText(licenseId: String) = "licenseText/$licenseId"

    fun folderAppPicker(folderId: Long) = "folderAppPicker/$folderId"

    /** One facet's own Favorites picker. */
    fun favoritesPicker(facetId: Long) = "favoritesPicker?facetId=$facetId"

    /** One facet's own Dock picker. */
    fun dockPicker(facetId: Long) = "dockPicker?facetId=$facetId"

    /** One facet's own Apps list settings. */
    fun homeAppsListSettings(facetId: Long) = "homeAppsListSettings?facetId=$facetId"

    /** One facet's own Dock settings. */
    fun dockSettings(facetId: Long) = "dockSettings?facetId=$facetId"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the launcher-wide Clock accessories. */
    /** The widget stack of [facetId]; omitting it (or passing [NO_ACTIVE_FACET_ID]) opens the active facet's. */
    fun clockStack(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "clockStack?facetId=$facetId" else "clockStack"

    fun clockAccessories(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "clockAccessories?facetId=$facetId" else "clockAccessories"
}
