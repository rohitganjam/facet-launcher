package com.facetlauncher.app.debug

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/** An empty Hilt-aware activity that Compose tests launch, so `hiltViewModel()` has a Hilt component to build from. */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
