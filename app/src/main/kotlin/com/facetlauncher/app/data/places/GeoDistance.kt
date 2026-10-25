package com.facetlauncher.app.data.places

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0088

/** Great-circle distance in kilometres (haversine), good to a fraction of a percent, which is plenty for "has the user moved". */
fun distanceKm(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Double {
    val lat1 = Math.toRadians(latitude1)
    val lat2 = Math.toRadians(latitude2)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(longitude2 - longitude1)
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
}
