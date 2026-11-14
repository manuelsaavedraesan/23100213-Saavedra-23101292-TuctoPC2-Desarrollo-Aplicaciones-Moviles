
package com.example.diarioespacial.data.repository

import com.example.diarioespacial.data.local.AsteroidEntity
import com.example.diarioespacial.data.remote.AsteroidDto

fun AsteroidDto.toEntity(
    approachDate: String
): AsteroidEntity? {

    val asteroidId = id ?: return null

    val approach = closeApproachData
        ?.firstOrNull { it.date == approachDate }
        ?: closeApproachData?.firstOrNull()

    return AsteroidEntity(
        id = asteroidId,
        name = name ?: "Sin nombre",
        closeApproachDate = approach?.date ?: approachDate,
        diameterMin = estimatedDiameter?.meters?.min ?: 0.0,
        diameterMax = estimatedDiameter?.meters?.max ?: 0.0,
        velocityKmH = approach?.relativeVelocity
            ?.kilometersPerHour?.toDoubleOrNull() ?: 0.0,
        missDistanceKm = approach?.missDistance
            ?.kilometers?.toDoubleOrNull() ?: 0.0,
        isPotentiallyHazardous = isPotentiallyHazardous ?: false
    )
}
