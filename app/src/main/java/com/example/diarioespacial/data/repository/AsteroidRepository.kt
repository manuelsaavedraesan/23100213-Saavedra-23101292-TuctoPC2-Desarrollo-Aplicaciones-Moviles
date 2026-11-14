
package com.example.diarioespacial.data.repository

import com.example.diarioespacial.data.local.AsteroidDao
import com.example.diarioespacial.data.local.AsteroidEntity
import com.example.diarioespacial.data.remote.NasaApiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AsteroidRepository(
    private val api: NasaApiService,
    private val dao: AsteroidDao,
    private val apiKey: String
) {

    // Lista completa desde Room
    val asteroids: Flow<List<AsteroidEntity>> =
        dao.getAllAsteroids()

    // Solo asteroides peligrosos
    val hazardousAsteroids: Flow<List<AsteroidEntity>> =
        dao.getHazardousAsteroids()

    // Asteroides favoritos
    val favoriteAsteroids: Flow<List<AsteroidEntity>> =
        dao.getFavoriteAsteroids()

    // Obtener detalle
    fun getAsteroidById(
        id: String
    ): Flow<AsteroidEntity?> {
        return dao.getAsteroidById(id)
    }

    // Consultar NASA y guardar en Room
    suspend fun refreshAsteroids() {

        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)

        val response = api.getAsteroids(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            apiKey = apiKey
        )

        val asteroidsFromApi = response.nearEarthObjects
            ?.flatMap { (date, list) ->
                list.mapNotNull { asteroid ->
                    asteroid.toEntity(date)
                }
            }
            ?: emptyList()

        // Conservar favoritos existentes
        val existingFavorites = dao.getFavoritesOnce()
            .map { it.id }
            .toSet()

        val entities = asteroidsFromApi.map { asteroid ->
            asteroid.copy(
                isFavorite = asteroid.id in existingFavorites
            )
        }

        dao.insertAsteroids(entities)
    }

    // Marcar o quitar favorito
    suspend fun updateFavorite(
        id: String,
        favorite: Boolean
    ) {
        dao.updateFavorite(id, favorite)
    }
}
