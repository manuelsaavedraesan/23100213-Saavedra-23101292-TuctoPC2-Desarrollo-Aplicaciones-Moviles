
package com.example.diarioespacial.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asteroids")
data class AsteroidEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val closeApproachDate: String,

    val diameterMin: Double,

    val diameterMax: Double,

    val velocityKmH: Double,

    val missDistanceKm: Double,

    val isPotentiallyHazardous: Boolean,

    val isFavorite: Boolean = false
)
