package com.facetlauncher.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/**
 * The app's standard screen-transition timing — every full-screen slide/fade (App Drawer,
 * onboarding's step transitions, `FacetNavHost`'s destination transitions, the drawer's own
 * settle animation) uses this exact duration/easing, previously hand-retyped identically in each
 * of those files rather than shared — see chat history.
 */
val FacetTransitionEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
const val FACET_TRANSITION_DURATION_MS = 340
