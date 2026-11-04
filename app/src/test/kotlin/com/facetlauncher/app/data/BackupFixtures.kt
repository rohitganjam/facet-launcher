package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.BackupSettings

/** The fewest settings a backup needs, for tests that only care about other parts of a bundle. */
fun minimalBackupSettings() = BackupSettings(
    use24HourTime = false, dockDisplayMode = "ICONS", drawerPresentation = "LIST", drawerGridSize = "FIVE_BY_SIX",
    drawerListItemSize = "REGULAR", drawerOpacity = 0.6f, notificationDotsEnabled = true, notificationBadgeStyle = "DOT",
    showDrawerIcons = true, showDrawerLabels = true, searchBarPosition = "TOP", activeFacetIndex = null,
    showAllDayEvents = true, searchContactsEnabled = false, themeMode = "SYSTEM", accentFromSystem = true,
    customAccentSwatch = null, wallpaperAccentRole = "PRIMARY", iconRenderMode = "SYSTEM_DEFAULT", launcherFontOption = "SYSTEM",
    appLabelColorOption = "THEME", appRowPosition = "LEFT", appRowPresentation = "ICON_AND_TEXT",
    listContentMode = "FAVORITES", appsToShowCount = 5, clockTemplateId = "LIGHT_STACK",
    clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME", clockShowMeridiem = false,
)
