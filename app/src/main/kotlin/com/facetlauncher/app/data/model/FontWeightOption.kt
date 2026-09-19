package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * A font weight choice for body/label text — deliberately capped at [SEMI_BOLD], no Bold/ExtraBold/
 * Black, since this governs regular text (Calendar events, Home's app list/Dock/Drawer labels, and —
 * via `ui/theme/Type.kt`'s `facetTypography` — every `bodyLarge`/`bodyMedium`/`bodySmall` consumer
 * app-wide, including Settings' own rows), never headlines/titles/labels. Lives in `data/model` so
 * it can be a real typed field on [LauncherSettings]/`FacetEntity` — the actual
 * [androidx.compose.ui.text.font.FontWeight] each option resolves to stays in the UI layer
 * (`ui/theme/Type.kt`'s `FontWeightOption.resolve()`).
 */
enum class FontWeightOption(@param:StringRes val displayNameRes: Int) {
    THIN(R.string.font_weight_thin),
    EXTRA_LIGHT(R.string.font_weight_extra_light),
    LIGHT(R.string.font_weight_light),
    REGULAR(R.string.font_weight_regular),
    MEDIUM(R.string.font_weight_medium),
    SEMI_BOLD(R.string.font_weight_semi_bold),
}
