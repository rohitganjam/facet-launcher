package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.clockWidgetConfig

/**
 * The clock's settings in force for [facet]: its clock page's own config, or the built-in defaults while the page has none (a new
 * facet's clock, or a facet with no clock). [settings] is unused for now but stays in the signature for the callers that pass it.
 */
@Suppress("UNUSED_PARAMETER")
fun effectiveClockConfig(facet: Facet?, settings: LauncherSettings): ClockWidgetConfig = facet?.clockWidgetConfig() ?: ClockWidgetConfig()
