package com.facetlauncher.app.data.di

import com.facetlauncher.app.data.weather.MetWeatherApi
import com.facetlauncher.app.data.weather.WeatherApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherModule {

    @Binds
    abstract fun bindWeatherApi(api: MetWeatherApi): WeatherApi
}
