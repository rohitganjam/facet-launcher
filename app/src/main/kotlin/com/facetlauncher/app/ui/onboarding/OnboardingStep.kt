package com.facetlauncher.app.ui.onboarding

/**
 * The four onboarding steps, in order — see `ONBOARDING_FLOW.md` §3. Reaching the end of
 * [NOTIFICATIONS] (via "Next" or "Skip") completes onboarding directly ([OnboardingScreen]'s [onFinish])
 * rather than advancing to a further step — the "make Facet your home screen" prompt lives on the
 * real Home screen instead, shown once there right after onboarding completes (see
 * [SetDefaultLauncherSheet]'s own doc for why).
 */
enum class OnboardingStep {
    INTRO,
    FACETS,
    HOME_SETUP,
    DRAWER_SETUP,
    NOTIFICATIONS,
}
