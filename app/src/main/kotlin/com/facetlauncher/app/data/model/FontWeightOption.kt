package com.facetlauncher.app.data.model

/**
 * A font weight choice for body/label text — deliberately capped at [SEMI_BOLD], no Bold/ExtraBold/
 * Black, since this governs regular text (Calendar events, Home's app list/Dock/Drawer labels),
 * never headlines. Lives in `data/model` so it can be a real typed field on [LauncherSettings]/
 * `FacetEntity` — the actual [androidx.compose.ui.text.font.FontWeight] each option resolves to
 * stays in the UI layer (`ui/theme/Type.kt`'s `FontWeightOption.resolve()`).
 */
enum class FontWeightOption(val displayName: String) {
    THIN("Thin"),
    EXTRA_LIGHT("Extra Light"),
    LIGHT("Light"),
    REGULAR("Regular"),
    MEDIUM("Medium"),
    SEMI_BOLD("Semi Bold"),
}
