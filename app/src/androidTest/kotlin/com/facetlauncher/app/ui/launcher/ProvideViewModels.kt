package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Makes `hiltViewModel()` below it return [viewModels] (hand-built fakes) instead of creating real ones.
 * Anything not listed is still created through the surrounding owner's own factory.
 */
@Composable
internal fun ProvideViewModels(vararg viewModels: ViewModel, content: @Composable () -> Unit) {
    val parent = checkNotNull(LocalViewModelStoreOwner.current)
    val owner = remember(parent) { StoreOwner(parent, viewModels.toList()) }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

private class StoreOwner(parent: ViewModelStoreOwner, viewModels: List<ViewModel>) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore = ViewModelStore()
    private val fallback = parent as? HasDefaultViewModelProviderFactory

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = checkNotNull(fallback).defaultViewModelProviderFactory
    override val defaultViewModelCreationExtras: CreationExtras
        get() = checkNotNull(fallback).defaultViewModelCreationExtras

    init {
        // A provider fills the store under the key a later `get` of the same class looks up.
        val byClass: Map<Class<out ViewModel>, ViewModel> = viewModels.associateBy { it.javaClass }
        val provider = ViewModelProvider.create(
            viewModelStore,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = modelClass.cast(byClass.getValue(modelClass))!!
            },
        )
        byClass.keys.forEach { provider[it] }
    }
}
