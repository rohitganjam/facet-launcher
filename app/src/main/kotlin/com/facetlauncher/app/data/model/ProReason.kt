package com.facetlauncher.app.data.model

/** Why a free user is being sent to the upgrade sheet; picks the line it shows. */
enum class ProReason {
    /** A rule uses a Pro automation trigger. */
    TRIGGER,

    /** The free user already has [AutomationLimits.FREE_MAX_RULES] rules. */
    RULE_LIMIT,

    /** The free user already has [FacetLimits.FREE_MAX_FACETS] facets and tried to add one. */
    FACET_LIMIT,

    /** The user tapped a facet that is disabled on the free plan. */
    FACET_LOCKED,

    /** The user opened Facet Pro from Settings, not from a limit. */
    ABOUT,
}
