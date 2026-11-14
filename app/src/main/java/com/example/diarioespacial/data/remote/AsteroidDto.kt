
package com.example.diarioespacial.data.remote

import com.google.gson.annotations.SerializedName

data class AsteroidFeedResponse(
    @SerializedName("near_earth_objects")
    val nearEarthObjects: Map<String, List<AsteroidDto>>? = null
)

data class AsteroidDto(
    val id: String? = null,
    val name: String? = null,

    @SerializedName("is_potentially_hazardous_asteroid")
    val isPotentiallyHazardous: Boolean? = null,

    @SerializedName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameterDto? = null,

    @SerializedName("close_approach_data")
    val closeApproachData: List<CloseApproachDto>? = null
)

data class EstimatedDiameterDto(
    val meters: DiameterMetersDto? = null
)

data class DiameterMetersDto(
    @SerializedName("estimated_diameter_min")
    val min: Double? = null,

    @SerializedName("estimated_diameter_max")
    val max: Double? = null
)

data class CloseApproachDto(
    @SerializedName("close_approach_date")
    val date: String? = null,

    @SerializedName("relative_velocity")
    val relativeVelocity: RelativeVelocityDto? = null,

    @SerializedName("miss_distance")
    val missDistance: MissDistanceDto? = null
)

data class RelativeVelocityDto(
    @SerializedName("kilometers_per_hour")
    val kilometersPerHour: String? = null
)

data class MissDistanceDto(
    val kilometers: String? = null
)
