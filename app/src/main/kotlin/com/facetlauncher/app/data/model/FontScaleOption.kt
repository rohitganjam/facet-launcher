package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/**
 * Settings → Appearance → "Text size" — a global scale multiplier applied to every text role
 * app-wide (`ui/theme/Type.kt`'s `facetTypography`) except the clock, which has its own independent
 * size control (`clockScale`). Named stops, not a raw float — same reasoning as [FontWeightOption]:
 * a stepped `Slider` walks [entries] by index rather than dragging a continuous value.
 */
enum class FontScaleOption(val scale: Float, @param:StringRes val displayNameRes: Int) {
    SMALL(0.85f, R.string.font_scale_small),
    DEFAULT(1.0f, R.string.font_scale_default),
    LARGE(1.15f, R.string.font_scale_large),
    EXTRA_LARGE(1.3f, R.string.font_scale_extra_large),
    HUGE(1.45f, R.string.font_scale_huge),
}
