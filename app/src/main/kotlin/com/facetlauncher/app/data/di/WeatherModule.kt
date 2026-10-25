package com.facetlauncher.app.data.di

import com.facetlauncher.app.data.places.AndroidPlaceGeocoder
import com.facetlauncher.app.data.places.PlaceGeocoder
import com.facetlauncher.app.data.weather.MetWeatherApi
import com.facetlauncher.app.data.weather.RoomWeatherStore
import com.facetlauncher.app.data.weather.WeatherApi
import com.facetlauncher.app.data.weather.WeatherStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherModule {

    @Binds
    abstract fun bindWeatherApi(api: MetWeatherApi): WeatherApi

    @Binds
    abstract fun bindWeatherStore(store: RoomWeatherStore): WeatherStore

    @Binds
    abstract fun bindPlaceGeocoder(geocoder: AndroidPlaceGeocoder): PlaceGeocoder
}
