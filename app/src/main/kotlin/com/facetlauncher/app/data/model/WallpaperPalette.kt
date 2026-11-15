package com.facetlauncher.app.data.model

/** The wallpaper's Material You accent roles, as ARGB, for a light and for a dark theme. */
data class WallpaperPalette(val light: Roles, val dark: Roles) {
    data class Roles(val primary: Int, val secondary: Int, val tertiary: Int) {
        fun of(role: WallpaperAccentRole): Int = when (role) {
            WallpaperAccentRole.PRIMARY -> primary
            WallpaperAccentRole.SECONDARY -> secondary
            WallpaperAccentRole.TERTIARY -> tertiary
        }
    }
}
